# Projeto Final em Grupo — BE-JV-009 (Nível II)

**Formato:** grupos (3–4 integrantes) · **tema livre** · apresentação na **Aula 9 (05/10)**.
**Apresentado na Aula 1.** Desenvolvido majoritariamente **dentro do tempo de aula** (2ª metade de cada encontro) + apresentação na Aula 9.
**Entrega:** repositório (ou zip) até **05/10 às 09:00 BRT**, antes das apresentações.

---

## 1. Princípio: o tema é livre, os critérios são a amarra

O grupo **escolhe o domínio e o produto**. O que **não** é negociável são os **critérios de avaliação** (§4): o projeto precisa **exercitar as competências do módulo de forma justificada** — ágil vivido, arquitetura decidida (e não só desenhada), testes que protegem, código limpo, SOLID e padrões onde fazem sentido. A API de Câmbio (projeto guiado) é apenas **uma sugestão de referência**; copiá-la não é o objetivo, transferir as ideias para um problema próprio é.

### Sugestões de tema (escolher uma OU propor a sua)
- **Agendamento de atendimento em agência** — cliente, agência, horários, fila; serviço de "capacidade" separado.
- **Portabilidade de salário / cadastro de conta-destino** — cadastro, validação de banco/agência/conta, ordem de portabilidade.
- **Consórcio: cotas e lances** — participante, grupo, lance; serviço de cálculo separado.
- **Cartão pré-pago: recarga e saldo** — titular, cartão, recarga com cotação/tarifa.
- **Qualquer domínio do dia a dia do grupo** que justifique **≥ 2 serviços conversando** e **1 integração** (interna ou simulada) — preferível, pois aproxima do trabalho real.

---

## 2. Escopo escalável

O esforço acompanha o tamanho do grupo. **CORE é obrigatório**; cada integrante acima de 3 assume **1 item opcional**.

**CORE (todo grupo entrega):**
- **Backlog + board Kanban** (`KANBAN.md` ou board no Teams/Planner) com histórias, critérios de aceite e o histórico das 3 sprints do módulo.
- **≥ 2 serviços Spring Boot** com **bases segregadas**, nascidos de uma decomposição **justificada** (ADR: por que cortar aqui).
- **Comunicação entre serviços** por client HTTP (OpenFeign com discovery **ou** `RestClient`/`@HttpExchange` com registro estático — declarado no README).
- **Testes** nos 3 níveis vistos: unitário (service com Mockito), controller (`@WebMvcTest`) e ≥ 1 de integração (`@SpringBootTest` + H2). `mvn test` verde.
- **Clean Code + SOLID** visíveis: injeção por construtor, sem números mágicos, classes com uma responsabilidade, dependência de abstrações.
- **≥ 1 Design Pattern de cada grupo** (criacional · estrutural · comportamental) **com justificativa** — não pattern por pattern.
- **ADRs** (≥ 3) + **README** de arquitetura (visão dos serviços, como rodar).

**Opcionais (+1 por integrante acima de 3):**
- Contrato de serviço documentado (OpenAPI/springdoc ou markdown) com versionamento.
- Service discovery (Eureka) com ≥ 2 instâncias de um serviço e balanceamento demonstrado.
- Integração com provedor "externo" simulado via **Adapter/ACL** (com fallback local).
- Tratamento de falha parcial (timeout + resposta degradada + teste que prova).
- Burndown/métricas da sprint + retro documentada.
- **Uso documentado e crítico de IA** no design/desenvolvimento (evidência do critério 9).

**Grupos:** `nº de grupos = teto(N/4)`. Papéis rotativos: **PO/arquiteto · dev serviço A · dev serviço B · dev testes/qualidade**. N<6 → 1–2 grupos com CORE; N grande → teto 5/grupo, apresentações em rodadas.

---

## 3. Contrato de entrega (estrutura padronizada do repositório)

```
<projeto-do-grupo>/
├── README.md                # tema, problema, visão de arquitetura, como rodar (com/sem discovery)
├── AVALIACAO.md             # auto-avaliação: cada critério → evidência (arquivo/commit). Ver §5.
├── KANBAN.md                # backlog + board (ou link para o board) + histórico das sprints
├── docs/
│   ├── adr/                 # 1 arquivo por decisão (ADR-001-..., formato curto)
│   └── arquitetura.md       # diagrama dos serviços (ASCII/imagem) + fluxo das chamadas
├── <servico-1>/ ... <servico-N>/   # um módulo por serviço, base segregada
└── pom.xml                  # multi-módulo
```

**Regras que tornam a entrega avaliável:**
- `AVALIACAO.md` é **obrigatório** e mapeia **cada critério → evidência** (caminho de arquivo, classe, teste ou commit). Sem ele, a nota não considera o que não for encontrado.
- O grupo declara no README **como rodou** (com Eureka/Feign ou registro estático). **Não há penalização por restrição de ambiente.**
- **Commits ao longo das semanas** (não um único dump) — evidência de processo e de participação de todos. O prazo é verificado pela data do commit no servidor (committer-date, UTC).

---

## 4. Critérios de avaliação (rubrica)

Pesos somam 100. Cada critério tem 4 níveis: **0 Insuficiente · 1 Básico · 2 Proficiente · 3 Avançado**. Nota do critério = `(nível/3) × peso`. A coluna **Evidência esperada** diz onde procurar (no código e no `AVALIACAO.md`).

| # | Critério | Peso | Evidência esperada |
|---|---|:---:|---|
| 1 | **Ágil vivido** (backlog priorizado, critérios de aceite, board mantido, sprints com review/retro) | 10 | `KANBAN.md`/board; histórico de movimentação; retro documentada |
| 2 | **Decomposição arquitetural** (corte por domínio justificado, bases segregadas, o que ficou junto e por quê) | 15 | módulos por serviço; `docs/arquitetura.md`; ADR de corte |
| 3 | **Comunicação entre serviços** (client HTTP, tratamento de 404/indisponível, discovery ou registro declarado) | 10 | clients; `@RestControllerAdvice`; README "como rodar" |
| 4 | **Qualidade e testes** (pirâmide: unitário, controller, integração; testes protegem a refatoração) | 12 | pastas `src/test`; `mvn test` verde; README "onde cada teste cai" |
| 5 | **Clean Code** (nomes, funções curtas, constantes, organização por domínio) | 10 | código; trecho antes/depois no `AVALIACAO.md` |
| 6 | **SOLID** (construtor, SRP, abstrações — LSP/ISP/DIP demonstrados) | 12 | classes/interfaces; exemplo de cada princípio apontado |
| 7 | **Design Patterns** (≥ 1 por grupo, aplicados a um problema real do projeto, com justificativa) | 12 | classes; `docs/patterns.md` ou ADR por padrão |
| 8 | **Decisões arquiteturais** (ADRs com alternativas e trade-offs reais, não descrição) | 8 | `docs/adr/` |
| 9 | **Uso crítico de IA** (como usaram IA no design/dev; o que validaram à mão) | 5 | seção no README/`AVALIACAO.md`; reflexão honesta |
| 10 | **Execução comprovada** (roda como o README diz; demo ao vivo) | 6 | README "como rodar"; demo na Aula 9 |

**Atitudinais / apresentação (à parte, para participação):** clareza na defesa, resposta à arguição individual, colaboração no grupo, avaliação entre pares.

> **Princípio anti-ambiente:** os critérios avaliam **domínio das ideias e qualidade da decisão**, não a infra disponível. Um grupo que rodou sem Eureka (registro estático) e **justificou bem** pode tirar nota máxima.

---

## 5. `AVALIACAO.md` — template que o grupo preenche

```markdown
# Auto-avaliação — <nome do projeto>
Grupo: <integrantes e papéis>
Tema/domínio: <descrição em 2 linhas>
Como rodamos: Eureka+Feign | registro estático  ·  Restrições encontradas: <quais>

## Evidências por critério
1. Ágil vivido — nível auto-atribuído: X
   Evidência: <KANBAN.md, board, retro>
2. Decomposição arquitetural — X
   Evidência: <serviços, bases, docs/arquitetura.md, ADR-00X>
3. Comunicação entre serviços — X
4. Qualidade e testes — X
5. Clean Code — X  (trecho antes/depois)
6. SOLID — X  (um exemplo por princípio)
7. Design Patterns — X  (pattern → classe → por quê)
8. Decisões arquiteturais — X  → ver docs/adr/
9. Uso crítico de IA — X
   Como usamos IA e o que validamos manualmente: <texto honesto>
10. Execução — X
   Como rodar: <comandos>

## Opcionais entregues
<lista, se grupo > 3 pessoas>

## Participação (quem fez o quê)
<por integrante — será confrontado com o histórico do git e com a arguição>
```

---

## 6. Apresentação (Aula 9 · 05/10)
Cada grupo: **demo** (2 serviços conversando) + **defesa das decisões** (não só "o que faz", mas "por que assim" e "o que descartamos") + **arguição individual** do docente e dos pares (cada integrante responde por uma parte) + reflexão sobre uso de IA. Tempo por grupo ≈ 15 min (10 apresentação + 5 arguição).
