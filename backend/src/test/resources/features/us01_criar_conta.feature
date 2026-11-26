# language: pt
@US01
Funcionalidade: Criar conta pelo link de adesão (US01, UC1)
  Como residente, quero criar conta pelo link da minha residência,
  para ficar logo associado à subfrota dela.

  Contexto:
    Dado a residência "Residência Ria Nova" com o link de adesão ativo
    E a residência "Residência Salinas" com o link de adesão ativo

  @BR2
  Cenário: Quem cria conta pelo link fica associado à subfrota da residência
    Quando "Marta Lopes" cria conta com o email "marta@exemplo.pt" pelo link da "Residência Ria Nova"
    Então a conta de "Marta Lopes" é criada na subfrota da "Residência Ria Nova"

  Cenário: Um link desativado não cria conta
    Dado que o link de adesão da "Residência Salinas" foi desativado
    Quando "Marta Lopes" cria conta com o email "marta@exemplo.pt" pelo link da "Residência Salinas"
    Então a conta não é criada porque o link está desativado

  Cenário: Um link que não existe não cria conta
    Quando "Marta Lopes" cria conta com o email "marta@exemplo.pt" pelo link "link-que-nao-existe"
    Então a conta não é criada porque o link não existe

  @BR2
  Cenário: Uma conta pertence a uma só subfrota
    Dado que "Marta Lopes" criou conta com o email "marta@exemplo.pt" pelo link da "Residência Ria Nova"
    Quando "Marta Lopes" cria conta com o email "MARTA@exemplo.pt" pelo link da "Residência Salinas"
    Então a conta não é criada porque o email já está registado
