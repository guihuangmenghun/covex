import requests, json

BASE = "http://localhost:8080/api"

def login(username, password):
    r = requests.post(f"{BASE}/user/login", json={"username": username, "password": password})
    return r.json()["data"]["token"]

admin_token = login("admin", "admin123")
admin_headers = {"Authorization": f"Bearer {admin_token}"}

# Check proposal list
r = requests.get(f"{BASE}/proposal?size=5", headers=admin_headers)
print(f"Status: {r.status_code}")
print(f"Body: {r.text[:500]}")
