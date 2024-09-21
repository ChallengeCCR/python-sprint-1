# Fazer um menu de navegação relacionada à solução


# Ver alertas atuais e históricos de alertas, ver histórico de alertas, 


# BEM-VINDO(A) AO SISTEMA


# COLOCAR A LÓGICA DO MENU DENTRO DE UMA FUNÇÃO (melhor) OU USAR UMA ESTRUTURA DE REPETIÇÃO (do, do-while)

# MÍNIMO DE 3 OPÇÕES
# 1. ALERTAS ATUAIS (exibe alertas atuais ou nenhum)
# 2. HISTÓRICO DE ALERTAS (busca o histórico de alertas)
# 3. INFORMAÇÕES OPERAÇÕES (operações normais, um pouco mais lentas)

# Falar que a opção é INVÁLIDA
# Deve incluir a opção de encerrar o programa


# EXEMPLO

import os

def cls():
    os.system('cls' if os.name=='nt' else 'clear')

from datetime import datetime

# FUNÇÃO PARA SEPARAR UTILIZANDO LINHAS
def hr(char, qtdVezes):
    return print(char * qtdVezes)

# PRIMEIRA TELA DE EXECUÇÃO
def inicioMenu():
    cls() # Limpa o menu anterior
    
    # Mensagem de boas vindas
    print("\tSeja bem-vindo!")
    hr("⎯", 30)
    # Opções principais
    print("Opções: ")
    print("[1] Registrar alertas")
    print("[2] Exibir alertas")
    print("[3] Deletar alertas")
    print("[4] Status de operação")
    
    hr("⎯", 30)
    # Opção de encerrar
    print("[0] Encerrar menu\n")

# Chama a função para iniciar
inicioMenu()

# Função para voltar menus
def voltarMenu():
    print("[0] Voltar para menu inicial")
    
    inicioMenu()

# Função para repetir outras funções, ainda não está 100%
def repetirAcao(nomeAcao, funcaoAcao):
    
    # Define opção
    repetirAcao = input(f"\nDeseja repetir \"{nomeAcao}\"? (S/N) ")
    opcao = repetirAcao.lower() 
    
    # Executa a função novamente
    if opcao == 's':
        funcaoAcao()
    else:
        inicioMenu()

alertasAtivos = [] # Onde os ALERTAS ATIVOS ficam armazenados

# Função para REGISTRAR ALERTAS e armazenar em @param Array[alertasAtivos] 
def registrarAlerta():
        # Opções de data
        data = datetime.now()
            
        print("Preencha as informações para registrar um alerta: ")
        
        # Variáveis dos objetos
        nomeAlerta = input("Nome: ")
        descricaoAlerta = input("Descrição: ")
        
        objetoAlerta = {
            "nome": nomeAlerta,
            "descricao": descricaoAlerta,
        }
        
        alertasAtivos.append(objetoAlerta)
        print(f"Seu alerta foi registrado!")
        
        repetirAcao("Registrar Alerta", registrarAlerta)

# Função para VISUALIZAR ALERTAS >ATIVOS<
def exibirAlertas():
    # Em python, utilizar colchetes [] para buscar por um atributo dentro de objeto Ex: print(alertasAtivos[0]["nome"])
    for alerta in alertasAtivos:
        i = 0
        print(f"Nome: {alerta["nome"]}\nDescrição: {alerta["descricao"]}")
        print("-" * 30)
        i =+ 1

# Condição que ativa o menu no loop
menu_ativo = True 

while (menu_ativo): 
    opcao = int(input())
    
    ## OPCAO 1 - REGISTRAR ALERTAS
    if opcao == 1:
        cls()
        registrarAlerta()
    
    ## OPCAO 2 - EXIBIR ALERTAS ATUAIS
    if opcao == 2:
        cls()
        if len(alertasAtivos) == 0:
            print("Nenhum alerta no momento, operações normais")
            voltarMenu()
        else:
            exibirAlertas()
            
    ## OPCAO 3 - DELETAR (ou "encerrar" ALERTAS EXIS
    if opcao == 3:
        # para deletar um alerta é necessário ser capaz de identificá-lo (adicionar ID) e movê-lo para uma nova array chamada "alertasExpirados"
        print("Deletar")

    if opcao == 0:
        cls()
        print("Gostaria de encerrar o sistema? (S/N)")
        encerrarSistema = input()
        
        if (encerrarSistema.lower() == "s"):
            menu_ativo = False
        else:
            inicioMenu()


# REGISTRAR ALERTAS E CADASTRAR EM UMA VARIÁVEL, SALVANDO APENAS DURANTE A EXECUÇÃO

