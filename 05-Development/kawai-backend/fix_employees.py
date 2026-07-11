with open('src/main/resources/data.sql', 'r', encoding='utf-8') as f:
    text = f.read()

# Fix the syntax error in Employees insert
# Find: 7500000);\n\n\n,
# Replace with: 7500000)\n,

import re
text = re.sub(r'7500000\s*\);\s*,\s*\(13', r'7500000),\n(13', text)

with open('src/main/resources/data.sql', 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed Employees insert syntax')
