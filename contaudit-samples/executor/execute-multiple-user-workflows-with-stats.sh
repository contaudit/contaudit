#! /bin/sh
executeWrapper() {
    local thread=$1
    local workflow=$2
    ./contaudit-samples/executor/time.sh "$thread" java -Dlog4j2.simplelogStatusLoggerLevel="OFF" -jar ./contaudit-wrapper.jar "ContAudITWrapper#$thread" "./contaudit-samples/executor/executor $workflow"  
}

max=$1

echo $(date +"%Y-%m-%d %T,%N") '[execute-multiple-user-workflow-with-stats] Starting statistics...'
sudo docker stats -a --format "{{ json . }}" > "experiment_${max}_stats.jsonl" &
stats=$!

echo $(date +"%Y-%m-%d %T,%N") '[execute-multiple-user-workflow-with-stats] Starting '$max' workflows asynchronously...'
pids=""
for thread in $(seq 1 $max);
do
    executeWrapper "$thread" "./contaudit-samples/executor/artifacts/user.workflow $thread" &
    pid=$!
    pids="$pids $pid"
done
for pid in $pids; do
    wait $pid
done

sudo kill -9 $stats
echo $(date +"%Y-%m-%d %T,%N") '[execute-multiple-user-workflow-with-stats] Finished...'