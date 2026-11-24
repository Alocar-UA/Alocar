# Rastreabilidade requisitos × testes

Liga as histórias e regras de negócio do Product Specs (secções 2.3 e 3.2) aos testes que as cobrem.
Atualizar no mesmo PR que adiciona ou altera um teste. Os cenários Gherkin têm as mesmas tags
(`@US03`, `@BR6`, …), por isso `grep -r "@BR6" backend/src/test/resources/features` encontra-os.

Quando as histórias existirem no Jira (projeto SCRUM), juntar a chave a cada linha e anotar os testes
JUnit com `@Requirement("SCRUM-n")` (xray-junit-extensions) para o Xray fazer esta ligação sozinho.

## Histórias

| História | Jira | Estado | Testes de aceitação (Gherkin) | Testes de integração |
| -------- | ---- | ------ | ----------------------------- | -------------------- |
| US02 Ver trotinetes livres e horário do hub | SCRUM-90 | Feito (API) | — | `HubControllerIT.listHubsShowsOpeningHoursAndFreeScooters`, `UseControllerIT.hubListsItsScootersWithStatus` |
| US03 Levantar trotinete e pagar a duração | SCRUM-91 | Feito (API) | `us03_levantar_trotinete.feature` (6 cenários) | `UseControllerIT` |
| US04 Devolver no hub e ver a coima | SCRUM-92 | Feito (API) | `us04_devolver_trotinete.feature` (5 cenários) | `UseControllerIT.residentPicksUpReturnsLateAndSeesTheFineInTheHistory` |
| US06 Histórico de utilizações e pagamentos | SCRUM-94 | Feito (API) | `us06_historico.feature` (3 cenários) | `UseControllerIT.residentPicksUpReturnsLateAndSeesTheFineInTheHistory`, `UseControllerIT.residentSeesTheDurationAndTheFineInThePayments` |

## Regras de negócio

| Regra | Resumo | Testes unitários | Aceitação / integração |
| ----- | ------ | ---------------- | ---------------------- |
| BR2 | Só usa trotinetes da subfrota da sua residência | `UseServiceTest.br2ScooterOfAnotherSubfleetIsRefused` | `@BR2` em US03 |
| BR3 | Trotinete disponível, hub aberto (6:59 recusado, 7:00 aceite) | `HubOpeningHoursTest.br3StartAt659IsRefusedAndAt700IsAccepted`, `UseServiceTest.br3*` | `@BR3` em US03, `UseControllerIT.br3*` |
| BR4 | Paga duração × tarifa; duração entre o mínimo e o máximo | `TariffTest.br4*`, `UseServiceTest.br4DurationOutsideTheLimitsIsRefused` | `@BR4` em US03 |
| BR5 | A utilização acaba antes de o hub fechar (22:00 + 60 aceite, 61 recusado) | `HubOpeningHoursTest.br5*`, `UseServiceTest.br5UseMustEndBeforeTheHubCloses` | `@BR5` em US03 |
| BR6 | Uma utilização ativa por residente | `UseServiceTest.br6SecondActiveUseIsRefused` | `@BR6` em US03, `UseControllerIT.br6SecondActiveUseIsRefused` |
| BR7 | Devolução no hub de levantamento | `UseServiceTest.br7ReturnAtAnotherHubIsRefused` | `@BR7` em US04, `UseControllerIT.br7ReturnAtAnotherHubIsRefused` |
| BR8 | Sem coima abaixo de 10 min; coima por minuto a partir de 10 min, cobrada na devolução | `TariffTest.br8*`, `UseServiceTest.br8*`, `UseServiceTest.declinedFineEndsTheUseAndIsRecordedAsDeclined` | `@BR8` em US04 e US06 |
| BR1, BR9–BR12 | Transferências, telemetria, alerta de roubo, avarias, âmbito por papel | — | Por implementar |

## Ainda em aberto

- BR7 com telemetria: hoje o residente indica o hub onde deixou a trotinete; falta confirmar com a
  localização enviada pelo simulador de frota.
- Valores «proposta» em `application.properties` (`alocar.pricing.*`): 0,05 €/min, coima 0,20 € por
  minuto de atraso a partir de 10 min, duração entre 10 e 480 min. Confirmar com o grupo.
- Pagamento: `SandboxPaymentGateway` aprova tudo. Desde a US06 cada cobrança fica em `payments`
  (duração paga no levantamento e coima cobrada na devolução); uma coima recusada termina na mesma a
  utilização e fica registada como `DECLINED` (em dívida). Falta decidir com o grupo o que acontece a
  quem tem coimas em dívida (por exemplo, impedir nova utilização).
