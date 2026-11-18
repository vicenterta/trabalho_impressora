# Controle de Impressora Elgin

Trabalho de Programação Orientada a Objetos: programa de console Java para controlar uma impressora Elgin i8 ou i9. Possui autenticação, configuração de conexão, impressão e operações auxiliares, com validação das entradas e tratamento de erros.

## Integrantes

- Eloisa Silveira
- Maria Rita
- Sophia Pupim

Conta prevista para publicação: https://github.com/eloisasilveira. O repositório foi preparado localmente; a publicação permanece pendente.

## Tecnologias

- Java JDK 21 (compilação verificada com Temurin 21.0.13).
- JNA 5.15.0 e E1_Impressora01.dll, fornecidos pela disciplina.
- Windows para comunicação com a DLL; IntelliJ IDEA como IDE opcional.
- Git para histórico das etapas.

## Executar

1. Extraia a pasta inteira, preservando as bibliotecas e os XMLs.
2. Instale um JDK 21. O script procura JAVA_HOME, a pasta local Programs/Java/jdk-21* e o PATH.
3. Dê dois cliques em `executar.cmd`.
4. Entre com usuário **admin** e senha **1234**. São permitidas três tentativas; a senha é demonstrativa e aparece no console.
5. Para testar hardware na escola, conecte e ligue a impressora, confira papel e driver/configuração necessários ao modelo. Configure o modelo correto e abra a conexão.

Configuração inicial: tipo 1 (USB), modelo i9, conexão USB, parâmetro 0. Para i8, altere pela opção 1. Para rede, informe IP e porta; para serial, porta COM e velocidade. A arquitetura da JVM determina a DLL x64 ou x86.

### IntelliJ IDEA

Abra a pasta como projeto Java, selecione o JDK 21 e importe o módulo `ImpressoraElgin.iml`, se necessário. Em Project Structure, marque `src` como Sources e adicione `libs Java/jna-5.15.0.jar` às Libraries caso não seja detectado. Execute `Main.java`, usando como Working directory a raiz do projeto, onde estão as bibliotecas e XMLs. Há uma configuração compartilhada em `.run`.

## Menu

| Opção | Função |
| --- | --- |
| 1 | Configurar tipo, modelo, conexão e parâmetro |
| 2 | Abrir conexão |
| 3 | Fechar conexão |
| 4 | Imprimir texto, cópias, alinhamento, estilo e tamanho |
| 5 | Imprimir QR Code |
| 6 | Imprimir código de barras |
| 7 | Imprimir XML SAT de venda |
| 8 | Imprimir XML de cancelamento SAT |
| 9 | Abrir gaveta |
| 10 | Emitir sinal sonoro |
| 11 | Avançar papel |
| 12 | Consultar estado do papel |
| 13 | Cortar papel |
| 0 | Encerrar e tentar fechar a conexão |

As opções 4 a 13 verificam a conexão antes de pedir dados. Cada comando verifica o retorno; impressão de texto aceita valores não negativos, enquanto a consulta de papel interpreta estados 5, 6 e 7. As demais operações exigem retorno zero. Sensores, gaveta, corte e bip dependem do equipamento.

## Organização

- `Main`: login, menu e perguntas ao usuário.
- `Usuario`: armazena e confere credenciais demonstrativas.
- `Entrada`: leitura e validação, com laços e tratamento de entradas inválidas.
- `Impressora`: estado da conexão, operações, leitura dos XMLs e mensagens de erro.
- `DispositivoImpressora`: contrato para comunicação e testes sem hardware.
- `AdaptadorElgin`: encaminha chamadas à DLL.
- `ImpressoraDLL`: interface original fornecida, preservada sem alterações.
- `testes/Testes.java`: verificações automáticas com dispositivo falso.

## Validação

Execute `testar.cmd`. Foram verificadas 38 condições, incluindo login, três falhas, números inválidos, bloqueio antes das perguntas, falha de abertura, DLL indisponível, cópias, retorno positivo de texto, interrupção após erro, assinatura do cancelamento, XML ausente/vazio, estados de papel e fechamento. Os testes usam um dispositivo falso: **não comprovam impressão física**.

### Testes pendentes na escola

- [ ] Abrir e fechar conexão na i8/i9 e registrar modelo/porta utilizados.
- [ ] Imprimir texto e várias cópias; conferir alinhamento e estilo.
- [ ] Imprimir e ler QR Code e código de barras.
- [ ] Imprimir ambos os XMLs de exemplo.
- [ ] Verificar avanço, corte, status, bip e gaveta disponível.
- [ ] Repetir cenário de erro/desconexão e conferir mensagens.

## Histórico e envio posterior

Os cinco commits representam preparação, login, conexão, operações e documentação/testes. A pasta oculta `.git` guarda esse histórico: preserve-a. O arquivo bundle entregue junto ao projeto também guarda os commits e pode ser restaurado com `git clone ImpressoraElgin.bundle ImpressoraElgin`.

Após conectar a conta correta, crie um repositório vazio nela e envie a branch `main` para esse destino. Não é necessário refazer os commits. O nome/e-mail de autoria Git deve ser conferido antes da publicação; a autoria local foi herdada da configuração existente do PC. Não foram inventadas contribuições das outras integrantes. Se o professor exigir commits de cada integrante, elas devem realizar suas próprias contribuições.

## Referências e créditos

Enunciado e arquivos iniciais fornecidos pela disciplina. Referência consultada, a pedido do grupo: https://github.com/leobiz0/projeto_impressora (organização em classes e fluxo de menu). A implementação foi escrita/adaptada com assistência de IA, incluindo tratamento centralizado de erros e testes sem hardware. As integrantes devem revisar e compreender o código para a apresentação.

Documentação Elgin indicada pelo enunciado: https://elgindevelopercommunity.github.io/group___m1.html
