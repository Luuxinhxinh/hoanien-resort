import glob

files = glob.glob('d:/SWP391/su26-swp391-se2023-g2/05-Development/kawai-backend/src/main/resources/templates/receptionist/*.html')
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
        if "logout" in content.lower() and "logout-btn" not in content.lower():
            pass # just want to see where it redirects
        # find all lines with logout
        lines = content.split('\n')
        for i, line in enumerate(lines):
            if 'logout' in line.lower():
                print(f"{f}:{i+1} {line.strip()}")
