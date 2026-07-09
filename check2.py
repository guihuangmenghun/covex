import requests, json

BASE = "http://localhost:8080/api"
r = requests.post(f"{BASE}/user/login", json={"username": "admin", "password": "admin123"})
token = r.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}"}

# List proposals
r = requests.get(f"{BASE}/proposal?size=3", headers=headers)
print(f"List: {r.status_code}")
data = r.json()
print(json.dumps(data, ensure_ascii=False, indent=2)[:1000])
