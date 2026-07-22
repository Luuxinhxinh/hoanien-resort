$file = '05-Development/kawai-backend/src/main/resources/templates/guest/tour-detail.html'
$content = Get-Content -Raw $file -Encoding UTF8

# 1. Remove the orphaned updateAdultSelectOptions(); at line 2349
$content = $content -replace '(?m)^\s*updateAdultSelectOptions\(\);\s*$', ''

# 2. Add function updateAdultSelectOptions before </script>
$exclusivityJS = @"
        function updateAdultSelectOptions() {
            const selects = document.querySelectorAll('.adult-companion-select');
            const selectedValues = Array.from(selects).map(s => s.value).filter(v => v !== '');
            
            selects.forEach(select => {
                const options = select.querySelectorAll('option');
                options.forEach(opt => {
                    if (opt.value === '') return;
                    if (selectedValues.includes(opt.value) && opt.value !== select.value) {
                        opt.style.display = 'none';
                        opt.disabled = true;
                    } else {
                        opt.style.display = '';
                        opt.disabled = false;
                    }
                });
            });
        }
"@

if (-not $content.Contains('function updateAdultSelectOptions()')) {
    $content = $content -replace '(?m)^    </script>', ($exclusivityJS + "`n    </script>")
}

Set-Content $file $content -Encoding UTF8
