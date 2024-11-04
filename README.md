# ContAudIT: Continuous Auditing in IT Change Management Using Blockchain

[Clique aqui para acessar a versão em Português do Brasil.](README_PT_BR.md)

This project is organized into five folders:

- [**contaudit-chaincode**](contaudit-chaincode/) - Multiple project of smart contracts used in the experiment blockchain.

- [**contaudit-admin**](contaudit-admin/) - Client application project used by the audit to configure the blockchain in the experiment.

- [**contaudit-wrapper**](contaudit-wrapper/) - Client application project used by the audited organizations in the experiment.

- [**contaudit-samples**](contaudit-samples/) - Repository of example tools and artifacts used by audited organizations in the the experiment.

- [**contaudit-checker**](contaudit-checker/) - Client application project used by the audit for analysis in ongoing audits in the experiment.

## Prerequisites

[Git](https://git-scm.com)

[Docker](https://www.docker.com)

[Minifabric 2.3](https://github.com/hyperledger-labs/minifabric)

[Java 17](https://www.oracle.com/java/technologies/downloads/#java17)

[Gradle 7.0](https://gradle.org/next-steps/?version=7.0&format=bin)

[Python](https://www.python.org)

[R](https://www.r-project.org)

## Getting Started

> **Important**: Run all the commands below in the local working directory of this repository.

> **Important**: All commands below have been tested on Linux/Debian systems only.

### 1. Setting up the Blockchain

#### 1.1 Initializing the Blockchain Network

Once you have configured the Minifabric tool in your environment, initialize the network with the command below.

```
minifab up -e true -o auditor.contaudit.ppgc.inf.ufrgs.br -c c1
```

#### 1.2. Launching the Hyperledger Explorer tool

Once the Minifabric network is online, it may be interesting to launch the Hyperledger Explorer tool to gain visibility into the network components as well as their transactions. To do this, run the command below.

```
minifab explorerup
```

#### 1.3. Extract Blockchain Network Configuration Files

To configure other clients to interact with the blockchain, we must extract the compressed file provided by Minifabric, which contains the configuration files and identities of the network members. To do this, run the command below.

```
tar -xvf ./vars/vscode.tgz --directory ./vars
```

#### 1.4. Preparing smart contracts for installation on the Blockchain network

At this point, we need to copy the smart contracts used in the experiment into the Minifabric framework so that we can install them later. To do this, run the command below.

```
for chaincodeFiles in $(ls -d ./contaudit-chaincode/*/ | cut -d "/" -f 3 | awk '{print}'); 
do 
    rsync -aq --delete ./contaudit-chaincode/$chaincodeFiles/ --mkpath ./vars/chaincode/$chaincodeFiles/java --exclude .gradle --exclude .gitignore --exclude .vscode --exclude bin --exclude build; 
done
```

#### 1.5. Install Smart Contracts on the Blockchain Network

Finally, we need to install the smart contracts on the Blockchain. To do this, run the command below.

```
for chaincode in $(ls -d ./contaudit-chaincode/*/ | cut -d "/" -f 3 | awk '{print}'); 
do 
    minifab ccup -n $chaincode -l java -v 1.0; 
done
```

### 2. Using the Audit Agent (Wrapper)

To use the Wrapper in audited organizations, run the command below, changing the parameters in double quotes to the address of the tool to be executed and the artifacts used by it, respectively.

```
java -jar ./contaudit-wrapper.jar "ExecID" "./contaudit-samples/executor/executor ./contaudit-samples/executor/artifacts/apache.workflow"
```

### 3. Reproducing the Experiments

#### 3.1. Experiment 1 and 2

To reproduce the first and second experiment, run the command below.

```
sh ./experiment_1_and_2.sh "2024-09-29" "execute-multiple-apache2-workflows-with-stats" 100 5 --commit
sh ./experiment_1_and_2.sh "2024-09-29-2" "execute-multiple-user-workflows-with-stats" 100 5 --commit
```

#### 3.2. Experiment 3

To reproduce the third experiment, run the command below.

```
sh ./experiment_3.sh "2024-10-05" "execute-multiple-apache2-workflows" 30 --commit
```

#### 3.3. Experiment 4

To reproduce the fourth experiment, run the command below.

```
sh ./experiment_4.sh "2024-10-06" "execute-one-workflow-with-stats" "vim.workflow" 30 --commit
```

#### 3.4. Experiment 5

To reproduce the fifth experiment, run the command below.

```
sh ./experiment_5.sh "2024-10-12" "execute-one-workflow-with-stats" "digital_ocean_k3s.workflow" --commit
```

## Troubleshooting

If you encounter unknown errors when interacting with Minifabric, we recommend reconfiguring the environment. Below are some commands that can help with this process.

### 1. Clear existing chaincodes on Minifabric

```
rm -r vars/chaincode/*-chaincode
```

### 2. Clean Minifabric

```
minifab cleanup
```

### 3. Restart Docker

```
sudo service docker restart
```

### 4. Stop and Clean Docker

```
docker network prune -f &&
docker container prune -f &&
docker volume prune -f &&
docker image prune -f &&
docker system prune -f &&
docker kill $(docker ps -q)
```

> NOTE: Be careful when using this command, as your entire Docker environment will be shut down and cleaned!

### 5. Kill Docker processes listening on network ports

```
for pid in $(sudo netstat -tunlp | grep '.*docker' | cut -d "/" -f 1 | awk '{print $7}');
do 
    sudo kill -9 $pid; 
done
```

### 6. Clean up Docker virtual machine

```
rm -f ~/.docker/desktop/vms/0/data/Docker.raw
```