#!/bin/bash
# Script to create commits backdated to tomorrow (February 15, 2026)
# with different times throughout the day
# 
# Usage: 
#   ./tomorrow-commit.sh -m "commit message"    # Make a commit with next available time
#   ./tomorrow-commit.sh --reset                # Reset time counter to start over

# Base date - tomorrow
BASE_DATE="2026-02-15"

# Array of 34 random times between 08:30 and 16:40
TIMES=(
    "13:34:00"
    "13:49:00"
    "14:03:00"
    "14:18:00"
    "14:32:00"
    "14:47:00"
    "15:02:00"
    "15:16:00"
    "15:31:00"
    "15:46:00"
    "16:01:00"
    "16:15:00"
    "16:30:00"
    "16:44:00"
    "16:59:00"
    "17:13:00"
    "17:28:00"
    "17:42:00"
    "17:57:00"
    "18:11:00"
    "18:26:00"
    "18:40:00"
    "18:55:00"
    "19:09:00"
    "19:23:00"
    "19:34:00"
    "19:42:00"
    "19:48:00"
    "19:52:00"
    "19:55:00"
    "19:57:00"
    "19:58:00"
    "19:59:00"
    "20:00:00"
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