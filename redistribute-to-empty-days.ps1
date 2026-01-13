# Script to redistribute commits from specific dates to fill empty days

# Source dates (where we'll take commits FROM)
$sourceDates = @(
    "2025-10-29", "2025-10-30", "2025-10-31",
    "2025-11-01",
    "2025-11-19", "2025-11-20", "2025-11-21",
    "2025-12-06", "2025-12-17"
)

# Generate all target dates (where we'll PUT commits TO)
$targetDates = @()

# 1. Jan 5 - May 10, 2025
$start = Get-Date "2025-01-05"
$end = Get-Date "2025-05-10"
while ($start -le $end) {
    $targetDates += $start.ToString("yyyy-MM-dd")
    $start = $start.AddDays(1)
}

# 2. June 9 - Aug 8, 2025
$start = Get-Date "2025-06-09"
$end = Get-Date "2025-08-08"
while ($start -le $end) {
    $targetDates += $start.ToString("yyyy-MM-dd")
    $start = $start.AddDays(1)
}

# 3. Aug 10 - Oct 27, 2025
$start = Get-Date "2025-08-10"
$end = Get-Date "2025-10-27"
while ($start -le $end) {
    $targetDates += $start.ToString("yyyy-MM-dd")
    $start = $start.AddDays(1)
}

# 4. All Sundays from Nov 2 - Dec 28, 2025
$start = Get-Date "2025-11-02"
$end = Get-Date "2025-12-28"
while ($start -le $end) {
    if ($start.DayOfWeek -eq "Sunday") {
        $targetDates += $start.ToString("yyyy-MM-dd")
    }
    $start = $start.AddDays(1)
}

# 5. Saturdays from Nov 8 - Nov 29, 2025
$saturdayDates = @("2025-11-08", "2025-11-15", "2025-11-22", "2025-11-29")
$targetDates += $saturdayDates

# Remove duplicates and sort
$targetDates = $targetDates | Sort-Object -Unique

Write-Host "=== Commit Redistribution Plan ===" -ForegroundColor Cyan
Write-Host "Source dates (taking commits FROM): $($sourceDates.Count) dates" -ForegroundColor Yellow
Write-Host "Target dates (distributing commits TO): $($targetDates.Count) dates" -ForegroundColor Yellow
Write-Host ""

# Count commits from source dates
Write-Host "Counting commits from source dates..." -ForegroundColor Green
$totalCommitsToMove = 0
foreach ($date in $sourceDates) {
    $count = (git log --all --since="$date 00:00:00" --until="$date 23:59:59" --oneline | Measure-Object).Count
    $totalCommitsToMove += $count
    Write-Host "  $date : $count commits" -ForegroundColor Gray
}

Write-Host "`nTotal commits to redistribute: $totalCommitsToMove" -ForegroundColor Green
Write-Host "Total target days: $($targetDates.Count)" -ForegroundColor Green

if ($totalCommitsToMove -eq 0) {
    Write-Host "`nError: No commits found on source dates!" -ForegroundColor Red
    exit
}

# Calculate commits per day (evenly distributed)
$commitsPerDay = [Math]::Floor($totalCommitsToMove / $targetDates.Count)
$remainderCommits = $totalCommitsToMove % $targetDates.Count

Write-Host "Average commits per day: $commitsPerDay" -ForegroundColor Yellow
Write-Host "Remainder commits: $remainderCommits (will be distributed randomly)" -ForegroundColor Yellow
Write-Host ""

# Ask for confirmation
$confirm = Read-Host "Do you want to proceed? This will rewrite commit history. (yes/no)"
if ($confirm -ne "yes") {
    Write-Host "Aborted." -ForegroundColor Red
    exit
}

Write-Host "`nFetching commits from source dates..." -ForegroundColor Green

# Get all commits from source dates
$allCommits = @()
foreach ($date in $sourceDates) {
    $commits = git log --all --since="$date 00:00:00" --until="$date 23:59:59" --format="%H|%s" --reverse
    if ($commits) {
        foreach ($commit in $commits) {
            $allCommits += $commit
        }
    }
}

if ($allCommits.Count -eq 0) {
    Write-Host "Error: No commits found!" -ForegroundColor Red
    exit
}

Write-Host "Found $($allCommits.Count) commits to redistribute" -ForegroundColor Green

# Find the oldest commit to reset to
$oldestSourceDate = ($sourceDates | Sort-Object)[0]
$baseCommit = git log --all --until="$oldestSourceDate 00:00:00" --format="%H" -1

if (-not $baseCommit) {
    Write-Host "Error: Could not find base commit" -ForegroundColor Red
    exit
}

Write-Host "Resetting to base commit: $baseCommit" -ForegroundColor Yellow
git reset --hard $baseCommit

# Redistribute commits across target dates
$commitIndex = 0
$processedDates = 0

foreach ($targetDate in $targetDates) {
    # Calculate how many commits for this day
    $commitsForDay = $commitsPerDay
    
    # Randomly add remainder commits
    if ($remainderCommits -gt 0 -and (Get-Random -Maximum 100) -lt 30) {
        $commitsForDay++
        $remainderCommits--
    }
    
    # Don't exceed available commits
    if ($commitIndex + $commitsForDay -gt $allCommits.Count) {
        $commitsForDay = $allCommits.Count - $commitIndex
    }
    
    if ($commitsForDay -eq 0) {
        break
    }
    
    $processedDates++
    if ($processedDates % 10 -eq 0) {
        Write-Host "Processing date $processedDates / $($targetDates.Count)..." -ForegroundColor Cyan
    }
    
    for ($i = 0; $i -lt $commitsForDay; $i++) {
        if ($commitIndex -ge $allCommits.Count) {
            break
        }
        
        # Get commit info
        $commitData = $allCommits[$commitIndex] -split '\|', 2
        $originalHash = $commitData[0]
        $commitMessage = if ($commitData.Count -gt 1) { $commitData[1] } else { "Update" }
        
        # Generate random time
        $hour = Get-Random -Minimum 9 -Maximum 21
        $minute = Get-Random -Minimum 0 -Maximum 60
        $second = Get-Random -Minimum 0 -Maximum 60
        $commitDateTime = "$targetDate" + "T" + ("{0:D2}:{1:D2}:{2:D2}" -f $hour, $minute, $second)
        
        # Cherry-pick and commit with new date
        git cherry-pick $originalHash --no-commit 2>$null | Out-Null
        
        $env:GIT_AUTHOR_DATE = $commitDateTime
        $env:GIT_COMMITTER_DATE = $commitDateTime
        
        git commit -m $commitMessage --no-verify 2>$null | Out-Null
        
        Remove-Item Env:\GIT_AUTHOR_DATE -ErrorAction SilentlyContinue
        Remove-Item Env:\GIT_COMMITTER_DATE -ErrorAction SilentlyContinue
        
        $commitIndex++
    }
}

Write-Host "`n=== Redistribution Complete! ===" -ForegroundColor Green
Write-Host "Total commits redistributed: $commitIndex" -ForegroundColor Yellow
Write-Host "Dates filled: $processedDates / $($targetDates.Count)" -ForegroundColor Yellow
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Review: git log --oneline --date=short --pretty=format:'%ad %s' -50" -ForegroundColor White
Write-Host "2. Force push: git push origin main --force" -ForegroundColor White
Write-Host "   WARNING: This will overwrite remote history!" -ForegroundColor Red