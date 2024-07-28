#! /bin/sh
executeWrapper() {
    local thread=$1
    local workflow=$2
    java -Dlog4j2.simplelogStatusLoggerLevel="OFF" -jar ./contaudit-wrapper.jar "ContAudITWrapper#$thread" "./contaudit-samples/executor/executor $workflow"
}

max=$1
echo $(date +"%d-%m-%Y %T.%N %Z") '[execute-multiple-apache2-workflow] Starting ' $max ' workflows asynchronously...'
for thread in $(seq 1 $max);
do
    executeWrapper "$thread" "./contaudit-samples/executor/artifacts/apache2.workflow $thread" &
done
wait
echo $(date +"%d-%m-%Y %T.%N %Z") '[execute-multiple-apache2-workflow] Finished...'