#!/usr/bin/env node
const fs = require('fs');
const path = 'docs/Covex前端开发计划.md';
let content = fs.readFileSync(path, 'utf8');

// F12 理赔管理
content = content.replace(/- \[ \] 创建理赔 API 层/, '- [x] 创建理赔 API 层');
content = content.replace(/- \[ \] 实现理赔工作台页面/, '- [x] 实现理赔工作台页面');
content = content.replace(/- \[ \] 实现理赔报案表单/, '- [x] 实现理赔报案表单');
content = content.replace(/- \[ \] 实现理赔详情页/, '- [x] 实现理赔详情页');
content = content.replace(/- \[ \] 实现审核表单/, '- [x] 实现审核表单');
content = content.replace(/- \[ \] 实现调查结论表单/, '- [x] 实现调查结论表单');
content = content.replace(/- \[ \] 理赔状态标签/, '- [x] 理赔状态标签');
content = content.replace(/- \[ \] 配置路由：\/claim/, '- [x] 配置路由：/claim');

fs.writeFileSync(path, content, 'utf8');
console.log('F12 checkboxes updated');
