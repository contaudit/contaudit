#!/bin/sh

check_package_installed() {
    local package_name=$1
    if command -v apt >/dev/null 2>&1; then
        # APT
        # Kubernetes config
        echo "[digital_ocean_k3s_workflow] Updating repositories and installing required packages for Kubernetes..."
        sudo apt-get update
        sudo apt-get install -y apt-transport-https ca-certificates curl gnupg

        echo "[digital_ocean_k3s_workflow] Setting the Kubernetes repository key..."
        sudo mkdir -p -m 755 /etc/apt/keyrings
        curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.31/deb/Release.key | sudo tee /etc/apt/keyrings/kubernetes-apt-keyring.gpg
        sudo chmod 644 /etc/apt/keyrings/kubernetes-apt-keyring.gpg

        echo "[digital_ocean_k3s_workflow] Adding Kubernetes repository..."
        echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.31/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list
        sudo chmod 644 /etc/apt/sources.list.d/kubernetes.list

        # Terraform config
        echo "[digital_ocean_k3s_workflow] Updating packages and configuring Terraform key..."
        sudo apt-get update
        sudo apt-get install -y gnupg software-properties-common
        wget -O- https://apt.releases.hashicorp.com/gpg | gpg --dearmor | sudo tee /usr/share/keyrings/hashicorp-archive-keyring.gpg
        gpg --no-default-keyring --keyring /usr/share/keyrings/hashicorp-archive-keyring.gpg --fingerprint

        echo "[digital_ocean_k3s_workflow] Adding Terraform repository..."
        echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list

        echo "[digital_ocean_k3s_workflow] Checking if package $package_name is installed..."
        if ! which "$package_name"; then
            echo "[digital_ocean_k3s_workflow] $package_name not found! Installing..."
            sudo apt update && sudo apt install -y "$package_name"
        else
            echo "[digital_ocean_k3s_workflow] $package_name is already installed."
        fi
    elif command -v yum >/dev/null 2>&1; then
        # YUM
        echo "[digital_ocean_k3s_workflow] Setting up Kubernetes repository in YUM..."
        cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://pkgs.k8s.io/core:/stable:/v1.31/rpm/
enabled=1
gpgcheck=1
gpgkey=https://pkgs.k8s.io/core:/stable:/v1.31/rpm/repodata/repomd.xml.key
EOF
        
        # Terraform config
        echo "[digital_ocean_k3s_workflow] Installing YUM utility packages and configuring Terraform repository..."
        sudo yum install -y yum-utils
        sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo

        echo "[digital_ocean_k3s_workflow] Checking if package $package_name is installed..."
        if ! yum list installed | grep -q "$package_name"; then
            echo "[digital_ocean_k3s_workflow] $package_name not found! Installing..."
            sudo yum install -y "$package_name"
        else
            echo "[digital_ocean_k3s_workflow] $package_name is already installed."
        fi
    elif command -v dnf >/dev/null 2>&1; then
        # DNF
        echo "[digital_ocean_k3s_workflow] Configuring Kubernetes repository in DNF..."
        cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-\$basearch
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOF
        sudo setenforce 0
        sudo dnf upgrade -y

        # Terraform config
        echo "[digital_ocean_k3s_workflow] Installing plugins and configuring Terraform repository..."
        sudo dnf install -y dnf-plugins-core
        sudo dnf config-manager --add-repo https://rpm.releases.hashicorp.com/fedora/hashicorp.repo

        echo "[digital_ocean_k3s_workflow] Checking if package $package_name is installed..."
        if ! dnf list installed | grep -q "$package_name"; then
            echo "[digital_ocean_k3s_workflow] $package_name not found! Installing..."
            sudo dnf install -y "$package_name"
        else
            echo "[digital_ocean_k3s_workflow] $package_name is already installed."
        fi
    else
        echo "[digital_ocean_k3s_workflow] Package manager not supported!"
        exit 1
    fi
}

setup_terraform() {
    echo '[digital_ocean_k3s_workflow] Cloning DigitalOcean Terraform repository with K3S...'
    git clone -b terraform-digitalocean-ha-k3s https://github.com/colinwilson/example-terraform-modules
    cd example-terraform-modules

    echo '[digital_ocean_k3s_workflow] Defining Terraform variables file...'
    rm -f terraform.tfvars
    local do_token='your_pat'
    local ssh_key_fingerprints='e2:99:86:4b:69:fd:85:89:a4:0e:57:05:88:bf:9d:dd'

    cat <<EOF > terraform.tfvars
do_token = "$do_token"
ssh_key_fingerprints = ["$ssh_key_fingerprints"]
EOF

    check_package_installed "terraform"

    echo '[digital_ocean_k3s_workflow] Initializing Terraform configuration...'
    terraform init

    echo '[digital_ocean_k3s_workflow] Applying Terraform recipe...'
    terraform apply -auto-approve | tee cluster_summary.txt
}

configure_kubectl() {
    echo '[digital_ocean_k3s_workflow] Adding cluster to Kubectl configuration...'
    mkdir -p $HOME/.kube

    local server_ip=$(grep -A 4 servers cluster_summary.txt | grep ip_public | grep -o -E '[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+')
    local ssh_key="$HOME/.ssh/id_rsa"
    local kubectl_config="$HOME/.kube/config"

    echo "[digital_ocean_k3s_workflow] Copying K3S config file from server to local kubectl..."
    scp -P 22 -o StrictHostKeyChecking=no -i "$ssh_key" root@"$server_ip":/etc/rancher/k3s/k3s.yaml "$kubectl_config"

    local api_server_ip=$(grep api_server_ip cluster_summary.txt | grep -o -E '[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+')
    sed -i -e "s|127.0.0.1|$api_server_ip|g" "$kubectl_config"

    echo '[digital_ocean_k3s_workflow] Checking K3S cluster nodes...'
    kubectl get nodes
}

echo '[digital_ocean_k3s_workflow] Checking if kubectl is installed...'
check_package_installed "kubectl"

if [ $(kubectl get nodes | grep -c 'k3s-server') -eq 0 ]; then
    cd "$HOME/Code"
    
    setup_terraform
    sleep 120
    
    configure_kubectl
else
    echo '[digital_ocean_k3s_workflow] Destroying K3S resources...'
    cd "$HOME/Code/example-terraform-modules"
    terraform destroy -auto-approve
fi
