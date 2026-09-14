# Checklist Semana 0 — Descoberta do Ambiente (BE-JV-009 · turma 1749)

> **Tarefa prévia (D-3, antes de 16/09).** Rode as seções abaixo na sua máquina da rede corporativa e devolva o resultado (print ou texto) no canal da turma. Nas turmas anteriores este checklist foi feito ao vivo e consumiu a Aula 1 — desta vez ele vem antes. Consolidação em `relatorio-semana-0.md`.

**Quando:** antes da Aula 1 (16/09); consolidação de 10 min na abertura.
**Por quê:** este módulo depende pouco de infra (só JVM + Maven + H2), mas a **Aula 6 (Eureka + OpenFeign)** precisa de artefatos **Spring Cloud** no Nexus. Sem fechar a seção 3, a Aula 6 fica no escuro (Plano B × Plano C).
**Saída:** `relatorio-semana-0.md` preenchido com o veredito por tema.

> Regra de ouro do ambiente: **se a turma vai baixar, vem do Nexus.** Use o `settings.xml` deste diretório (com URL/credenciais reais) antes de qualquer build.

---

## 1. Infra base (bloqueante)

| # | Verificação | Comando / como | Resultado esperado |
|---|---|---|---|
| 1.1 | Java 21+ | `java -version` | `21` ou superior |
| 1.2 | Maven | `mvn -version` | 3.8+ resolvendo do Nexus |
| 1.3 | `settings.xml` aponta p/ Nexus | `mvn -s settings.xml help:effective-settings` | mirror `*` → Nexus |
| 1.4 | IDE | IntelliJ / VS Code / Eclipse com suporte a Maven e Spring | abre e importa projeto Maven |

## 2. Docker / contêineres — sem necessidade neste módulo

Confirmado pelas turmas anteriores: **não há Docker nem sandbox** na rede. **Este módulo não precisa** — tudo roda como processos Java locais. Seção mantida só como registro.

## 3. Resolução de artefatos no Nexus (decide Plano B × C da Aula 6)

Rodar `mvn -s settings.xml dependency:get -Dartifact=<GAV>` para cada um. **Marque o que resolve.**

| # | Artefato | GAV | Resolve? |
|---|---|---|---|
| 3.1 | Spring Boot parent | `org.springframework.boot:spring-boot-starter-parent:3.5.5:pom` | ☐ |
| 3.2 | Web | `org.springframework.boot:spring-boot-starter-web:3.5.5` | ☐ |
| 3.3 | Data JPA | `org.springframework.boot:spring-boot-starter-data-jpa:3.5.5` | ☐ |
| 3.4 | Validation | `org.springframework.boot:spring-boot-starter-validation:3.5.5` | ☐ |
| 3.5 | Test (JUnit 5, Mockito, MockMvc) | `org.springframework.boot:spring-boot-starter-test:3.5.5` | ☐ |
| 3.6 | H2 | `com.h2database:h2:2.3.232` | ☐ |
| 3.7 | **Spring Cloud BOM** | `org.springframework.cloud:spring-cloud-dependencies:2025.0.0:pom` | ☐ |
| 3.8 | **Eureka Server** | `org.springframework.cloud:spring-cloud-starter-netflix-eureka-server:4.3.0` | ☐ |
| 3.9 | **Eureka Client** | `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.3.0` | ☐ |
| 3.10 | **OpenFeign** | `org.springframework.cloud:spring-cloud-starter-openfeign:4.3.0` | ☐ |
| 3.11 | **LoadBalancer** | `org.springframework.cloud:spring-cloud-starter-loadbalancer:4.3.0` | ☐ |

> **Se 3.7–3.11 NÃO resolverem:** a Aula 6 roda no **Plano C** (`RestClient` + `@HttpExchange` + registro estático de URLs) e abrimos chamado para publicar Spring Cloud no Nexus. O gabarito `aula-06` já traz o perfil `plano-c`.

> As versões acima são exemplos; se o Nexus tiver outra 3.5.x / 2025.x, anote qual.

## 4. Rede e processos locais

| # | Verificação | Como | Importa para |
|---|---|---|---|
| 4.1 | Subir app Spring em `localhost:8081` | `mvn spring-boot:run` no `inicio/cambio-api` e acessar `http://localhost:8081/h2-console` | qualquer aula |
| 4.2 | **3–4 processos Java simultâneos** em portas 8081/8082/8083/8761 | subir dois `spring-boot:run` ao mesmo tempo | Aulas 5–6 |
| 4.3 | `https://economia.awesomeapi.com.br/last/USD-BRL` | abrir no navegador / `curl` | esperado **bloqueado** → provedor local |
| 4.4 | Sites externos (start.spring.io, Maven Central, GitHub) | tentar abrir | confirma "tudo via Nexus"; decide como distribuir o repositório |
| 4.5 | `https://github.com/jcarlosvale/1749-be-jv-009` | abrir / `git clone` | se bloqueado, material vai por zip (Teams/LMS) |

## 5. Colaboração e didática no Teams (bloqueante p/ projeto em grupo)

| # | Verificação | Importa para |
|---|---|---|
| 5.1 | **Breakout rooms** liberados? | blocos 4 e 5 de toda aula |
| 5.2 | Compartilhar tela / dar controle? | pair/mob programming |
| 5.3 | **Planner / Tasks no Teams** disponível? | board Kanban do projeto guiado e dos grupos (fallback `KANBAN.md`) |
| 5.4 | Onde os grupos versionam código? (git interno? zip?) | entrega do projeto final |
| 5.5 | Copilot: tem agente na IDE? só chat M365? nenhum? | atividades de IA (opcionais) |

## 6. Devolutiva (responda no canal da turma)

```
Java: ___   Maven: ___   IDE: ___
Nexus resolve 3.1–3.6: sim/não   Spring Cloud 3.7–3.11: sim/não/parcial (quais)
awesomeapi: bloqueada/aberta   GitHub: bloqueado/aberto
Sobe 2 apps ao mesmo tempo: sim/não
Planner: sim/não   Breakout: sim/não   Copilot: agente/chat/nenhum
```
