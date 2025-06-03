# 🚈 Autorail Monitor - API Backend

Essa API foi desenvolvida como parte do **Challenge CCR** da **FIAP**.

O **Autorail Monitor** é uma plataforma integrada desenvolvida para auxiliar tanto o CCO (Centro de Controle Operacional) e seus colaboradores quanto os passageiros, oferecendo uma gestão inteligente de alertas e status de serviços nas linhas 8 e 9 do transporte metropolitano.  
Essa solução promove maior controle e comunicação eficiente entre as equipes internas e o público, garantindo um sistema mais seguro, transparente e responsivo.

---

## ⚙️ Tecnologias Utilizadas

- **Java**
- **Quarkus**
- **JDBC com Oracle**
- **Railway (deploy)**

---

## 🚀 Deploy

A API por ser acessada em: ``Link``

É necessário passar a API Key armazenada em "application.properties" nos headers da requisição. 


# 📃 Rotas da API

## 📍 `/abrigo`

| Método  | Endpoint         | Descrição                                  | Códigos de status               |
|--------:|------------------|--------------------------------------------|----------------------------------|
| `POST`  | `/abrigo`        | Registra um novo abrigo a partir de um CEP | 201, 400, 500, 503               |
| `GET`   | `/abrigo/search` | Busca abrigos com filtros                  | 200, 400, 500                    |
| `GET`   | `/abrigo/{id}`   | Busca um abrigo por ID                     | 200, 404, 500                    |
| `PUT`   | `/abrigo/{id}`   | Atualiza dados de um abrigo por ID         | 200, 400, 404, 500, 503         |
| `DELETE`| `/abrigo/{id}`   | Deleta (logicamente) um abrigo por ID      | 200, 404, 500                    |

### 📑 Corpo para criação (`POST /abrigo`)
```json
{
  "idCidade": 1,                       
  "nomeAbrigo": "Estádio Municipal Carlos Ferracini",   
  "cep": "07700-660",                
  "capacidadeMaxima": 300,          
  "enderecoAbrigo": "Rua Portugal, 300, Avenida dos Estudantes, Caieiras - SP", 
  "telefoneContato": "(11) 98765-4321",
  "statusFuncionamento": "PARCIAL",     
  "nivelSegurancaAtual": "ALTO"        
}
```

### 📝 Corpo para atualização (`PUT /abrigo`)
```json
{
  "idCidade": 1,
  "nomeAbrigo": "Abrigo Estadial",
  "cep": "07700-660",
  "capacidadeMaxima": 600,
  "enderecoAbrigo": "Rua Portugal, 300, Avenida dos Estudantes, Caieiras - SP",
  "telefoneContato": "(11) 12345-6789",
  "statusFuncionamento": "PARCIAL",
  "nivelSegurancaAtual": "ALTO"
}
```
---

## 📍 `/cidade`

| Método  | Endpoint          | Descrição                                   | Códigos de status               |
|--------:|-------------------|---------------------------------------------|----------------------------------|
| `POST`  | `/cidades`        | Registra uma nova cidade a partir de um CEP | 201, 400, 500, 503               |
| `GET`   | `/cidades/search` | Busca cidades com filtros                   | 200, 400, 500                    |
| `GET`   | `/cidades/{id}`   | Busca uma cidade por ID                     | 200, 404, 500                    |
| `PUT`   | `/cidades/{id}`   | Atualiza dados de uma cidade por ID         | 200, 400, 404, 500, 503         |
| `DELETE`| `/cidades/{id}`   | Deleta (logicamente) uma cidade por ID      | 200, 404, 500                    |

### 📑 Corpo para criação (`POST /cidade`)
```json
{
  "cep": "01001-000",
  "nomeCidade": "São Paulo" // O `nomeCidade` é opcional; se fornecido, pode sobrescrever o nome obtido pelo ViaCEP.
}
```
### 📝 Corpo para atualização (`PUT /cidade`)
```json
{
  "cep": "01538-001",
  "nomeCidade": "São Paulo Atualizada" // O `nomeCidade` é opcional; se fornecido, pode sobrescrever o nome obtido pelo ViaCEP.
}
```

---

## 📍 `/ocorrencia`

| Método  | Endpoint              | Descrição                                     | Códigos de status       |
|--------:|-----------------------|-----------------------------------------------|-------------------------|
| `POST`  | `/ocorrencia`        | Registra uma nova ocorrência a partir de um CEP | 201, 400, 500, 503      |
| `GET`   | `/ocorrencia/search` | Busca ocorrências com filtros | 200, 400, 500           |
| `GET`   | `/ocorrencia/{id}`   | Busca uma ocorrência por ID                   | 200, 404, 500           |
| `PUT`   | `/ocorrencia/{id}`   | Atualiza dados de uma ocorrência por ID       | 200, 400, 404, 500, 503 |
| `DELETE`| `/ocorrencia/{id}`   | Deleta (logicamente) uma ocorrência por ID    | 200, 404, 500           |

### 📑 Corpo para criação (`POST /ocorrencia`)
```json
{
  "cep": "07724-030",
  "tipoOcorrencia": "QUEIMADA",
  "nivelGravidade": "ALTO",
  "idCidade": 1
}
```
### 📝 Corpo para atualização (`PUT /ocorrencia`)
```json
{
  "cep": "07724-030",
  "tipoOcorrencia": "QUEIMADA",
  "nivelGravidade": "BAIXO",
  "idCidade": 1
}
```

---

## 📍 `/usuario`

| Método  | Endpoint                        | Descrição                               | Códigos de status         |
|--------:|---------------------------------|-----------------------------------------|----------------------------|
| `POST`  | `/usuario`                      | Registra um novo usuário                | 201, 400, 500              |
| `GET`   | `/usuario/search`               | Busca usuários com filtros              | 200, 400, 500              |
| `GET`   | `/usuario/{id}`                 | Busca um usuários por ID                | 200, 404, 500              |
| `PUT`   | `/usuario/{id}`                 | Atualiza nome, telefone ou cidade de um usuário por ID | 200, 400, 404, 500         |
| `PUT`   | `/usuario/atualizar-email/{id}` | Atualiza o e-mail de um usuário por ID  | 200, 400, 404, 500         |
| `PUT`   | `/usuario/atualizar-senha/{id}` | Atualiza a senha de um usuário por ID   | 200, 400, 404, 500         |
| `PUT`   | `/usuario/atualizar-telefone/{id}`| Atualiza o telefone de um usuário por ID| 200, 400, 404, 500         |
| `DELETE`| `/usuario/{id}`                 | Deleta um usuário por ID                | 200, 404, 500              |

### 📑 Corpo para criação (`POST /usuario`)
```json
{
  "nomeUsuario": "Mateus",
  "tipoUsuario": "CLIENTE",
  "autenticaUsuario": {
    "emailUsuario": "devmtslma@email.com",
    "senhaUsuario": "senha1234"
  },
  "telefoneContato": "+55 11 12345-6789",
  "idCidade": 2
}
```

### 📝 Atualização de e-mail (`PUT /usuario/atualizar-email/{id}`)
```json
{
  "autenticaUsuario": {
    "emailUsuario": "email@exemplo.com"
  }
}
```

### 📝 Atualização de senha (`PUT /usuario/atualizar-senha/{id}`)
```json
{
  "autenticaUsuario": {
    "senhaUsuario": "mPiramide24"
  }
}
```

### 📝 Atualização de cidade (`PUT /usuario/atualizar-cidade/{id}`)
```json
{
  "idCidade": 2
}
```

### 📝 Atualização de telefone (`PUT /usuario/atualizar-telefone/{id}`)
```json
{
  "telefoneContato": "11 98765-4321"
}
```
---

## 📍 `/autenticacao`

| Método  | Endpoint         | Descrição                                           | Códigos de status         |
|--------:|------------------|-----------------------------------------------------|----------------------------|
| `POST`  | `/autenticacao`  | Valida o login e retorna um token de sessão        | 200, 400, 401, 500         |

### 📑 Corpo para autenticação (`POST /autenticacao`)
```json
{
  "email": "exemplocolab@email.com",
  "senha": "maxverstappen33"
}
```
---

## 📍 `/sessao`

| Método  | Endpoint             | Descrição                                                        | Códigos de status         |
|--------:|----------------------|------------------------------------------------------------------|----------------------------|
| `GET`   | `/{token}`           | Busca informações de uma sessão pelo token                       | 200, 404, 500              |
| `PUT`   | `/sessao/logout`     | Torna uma sessão inativa e nega o acesso por meio do mesmo token | 200, 400, 404, 500         |

### 📝 Corpo para logout (`PUT /sessao/logout`)
```json
{
  "tokenSessao": "fb4aea98-d71a-4a73-82d8-0d99e6d8aa33"
}
```


