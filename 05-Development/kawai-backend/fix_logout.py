import glob
import re

files = glob.glob('d:/SWP391/su26-swp391-se2023-g2/05-Development/kawai-backend/src/main/resources/templates/receptionist/*.html')
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Replace exactly <button class="logout-btn"> with <button class="logout-btn" onclick="window.location.href='/logout'">
    # Make sure we don't duplicate it if it already has onclick
    new_content = re.sub(r'<button class="logout-btn">', r'<button class="logout-btn" onclick="window.location.href=\'/logout\'">', content)
    
    if new_content != content:
        with open(f, 'w', encoding='utf-8') as file:
            file.write(new_content)
        print(f"Fixed {f}")
print("Done")
