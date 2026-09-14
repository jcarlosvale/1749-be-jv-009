# Material do Aluno — Aula 8: Design Patterns, do catálogo ao código

> **Tempo de leitura:** ~16 min. Na aula passada deixamos o Câmbio limpo: constantes no lugar dos números mágicos, `OrdemService` com cinco linhas no método público, injeção por construtor, dependência em `CotacaoProvider` e não no Feign. E deixamos um requisito pendurado: **o dólar passa a ter spread de 1%; o euro, não.** A solução de dez segundos é um `if (moeda == USD)`. Ela funciona hoje, e nos condena quando entrarem libra e iene. A solução certa tem nome, está num catálogo publicado em 1994 e a comunidade inteira já sabe do que você está falando quando você o pronuncia. Hoje é sobre isso: **dar nome às soluções** — e saber quando não usá-las.

---

## 1. O `if/else` que cresce toda semana

Vamos ser honestos com o código ingênuo:

```java
BigDecimal bruto = valorMoedaEstrangeira.multiply(valorCotacao);
if (moeda == Moeda.USD) {
    total = bruto.multiply(new BigDecimal("1.01"));   // spread
} else if (moeda == Moeda.EUR) {
    total = bruto;
}
total = total.setScale(ESCALA_MONETARIA, ARREDONDAMENTO);
```

Funciona. Passa nos testes. E tem três problemas que só aparecem com o tempo:

1. **Viola o OCP.** Cada moeda nova obriga a **editar** um método que já funciona e já foi testado — e todo `git blame` desse arquivo vai apontar para quem mexeu por último.
2. **O `if` se replica.** A moeda nova não muda só o cálculo: muda validação, formatação, relatório, tela. Em seis meses existem cinco `if` de moeda espalhados, e um deles vai ser esquecido.
3. **Esconde a intenção.** O código diz *como* calcula; não diz que existem **regras de cálculo diferentes por moeda** — que é a informação de negócio que importa.

> **💭 Pare e reflita:** no sistema que você mantém, qual é o `if/else` (ou `switch`) que cresce a cada produto, canal ou convênio novo? Quantos lugares diferentes precisam ser alterados quando entra mais um caso? Esse número é a sua dívida de OCP.

---

## 2. O que é um design pattern (e o que não é)

Em 1994, Erich Gamma, Richard Helm, Ralph Johnson e John Vlissides — a *Gang of Four* — publicaram *Design Patterns: Elements of Reusable Object-Oriented Software*. Eles não inventaram os 23 padrões do livro: eles **observaram** que os mesmos problemas apareciam em projetos diferentes e eram resolvidos com as mesmas estruturas, e resolveram catalogar isso com nome, contexto, consequências e exemplo.

Um design pattern é, portanto: **uma solução nomeada para um problema recorrente, num contexto dado, com consequências conhecidas.**

O que ele **não** é:

- **Não é biblioteca nem framework.** Você não instala Strategy; você o desenha.
- **Não é meta.** "Aplicar cinco padrões" não é objetivo de projeto nenhum. O objetivo é resolver o problema; o padrão é meio.
- **Não é receita imutável.** A implementação de 1994 assumia C++ e Java sem lambdas. Hoje, `record`, lambda e injeção de dependência mudam a forma — a **intenção** é que permanece.

E o maior benefício prático costuma passar despercebido: **vocabulário**. Quando você diz "o `CotacaoExternaProvider` é um Adapter", quem ouve já sabe o desenho, o motivo e o risco — cinco minutos de explicação viram cinco palavras. É por isso que padrão continua valendo mesmo quando a linguagem simplifica a implementação.

---

## 3. Os três grupos e o catálogo GoF

Os 23 padrões originais se dividem por **intenção**:

| Grupo | Responde a… | Padrões (GoF) |
|---|---|---|
| **Criacionais** | como objetos **nascem** | Factory Method, Abstract Factory, **Builder**, Prototype, **Singleton** |
| **Estruturais** | como objetos se **compõem** | **Adapter**, Bridge, Composite, Decorator, **Facade**, Flyweight, Proxy |
| **Comportamentais** | como objetos **colaboram** | **Strategy**, Observer, Command, Template Method, State, Chain of Responsibility, Iterator, Mediator, Memento, Visitor |

Ninguém decora essa tabela — e você não precisa. O que se espera de um dev intermediário é: **reconhecer** o problema, saber que existe um nome para ele, e conseguir procurar. O catálogo do refactoring.guru existe exatamente para isso.

Os cinco em negrito são os que o Câmbio usa. Note que eles cobrem os três grupos, como pede o planejamento do módulo — e que **um deles já estava no nosso código desde a Aula 4**, sem nome.

<details><summary>Ver esquema em texto…</summary>

```
   Problema recorrente                     Padrão            Grupo
   ────────────────────────────────────────────────────────────────────────
   "o cálculo muda conforme a moeda"    →  Strategy       comportamental
   "o controller conhece 4 colaboradores" → Facade        estrutural
   "só pode existir um catálogo"        →  Singleton      criacional
   "o formato externo não é o meu"      →  Adapter        estrutural
   "objeto com 9 campos, 2 do mesmo tipo" → Builder       criacional
```
</details>

---

## 4. Strategy — o cálculo que varia por moeda

**Problema:** existe uma família de algoritmos que fazem a mesma coisa de formas diferentes, e a escolha acontece em runtime.
**Solução:** cada algoritmo vira uma classe com a mesma interface; quem usa recebe a implementação certa, sem saber qual é.

```java
public interface CalculoOperacaoStrategy {

    Moeda moeda();

    BigDecimal calcularTotal(BigDecimal valorMoedaEstrangeira, BigDecimal valorCotacao);
}
```

```java
@Component
class CalculoDolarStrategy implements CalculoOperacaoStrategy {

    private static final BigDecimal SPREAD = new BigDecimal("0.01");

    @Override public Moeda moeda() { return Moeda.USD; }

    @Override
    public BigDecimal calcularTotal(BigDecimal valorMoedaEstrangeira, BigDecimal valorCotacao) {
        return valorMoedaEstrangeira.multiply(valorCotacao)
                .multiply(BigDecimal.ONE.add(SPREAD))
                .setScale(RegrasCambio.ESCALA_MONETARIA, RegrasCambio.ARREDONDAMENTO);
    }
}

@Component
class CalculoEuroStrategy implements CalculoOperacaoStrategy {

    @Override public Moeda moeda() { return Moeda.EUR; }

    @Override
    public BigDecimal calcularTotal(BigDecimal valorMoedaEstrangeira, BigDecimal valorCotacao) {
        return valorMoedaEstrangeira.multiply(valorCotacao)
                .setScale(RegrasCambio.ESCALA_MONETARIA, RegrasCambio.ARREDONDAMENTO);
    }
}
```

Falta escolher a estratégia. Aqui o Spring faz um trabalho elegante: **peça uma `List` da interface e ele injeta todas as implementações registradas**. Reduzimos essa lista a um `Map<Moeda, CalculoOperacaoStrategy>`:

```java
@Component
public class CalculoOperacaoResolver {

    private final Map<Moeda, CalculoOperacaoStrategy> porMoeda;

    public CalculoOperacaoResolver(List<CalculoOperacaoStrategy> estrategias) {
        this.porMoeda = estrategias.stream()
                .collect(Collectors.toUnmodifiableMap(CalculoOperacaoStrategy::moeda,
                                                      Function.identity()));
    }

    public CalculoOperacaoStrategy para(Moeda moeda) {
        CalculoOperacaoStrategy estrategia = porMoeda.get(moeda);
        if (estrategia == null) {
            throw new MoedaNaoSuportadaException(moeda);
        }
        return estrategia;
    }
}
```

Repare no que aconteceu com o requisito da libra: criar `CalculoLibraStrategy` com `@Component` **basta**. Nenhuma linha do `CalculoOperacaoResolver`, do `OrdemService` ou do `CambioFacade` muda. Isso é o Open/Closed Principle deixando de ser slide e virando diff.

> **✍️ Experimente agora:** implemente `CalculoLibraStrategy` no seu gabarito, rode `mvn test` e depois rode `git diff --stat`. Se algum arquivo **existente** de cálculo aparecer na lista, o padrão não está aplicado corretamente — algo ainda decide por `if`.

**Quando Strategy é exagero:** com **duas** variações que nunca vão crescer, um `switch` expressivo pode ser mais legível que três arquivos. O padrão se paga a partir da terceira variação, ou quando as regras mudam por motivos independentes — que é exatamente o nosso caso (o time de câmbio muda o spread do dólar sem tocar no euro).

---

## 5. Facade — uma porta para o controller

**Problema:** o cliente precisa coordenar vários colaboradores para realizar **uma** operação de negócio, e acaba conhecendo o subsistema inteiro.
**Solução:** uma classe de fachada expõe a operação de negócio e esconde a orquestração.

```java
@Service
public class CambioFacade {

    private final ConsultaCliente consultaCliente;
    private final CotacaoProvider cotacaoProvider;
    private final CalculoOperacaoResolver calculos;
    private final OrdemRepository ordemRepository;

    public CambioFacade(ConsultaCliente consultaCliente,
                        CotacaoProvider cotacaoProvider,
                        CalculoOperacaoResolver calculos,
                        OrdemRepository ordemRepository) {
        this.consultaCliente = consultaCliente;
        this.cotacaoProvider = cotacaoProvider;
        this.calculos = calculos;
        this.ordemRepository = ordemRepository;
    }

    public OrdemResponse registrarOrdem(NovaOrdem nova) {
        Cliente cliente = consultaCliente.buscarPorCpf(nova.cpfCliente());
        Cotacao cotacao = cotacaoProvider.buscar(nova.moeda());

        BigDecimal total = calculos.para(nova.moeda())
                .calcularTotal(nova.valorMoedaEstrangeira(), cotacao.valorCotacao());

        Ordem ordem = Ordem.nova(cliente, nova, cotacao, total);
        return OrdemResponse.de(ordemRepository.save(ordem));
    }
}
```

E o controller emagrece até virar o que ele deveria sempre ter sido — tradução entre HTTP e domínio:

```java
@RestController
@RequestMapping("/ordens")
class OrdemController {

    private final CambioFacade cambio;

    OrdemController(CambioFacade cambio) { this.cambio = cambio; }

    @PostMapping
    ResponseEntity<OrdemResponse> registrar(@RequestBody @Valid NovaOrdemRequest requisicao) {
        OrdemResponse resposta = cambio.registrarOrdem(requisicao.paraComando());
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
}
```

<details><summary>Ver esquema em texto…</summary>

```
   OrdemController                 conhece 1 colaborador
        │
        ▼
   CambioFacade  ──────────────────────────────────────────┐
        ├──► ConsultaCliente          (cliente)            │
        ├──► CotacaoProvider          (cotação)            │ orquestração
        ├──► CalculoOperacaoResolver ─┐                    │ escondida
        │         │ Map<Moeda, CalculoOperacaoStrategy>    │
        │         ├──► CalculoDolarStrategy   (spread 1%)  │
        │         └──► CalculoEuroStrategy    (sem spread) │
        └──► OrdemRepository          (persistência)  ─────┘

   Moeda nova = mais uma @Component implementando a interface.
   Nenhuma seta acima precisa ser redesenhada.
```
</details>

> **🎬 Imagine o cenário:** seis meses depois, o time decide que toda ordem precisa passar por uma checagem de limite diário. Com Facade, existe **um** lugar óbvio para isso, e o controller nem fica sabendo. Sem Facade — com o controller chamando os quatro colaboradores — a mudança acontece em todo lugar que registra ordem: o app, o canal interno, o batch.

**O risco honesto:** uma Facade que continua crescendo vira God class. A regra é: ela **orquestra**, não decide regra de negócio. Se você vê um `if` de negócio dentro da fachada, esse `if` pertence ao domínio ou a uma Strategy. E se o controller chama um único serviço, **não crie fachada** — seria uma camada que só repassa.

---

## 6. Singleton — o padrão mais famoso e o mais discutido

**Problema:** garantir que exista uma única instância de algo e um ponto global de acesso a ela.
**Solução de livro:**

```java
public final class CatalogoMoedas {

    private static final CatalogoMoedas INSTANCIA = new CatalogoMoedas();

    private final Set<Moeda> suportadas = EnumSet.of(Moeda.USD, Moeda.EUR);

    private CatalogoMoedas() { }

    public static CatalogoMoedas instancia() { return INSTANCIA; }

    public boolean suporta(Moeda moeda) { return suportadas.contains(moeda); }
}
```

Agora a parte honesta. Veja o que acontece com quem usa:

```java
// dependência ESCONDIDA: a assinatura da classe não revela que existe um catálogo
if (!CatalogoMoedas.instancia().suporta(moeda)) {
    throw new MoedaNaoSuportadaException(moeda);
}
```

Os problemas, na ordem em que doem:

- **Dependência invisível.** O construtor não conta que essa classe depende do catálogo. Você só descobre lendo o corpo dos métodos.
- **Impossível substituir no teste.** Não há como injetar um catálogo de teste com uma moeda fictícia; você fica preso à instância real.
- **Estado global entre testes.** Se o singleton guardar estado mutável, o teste A contamina o teste B — e a suíte passa a falhar dependendo da ordem.

**E aqui está o ponto que vale para o resto da sua vida com Spring:** o escopo padrão de um bean **já é singleton** — uma instância por contexto de aplicação. Só que com uma diferença decisiva:

| | Singleton do GoF | Bean com escopo singleton |
|---|---|---|
| Quem garante a unicidade | a própria classe (construtor privado) | o container |
| A classe pode ser instanciada em teste | não | sim (`new CatalogoMoedas()`) |
| Dá para injetar um substituto | não | sim, é só outro bean |
| A dependência aparece no construtor | não | sim |

Ou seja: dentro do Spring, escrever um Singleton "de livro" quase sempre é resolver com um martelo um prego que o container já segurou. A versão certa no nosso projeto é simplesmente:

```java
@Component
public class CatalogoMoedas {
    private final Set<Moeda> suportadas = EnumSet.of(Moeda.USD, Moeda.EUR);
    public boolean suporta(Moeda moeda) { return suportadas.contains(moeda); }
}
```

**Quando o Singleton de livro ainda faz sentido:** fora de container (uma biblioteca, um utilitário standalone, um cache de processo em código sem injeção). Nesse caso, a forma mais segura em Java é o `enum` de elemento único — o próprio Josh Bloch recomenda em *Effective Java* —, porque a JVM garante a unicidade inclusive contra reflexão e serialização. E o material oficial do módulo é direto: dependendo do contexto, Singleton é considerado **anti-pattern**. Saber por quê é mais valioso que saber implementá-lo.

---

## 7. Adapter e Builder — o que já tínhamos e o que falta nomear

### Adapter: ele estava aqui desde a Aula 4

**Problema:** a interface que você tem não é a que o seu código espera.
**Solução:** uma classe que traduz uma na outra.

Abra o `CotacaoExternaProvider` da Aula 4: ele recebe o JSON do provedor externo (com nomes, formato de data e estrutura dele) e devolve um `Cotacao` do **nosso** modelo. E o `CotacaoRemotaProvider` da Aula 7 faz o mesmo com a resposta do Feign:

```java
@Override
public Cotacao buscar(Moeda moeda) {
    CotacaoRemotaResponse externa = feign.buscarCotacao(moeda.name());
    return new Cotacao(moeda, externa.valorCotacao(), externa.dataHora());   // tradução
}
```

Os dois são Adapters, e cumprem um papel maior que "converter JSON": eles são a **camada anticorrupção** do sistema. O formato do fornecedor não vaza para dentro do domínio; se o provedor mudar o contrato amanhã, muda o adaptador — **uma** classe — e o resto do sistema nem fica sabendo. Foi por isso que trocar `RestClient` (Aula 5) por Feign (Aula 6) e ainda mudar para provedor local não exigiu tocar no `OrdemService`.

### Builder: opcional, e útil quando o objeto tem muitos campos

Olhe o contrato da resposta da ordem: nove campos, e dois deles são `String` **adjacentes** — `cpf_cliente` e `numero_agencia_retirada`. Um construtor posicional aceita os dois trocados **em silêncio**:

```java
// compila, roda, e devolve o comprovante errado
new OrdemResponse(id, idCliente, "7057", data, moeda, valor, cotacao, total, "43488428095");
```

Com Builder, a troca fica visível na leitura:

```java
OrdemResponse resposta = OrdemResponse.builder()
        .idCompra(ordem.id())
        .idCliente(cliente.id())
        .cpfCliente(cliente.cpf())
        .dataSolicitacao(ordem.dataSolicitacao())
        .tipoMoeda(ordem.moeda())
        .valorMoedaEstrangeira(ordem.valorMoedaEstrangeira())
        .valorCotacao(ordem.valorCotacao())
        .valorTotalOperacao(ordem.valorTotalOperacao())
        .numeroAgenciaRetirada(ordem.numeroAgenciaRetirada())
        .build();
```

Vale o custo? Depende. Um `record` com um método de fábrica nomeado (`OrdemResponse.de(ordem)`) resolve o mesmo problema com menos código — e é a alternativa que você deve considerar antes. Builder se paga quando há muitos campos **opcionais** ou construção em etapas.

---

## 8. Quando **não** usar um padrão — e o ADR de uma página

Esta seção é tão importante quanto as anteriores. O material oficial do módulo diz, com todas as letras: o padrão acrescenta complexidade, e essa complexidade **precisa ser paga** por uma vantagem real.

Sinais de over-engineering — todos comuns em código de quem acabou de estudar padrões:

- **Strategy com uma estratégia só.** É uma interface, uma classe e um resolver para fazer o que uma função fazia.
- **Factory que só chama `new`.** Se não há decisão a tomar na criação, a fábrica é ruído.
- **Interface para tudo.** Uma implementação, nenhuma intenção de troca: só um arquivo a mais entre você e o código.
- **Facade que repassa uma chamada.** Camada sem conteúdo.
- **Padrão escolhido pelo nome.** "Vamos usar Observer aqui" antes de existir alguém observando.

Três perguntas antes de aplicar qualquer padrão — se as três respostas não vierem fáceis, não aplique:

1. **Qual problema recorrente eu tenho?** (não "qual padrão eu quero usar")
2. **Qual complexidade ele adiciona, e o que ela me compra?**
3. **Existe solução mais simples na linguagem?** (lambda, `record`, `enum`, `Map`)

E é isso que o **ADR** registra. Um por padrão, curto, no `docs/adr/`:

```markdown
# ADR-006: Strategy para o cálculo da operação por moeda

## Status
Aceito — 02/10/2026

## Contexto
O cálculo do valor total varia por moeda (USD tem spread; EUR não) e novas
moedas estão previstas. Hoje a regra está num if/else dentro do OrdemService.

## Decisão
Uma implementação de CalculoOperacaoStrategy por moeda, registradas pelo
Spring e resolvidas por Map<Moeda, CalculoOperacaoStrategy>.

## Alternativas consideradas
- if/else no serviço: rejeitada — viola OCP e o if se replica em validação e relatório.
- Regras parametrizadas em tabela: rejeitada por ora — a regra é código (arredondamento,
  spread), não dado; reavaliar se o negócio pedir alteração sem deploy.

## Consequências
+ Moeda nova = classe nova; nenhum arquivo existente é editado.
− Mais uma indireção: para entender o cálculo é preciso achar a estratégia da moeda.
```

Repare no que dá valor ao ADR: as **alternativas rejeitadas** e a consequência **negativa**. ADR que só elogia a própria decisão não é registro, é propaganda — e a banca da Aula 9 percebe.

> **💭 Pare e reflita:** pegue um padrão que existe no sistema em que você trabalha. Você consegue escrever, em três linhas, qual problema ele resolve e qual alternativa foi descartada? Se não consegue, ou o padrão está mal aplicado, ou o conhecimento está na cabeça de uma pessoa só — e as duas hipóteses são risco.

---

## 9. Ponte com o legado

Padrões não são novidade para quem mantém sistema antigo — eles já estão lá, com outros nomes ou mal implementados:

- **O `if/else` de tipo que virou instituição.** `if (tipoProduto.equals("01")) ... else if ("02") ...` com trinta ramos e comentários explicando códigos que ninguém lembra. É o candidato número um a Strategy — e a refatoração pode ser incremental: extraia **um** ramo por vez para uma estratégia, mantendo o `if` para os demais até esvaziá-lo.
- **A `FabricaGeral` de 2 000 linhas.** Factory é padrão criacional legítimo, mas a fábrica que cria tudo virou God class. Um `Map` de fábricas por tipo resolve, e volta a caber na cabeça.
- **O singleton estático de conexão.** `ConexaoBanco.getInstance()` chamado de dentro da regra é a razão de metade do sistema não ter teste unitário. Injetar a conexão (ou o repositório) é a mesma discussão de DIP da aula passada.
- **A God class com nome de "serviço geral".** Muitas vezes o remédio não é padrão nenhum: é separar responsabilidades (SRP) primeiro e ver o que sobra.
- **A camada de integração que cresce a cada parceiro.** Adapter por parceiro + registro é a resposta — e é o mesmo desenho que fizemos hoje com as cotações.

Nenhuma dessas refatorações precisa ser feita de uma vez. Todas precisam de teste antes.

---

## 10. IA & agentes hoje

**Padrão é vocabulário compartilhado com a IA.** Um pedido como *"extraia este cálculo para uma Strategy por moeda, registrada pelo Spring num `Map<Moeda, …>`, sem alterar o comportamento"* é curto, preciso e quase impossível de interpretar errado — porque o nome do padrão carrega o desenho inteiro. Sem esse vocabulário, o mesmo pedido vira três parágrafos ambíguos. Quanto mais você programa com assistentes, **mais** valem os nomes que a comunidade compartilha.

**E o risco simétrico:** modelos aplicam padrões com entusiasmo. Peça uma melhoria genérica e você recebe uma Factory, uma interface por classe e um Builder onde cabia um `record`. Isso não é bug do modelo — é o reflexo do que existe em maior quantidade no código público. **A decisão de se o padrão se paga continua sendo sua**, e a pergunta de sempre vale aqui: *eu sei defender isso numa revisão?* Se a resposta é não, o código não entra.

**Os "novos GoF".** Está se formando um vocabulário para sistemas com agentes — *tool use* (o modelo chama ferramentas com contrato declarado), *reflection* (o modelo critica a própria saída antes de entregar), *planner-executor* (um passo planeja, outro executa), *multiagente* (papéis especializados que se coordenam). O mecanismo é idêntico ao de 1994: nomes dados a soluções que se repetiam. A diferença é a maturidade — é vocabulário **emergente**, sem um catálogo canônico e ainda em disputa. Use com essa ressalva.

**Facade reaparece na fronteira com o agente.** O que você expõe a um agente deveria ser uma **fachada estável** — `registrarOrdem`, uma operação de negócio com contrato claro — e não os três passos internos que ela orquestra. Contrato menor significa menos formas de errar, menos superfície para mudar e menos contexto para o modelo carregar. É a mesma lição da Aula 4 sobre contratos de serviço, aplicada a um consumidor novo.

---

## 11. Para ir além

- **GAMMA, HELM, JOHNSON, VLISSIDES**, *Padrões de Projeto: Soluções Reutilizáveis de Software Orientado a Objetos* (Bookman) — o livro original de 1994; leia a introdução e os padrões que você usa, não os 23.
- **Refactoring Guru** — `https://refactoring.guru/pt-br/design-patterns/catalog`: catálogo em português, com exemplos em Java e problema/solução por padrão. É a referência para consultar durante o projeto.
- **Martin Fowler**, *Refactoring* (2ª ed.) — a refatoração *Replace Conditional with Polymorphism*: o caminho passo a passo do `if/else` até a Strategy.
- **Joshua Bloch**, *Effective Java* (3ª ed.) — item sobre Singleton com `enum` e o item sobre Builder para construtores com muitos parâmetros.
- **Robert C. Martin**, *Arquitetura Limpa* — para ver Strategy, Adapter e Facade como consequências dos princípios da Aula 7, e não como truques isolados.

> **Na próxima aula (Aula 9 — apresentações):** o Câmbio está pronto: nasceu monólito, virou três serviços, aprendeu a conversar, ficou limpo e agora é extensível. Segunda é a vez do **seu** projeto. Cada grupo tem ~15 minutos para demonstrar o que roda, defender as decisões que tomou e responder à arguição do docente e dos colegas — individualmente. Leia o material da Aula 9 **antes** de montar a apresentação: ele traz a estrutura dos 15 minutos, o banco de perguntas da banca e o checklist de entrega do repositório.
