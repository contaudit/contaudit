# Load required packages
if (!require("jsonlite")) install.packages("jsonlite", dependencies=TRUE)
if (!require("dplyr")) install.packages("dplyr", dependencies=TRUE)

library(jsonlite)
library(dplyr)

# Capture the arguments passed to the script
args <- commandArgs(trailingOnly = TRUE)

# Check if arguments were provided correctly
if (length(args) < 2) {
  stop("Please provide arguments: <experiment_name> <experiment_threads>")
}

# Define variables based on arguments
experiment_name <- args[1] 
experiment_threads <- args[2]
base_path <- file.path(path.expand("."), "contaudit-wrapper", "experiments", experiment_name, experiment_threads)
input_path <- file.path(base_path, paste0("experiment_", experiment_threads, "_stats.jsonl"))
output_path <- file.path(base_path, paste0("experiment_", experiment_threads, "_stats.csv"))

# Read JSON file line by line
data <- stream_in(file(input_path))

# Function to calculate the mode
calc_mode <- function(x) {
  ux <- unique(x)
  ux[which.max(tabulate(match(x, ux)))]
}

# Clean up the CPUPerc and MemPerc columns to remove the '%' and convert to numeric
data$CPUPerc <- as.numeric(gsub("%", "", data$CPUPerc))
data$MemPerc <- as.numeric(gsub("%", "", data$MemPerc))

# Group data by 'Name' and calculate metrics for 'MemPerc' and 'CPUPerc'
summary_stats <- data %>%
  group_by(Name) %>%
  summarise(
    mean_mem = mean(MemPerc, na.rm = TRUE),
    median_mem = median(MemPerc, na.rm = TRUE),
    mode_mem = calc_mode(MemPerc),
    min_mem = min(MemPerc, na.rm = TRUE),
    max_mem = max(MemPerc, na.rm = TRUE),
    percentile_0_mem = quantile(MemPerc, probs = 0, na.rm = TRUE),
    percentile_25_mem = quantile(MemPerc, probs = 0.25, na.rm = TRUE),
    percentile_50_mem = quantile(MemPerc, probs = 0.50, na.rm = TRUE),
    percentile_75_mem = quantile(MemPerc, probs = 0.75, na.rm = TRUE),
    percentile_100_mem = quantile(MemPerc, probs = 1, na.rm = TRUE),
    
    mean_cpu = mean(CPUPerc, na.rm = TRUE),
    median_cpu = median(CPUPerc, na.rm = TRUE),
    mode_cpu = calc_mode(CPUPerc),
    min_cpu = min(CPUPerc, na.rm = TRUE),
    max_cpu = max(CPUPerc, na.rm = TRUE),
    percentile_0_cpu = quantile(CPUPerc, probs = 0, na.rm = TRUE),
    percentile_25_cpu = quantile(CPUPerc, probs = 0.25, na.rm = TRUE),
    percentile_50_cpu = quantile(CPUPerc, probs = 0.50, na.rm = TRUE),
    percentile_75_cpu = quantile(CPUPerc, probs = 0.75, na.rm = TRUE),
    percentile_100_cpu = quantile(CPUPerc, probs = 1, na.rm = TRUE)
  )

# Show results
print(summary_stats)

# Save results to CSV
write.csv(summary_stats, output_path, row.names = FALSE)