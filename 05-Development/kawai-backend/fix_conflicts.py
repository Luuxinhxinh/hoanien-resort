import re

def resolve_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Pattern to find git conflict markers
    # It captures the HEAD block and the remote block
    pattern = re.compile(r'<<<<<<< HEAD\n(.*?)\n=======\n(.*?)\n>>>>>>> [a-f0-9]+', re.DOTALL)
    
    def replacer(match):
        head_content = match.group(1)
        remote_content = match.group(2)
        # Keep both! We'll put remote first, then head, or vice versa depending on context.
        # For FolioRestController, remote changed loyalty formula, head added paymentTransactionRepository.
        # Actually it's safer to just combine them.
        return head_content + "\n" + remote_content

    resolved = pattern.sub(replacer, content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(resolved)

resolve_file('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\java\\com\\kawai\\controllers\\api\\FolioRestController.java')
resolve_file('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql')
