# ContAudIT - Uma Abordagem de Auditoria Contínua com Blockchain para Gerenciamento de Mudanças em TI

Esse projeto é organizado em cinco pastas.

- [**contaudit-chaincode**](contaudit-chaincode/) - Projeto múltiplo de chaincodes utilizados na blockchain do experimento. 

- [**contaudit-admin**](contaudit-admin/) - Projeto de aplicativo cliente utilzado pelas auditorias do experimento para configuração da blockchain. 

- [**contaudit-wrapper**](contaudit-wrapper/) - Projeto de aplicativo cliente utilizado pelas organizações auditadas do experimento.

- [**contaudit-samples**](contaudit-samples/) - Repositório de ferramentas e artefatos de exemplo usados pelas organizações auditadas do experimento.

- [**contaudit-checker**](contaudit-checker/) - Projeto de aplicativo cliente utilizado pelas auditorias do experimento para análises em auditorias em curso.

## Pré Requisitos

[Minifabric 2.3+](https://github.com/hyperledger-labs/minifabric)

## Começando

> **Importante**: Rode todos os comandos abaixo no diretório de trabalho local desse repositório.

> **Importante**: Todos os comandos abaixo foram testados apenas em sistema Linux/Debian.

### 1. Inicializando a rede Blockchain

Uma vez que você tenha configurado a ferramenta Minifabric em seu ambiente, vamos inicializar a rede com o comando abaixo.

```
minifab up -e true
```

### 2. Inicializando a ferramenta Hyperledger Explorer

Uma vez que a rede Minifabric esteja online, pode ser interessante inicializar a ferramenta Hyperledger Explorer para que se tenha uma visibilidade dos componentes da rede bem como das suas transações.

Para isso, execute o comando abaixo.

```
minifab explorerup
```

### 3. Extrair arquivos de configuração da rede Blockchain

Em seguida, para que possamos configurar outros clientes de interação com a blockchain, devemos extrair o arquivo compactado provido pelo Minifabric onde estão os arquivos de configuração e identidades dos membros da rede. Faça isso executando o comando abaixo.

```
tar -xvf ./vars/vscode.tgz --directory ./vars
```

### 4. Preparar chaincodes para instalação na rede Blockchain

Nesse momento precisamos copiar os chaincodes utilizados no experimento para dentro da estrutura do Minifabric de modo que possamos instalá-los em seguida. Faça isso executando o comando abaixo.

```
for chaincodeFiles in $(ls -d ./contaudit-chaincode/*/ | cut -d "/" -f 3 | awk '{print}'); 
do 
    rsync -aq --delete ./contaudit-chaincode/$chaincodeFiles/ --mkpath ./vars/chaincode/$chaincodeFiles/java --exclude .gradle --exclude .gitignore --exclude .vscode --exclude bin; 
done
```

### 5. Instalar chaincodes na rede Blockchain

Finalmente, precisamos instalar os chaincodes na Blockchain. Faça isso executando o comando abaixo.

```
for chaincode in $(ls -d ./contaudit-chaincode/*/ | cut -d "/" -f 3 | awk '{print}'); 
do 
    minifab ccup -n $chaincode -l java -v 1.0; 
done
```

### 6. Usando o Wrapper

Para utilizar o Wrapper nas organizações auditadas, execute o comando abaixo alterando os parâmetros entre aspas duplas para o endereço da ferramenta à ser executada e os artefatos utilizados pela mesma, respectivamente.

```
java -jar ./contaudit-wrapper.jar "ExecID" "./contaudit-samples/executor/executor ./contaudit-samples/executor/artifacts/apache.workflow"
```

## Troubleshooting

Caso você se depare com erros não conhecidos na interação com o Minifabric, recomendamos uma reconfiguração do ambiente. Abaixo alguns comandos que podem ajudar nesse processo.

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

> OBS: Cuidado ao utilizar esse comando pois todo o seu ambiente Docker será desligado e limpo!

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