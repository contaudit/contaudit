#!/bin/bash
thread="$1"
shift

output_file="time_result_thread_${thread}.txt"
{ time "$@" ; } 2>&1 | tee "$output_file"
echo "Thread: $thread" >> "$output_file"