# Script khoi dong ung dung ManagerStudentSystem
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 1. Nap cau hinh tu file .env
$envFile = Join-Path $scriptDir ".env"
if (Test-Path $envFile) {
    Write-Host "[INFO] Dang nap bien moi truong tu file .env..." -ForegroundColor Cyan
    Get-Content $envFile | Where-Object { $_ -match '^\s*[^#=\s]+\s*=' } | ForEach-Object {
        $parts = $_ -split '=', 2
        $key = $parts[0].Trim()
        $val = $parts[1].Trim()
        [System.Environment]::SetEnvironmentVariable($key, $val, 'Process')
    }
} else {
    Write-Warning "Khong tim thay file .env!"
}

# 2. Cau hinh bundled JDK 21 va Maven
$jdkPath = Join-Path $scriptDir ".build-tools\jdk\jdk-21.0.12.1+1"
$mavenPath = Join-Path $scriptDir ".build-tools\maven\apache-maven-3.9.11"

if (Test-Path $jdkPath) {
    $env:JAVA_HOME = $jdkPath
    $env:PATH = "$jdkPath\bin;$mavenPath\bin;$env:PATH"
    Write-Host "[INFO] Su dung JDK 21 va Maven tu .build-tools" -ForegroundColor Green
} else {
    Write-Warning "Khong tim thay .build-tools, dung Java/Maven he thong"
}

# 3. Kiem tra DB_PASSWORD
if ($env:DB_PASSWORD -eq "YOUR_DATABASE_PASSWORD" -or [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    Write-Host "[CANH BAO] Vui long mo file .env va dien mat khau Database Supabase vao muc DB_PASSWORD!" -ForegroundColor Red
    exit 1
}

# 4. Khoi chay ung dung
Write-Host "[INFO] Dang khoi chay ung dung Spring Boot..." -ForegroundColor Cyan
mvn clean spring-boot:run "-Dmaven.test.skip=true"
