# Script để kill process đang sử dụng port 3000
Write-Host "=== Tìm và dừng process đang sử dụng port 3000 ===" -ForegroundColor Cyan

$port = 3000
$processes = netstat -ano | findstr ":$port"

if ($processes) {
    Write-Host "`nĐang tìm process sử dụng port $port..." -ForegroundColor Yellow
    
    $processes | ForEach-Object {
        $parts = $_ -split '\s+'
        $pid = $parts[-1]
        
        if ($pid -match '^\d+$') {
            Write-Host "Tìm thấy process ID: $pid" -ForegroundColor Yellow
            try {
                $processInfo = Get-Process -Id $pid -ErrorAction SilentlyContinue
                if ($processInfo) {
                    Write-Host "  Process: $($processInfo.ProcessName)" -ForegroundColor Cyan
                    Write-Host "  Đang dừng process..." -ForegroundColor Yellow
                    Stop-Process -Id $pid -Force
                    Write-Host "  ✅ Đã dừng process $pid" -ForegroundColor Green
                }
            } catch {
                Write-Host "  ⚠️  Không thể dừng process $pid: $_" -ForegroundColor Red
            }
        }
    }
    
    Write-Host "`n✅ Hoàn tất!" -ForegroundColor Green
    Write-Host "Bây giờ bạn có thể chạy server với: npm start" -ForegroundColor Cyan
} else {
    Write-Host "✅ Không có process nào đang sử dụng port $port" -ForegroundColor Green
}

