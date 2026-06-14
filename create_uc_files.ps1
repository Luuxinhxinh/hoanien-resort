$mods = @{}
$mods['mod1_auth'] = @('uc02','uc03','uc04','uc05','uc06','uc07','uc08')
$mods['mod3_pos'] = @('uc14','uc15','uc16','uc17','uc18')
$mods['mod4_tour'] = @('uc19','uc20','uc21','uc22','uc23')
$mods['mod5_finance'] = @('uc24','uc25','uc26','uc27','uc28')

$descriptions = @{}
$descriptions['uc02']='UC02 Profile Management'
$descriptions['uc03']='UC03 Employee Management CRUD'
$descriptions['uc04']='UC04 Master Data Management'
$descriptions['uc05']='UC05 RBAC Role Management'
$descriptions['uc06']='UC06 Audit Log Monitoring'
$descriptions['uc07']='UC07 PII Anonymization GDPR'
$descriptions['uc08']='UC08 Price and Category Config'
$descriptions['uc14']='UC14 Menu and E-Menu'
$descriptions['uc15']='UC15 Restaurant Table Booking'
$descriptions['uc16']='UC16 POS Point of Sale'
$descriptions['uc17']='UC17 Kitchen Display System KDS'
$descriptions['uc18']='UC18 Post to Room Credit Limit'
$descriptions['uc19']='UC19 Tour Search and Weather'
$descriptions['uc20']='UC20 Tour Booking and Schedule'
$descriptions['uc21']='UC21 AI Face Scan Attendance'
$descriptions['uc22']='UC22 Review and Feedback'
$descriptions['uc23']='UC23 Tour Schedule Management'
$descriptions['uc24']='UC24 Night Audit Auto Folio'
$descriptions['uc25']='UC25 Checkout Settlement'
$descriptions['uc26']='UC26 Consolidated Invoice'
$descriptions['uc27']='UC27 USALI Revenue Report'
$descriptions['uc28']='UC28 PDF Excel Report Export'

$owners = @{}
$owners['mod1_auth']='Luu'
$owners['mod3_pos']='Duc'
$owners['mod4_tour']='Ngoc'
$owners['mod5_finance']='Lan'

$modPrefix = @{}
$modPrefix['mod1_auth']='MOD1'
$modPrefix['mod3_pos']='MOD3'
$modPrefix['mod4_tour']='MOD4'
$modPrefix['mod5_finance']='MOD5'

foreach($mod in $mods.Keys) {
    $owner = $owners[$mod]
    $prefix = $modPrefix[$mod]
    $base = "06-Testing/$mod"
    foreach($uc in $mods[$mod]) {
        $desc = $descriptions[$uc]
        $ucUpper = $uc.ToUpper()
        $tddFile = "$base/$uc/TDD_$uc`_SPEC.md"
        $edsFile = "$base/$uc/EDS_$uc`_SPEC.md"
        
        $tddContent = @"
# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## $desc

| Field | Value |
|-------|-------|
| Document ID | KAWAI-TDD-$prefix-$ucUpper-001 |
| Version | 1.0 |
| Date | 2026-06-14 |
| Status | Draft |
| Author | $owner |
| Classification | Internal |

### Test Cases
TC-$ucUpper-001 to TC-$ucUpper-XXX

### Red-Green-Refactor Tracker
| TC ID | Description | RED | GREEN | REFACTOR |
|-------|-------------|-----|-------|----------|
"@
        [System.IO.File]::WriteAllText($tddFile, $tddContent, [System.Text.Encoding]::UTF8)

        $edsContent = @"
# ENGINEERING DOCUMENTATION STANDARD (EDS) v2.0
## $desc

| Field | Value |
|-------|-------|
| Document ID | KAWAI-EDS-$prefix-$ucUpper-001 |
| Version | 1.0 |
| Date | 2026-06-14 |
| Status | Draft |
| Document Owner | $owner |
| Based on EDS | v2.0 |

### Section 1: Module Overview
$desc - $prefix

### Section 9: API Specification
API endpoints for $ucUpper

### Section 10: Error Codes
Standard error codes for $ucUpper
"@
        [System.IO.File]::WriteAllText($edsFile, $edsContent, [System.Text.Encoding]::UTF8)
    }
}
Write-Host "Created all files successfully"