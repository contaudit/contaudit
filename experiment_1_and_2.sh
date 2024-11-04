#!/bin/bash

# Collect parameters
experiment=$1
script=$2
max_threads=$3
increment=$4
commit_flag=$5

# Loop for executing rounds with different numbers of threads
# First run with 1 thread
current_threads=1

echo "$(date +"%Y-%m-%d %T,%N") [experiment] Running experiment "$experiment" with "$current_threads" threads..."
{ time sh ./contaudit-samples/executor/"$script".sh "$current_threads" > /dev/null 2>&1; }

echo "$(date +"%Y-%m-%d %T,%N") [experiment] Cleaning experiment result file..."
sed -i 's/\[2J\[H//g' experiment_"$current_threads"_stats.jsonl

echo "$(date +"%Y-%m-%d %T,%N") [experiment] Creating folder of experiment result files..."
mkdir -p ./contaudit-wrapper/experiments/"$experiment"/"$current_threads"

echo "$(date +"%Y-%m-%d %T,%N") [experiment] Moving experiment result files..."
mv contaudit-wrapper-ContAudITWrapper#* ./contaudit-wrapper/experiments/"$experiment"/"$current_threads"
mv time_result_thread_* ./contaudit-wrapper/experiments/"$experiment"/"$current_threads"
mv experiment_"$current_threads"_stats.jsonl ./contaudit-wrapper/experiments/"$experiment"/"$current_threads"

echo "$(date +"%Y-%m-%d %T,%N") [experiment] Calculating and exporting experiment metrics..."
Rscript ./contaudit-samples/metrics.r "$experiment" "$current_threads" > /dev/null 2>&1

# Perform the remaining iterations with the increment starting from 0
for (( current_threads=$increment; current_threads<=$max_threads; current_threads+=$increment ))
do
    # Checks if we didn't exceed the thread limit in the last run
    if [ $current_threads -gt $max_threads ]; then
        current_threads=$max_threads
    fi

    # Run the experiment with the current number of threads
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Running experiment "$experiment" with "$current_threads" threads..."
    { time sh ./contaudit-samples/executor/"$script".sh "$current_threads" > /dev/null 2>&1; }

    # Clean special characters from experiment result file
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Cleaning experiment result file..."
    sed -i 's/\[2J\[H//g' experiment_"$current_threads"_stats.jsonl

    # Create the experiment file folder
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Creating folder of experiment result files..."
    mkdir -p ./contaudit-wrapper/experiments/"$experiment"/"$current_threads"

    # Move thread output to experiment files folder
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Moving experiment result files..."
    mv contaudit-wrapper-ContAudITWrapper#* ./contaudit-wrapper/experiments/"$experiment"/"$current_threads"

    # Move the thread timing results to the experiment files folder
    mv time_result_thread_* ./contaudit-wrapper/experiments/"$experiment"/"$current_threads"

    # Move the experiment result file to the experiment files folder
    mv experiment_"$current_threads"_stats.jsonl ./contaudit-wrapper/experiments/"$experiment"/"$current_threads"

    # Calculates metrics
    echo "$(date +"%Y-%m-%d %T,%N") [experiment] Calculating and exporting experiment metrics..."
    Rscript ./contaudit-samples/metrics.r "$experiment" "$current_threads" > /dev/null 2>&1
done

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