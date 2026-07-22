$file = '05-Development/kawai-backend/src/main/resources/templates/guest/tour-detail.html'
$content = Get-Content -Raw $file -Encoding UTF8
$content = $content -replace '(?s)const payerName.*?\}', ''
Set-Content $file $content -Encoding UTF8
