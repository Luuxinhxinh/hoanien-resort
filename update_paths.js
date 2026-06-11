const fs = require('fs');
const path = require('path');

const guestDir = path.join(__dirname, '03_sourcecode/kawai-backend/src/main/resources/templates/guest');
const files = fs.readdirSync(guestDir).filter(f => f.endsWith('.html'));

files.forEach(file => {
    const filePath = path.join(guestDir, file);
    let content = fs.readFileSync(filePath, 'utf8');

    // Update animations.js path
    content = content.replace(/\/guest\/js\/animations\.js(\?v=\d+)?/g, '/js/animations.js?v=3');
    
    // Update booking.css path
    content = content.replace(/\/guest\/css\/booking\.css(\?v=\d+)?/g, '/css/booking.css?v=3');

    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`Updated paths in ${file}`);
});
