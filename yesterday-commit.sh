#!/bin/bash
# Script to create commits backdated to tomorrow (February 14, 2026)
# with different times throughout the day
# 
# Usage: 
#   ./tomorrow-commit.sh -m "commit message"    # Make a commit with next available time
#   ./tomorrow-commit.sh --reset                # Reset time counter to start over

# Base date - tomorrow
BASE_DATE="2026-02-14"

# Array of 34 random times between 08:30 and 16:40
TIMES=(
    "08:32:00"
    "08:47:00"
    "09:03:00"
    "09:18:00"
    "09:34:00"
    "09:51:00"
    "10:07:00"
    "10:23:00"
    "10:39:00"
    "10:56:00"
    "11:12:00"
    "11:27:00"
    "11:43:00"
    "11:58:00"
    "12:14:00"
    "12:31:00"
    "12:47:00"
    "13:02:00"
    "13:18:00"
    "13:35:00"
    "13:51:00"
    "14:06:00"
    "14:22:00"
    "14:38:00"
    "14:54:00"
    "15:09:00"
    "15:25:00"
    "15:41:00"
    "15:56:00"
    "16:11:00"
    "16:19:00"
    "16:27:00"
    "16:34:00"
    "16:39:00"
)

# Counter to track which time to use
TIME_INDEX=0
TIME_INDEX_FILE="/tmp/git_time_index_tomorrow.txt"

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

# Check if we've used all time slots
if [ $TIME_INDEX -ge $NUM_TIMES ]; then
    echo "⚠ Warning: All $NUM_TIMES time slots have been used!"
    echo "  Use './tomorrow-commit.sh --reset' to start over, or the times will cycle."
fi

# Get the current time slot (cycle through if we exceed available times)
CURRENT_TIME=${TIMES[$((TIME_INDEX % NUM_TIMES))]}

# Create the full timestamp
COMMIT_TIMESTAMP="$BASE_DATE $CURRENT_TIME"

# Increment and save the index for next time
echo $((TIME_INDEX + 1)) > "$TIME_INDEX_FILE"

# Stage all changes first
git add -A

# Execute git commit with the backdated timestamp
GIT_AUTHOR_DATE="$COMMIT_TIMESTAMP" GIT_COMMITTER_DATE="$COMMIT_TIMESTAMP" git commit "$@"

# Show what time was used
echo ""
echo "✓ Commit created with timestamp: $COMMIT_TIMESTAMP"
echo "  (Time slot $((TIME_INDEX % NUM_TIMES + 1)) of $NUM_TIMES)"