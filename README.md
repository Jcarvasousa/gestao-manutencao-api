# Sistema de Gestão de Manutenção

API REST para gestão operacional de manutenção industrial — controle de máquinas, ordens de manutenção, estoque de peças e solicitações de compra.

## O problema

A empresa possui um ERP corporativo, mas a equipe de manutenção não o utiliza de forma completa no dia a dia por questões de custo de licença, custo de treinamento e alta rotatividade de operadores treinados. Isso resultou em controle paralelo manual (planilhas), sem confiabilidade sobre o estoque real de peças.

**Consequência concreta**: quando uma máquina quebra, a equipe verifica se existe a peça necessária. Sem controle confiável, é comum que uma peça já existente não seja localizada corretamente — a equipe então inicia todo o processo de compra (cotação, fornecedor, aprovação, prazo de entrega, máquina parada aguardando) para, no final, descobrir que a peça já estava disponível. O problema não é o custo da compra em si — é a falta de confiabilidade da informação de estoque, que gera desperdício de tempo e dinheiro em compras desnecessárias.

## A decisão de escopo

O objetivo **não é substituir o ERP corporativo**. É criar uma ferramenta operacional simples, para uso direto da equipe de manutenção, que resolva especificamente a lacuna entre o ERP e a operação prática do dia a dia — com foco em confiabilidade de estoque e prevenção de compras desnecessárias.

## O que o sistema resolve

A regra central do sistema: **uma solicitação de compra só pode ser criada se o estoque atual for insuficiente para a necessidade da manutenção.** Se a peça já existe em quantidade suficiente, o sistema bloqueia a criação da solicitação automaticamente — a checagem que faltava no processo manual.

Além disso, toda entrada de peça (compra recebida) atualiza o estoque automaticamente, na mesma transação, eliminando o risco de divergência entre "o que foi comprado" e "o que o sistema diz que existe".

## Fluxo principal

Máquina quebra → abre-se Manutenção → identifica peça necessária
→ consulta estoque
→ se disponível: registra saída (vinculada à manutenção)
→ se insuficiente: sistema permite solicitação de compra
→ compra recebida → estoque atualiza automaticamente

## Stack

- Java 25
- Spring Boot 4
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven

## Modelo de domínio

| Entidade | Responsabilidade |
|---|---|
| `Maquina` | Cadastro de máquinas e status operacional |
| `Peca` | Estoque de peças, localização física, estoque mínimo |
| `Manutencao` | Ordens de manutenção, vinculadas a uma máquina |
| `MovimentacaoEstoque` | Histórico de entradas/saídas de peça, rastreável por manutenção |
| `SolicitacaoCompra` | Solicitações de compra, com a regra de bloqueio de compra desnecessária |

## Endpoints principais

- `POST /api/pecas` — cadastra peça
- `GET /api/pecas/abaixo-do-minimo` — lista peças que precisam de reposição
- `POST /api/manutencoes` — abre ordem de manutenção
- `POST /api/movimentacoes-estoque/saida` — registra saída de peça vinculada a manutenção
- `POST /api/solicitacoes-compra` — solicita compra (bloqueada se estoque já suficiente)
- `PATCH /api/solicitacoes-compra/{id}/receber` — marca compra como recebida (gera entrada automática de estoque)

## Status do projeto

MVP em desenvolvimento. Núcleo funcional (entidades, regras de negócio, API REST) implementado e testado manualmente via Postman. Pendente: validação de entrada formal, testes automatizados, autenticação, frontend.

## Contexto

Projeto desenvolvido a partir de um problema operacional real, identificado através de acesso profissional à operação de manutenção de uma empresa de médio/grande porte do setor industrial. Dados de exemplo utilizados no desenvolvimento e testes são fictícios.