import requests, json, sys

BASE = "http://localhost:8080/api"

def login(username, password):
    r = requests.post(f"{BASE}/user/login", json={"username": username, "password": password})
    return r.json()["data"]["token"]

agent_token = login("agent", "test123")
agent_headers = {"Authorization": f"Bearer {agent_token}", "Content-Type": "application/json"}

# Check proposal 26
r = requests.get(f"{BASE}/proposal/26", headers=agent_headers)
print(f"Proposal 26 response: {r.status_code}")
print(f"Body: {r.text[:500]}")

# List proposals
r = requests.get(f"{BASE}/proposal?size=5", headers=agent_headers)
data = r.json()
print(f"\nProposal list: code={data['code']}, total={data.get('data', {}).get('total', 'N/A')}")
if data.get('data', {}).get('records'):
    for p in data['data']['records'][:3]:
        print(f"  ID={p['id']} no={p['proposalNo']} status={p['status']}")
