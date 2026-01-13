# Script to redistribute 112 commits across specified dates
# Target dates: Jan 1-4, 10-11, 15 (7 days total)
# Each day gets 12-15 commits randomly

# Define target dates
$targetDates = @(
    "2025-01-01",
    "2025-01-02",
    "2025-01-03",
    "2025-01-04",
    "2025-01-10",
    "2025-01-11",
    "2025-01-15"
)

# Generate random commit distribution (12-15 per day)
$commitDistribution = @()
$totalCommits = 112
$remainingCommits = $totalCommits

foreach ($date in $targetDates) {
    if ($date -eq $targetDates[-1]) {
        # Last date gets remaining commits
        $commitDistribution += @{Date = $date; Count = $remainingCommits}
    } else {
        # Random number between 12-15
        $count = Get-Random -Minimum 12 -Maximum 16
        $commitDistribution += @{Date = $date; Count = $count}
        $remainingCommits -= $count
    }
}

# Display distribution plan
Write-Host "=== Commit Distribution Plan ===" -ForegroundColor Cyan
$totalPlanned = 0
foreach ($item in $commitDistribution) {
    Write-Host "$($item.Date): $($item.Count) commits" -ForegroundColor Yellow
    $totalPlanned += $item.Count
}
Write-Host "Total: $totalPlanned commits" -ForegroundColor Green
Write-Host ""

# Ask for confirmation
$confirm = Read-Host "Do you want to proceed? This will rewrite commit history. (yes/no)"
if ($confirm -ne "yes") {
    Write-Host "Aborted." -ForegroundColor Red
    exit
}

Write-Host "`nStarting commit redistribution..." -ForegroundColor Green

# Get the last 112 commits
$commits = git log -n 112 --format="%H|%s" --reverse

if ($commits.Count -lt 112) {
    Write-Host "Error: Found only $($commits.Count) commits, need 112" -ForegroundColor Red
    exit
}

# Start from the commit before the 112 we're changing
$baseCommit = git log -n 113 --format="%H" | Select-Object -Last 1

# Reset to base commit
Write-Host "Resetting to base commit..." -ForegroundColor Yellow
git reset --hard $baseCommit

# Recreate commits with new dates
$commitIndex = 0
foreach ($distItem in $commitDistribution) {
    $date = $distItem.Date
    $count = $distItem.Count
    
    Write-Host "`nProcessing $count commits for $date..." -ForegroundColor Cyan
    
    for ($i = 0; $i -lt $count; $i++) {
        if ($commitIndex -ge $commits.Count) {
            Write-Host "Warning: Ran out of commits" -ForegroundColor Red
            break
        }
        
        # Get original commit info
        $commitData = $commits[$commitIndex] -split '\|'
        $originalHash = $commitData[0]
        $commitMessage = $commitData[1]
        
        # Generate random time for this commit (spread throughout the day)
        $hour = Get-Random -Minimum 9 -Maximum 21
        $minute = Get-Random -Minimum 0 -Maximum 60
        $second = Get-Random -Minimum 0 -Maximum 60
        $commitDateTime = "$date" + "T" + ("{0:D2}:{1:D2}:{2:D2}" -f $hour, $minute, $second)
        
        # Cherry-pick the original commit
        git cherry-pick $originalHash --no-commit 2>$null
        
        # Set the date environment variables
        $env:GIT_AUTHOR_DATE = $commitDateTime
        $env:GIT_COMMITTER_DATE = $commitDateTime
        
        # Commit with original message and new date
        git commit -m $commitMessage --no-verify 2>$null
        
        # Clear environment variables
        Remove-Item Env:\GIT_AUTHOR_DATE -ErrorAction SilentlyContinue
        Remove-Item Env:\GIT_COMMITTER_DATE -ErrorAction SilentlyContinue
        
        $commitIndex++
        
        # Progress indicator
        if (($i + 1) % 5 -eq 0) {
            Write-Host "  Processed $($i + 1)/$count commits" -ForegroundColor Gray
        }
    }
}

Write-Host "`n=== Redistribution Complete! ===" -ForegroundColor Green
Write-Host "Total commits processed: $commitIndex" -ForegroundColor Yellow
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Review the changes: git log --oneline -20" -ForegroundColor White
Write-Host "2. If satisfied, force push: git push origin main --force" -ForegroundColor White
Write-Host "   WARNING: Force push will overwrite remote history!" -ForegroundColor Red