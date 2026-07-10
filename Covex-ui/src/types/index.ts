// ============ 与后端 Result<T> 对齐 ============

export interface Result<T = any> {
  code: number
  data: T
  message: string
}

// ============ 分页请求 ============

export interface PageQuery {
  page: number
  size: number
  keyword?: string
  [key: string]: any
}

// ============ 分页响应 ============

export interface PageResult<T = any> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

// ============ 通用实体基类 ============

export interface BaseEntity {
  id: number
  tenantId: number
  isDeleted: number
  deletedAt: string | null
  createdBy: string | null
  updatedBy: string | null
  createdAt: string
  updatedAt: string
}

// ============ 字典 ============

export interface DictItem extends BaseEntity {
  dictType: string
  dictCode: string
  dictName: string
  parentCode: string | null
  sortOrder: number
  isActive: number
  remark: string | null
}

// ============ 用户 ============

export interface User extends BaseEntity {
  username: string
  realName: string
  phone: string | null
  email: string | null
  status: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
}

// ============ 角色 ============

export interface Role extends BaseEntity {
  roleCode: string
  roleName: string
  description: string | null
}

// ============ 权限 ============

export interface Permission extends BaseEntity {
  permissionCode: string
  permissionName: string
  module: string
  action: string
}

// ============ 数据范围 ============

export interface DataScope {
  roleId: number
  scopeType: number
  customScopes: string[]
}

// ============ 客户 ============

export interface Customer extends BaseEntity {
  customerCode: string
  customerName: string
  idType: number
  idNo: string
  idExpiry: string | null
  gender: number | null
  birthDate: string | null
  nationality: string | null
  phone: string
  email: string | null
  customerType: number | null
  roleFlags: Record<string, any> | null
  source: number | null
  attributes: Record<string, any> | null
}

export interface CustomerHealthData {
  medicalHistory: Record<string, any>[] | null
  familyHistory: Record<string, any>[] | null
  currentMedications: Record<string, any>[] | null
  lastHealthUpdate: string | null
}

export interface CustomerAddress extends BaseEntity {
  customerId: number
  addressType: number
  province: string
  city: string
  district: string
  detail: string
  zipCode: string | null
  isDefault: number
}

export interface CustomerBankAccount extends BaseEntity {
  customerId: number
  bankName: string
  accountNo: string
  branchName: string | null
  usageType: number
  accountHolder: string
  isDefault: number
}

// ============ 产品 ============

export interface Product extends BaseEntity {
  productCode: string
  version: string
  versionStatus: number
  productName: string
  shortName: string | null
  productType: number
  productNature: number | null
  termType: number | null
  mainRiderFlag: number | null
  saleChannel: any
  startDate: string | null
  endDate: string | null
  status: number
  capabilities: Record<string, any> | null
  attributes: Record<string, any> | null
  parentVersionId: number | null
  templateSource: number | null
  templateRefId: number | null
}

export interface ProductCoverage extends BaseEntity {
  productId: number
  coverageCode: string
  coverageName: string
  selectionMode: number | null
  benefitType: number | null
  coverageDetail: Record<string, any> | null
  sortOrder: number
}

export interface ProductPremium extends BaseEntity {
  productId: number
  premiumPlanCode: string
  premiumPlanName: string
  paymentFrequency: number
  paymentTerm: number
  paymentTermUnit: number
  gracePeriod: number
  roundingMode: number
  premiumDetail: Record<string, any> | null
}

export interface ProductRule {
  id: number
  tenantId: number
  productId: number
  coverageId: number | null
  ruleType: number
  ruleEngine: string
  ruleCode: string
  ruleName: string
  sortOrder: number
  isActive: number
  isDeleted: number
  deletedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface ProductDocument {
  id: number
  tenantId: number
  productId: number
  documentType: number
  documentName: string
  fileUrl: string
  version: string
  effectiveDate: string | null
  expiryDate: string | null
  isDeleted: number
  deletedAt: string | null
  createdBy: string | null
  createdAt: string
}

export interface ProductRiderRel {
  id: number
  tenantId: number
  mainProductCode: string
  riderProductCode: string
  maxRiderCount: number
  isActive: number
  isDeleted: number
  deletedAt: string | null
  createdAt: string
}

export interface ProductChangelog {
  id: number
  tenantId: number
  productId: number
  changeType: number
  changeTarget: string
  changeTargetId: number | null
  fieldName: string
  oldValue: string
  newValue: string
  operator: string
  operatedAt: string
  remark: string | null
}

// ============ 产品模板 ============

export interface ProductTemplate extends BaseEntity {
  templateCode: string
  templateName: string
  templateDesc: string | null
  productType: number
  icon: string | null
  sortOrder: number
  isActive: number
  templateData: Record<string, any>
  paramSchema: Record<string, any>
}

export interface FromTemplateRequest {
  templateCode: string
  tenantId?: number
  params: Record<string, any>
}

// ============ 费率表 ============

export interface RateTable {
  id: number
  tenantId: number
  rateTableCode: string
  rateTableName: string
  productId: number
  version: string
  tableSchema: Record<string, any> | null
  effectiveDate: string | null
  expiryDate: string | null
  isDeleted: number
  deletedAt: string | null
  createdBy: string | null
  createdAt: string
}

export interface RateTableRow {
  id: number
  rateTableId: number
  dimensionKey: string
  dimensionJson: Record<string, any> | null
  rateValue: number
  extraValues: Record<string, any> | null
}

// ============ 投保单 ============

export interface Proposal extends BaseEntity {
  proposalNo: string
  productId: number
  channelId: number | null
  channelUserId: number | null
  applicantId: number
  insuredId: number
  productSnapshot: Record<string, any> | null
  selectedCoverages: any
  selectedPremiumPlan: Record<string, any> | null
  healthDeclaration: any
  totalPremium: number | null
  totalSumInsured: number
  status: number
  submitAt: string | null
  operator: string | null
}

export interface CreateProposalRequest {
  tenantId?: number
  productId: number
  channelId?: number | null
  channelUserId?: number | null
  applicantId: number
  insuredId: number
  selectedCoverages?: any[]
  selectedPremiumPlan?: Record<string, any> | null
  healthDeclaration?: any[]
  totalSumInsured: number
}

// ============ 核保 ============

export interface UnderwritingRecord extends BaseEntity {
  proposalId: number
  uwType: number
  uwResult: number
  loadingAmount: number | null
  exclusionDesc: string | null
  uwComment: string | null
  uwOperator: string
  uwAt: string
}

// ============ 支付 ============

export interface Payment extends BaseEntity {
  paymentNo: string
  policyId: number | null
  proposalId: number
  paymentType: number
  amount: number
  payChannel: number | null
  payChannelNo: string | null
  status: number
  paidAt: string | null
  operator: string | null
}

// ============ 保单 ============

export interface Policy extends BaseEntity {
  policyNo: string
  proposalId: number
  productId: number
  channelId: number | null
  applicantId: number
  insuredId: number
  productSnapshot: Record<string, any> | null
  effectiveDate: string | null
  expiryDate: string | null
  totalPremium: number
  totalSumInsured: number
  paymentMode: number
  status: number
  terminationReason: number | null
  terminatedAt: string | null
  beneficiaries: any
}

export interface PolicyCoverage extends BaseEntity {
  policyId: number
  coverageCode: string
  coverageName: string
  sumInsured: number
  premium: number
  deductible: number
  coverageDetail: any
  status: number
  cumulativePaid: number | null
  version: number | null
}

export interface PolicyPremium extends BaseEntity {
  policyId: number
  premiumPlanCode: string
  paymentFrequency: number
  paymentTerm: number
  paymentTermUnit: number
  periodPremium: number
  totalPeriods: number
  paidPeriods: number
  nextDueDate: string | null
  gracePeriod: number
}

// ============ 理赔 ============

export interface Claim extends BaseEntity {
  claimNo: string
  policyId: number
  coverageId: number | null
  reporterId: number | null
  reporterRelation: number | null
  accidentDate: string | null
  accidentType: string | null
  accidentDesc: string | null
  accidentLocation: string | null
  claimAmount: number | null
  approvedAmount: number | null
  status: number
  claimHandler: string | null
  reportedAt: string | null
  closedAt: string | null
}

export interface ReportClaimRequest {
  policyId: number
  coverageId?: number | null
  reporterId?: number | null
  reporterRelation?: number | null
  accidentDate: string
  accidentType: string
  accidentDesc: string
  accidentLocation?: string
  claimAmount: number
}

export interface ClaimDocument extends BaseEntity {
  claimId: number
  documentType: number
  fileUrl: string
  fileName: string
  uploadedAt: string | null
  uploadedBy: string | null
}

export interface ClaimReview extends BaseEntity {
  claimId: number
  reviewType: number
  reviewResult: number
  approvedAmount: number | null
  rejectReason: string | null
  reviewComment: string | null
  reviewer: string
  reviewedAt: string | null
}

export interface ClaimReviewRequest {
  reviewType: number
  reviewResult: number
  approvedAmount?: number | null
  rejectReason?: string
  comment?: string
  reviewer: string
}

export interface InvestigationResultRequest {
  result: number
  comment: string
}

export interface ClaimPayment extends BaseEntity {
  claimId: number
  paymentId: number | null
  beneficiaryId: number | null
  beneficiaryName: string | null
  amount: number
  paidAt: string | null
  operator: string | null
}

// ============ 佣金 ============

export interface Commission extends BaseEntity {
  commissionNo: string
  channelId: number
  channelUserId: number | null
  policyId: number
  commissionType: number
  premiumAmount: number
  commissionRate: number
  commissionAmount: number
  settleMonth: string
  settleStatus: number
  settledAt: string | null
  operator: string | null
}

// ============ 渠道 ============

export interface Channel extends BaseEntity {
  channelCode: string
  channelName: string
  channelType: number
  licenseNo: string | null
  licenseExpiry: string | null
  contactName: string
  contactPhone: string
  contactEmail: string | null
  regionCode: string | null
  status: number
  contractNo: string | null
  contractStart: string | null
  contractEnd: string | null
  attributes: Record<string, any> | null
}

export interface ChannelUser extends BaseEntity {
  channelId: number
  username: string
  realName: string
  agentLicenseNo: string | null
  phone: string | null
  status: number
  lastLoginAt: string | null
}

export interface ChannelProduct extends BaseEntity {
  channelId: number
  productId: number
  firstYearRate: number
  renewalRate: number
  saleRegion: string | null
  isActive: number
}

// ============ 渠道授权请求 ============

export interface ChannelAuthorizeRequest {
  productId: number
  firstYearRate: number
  renewalRate: number
}

