import re
import glob

def resolve_html_conflict(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Pattern to find git conflict markers
    # It captures the HEAD block and the remote block
    # We want to KEEP ONLY the remote block because it contains the new 'Duyệt yêu cầu' menu 
    # AND the 'Báo cáo Doanh thu' menu which was already there in HEAD.
    pattern = re.compile(r'<<<<<<< HEAD\n.*?\n=======\n(.*?)\n>>>>>>> [a-f0-9]+', re.DOTALL)
    
    def replacer(match):
        remote_content = match.group(1)
        return remote_content

    resolved = pattern.sub(replacer, content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(resolved)

files = glob.glob('d:/SWP391/su26-swp391-se2023-g2/05-Development/kawai-backend/src/main/resources/templates/manager/*.html')
for f in files:
    resolve_html_conflict(f)
print("Resolved HTML conflicts")
