# Material do Aluno — Aula 1: Ágil na prática (Manifesto, Scrum, Kanban)

> **Tempo de leitura:** ~15 min. Hoje começa a **API de Câmbio**: um banco quer vender dólar e euro pelo app, com retirada em agência. O cliente se cadastra, consulta a cotação, registra uma ordem e recebe o comprovante. Nada disso é difícil de programar. O difícil é outra coisa: você recebe esse pedido numa terça, sem documento, com a área de negócio disponível meia hora por dia, e precisa mostrar algo **utilizável** na semana seguinte — sabendo que metade do que pediram vai mudar. É esse problema que o ágil tenta resolver, e é por isso que a primeira coisa que construiremos hoje não é código: é um **board com o backlog** e uma **sprint planejada**.
>
> Três pausas aparecem ao longo do texto: 🎬 **Imagine o cenário**, 💭 **Pare e reflita** e ✍️ **Experimente agora**. Não são enfeite — é onde o conceito encosta na sua realidade.

---

## 1. Por que existe "metodologia" — e por que o cascata fazia sentido

Desenvolver software parece simples até você olhar de perto: alinhar com o negócio, escolher tecnologia, revisar entregas, respeitar prazo. Some tudo isso e aparece o problema real — **pessoas não pensam do mesmo jeito**, mesmo diante da mesma situação. Sem regras e acordos combinados, cada um puxa para um lado. **Metodologia de desenvolvimento** é esse acordo: um conjunto ordenado de processos que guia o ciclo de vida de um projeto.

A primeira metodologia pensada para software, consolidada entre os anos 1950 e 1970, foi a **cascata** (*waterfall*). Veio da engenharia civil e da manufatura, e propõe fases sequenciais em sentido único: requisitos → design → implementação → teste → entrega → manutenção. Enquanto o design não termina, a implementação não começa.

É moda ridicularizar a cascata. Resista a isso: ela **funcionava bem** no contexto para o qual foi criada — requisitos estáveis, software como apoio, forte necessidade de controle. Se você constrói uma ponte, planejar tudo antes é exatamente o certo.

O que mudou não foi a inteligência de quem a usava; foi o **negócio**. Quando o software deixou de ser apoio e virou o produto — o app **é** o banco, para boa parte dos clientes — o requisito passou a ser volátil por natureza. E aí o modelo quebra num ponto cruel: **na cascata, o erro de entendimento só aparece no fim**, na fase de teste, quando corrigir custa mais caro. Anos de trabalho podem se revelar a solução errada para o problema certo.

<details><summary>Ver esquema em texto — cascata × ciclos curtos</summary>

```
CASCATA (um único ciclo longo)
Requisitos ──► Design ──► Implementação ──► Teste ──► Entrega
   |                                          ▲
   └──────── feedback do usuário chega AQUI ───┘
              (meses ou anos depois)

CICLOS CURTOS (vários ciclos pequenos)
 [plan→faz→mostra→ajusta] [plan→faz→mostra→ajusta] [plan→faz→mostra→ajusta]
       ▲ feedback              ▲ feedback              ▲ feedback
   (1 a 4 semanas)

A diferença não é "documentar menos".
É DESCOBRIR O ERRO CEDO, quando corrigir ainda é barato.
```
</details>

Nos anos 1990 surgiram tentativas de flexibilizar o modelo — Scrum, XP, RUP, entre outras —, cada uma por seu lado, sem linguagem comum. Foi essa dispersão que o Manifesto veio organizar.

> 💭 **Pare e reflita**
> Pense no último projeto em que você entrou. Em que momento a equipe descobriu que tinha entendido algo errado? No começo, ou perto da entrega? Quanto custou corrigir naquele momento — em dias, em retrabalho, em conversa difícil?

---

## 2. 2001: o Manifesto — os 4 valores lidos com juízo

Em fevereiro de 2001, dezessete profissionais de desenvolvimento se reuniram em Snowbird, Utah — entre eles Kent Beck (XP), Martin Fowler, Ken Schwaber e Jeff Sutherland (Scrum). Saíram de lá com um texto de menos de setenta palavras, o **Manifesto para Desenvolvimento Ágil de Software**, e quatro valores:

| Valorizamos mais... | ...do que |
|---|---|
| **Indivíduos e interações** | processos e ferramentas |
| **Software em funcionamento** | documentação abrangente |
| **Colaboração com o cliente** | negociação de contratos |
| **Responder a mudanças** | seguir um plano |

Agora a parte que quase todo mundo ignora, e que está **escrita no próprio manifesto**: *"mesmo havendo valor nos itens à direita, valorizamos mais os itens à esquerda."* O manifesto é uma declaração de **preferência**, não de **abolição**.

Isso muda tudo na prática. "Software funcionando mais que documentação abrangente" **não** autoriza um sistema sem documentação. Num ambiente regulado, com auditoria e rastreabilidade, documentação é obrigação legal. O que o manifesto ataca é a documentação que **ninguém lê** — as 200 páginas produzidas para cumprir rito e arquivadas para sempre. A resposta madura não é "não documentar": é trocar 200 páginas mortas por **decisões registradas em uma página cada** (o ADR que escreveremos na Aula 3).

O mesmo vale para "responder a mudanças mais que seguir um plano": não significa não planejar. Significa que o plano é uma **hipótese**, e que informação nova vale mais que a coerência com o que você escreveu há três meses.

> 💭 **Pare e reflita**
> Qual dos quatro valores é o mais difícil de praticar na sua realidade? Na maioria das equipes corporativas a resposta é "colaboração com o cliente" — porque o cliente é interno, tem outra chefia, outras metas e trinta minutos por dia. Isso é um problema de organização, não de método. Reconhecer isso é o primeiro passo.

---

## 3. Os 12 princípios, agrupados e com leitura crítica

Os doze princípios detalham os valores. Decorá-los em ordem é inútil; entender o que cada grupo defende é o que importa.

**Entregar valor cedo e sempre** — princípios 1, 3 e 4: entrega contínua e adiantada; software funcionando em semanas, não meses; e o mais contraintuitivo, *"mudanças nos requisitos são bem-vindas, mesmo tardiamente"*. Leitura crítica: "bem-vindas" não quer dizer "gratuitas". Mudança tem custo; o ponto é que arquitetura e processo devem tornar esse custo **suportável**, não proibitivo. É para isso que existem os testes da Aula 2 e o SOLID da Aula 7.

**Pessoas** — princípios 5 a 8: negócio e desenvolvimento trabalhando **diariamente** juntos; projetos construídos em torno de indivíduos motivados e confiáveis; conversa cara a cara; e **software funcionando como medida primária de progresso**. O princípio 8 é o mais afiado: se a única evidência de progresso é uma apresentação de status, não há progresso — há relato.

**Sustentabilidade e qualidade** — princípios 9 a 11: ritmo constante indefinidamente (nada de correria); atenção contínua à excelência técnica **aumenta** a agilidade; e simplicidade, *"a arte de maximizar a quantidade de trabalho não realizado"*. Guarde o 10: é a defesa explícita, dentro do manifesto, de que qualidade técnica não é luxo — é o que permite continuar rápido.

**Auto-organização e melhoria** — princípio 12: as melhores arquiteturas, requisitos e designs emergem de equipes auto-organizáveis. Leitura crítica: "emergem" **não** significa "não se desenha arquitetura". Significa que a decisão pertence a quem constrói, não a um comitê distante — e que é revista quando o sistema ensina algo novo.

> 🎬 **Imagine o cenário**
> Sua equipe entrega, em duas semanas, o cadastro de cliente da API de Câmbio funcionando de ponta a ponta. Na demonstração, a área de negócio vê a tela e diz: *"ah, mas o cliente estrangeiro não tem CPF; vai precisar de passaporte."* Na cascata, essa frase chegaria em outubro e custaria um replanejamento. Aqui, ela chegou na segunda semana e custa um campo e uma validação. **Não foi o software que ficou melhor — foi o momento da descoberta que mudou.**

---

## 4. Scrum: o que o Guia de 2020 realmente diz

Scrum é um **framework** — não uma metodologia completa — para times lidarem com problemas complexos e requisitos que mudam. O nome vem do rugby, e a semente conceitual está no artigo *The New New Product Development Game* (Takeuchi e Nonaka, Harvard Business Review, 1986). Ken Schwaber e Jeff Sutherland o formalizaram em meados dos anos 1990 e mantêm o **Scrum Guide** até hoje.

Atenção a um detalhe que você vai encontrar em muito material desatualizado: o Guia mudou. A versão de **2020** fala em **três responsabilidades** (não "papéis"), três artefatos **com compromissos** e cinco eventos.

**As três responsabilidades:**

- **Product Owner** — uma **pessoa** (não um comitê) responsável por maximizar o valor do produto e pelo Product Backlog. É quem decide a **ordem**. Se o PO não tem esse poder, você não tem PO: tem um secretário de pedidos.
- **Scrum Master** — responsável pela eficácia do time e por ajudar a organização a entender o Scrum. Não é chefe, não distribui tarefas, não cobra status: remove impedimentos e melhora o processo.
- **Developers** — todos que constroem o incremento (inclui QA, dados, design). São eles que decidem **como** o trabalho é feito.

**Os três artefatos e seus compromissos** — este pareamento é o que mais gente desconhece:

| Artefato | O que é | Compromisso |
|---|---|---|
| Product Backlog | lista ordenada de tudo que pode ser necessário no produto | **Product Goal** — o objetivo de longo prazo |
| Sprint Backlog | o que o time escolheu para esta sprint + o plano para entregar | **Sprint Goal** — o objetivo único da sprint |
| Increment | o resultado utilizável produzido | **Definition of Done** — o que significa "pronto" |

**Os cinco eventos:** a **Sprint** (que contém todos os outros), **Sprint Planning**, **Daily Scrum**, **Sprint Review** e **Sprint Retrospective**.

Duas confusões clássicas, resolvidas de uma vez:

- **Review ≠ Retrospective.** A *review* olha para o **produto**: mostramos o incremento e colhemos feedback sobre o **valor** entregue. A *retrospective* olha para o **processo**: o que funcionou, o que atrapalhou, qual **uma** melhoria entra na próxima sprint. Quem faz só a review nunca melhora o jeito de trabalhar; quem faz só a retro aperfeiçoa o processo de construir a coisa errada.
- **Daily ≠ relatório de status.** É do time, para o time, em no máximo 15 minutos. A pergunta útil não é "o que você fez ontem", e sim **"o que ameaça a meta da sprint e o que faremos a respeito?"**.

E o **burndown chart**? É um gráfico popular e útil (trabalho restante × tempo), mas **não é artefato obrigatório** do Scrum Guide 2020 — o que te protege da discussão "não é Scrum se não tem burndown".

<details><summary>Ver esquema em texto — o ciclo de uma sprint</summary>

```
   PRODUCT BACKLOG  (ordenado pelo PO)
        │
        │  SPRINT PLANNING  → define o SPRINT GOAL
        ▼
   ┌───────────────── SPRINT (1 a 4 semanas) ──────────────────┐
   │  SPRINT BACKLOG                                           │
   │   dia 1 ─ daily ─ dia 2 ─ daily ─ ... (daily <= 15 min)    │
   │   trabalho ─────────────────────────►  INCREMENT (DoD)    │
   └───────────────────────────────────────────────────────────┘
        │                                    │
        ▼                                    ▼
   SPRINT REVIEW                     SPRINT RETROSPECTIVE
   (o PRODUTO: que valor              (o PROCESSO: o que
    entregamos? feedback)              melhoramos na próxima?)
        │
        └──► volta a alimentar o Product Backlog
```
</details>

---

## 5. A Sprint — e o nosso Scrum-lite de uma semana

A **sprint** é uma caixa de tempo fixa, de no máximo um mês, ao fim da qual existe um incremento **utilizável**. "Fixa" quer dizer que a data **não se move**: se o trabalho não coube, sai escopo — nunca a data. É essa rigidez que torna a previsibilidade possível.

Neste módulo vivemos **três sprints de uma semana**, cada uma com três aulas:

| Sprint | Aulas | Meta |
|---|---|---|
| 1 | 1–3 | monólito com Cliente, Cotação e Ordem, testado |
| 2 | 4–6 | serviços com contratos, quebra do monólito e comunicação |
| 3 | 7–8 | código limpo, SOLID, padrões; projetos finais entregues |

Cada aula abre com uma **daily de 3 minutos** sobre o board do Câmbio, e a última aula de cada sprint fecha com **review + retrospectiva de 10 minutos**. É Scrum em escala reduzida — mas o **ciclo** é o real: planejar → executar → mostrar → ajustar.

> ✍️ **Experimente agora**
> Escreva a meta da Sprint 1 do **seu** projeto de grupo em **uma frase**, no formato *"ao fim da semana, um usuário consegue ____"*. Se você não consegue completar a frase sem usar a palavra "e" duas vezes, sua meta ainda é uma lista de tarefas, não um objetivo.

---

## 6. Kanban: fluxo, WIP e um board que não é decoração

O **Kanban** nasceu na Toyota, nos anos 1940–50, com o engenheiro **Taiichi Ohno**, dentro do *Toyota Production System*. Em 2010, **David Anderson** formalizou sua aplicação ao trabalho de conhecimento — e ao desenvolvimento de software em particular.

Quase todo mundo conhece o Kanban pelo quadro de três colunas: *A fazer* → *Fazendo* → *Pronto*. Mas o quadro é só a parte visível. A prática central que a maioria ignora é o **limite de trabalho em progresso (WIP)**.

**Por que limitar o WIP acelera a entrega?** Parece contraintuitivo. Com cinco tarefas abertas por pessoa, você paga **troca de contexto** o dia inteiro e **nada** termina — cinco itens ficam 80% prontos, o que vale zero para o cliente. Com duas, elas terminam, saem do board e viram valor. O quadro fica visualmente mais vazio e a entrega real **aumenta**. Um item 90% pronto não é 90% de valor: é 0% de valor e 100% de risco.

**As métricas do Kanban** também não são o burndown. São métricas de **fluxo**:

- **Lead time** — quanto tempo um item leva do pedido à entrega (o que o cliente sente).
- **Cycle time** — quanto tempo leva desde que o time começou a trabalhar nele.
- **Throughput** — quantos itens são concluídos por período.
- **WIP** — quantos itens estão em andamento agora.

A diferença prática em relação à velocidade de sprint: *velocity* diz quanto o time faz por sprint; **lead time diz quanto tempo o cliente espera** — e é essa a pergunta que o negócio faz.

<details><summary>Ver esquema em texto — board com limite de WIP</summary>

```
 BACKLOG   │ BACKLOG   │ A FAZER │ FAZENDO  │ EM REVISÃO │ PRONTO
 DO PRODUTO│ DA SPRINT │         │ (WIP 2)  │  (WIP 2)   │
───────────┼───────────┼─────────┼──────────┼────────────┼────────
 H6 Trocar │ H3 Testar │ H4 Cotar│ H1 Cadas-│            │
 provedor  │ o cadastro│  moeda  │ trar     │            │
           │           │         │ cliente  │            │
 H7 Separar│ H5 Ordem  │         │          │            │
 serviços  │ de compra │         │ H2 Consul│            │
           │           │         │ tar CPF  │            │
 H8 Desco- │           │         │          │            │
 brir serv.│           │         │ ↑ CHEIO: │            │
           │           │         │ ninguém  │            │
 H9 SOLID  │           │         │ puxa mais│            │
 H10 Padrões│          │         │ nada     │            │
```
Quando "Fazendo" atinge o limite, a regra é: **ajude a terminar
o que já está lá** — não puxe um item novo.
</details>

> 💭 **Pare e reflita**
> Abra mentalmente o board da sua equipe agora. Quantos itens estão em "Fazendo"? Quantas pessoas há no time? Se o primeiro número é maior que o segundo, você já sabe por que as coisas demoram.

---

## 7. Scrumban: juntando os dois

Scrum é forte em **cadência, papéis e ritmo de inspeção**, e fraco em representação visual do trabalho. Kanban é forte em **visualização e fluxo**, e nada diz sobre papéis ou cerimônias. Juntá-los é tão natural que ganhou nome: **Scrumban**.

| | Scrum | Kanban | Scrumban (o que a maioria faz) |
|---|---|---|---|
| Ritmo | sprints de duração fixa | fluxo contínuo | sprints, com fluxo dentro delas |
| Compromisso | escopo da sprint | nenhum formal | meta de sprint, escopo flexível |
| Limite de trabalho | indireto (capacidade da sprint) | **explícito** (WIP) | meta de sprint **+ WIP explícito** |
| Papéis | PO, SM, Developers | não define | os do Scrum |
| Métrica principal | velocity, burndown | lead time, throughput | as duas |
| Mudança no meio | evitada | a qualquer momento | negociada com o PO |

Quando escolher **Kanban puro**, sem sprints? Quando o trabalho chega de forma imprevisível e não cabe em caixas fechadas — sustentação, produção, incidentes. Forçar sprint num time de sustentação produz sprints que sempre "falham" e métricas que viram ficção; fluxo contínuo com WIP e lead time é mais honesto.

---

## 8. "Scrum de fachada": como reconhecer

Em 2018, **Martin Fowler** — um dos signatários do Manifesto — criticou publicamente o que chamou de *faux agile* e **"Agile Industrial Complex"**: organizações que compram o **ritual** (cerimônias, certificações, ferramentas) e descartam o **princípio** (equipes decidindo como trabalham). Grandes programas públicos já foram auditados por isso: o relatório do **National Audit Office** britânico sobre o **Universal Credit** (2013) apontou que o "ágil" foi adotado **sem as condições de governança, contratação e medição que o método pressupõe**, com pouca visibilidade real de progresso.

Os sintomas, que talvez você reconheça:

- A **daily** dura 45 minutos e é, na prática, relatório de status para o gestor.
- O **board** tem 30 cards em "Fazendo" e nenhum limite de WIP.
- A **retrospectiva** produz as mesmas três ações há seis sprints — e nenhuma foi executada.
- A **sprint** "fecha" com tudo em homologação; o resto entra na próxima como dívida.
- O **PO** não tem autoridade para dizer não a nenhum pedido.
- O time é chamado de "auto-organizável", mas as tarefas chegam distribuídas de fora.

O diagnóstico é sempre o mesmo: **cerimônia sem entrega**. O teste mais simples é o princípio 8 — se a única evidência de que o mês foi produtivo é uma apresentação, não houve progresso: houve relato.

---

## 9. O board do Câmbio: nosso backlog real

Hoje criamos, juntos, o `KANBAN.md` do projeto guiado e o board espelho. O **backlog do produto** cobre o módulo inteiro:

| # | História | Aula |
|---|---|---|
| H1 | Como cliente, quero **me cadastrar** para poder comprar moeda | 1 |
| H2 | Como atendente, quero **consultar um cliente pelo CPF** para confirmar cadastro | 1 |
| H3 | Como time, quero **testes nos três níveis** para mudar o código sem medo | 2 |
| H4 | Como cliente, quero **consultar a cotação** de USD/EUR antes de comprar | 3 |
| H5 | Como cliente, quero **registrar uma ordem de compra** e receber o comprovante | 3 |
| H6 | Como time, quero **trocar o provedor de cotação** sem alterar o serviço | 4 |
| H7 | Como time, quero **serviços separados** por domínio, com bases próprias | 5 |
| H8 | Como time, quero **descobrir serviços** sem URL fixa no arquivo de configuração | 6 |
| H9 | Como time, quero **refatorar com SOLID** sem quebrar comportamento | 7 |
| H10 | Como time, quero **aplicar padrões** e registrar as decisões em ADRs | 8 |

Uma história só entra na sprint com **critérios de aceite verificáveis**. Compare:

- ❌ *"Cadastrar cliente."*
- ✅ *"`POST /clientes` com nome, CPF (11 dígitos), data de nascimento, estado civil e sexo devolve **201** com o `id` gerado; CPF ausente ou fora do formato devolve **400**; CPF já cadastrado devolve **conflito** com mensagem clara; `GET /clientes/{cpf}` devolve **200** com os dados ou **404**."*

A segunda versão pode ser conferida por qualquer pessoa, sem discussão. E a nossa **Definition of Done** — o acordo sobre o que significa "pronto" — começa assim: compila · o endpoint responde no cliente HTTP · o card foi movido · o `README` foi atualizado.

Repare que **`mvn test` verde não está na lista de hoje** — e não está por um motivo: ainda não escrevemos nenhum teste. Ele entra na DoD **a partir da Aula 2**, quando a suíte existir. Isso não é descuido: o Scrum Guide trata a Definition of Done como um acordo que o time **amplia conforme amadurece**. Uma DoD que promete o que o time ainda não consegue entregar é exatamente o tipo de cerimônia vazia que criticamos há pouco.

Feita a planning, movemos H1 para *Fazendo* e escrevemos o primeiro pedaço do monólito:

```java
@RestController
@RequestMapping("/clientes")
class ClienteController {

    private final ClienteService service;

    ClienteController(ClienteService service) { // injeção por construtor desde o dia 1
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)                       // 201
    ClienteResponse cadastrar(@RequestBody @Valid ClienteRequest req) {
        return ClienteResponse.de(service.cadastrar(req.paraDominio()));
    }

    @GetMapping("/{cpf}")
    ClienteResponse porCpf(@PathVariable String cpf) {        // 200 ou 404
        return ClienteResponse.de(service.buscarPorCpf(cpf));
    }
}
```

O `ClienteService` guarda a regra (buscar por CPF e lançar `ClienteNaoEncontradoException`, que o `@ControllerAdvice` converte em 404) e o `ClienteRepository` fala com o H2. Repare: o card **não** se move para *Pronto* porque o código compilou. Ele se move quando a resposta esperada aparece no cliente HTTP — evidência, não opinião.

> ✍️ **Experimente agora**
> Pegue a história H4 ("consultar a cotação de USD/EUR") e escreva **três** critérios de aceite verificáveis para ela, incluindo o que deve acontecer quando alguém pede `GBP`. Guarde: vamos comparar com o que implementarmos na Aula 3.

---

## 10. Ponte com o legado

Se você trabalha com sistemas mais antigos — EJB em JBoss, EAR único implantado por janela de mudança, integrações SOAP num barramento, rotinas batch noturnas —, o Manifesto não é uma crítica ao seu trabalho. É uma descrição precisa do que **doía** naquele ciclo:

- **Documento de 200 páginas assinado antes da primeira linha** → "software em funcionamento mais que documentação abrangente".
- **Comitê de mudança que se reúne quinzenalmente para aprovar um `if`** → "responder a mudanças mais que seguir um plano".
- **Release trimestral com trinta itens juntos**, em que um erro obriga a voltar tudo → princípios 1 e 4 (entregar frequentemente, em pequenas fatias).
- **Analista que fala com o cliente e "passa o requisito" adiante** → princípio 5 (negócio e desenvolvimento **diariamente** juntos).

E a parte que costuma passar despercebida: aquele ciclo tinha coisas boas que o ágil mal implementado joga fora — rastreabilidade da decisão, atenção a requisitos não funcionais, disciplina de homologação. Um time ágil maduro **mantém** isso, em formato mais leve. Trocar 200 páginas por um ADR de uma página é ganho dos dois lados: mais rápido de escrever **e** mais fácil de auditar.

---

## 11. IA & agentes hoje

Assistentes e agentes de código mudaram o custo relativo das etapas do ciclo — e isso tem consequência direta no que você acabou de ler.

- **O gargalo se deslocou.** Gerar código ficou barato. **Decidir o que gerar** e **revisar o que veio** virou o trabalho caro. O valor de um backlog bem priorizado **subiu**, não caiu — quem escreve histórias vagas agora produz código errado mais depressa.
- **História com critério de aceite é uma *spec* executável.** Compare os dois textos da §9: o primeiro produz implementação genérica, sem tratamento de erro; o segundo funciona como especificação, permite construir exatamente o comportamento combinado e permite **conferir** o resultado. Escrever bem o card virou habilidade técnica.
- **O princípio 8 fica mais rigoroso.** Quatrocentas linhas geradas em quarenta segundos não são progresso; o endpoint devolvendo 201 com teste verde é. A daily de um time que usa IA pergunta **"o que já funciona de ponta a ponta?"**, não "quanto código saiu".
- **A revisão virou o novo gargalo.** No board isso tem efeito prático: a raia *Em revisão* precisa de limite de WIP tanto quanto *Fazendo*. Uma fila de pull requests gerados por IA esperando revisão humana é exatamente o "item 90% pronto" — valor zero, risco cheio.

---

## 12. Para ir além

- **Manifesto Ágil** — os 4 valores e os 12 princípios, em português: [agilemanifesto.org/iso/ptbr](https://agilemanifesto.org/iso/ptbr/manifesto.html). Leia o original; leva dois minutos e desmonta metade dos mitos.
- **Scrum Guide 2020** (Schwaber e Sutherland) — [scrumguides.org](https://scrumguides.org/scrum-guide.html). Treze páginas, gratuito, e é **a** fonte: responsabilidades, artefatos com compromissos e eventos.
- **Kanban Guide** — [resources.kanban.university/kanban-guide](https://resources.kanban.university/kanban-guide/): práticas, limite de WIP e métricas de fluxo.
- **Martin Fowler, *The State of Agile Software in 2018*** — martinfowler.com: a crítica ao *faux agile* e ao "Agile Industrial Complex", feita por um signatário do Manifesto.
- **Takeuchi e Nonaka, *The New New Product Development Game*** (Harvard Business Review, 1986) — o artigo que deu origem à metáfora do Scrum.

> **Na próxima aula (Aula 2 — qualidade e pirâmide de testes):** o `POST /clientes` funciona. Amanhã eu vou pedir para você **mudar** o `ClienteService` — e a pergunta vai ser: como você sabe que não quebrou nada? Clicar no Postman e ver "deu certo" conta como prova? Vamos construir a rede de proteção que torna todas as mudanças das próximas sete aulas possíveis — e, de quebra, entender por que essa API é chamada de **monólito**.
