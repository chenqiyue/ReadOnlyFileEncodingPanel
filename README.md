解决2019版本后intellij无法切换jar文件编码的问题。突破了两个限制：

- 1.原先是status bar在只读文件下被禁用：新增了一个专供只读文件使用是status bar
- 2.由EncodingManager.setEncoding在打开多窗口时，切换功能失效：使用强制刷新editor的方式更新只读文件编码