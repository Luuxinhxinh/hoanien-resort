import re

filepath = 'd:/SWP391/su26-swp391-se2023-g2/05-Development/kawai-backend/src/main/java/com/kawai/controllers/api/PosApiController.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove class level PreAuthorize
content = re.sub(r'@org\.springframework\.security\.access\.prepost\.PreAuthorize\("hasAnyAuthority\(\'OP_FNB_ORDER\', \'ROLE_ADMIN\', \'ROLE_MANAGER\'\)"\)\s*public class PosApiController', 'public class PosApiController', content)

# Add PreAuthorize to all mapping methods except createOrder
# We can do this by finding all @*Mapping annotations
auth_str = '@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority(\'OP_FNB_ORDER\', \'ROLE_ADMIN\', \'ROLE_MANAGER\')")\n    '

def add_auth(match):
    mapping = match.group(0)
    if '("/orders")' in mapping and '@PostMapping' in mapping:
        return mapping # don't add to createOrder
    return auth_str + mapping.strip() + '\n'

# Find lines like @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
content = re.sub(r'^[ \t]*@(Get|Post|Put|Delete)Mapping.*$', add_auth, content, flags=re.MULTILINE)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated PosApiController")
