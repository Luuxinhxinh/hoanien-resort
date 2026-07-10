import re
with open('src/main/resources/data.sql', 'r', encoding='utf-8', errors='ignore') as f:
    text = f.read()

# Remove any weird invisible characters
text = text.replace('\uFEFF', '') # BOM
text = text.replace('\u200B', '') # ZWSP
# Find the first INSERT INTO Roles and ensure no weird chars before it
idx = text.find('INSERT INTO Roles')
if idx != -1:
    before = text[:idx]
    # Replace any non-ascii in 'before' with space
    before_clean = ''.join([c if ord(c) < 128 else ' ' for c in before])
    text = before_clean + text[idx:]

with open('src/main/resources/data.sql', 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed data.sql encoding/weird characters')
