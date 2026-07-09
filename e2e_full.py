import requests, json, sys

BASE = "http://localhost:8080/api"

def api(method, path, token=None, body=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if method == "GET":
        r = requests.get(f"{BASE}{path}", headers=headers, timeout=15)
    elif method == "POST":
        r = requests.post(f"{BASE}{path}", headers=headers, json=body, timeout=15)
    elif method == "PUT":
        r = requests.put(f"{BASE}{path}", headers=headers, json=body, timeout=15)
    return r.json()

def login(u, p):
    return api("POST", "/user/login", body={"username": u, "password": p})["data"]["token"]

# 1. Login as agent
print("1. Login as agent...")
agent_token = login("agent", "test123")
agent_h = {"Authorization": f"Bearer {agent_token}", "Content-Type": "application/json"}

# 2. Create proposal
print("2. Create proposal...")
proposal = api("POST", "/proposal", token=agent_token, body={
    "productId": 26,  # TS058TL01 (published)
    "applicantId": 2,
    "insuredId": 3,
    "totalSumInsured": 100000,
    "selectedCoverages": [{"coverageCode": "COV_DEATH", "coverageName": "身故保险金", "sumInsured": 100000}],
    "selectedPremiumPlan": {"premiumPlanCode": "PP_ANNUAL", "paymentFrequency": 12, "paymentTerm": 20, "paymentTermUnit": 1}
})
print(f"   code={proposal['code']}", end="")
if proposal["code"] != 200:
    print(f" ERROR: {proposal['message']}")
    sys.exit(1)
pid = proposal["data"]["id"]
print(f" OK, proposalId={pid}")

# 3. Submit for underwriting
print("3. Submit for underwriting...")
r = api("PUT", f"/proposal/{pid}/submit", token=agent_token)
print(f"   code={r['code']}, status={r.get('data', {}).get('status', 'N/A')}")

# 4. Login as underwriter and approve
print("4. Underwriter approve...")
uw_token = login("underwriter", "test123")
r = api("POST", f"/underwriting/manual/{pid}", token=uw_token, body={
    "uwResult": 1, "comment": "E2E test pass"
})
print(f"   code={r['code']}")

# 5. Check proposal status
print("5. Check proposal status...")
admin_token = login("admin", "admin123")
admin_h = {"Authorization": f"Bearer {admin_token}", "Content-Type": "application/json"}
r = requests.get(f"{BASE}/proposal?size=50", headers=admin_h, timeout=15)
data = r.json()
for p in data.get("data", {}).get("records", []):
    if p["id"] == pid:
        print(f"   Proposal {pid}: status={p['status']}")
        break

# 6. Create payment
print("6. Create payment...")
r = api("POST", "/payment/create", token=agent_token, body={"proposalId": pid, "payChannel": 1})
print(f"   code={r['code']}", end="")
if r["code"] != 200:
    print(f" ERROR: {r['message']}")
else:
    payment_no = r["data"]["paymentNo"]
    print(f" OK, paymentNo={payment_no}")

    # 7. Payment callback
    print("7. Payment callback...")
    r = api("POST", "/payment/callback", token=agent_token, body={
        "paymentNo": payment_no, "success": True, "payChannelNo": "MOCK_PAY_001"
    })
    print(f"   code={r['code']}", end="")
    if r["code"] != 200:
        print(f" ERROR: {r['message']}")
    else:
        print(f" OK, payment status={r['data']['status']}")

        # 8. Check final status
        import time
        time.sleep(3)  # Wait for MQ processing
        r = requests.get(f"{BASE}/proposal?size=50", headers=admin_h, timeout=15)
        data = r.json()
        for p in data.get("data", {}).get("records", []):
            if p["id"] == pid:
                print(f"8. Final proposal status: {p['status']} (6=已出单)")
                break

        # 9. Check policies
        r = requests.get(f"{BASE}/policy?size=5", headers=admin_h, timeout=15)
        policies = r.json()
        print(f"9. Total policies: {policies.get('data', {}).get('total', 0)}")

print("\n=== E2E Flow Test Complete ===")
