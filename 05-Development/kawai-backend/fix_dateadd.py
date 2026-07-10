import re
with open('src/main/resources/data.sql', 'r', encoding='utf-8') as f:
    text = f.read()

# Replace DATEADD(DAY, X, CURRENT_DATE) with DATE_ADD(CURRENT_DATE, INTERVAL X DAY)
text = re.sub(r'DATEADD\(DAY,\s*(-?\d+),\s*CURRENT_DATE\)', r'DATE_ADD(CURRENT_DATE, INTERVAL \1 DAY)', text)

with open('src/main/resources/data.sql', 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed DATEADD')
