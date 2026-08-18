# Simple Login Launch Configuration Verification
# Task 5.2: Verify application launches with login screen

Write-Host "`n=== Login Launch Verification ===" -ForegroundColor Cyan
Write-Host "Task 5.2 - login-system spec`n" -ForegroundColor Cyan

$tests = 0
$passed = 0

# Test 1: Main.java configuration
Write-Host "[ 1] Checking Main.java..." -NoNewline
if ((Test-Path "src\main\java\tests\Main.java") -and 
    ((Get-Content "src\main\java\tests\Main.java" -Raw) -match '/login-view\.fxml') -and
    ((Get-Content "src\main\java\tests\Main.java" -Raw) -match 'Connexion - Lacroix Electronics') -and
    ((Get-Content "src\main\java\tests\Main.java" -Raw) -match 'setMinWidth\(500\)') -and
    ((Get-Content "src\main\java\tests\Main.java" -Raw) -match 'setMinHeight\(400\)')) {
    Write-Host " PASS" -ForegroundColor Green
    $passed++
} else {
    Write-Host " FAIL" -ForegroundColor Red
}
$tests++

# Test 2: login-view.fxml exists
Write-Host "[ 2] Checking login-view.fxml..." -NoNewline
if (Test-Path "src\main\resources\login-view.fxml") {
    Write-Host " PASS" -ForegroundColor Green
    $passed++
} else {
    Write-Host " FAIL" -ForegroundColor Red
}
$tests++

# Test 3: LoginController exists
Write-Host "[ 3] Checking LoginController..." -NoNewline
if (Test-Path "src\main\java\controller\LoginController.java") {
    Write-Host " PASS" -ForegroundColor Green
    $passed++
} else {
    Write-Host " FAIL" -ForegroundColor Red
}
$tests++

# Test 4: CSS file exists
Write-Host "[ 4] Checking style.css..." -NoNewline
if (Test-Path "src\main\resources\css\style.css") {
    Write-Host " PASS" -ForegroundColor Green
    $passed++
} else {
    Write-Host " FAIL" -ForegroundColor Red
}
$tests++

# Test 5: pom.xml configuration
Write-Host "[ 5] Checking pom.xml..." -NoNewline
if ((Test-Path "pom.xml") -and 
    ((Get-Content "pom.xml" -Raw) -match 'tests\.Main')) {
    Write-Host " PASS" -ForegroundColor Green
    $passed++
} else {
    Write-Host " FAIL" -ForegroundColor Red
}
$tests++

# Summary
Write-Host "`n=== Results ===" -ForegroundColor Cyan
Write-Host "$passed / $tests tests passed" -ForegroundColor $(if ($passed -eq $tests) { "Green" } else { "Yellow" })

if ($passed -eq $tests) {
    Write-Host "`n✓ Configuration verified!" -ForegroundColor Green
    Write-Host "`nMain.java correctly configured with:" -ForegroundColor White
    Write-Host "  - Login view as initial scene" -ForegroundColor Gray
    Write-Host "  - Window title: 'Connexion - Lacroix Electronics'" -ForegroundColor Gray
    Write-Host "  - Minimum size: 500x400 pixels" -ForegroundColor Gray
    Write-Host "`nNext Step: Run the application to complete manual verification" -ForegroundColor Yellow
    Write-Host "See TASK_5.2_VERIFICATION_SUMMARY.md for details" -ForegroundColor Cyan
} else {
    Write-Host "`n✗ Some configuration issues found" -ForegroundColor Red
}

Write-Host ""
