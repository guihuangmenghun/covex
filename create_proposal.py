import requests, json, sys

BASE = "http://localhost:8080/api"

# Login as agent
r = requests.post(f"{BASE}/user/login", json={"username": "agent", "password": "test123"})
token = r.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
print(f"Agent logged in, token: {token[:30]}...")

# Create proposal
proposal_data = {
    "productId": 29,
    "applicantId": 2,
    "insuredId": 2,
    "totalSumInsured": 100000,
    "selectedCoverages": [
        {"coverageCode": "COV_CT", "coverageName": "车损险", "sumInsured": 100000}
    ],
    "selectedPremiumPlan": {"premiumPlanCode": "PP_MONTHLY", "paymentFrequency": 12, "paymentTerm": 20, "paymentTermUnit": 1}
}

r = requests.post(f"{BASE}/proposal", json=proposal_data, headers=headers)
result = r.json()
print(f"Create proposal: code={result['code']}")
if result['code'] == 200:
    proposal = result['data']
    print(f"  Proposal ID: {proposal['id']}")
    print(f"  Proposal No: {proposal['proposalNo']}")
    print(f"  Status: {proposal['status']}")
    print(f"  Product ID: {proposal['productId']}")
else:
    print(f"  Error: {result['message']}")
    sys.exit(1)
