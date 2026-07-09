import subprocess, sys

sql = """
SET NAMES utf8mb4;
SELECT coverage_code, coverage_name FROM ins_product_coverage 
WHERE coverage_code IN ('COV_CT','COV_VD','COV_ACC_DEATH','COV_DEATH','COV_CI','COV_ACC_MED') 
GROUP BY coverage_code ORDER BY coverage_code;
"""

r = subprocess.run(
    ['D:/mysql-8.4.9/bin/mysql.exe', '-u', 'root', '-pguihuang50', 'covex', '--default-character-set=utf8mb4', '-e', sql],
    capture_output=True, text=True, encoding='utf-8'
)
print(r.stdout)
if r.stderr:
    print("STDERR:", r.stderr, file=sys.stderr)
