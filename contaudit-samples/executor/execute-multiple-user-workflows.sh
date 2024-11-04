#! /bin/sh
executeWrapper() {
    local thread=$1
    local workflow=$2
    ./contaudit-samples/executor/time.sh "$thread" java -Dlog4j2.simplelogStatusLoggerLevel="OFF" -jar ./contaudit-wrapper.jar "ContAudITWrapper#$thread" "./contaudit-samples/executor/executor $workflow"
}

max=$1
echo $(date +"%Y-%m-%d %T,%N") '[execute-multiple-user-workflow] Starting ' $max ' workflows asynchronously...'
for thread in $(seq 1 $max);
do
    executeWrapper "$thread" "./contaudit-samples/executor/artifacts/user.workflow $thread" &
done
wait
echo $(date +"%Y-%m-%d %T,%N") '[execute-multiple-user-workflow] Finished...'