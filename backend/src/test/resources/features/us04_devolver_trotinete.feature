# language: pt
@US04
Funcionalidade: Devolver a trotinete (US04, UC3)
  Como residente, quero devolver a trotinete no hub pela aplicação e ver se houve coima,
  para não ter surpresas.

  Contexto:
    Dado a residência "Residência Ria Nova" com o hub "Hub Ria Nova" aberto das 07:00 às 23:00
    E a residência "Residência Ria Nova" com o hub "Hub Ria Nova Sul" aberto das 07:00 às 23:00
    E a trotinete "RN-01" disponível no hub "Hub Ria Nova"
    E o residente "Tiago" da residência "Residência Ria Nova"
    E o residente "Tiago" tem a trotinete "RN-01" desde as 08:35 por 240 minutos

  Cenário: Devolver a tempo não tem coima
    Quando o residente "Tiago" devolve a trotinete no hub "Hub Ria Nova" às 12:30
    Então a utilização termina com 0 minutos de atraso e coima de 0,00 €
    E a trotinete "RN-01" fica disponível

  @BR8
  Esquema do Cenário: A coima começa aos 10 minutos de atraso e cresce com o atraso
    Quando o residente "Tiago" devolve a trotinete no hub "Hub Ria Nova" às <devolução>
    Então a utilização termina com <atraso> minutos de atraso e coima de <coima>

    Exemplos:
      | devolução | atraso | coima  |
      | 12:44     | 9      | 0,00 € |
      | 12:45     | 10     | 2,00 € |
      | 12:49     | 14     | 2,80 € |

  @BR7
  Cenário: A trotinete tem de ser devolvida no hub onde foi levantada
    Quando o residente "Tiago" devolve a trotinete no hub "Hub Ria Nova Sul" às 12:30
    Então o pedido é recusado pela BR7
    E a trotinete "RN-01" fica em uso
