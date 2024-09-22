## COMENTÁRIOS 

# MELHORARA A MENSAGEM PARA "STRING VAZIA", ADICIONAR INFORMAÇÕES 

# ADICIONAR OPÇÃO PARA CANCELAR OPERAÇÕES, EX: CANCELAR REMOVER

import os
from datetime import datetime

# Função para limpar o console
def cls():
    os.system('cls' if os.name == 'nt' else 'clear')

menuOptions = ["1", "2", "3", "0"] # Opções existentes
activeAlerts = []  # Lista para armazenar os alertas

# Print() - Quebra linha
# Input() - Está sendo usado para confirmar o retorno ao Menu Inicial

# Função para exibir as opções do menu inicial
def startMenu():
    print("Escolha uma opção:")
    print()
    print("[1] Registrar alerta")
    print("[2] Remover alerta")
    print("[3] Exibir alertas")
    print()
    print("[0] Encerrar programa") 
    print()

# Função para receber e validar as opções escolhidas
def getUserOption():  
    # Chama o menu inicial
    startMenu()
    # Validação
    userOption = input()
    
    if userOption in menuOptions:
        return userOption
    else: 
        cls()
        print("\t!!! OPÇÃO INVÁLIDA")
        print()  
        return getUserOption()

# Função para verificar se input é um INT
def verifyInt(inputStr):  
    try:
        # Tenta converter a entrada para um número inteiro
        numero = int(inputStr)  
        return numero
    except ValueError:
        # Mensagem de erro, depois precisa implementar o mesmo loop de verifyBlankString para o usuário tentar novamente
        cls()
        print("O valor fornecido precisa ser do tipo INTEIRO")
        
# Função para verificar se input é VAZIA ou SÓ TEM ESPAÇOS
def verifyBlankString(inputName):
    
    userInput = input(f"{inputName}: ")
    
    if not userInput.strip():
        cls()
        print(f"O campo \"{inputName}\" não pode ser vazio!")
        return verifyBlankString(inputName)  # Executa a função para tentar novamente
    else:
        return userInput


### LOOP PRINCIPAL, ONDE OS COMANDOS SERÃO RECEBIDOS E EXECUTADOS
while True: # Verificação das escolhas para executar determinada opção
    cls()
    userOption = getUserOption()
    
    ## PRIMEIRA OPÇÃO
    if userOption == "1":  # Registrar alertas
        cls()
        print("\t### REGISTRAR ALERTA")
        print()
        print("# Informe os seguintes dados para registrar")
        print()
        
        # Informações de registro, ID e hora de registro 
        alertId = f"{len(activeAlerts)}"
        alertTimestamp = str(datetime.now()).split('.')[0] # Indica a hora atual e remove os milisegundos 
    
        # Informações de registro, Nome e Descrição, são verificadas e não podem ser vazia
        alertName = verifyBlankString("Nome")
        alertDescription = verifyBlankString("Descrição")
        
        # Objeto que será registrado
        alert = {
            "id": alertId,
            "timestamp": alertTimestamp,
            "nome": alertName,
            "descricao": alertDescription
        }
        
        # Adiciona o alerta na Array
        activeAlerts.append(alert)
        
        # Input para voltar para o menu
        cls()
        print(f"\nAlerta \"{alertName}\" registrado!")
        print("Pressione ENTER para voltar ao menu inicial.")
        input()
        cls()
        
    ## SEGUNDA OPÇÃO
    elif userOption == "2":  # Remover alertas
        cls()
        # Verifica se há alertas existentes
        if not activeAlerts:
            print("Nenhum alerta ativo.")
        else:
            print("\t### REMOVER ALERTA")
            print()
            # Lista todos os alertas
            for alert in activeAlerts:
                print(f"ID: {alert["id"]} | {alert["nome"]}")
                print("_" * 60)
                print()
            
            print("# Insira o ID do alerta que deseja remover: ")
        
            # Usa a função verifyInt para ver se o valor string pode ser convertido para INT, caso contrário alerta um erro
            idToRemove = verifyInt(input())
            
            # Verifica se o ID é válido dentro da array e o remove
            if type(idToRemove) == int: # TESTAR REMOVER ISSO, ACHO QUE NÃO É NECESSÁRIO !!!          
                if  0 >= idToRemove < len(activeAlerts): 
                        cls()
                        print(f"\nAlerta \"{activeAlerts[idToRemove]["nome"]}\" removido!")
                        activeAlerts.pop(idToRemove)
                else: 
                    cls()
                    print("Esse ID não existe!")
                    
        # Volta ao início do programa
        print()
        print("Pressione ENTER para voltar ao Menu Inicial")
        input()
        cls()
        
    ## TERCEIRA OPÇÃO
    elif userOption == "3":  # Remover alertas
        cls()
        # Lista todos os alertas
        if not activeAlerts:
            print("Nenhum alerta ativo.")
        else:
            print("\t### ALERTAS ATIVOS")
            print()
            for alert in activeAlerts:
                print(f"ID: {alert["id"]}\nNome: {alert["nome"]}\nDescrição: {alert["descricao"]}\nTimestamp: {alert["timestamp"]}")
                print("_" * 60)
                print()
             
        # Volta ao início do programa
        print()
        print("Pressione ENTER para voltar ao Menu Inicial")
        input()
        cls()
        
    ## ENCERRAR SISTEMA
    if userOption == "0":
        cls()
        print("Deseja mesmo encerrar o programa? (S/ N)")
        confirmAction = input()
        
        # Confirma se realmente deseja sair
        if confirmAction.lower() == "s":  
            cls()
            print("Encerrando...")
            cls()
            exit() 


