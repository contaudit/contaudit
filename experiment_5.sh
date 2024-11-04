#!/bin/bash

# Collect parameters
experiment=$1
script=$2
workflow=$3
commit_flag=$4

# Run the experiment
echo "$(date +"%Y-%m-%d %T,%N") [experiment] Running experiment "$experiment"..."
{ time sh ./contaudit-samples/executor/"$script".sh 1 ./contaudit-samples/executor/artifacts/"$workflow" > /dev/null 2>&1; }

# Clean special characters from experiment result file
echo "$(date +"%Y-%m-%d %T,%N") [experiment] Cleaning experiment result file..."
sed -i 's/\[2J\[H//g' experiment_1_stats.jsonl

# Create the experiment file folder
echo "$(date +"%Y-%m-%d %T,%N") [experiment] Creating folder of experiment result files..."
mkdir -p ./contaudit-wrapper/experiments/"$experiment"/1

# Move thread output to experiment files folder
echo "$(date +"%Y-%m-%d %T,%N") [experiment] Moving experiment result files..."
mv contaudit-wrapper-ContAudITWrapper#* ./contaudit-wrapper/experiments/"$experiment"/1

# Move the thread timing results to the experiment files folder
mv time_result_thread_* ./contaudit-wrapper/experiments/"$experiment"/1

# Move the experiment result file to the experiment files folder
mv experiment_1_stats.jsonl ./contaudit-wrapper/experiments/"$experiment"/1

# Calculates metrics
echo "$(date +"%Y-%m-%d %T,%N") [experiment] Calculating and exporting experiment metrics..."
Rscript ./contaudit-samples/metrics.r "$experiment" 1 > /dev/null 2>&1

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