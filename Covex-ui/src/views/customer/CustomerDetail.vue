<template>
  <div>
    <el-page-header @back="$router.push('/customer')" :title="isEdit ? '编辑客户' : '客户详情'" />

    <el-card style="margin-top: 20px" v-loading="loading">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- Tab 1: 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border v-if="!isEdit">
            <el-descriptions-item label="客户编号">{{ customer.customerCode }}</el-descriptions-item>
            <el-descriptions-item label="客户姓名">{{ customer.customerName }}</el-descriptions-item>
            <el-descriptions-item label="证件类型">{{ idTypeLabel(customer.idType) }}</el-descriptions-item>
            <el-descriptions-item label="证件号">{{ customer.idNo }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ customer.gender === 1 ? '男' : customer.gender === 2 ? '女' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ customer.birthDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="国籍">{{ customer.nationality || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ customer.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ customer.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="客户类型">{{ customer.customerType === 1 ? '个人' : customer.customerType === 2 ? '团体' : '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-form v-else ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px" style="max-width: 600px; margin-top: 16px">
            <el-form-item label="客户姓名" prop="customerName">
              <el-input v-model="editForm.customerName" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" />
            </el-form-item>
            <el-form-item label="国籍">
              <el-input v-model="editForm.nationality" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">保存</el-button>
              <el-button @click="isEdit = false">取消</el-button>
            </el-form-item>
          </el-form>

          <div v-if="!isEdit" style="margin-top: 20px; display: flex; gap: 8px">
            <el-button type="primary" @click="startEdit">编辑</el-button>
            <el-button type="success" @click="handleEnsureApplicant">设为投保人</el-button>
            <el-button type="warning" @click="handleEnsureInsured">设为被保人</el-button>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 联系地址 -->
        <el-tab-pane label="联系地址" name="address">
          <div style="display: flex; justify-content: flex-end; margin-bottom: 16px">
            <el-button type="primary" :icon="Plus" @click="openAddressDialog()">新增地址</el-button>
          </div>
          <el-table :data="addresses" stripe border v-loading="addressLoading">
            <el-table-column prop="addressType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ addressTypeLabel(row.addressType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="地址" min-width="300">
              <template #default="{ row }">{{ row.province }}{{ row.city }}{{ row.district }}{{ row.detail }}</template>
            </el-table-column>
            <el-table-column prop="zipCode" label="邮编" width="80">
              <template #default="{ row }">{{ row.zipCode || '-' }}</template>
            </el-table-column>
            <el-table-column prop="isDefault" label="默认" width="70" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault === 1" type="success" size="small">默认</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="openAddressDialog(row)">编辑</el-button>
                <el-button size="small" type="success" link @click="handleSetDefaultAddress(row)" :disabled="row.isDefault === 1">设为默认</el-button>
                <el-button size="small" type="danger" link @click="handleDeleteAddress(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 3: 银行账户 -->
        <el-tab-pane label="银行账户" name="bank">
          <el-alert type="info" :closable="false" style="margin-bottom: 16px">
            有代扣协议关联的账户不允许删除，请先解除代扣协议。
          </el-alert>
          <div style="display: flex; justify-content: flex-end; margin-bottom: 16px">
            <el-button type="primary" :icon="Plus" @click="openBankDialog()">新增账户</el-button>
          </div>
          <el-table :data="bankAccounts" stripe border v-loading="bankLoading">
            <el-table-column prop="bankName" label="银行名称" width="150" />
            <el-table-column prop="accountNo" label="银行账号" width="200">
              <template #default="{ row }">{{ maskAccountNo(row.accountNo) }}</template>
            </el-table-column>
            <el-table-column prop="branchName" label="开户行" min-width="180">
              <template #default="{ row }">{{ row.branchName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="usageType" label="用途" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ usageTypeLabel(row.usageType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="accountHolder" label="户名" width="100" />
            <el-table-column prop="isDefault" label="默认" width="70" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault === 1" type="success" size="small">默认</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="openBankDialog(row)">编辑</el-button>
                <el-button size="small" type="success" link @click="handleSetDefaultBank(row)" :disabled="row.isDefault === 1">设为默认</el-button>
                <el-button size="small" type="danger" link @click="handleDeleteBank(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 4: 健康档案 -->
        <el-tab-pane label="健康档案" name="health">
          <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
            健康档案需要客户先被设为被保人后才能填写。
          </el-alert>

          <el-form label-width="120px" style="max-width: 800px">
            <el-divider content-position="left">既往病史</el-divider>
            <div v-for="(item, idx) in healthForm.medicalHistory" :key="'med-' + idx" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
              <el-input v-model="item.disease" placeholder="疾病名称" style="width: 200px" />
              <el-date-picker v-model="item.diagnosedDate" type="date" placeholder="诊断日期" value-format="YYYY-MM-DD" style="width: 160px" />
              <el-select v-model="item.treatmentStatus" placeholder="治疗状态" style="width: 140px">
                <el-option label="治疗中" value="treating" />
                <el-option label="已治愈" value="cured" />
                <el-option label="慢性" value="chronic" />
              </el-select>
              <el-button type="danger" :icon="Delete" circle size="small" @click="healthForm.medicalHistory.splice(idx, 1)" />
            </div>
            <el-button type="primary" link @click="healthForm.medicalHistory.push({ disease: '', diagnosedDate: '', treatmentStatus: '' })">
              + 添加既往病史
            </el-button>

            <el-divider content-position="left">家族病史</el-divider>
            <div v-for="(item, idx) in healthForm.familyHistory" :key="'fam-' + idx" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
              <el-select v-model="item.relationship" placeholder="亲属关系" style="width: 140px">
                <el-option label="父亲" value="father" />
                <el-option label="母亲" value="mother" />
                <el-option label="兄弟" value="brother" />
                <el-option label="姐妹" value="sister" />
                <el-option label="祖父母" value="grandparent" />
              </el-select>
              <el-input v-model="item.disease" placeholder="疾病名称" style="width: 200px" />
              <el-input-number v-model="item.onsetAge" placeholder="发病年龄" :min="0" :max="120" style="width: 140px" />
              <el-button type="danger" :icon="Delete" circle size="small" @click="healthForm.familyHistory.splice(idx, 1)" />
            </div>
            <el-button type="primary" link @click="healthForm.familyHistory.push({ relationship: '', disease: '', onsetAge: null })">
              + 添加家族病史
            </el-button>

            <el-divider content-position="left">当前用药</el-divider>
            <div v-for="(item, idx) in healthForm.currentMedications" :key="'cur-' + idx" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
              <el-input v-model="item.drugName" placeholder="药品名称" style="width: 160px" />
              <el-input v-model="item.dosage" placeholder="剂量" style="width: 100px" />
              <el-input v-model="item.frequency" placeholder="频率" style="width: 120px" />
              <el-date-picker v-model="item.startDate" type="date" placeholder="开始时间" value-format="YYYY-MM-DD" style="width: 160px" />
              <el-button type="danger" :icon="Delete" circle size="small" @click="healthForm.currentMedications.splice(idx, 1)" />
            </div>
            <el-button type="primary" link @click="healthForm.currentMedications.push({ drugName: '', dosage: '', frequency: '', startDate: '' })">
              + 添加当前用药
            </el-button>

            <div style="margin-top: 24px">
              <el-button type="primary" :loading="healthLoading" @click="handleSaveHealth">保存健康档案</el-button>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 地址弹窗 -->
    <el-dialog v-model="addressDialogVisible" :title="editingAddress ? '编辑地址' : '新增地址'" width="500px" destroy-on-close>
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="90px">
        <el-form-item label="地址类型" prop="addressType">
          <el-select v-model="addressForm.addressType" placeholder="选择类型" style="width: 100%">
            <el-option label="通讯地址" :value="1" />
            <el-option label="账单地址" :value="2" />
            <el-option label="居住地址" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="省市区" prop="region">
          <el-cascader
            v-model="selectedRegion"
            :options="regionData"
            placeholder="选择省市区"
            style="width: 100%"
            clearable
            filterable
            @change="handleRegionChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="addressForm.zipCode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSubmitLoading" @click="handleAddressSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 银行账户弹窗 -->
    <el-dialog v-model="bankDialogVisible" :title="editingBank ? '编辑银行账户' : '新增银行账户'" width="500px" destroy-on-close>
      <el-form ref="bankFormRef" :model="bankForm" :rules="bankRules" label-width="90px">
        <el-form-item label="银行名称" prop="bankName">
          <el-input v-model="bankForm.bankName" />
        </el-form-item>
        <el-form-item label="银行账号" prop="accountNo">
          <el-input v-model="bankForm.accountNo" />
        </el-form-item>
        <el-form-item label="开户行">
          <el-input v-model="bankForm.branchName" />
        </el-form-item>
        <el-form-item label="户名" prop="accountHolder">
          <el-input v-model="bankForm.accountHolder" />
        </el-form-item>
        <el-form-item label="用途类型" prop="usageType">
          <el-select v-model="bankForm.usageType" placeholder="选择用途" style="width: 100%">
            <el-option label="保费扣款" :value="1" />
            <el-option label="理赔收款" :value="2" />
            <el-option label="佣金收款" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bankDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bankSubmitLoading" @click="handleBankSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getCustomerById, updateCustomer, ensureApplicant, ensureInsured, getHealth, updateHealth,
  createAddress, getAddressList, updateAddress, deleteAddress, setDefaultAddress,
  createBankAccount, getBankAccountList, updateBankAccount, deleteBankAccount, setDefaultBankAccount,
} from '@/api/customer'
import type { Customer, CustomerAddress, CustomerBankAccount } from '@/types'
import { useDictStore } from '@/stores/dict'
import { regionData, codeToText } from 'element-china-area-data'

const route = useRoute()
const customerId = computed(() => Number(route.params.id))

// ====== 基本信息 ======
const loading = ref(false)
const customer = ref<Customer>({} as Customer)
const activeTab = ref('info')
const isEdit = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = ref({ customerName: '', phone: '', email: '', nationality: '' })
const editRules: FormRules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

function idTypeLabel(type: number): string {
  return useDictStore().getDictLabel('id_type', String(type))
}

function addressTypeLabel(type: number): string {
  return useDictStore().getDictLabel('address_type', String(type))
}

function usageTypeLabel(type: number): string {
  return useDictStore().getDictLabel('account_usage_type', String(type))
}

function maskAccountNo(accountNo: string | null): string {
  if (!accountNo || accountNo.length < 8) return accountNo || '-'
  return accountNo.slice(0, 4) + '****' + accountNo.slice(-4)
}

async function loadCustomer() {
  loading.value = true
  try {
    const res = await getCustomerById(customerId.value)
    customer.value = res.data || ({} as Customer)
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function startEdit() {
  editForm.value = {
    customerName: customer.value.customerName,
    phone: customer.value.phone,
    email: customer.value.email || '',
    nationality: customer.value.nationality || '',
  }
  isEdit.value = true
}

async function handleEditSubmit() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    await updateCustomer(customerId.value, editForm.value)
    ElMessage.success('更新成功')
    isEdit.value = false
    await loadCustomer()
  } catch { /* handled */ } finally {
    editLoading.value = false
  }
}

async function handleEnsureApplicant() {
  try {
    await ElMessageBox.confirm('确定将此客户设为投保人吗？', '确认')
    await ensureApplicant(customerId.value)
    ElMessage.success('已设为投保人')
    await loadCustomer()
  } catch { /* cancel */ }
}

async function handleEnsureInsured() {
  try {
    await ElMessageBox.confirm('确定将此客户设为被保人吗？', '确认')
    await ensureInsured(customerId.value)
    ElMessage.success('已设为被保人')
    await loadCustomer()
  } catch { /* cancel */ }
}

// ====== 地址 ======
const addresses = ref<CustomerAddress[]>([])
const addressLoading = ref(false)
const addressDialogVisible = ref(false)
const addressSubmitLoading = ref(false)
const addressFormRef = ref<FormInstance>()
const editingAddress = ref<CustomerAddress | null>(null)
const addressForm = ref({ addressType: 1, province: '', city: '', district: '', detail: '', zipCode: '' })
const selectedRegion = ref<string[]>([])
const addressRules: FormRules = {
  addressType: [{ required: true, message: '请选择地址类型', trigger: 'change' }],
  region: [{ required: true, message: '请选择省市区', trigger: 'change', type: 'array', min: 3 }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

function handleRegionChange(val: string[] | null) {
  if (val && val.length >= 3) {
    addressForm.value.province = codeToText[val[0]] || ''
    addressForm.value.city = codeToText[val[1]] || ''
    addressForm.value.district = codeToText[val[2]] || ''
    ;(addressForm.value as any).region = val
  } else {
    addressForm.value.province = ''
    addressForm.value.city = ''
    addressForm.value.district = ''
    ;(addressForm.value as any).region = []
  }
}

/** 省市区文本转代码（反向查找） */
function textToRegionCodes(province: string, city: string, district: string): string[] {
  for (const prov of regionData) {
    if (prov.label === province) {
      for (const c of prov.children || []) {
        if (c.label === city) {
          for (const d of c.children || []) {
            if (d.label === district) {
              return [prov.value, c.value, d.value]
            }
          }
        }
      }
    }
  }
  return []
}

async function loadAddresses() {
  addressLoading.value = true
  try {
    const res = await getAddressList(customerId.value)
    addresses.value = res.data || []
  } catch { addresses.value = [] } finally {
    addressLoading.value = false
  }
}

function openAddressDialog(row?: CustomerAddress) {
  if (row) {
    editingAddress.value = row
    addressForm.value = { addressType: row.addressType, province: row.province, city: row.city, district: row.district, detail: row.detail, zipCode: row.zipCode || '' }
    selectedRegion.value = textToRegionCodes(row.province, row.city, row.district)
    ;(addressForm.value as any).region = selectedRegion.value
  } else {
    editingAddress.value = null
    addressForm.value = { addressType: 1, province: '', city: '', district: '', detail: '', zipCode: '' }
    selectedRegion.value = []
    ;(addressForm.value as any).region = []
  }
  addressDialogVisible.value = true
}

async function handleAddressSubmit() {
  const valid = await addressFormRef.value?.validate().catch(() => false)
  if (!valid) return
  addressSubmitLoading.value = true
  try {
    if (editingAddress.value) {
      await updateAddress(customerId.value, editingAddress.value.id, addressForm.value)
      ElMessage.success('更新成功')
    } else {
      await createAddress(customerId.value, addressForm.value)
      ElMessage.success('创建成功')
    }
    addressDialogVisible.value = false
    await loadAddresses()
  } catch { /* handled */ } finally {
    addressSubmitLoading.value = false
  }
}

async function handleSetDefaultAddress(row: CustomerAddress) {
  try {
    await setDefaultAddress(customerId.value, row.id)
    ElMessage.success('已设为默认地址')
    await loadAddresses()
  } catch { /* handled */ }
}

async function handleDeleteAddress(row: CustomerAddress) {
  try {
    await ElMessageBox.confirm('确定删除此地址吗？', '确认删除', { type: 'warning' })
    await deleteAddress(customerId.value, row.id)
    ElMessage.success('删除成功')
    await loadAddresses()
  } catch { /* cancel */ }
}

// ====== 银行账户 ======
const bankAccounts = ref<CustomerBankAccount[]>([])
const bankLoading = ref(false)
const bankDialogVisible = ref(false)
const bankSubmitLoading = ref(false)
const bankFormRef = ref<FormInstance>()
const editingBank = ref<CustomerBankAccount | null>(null)
const bankForm = ref({ bankName: '', accountNo: '', branchName: '', accountHolder: '', usageType: 1 })
const bankRules: FormRules = {
  bankName: [{ required: true, message: '请输入银行名称', trigger: 'blur' }],
  accountNo: [{ required: true, message: '请输入银行账号', trigger: 'blur' }],
  accountHolder: [{ required: true, message: '请输入户名', trigger: 'blur' }],
  usageType: [{ required: true, message: '请选择用途类型', trigger: 'change' }],
}

async function loadBankAccounts() {
  bankLoading.value = true
  try {
    const res = await getBankAccountList(customerId.value)
    bankAccounts.value = res.data || []
  } catch { bankAccounts.value = [] } finally {
    bankLoading.value = false
  }
}

function openBankDialog(row?: CustomerBankAccount) {
  if (row) {
    editingBank.value = row
    bankForm.value = { bankName: row.bankName, accountNo: row.accountNo, branchName: row.branchName || '', accountHolder: row.accountHolder, usageType: row.usageType }
  } else {
    editingBank.value = null
    bankForm.value = { bankName: '', accountNo: '', branchName: '', accountHolder: '', usageType: 1 }
  }
  bankDialogVisible.value = true
}

async function handleBankSubmit() {
  const valid = await bankFormRef.value?.validate().catch(() => false)
  if (!valid) return
  bankSubmitLoading.value = true
  try {
    if (editingBank.value) {
      await updateBankAccount(customerId.value, editingBank.value.id, bankForm.value)
      ElMessage.success('更新成功')
    } else {
      await createBankAccount(customerId.value, bankForm.value)
      ElMessage.success('创建成功')
    }
    bankDialogVisible.value = false
    await loadBankAccounts()
  } catch { /* handled */ } finally {
    bankSubmitLoading.value = false
  }
}

async function handleSetDefaultBank(row: CustomerBankAccount) {
  try {
    await setDefaultBankAccount(customerId.value, row.id)
    ElMessage.success('已设为默认账户')
    await loadBankAccounts()
  } catch { /* handled */ }
}

async function handleDeleteBank(row: CustomerBankAccount) {
  try {
    await ElMessageBox.confirm('确定删除此银行账户吗？如有代扣协议请先解除。', '确认删除', { type: 'warning' })
    await deleteBankAccount(customerId.value, row.id)
    ElMessage.success('删除成功')
    await loadBankAccounts()
  } catch { /* cancel */ }
}

// ====== 健康档案 ======
const healthLoading = ref(false)
const healthForm = reactive({
  medicalHistory: [] as { disease: string; diagnosedDate: string; treatmentStatus: string }[],
  familyHistory: [] as { relationship: string; disease: string; onsetAge: number | null }[],
  currentMedications: [] as { drugName: string; dosage: string; frequency: string; startDate: string }[],
})

async function loadHealth() {
  try {
    const res = await getHealth(customerId.value)
    if (res.data) {
      healthForm.medicalHistory = Array.isArray(res.data.medicalHistory) ? res.data.medicalHistory : []
      healthForm.familyHistory = Array.isArray(res.data.familyHistory) ? res.data.familyHistory : []
      healthForm.currentMedications = Array.isArray(res.data.currentMedications) ? res.data.currentMedications : []
    }
  } catch { /* 无健康档案数据时静默失败 */ }
}

async function handleSaveHealth() {
  healthLoading.value = true
  try {
    await updateHealth(customerId.value, {
      medicalHistory: healthForm.medicalHistory,
      familyHistory: healthForm.familyHistory,
      currentMedications: healthForm.currentMedications,
    })
    ElMessage.success('健康档案保存成功')
  } catch { /* handled */ } finally {
    healthLoading.value = false
  }
}

// ====== Tab 切换懒加载 ======
function handleTabChange(tab: string) {
  if (tab === 'address' && addresses.value.length === 0) loadAddresses()
  if (tab === 'bank' && bankAccounts.value.length === 0) loadBankAccounts()
  if (tab === 'health') loadHealth()
}

// ====== 初始化 ======
onMounted(async () => {
  await loadCustomer()
  if (route.query.edit === 'true') startEdit()
})
</script>
