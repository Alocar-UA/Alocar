# language: pt
@US03
Funcionalidade: Levantar uma trotinete (US03, UC2)
  Como residente, quero levantar uma trotinete escolhendo quanto tempo a vou usar
  e pagar pela aplicação, sem chaves nem intermediários.

  Contexto:
    Dado a residência "Residência Ria Nova" com o hub "Hub Ria Nova" aberto das 07:00 às 23:00
    E a trotinete "RN-01" disponível no hub "Hub Ria Nova"
    E a trotinete "RN-02" disponível no hub "Hub Ria Nova"
    E o residente "Tiago" da residência "Residência Ria Nova"

  @BR4
  Cenário: Levantar uma trotinete livre e pagar a duração escolhida
    Dado que são 08:35 de segunda-feira
    Quando o residente "Tiago" levanta a trotinete "RN-01" por 240 minutos
    Então a utilização começa com fim previsto às 12:35
    E o residente pagou 12,00 €
    E a trotinete "RN-01" fica em uso

  @BR3
  Esquema do Cenário: Só se levanta uma trotinete com o hub aberto
    Dado que são <hora> de segunda-feira
    Quando o residente "Tiago" levanta a trotinete "RN-01" por 30 minutos
    Então o pedido é <resultado>

    Exemplos:
      | hora  | resultado          |
      | 06:59 | recusado pela BR3  |
      | 07:00 | aceite             |

  @BR5
  Esquema do Cenário: A duração escolhida tem de acabar antes de o hub fechar
    Dado que são 22:00 de segunda-feira
    Quando o residente "Tiago" levanta a trotinete "RN-01" por <minutos> minutos
    Então o pedido é <resultado>

    Exemplos:
      | minutos | resultado         |
      | 60      | aceite            |
      | 61      | recusado pela BR5 |

  @BR6
  Cenário: Um residente só tem uma utilização ativa de cada vez
    Dado o residente "Tiago" tem a trotinete "RN-01" desde as 09:00 por 60 minutos
    Quando o residente "Tiago" levanta a trotinete "RN-02" por 60 minutos
    Então o pedido é recusado pela BR6
    E a trotinete "RN-02" fica disponível

  @BR3
  Cenário: Uma trotinete em uso não pode ser levantada por outro residente
    Dado o residente "Rita" da residência "Residência Ria Nova"
    E o residente "Tiago" tem a trotinete "RN-01" desde as 09:00 por 60 minutos
    Quando o residente "Rita" levanta a trotinete "RN-01" por 60 minutos
    Então o pedido é recusado pela BR3

  @BR2
  Cenário: Um residente só usa trotinetes da subfrota da sua residência
    Dado a residência "Residência Salinas" com o hub "Hub Salinas" aberto das 07:00 às 23:00
    E o residente "Hugo" da residência "Residência Salinas"
    E que são 10:00 de segunda-feira
    Quando o residente "Hugo" levanta a trotinete "RN-01" por 60 minutos
    Então o pedido é recusado pela BR2
