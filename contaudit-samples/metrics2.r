# Install the required libraries if they are not already installed.
if (!require("jsonlite")) install.packages("jsonlite")
if (!require("dplyr")) install.packages("dplyr")
if (!require("stringr")) install.packages("stringr")

# Load the libraries
library(jsonlite)
library(dplyr)
library(stringr)

# Capture the arguments passed to the script
args <- commandArgs(trailingOnly = TRUE)

# Check if arguments were provided correctly
if (length(args) < 1) {
  stop("Please provide arguments: <experiment_name>")
}

# Define variables based on arguments
experiment_name <- args[1]

# Construct the file path
base_path <- file.path(path.expand("."), "contaudit-wrapper", "experiments", experiment_name)
input_path <- file.path(base_path, paste0("times.json"))
output_path <- file.path(base_path, paste0("times_final_metrics.csv"))

# Check if the file exists
if (!file.exists(input_path)) {
  stop(paste("File not found:", input_path))
}

# Read the JSON file
data <- tryCatch({
  fromJSON(input_path)
}, error = function(e) {
  stop(paste("Error reading JSON file:", e$message))
})

# Convert to dataframe
df <- as.data.frame(data)

# Extract the last directory from the 'file' column
df <- df %>%
  mutate(folder = str_extract(file, "(?<=/)[^/]+(?=/[^/]+$)"))

# Calculate mean and standard deviation of 'real' metric by the extracted folder
results <- df %>%
  group_by(folder) %>%
  summarise(
    mean_real_time = mean(real, na.rm = TRUE),
    standard_deviation_real_time = sd(real, na.rm = TRUE)
  )

# Display the result
print(results)

# Save results to CSV
write.csv(results, output_path, row.names = FALSE)