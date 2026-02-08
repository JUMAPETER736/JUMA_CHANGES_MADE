#!/bin/bash

# Script to create commits backdated to last Saturday (February 1, 2026)
# with different times throughout the day
# 
# Usage: 
#   ./saturday-commit.sh -m "commit message"    # Make a commit with next available time
#   ./saturday-commit.sh --reset                # Reset time counter to start over

# Base date - last Saturday
BASE_DATE="2026-02-01"

# Array of times throughout the day (you can customize these)
TIMES=(
    "09:15:00"
    "10:30:00"
    "11:45:00"
    "13:20:00"
    "14:35:00"
    "15:50:00"
    "17:10:00"
    "18:25:00"
    "19:40:00"
    "20:55:00"
)

# Counter to track which time to use
TIME_INDEX=0
TIME_INDEX_FILE="/tmp/git_time_index.txt"

# Check if user wants to reset
if [ "$1" == "--reset" ]; then
    rm -f "$TIME_INDEX_FILE"
    echo "✓ Time counter reset. Next commit will use the first time slot."
    exit 0
fi

# Load the current index if it exists
if [ -f "$TIME_INDEX_FILE" ]; then
    TIME_INDEX=$(cat "$TIME_INDEX_FILE")
fi

# Get the number of available times
NUM_TIMES=${#TIMES[@]}

# Get the current time slot (cycle through if we exceed available times)
CURRENT_TIME=${TIMES[$((TIME_INDEX % NUM_TIMES))]}

# Create the full timestamp
COMMIT_TIMESTAMP="$BASE_DATE $CURRENT_TIME"

# Increment and save the index for next time
echo $((TIME_INDEX + 1)) > "$TIME_INDEX_FILE"

# Execute git commit with the backdated timestamp
GIT_AUTHOR_DATE="$COMMIT_TIMESTAMP" GIT_COMMITTER_DATE="$COMMIT_TIMESTAMP" git commit "$@"

# Show what time was used
echo ""
echo "✓ Commit created with timestamp: $COMMIT_TIMESTAMP"
echo "  (Time slot $((TIME_INDEX % NUM_TIMES + 1)) of $NUM_TIMES)"
