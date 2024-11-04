#! /bin/sh
executeWrapper() {
    local thread=$1
    local workflow=$2
    ./contaudit-samples/executor/time.sh "$thread" java -Dlog4j2.simplelogStatusLoggerLevel="OFF" -jar ./contaudit-wrapper.jar "ContAudITWrapper#$thread" "./contaudit-samples/executor/executor $workflow"
}

thread=$1
workflow=$2

echo $(date +"%Y-%m-%d %T,%N") '[execute-one-workflow-with-stats] Starting statistics...'
sudo docker stats -a --format "{{ json . }}" > "experiment_"$thread"_stats.jsonl" &
stats=$!
echo $(date +"%Y-%m-%d %T,%N") '[execute-one-workflow-with-stats] Starting workflow...'

executeWrapper "$thread" "$workflow"

sudo kill -9 $stats
echo $(date +"%Y-%m-%d %T,%N") '[execute-one-workflow-with-stats] Finished...'