$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$backendPath = Join-Path $projectRoot 'backend'
$schemaPath = (Join-Path $backendPath 'src/main/resources/db/schema.sql').Replace('\', '/')
$sampleDataPath = (Join-Path $backendPath 'src/main/resources/db/sample-data.sql').Replace('\', '/')
$mysqlExecutable = 'D:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$mavenExecutable = 'D:\Users\Lenovo\AppData\Local\Programs\IntelliJ IDEA 2025.2.2\plugins\maven\lib\maven3\bin\mvn.cmd'
$mavenRepository = 'C:\Users\Lenovo\Documents\Codex\2026-09-19\new-chat\work\m2'
$statusFile = Join-Path $projectRoot 'backend-local.log'

Set-Content -LiteralPath $statusFile -Value 'WAITING_FOR_PASSWORD' -Encoding utf8
Write-Host ''
Write-Host '知序本地数据库初始化' -ForegroundColor Cyan
Write-Host '密码只用于当前进程，不会保存到项目。' -ForegroundColor DarkGray
$securePassword = Read-Host '请输入本机 MySQL root 密码（输入不可见）' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)

try {
    $databasePassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
}

Write-Host ''
Write-Host 'DeepSeek 密钥仅注入当前后端进程，不会写入文件。' -ForegroundColor DarkGray
$secureApiKey = Read-Host '请输入 DeepSeek API Key（输入不可见）' -AsSecureString
$apiKeyPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureApiKey)
try {
    $deepSeekApiKey = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($apiKeyPointer)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($apiKeyPointer)
}

try {
    $env:MYSQL_PWD = $databasePassword
    & $mysqlExecutable --protocol=TCP -h 127.0.0.1 -u root -e 'SELECT VERSION() AS mysql_version;'
    if ($LASTEXITCODE -ne 0) { throw 'MySQL 密码验证失败。' }

    & $mysqlExecutable --protocol=TCP -h 127.0.0.1 -u root --default-character-set=utf8mb4 -e "source $schemaPath"
    if ($LASTEXITCODE -ne 0) { throw '数据库建表失败。' }

    & $mysqlExecutable --protocol=TCP -h 127.0.0.1 -u root --default-character-set=utf8mb4 -e "source $sampleDataPath"
    if ($LASTEXITCODE -ne 0) { throw '演示数据导入失败。' }

    $env:DB_USERNAME = 'root'
    $env:DB_PASSWORD = $databasePassword
    $env:DEEPSEEK_API_KEY = $deepSeekApiKey
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    $databasePassword = $null
    $deepSeekApiKey = $null

    Set-Content -LiteralPath $statusFile -Value 'DATABASE_READY' -Encoding utf8
    Write-Host ''
    Write-Host '数据库初始化成功，正在启动后端服务……' -ForegroundColor Green
    Set-Location -LiteralPath $backendPath
    & $mavenExecutable "-Dmaven.repo.local=$mavenRepository" '-Dmaven.compiler.fork=true' spring-boot:run
} catch {
    Set-Content -LiteralPath $statusFile -Value "FAILED: $($_.Exception.Message)" -Encoding utf8
    Write-Host ''
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host '请关闭窗口后告诉我，我会继续处理。' -ForegroundColor Yellow
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    Remove-Item Env:DEEPSEEK_API_KEY -ErrorAction SilentlyContinue
    $databasePassword = $null
    $deepSeekApiKey = $null
}
