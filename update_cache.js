const fs = require('fs');
const path = require('path');

const guestDir = path.join(__dirname, '03_sourcecode/kawai-backend/src/main/resources/templates/guest');
const files = fs.readdirSync(guestDir).filter(f => f.endsWith('.html'));

files.forEach(file => {
    const filePath = path.join(guestDir, file);
    let content = fs.readFileSync(filePath, 'utf8');

    // Add cache buster to animations.js
    content = content.replace(/\/guest\/js\/animations\.js(\?v=\d+)?/g, '/guest/js/animations.js?v=2');

    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`Updated ${file}`);
});
