# ContAudIT: Auditoria Contínua em Gestão de Mudanças de TI Usando Blockchain

[Click here to access the English version.](README.md)

Este projeto está organizado em cinco pastas:

- [**contaudit-chaincode**](contaudit-chaincode/) - Projeto múltiplo de contratos inteligentes utilizados na blockchain do experimento. 

- [**contaudit-admin**](contaudit-admin/) - Projeto de aplicativo cliente utilizado pelas auditorias do experimento para configuração da blockchain. 

- [**contaudit-wrapper**](contaudit-wrapper/) - Projeto de aplicativo cliente utilizado pelas organizações auditadas do experimento.

- [**contaudit-samples**](contaudit-samples/) - Repositório de ferramentas e artefatos de exemplo usados pelas organizações auditadas do experimento.

- [**contaudit-checker**](contaudit-checker/) - Projeto de aplicativo cliente utilizado pelas auditorias do experimento para análises em auditorias em curso.

## Pré-Requisitos

[Git](https://git-scm.com)

[Docker](https://www.docker.com)

[Minifabric 2.3](https://github.com/hyperledger-labs/minifabric)

[Java 17](https://www.oracle.com/java/technologies/downloads/#java17)

[Gradle 7.0](https://gradle.org/next-steps/?version=7.0&format=bin)

[Python](https://www.python.org)

[R](https://www.r-project.org)

## Começando

> **Importante**: Execute todos os comandos abaixo no diretório de trabalho local deste repositório.

> **Importante**: Todos os comandos abaixo foram testados apenas em sistemas Linux/Debian.

### 1. Configurando a Blockchain

#### 1.1 Inicializando a rede Blockchain

Uma vez que você tenha configurado a ferramenta Minifabric em seu ambiente, inicialize a rede com o comando abaixo.

```
minifab up -e true -o auditor.contaudit.ppgc.inf.ufrgs.br -c c1
```

#### 1.2. Inicializando a ferramenta Hyperledger Explorer

Uma vez que a rede Minifabric esteja online, pode ser interessante inicializar a ferramenta Hyperledger Explorer para ter uma visibilidade dos componentes da rede, bem como das suas transações.Para isso, execute o comando abaixo.

```
minifab explorerup
```

#### 1.3. Extrair arquivos de configuração da rede Blockchain

Para configurar outros clientes de interação com a blockchain, devemos extrair o arquivo compactado fornecido pelo Minifabric, onde estão os arquivos de configuração e identidades dos membros da rede. Para isso, execute o comando abaixo.

```
tar -xvf ./vars/vscode.tgz --directory ./vars
```

#### 1.4. Preparar contratos inteligentes para instalação na rede Blockchain

Neste momento, precisamos copiar os contratos inteligentes utilizados no experimento para dentro da estrutura do Minifabric, de modo que possamos instalá-los em seguida. Para isso, execute o comando abaixo.

```
for chaincodeFiles in $(ls -d ./contaudit-chaincode/*/ | cut -d "/" -f 3 | awk '{print}'); 
do 
    rsync -aq --delete ./contaudit-chaincode/$chaincodeFiles/ --mkpath ./vars/chaincode/$chaincodeFiles/java --exclude .gradle --exclude .gitignore --exclude .vscode --exclude bin --exclude build; 
done
```

#### 1.5. Instalar contratos inteligentes na rede Blockchain

Finalmente, precisamos instalar os contratos inteligentes na Blockchain. Para isso, execute o comando abaixo.

```
for chaincode in $(ls -d ./contaudit-chaincode/*/ | cut -d "/" -f 3 | awk '{print}'); 
do 
    minifab ccup -n $chaincode -l java -v 1.0; 
done
```

### 2. Usando o Agente da Auditoria (Wrapper)

Para utilizar o Wrapper nas organizações auditadas, execute o comando abaixo alterando os parâmetros entre aspas duplas para o endereço da ferramenta à ser executada e os artefatos utilizados pela mesma, respectivamente.

```
java -jar ./contaudit-wrapper.jar "ExecID" "./contaudit-samples/executor/executor ./contaudit-samples/executor/artifacts/apache.workflow"
```

### 3. Reproduzindo os Experimentos

#### 3.1. Experimentos 1 and 2

Para reproduzir o primeiro e o segundo experimento, execute o oomando abaixo.

```
sh ./experiment_1_and_2.sh "2024-09-29" "execute-multiple-apache2-workflows-with-stats" 100 5 --commit
sh ./experiment_1_and_2.sh "2024-09-29-2" "execute-multiple-user-workflows-with-stats" 100 5 --commit
```

#### 3.2. Experimento 3

Para reproduzir o terceiro experimento, execute o oomando abaixo.

```
sh ./experiment_3.sh "2024-10-05" "execute-multiple-apache2-workflows" 30 --commit
```

#### 3.3. Experimento 4

Para reproduzir o quarto experimento, execute o oomando abaixo.

```
sh ./experiment_4.sh "2024-10-06" "execute-one-workflow-with-stats" "vim.workflow" 30 --commit
```

#### 3.4. Experimento 5

Para reproduzir o quinto experimento, execute o oomando abaixo.

```
sh ./experiment_5.sh "2024-10-12" "execute-one-workflow-with-stats" "digital_ocean_k3s.workflow" --commit
```

## Solução de Problemas

Caso você se depare com erros desconhecidos na interação com o Minifabric, recomendamos uma reconfiguração do ambiente. Abaixo alguns comandos que podem ajudar nesse processo.

### 1. Limpar chaincodes existentes no Minifabric

```
rm -r vars/chaincode/*-chaincode
```

### 2. Limpar Minifabric

```
minifab cleanup
```

### 3. Reiniciar Docker

```
sudo service docker restart
```

### 4. Parar e Limpar Docker

```
docker network prune -f &&
docker container prune -f &&
docker volume prune -f &&
docker image prune -f &&
docker system prune -f &&
docker kill $(docker ps -q)
```

> OBS: Cuidado ao utilizar esse comando, pois todo o seu ambiente Docker será desligado e limpo!

### 5. Matar processos do Docker que estejam escutando portas na rede

```
for pid in $(sudo netstat -tunlp | grep '.*docker' | cut -d "/" -f 1 | awk '{print $7}');
do 
    sudo kill -9 $pid; 
done
```

### 6. Limpar máquina virtual do Docker

```
rm -f ~/.docker/desktop/vms/0/data/Docker.raw
```