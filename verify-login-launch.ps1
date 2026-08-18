# Login Launch Configuration Verification Script
# Task 5.2: Verify application launches with login screen
# Requirements: 1.1, 8.1, 8.2

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Login Launch Configuration Verification" -ForegroundColor Cyan
Write-Host "Task 5.2 - login-system spec" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$allPassed = $true

# Test 1: Verify Main.java exists and contains correct configuration
Write-Host "Test 1: Verifying Main.java configuration..." -ForegroundColor Yellow
$mainFile = "src\main\java\tests\Main.java"
if (Test-Path $mainFile) {
    $mainContent = Get-Content $mainFile -Raw
    
    # Check for login-view.fxml loading
    if ($mainContent -match '/login-view\.fxml') {
        Write-Host "  ✓ PASS: Main.java loads login-view.fxml" -ForegroundColor Green
    } else {
        Write-Host "  ✗ FAIL: Main.java does not load login-view.fxml" -ForegroundColor Red
        $allPassed = $false
    }
    
    # Check for correct window title
    if ($mainContent -match 'Connexion - Lacroix Electronics') {
        Write-Host "  ✓ PASS: Window title is correctly set" -ForegroundColor Green
    } else {
        Write-Host "  ✗ FAIL: Window title is not 'Connexion - Lacroix Electronics'" -ForegroundColor Red
        $allPassed = $false
    }
    
    # Check for minimum width
    if ($mainContent -match 'setMinWidth\(500\)') {
        Write-Host "  ✓ PASS: Minimum width set to 500px" -ForegroundColor Green
    } else {
        Write-Host "  ✗ FAIL: Minimum width not set to 500px" -ForegroundColor Red
        $allPassed = $false
    }
    
    # Check for minimum height
    if ($mainContent -match 'setMinHeight\(400\)') {
        Write-Host "  ✓ PASS: Minimum height set to 400px" -ForegroundColor Green
    } else {
        Write-Host "  ✗ FAIL: Minimum height not set to 400px" -ForegroundColor Red
        $allPassed = $false
    }
} else {
    Write-Host "  ✗ FAIL: Main.java not found at $mainFile" -ForegroundColor Red
    $allPassed = $false
}
Write-Host ""

# Test 2: Verify login-view.fxml exists
Write-Host "Test 2: Verifying login-view.fxml exists..." -ForegroundColor Yellow
$loginViewFile = "src\main\resources\login-view.fxml"
if (Test-Path $loginViewFile) {
    Write-Host "  ✓ PASS: login-view.fxml found at $loginViewFile" -ForegroundColor Green
    
    $loginContent = Get-Content $loginViewFile -Raw
    
    # Check for controller binding
    if ($loginContent -match 'controller\.LoginController') {
        Write-Host "  ✓ PASS: LoginController is properly bound" -ForegroundColor Green
    } else {
        Write-Host "  ✗ FAIL: LoginController binding not found" -ForegroundColor Red
        $allPassed = $false
    }
    
    # Check for required UI elements
    $requiredElements = @(
        @{Name="usernameField"; Pattern='fx:id="usernameField"'},
        @{Name="passwordField"; Pattern='fx:id="passwordField"'},
        @{Name="btnLogin"; Pattern='fx:id="btnLogin"'},
        @{Name="errorLabel"; Pattern='fx:id="errorLabel"'}
    )
    
    foreach ($element in $requiredElements) {
        if ($loginContent -match $element.Pattern) {
            Write-Host "  ✓ PASS: $($element.Name) element found" -ForegroundColor Green
        } else {
            Write-Host "  ✗ FAIL: $($element.Name) element not found" -ForegroundColor Red
            $allPassed = $false
        }
    }
} else {
    Write-Host "  ✗ FAIL: login-view.fxml not found at $loginViewFile" -ForegroundColor Red
    $allPassed = $false
}
Write-Host ""

# Test 3: Verify LoginController exists
Write-Host "Test 3: Verifying LoginController.java exists..." -ForegroundColor Yellow
$controllerFile = "src\main\java\controller\LoginController.java"
if (Test-Path $controllerFile) {
    Write-Host "  ✓ PASS: LoginController.java found at $controllerFile" -ForegroundColor Green
    
    $controllerContent = Get-Content $controllerFile -Raw
    
    # Check for required methods
    $requiredMethods = @("initialize", "handleLogin", "showError", "navigateToDashboard")
    foreach ($method in $requiredMethods) {
        if ($controllerContent -match $method) {
            Write-Host "  ✓ PASS: $method method found" -ForegroundColor Green
        } else {
            Write-Host "  ✗ FAIL: $method method not found" -ForegroundColor Red
            $allPassed = $false
        }
    }
} else {
    Write-Host "  ✗ FAIL: LoginController.java not found at $controllerFile" -ForegroundColor Red
    $allPassed = $false
}
Write-Host ""

# Test 4: Verify CSS file exists
Write-Host "Test 4: Verifying style.css exists..." -ForegroundColor Yellow
$cssFile = "src\main\resources\css\style.css"
if (Test-Path $cssFile) {
    Write-Host "  ✓ PASS: style.css found at $cssFile" -ForegroundColor Green
} else {
    Write-Host "  ✗ FAIL: style.css not found at $cssFile" -ForegroundColor Red
    $allPassed = $false
}
Write-Host ""

# Test 5: Verify pom.xml configuration
Write-Host "Test 5: Verifying Maven configuration..." -ForegroundColor Yellow
$pomFile = "pom.xml"
if (Test-Path $pomFile) {
    $pomContent = Get-Content $pomFile -Raw
    
    # Check for main class configuration
    if ($pomContent -match 'tests\.Main') {
        Write-Host "  ✓ PASS: Main class correctly configured in pom.xml" -ForegroundColor Green
    } else {
        Write-Host "  ✗ FAIL: Main class not configured as tests.Main" -ForegroundColor Red
        $allPassed = $false
    }
    
    # Check for JavaFX dependencies
    if ($pomContent -match 'javafx-controls' -and $pomContent -match 'javafx-fxml') {
        Write-Host "  ✓ PASS: JavaFX dependencies found" -ForegroundColor Green
    } else {
        Write-Host "  ✗ FAIL: JavaFX dependencies missing" -ForegroundColor Red
        $allPassed = $false
    }
} else {
    Write-Host "  ✗ FAIL: pom.xml not found" -ForegroundColor Red
    $allPassed = $false
}
Write-Host ""

# Test 6: Check Java version
Write-Host "Test 6: Verifying Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion -match '17') {
        Write-Host "  ✓ PASS: Java 17 is installed" -ForegroundColor Green
        Write-Host "  $javaVersion" -ForegroundColor Gray
    } else {
        Write-Host "  ⚠ WARNING: Java version may not match project requirements (needs Java 17)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ FAIL: Java not found in PATH" -ForegroundColor Red
    $allPassed = $false
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VERIFICATION SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($allPassed) {
    Write-Host "✓ ALL STATIC TESTS PASSED" -ForegroundColor Green
    Write-Host ""
    Write-Host "Configuration Verified:" -ForegroundColor Green
    Write-Host "  • Main.java loads login-view.fxml as initial scene" -ForegroundColor White
    Write-Host "  • Window title: 'Connexion - Lacroix Electronics'" -ForegroundColor White
    Write-Host "  • Minimum dimensions: 500px × 400px" -ForegroundColor White
    Write-Host "  • All required resources present" -ForegroundColor White
    Write-Host "  • LoginController properly implemented" -ForegroundColor White
    Write-Host ""
    Write-Host "MANUAL TESTING REQUIRED:" -ForegroundColor Yellow
    Write-Host "To complete Task 5.2 verification, run the application:" -ForegroundColor White
    Write-Host "  1. Open IntelliJ IDEA" -ForegroundColor White
    Write-Host "  2. Build > Rebuild Project" -ForegroundColor White
    Write-Host "  3. Right-click tests/Main.java > Run 'Main.main()'" -ForegroundColor White
    Write-Host "  4. Verify login screen displays correctly" -ForegroundColor White
    Write-Host "  5. Verify window title and minimum dimensions" -ForegroundColor White
    Write-Host ""
    Write-Host "See TASK_5.2_VERIFICATION_SUMMARY.md for detailed test procedures." -ForegroundColor Cyan
} else {
    Write-Host "✗ SOME TESTS FAILED" -ForegroundColor Red
    Write-Host "Please review the failures above and fix the configuration." -ForegroundColor Red
}

Write-Host "========================================" -ForegroundColor Cyan
