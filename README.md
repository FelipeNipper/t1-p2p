# Programação Distribuída - Trabalho 1
Consiste na implementação de um sistema P2P básico, com uma arquitetura centralizada,
onde o controle de toda a aplicação (lógica e estado) é concentrado em um
computador servidor. Um único programa deve ser utilizado, configurado em um dos dois modos de operação (servidor/peer P2P).
Para isso, pode-se passar essa informação como parâmetro durante a carga do
programa, juntamente com outras informações de configuração, se necessário.

## Funcionalidades
- [x] Os peers devem se reegistrar no servidor para poderem realizar a troca de arquivos entre si.

- [x] Durante o registro, cada peer informa seus recursos disponiveis (um diretorio com alguns arquivos, calcule o hash de cada um). Para cada arquivo, o peer fornece ao servidor o nome do arquivo e sua hash, calculada sobre o conteúdo.

- [x] O servidor associa cada recurso em uma estrutura de dados.

- [x] Cada recurso possuí associado o IP do peer onde esta o recurso e sua hash.

- [x] Os peers podem solicitar uma lista de recursos (nomes dos arquivos/strings de identificação, IPs dos peers que contém os recursos e hashes) ai servidor ou um recurso específico

- [x] Ao solicitar um recurso ao servidor, o peer recebe a informação sobre sua localização
  (outro peer) e deve realizar essa comunicação diretamente com o mesmo
  
- [x] O servidor é responsavel por manter a estrutura da rede de overlay

- [x] Os peers devem contatar o servidor a cada 10 segundos, se em 30 segundos o peer não 
  contatar o servidor, o peer em questão é removido.
  
## Critérios

O trabalho ve ser feito usando JAVA RMI ou RPC, com excessao da comunicação direta entre os 
peers que poderá ser feita utilizando sockets, RPC ou RMI.

É sugerido a utilização de uma rede com topologia definida, e que sejam realizados testes
com um numero suficiente de máquinas:

- [x] 3 VMSs
- [x] Pelo menos 6 teminais

A entrega deve ser um arquivo .tar.gz contendo o código fonte da implementação e uma lista
de nomes completos dos integrantes do grupo.
