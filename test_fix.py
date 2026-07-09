import requests, json

BASE = "http://localhost:8080/api"

# Test login
r = requests.post(f"{BASE}/user/login", json={"username": "admin", "password": "admin123"}, timeout=15)
print(f"Login: {r.status_code}")
data = r.json()
print(f"Code: {data.get('code')}")
token = data.get("data", {}).get("token", "")
print(f"Token len: {len(token)}")

if token:
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    
    # Test proposal list
    r = requests.get(f"{BASE}/proposal?size=3", headers=headers, timeout=15)
    print(f"Proposal list: {r.status_code}")
    
    # Test proposal create
    r = requests.post(f"{BASE}/proposal", headers=headers, json={
        "productId": 26, "applicantId": 2, "insuredId": 3,
        "totalSumInsured": 100000,
        "selectedCoverages": [{"coverageCode": "COV_DEATH", "coverageName": "death", "sumInsured": 100000}],
        "selectedPremiumPlan": {"premiumPlanCode": "PP_ANNUAL", "paymentFrequency": 12, "paymentTerm": 20, "paymentTermUnit": 1}
    }, timeout=15)
    print(f"Proposal create: {r.status_code}")
    if r.status_code == 200:
        d = r.json()
        print(f"  code={d.get('code')} id={d.get('data',{}).get('id')} status={d.get('data',{}).get('status')}")
    else:
        print(f"  Error: {r.text[:200]}")
