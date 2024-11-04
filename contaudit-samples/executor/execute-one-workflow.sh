#! /bin/sh
executeWrapper() {
    local thread=$1
    local workflow=$2
    java -Dlog4j2.simplelogStatusLoggerLevel="OFF" -jar ./contaudit-wrapper.jar "ContAudITWrapper#$thread" "./contaudit-samples/executor/executor $workflow"
}

echo $(date +"%Y-%m-%d %T,%N") '[execute-one-workflow] Starting workflow...'

executeWrapper "1" "$1"

echo $(date +"%Y-%m-%d %T,%N") '[execute-one-workflow] Finished...'