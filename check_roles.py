import sys, requests
sys.stdout.reconfigure(encoding='utf-8')
r = requests.post('http://localhost:8080/api/user/login', json={'username': 'admin', 'password': 'admin123'})
token = r.json()['data']['token']
r2 = requests.get('http://localhost:8080/api/role', headers={'Authorization': f'Bearer {token}'})
for role in r2.json()['data']:
    print(f"{role['id']:3} {role['roleCode']:20} {role.get('roleName',''):10} {role.get('description','')}")
