# Set Git commit date to Sunday, January 25, 2026
# Random time between 08:00 AM and 17:00 PM (5:00 PM)

# Generate random hour (8-16, because 17:00 is the max)
$randomHour = Get-Random -Minimum 8 -Maximum 17

# Generate random minute (0-59)
$randomMinute = Get-Random -Minimum 0 -Maximum 60

# Generate random second (0-59)
$randomSecond = Get-Random -Minimum 0 -Maximum 60

# Format time as HH:MM:SS
$timeString = "{0:D2}:{1:D2}:{2:D2}" -f $randomHour, $randomMinute, $randomSecond

# Create the full commit date string
$commitDate = "Sun Jan 25 $timeString 2026 +0200"

# Set environment variables for the current PowerShell session
$env:GIT_AUTHOR_DATE = $commitDate
$env:GIT_COMMITTER_DATE = $commitDate

Write-Host "Git commit date has been set to: $commitDate" -ForegroundColor Green
Write-Host "Time: $timeString (Random between 08:00 and 17:00)" -ForegroundColor Cyan
Write-Host ""
Write-Host "This setting applies to the current PowerShell session only." -ForegroundColor Yellow
Write-Host ""
Write-Host "To make commits with this date, use git normally:" -ForegroundColor Cyan
Write-Host "  git add ." -ForegroundColor White
Write-Host "  git commit -m 'Your commit message'" -ForegroundColor White
Write-Host ""
Write-Host "Run this script again before each commit to get a new random time!" -ForegroundColor Green