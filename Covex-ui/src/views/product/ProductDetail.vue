<template>
  <div>
    <el-page-header @back="$router.push('/product')" :title="isCreate ? '创建产品' : '产品详情'" />

    <!-- 创建模式 -->
    <el-card v-if="isCreate" style="margin-top: 20px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px" style="max-width: 600px">
        <el-form-item label="产品编码" prop="productCode">
          <el-input v-model="createForm.productCode" placeholder="如 LIFE_TERM_001" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="createForm.productName" />
        </el-form-item>
        <el-form-item label="简称">
          <el-input v-model="createForm.shortName" />
        </el-form-item>
        <el-form-item label="产品类型" prop="productType">
          <el-select v-model="createForm.productType" style="width: 100%">
            <el-option label="寿险" :value="1" />
            <el-option label="意外险" :value="2" />
            <el-option label="健康险" :value="3" />
            <el-option label="车险" :value="4" />
            <el-option label="财产险" :value="5" />
            <el-option label="责任险" :value="6" />
            <el-option label="乘务险" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="createForm.version" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="createLoading" @click="handleCreate">创建</el-button>
          <el-button @click="$router.push('/product')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 详情模式 -->
    <template v-else>
      <!-- 基本信息卡片 -->
      <el-card style="margin-top: 20px" v-loading="productLoading">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="产品编码">{{ product.productCode }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ product.productName }}</el-descriptions-item>
          <el-descriptions-item label="简称">{{ product.shortName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品类型">
            <el-tag :type="typeTagMap[product.productType]?.type || 'info'" size="small">{{ typeTagMap[product.productType]?.label || '未知' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="版本号">{{ product.version }}</el-descriptions-item>
          <el-descriptions-item label="版本状态">
            <el-tag :type="vsMap[product.versionStatus]?.type || 'info'" size="small">{{ vsMap[product.versionStatus]?.label || '未知' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="product.status === 1 ? 'success' : 'info'" size="small">{{ product.status === 1 ? '启用' : '停用' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ product.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ product.updatedAt }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 16px; display: flex; gap: 8px">
          <el-button v-if="product.versionStatus === 0 || product.versionStatus === 3" type="primary" @click="openEditDialog">编辑</el-button>
          <el-button v-if="product.versionStatus === 0" type="success" @click="handlePublish">发布</el-button>
          <el-button v-if="product.versionStatus === 1" type="warning" @click="handleFreeze">冻结</el-button>
          <el-button type="info" @click="handleClone">克隆</el-button>
        </div>
      </el-card>

      <!-- 7 个 Tab -->
      <el-card style="margin-top: 16px">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- Tab 1: 基本信息编辑 -->
          <el-tab-pane label="基本信息" name="info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="产品性质">{{ product.productNature ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="期限类型">{{ product.termType ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="主附险标志">{{ product.mainRiderFlag ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="销售日期">{{ product.startDate || '-' }} ~ {{ product.endDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="能力声明" :span="2">{{ product.capabilities ? JSON.stringify(product.capabilities) : '-' }}</el-descriptions-item>
              <el-descriptions-item label="扩展属性" :span="2">{{ product.attributes ? JSON.stringify(product.attributes) : '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- Tab 2: 保障定义 -->
          <el-tab-pane label="保障定义" name="coverage">
            <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
              <el-button type="primary" :icon="Plus" size="small" @click="openCoverageDialog()">新增保障</el-button>
            </div>
            <el-table :data="coverages" stripe border v-loading="coverageLoading" size="small">
              <el-table-column prop="coverageCode" label="编码" width="120" />
              <el-table-column prop="coverageName" label="名称" min-width="140" />
              <el-table-column prop="selectionMode" label="选择模式" width="90" align="center">
                <template #default="{ row }">{{ row.selectionMode ?? '-' }}</template>
              </el-table-column>
              <el-table-column prop="benefitType" label="给付类型" width="90" align="center">
                <template #default="{ row }">{{ row.benefitType ?? '-' }}</template>
              </el-table-column>
              <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
              <el-table-column label="操作" width="200" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="openCoverageDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteCoverage(row)">删除</el-button>
                  <el-button size="small" type="info" link @click="viewCoveragePremiums(row)">关联缴费</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Tab 3: 缴费计划 -->
          <el-tab-pane label="缴费计划" name="premium">
            <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
              <el-button type="primary" :icon="Plus" size="small" @click="openPremiumDialog()">新增缴费计划</el-button>
            </div>
            <el-table :data="premiums" stripe border v-loading="premiumLoading" size="small">
              <el-table-column prop="premiumPlanCode" label="计划编码" width="130" />
              <el-table-column prop="premiumPlanName" label="计划名称" min-width="140" />
              <el-table-column prop="paymentFrequency" label="缴费频率" width="90" align="center" />
              <el-table-column prop="paymentTerm" label="缴费期限" width="90" align="center" />
              <el-table-column prop="paymentTermUnit" label="期限单位" width="90" align="center" />
              <el-table-column prop="gracePeriod" label="宽限天数" width="90" align="center" />
              <el-table-column label="操作" width="140" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="openPremiumDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" link @click="handleDeletePremium(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Tab 4: 规则引用 -->
          <el-tab-pane label="规则引用" name="rule">
            <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
              <el-button type="primary" :icon="Plus" size="small" @click="openRuleDialog()">新增规则</el-button>
            </div>
            <el-table :data="rules" stripe border v-loading="ruleLoading" size="small">
              <el-table-column prop="ruleCode" label="规则编码" width="150" />
              <el-table-column prop="ruleName" label="规则名称" min-width="160" />
              <el-table-column prop="ruleEngine" label="引擎类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.ruleEngine === 'liteflow' ? 'success' : row.ruleEngine === 'aviator' ? 'warning' : 'info'">{{ row.ruleEngine }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="ruleType" label="规则类型" width="90" align="center" />
              <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
              <el-table-column prop="isActive" label="状态" width="70" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">{{ row.isActive === 1 ? '启用' : '停用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="openRuleDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteRule(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Tab 5: 条款文档 -->
          <el-tab-pane label="条款文档" name="document">
            <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
              <el-button type="primary" :icon="Plus" size="small" @click="openDocDialog()">新增文档</el-button>
            </div>
            <el-table :data="documents" stripe border v-loading="docLoading" size="small">
              <el-table-column prop="documentName" label="文档名称" min-width="160" />
              <el-table-column prop="documentType" label="文档类型" width="100" align="center" />
              <el-table-column prop="fileUrl" label="文件路径" min-width="200" />
              <el-table-column prop="version" label="版本" width="80" align="center" />
              <el-table-column prop="effectiveDate" label="生效日期" width="120">
                <template #default="{ row }">{{ row.effectiveDate || '-' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="140" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="openDocDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteDoc(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Tab 6: 附加险 -->
          <el-tab-pane label="附加险" name="rider">
            <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
              <el-button type="primary" :icon="Plus" size="small" @click="openRiderDialog()">新增关联</el-button>
            </div>
            <el-table :data="riders" stripe border v-loading="riderLoading" size="small">
              <el-table-column prop="mainProductCode" label="主险编码" width="150" />
              <el-table-column prop="riderProductCode" label="附加险编码" width="150" />
              <el-table-column prop="maxRiderCount" label="最大份数" width="100" align="center" />
              <el-table-column prop="isActive" label="状态" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">{{ row.isActive === 1 ? '启用' : '停用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="170" />
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="danger" link @click="handleDeleteRider(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Tab 7: 变更历史 -->
          <el-tab-pane label="变更历史" name="changelog">
            <div v-loading="changelogLoading">
              <el-timeline v-if="changelogs.length > 0">
                <el-timeline-item v-for="log in changelogs" :key="log.id" :timestamp="log.operatedAt" placement="top">
                  <el-card shadow="never" style="padding: 8px 12px">
                    <div><strong>{{ changeTypeLabel(log.changeType) }}</strong> - {{ log.changeTarget }}<span v-if="log.fieldName">.{{ log.fieldName }}</span></div>
                    <div v-if="log.oldValue || log.newValue" style="color: #606266; font-size: 13px; margin-top: 4px">
                      <span style="color: #f56c6c">{{ log.oldValue || '(空)' }}</span> → <span style="color: #67c23a">{{ log.newValue || '(空)' }}</span>
                    </div>
                    <div style="color: #909399; font-size: 12px; margin-top: 4px">操作人：{{ log.operator }}<span v-if="log.remark"> | {{ log.remark }}</span></div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-else description="暂无变更记录" :image-size="60" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>

    <!-- ====== 弹窗们 ====== -->
    <!-- 编辑产品弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑产品" width="550px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="产品编码"><el-input v-model="editForm.productCode" disabled /></el-form-item>
        <el-form-item label="产品名称" prop="productName"><el-input v-model="editForm.productName" /></el-form-item>
        <el-form-item label="简称"><el-input v-model="editForm.shortName" /></el-form-item>
        <el-form-item label="产品类型" prop="productType">
          <el-select v-model="editForm.productType" style="width: 100%">
            <el-option label="寿险" :value="1" /><el-option label="意外险" :value="2" /><el-option label="健康险" :value="3" />
            <el-option label="车险" :value="4" /><el-option label="财产险" :value="5" /><el-option label="责任险" :value="6" /><el-option label="乘务险" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version"><el-input v-model="editForm.version" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 保障弹窗 -->
    <el-dialog v-model="coverageDialogVisible" :title="editingCoverage ? '编辑保障' : '新增保障'" width="500px" destroy-on-close>
      <el-form ref="covFormRef" :model="covForm" :rules="covRules" label-width="90px">
        <el-form-item label="保障编码" prop="coverageCode"><el-input v-model="covForm.coverageCode" /></el-form-item>
        <el-form-item label="保障名称" prop="coverageName"><el-input v-model="covForm.coverageName" /></el-form-item>
        <el-form-item label="选择模式"><el-input-number v-model="covForm.selectionMode" :min="0" /></el-form-item>
        <el-form-item label="给付类型"><el-input-number v-model="covForm.benefitType" :min="0" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="covForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="coverageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="covLoading" @click="handleCoverageSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 缴费计划弹窗 -->
    <el-dialog v-model="premiumDialogVisible" :title="editingPremium ? '编辑缴费计划' : '新增缴费计划'" width="500px" destroy-on-close>
      <el-form ref="premFormRef" :model="premForm" :rules="premRules" label-width="90px">
        <el-form-item label="计划编码" prop="premiumPlanCode"><el-input v-model="premForm.premiumPlanCode" /></el-form-item>
        <el-form-item label="计划名称" prop="premiumPlanName"><el-input v-model="premForm.premiumPlanName" /></el-form-item>
        <el-form-item label="缴费频率"><el-input-number v-model="premForm.paymentFrequency" :min="0" /></el-form-item>
        <el-form-item label="缴费期限"><el-input-number v-model="premForm.paymentTerm" :min="0" /></el-form-item>
        <el-form-item label="期限单位"><el-input-number v-model="premForm.paymentTermUnit" :min="0" /></el-form-item>
        <el-form-item label="宽限天数"><el-input-number v-model="premForm.gracePeriod" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="premiumDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="premLoading" @click="handlePremiumSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 规则弹窗 -->
    <el-dialog v-model="ruleDialogVisible" :title="editingRule ? '编辑规则' : '新增规则'" width="500px" destroy-on-close>
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRules" label-width="90px">
        <el-form-item label="规则编码" prop="ruleCode"><el-input v-model="ruleForm.ruleCode" /></el-form-item>
        <el-form-item label="规则名称" prop="ruleName"><el-input v-model="ruleForm.ruleName" /></el-form-item>
        <el-form-item label="引擎类型" prop="ruleEngine">
          <el-select v-model="ruleForm.ruleEngine" style="width: 100%">
            <el-option label="liteflow" value="liteflow" /><el-option label="aviator" value="aviator" /><el-option label="java" value="java" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则类型"><el-input-number v-model="ruleForm.ruleType" :min="0" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="ruleForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleLoading" @click="handleRuleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 文档弹窗 -->
    <el-dialog v-model="docDialogVisible" :title="editingDoc ? '编辑文档' : '新增文档'" width="500px" destroy-on-close>
      <el-form ref="docFormRef" :model="docForm" :rules="docRules" label-width="90px">
        <el-form-item label="文档名称" prop="documentName"><el-input v-model="docForm.documentName" /></el-form-item>
        <el-form-item label="文档类型"><el-input-number v-model="docForm.documentType" :min="0" /></el-form-item>
        <el-form-item label="文件路径" prop="fileUrl"><el-input v-model="docForm.fileUrl" /></el-form-item>
        <el-form-item label="版本"><el-input v-model="docForm.version" /></el-form-item>
        <el-form-item label="生效日期"><el-date-picker v-model="docForm.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="docDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="docLoading" @click="handleDocSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 附加险弹窗 -->
    <el-dialog v-model="riderDialogVisible" title="新增附加险关联" width="450px" destroy-on-close>
      <el-form ref="riderFormRef" :model="riderForm" :rules="riderRules" label-width="110px">
        <el-form-item label="附加险编码" prop="riderProductCode"><el-input v-model="riderForm.riderProductCode" /></el-form-item>
        <el-form-item label="最大份数"><el-input-number v-model="riderForm.maxRiderCount" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="riderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="riderSubmitLoading" @click="handleRiderSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 关联缴费弹窗 -->
    <el-dialog v-model="covPremiumDialogVisible" title="保障关联缴费计划" width="500px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 12px">
        保障：<strong>{{ selectedCoverage?.coverageName }}</strong>
      </el-alert>
      <el-table :data="covPremiums" stripe size="small" v-loading="covPremLoading">
        <el-table-column prop="premiumPlanCode" label="计划编码" />
        <el-table-column prop="premiumPlanName" label="计划名称" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="handleUnlinkPremium(row)">取消关联</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!covPremLoading" style="margin-top: 12px; display: flex; gap: 8px; align-items: center">
        <el-select v-model="linkPremiumId" placeholder="选择缴费计划" style="flex: 1" size="small">
          <el-option v-for="p in premiums" :key="p.id" :label="p.premiumPlanName" :value="p.id" />
        </el-select>
        <el-button type="primary" size="small" @click="handleLinkPremium">关联</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createProduct, getProductById, updateProduct, cloneProduct, publishProduct, freezeProduct, getProductChangelog,
  getCoverageList, createCoverage, updateCoverage, deleteCoverage, getCoveragePremiums, linkPremium, unlinkPremium,
  getPremiumList, createPremium, updatePremium, deletePremium,
  getRuleList, createRule, updateRule, deleteRule,
  getDocumentList, createDocument, updateDocument, deleteDocument,
  getRiderList, createRider, deleteRider,
} from '@/api/product'
import type { Product, ProductCoverage, ProductPremium, ProductRule, ProductDocument, ProductRiderRel, ProductChangelog } from '@/types'

const route = useRoute()
const router = useRouter()
const productId = computed(() => Number(route.params.id))
const isCreate = computed(() => route.name === 'product-create')

const typeTagMap: Record<number, { label: string; type: string }> = {
  1: { label: '寿险', type: 'primary' }, 2: { label: '意外险', type: 'warning' }, 3: { label: '健康险', type: 'success' },
  4: { label: '车险', type: 'danger' }, 5: { label: '财产险', type: 'info' }, 6: { label: '责任险', type: '' }, 7: { label: '乘务险', type: 'warning' },
}
const vsMap: Record<number, { label: string; type: string }> = {
  0: { label: '草稿', type: 'info' }, 1: { label: '已发布', type: 'success' }, 2: { label: '已冻结', type: 'danger' }, 3: { label: '已驳回', type: 'warning' },
}
function changeTypeLabel(t: number) {
  return ({ 1: '创建', 2: '更新', 3: '删除', 4: '发布', 5: '冻结', 6: '克隆' } as Record<number, string>)[t] || '操作'
}

// ====== 产品 ======
const productLoading = ref(false)
const product = ref<Product>({} as Product)
const activeTab = ref('info')

const createFormRef = ref<FormInstance>()
const createLoading = ref(false)
const createForm = ref({ productCode: '', productName: '', shortName: '', productType: 1, version: '1.0.0' })
const createRules: FormRules = {
  productCode: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  productType: [{ required: true, message: '请选择产品类型', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
}

async function handleCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  createLoading.value = true
  try {
    const res = await createProduct(createForm.value)
    ElMessage.success('创建成功')
    router.replace(`/product/${res.data.id}`)
  } catch { /* handled */ } finally { createLoading.value = false }
}

async function loadProduct() {
  productLoading.value = true
  try { const res = await getProductById(productId.value); product.value = res.data || ({} as Product) } catch { /* handled */ } finally { productLoading.value = false }
}

// 编辑
const editDialogVisible = ref(false); const editLoading = ref(false); const editFormRef = ref<FormInstance>()
const editForm = ref({ productCode: '', productName: '', shortName: '', productType: 1, version: '' })
const editRules: FormRules = { productName: [{ required: true, message: '请输入', trigger: 'blur' }], productType: [{ required: true, message: '请选择', trigger: 'change' }], version: [{ required: true, message: '请输入', trigger: 'blur' }] }
function openEditDialog() { editForm.value = { productCode: product.value.productCode, productName: product.value.productName, shortName: product.value.shortName || '', productType: product.value.productType, version: product.value.version }; editDialogVisible.value = true }
async function handleEditSubmit() { const v = await editFormRef.value?.validate().catch(() => false); if (!v) return; editLoading.value = true; try { await updateProduct(productId.value, editForm.value); ElMessage.success('更新成功'); editDialogVisible.value = false; await loadProduct() } catch {} finally { editLoading.value = false } }
async function handlePublish() { try { await ElMessageBox.confirm(`确定发布产品「${product.value.productName}」吗？`, '确认', { type: 'warning' }); await publishProduct(productId.value); ElMessage.success('发布成功'); await loadProduct() } catch {} }
async function handleFreeze() { try { await ElMessageBox.confirm(`确定冻结产品「${product.value.productName}」吗？`, '确认', { type: 'warning' }); await freezeProduct(productId.value); ElMessage.success('冻结成功'); await loadProduct() } catch {} }
async function handleClone() { try { await ElMessageBox.confirm(`确定克隆产品「${product.value.productName}」吗？`, '确认'); await cloneProduct(productId.value); ElMessage.success('克隆成功'); router.push('/product') } catch {} }

// ====== 保障 ======
const coverages = ref<ProductCoverage[]>([]); const coverageLoading = ref(false)
const coverageDialogVisible = ref(false); const covLoading = ref(false); const covFormRef = ref<FormInstance>(); const editingCoverage = ref<ProductCoverage | null>(null)
const covForm = ref({ coverageCode: '', coverageName: '', selectionMode: 0, benefitType: 0, sortOrder: 0 })
const covRules: FormRules = { coverageCode: [{ required: true, message: '请输入', trigger: 'blur' }], coverageName: [{ required: true, message: '请输入', trigger: 'blur' }] }
async function loadCoverages() { coverageLoading.value = true; try { const r = await getCoverageList(productId.value); coverages.value = r.data || [] } catch { coverages.value = [] } finally { coverageLoading.value = false } }
function openCoverageDialog(row?: ProductCoverage) { if (row) { editingCoverage.value = row; covForm.value = { coverageCode: row.coverageCode, coverageName: row.coverageName, selectionMode: row.selectionMode || 0, benefitType: row.benefitType || 0, sortOrder: row.sortOrder } } else { editingCoverage.value = null; covForm.value = { coverageCode: '', coverageName: '', selectionMode: 0, benefitType: 0, sortOrder: 0 } }; coverageDialogVisible.value = true }
async function handleCoverageSubmit() { const v = await covFormRef.value?.validate().catch(() => false); if (!v) return; covLoading.value = true; try { if (editingCoverage.value) { await updateCoverage(productId.value, editingCoverage.value.id, covForm.value) } else { await createCoverage(productId.value, covForm.value) }; ElMessage.success('成功'); coverageDialogVisible.value = false; await loadCoverages() } catch {} finally { covLoading.value = false } }
async function handleDeleteCoverage(row: ProductCoverage) { try { await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' }); await deleteCoverage(productId.value, row.id); ElMessage.success('删除成功'); await loadCoverages() } catch {} }

// 关联缴费
const covPremiumDialogVisible = ref(false); const covPremLoading = ref(false); const selectedCoverage = ref<ProductCoverage | null>(null); const covPremiums = ref<ProductPremium[]>([]); const linkPremiumId = ref<number | null>(null)
async function viewCoveragePremiums(row: ProductCoverage) { selectedCoverage.value = row; covPremiumDialogVisible.value = true; covPremLoading.value = true; try { const r = await getCoveragePremiums(productId.value, row.id); covPremiums.value = r.data || [] } catch { covPremiums.value = [] } finally { covPremLoading.value = false } }
async function handleLinkPremium() { if (!linkPremiumId.value || !selectedCoverage.value) return; try { await linkPremium(productId.value, selectedCoverage.value.id, linkPremiumId.value); ElMessage.success('关联成功'); const r = await getCoveragePremiums(productId.value, selectedCoverage.value.id); covPremiums.value = r.data || [] } catch {} }
async function handleUnlinkPremium(row: ProductPremium) { if (!selectedCoverage.value) return; try { await unlinkPremium(productId.value, selectedCoverage.value.id, row.id); ElMessage.success('取消关联'); const r = await getCoveragePremiums(productId.value, selectedCoverage.value.id); covPremiums.value = r.data || [] } catch {} }

// ====== 缴费计划 ======
const premiums = ref<ProductPremium[]>([]); const premiumLoading = ref(false)
const premiumDialogVisible = ref(false); const premLoading = ref(false); const premFormRef = ref<FormInstance>(); const editingPremium = ref<ProductPremium | null>(null)
const premForm = ref({ premiumPlanCode: '', premiumPlanName: '', paymentFrequency: 1, paymentTerm: 20, paymentTermUnit: 1, gracePeriod: 30, roundingMode: 0 })
const premRules: FormRules = { premiumPlanCode: [{ required: true, message: '请输入', trigger: 'blur' }], premiumPlanName: [{ required: true, message: '请输入', trigger: 'blur' }] }
async function loadPremiums() { premiumLoading.value = true; try { const r = await getPremiumList(productId.value); premiums.value = r.data || [] } catch { premiums.value = [] } finally { premiumLoading.value = false } }
function openPremiumDialog(row?: ProductPremium) { if (row) { editingPremium.value = row; premForm.value = { premiumPlanCode: row.premiumPlanCode, premiumPlanName: row.premiumPlanName, paymentFrequency: row.paymentFrequency, paymentTerm: row.paymentTerm, paymentTermUnit: row.paymentTermUnit, gracePeriod: row.gracePeriod, roundingMode: row.roundingMode } } else { editingPremium.value = null; premForm.value = { premiumPlanCode: '', premiumPlanName: '', paymentFrequency: 1, paymentTerm: 20, paymentTermUnit: 1, gracePeriod: 30, roundingMode: 0 } }; premiumDialogVisible.value = true }
async function handlePremiumSubmit() { const v = await premFormRef.value?.validate().catch(() => false); if (!v) return; premLoading.value = true; try { if (editingPremium.value) { await updatePremium(productId.value, editingPremium.value.id, premForm.value) } else { await createPremium(productId.value, premForm.value) }; ElMessage.success('成功'); premiumDialogVisible.value = false; await loadPremiums() } catch {} finally { premLoading.value = false } }
async function handleDeletePremium(row: ProductPremium) { try { await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' }); await deletePremium(productId.value, row.id); ElMessage.success('删除成功'); await loadPremiums() } catch {} }

// ====== 规则 ======
const rules = ref<ProductRule[]>([]); const ruleLoading = ref(false)
const ruleDialogVisible = ref(false); const ruleFormRef = ref<FormInstance>(); const editingRule = ref<ProductRule | null>(null)
const ruleForm = ref({ ruleCode: '', ruleName: '', ruleEngine: 'liteflow', ruleType: 1, sortOrder: 0 })
const ruleRules: FormRules = { ruleCode: [{ required: true, message: '请输入', trigger: 'blur' }], ruleName: [{ required: true, message: '请输入', trigger: 'blur' }], ruleEngine: [{ required: true, message: '请选择', trigger: 'change' }] }
async function loadRules() { ruleLoading.value = true; try { const r = await getRuleList(productId.value); rules.value = r.data || [] } catch { rules.value = [] } finally { ruleLoading.value = false } }
function openRuleDialog(row?: ProductRule) { if (row) { editingRule.value = row; ruleForm.value = { ruleCode: row.ruleCode, ruleName: row.ruleName, ruleEngine: row.ruleEngine, ruleType: row.ruleType, sortOrder: row.sortOrder } } else { editingRule.value = null; ruleForm.value = { ruleCode: '', ruleName: '', ruleEngine: 'liteflow', ruleType: 1, sortOrder: 0 } }; ruleDialogVisible.value = true }
async function handleRuleSubmit() { const v = await ruleFormRef.value?.validate().catch(() => false); if (!v) return; ruleLoading.value = true; try { if (editingRule.value) { await updateRule(productId.value, editingRule.value.id, ruleForm.value) } else { await createRule(productId.value, ruleForm.value) }; ElMessage.success('成功'); ruleDialogVisible.value = false; await loadRules() } catch {} finally { ruleLoading.value = false } }
async function handleDeleteRule(row: ProductRule) { try { await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' }); await deleteRule(productId.value, row.id); ElMessage.success('删除成功'); await loadRules() } catch {} }

// ====== 文档 ======
const documents = ref<ProductDocument[]>([]); const docLoading = ref(false)
const docDialogVisible = ref(false); const docFormRef = ref<FormInstance>(); const editingDoc = ref<ProductDocument | null>(null)
const docForm = ref({ documentName: '', documentType: 1, fileUrl: '', version: '1.0', effectiveDate: null as string | null })
const docRules: FormRules = { documentName: [{ required: true, message: '请输入', trigger: 'blur' }], fileUrl: [{ required: true, message: '请输入', trigger: 'blur' }] }
async function loadDocuments() { docLoading.value = true; try { const r = await getDocumentList(productId.value); documents.value = r.data || [] } catch { documents.value = [] } finally { docLoading.value = false } }
function openDocDialog(row?: ProductDocument) { if (row) { editingDoc.value = row; docForm.value = { documentName: row.documentName, documentType: row.documentType, fileUrl: row.fileUrl, version: row.version, effectiveDate: row.effectiveDate } } else { editingDoc.value = null; docForm.value = { documentName: '', documentType: 1, fileUrl: '', version: '1.0', effectiveDate: null } }; docDialogVisible.value = true }
async function handleDocSubmit() { const v = await docFormRef.value?.validate().catch(() => false); if (!v) return; docLoading.value = true; try { if (editingDoc.value) { await updateDocument(productId.value, editingDoc.value.id, docForm.value) } else { await createDocument(productId.value, docForm.value) }; ElMessage.success('成功'); docDialogVisible.value = false; await loadDocuments() } catch {} finally { docLoading.value = false } }
async function handleDeleteDoc(row: ProductDocument) { try { await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' }); await deleteDocument(productId.value, row.id); ElMessage.success('删除成功'); await loadDocuments() } catch {} }

// ====== 附加险 ======
const riders = ref<ProductRiderRel[]>([]); const riderLoading = ref(false)
const riderDialogVisible = ref(false); const riderSubmitLoading = ref(false); const riderFormRef = ref<FormInstance>()
const riderForm = ref({ riderProductCode: '', maxRiderCount: 1 })
const riderRules: FormRules = { riderProductCode: [{ required: true, message: '请输入附加险编码', trigger: 'blur' }] }
async function loadRiders() { riderLoading.value = true; try { const r = await getRiderList(productId.value); riders.value = r.data || [] } catch { riders.value = [] } finally { riderLoading.value = false } }
function openRiderDialog() { riderForm.value = { riderProductCode: '', maxRiderCount: 1 }; riderDialogVisible.value = true }
async function handleRiderSubmit() { const v = await riderFormRef.value?.validate().catch(() => false); if (!v) return; riderSubmitLoading.value = true; try { await createRider(productId.value, { ...riderForm, mainProductCode: product.value.productCode } as any); ElMessage.success('关联成功'); riderDialogVisible.value = false; await loadRiders() } catch {} finally { riderSubmitLoading.value = false } }
async function handleDeleteRider(row: ProductRiderRel) { try { await ElMessageBox.confirm('确定删除关联？', '确认', { type: 'warning' }); await deleteRider(productId.value, row.id); ElMessage.success('删除成功'); await loadRiders() } catch {} }

// ====== 变更历史 ======
const changelogs = ref<ProductChangelog[]>([]); const changelogLoading = ref(false)
async function loadChangelogs() { changelogLoading.value = true; try { const r = await getProductChangelog(productId.value); changelogs.value = r.data || [] } catch { changelogs.value = [] } finally { changelogLoading.value = false } }

// ====== Tab 懒加载 ======
const loadedTabs = new Set<string>()
function handleTabChange(tab: string) {
  if (loadedTabs.has(tab)) return
  loadedTabs.add(tab)
  if (tab === 'coverage') loadCoverages()
  if (tab === 'premium') loadPremiums()
  if (tab === 'rule') loadRules()
  if (tab === 'document') loadDocuments()
  if (tab === 'rider') loadRiders()
  if (tab === 'changelog') loadChangelogs()
}

onMounted(() => { if (!isCreate.value) loadProduct() })
</script>
