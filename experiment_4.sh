#!/bin/bash

# Collect parameters
experiment=$1
script=$2
workflow=$3
max_executions=$4
commit_flag=$5

for (( current_iteration=1; current_iteration<=$max_executions; current_iteration++ ))
do
    # Run the current iteration of experiment with the pre-defined number of threads
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Running "$current_iteration"' iteration of experiment "$experiment"..."
    { time sh ./contaudit-samples/executor/"$script".sh "$current_iteration" ./contaudit-samples/executor/artifacts/"$workflow" > /dev/null 2>&1; }

    # Clean special characters from experiment result file
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Cleaning experiment result file..."
    sed -i 's/\[2J\[H//g' experiment_"$current_iteration"_stats.jsonl

    # Create the experiment file folder
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Creating folder of experiment result files..."
    mkdir -p ./contaudit-wrapper/experiments/"$experiment"/"$current_iteration"

    # Move thread output to experiment files folder
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Moving experiment result files..."
    mv contaudit-wrapper-ContAudITWrapper#* ./contaudit-wrapper/experiments/"$experiment"/"$current_iteration"

    # Move the thread timing results to the experiment files folder
    mv time_result_thread_* ./contaudit-wrapper/experiments/"$experiment"/"$current_iteration"

    # Move the experiment result file to the experiment files folder
    mv experiment_"$current_iteration"_stats.jsonl ./contaudit-wrapper/experiments/"$experiment"/"$current_iteration"

    # Calculates metrics
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Calculating and exporting experiment metrics..."
    Rscript ./contaudit-samples/metrics.r "$experiment" "$current_iteration" > /dev/null 2>&1
done

# Aggregate the result times to JSON file
python ./contaudit-samples/times.py ./contaudit-wrapper/experiments/"$experiment"

# Calculates final metrics
echo "$(date +"%Y-%m-%d %T,%N") [experiment] Calculating and exporting final experiment metrics..."
Rscript ./contaudit-samples/metrics2.r "$experiment" > /dev/null 2>&1

# Check if the --commit parameter was passed
if [ "$commit_flag" == "--commit" ]; then
    # Prepare new files to commit to the repository
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Preparing files to commit..."
    git add --all > /dev/null 2>&1

    # Commit experiment files to the repository
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Committing files..."
    git commit -am "* Adding new experiments files." > /dev/null 2>&1

    # Send commit to origin
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Synchronizing commit..."
    git push > /dev/null 2>&1
else
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Parameter --commit not provided, skipping commit step..."
fi

echo "$(date +"%Y-%m-%d %T,%N") [experiment] Experiment finished."