<template>
  <div>
    <el-page-header @back="$router.push('/product')" title="返回" content="从模板创建产品" />

    <el-card style="margin-top: 20px">
      <!-- 步骤条 -->
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 28px">
        <el-step title="选择模板" />
        <el-step title="填写参数" />
        <el-step title="确认创建" />
      </el-steps>

      <!-- ========== Step 1: 模板选择 ========== -->
      <div v-if="currentStep === 0" v-loading="templatesLoading">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" v-for="tpl in templates" :key="tpl.id" style="margin-bottom: 16px">
            <el-card
              shadow="hover"
              :class="['tpl-card', { 'tpl-card--selected': selectedTemplate?.id === tpl.id }]"
              @click="selectedTemplate = tpl"
              style="cursor: pointer; height: 100%"
            >
              <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px">
                <el-icon :size="28" :color="typeColorMap[tpl.productType]?.color || '#409eff'">
                  <component :is="tpl.icon || 'Document'" />
                </el-icon>
                <div>
                  <div style="font-weight: 600; font-size: 15px">{{ tpl.templateName }}</div>
                  <el-tag :type="typeColorMap[tpl.productType]?.type || 'info'" size="small">{{ typeLabel(tpl.productType) }}</el-tag>
                </div>
              </div>
              <div style="color: #606266; font-size: 13px; line-height: 1.5">{{ tpl.templateDesc }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!templatesLoading && templates.length === 0" description="暂无可用模板" />
        <div style="text-align: right; margin-top: 16px">
          <el-button @click="$router.push('/product')">取消</el-button>
          <el-button type="primary" :disabled="!selectedTemplate" @click="onTemplateSelected">下一步</el-button>
        </div>
      </div>

      <!-- ========== Step 2: 参数表单 ========== -->
      <div v-if="currentStep === 1" style="max-width: 650px; margin: 0 auto">
        <el-form ref="paramFormRef" :model="paramForm" :rules="paramRules" label-width="130px">
          <template v-for="section in paramSections" :key="section.title">
            <el-divider content-position="left">{{ section.title }}</el-divider>
            <el-form-item
              v-for="field in section.fields"
              :key="field.key"
              :label="field.label"
              :prop="field.key"
            >
              <!-- string -->
              <el-input
                v-if="field.type === 'string'"
                v-model="paramForm[field.key]"
                :placeholder="field.placeholder || ''"
              />
              <!-- number -->
              <el-input-number
                v-if="field.type === 'number'"
                v-model="paramForm[field.key]"
                :min="0"
                :step="field.key.includes('sum') ? 10000 : 1"
                style="width: 100%"
              />
              <span v-if="field.type === 'number' && field.unit" style="margin-left: 8px; color: #909399; white-space: nowrap">{{ field.unit }}</span>
              <!-- select -->
              <el-select
                v-if="field.type === 'select'"
                v-model="paramForm[field.key]"
                style="width: 100%"
              >
                <el-option
                  v-for="opt in field.options"
                  :key="typeof opt === 'object' ? opt.value : opt"
                  :label="typeof opt === 'object' ? opt.label : `${opt}${field.unit || ''}`"
                  :value="typeof opt === 'object' ? opt.value : opt"
                />
              </el-select>
              <!-- multi_select -->
              <el-checkbox-group
                v-if="field.type === 'multi_select'"
                v-model="paramForm[field.key]"
              >
                <el-checkbox
                  v-for="opt in field.options"
                  :key="opt"
                  :label="opt"
                  :value="opt"
                >{{ opt }}{{ field.unit || '' }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </template>

          <!-- 模板预置摘要 -->
          <el-divider content-position="left">模板已预置（可在详情页调整）</el-divider>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="保障责任数">{{ templateSummary.coverages }} 项</el-descriptions-item>
            <el-descriptions-item label="缴费计划数">{{ templateSummary.premiums }} 个</el-descriptions-item>
            <el-descriptions-item label="核保规则数">{{ templateSummary.rules }} 条</el-descriptions-item>
            <el-descriptions-item label="产品类型">{{ typeLabel(selectedTemplate?.productType) }}</el-descriptions-item>
          </el-descriptions>
        </el-form>

        <div style="text-align: right; margin-top: 20px">
          <el-button @click="currentStep = 0">上一步</el-button>
          <el-button type="primary" @click="onParamsFilled">下一步</el-button>
        </div>
      </div>

      <!-- ========== Step 3: 确认预览 ========== -->
      <div v-if="currentStep === 2" style="max-width: 700px; margin: 0 auto">
        <el-alert type="info" :closable="false" style="margin-bottom: 16px">
          请确认以下产品配置。创建后为草稿状态，可在详情页进一步编辑。
        </el-alert>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品名称">{{ paramForm.product_name }}</el-descriptions-item>
          <el-descriptions-item label="产品编码">{{ paramForm.product_code }}</el-descriptions-item>
          <el-descriptions-item label="简称">{{ paramForm.short_name }}</el-descriptions-item>
          <el-descriptions-item label="产品类型">{{ typeLabel(selectedTemplate?.productType) }}</el-descriptions-item>
          <el-descriptions-item label="来源模板">{{ selectedTemplate?.templateName }}</el-descriptions-item>
          <el-descriptions-item label="版本号">1.0.0</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 16px">
          <div style="font-weight: 600; margin-bottom: 8px">保障责任（{{ templateDataCoverages.length }} 项）</div>
          <el-table :data="templateDataCoverages" stripe border size="small">
            <el-table-column prop="coverage_name" label="名称" />
            <el-table-column prop="coverage_code" label="编码" width="140" />
            <el-table-column label="选择模式" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.selection_mode === 1 ? 'success' : 'info'" size="small">{{ row.selection_mode === 1 ? '必选' : '可选' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div style="margin-top: 16px">
          <div style="font-weight: 600; margin-bottom: 8px">缴费计划（{{ templateDataPremiums.length }} 个）</div>
          <el-table :data="templateDataPremiums" stripe border size="small">
            <el-table-column prop="premium_plan_name" label="名称" />
            <el-table-column prop="premium_plan_code" label="编码" width="140" />
            <el-table-column label="缴费频率" width="90" align="center">
              <template #default="{ row }">{{ freqLabel(row.payment_frequency) }}</template>
            </el-table-column>
            <el-table-column label="期限" width="100" align="center">
              <template #default="{ row }">{{ row.payment_term }}{{ row.payment_term_unit === 1 ? '年' : '月' }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div style="margin-top: 16px">
          <div style="font-weight: 600; margin-bottom: 8px">核保规则（{{ templateDataRules.length }} 条）</div>
          <el-table :data="templateDataRules" stripe border size="small">
            <el-table-column prop="rule_name" label="名称" />
            <el-table-column prop="rule_code" label="编码" width="180" />
            <el-table-column prop="rule_engine" label="引擎" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.rule_engine === 'aviator' ? 'warning' : 'success'" size="small">{{ row.rule_engine }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px">
          <el-checkbox v-model="confirmed">我确认以上配置正确，创建为草稿产品</el-checkbox>
        </div>

        <div style="text-align: right; margin-top: 20px">
          <el-button @click="currentStep = 1">上一步</el-button>
          <el-button type="primary" :loading="createLoading" :disabled="!confirmed" @click="handleCreate">创建产品</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTemplateList, createProductFromTemplate } from '@/api/product'
import type { ProductTemplate } from '@/types'
import { useDictStore } from '@/stores/dict'

const router = useRouter()

// ====== Step 1: 模板选择 ======
const templates = ref<ProductTemplate[]>([])
const templatesLoading = ref(false)
const selectedTemplate = ref<ProductTemplate | null>(null)
const currentStep = ref(0)

const typeColorMap: Record<number, { type: string; color: string }> = {
  1: { type: 'primary', color: '#409eff' },
  2: { type: 'warning', color: '#e6a23c' },
  3: { type: 'success', color: '#67c23a' },
  4: { type: 'danger', color: '#f56c6c' },
  5: { type: 'info', color: '#909399' },
  6: { type: '', color: '#909399' },
  7: { type: 'warning', color: '#e6a23c' },
}
function typeLabel(t: number | undefined) {
  if (t == null) return '未知'
  return useDictStore().getDictLabel('product_type', String(t)) || '未知'
}

async function loadTemplates() {
  templatesLoading.value = true
  try {
    const res = await getTemplateList()
    templates.value = res.data || []
  } catch { /* handled */ } finally {
    templatesLoading.value = false
  }
}

function onTemplateSelected() {
  if (!selectedTemplate.value) return
  initParamForm(selectedTemplate.value)
  currentStep.value = 1
}

// ====== Step 2: 参数表单 ======
const paramFormRef = ref<FormInstance>()
const paramForm = ref<Record<string, any>>({})
const paramRules = ref<FormRules>({})
const paramSections = ref<any[]>([])

const templateSummary = computed(() => {
  const td = (selectedTemplate.value?.templateData || {}) as any
  return {
    coverages: (td.coverages || []).length,
    premiums: (td.premium_plans || []).length,
    rules: (td.rules || []).length,
  }
})

const templateDataCoverages = computed(() => {
  const td = (selectedTemplate.value?.templateData || {}) as any
  return td.coverages || []
})
const templateDataPremiums = computed(() => {
  const td = (selectedTemplate.value?.templateData || {}) as any
  return td.premium_plans || []
})
const templateDataRules = computed(() => {
  const td = (selectedTemplate.value?.templateData || {}) as any
  return td.rules || []
})

function initParamForm(tpl: ProductTemplate) {
  const schema = (tpl.paramSchema || { sections: [] }) as any
  const sections = schema.sections || []
  paramSections.value = sections

  const form: Record<string, any> = {}
  const rules: FormRules = {}

  for (const section of sections) {
    for (const field of section.fields || []) {
      // 设置默认值
      if (field.type === 'multi_select') {
        form[field.key] = Array.isArray(field.default) ? [...field.default] : []
      } else {
        form[field.key] = field.default ?? ''
      }
      // 构建校验规则
      if (field.required) {
        rules[field.key] = [{
          required: true,
          message: `请填写${field.label}`,
          trigger: field.type === 'select' || field.type === 'multi_select' ? 'change' : 'blur',
        }]
      }
    }
  }

  paramForm.value = form
  paramRules.value = rules
}

async function onParamsFilled() {
  const valid = await paramFormRef.value?.validate().catch(() => false)
  if (!valid) return
  currentStep.value = 2
}

// ====== Step 3: 确认创建 ======
const confirmed = ref(false)
const createLoading = ref(false)

function freqLabel(f: number) {
  return useDictStore().getDictLabel('payment_frequency', String(f)) || `频率${f}`
}

async function handleCreate() {
  if (!confirmed.value || !selectedTemplate.value) return
  createLoading.value = true
  try {
    const res = await createProductFromTemplate({
      templateCode: selectedTemplate.value.templateCode,
      params: { ...paramForm.value },
    })
    ElMessage.success(`产品「${res.data.productName}」创建成功`)
    router.push(`/product/${res.data.productId}`)
  } catch { /* handled */ } finally {
    createLoading.value = false
  }
}

onMounted(() => { loadTemplates() })
</script>

<style scoped>
.tpl-card {
  transition: all 0.2s ease;
  border: 2px solid transparent;
}
.tpl-card:hover {
  transform: translateY(-2px);
}
.tpl-card--selected {
  border-color: #409eff;
  background: #ecf5ff;
}
</style>
