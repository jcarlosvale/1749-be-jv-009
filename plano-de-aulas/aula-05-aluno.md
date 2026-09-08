# Material do Aluno — Aula 5: Quebrando o monólito em microsserviços

> **Tempo de leitura:** ~17 min. Nosso monólito de Câmbio está em boa forma: três domínios organizados em pacotes, testes verdes, um contrato limpo para a cotação. E é justamente por isso que a pergunta desta aula é honesta e desconfortável — **se está tão bem organizado, por que separar?** Vamos responder olhando os dois lados da conta: o que microsserviços de fato compram, o que eles cobram (a parte que os posts de blog costumam esquecer), por onde se corta, como se faz a transição sem parar o mundo e — importante — **quando não quebrar**. No fim da aula você terá três serviços rodando em três portas, e um problema novo no colo.

---

## 1. O problema: o deploy que espera pelos outros

🎬 **Imagine o cenário.** Sexta-feira, 16h. Foi encontrado um erro na validação de CPF: clientes com um formato específico não conseguem se cadastrar. A correção tem quatro linhas e está testada. Para colocá-la no ar, porém, você precisa subir o `cambio-api` inteiro — e no `main` já está a mudança da cotação que o time ao lado fez ontem e que ainda não passou por homologação. Você tem três opções, todas ruins: subir tudo (e arriscar), esperar (e deixar o cliente sem cadastro até segunda), ou fazer *cherry-pick* às pressas numa branch de emergência (e criar dívida).

Esse é o problema real que microsserviços resolvem, e vale enunciá-lo com precisão: **o monólito acopla o ritmo de entrega de partes que não têm nada a ver umas com as outras**. Não é sobre "código feio" — o nosso está bonito. É sobre **autonomia de deploy**.

💭 **Pare e reflita.** No seu sistema atual, quantas pessoas precisam concordar para uma correção de quatro linhas chegar em produção? Esse número é o custo do acoplamento de deploy.

---

## 2. O que microsserviços realmente compram

Vamos aos ganhos concretos, sem retórica:

- **Deploy independente.** É o benefício de raiz — todos os outros derivam dele. Um serviço sobe sozinho, com o próprio ciclo, o próprio *rollback* e o próprio risco.
- **Escala seletiva.** No Câmbio, a consulta de cotação é lida ordens de magnitude mais que a criação de ordens. No monólito, escalar a cotação significa replicar **tudo** — inclusive o cadastro, que quase não é usado. Separado, você sobe cinco instâncias de cotação e uma de cadastro. Isso é dinheiro.
- **Autonomia de time.** Times pequenos com um serviço próprio negociam menos, esperam menos e mudam mais rápido. (Reparou que este benefício é organizacional, não técnico? Guarde: metade da conversa sobre microsserviços é sobre gente.)
- **Isolamento de falha.** Um `OutOfMemoryError` no cadastro derruba o processo do cadastro — não a consulta de cotação. No monólito, o processo é um só.
- **Liberdade tecnológica.** Cada serviço pode usar a stack que fizer sentido. Na prática, use isso com parcimônia: cinco linguagens = cinco esteiras, cinco padrões de log, cinco conjuntos de conhecimento.

---

## 3. O que eles cobram

Aqui está a metade que decide se a sua quebra vai dar certo:

| Custo | O que muda na sua vida |
|---|---|
| **Operação × N** | Cada serviço quer pipeline, ambiente, monitoramento, alerta, log, plantão. Três serviços não dão o triplo do trabalho de um — dão mais, porque aparece a integração entre eles. |
| **Consistência** | Acabou a transação única. `POST /ordens` não consegue mais "gravar a ordem e debitar" atomicamente se as duas coisas estiverem em bancos diferentes. Entra consistência **eventual** e compensação. |
| **Latência** | Uma chamada de método leva nanossegundos; uma chamada HTTP na mesma rede leva milissegundos. Multiplique por quantas hipóteses de chamada você criou. |
| **Falha parcial** | Antes, o sistema estava no ar ou fora. Agora ele pode estar **meio no ar** — e alguém precisa decidir o que responder nesse estado. É o assunto do §8. |
| **Depuração** | O *stack trace* não atravessa mais a fronteira. Sem log correlacionado (um id de requisição propagado), investigar "por que a ordem falhou" vira arqueologia em três máquinas. |
| **Padronização** | Cada base evolui sozinha e diverge: um serviço loga em JSON, o outro em texto; um versiona a API, o outro não. Sem combinado, o ecossistema fica heterogêneo do jeito ruim. |

💭 **Pare e reflita.** Dos seis custos acima, quais o seu time já sabe pagar hoje, com as ferramentas que já tem? Os que sobrarem são o seu verdadeiro pré-requisito.

---

## 4. Critérios de corte: por onde se separa

Se você lembrar de uma frase desta seção, que seja esta: **corte por capacidade de negócio, e verifique pelos dados.**

- **Por capacidade de negócio** — o recorte primário. Uma capacidade é algo que a organização *faz*: cadastrar cliente, cotar moeda, registrar ordem. Ela sobrevive a mudanças de tecnologia e de tela.
- **Por subdomínio** — quando uma capacidade é grande demais, olha-se dentro dela. "Pagamentos" pode virar "pagamento por cartão" e "pagamento por boleto" se as regras forem realmente distintas.
- **Por time** — recorte secundário, mas real: se um time já é dono de um assunto, o serviço tende a seguir a fronteira desse time (é o efeito da Lei de Conway, que diz que o desenho do sistema tende a espelhar a estrutura de comunicação da organização).
- **Pelos dados — o teste decisivo.** Se dois candidatos a serviço precisam ler e escrever **a mesma tabela**, eles não são dois serviços: são um. Base compartilhada significa contrato implícito no esquema, e ninguém consegue deployar sozinho.

E o corte que **não** funciona: **por camada técnica**. Um "serviço de controllers", um "serviço de services" e um "serviço de repositories" parecem organizados e são o pior dos mundos — uma única mudança de negócio ("a agência agora tem 5 dígitos") exige alterar e deployar os três, em ordem. Você paga toda a rede e não compra nenhuma independência.

<details><summary>Ver esquema em texto — corte por camada × corte por domínio…</summary>

```
CORTE POR CAMADA (errado)              CORTE POR DOMÍNIO (certo)
┌──────────────────────────┐          ┌───────────┐ ┌───────────┐ ┌───────────┐
│  serviço de controllers  │          │  cliente  │ │  cotacao  │ │  cambio   │
├──────────────────────────┤          │  api      │ │  api      │ │  api      │
│  serviço de services     │          │  dominio  │ │  dominio  │ │  dominio  │
├──────────────────────────┤          │  infra    │ │  infra    │ │  infra    │
│  serviço de repositories │          │  [db_cli] │ │  [db_cot] │ │  [db_cam] │
└──────────────────────────┘          └───────────┘ └───────────┘ └───────────┘

"agência passa a ter 5 dígitos"        "agência passa a ter 5 dígitos"
  → muda os 3, deploy em ordem           → muda 1, deploy de 1
```
</details>

---

## 5. Estratégias de transição: ninguém quebra de uma vez

Reescrever um sistema em produção do zero, com a promessa de "ligar o novo quando ficar pronto", é o roteiro conhecido do desastre: o sistema antigo continua evoluindo, o novo nunca alcança e o negócio passa dois anos sem receber nada.

A alternativa consagrada é a **strangler fig application**, nome dado por Martin Fowler a partir da figueira-mata-pau, que cresce ao redor da árvore hospedeira até substituí-la. O nome descreve o método: o novo cresce **em volta** do antigo, assumindo uma função de cada vez, até que o antigo possa ser removido.

Na prática, a ordem que costuma dar certo:

1. **Achar as bordas.** Comece pelo que tem menos dependência de entrada — tipicamente uma leitura, ou uma funcionalidade nova.
2. **Criar a fronteira dentro do monólito primeiro.** Antes de haver rede, faça a comunicação passar por uma **interface explícita** dentro do próprio código. (É exatamente o que fizemos na Aula 4 com `CotacaoProvider` — sem saber, já estávamos preparando o corte.)
3. **Segregar os dados antes de segregar o processo.** Separe as tabelas, remova os `JOIN` que atravessam a fronteira, e só então extraia o serviço. Quem inverte essa ordem descobre em produção que os dois "serviços" ainda são um.
4. **Redirecionar o tráfego aos poucos**, com o antigo ainda de pé e capaz de retomar.
5. **Remover o código morto.** Etapa que quase todo mundo pula — e é assim que se acumula um monólito *e* um ecossistema de serviços ao mesmo tempo.

💭 **Pare e reflita.** Qual dos cinco passos você imagina que é o mais pulado na prática? (Dica: é o quinto — e é por isso que tanta empresa termina com o monólito antigo *e* os serviços novos, pagando os dois.)

<details><summary>Ver esquema em texto — strangler fig em três tempos…</summary>

```
T0  ┌──────────────────────────────┐
    │  MONÓLITO                    │   tudo aqui dentro
    │  cliente · cotacao · ordem   │
    └──────────────────────────────┘

T1  ┌──────────────────────────────┐        ┌──────────────┐
    │  MONÓLITO                    │ ─────► │ cotacao-svc  │   uma borda extraída;
    │  cliente · ordem · [fachada] │        │  [db_cot]    │   o monólito chama por HTTP
    └──────────────────────────────┘        └──────────────┘

T2  ┌────────────┐  ┌────────────┐  ┌────────────┐
    │ cliente-svc│  │ cotacao-svc│  │ cambio-svc │   monólito removido
    │  [db_cli]  │  │  [db_cot]  │  │  [db_cam]  │
    └────────────┘  └────────────┘  └────────────┘
```
</details>

---

## 6. Síncrono × assíncrono: panorama

Separados os serviços, eles precisam conversar. Há dois modos, com naturezas diferentes:

| | **Síncrono** (HTTP/REST, gRPC) | **Assíncrono** (fila/tópico) |
|---|---|---|
| Quem espera | O chamador espera a resposta | O produtor segue em frente |
| Acoplamento | **Temporal**: os dois precisam estar no ar ao mesmo tempo | O consumidor pode estar fora agora e processar depois |
| Consistência | Imediata dentro da chamada | **Eventual** |
| Falha | Propaga na hora (é visível) | Fica na fila (é invisível até você olhar) |
| Bom para | Consulta, validação, algo que o usuário aguarda | Efeito colateral, notificação, processamento pesado |

No Câmbio, `POST /ordens` precisa da cotação **agora** para calcular o total e devolver o comprovante: é síncrono por natureza. Já "avisar o BI que uma ordem foi criada" não precisa segurar o cliente — é candidato natural a assíncrono.

🎬 **Imagine o cenário.** `POST /ordens` chama cliente, que chama cotação, que chama um serviço de risco. Se cada um tem 99,9% de disponibilidade, a cadeia inteira tem ~99,7% — e a latência é a soma das três. Foi assim que um sistema "com três noves em cada peça" virou um sistema com trinta minutos de indisponibilidade por semana.

A regra de bolso: **síncrono para o que o usuário está esperando; assíncrono para o que pode acontecer depois.** O erro clássico é fazer uma cadeia longa de chamadas síncronas (A chama B, que chama C, que chama D): a disponibilidade do conjunto é o **produto** das disponibilidades, e a latência é a **soma** delas.

> Aqui só damos o panorama — filas, entrega ao menos uma vez, idempotência e consistência eventual são o assunto do módulo seguinte da trilha. O que você precisa levar hoje é o **critério de escolha**.

---

## 7. Quando NÃO quebrar

Esta seção vale tanto quanto as anteriores, e é a que menos se lê por aí. **Não quebre** quando:

- **O domínio ainda está instável.** Se as fronteiras de negócio mudam a cada mês, cada mudança de fronteira vira uma migração de dados entre serviços. Dentro do monólito, mover uma classe de pacote é um *refactor* de dez segundos.
- **O time é pequeno.** Três pessoas mantendo oito serviços passam mais tempo em infraestrutura que em produto.
- **Não existe automação de deploy nem observabilidade.** Sem esteira e sem log correlacionado, microsserviços entregam a dor sem o benefício.
- **O sistema não tem problema de escala nem de deploy.** Se o monólito sobe em cinco minutos e ninguém espera por ninguém, você resolveria um problema que não tem.

A alternativa honesta e frequentemente superior é o **monólito modular**: um único processo e um único deploy, com fronteiras internas rigorosas (pacotes por domínio, dependências só via interface, nenhuma classe atravessando o limite do módulo). Você fica com a simplicidade operacional de um processo **e** com as fronteiras desenhadas — de modo que, no dia em que houver um motivo mensurável, a extração é quase mecânica. É a arquitetura do nosso projeto até ontem.

💭 **Pare e reflita.** Escreva em uma frase o motivo pelo qual o **seu** sistema deveria ser quebrado. Se a frase contiver a palavra "moderno" ou "escalável" sem um número ao lado, ela ainda não é um motivo.

---

## 8. No projeto: três serviços, três bases, uma dor nova

O incremento de hoje transforma o `cambio-api` em três projetos Maven:

| Serviço | Porta | Responsabilidade | Base |
|---|---|---|---|
| `cliente-service` | 8081 | `POST /clientes`, `GET /clientes/{cpf}` | `jdbc:h2:mem:clientedb` |
| `cotacao-service` | 8082 | `GET /cotacoes/{moeda}`, `PUT /cotacoes/{moeda}` | `jdbc:h2:mem:cotacaodb` |
| `cambio-service` | 8083 | **dono de `Ordem`**: `POST /ordens`, `GET /ordens/{id}` | `jdbc:h2:mem:cambiodb` |

O `cambio-service` orquestra: confirma o cliente no 8081, busca a cotação no 8082, calcula e grava a ordem na própria base.

```yaml
# cambio-service/src/main/resources/application.yml
server:
  port: 8083
servicos:
  cliente:  { url: "http://localhost:8081" }   # ← URL fixa, de propósito
  cotacao:  { url: "http://localhost:8082" }
```

```java
@Component
class ClienteClient {
    private final RestClient http;

    ClienteClient(@Value("${servicos.cliente.url}") String baseUrl) {
        this.http = RestClient.create(baseUrl);
    }

    ClienteResponse porCpf(String cpf) {
        return http.get().uri("/clientes/{cpf}", cpf)
                   .retrieve().body(ClienteResponse.class);
    }
}
```

✍️ **Experimente agora.** Suba os três e teste **cada API separadamente** — é o que o planejamento do módulo pede, e é o que prova que cada serviço tem vida própria:

```bash
curl -X POST localhost:8081/clientes -H 'Content-Type: application/json' \
     -d '{"nome":"Ana","cpf":"43488428095","dataNascimento":"1990-04-12",
          "estadoCivil":"SOLTEIRO","sexo":"F"}'

curl localhost:8082/cotacoes/USD

curl -X POST localhost:8083/ordens -H 'Content-Type: application/json' \
     -d '{"cpfCliente":"43488428095","moeda":"USD",
          "valorMoedaEstrangeira":100,"numeroAgenciaRetirada":"7057"}'
```

Agora a parte que interessa. **Derrube o `cliente-service`** (Ctrl+C no 8081) e repita o `POST /ordens`.

O que acontece, sem nenhum tratamento: a requisição fica pendurada até o timeout padrão do cliente HTTP, a exceção de I/O sobe pela pilha e o cliente recebe um **`500 Internal Server Error`** — depois de esperar. Duas coisas estão erradas aí. A primeira é o **tempo**: o usuário esperou por nada. A segunda é a **mentira**: `500` diz "meu código quebrou", quando a verdade é "uma dependência minha está fora" — o que se anuncia com **`503 Service Unavailable`**, e idealmente rápido.

Duas dores nasceram nesta aula, e não vamos resolvê-las hoje:

1. **A URL fixa.** `http://localhost:8081` funciona no seu notebook. Em produção há três instâncias, em endereços que mudam a cada deploy. Quem vai saber onde o `cliente-service` está?
2. **A falha parcial.** O sistema agora pode estar **meio** no ar, e alguém precisa decidir, para cada caminho, o que responder nesse estado.

A dor 1 é a Aula 6 inteira. A dor 2 começa na Aula 6 e se aprofunda no módulo seguinte.

> **Registre a decisão.** Um ADR de dez linhas em `docs/adr/ADR-003-quebra-do-monolito.md`: *contexto* (deploy acoplado), *decisão* (cortar por domínio em três serviços com bases segregadas), *alternativas* (manter monólito modular; cortar por camada), *consequências* (chamadas remotas, falha parcial, fim do `JOIN`). Arquitetura sem registro vira lenda oral — em seis meses ninguém lembra por que foi assim.

---

## 9. Ponte com o legado

Se você mantém sistemas antigos, três reconhecimentos rápidos:

- **O EAR único com vários módulos** é o monólito desta aula: separação lógica boa, deploy indivisível. É o exato problema do §1 — e, quase sempre, o ponto de partida real de qualquer modernização.
- **O "monólito distribuído" já existia.** Muitos parques têm dezenas de aplicações que **precisam subir juntas**, porque compartilham a mesma base, o mesmo EAR de bibliotecas ou o mesmo barramento. Isso não é microsserviço: é monólito com latência de rede. O sintoma diagnóstico é uma pergunta só: *"esta aplicação sobe sozinha?"* Se a resposta for "depende", você tem um monólito distribuído.
- **O banco corporativo compartilhado** é a fronteira que mais impede a extração. Por isso a ordem correta é **dados primeiro**: enquanto duas aplicações escreverem na mesma tabela, elas são uma só, por mais separados que estejam os artefatos.

A modernização de um parque legado quase nunca é reescrita. É strangler fig: contrato na borda, dados segregados, tráfego redirecionado aos poucos, código morto removido. Exatamente o que esta aula ensaiou em escala pequena.

---

## 10. IA & agentes hoje

- **Sistemas multiagente são microsserviços com outro nome.** Cada agente tem uma fronteira de responsabilidade, um conjunto próprio de ferramentas, um "dono" e um modo próprio de falhar. Um agente monolítico que faz tudo sofre dos mesmos males do monólito grande: contexto demais para caber, responsabilidade difusa, mudança arriscada.
- **A conta de custo se repete.** N agentes significam N prompts para versionar, N conjuntos de ferramentas, N pontos de latência e de custo por token, e o mesmo problema de rastrear "onde o pedido parou". A pergunta desta aula vale igual: **o que exatamente eu compro dividindo?**
- **Falha parcial é o mesmo problema.** Um passo de agente que não responde é o `cliente-service` fora do ar. As respostas disponíveis também são as mesmas: falhar rápido e com honestidade, degradar com resposta parcial, ou enfileirar para depois.
- **Critério de corte também é por capacidade.** Divida agentes por capacidade ("pesquisar", "revisar", "executar"), não por etapa técnica do prompt — pelo mesmo motivo que não se corta serviço por camada.

---

## 11. Para ir além

- **Martin Fowler**, *Microservices* (2014) e *MicroservicePremium*: o segundo é o argumento de que microsserviços cobram um prêmio que nem todo sistema deve pagar.
- **Martin Fowler**, *StranglerFigApplication*: a técnica de transição do §5, na fonte.
- **Sam Newman**, *Building Microservices* (2ª ed.) e *Monolith to Microservices*: o segundo é praticamente um manual de padrões de extração.
- **Chris Richardson**, `microservices.io`: catálogo de padrões — *Decompose by Business Capability*, *Database per Service*, *Strangler Application*.
- **Amazon Prime Video Tech Blog** (2023), *Scaling up the Prime Video audio/video monitoring service*; e **Segment** (2018), *Goodbye Microservices*: os dois relatos discutidos em aula. Leia notando o **contexto específico** de cada um.

> **Na próxima aula (Aula 6 — comunicação entre microsserviços):** vamos atacar a dor nº 1. Se a URL do vizinho não pode ficar fixa, alguém precisa manter um **registro** de quem está no ar e onde — é o *service discovery*, com Eureka. E, do lado do chamador, vamos trocar o `RestClient` escrito à mão por um **cliente declarativo** (OpenFeign): você descreve a chamada numa interface e a biblioteca escreve o resto. De quebra, aparece uma pergunta incômoda: se todo mundo depende do registro para se achar, **o que acontece quando o registro cai?**
