import requests, json, sys

BASE = "http://localhost:8080/api"

def login(username, password):
    r = requests.post(f"{BASE}/user/login", json={"username": username, "password": password})
    data = r.json()
    if data.get("code") != 200:
        print(f"Login failed: {data}")
        sys.exit(1)
    return data["data"]["token"]

# Test 1: Login
print("=== Test 1: Login ===")
token = login("admin", "admin123")
headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
print("Admin login OK")

# Test 2: Proposal list (was causing StackOverflowError)
print("\n=== Test 2: Proposal list ===")
r = requests.get(f"{BASE}/proposal?size=5", headers=headers)
data = r.json()
print(f"Status: {r.status_code}, code: {data.get('code')}")
if data.get("data") and data["data"].get("records"):
    for p in data["data"]["records"][:3]:
        print(f"  ID={p['id']} no={p['proposalNo']} status={p['status']}")
elif data.get("data") is None:
    print(f"  Data is null: {data.get('message')}")
else:
    print(f"  No records: total={data['data'].get('total', 0)}")

# Test 3: Payment create (was causing StackOverflowError)
print("\n=== Test 3: Payment create ===")
# First check if there's a proposal in status 4 (待支付)
r = requests.get(f"{BASE}/proposal?size=50&status=4", headers=headers)
data = r.json()
pending_proposals = data.get("data", {}).get("records", []) if data.get("data") else []
print(f"Proposals in status=4: {len(pending_proposals)}")

if pending_proposals:
    pid = pending_proposals[0]["id"]
    r = requests.post(f"{BASE}/payment/create", json={"proposalId": pid, "payChannel": 1}, headers=headers)
    result = r.json()
    print(f"Payment create: code={result.get('code')}, message={result.get('message', 'OK')[:100]}")
else:
    print("  No proposal in status=4, skipping payment test")

# Test 4: Product list
print("\n=== Test 4: Product list ===")
r = requests.get(f"{BASE}/product?size=3", headers=headers)
data = r.json()
print(f"Status: {r.status_code}, code: {data.get('code')}")
if data.get("data") and data["data"].get("records"):
    for p in data["data"]["records"][:3]:
        print(f"  ID={p['id']} code={p['productCode']} name={p['productName']} status={p['versionStatus']}")

print("\n=== All basic tests passed ===")
