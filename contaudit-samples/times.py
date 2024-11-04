import re
import json
import os

# Function to convert time in "{MM}m{SS.sss}s" format to "SS.sss"
def convert_time_to_seconds(time_str):
    match = re.match(r'(\d+)m([\d.]+)s', time_str)
    if match:
        minutes = int(match.group(1))
        seconds = float(match.group(2))
        return minutes * 60 + seconds
    return float(time_str)

# Function to process a single file and extract the times as a dictionary
def process_file(file_path):
    time_data = {}
    real_time = None
    user_time = None
    sys_time = None
    thread = None

    with open(file_path, 'r') as file:
        lines = file.readlines()

    # Process each line to find the times
    for line in lines:
        if line.startswith("real"):
            real_time = convert_time_to_seconds(line.split()[1])
        elif line.startswith("user"):
            user_time = convert_time_to_seconds(line.split()[1])
        elif line.startswith("sys"):
            sys_time = convert_time_to_seconds(line.split()[1])
        elif line.startswith("Thread"):
            thread = int(line.split(":")[1].strip())

    # Create the dictionary with the information
    time_data = {
        "thread": thread,
        "real": real_time,
        "user": user_time,
        "sys": sys_time,
        "file": file_path
    }

    return time_data

# Function to scan the folder and its subfolders, processing the corresponding files
def process_directory_recursive(directory):
    results = []

    # Walk the directory tree recursively
    for root, dirs, files in os.walk(directory):
        # For each file within this subfolder, check if it matches the pattern 'time_result_thread_*.txt'
        for file in files:
            if file.startswith('time_result_thread_') and file.endswith('.txt'):
                file_path = os.path.join(root, file)
                time_data = process_file(file_path)
                results.append(time_data)

    return results

# Main function to accept folder as parameter and save single JSON file
if __name__ == "__main__":
    import sys
    if len(sys.argv) != 2:
        print("Use: python script.py <folder>")
        sys.exit(1)

    # Get the argument folder
    directory = sys.argv[1]

    # Check if the folder exists
    if not os.path.isdir(directory):
        print(f"Error: The folder {directory} doesn't exist.")
        sys.exit(1)

    # Process files in folder and subfolders
    results = process_directory_recursive(directory)

    # Save all results to a single JSON file
    output_file = os.path.join(directory, 'times.json')
    with open(output_file, 'w') as json_file:
        json.dump(results, json_file, indent=4)

    print(f"Single JSON file saved as {output_file}.")
