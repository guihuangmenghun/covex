import requests, json, sys, time

BASE = "http://localhost:8080/api"

def api(method, path, token=None, body=None, params=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if method == "GET":
        r = requests.get(f"{BASE}{path}", headers=headers, params=params, timeout=15)
    elif method == "POST":
        r = requests.post(f"{BASE}{path}", headers=headers, json=body, timeout=15)
    elif method == "PUT":
        r = requests.put(f"{BASE}{path}", headers=headers, json=body, timeout=15)
    return r

def login(u, p):
    r = api("POST", "/user/login", body={"username": u, "password": p})
    d = r.json()
    assert d.get("code") == 200, f"Login {u} failed: {d.get('message')}"
    return d["data"]["token"]

print("=== 链路2 E2E 测试 ===\n")

agent_token = login("agent", "test123")
print("1. Agent 登录 OK")

# 创建投保单
r = api("POST", "/proposal", token=agent_token, body={
    "productId": 26, "applicantId": 2, "insuredId": 3,
    "totalSumInsured": 100000,
    "selectedCoverages": [{"coverageCode": "COV_DEATH", "coverageName": "death", "sumInsured": 100000}],
    "selectedPremiumPlan": {"premiumPlanCode": "PP_ANNUAL", "paymentFrequency": 12, "paymentTerm": 20, "paymentTermUnit": 1}
})
d = r.json()
assert d["code"] == 200, f"Create proposal failed: {d.get('message')}"
pid = d["data"]["id"]
print(f"2. 创建投保单 OK, id={pid}")

# 提交
r = api("PUT", f"/proposal/{pid}/submit", token=agent_token)
d = r.json()
assert d["code"] == 200, f"Submit failed: {d.get('message')}"
print(f"3. 提交 OK, status={d['data']['status']}")

# 支付
r = api("POST", "/payment/create", token=agent_token, body={"proposalId": pid, "payChannel": 1})
d = r.json()
assert d["code"] == 200, f"Payment create failed: {d.get('message')}"
payment_no = d["data"]["paymentNo"]
print(f"4. 创建支付 OK, paymentNo={payment_no}")

# 支付回调
r = api("POST", "/payment/callback", token=agent_token, body={
    "paymentNo": payment_no, "success": True, "payChannelNo": "MOCK_PAY_001"
})
d = r.json()
assert d["code"] == 200, f"Payment callback failed: {d.get('message')}"
print(f"5. 支付回调 OK, payment status={d['data']['status']}")

# 等待 MQ 处理
time.sleep(5)

# 验证投保单状态
r = api("GET", f"/proposal/{pid}", token=agent_token)
d = r.json()
final_status = d["data"]["status"]
print(f"6. 投保单最终状态: {final_status} (期望: 6=已出单)")

# 验证保单
admin_token = login("admin", "admin123")
r = api("GET", "/policy?size=10", token=admin_token)
d = r.json()
records = d.get("data", {}).get("records", [])
found = None
for p in records:
    if p.get("proposalId") == pid:
        found = p
        break

if found:
    print(f"7. 保单验证 OK: policyNo={found['policyNo']}")
    print("\n=== 链路2 E2E 测试 PASSED ===")
else:
    print(f"7. 保单验证 FAIL: 未找到 proposalId={pid} 的保单")
    print("\n=== 链路2 E2E 测试 FAILED ===")
