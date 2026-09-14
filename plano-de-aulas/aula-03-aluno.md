# Material do Aluno — Aula 3: Arquitetura monolítica (crescendo o monólito com juízo)

> **Tempo de leitura:** ~15 min. Hoje a API de Câmbio ganha dois domínios novos: **Cotação** (quanto vale 1 USD ou 1 EUR em reais, agora) e **Ordem** (o pedido de compra que calcula `valorTotalOperacao` e devolve o comprovante). Os três domínios — Cliente, Cotação, Ordem — vão morar **no mesmo projeto**, no mesmo artefato, no mesmo banco. Isso tem nome: **monólito**. E a pergunta desta aula não é "quando quebramos isso em microsserviços?", e sim uma bem mais útil: **como fazer um monólito crescer sem virar um emaranhado?** Porque essa é a diferença entre um monólito que um dia poderá ser quebrado e um que nunca poderá.
>
> As três pausas continuam: 🎬 **Imagine o cenário**, 💭 **Pare e reflita** e ✍️ **Experimente agora**.

---

## 1. O monólito não é o vilão

Uma **arquitetura monolítica** concentra todos os componentes de negócio em **uma aplicação**, implantada como **um artefato**, rodando em **um processo**. O nome vem da geologia: monólito é um bloco único de rocha.

Para o tamanho em que estamos, isso não é dívida — é a decisão certa. As vantagens são concretas:

- **Implantação simples.** Um artefato, um pipeline, um ambiente. Comparado a orquestrar cinco serviços com versões compatíveis entre si, é outra ordem de grandeza de esforço.
- **Desenvolvimento simples.** Um repositório, um `mvn test`, uma IDE que enxerga o sistema inteiro. Renomear um método e ver todos os usos é trivial.
- **Comunicação barata.** `ordemService` chama `cotacaoService` com uma **chamada de método**: sem rede, sem serialização, sem *timeout*, sem falha parcial. Isso não é detalhe — é a origem de metade da complexidade dos sistemas distribuídos.
- **Transação simples.** Uma transação do banco cobre a operação inteira. Distribuído, isso vira SAGA, compensação e consistência eventual.
- **Depuração simples.** Um *stack trace* conta a história toda, em vez de você correlacionar logs de cinco serviços por um id de rastreio.

E as desvantagens, igualmente concretas:

- **Escala em bloco.** Se a consulta de cotação recebe cem vezes mais carga que o cadastro, você não consegue escalar só a cotação: sobe tudo junto, e paga infraestrutura por capacidade que não precisa.
- **Falha que se propaga.** Um vazamento de memória num ponto derruba o processo inteiro — inclusive as partes saudáveis.
- **Velocidade que cai com o tamanho.** Base grande, build longo, mais pessoas mexendo nos mesmos arquivos, mais conflito.
- **Reimplantação total.** Corrigir uma linha da cotação obriga a reimplantar cliente e ordem junto, com todo o rito de release.

> 💭 **Pare e reflita**
> Olhe essa lista de desvantagens e responda com honestidade: **quantas delas doem no seu sistema hoje?** Se a resposta for "nenhuma, ainda", você acabou de descobrir por que a resposta certa para o seu caso pode ser continuar monolítico — e por que essa é uma decisão de engenharia, não de conservadorismo.

---

## 2. Camadas: o corte técnico e onde ele falha

A organização mais comum de um projeto Spring é por **camada técnica**:

```
br.com.ada.cambio
├── controller
│   ├── ClienteController
│   ├── CotacaoController
│   └── OrdemController
├── service
│   ├── ClienteService  ...
└── repository
    └── ...
```

Isso é organizado — e engana. Faça o teste: para mudar uma regra de **negócio** (digamos, o arredondamento do valor da ordem), quantos pacotes você abre? Três, no mínimo. E o que fica junto num mesmo pacote são coisas que **não têm nada a ver entre si**: o controller de cliente e o de ordem só compartilham o fato de serem controllers.

O sintoma clássico é o *"caminho longo"*: para entender uma funcionalidade, você navega por três pacotes distantes. Em projetos pequenos isso é irrelevante. Em projetos grandes, é o que faz cada mudança tocar meio sistema.

---

## 3. Pacote por domínio: o monólito modular

A alternativa é cortar por **domínio**, e colocar as camadas **dentro** de cada domínio:

```
br.com.ada.cambio
├── cliente
│   ├── api        (ClienteController, DTOs)
│   ├── dominio    (Cliente, ClienteService, exceções)
│   └── infra      (ClienteRepository)
├── cotacao
│   ├── api / dominio / infra
└── ordem
    ├── api / dominio / infra
```

Agora a mudança de negócio fica **contida**: mexer em cotação abre um pacote. O nome disso é **monólito modular** — um único artefato implantável, internamente dividido em módulos com **fronteiras explícitas**.

E aqui está o ponto que decide o futuro do sistema: **um monólito modular é um monólito que pode ser quebrado; um monólito emaranhado não é.** Quando cada domínio já tem seus dados, sua regra e sua porta de entrada, extrair um deles para um serviço próprio (Aula 5) é mover um pacote e trocar chamadas de método por chamadas HTTP. Quando tudo acessa tudo, "quebrar" é um eufemismo para "reescrever".

<details><summary>Ver esquema em texto — corte por camada × corte por domínio</summary>

```
CORTE POR CAMADA (técnico)          CORTE POR DOMÍNIO (modular)
                                    
┌───────────────────────────┐       ┌────────┐ ┌────────┐ ┌────────┐
│ controller: cli cot ord   │       │CLIENTE │ │COTAÇÃO │ │ ORDEM  │
├───────────────────────────┤       │ api    │ │ api    │ │ api    │
│ service:    cli cot ord   │       │ domínio│ │ domínio│ │ domínio│
├───────────────────────────┤       │ infra  │ │ infra  │ │ infra  │
│ repository: cli cot ord   │       └────────┘ └────────┘ └───┬────┘
└───────────────────────────┘            ▲          ▲         │
                                         └──────────┴─────────┘
 mudar UMA regra de negócio               ordem chama os outros dois
 = abrir OS TRÊS pacotes                  pela porta da frente (Service)

 extrair um domínio                       extrair um domínio
 = cirurgia em 3 pacotes                  = mover UMA pasta
```
</details>

> 🎬 **Imagine o cenário**
> Daqui a um ano, a área de negócio quer que a cotação venha de um provedor externo e seja consultada mil vezes por minuto, enquanto o cadastro de cliente segue com dez chamadas por hora. Se `cotacao` já é um módulo com fronteira, extrair é um dia de trabalho. Se a lógica de cotação está espalhada entre um controller, dois services e um repositório compartilhado, é um projeto de três meses — com risco.

---

## 4. Coesão e acoplamento, sem decoreba

Duas palavras que todo mundo repete e poucos usam para decidir. Em português direto:

- **Coesão** — o quanto as coisas que estão juntas *deveriam* estar juntas. Alta coesão: **o que muda junto mora junto**. `Cotacao`, `CotacaoService` e a regra "moeda não suportada é 422" mudam pelas mesmas razões — então moram no mesmo módulo.
- **Acoplamento** — o quanto um módulo precisa **saber** sobre o outro para funcionar. Se `ordem` conhece o nome da tabela de cliente, o acoplamento é altíssimo: mudar a tabela quebra `ordem`.

O erro comum é imaginar que a meta é **acoplamento zero**. Não é, e não pode ser: se os módulos não se conhecessem em nada, o sistema não faria nada. A meta é acoplamento **explícito, mínimo e numa direção só**:

| | Acoplamento ruim | Acoplamento aceitável |
|---|---|---|
| Por onde | pelo **dado** (tabela, entidade JPA do outro) | pela **operação** (método público do serviço dono) |
| Direção | mútua (A chama B, B chama A) | única (`ordem` → `cliente`, nunca o contrário) |
| Visibilidade | implícita (descobre-se depurando) | explícita (está no construtor) |
| Efeito de mudar | quebra o vizinho em silêncio | quebra a compilação, na hora |

Repare no último item: **acoplamento explícito é um recurso**, porque falha cedo e barulhento. Acoplamento implícito é o que falha tarde e caro — de novo, a Aula 2.

> 💭 **Pare e reflita**
> No sistema que você mantém: existe alguma tabela lida por duas partes diferentes do código, com regras diferentes? Essa tabela é o ponto de acoplamento mais perigoso que existe — e é sempre o que impede a extração.

---

## 5. Limites internos: quem pode chamar quem

Num monólito, nada **impede tecnicamente** que `ordem` injete `ClienteRepository` e leia a tabela diretamente. O compilador não reclama, o teste passa, funciona hoje.

E é justamente por isso que o limite precisa ser uma **regra combinada**, e verificada. Nossa regra no projeto é curta:

> **Um domínio só fala com outro pela porta da frente: o `Service` do domínio dono. Nunca pelo repositório, nunca pela entidade JPA do outro, nunca pela tabela.**

Por que isso importa se "funciona"? Três razões práticas:

1. **A regra de negócio some.** Se `ordem` lê a tabela de cliente direto, a validação "cliente precisa existir e estar ativo" fica duplicada — ou esquecida. O `ClienteService` existe para ser o único lugar onde essa regra mora.
2. **A extração fica impossível.** Na Aula 5, `cliente` vira um serviço com **banco próprio**. Quem falava com o `ClienteService` só troca a implementação; quem lia a tabela precisa ser reescrito.
3. **A mudança fica imprevisível.** Com fronteiras, você sabe o alcance de uma alteração olhando as assinaturas. Sem elas, só descobre executando.

<details><summary>Ver esquema em texto — quem pode chamar quem</summary>

```
            ┌──────────────┐
            │    ORDEM     │  orquestra
            │ OrdemService │
            └───┬──────┬───┘
        PERMITIDO│      │PERMITIDO
                 ▼      ▼
      ┌────────────┐  ┌──────────────┐
      │ClienteServ.│  │CotacaoService│   ← porta da frente
      └─────┬──────┘  └──────┬───────┘
            │ (privado)      │ (privado)
            ▼                ▼
    ClienteRepository   CotacaoRepository
            ▲                ▲
            └────── PROIBIDO ┘  ordem NUNCA acessa
                              repositório/tabela alheios

 Regra: dependência em UMA direção, pela porta da frente.
 Se o desenho tiver seta de volta, você tem ciclo — e não tem módulo.
```

</details>

Em projetos maduros, essa regra é verificada **por ferramenta** — ArchUnit em Java permite escrever um teste que reprova o build se `ordem` importar algo de `cliente.infra`. Onde a ferramenta não está disponível, a regra vira item de checklist de revisão. O que não pode é ficar só na intenção.

---

## 6. Crescendo o Câmbio: Cotação e Ordem

Vamos ao código. Primeiro, o domínio **Cotação**. A moeda é um **enum**, e não uma `String` — assim "moeda inválida" deixa de ser um `if` espalhado e vira um ponto único de conversão:

```java
public enum Moeda {
    USD, EUR;

    public static Moeda de(String sigla) {
        try {
            return Moeda.valueOf(sigla.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new MoedaNaoSuportadaException(sigla);   // vira 422
        }
    }
}
```

```java
@Service
public class CotacaoService {

    private final CotacaoRepository repositorio;

    public CotacaoService(CotacaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Cotacao cotacaoAtual(Moeda moeda) {
        return repositorio.findTopByMoedaOrderByDataHoraDesc(moeda)
                .orElseThrow(() -> new CotacaoIndisponivelException(moeda));
    }
}
```

Por que **422** e não 400 para `GET /cotacoes/GBP`? Porque a requisição está **sintaticamente correta** — "GBP" é uma sigla bem formada; o problema é **semântico**: não oferecemos essa moeda. `400 Bad Request` fica reservado para o que está malformado (JSON quebrado, campo obrigatório ausente). Vale saber que essa distinção é debatida entre times; o que **não** é opcional é escolher, registrar e ser consistente.

Agora o domínio **Ordem**, que é o interessante: ele **depende dos outros dois** — pela porta da frente, e por construtor:

```java
@Service
public class OrdemService {

    private final ClienteService clienteService;   // porta da frente do domínio cliente
    private final CotacaoService cotacaoService;   // porta da frente do domínio cotação
    private final OrdemRepository repositorio;

    public OrdemService(ClienteService clienteService,
                        CotacaoService cotacaoService,
                        OrdemRepository repositorio) {
        this.clienteService = clienteService;
        this.cotacaoService = cotacaoService;
        this.repositorio = repositorio;
    }

    public Ordem registrar(NovaOrdem nova) {
        Cliente cliente = clienteService.buscarPorCpf(nova.cpfCliente());   // 404 se não existir
        Moeda moeda = Moeda.de(nova.moeda());                               // 422 se não suportada
        Cotacao cotacao = cotacaoService.cotacaoAtual(moeda);

        BigDecimal total = nova.valorMoedaEstrangeira()
                .multiply(cotacao.getValorCotacao())
                .setScale(2, RoundingMode.HALF_EVEN);                       // dinheiro: nunca double

        return repositorio.save(new Ordem(cliente, moeda, nova.valorMoedaEstrangeira(),
                                          cotacao.getValorCotacao(), total,
                                          nova.numeroAgenciaRetirada(), LocalDateTime.now()));
    }
}
```

Três detalhes que valem por si:

- **`BigDecimal`, nunca `double`.** Dinheiro em ponto flutuante binário acumula erro de representação. `setScale(2, HALF_EVEN)` fixa a escala e o arredondamento — `HALF_EVEN` ("arredondamento do banqueiro") é o padrão em finanças porque não enviesa a soma de muitos valores para cima.
- **A dependência aparece no construtor.** Você lê a classe e sabe exatamente do que ela depende. Isso é o que torna o teste unitário da Aula 2 possível, e é o **D** do SOLID, que aprofundaremos na Aula 7.
- **Cada erro tem seu dono.** "Cliente não existe" é 404 e vem do domínio cliente; "moeda não suportada" é 422 e vem do domínio cotação. `OrdemService` **orquestra**; não reimplementa a regra dos outros.

> ✍️ **Experimente agora**
> A ordem guarda `valorCotacao` **junto** com o registro, em vez de consultar a cotação de novo na hora de exibir o comprovante. Por quê? Escreva sua resposta em uma frase antes de seguir. (Dica: pense no que acontece com um comprovante de ontem quando o dólar sobe hoje.)

---

## 7. Quando o monólito é a resposta certa — e quando não é

A pergunta "monólito ou microsserviços?" é mal formulada, porque não tem contexto. As perguntas úteis são duas: **o que dói hoje?** e **o que custa mudar?**

**Sinais de que o monólito ainda é a resposta certa:**

- O time é pequeno, ou está se formando no domínio.
- O domínio ainda é **instável** — as fronteiras de negócio ainda estão sendo descobertas. Cortar cedo é cortar errado, e um corte errado entre serviços é muito mais caro de desfazer que entre pacotes.
- O *time to market* aperta: o custo inicial de N serviços atrasa a primeira entrega.
- A organização **não tem** maturidade operacional para operar N serviços (observabilidade, pipeline, plantão, rastreio distribuído). Lembre da "viabilidade" da Aula 2: a solução que sua organização não consegue operar não é a melhor solução.

**Sinais de que chegou a hora de quebrar:**

- Uma parte precisa **escalar de forma independente**, com números na mão — não com hipótese.
- Times diferentes disputam a mesma base e o mesmo calendário de release, e isso está medindo em atraso.
- Partes distintas têm requisitos **não funcionais** incompatíveis (uma precisa de altíssima disponibilidade, outra roda em lote à noite).
- O ciclo de build/deploy ficou lento a ponto de virar o gargalo do time.

E um sinal que **não** conta: "todo mundo está fazendo". Martin Fowler chegou a nomear a recomendação oposta — *MonolithFirst*: comece monolítico e extraia quando as fronteiras estiverem claras. Simon Brown resume melhor ainda: *"se você não consegue construir um monólito bem estruturado, o que te faz pensar que microsserviços são a resposta?"*

---

## 8. Casos reais: monólitos em escala e microsserviços que voltaram

**Monólito por escolha, em escala grande.** A **Shopify** publicou em 2019 (*Deconstructing the Monolith*) como lida com uma das maiores bases Rails do mundo: em vez de fatiar em microsserviços, investiu num **monólito modular**, com componentes de fronteira explícita e dependências verificadas por ferramenta — exatamente a ideia da §5, em escala industrial. O **Stack Overflow** é o outro clássico: por muitos anos serviu um volume enorme de tráfego com uma aplicação **monolítica** em .NET rodando em poucos servidores, e documentou publicamente sua arquitetura.

**Distribuído demais, e a volta.** Em março de 2023, o time do **Prime Video** publicou que migrou seu serviço de **monitoramento de qualidade de áudio e vídeo** de uma arquitetura distribuída (orquestração serverless entre várias funções) para um **processo único** — um monólito —, reduzindo o custo em cerca de **90%**. O caveat é essencial e costuma ser omitido nas manchetes: trata-se de **um componente**, não do Prime Video inteiro, e o gargalo era o custo de orquestrar e transferir dados entre as etapas. Antes disso, em 2018, a empresa **Segment** documentou publicamente o caminho inverso ao da moda: de centenas de microsserviços de volta a um monólito, por custo de manutenção.

**A leitura correta é a mesma nos dois lados:** nenhum desses times concluiu que "microsserviços são ruins" ou que "monólitos são melhores". Todos **mediram o que doía** e escolheram a estrutura que reduzia essa dor específica. Arquitetura boa não é a que segue a tendência; é a que responde à pergunta certa com evidência.

---

## 9. ADR: registrando "por que ainda monólito"

Uma decisão de arquitetura que só existe na cabeça de quem estava na sala é uma decisão que será desfeita por engano em dezoito meses. O **ADR** (*Architecture Decision Record*), proposto por Michael Nygard em 2011, é o remédio mais barato que existe: um arquivo de **uma página** no repositório, versionado junto com o código.

A estrutura é fixa e curta: **Título · Status · Contexto · Decisão · Consequências**. Veja o nosso:

```markdown
# ADR-001 — Manter o câmbio como monólito modular

## Status
Aceito — 21/09/2026

## Contexto
Três domínios (cliente, cotação, ordem) com um time único, em formação,
e volume atual baixo. Não há requisito de escala independente nem
maturidade operacional para operar múltiplos serviços.

## Decisão
Manter um único artefato (`cambio-api`), organizado em pacotes por
domínio, com camadas internas (api/dominio/infra). Comunicação entre
domínios apenas pelo Service do domínio dono — nunca por repositório
ou tabela de outro domínio.

## Consequências
+ Implantação, teste e depuração simples; transação local.
+ Fronteiras preservadas: a extração futura é viável.
- Não é possível escalar um domínio isoladamente.
- Falha em um domínio afeta o processo inteiro.

## Quando revisar
Se a cotação exigir escala independente, se surgirem times separados
por domínio, ou se o tempo de build virar gargalo.
```

Repare na última seção — a que quase todo ADR esquece. **Registrar o que tornaria a decisão obsoleta** é o que transforma o documento de "justificativa do passado" em "gatilho para o futuro". É também, na prática, a resposta ágil madura ao valor "software funcionando mais que documentação abrangente": não é documentar menos, é documentar **o que decide**, em uma página que alguém realmente lerá.

---

## 10. Ponte com o legado

Se você mantém um EAR com dezenas de EJBs implantado num servidor de aplicação, você mantém um **monólito** — e provavelmente reconheceu várias das dores da §1. Mas vale separar o que é culpa do formato e o que não é:

- **Ser um único artefato não é o problema.** O problema típico daquele EAR é que **tudo acessa tudo**: qualquer EJB busca qualquer DAO, qualquer módulo lê qualquer tabela, e não há nenhuma fronteira além dos nomes de pacote. É o monólito **emaranhado**, não o monólito modular.
- **Tabela compartilhada é o acoplamento mais caro.** Quando três módulos leem e escrevem a mesma tabela com regras diferentes, você não tem três módulos — tem um só, espalhado. É o primeiro obstáculo em qualquer projeto de extração, e o motivo pelo qual "quebrar em microsserviços" costuma travar antes de começar.
- **Procedure com regra de negócio** é o mesmo fenômeno, um andar abaixo: a regra fora do módulo dono, invisível para quem lê o código Java.
- **A boa notícia:** modularizar um monólito legado é um trabalho **incremental e de baixo risco**, feito com a rede de proteção da Aula 2. Você escolhe um domínio, escreve testes de caracterização do comportamento atual, move as classes, fecha a fronteira e faz o resto conversar só pelo Service. Não é uma reescrita; é uma sequência de refatorações. E, ao fim dela, você tem a opção de extrair — que hoje você não tem.

---

## 11. IA & agentes hoje

O monólito modular é, hoje, o formato de repositório mais **amigável para agentes de código** — e vale entender por quê, porque a razão é a mesma que vale para humanos.

- **Contexto único.** Um agente trabalha bem quando consegue ver, ao mesmo tempo, a chamada, a regra e o teste. Num monólito modular isso está tudo no mesmo repositório, e ele consegue propor uma refatoração completa e verificá-la com `mvn test`. Espalhado em cinco repositórios, ele enxerga fatias — e propõe mudanças que quebram o vizinho **em silêncio**, porque o build do vizinho não roda.
- ***Context engineering* é escolher o que o agente vê.** Módulos com fronteira clara permitem instruções precisas: *"trabalhe apenas em `cotacao`; não altere `cliente`"*. Isso melhora a saída exatamente pelo mesmo motivo que melhora a de uma pessoa nova no time — menos superfície, menos ambiguidade, menos chance de efeito colateral.
- **Fronteira de módulo = tamanho de tarefa.** Um card que cabe dentro de um módulo é um card que um agente consegue executar de ponta a ponta, com o teste da Aula 2 como critério de aceitação. Um card que atravessa três módulos precisa de julgamento humano sobre a fronteira — e é aí que você entra.
- **O efeito colateral bom.** Como o custo de escrever código caiu, o custo relativo de **entender** e **manter** subiu. Estrutura clara deixou de ser preferência estética e virou a variável que determina quanto trabalho a máquina consegue fazer por você com segurança.

---

## 12. Para ir além

- **Martin Fowler, *MonolithFirst*** e ***MicroservicePremium*** (martinfowler.com/bliki): dois textos curtos que explicam por que começar monolítico costuma ser a aposta certa, e qual é o "prêmio" que microsserviços cobram.
- **Shopify Engineering, *Deconstructing the Monolith: Designing Software that Maximizes Developer Productivity*** (2019): monólito modular em escala real, com fronteiras verificadas por ferramenta.
- **Prime Video Tech Blog (2023), *Scaling up the Prime Video audio/video monitoring service and reducing costs by 90%***: o caso da volta ao processo único — leia com atenção ao escopo do componente.
- **Michael Nygard, *Documenting Architecture Decisions*** (2011): a origem do ADR, em duas páginas.
- **Simon Brown, *Software Architecture for Developers*** e o modelo **C4**: como desenhar e comunicar a estrutura de um monólito modular.

> **Na próxima aula (Aula 4 — SOA × REST):** hoje o provedor de cotação é nosso, mora dentro do monólito e sempre responde. E quando ele for de **outra empresa**, com outro formato de JSON, com latência, e fora do ar às vezes? Vamos transformar `CotacaoProvider` num **contrato de serviço** com duas implementações — local e externa — e descobrir por que "expor um endpoint REST" não é, nem de longe, a mesma coisa que ter uma arquitetura orientada a serviços.
