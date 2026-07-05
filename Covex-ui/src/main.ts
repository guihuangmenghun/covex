import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'
import { permission } from './directives/permission'
import { useDictStore } from './stores/dict'
import './assets/main.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 注册全局指令
app.directive('permission', permission)

// 初始化字典缓存
const dictStore = useDictStore()
dictStore.loadAllDicts()

app.mount('#app')
