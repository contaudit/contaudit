#! /bin/sh
echo '[digital_ocean_k3s_workflow] Checking if package kubectl exists...'
if [ $(snap list | grep -c "kubectl") -eq 0 ]
then
    echo '[digital_ocean_k3s_workflow] Package not exists! Installing...'
    sudo snap install kubectl
fi

if [ $(kubectl get nodes | grep -c 'k3s-server') -eq 0 ]
then
    echo '[digital_ocean_k3s_workflow] Downloading DO + K3S Terraform recipe...'
    git clone -b terraform-digitalocean-ha-k3s https://github.com/colinwilson/example-terraform-modules

    echo '[digital_ocean_k3s_workflow] Defining Terraform recipe variables file...'
    cd example-terraform-modules
    rm -f terraform.tfvars
    do_token='YOUR_TOKEN'
    ssh_key_fingerprints='YOUR_FINGERPRINT'
    echo "# terraform.tfvars (example)\n\n# Your DigitalOcean Personal Access Token (Read & Write)\ndo_token = \"$do_token\"\n\n# Your SSH Public Key Fingerprint\nssh_key_fingerprints = [\"$ssh_key_fingerprints\"]" > terraform.tfvars

    echo '[digital_ocean_k3s_workflow] Checking if package terraform exists...'
    if [ $(snap list | grep -c "terraform") -eq 0 ]
    then
        echo '[digital_ocean_k3s_workflow] Package not exists! Installing...'
        sudo snap install terraform
        export PATH=$PATH:/snap/bin
    fi

    echo '[digital_ocean_k3s_workflow] Initialize Terraform configuration...'
    echo $PWD
    terraform init

    echo '[digital_ocean_k3s_workflow] Executing terraform recipe...'
    terraform apply -auto-approve | tee cluster_summary.txt
    sleep 120

    echo '[digital_ocean_k3s_workflow] Adding cluster to Kubectl Config file ...'
    mkdir -p $HOME/.kube
    server_ip=$(grep -A 4 servers cluster_summary.txt | grep ip_public | grep -o -E '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
    ssh_key=~/.ssh/id_rsa
    kubectl_config=~/.kube/config
    scp -P 22 -oStrictHostKeyChecking=no -i $ssh_key root@$server_ip:/etc/rancher/k3s/k3s.yaml $kubectl_config
    api_server_ip=$(grep api_server_ip cluster_summary.txt | grep -o -E '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
    sed -i -e "s/127.0.0.1/$api_server_ip/g" ~/.kube/config

    echo '[digital_ocean_k3s_workflow] Checking K3S Cluster nodes...'
    kubectl get nodes
else
    echo '[digital_ocean_k3s_workflow] Destroying K3S resources...'
    cd example-terraform-modules
    terraform destroy -auto-approve
fi