const fs = require('fs');
const path = require('path');

const guestDir = path.join(__dirname, '03_sourcecode/kawai-backend/src/main/resources/templates/guest');

// Remove wellbeing.html
const wellbeingFile = path.join(guestDir, 'wellbeing.html');
if (fs.existsSync(wellbeingFile)) {
    fs.unlinkSync(wellbeingFile);
    console.log('Removed wellbeing.html');
}

// Read all html files
const files = fs.readdirSync(guestDir).filter(f => f.endsWith('.html'));

files.forEach(file => {
    const filePath = path.join(guestDir, file);
    let content = fs.readFileSync(filePath, 'utf8');

    // 1. Remove Wellbeing links
    content = content.replace(/<a[^>]*href="\/wellbeing"[^>]*>.*?<\/a>\s*/g, '');

    // 2. Add animations.js script before closing body
    if (!content.includes('/guest/js/animations.js')) {
        content = content.replace(/<\/body>/g, '    <script src="/guest/js/animations.js"></script>\n</body>');
    }

    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`Refactored ${file}`);
});
