#!/bin/bash
# Git Commit Date Redistributor
# Redistributes commits from 17-01-2026 to specified January dates
# TODAY IS: 2026-01-17

echo "========================================"
echo "Git Commit Date Redistributor"
echo "TODAY: 2026-01-17"
echo "========================================"
echo ""

# Get today's commits
echo "Fetching commits from today (2026-01-17)..."
mapfile -t COMMITS < <(git log --since="2026-01-17 00:00" --until="2026-01-17 23:59" --format="%H" --reverse)
COMMIT_COUNT=${#COMMITS[@]}

echo "Found $COMMIT_COUNT commits from 2026-01-17"
echo ""

if [ $COMMIT_COUNT -eq 0 ]; then
    echo "No commits found for 2026-01-17. Exiting."
    exit 1
fi

# Distribution plan
echo "Distribution Plan:"
echo "  2026-01-18 (Tomorrow): 25 commits"
echo "  2026-01-01 (Jan 1): 5 commits"
echo "  2026-01-02 (Jan 2): 5 commits"
echo "  2026-01-03 (Jan 3): 5 commits"
echo "  2026-01-04 (Jan 4): 5 commits"
echo "  2026-01-10 (Jan 10): 5 commits"
echo "  2026-01-11 (Jan 11): 5 commits"
echo "  Total needed: 55 commits"
echo ""

# Create backup
CURRENT_BRANCH=$(git branch --show-current)
BACKUP_BRANCH="backup-$(date +%Y%m%d-%H%M%S)"

echo "Current branch: $CURRENT_BRANCH"
echo "Creating backup branch: $BACKUP_BRANCH"
git branch $BACKUP_BRANCH
echo ""

read -p "Proceed with commit redistribution? (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 0
fi

echo ""
echo "Generating rebase script..."
echo "================================"
echo ""

# Get parent of first commit
FIRST_COMMIT=${COMMITS[0]}
PARENT_COMMIT=$(git rev-parse "$FIRST_COMMIT^" 2>/dev/null)

if [ $? -ne 0 ]; then
    echo "Error: Cannot find parent commit."
    exit 1
fi

# Create rebase todo file
REBASE_TODO=$(mktemp)

# Distribution arrays
DATES=("2026-01-18" "2026-01-01" "2026-01-02" "2026-01-03" "2026-01-04" "2026-01-10" "2026-01-11")
COUNTS=(25 5 5 5 5 5 5)

INDEX=0

for i in "${!DATES[@]}"; do
    DATE=${DATES[$i]}
    COUNT=${COUNTS[$i]}
    
    echo "Planning $COUNT commits for $DATE..."
    
    for ((j=1; j<=COUNT; j++)); do
        if [ $INDEX -ge $COMMIT_COUNT ]; then
            break 2
        fi
        
        # Calculate time (9 AM to 6 PM)
        TOTAL_MINUTES=$((9 * 60))
        MINUTE_OFFSET=$(( (j - 1) * TOTAL_MINUTES / COUNT ))
        HOUR=$(( 9 + MINUTE_OFFSET / 60 ))
        MINUTE=$(( MINUTE_OFFSET % 60 ))
        SECOND=$(( RANDOM % 50 + 10 ))
        
        TIME=$(printf "%02d:%02d:%02d +0200" $HOUR $MINUTE $SECOND)
        NEW_DATE="$DATE $TIME"
        
        COMMIT_HASH=${COMMITS[$INDEX]}
        COMMIT_MSG=$(git log -1 --format="%s" $COMMIT_HASH)
        
        # Add to rebase todo
        echo "pick $COMMIT_HASH $COMMIT_MSG" >> $REBASE_TODO
        echo "exec GIT_COMMITTER_DATE=\"$NEW_DATE\" git commit --amend --no-edit --date=\"$NEW_DATE\"" >> $REBASE_TODO
        
        echo "  [$((INDEX + 1))] ${COMMIT_HASH:0:8} -> $NEW_DATE"
        
        INDEX=$((INDEX + 1))
    done
    echo ""
done

echo ""
echo "Starting rebase..."
echo "================================"
echo ""

# Run rebase with custom todo
GIT_SEQUENCE_EDITOR="cp $REBASE_TODO" git rebase -i $PARENT_COMMIT

if [ $? -eq 0 ]; then
    echo ""
    echo "================================"
    echo "SUCCESS! Commits redistributed!"
    echo "Processed $INDEX out of $COMMIT_COUNT commits"
    echo ""
    echo "Verify with:"
    echo "  git log --oneline --date=short -30"
    echo ""
    echo "Backup branch: $BACKUP_BRANCH"
    echo "To undo: git reset --hard $BACKUP_BRANCH"
    echo "To push: git push --force"
else
    echo ""
    echo "Rebase encountered issues."
    echo "To abort: git rebase --abort"
    echo "To continue: git rebase --continue"
fi

# Cleanup
rm -f $REBASE_TODO

echo ""
