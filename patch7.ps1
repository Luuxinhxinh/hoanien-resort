$file = '05-Development/kawai-backend/src/main/resources/templates/guest/tour-detail.html'
$content = Get-Content -Raw $file -Encoding UTF8

$content = $content -replace '(?s)(if \(phoneInput\) phoneInput\.value = dep\.phone \|\| '''';\s*\})', "`$1`n            updateAdultSelectOptions();"

$content = $content -replace '(?s)(if \(phoneInput\) phoneInput\.value = '''';\s*)(return;\s*\})', "`$1 updateAdultSelectOptions();`n            `$2"

Set-Content $file $content -Encoding UTF8
