const fs = require('fs');
const path = require('path');
const file = path.resolve('05-Development/kawai-backend/src/main/resources/templates/guest/tour-detail.html');
let content = fs.readFileSync(file, 'utf8');

// 1. Remove the checkbox HTML
content = content.replace(/<!-- Checkbox: Tôi là người tham gia chuyến đi -->[\s\S]*?<\/label>\s*<\/div>/, '');

// 2. Modify userDependentsList init
const listInitRegex = /let userDependentsList = \[\];\s*try {\s*userDependentsList = .*?;\s*} catch \(e\) {\s*userDependentsList = \[\];\s*}/;
const newListInit = `let userDependentsList = [];
        try {
            userDependentsList = (typeof rawDependentsJson === 'string') ? JSON.parse(rawDependentsJson) : (rawDependentsJson || []);
            const payerName = document.getElementById('fullName') ? document.getElementById('fullName').value.trim() : '';
            if (payerName) {
                userDependentsList.unshift({
                    id: null,
                    name: payerName,
                    age: 30, // Default adult age for payer
                    relationship: 'Chủ tài khoản'
                });
            }
        } catch (e) {
            userDependentsList = [];
        }`;
content = content.replace(listInitRegex, newListInit);

// 3. Modify validateCompanions logic
const validateReplaceRegex = /\/\/ Nếu khách CHỌN làm người tham gia[\s\S]*?generateAdultCompanionRows\(adultCompanionsCount\);/;
const newValidate = `generateAdultCompanionRows(adults);`;
content = content.replace(validateReplaceRegex, newValidate);

// 4. Modify submitBookingFlow logic
const submitRegex = /const chkParticipateSubmit = document\.getElementById\('isPayerParticipating'\);[\s\S]*?if \(!isPayerParticipatingValSubmit \|\| adultsVal > 1\) {/;
const newSubmit = `if (adultsVal > 0) {`;
content = content.replace(submitRegex, newSubmit);

// 5. Add update function for exclusivity and attach it
const exclusivityJS = `
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
`;
content = content.replace(/(function autoFillAdultCompanion.*?{[\s\S]*?)(return;\s*})(\s*const dep =)/, '$1 updateAdultSelectOptions();\n            $2$3');
content = content.replace(/(function autoFillAdultCompanion.*?{[\s\S]*?)(if \(phoneInput\) phoneInput\.value.*?;\s*})/, '$1$2\n            updateAdultSelectOptions();\n');

if (!content.includes('function updateAdultSelectOptions()')) {
    content = content.replace(/<\/script>\s*<\/body>/, exclusivityJS + '\n</script>\n</body>');
}

// Remove onPayerParticipatingChange function completely
content = content.replace(/function onPayerParticipatingChange\(\) {[\s\S]*?validateCompanions\(false, false\);\s*}/, '');

fs.writeFileSync(file, content, 'utf8');
