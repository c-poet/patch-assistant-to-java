# 🔧 PatchAssistant4J (补丁助手) By CPoet

### 📜 说明
**🚀 Jar应用快速补丁替换**  
去除打增量补丁时通过WinRAR（或者其他压缩工具）手动查找升级文件、替换文件的繁琐。

---

### 🖥️ 界面展示
#### 🏠 主界面
![主界面](./images/app-main.png)

#### 💻 代码反编译
![代码反编译](./images/app-decompile.png)

#### 📄 更新说明文件
![更新说明文件](./images/app-edit-readme.png)

#### ✏️ 写入补丁签名
![写入补丁签名](./images/app-patch-sign.png)

---

### 📂 使用文档
- [📌 PatchAssistant2J安装说明](./docs/PatchAssistant2J安装说明.md)

---

### ✨ 核心功能
1. **智能匹配**  
   🔸 全路径/文件名双模式匹配  
   🔸 自动解析Readme.txt升级指令
2. **代码分析**  
   🔸 Class反编译 + 差异高亮对比  
   🔸 支持Java/HTML/CSS/JS语法着色
3. **高效管理**  
   🔸 无限展开Jar/Zip嵌套结构  
   🔸 拖拽式补丁文件替换
4. **安全升级**  
   🔸 补丁签名校验机制  
   🔸 变更记录自动存档

### Next V1.4.7
- [ ] 写入补丁签名按名称倒叙
- [x] 解决文件拖到外部应用时因临时文件被删除报文件不存在问题

### Next V1.4.8
- [ ] 代码反编译支持全局反编译并推断泛型类型

---

### 🙏 特别鸣谢
- 🌐 gluonfx
- 🖥️ Fernflower
- ✏️ richtextfx
- 🔍 java-diff-utils
- 🎨 iconfont（图标）

---

<div align="center">
🛠️ 开源地址：<a href="https://github.com/c-poet/patch-assistant-to-java">https://github.com/c-poet/patch-assistant-to-java</a>
💖 欢迎Star & Fork！
</div>
