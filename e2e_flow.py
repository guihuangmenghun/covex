import requests, json, sys

BASE = "http://localhost:8080/api"

def login(username, password):
    r = requests.post(f"{BASE}/user/login", json={"username": username, "password": password})
    return r.json()["data"]["token"]

admin_token = login("admin", "admin123")
admin_headers = {"Authorization": f"Bearer {admin_token}", "Content-Type": "application/json"}

# 1. Check proposal status via list API
r = requests.get(f"{BASE}/proposal?size=50", headers=admin_headers)
data = r.json()
proposal = None
for p in data.get("data", {}).get("records", []):
    if p["id"] == 26:
        proposal = p
        break

if not proposal:
    print("ERROR: Proposal 26 not found")
    sys.exit(1)
print(f"1. Proposal 26 status: {proposal['status']} (4=待支付)")

# 2. Create payment
agent_token = login("agent", "test123")
agent_headers = {"Authorization": f"Bearer {agent_token}", "Content-Type": "application/json"}

r = requests.post(f"{BASE}/payment/create", json={
    "proposalId": 26,
    "payChannel": 1
}, headers=agent_headers)
result = r.json()
print(f"2. Create payment: code={result['code']}", end="")
if result['code'] != 200:
    print(f" ERROR: {result['message']}")
    sys.exit(1)
payment_no = result['data']['paymentNo']
print(f" OK, paymentNo={payment_no}")

# 3. Payment callback
r = requests.post(f"{BASE}/payment/callback", json={
    "paymentNo": payment_no,
    "success": True,
    "payChannelNo": "MOCK_PAY_001"
}, headers=agent_headers)
result = r.json()
print(f"3. Payment callback: code={result['code']}", end="")
if result['code'] != 200:
    print(f" ERROR: {result['message']}")
else:
    print(" OK")

# 4. Check final status
r = requests.get(f"{BASE}/proposal?size=50", headers=admin_headers)
data = r.json()
for p in data.get("data", {}).get("records", []):
    if p["id"] == 26:
        proposal = p
        break
print(f"4. Final proposal status: {proposal['status']} (6=已出单)")

# 5. Check policies
r = requests.get(f"{BASE}/policy?size=3", headers=admin_headers)
policies = r.json()["data"]
print(f"5. Total policies: {policies.get('total', 0)}")

if proposal['status'] == 6:
    print("\nE2E Flow SUCCESS!")
else:
    print(f"\nE2E Flow status={proposal['status']}")
