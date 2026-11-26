# language: pt
@US06
Funcionalidade: Consultar o histórico de utilizações e pagamentos (US06, UC4)
  Como residente, quero consultar o histórico de utilizações e pagamentos,
  para saber quanto paguei e se tive coimas.

  Contexto:
    Dado a residência "Residência Ria Nova" com o hub "Hub Ria Nova" aberto das 07:00 às 23:00
    E a trotinete "RN-01" disponível no hub "Hub Ria Nova"
    E a trotinete "RN-02" disponível no hub "Hub Ria Nova"
    E o residente "Tiago" da residência "Residência Ria Nova"

  Cenário: O histórico mostra a utilização em curso e as anteriores, da mais recente para a mais antiga
    Dado o residente "Tiago" tem a trotinete "RN-01" desde as 08:00 por 60 minutos
    E o residente "Tiago" devolve a trotinete no hub "Hub Ria Nova" às 09:00
    E o residente "Tiago" tem a trotinete "RN-02" desde as 10:00 por 30 minutos
    Quando o residente "Tiago" consulta o histórico de utilizações
    Então vê as utilizações:
      | trotinete | estado    | pago   | coima  |
      | RN-02     | em curso  | 1,50 € |        |
      | RN-01     | terminada | 3,00 € | 0,00 € |

  @BR8
  Cenário: Os pagamentos mostram a duração paga e a coima, do mais recente para o mais antigo
    Dado o residente "Tiago" tem a trotinete "RN-01" desde as 08:35 por 240 minutos
    E o residente "Tiago" devolve a trotinete no hub "Hub Ria Nova" às 12:49
    Quando o residente "Tiago" consulta os pagamentos
    Então vê os pagamentos:
      | tipo       | trotinete | valor   | estado |
      | coima      | RN-01     | 2,80 €  | pago   |
      | utilização | RN-01     | 12,00 € | pago   |

  @BR8
  Cenário: Devolver a tempo não gera pagamento de coima
    Dado o residente "Tiago" tem a trotinete "RN-01" desde as 08:35 por 240 minutos
    E o residente "Tiago" devolve a trotinete no hub "Hub Ria Nova" às 12:44
    Quando o residente "Tiago" consulta os pagamentos
    Então vê os pagamentos:
      | tipo       | trotinete | valor   | estado |
      | utilização | RN-01     | 12,00 € | pago   |
