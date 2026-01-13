# Script to redistribute commits from specific dates to empty days
# Based on the working redistribution script

# Source dates - commits we'll take FROM these dates
$sourceDates = @(
    "2025-10-29",
    "2025-10-30",
    "2025-10-31",
    "2025-11-01",
    "2025-11-19",
    "2025-11-20",
    "2025-11-21",
    "2025-12-06",
    "2025-12-17"
)

Write-Host "=== Building Target Date List ===" -ForegroundColor Cyan

# Generate target dates (empty days to fill)
$targetDates = @()

# 1. Jan 5 - May 5, 2025
$start = Get-Date "2025-01-05"
$end = Get-Date "2025-05-05"
while ($start -le $end) {
    $targetDates += $start.ToString("yyyy-MM-dd")
    $start = $start.AddDays(1)
}

# 2. June 2 - Aug 8, 2025 (excluding Sundays between June 22 - Aug 3)
$start = Get-Date "2025-06-02"
$end = Get-Date "2025-08-08"
while ($start -le $end) {
    $dateStr = $start.ToString("yyyy-MM-dd")
    
    # Skip Sundays between June 22 - Aug 3
    if ($start -gt (Get-Date "2025-06-21") -and $start -lt (Get-Date "2025-08-04") -and $start.DayOfWeek -eq "Sunday") {
        # Skip this date
    } else {
        $targetDates += $dateStr
    }
    $start = $start.AddDays(1)
}

# 3. Aug 10 - Oct 27, 2025
$start = Get-Date "2025-08-10"
$end = Get-Date "2025-10-27"
while ($start -le $end) {
    $targetDates += $start.ToString("yyyy-MM-dd")
    $start = $start.AddDays(1)
}

# 4. Specific Sundays and Saturdays
$specificDates = @(
    "2025-11-02", "2025-11-08", "2025-11-09", "2025-11-15", 
    "2025-11-16", "2025-11-22", "2025-11-23", "2025-11-29", 
    "2025-11-30", "2025-12-07", "2025-12-14", "2025-12-21", "2025-12-28"
)
$targetDates += $specificDates

Write-Host "Total target days: $($targetDates.Count)" -ForegroundColor Yellow
Write-Host ""

# Count commits from source dates
Write-Host "=== Counting Source Commits ===" -ForegroundColor Cyan
$allCommitsToMove = @()

foreach ($srcDate in $sourceDates) {
    $logOutput = git log --date=short --format="%H|%ad|%s" | Select-String "\\|$srcDate\\|"
    
    $count = 0
    if ($logOutput) {
        foreach ($line in $logOutput) {
            $allCommitsToMove += $line.ToString()
            $count++
        }
    }
    
    Write-Host "  $srcDate : $count commits" -ForegroundColor Gray
}

$totalCommits = $allCommitsToMove.Count
Write-Host ""
Write-Host "Total commits to redistribute: $totalCommits" -ForegroundColor Green
Write-Host "Total target days: $($targetDates.Count)" -ForegroundColor Green

if ($totalCommits -eq 0) {
    Write-Host "`nError: No commits found on source dates!" -ForegroundColor Red
    exit
}

# Calculate distribution
$commitsPerDay = [Math]::Floor($totalCommits / $targetDates.Count)
$remainder = $totalCommits % $targetDates.Count

Write-Host "Average commits per day: $commitsPerDay" -ForegroundColor Yellow
Write-Host "Remainder commits: $remainder" -ForegroundColor Yellow
Write-Host ""

# Ask for confirmation
$confirm = Read-Host "Do you want to proceed? This will rewrite commit history. (yes/no)"
if ($confirm -ne "yes") {
    Write-Host "Aborted." -ForegroundColor Red
    exit
}

Write-Host "`nStarting redistribution..." -ForegroundColor Green

# Find the commit just before the earliest source date
$earliestDate = ($sourceDates | Sort-Object)[0]
$baseCommit = git log --before="$earliestDate" --format="%H" -1

if (-not $baseCommit) {
    Write-Host "Error: Could not find base commit" -ForegroundColor Red
    exit
}

Write-Host "Resetting to base commit: $baseCommit" -ForegroundColor Yellow
git reset --hard $baseCommit

# Redistribute commits
$commitIndex = 0
$dateIndex = 0

foreach ($targetDate in $targetDates) {
    # Calculate commits for this day
    $commitsForDay = $commitsPerDay
    
    # Randomly distribute remainder
    if ($remainder -gt 0 -and (Get-Random -Maximum 100) -lt 40) {
        $commitsForDay++
        $remainder--
    }
    
    # Don't exceed available commits
    if ($commitIndex + $commitsForDay -gt $allCommitsToMove.Count) {
        $commitsForDay = $allCommitsToMove.Count - $commitIndex
    }
    
    if ($commitsForDay -eq 0) {
        break
    }
    
    $dateIndex++
    if ($dateIndex % 20 -eq 0) {
        Write-Host "Processing date $dateIndex / $($targetDates.Count)..." -ForegroundColor Cyan
    }
    
    for ($i = 0; $i -lt $commitsForDay; $i++) {
        if ($commitIndex -ge $allCommitsToMove.Count) {
            break
        }
        
        # Parse commit data
        $commitLine = $allCommitsToMove[$commitIndex]
        $parts = $commitLine -split '\|'
        $hash = $parts[0]
        $msg = if ($parts.Count -gt 2) { $parts[2] } else { "Update" }
        
        # Generate random time
        $hour = Get-Random -Minimum 9 -Maximum 21
        $minute = Get-Random -Minimum 0 -Maximum 60
        $second = Get-Random -Minimum 0 -Maximum 60
        $commitDateTime = "$targetDate" + "T" + ("{0:D2}:{1:D2}:{2:D2}" -f $hour, $minute, $second)
        
        # Cherry-pick the commit
        git cherry-pick $hash --no-commit 2>$null | Out-Null
        
        # Set date and commit
        $env:GIT_AUTHOR_DATE = $commitDateTime
        $env:GIT_COMMITTER_DATE = $commitDateTime
        
        git commit -m $msg --no-verify 2>$null | Out-Null
        
        Remove-Item Env:\GIT_AUTHOR_DATE -ErrorAction SilentlyContinue
        Remove-Item Env:\GIT_COMMITTER_DATE -ErrorAction SilentlyContinue
        
        $commitIndex++
    }
}

Write-Host "`n=== Redistribution Complete! ===" -ForegroundColor Green
Write-Host "Redistributed $commitIndex commits across $dateIndex days" -ForegroundColor Yellow
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Review: git log --oneline --date=short -20" -ForegroundColor White
Write-Host "2. Force push: git push origin main --force" -ForegroundColor White
Write-Host "   WARNING: This will overwrite remote history!" -ForegroundColor Red