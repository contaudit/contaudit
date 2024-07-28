#! /bin/sh
executeWrapper() {
    local thread=$1
    local workflow=$2
    java -Dorg.hyperledger.fabric.sdk.configuration="./contaudit-wrapper/app/config.properties" -Dlog4j2.simplelogStatusLoggerLevel="OFF" -jar ./contaudit-wrapper.jar "ContAudITWrapper#"$thread " ./contaudit-samples/executor/executor "$workflow
}

thread=0
max=25
for workflow in $(ls -d ./contaudit-samples/executor/artifacts/* | awk '{print}'); 
do
    if [ $thread -lt $max ]
    then 
        thread=$(expr $thread + 1)
        executeWrapper "$thread" "$workflow" &
    else
        echo 'This workflow ('$workflow') will not execute because the threads max number was achived...'
    fi
done
wait