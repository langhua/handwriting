## 后台开发笔记
汉字手写检查的后台，使用Tomcat作为Web服务器，Groovy作为实现Web服务的开发语言，通过JNA库调用Win32 DLL里的汉字手写检查算法库。

这里要注意，由于DLL库是32位的，所以JDK也必须是32位的。

#### C接口
根据[DLL库C语言头文件](https://github.com/langhua/handwriting/blob/master/src/main/resources/xw_api.h)，通过JNA调用以下4个接口：
1. XW_CreateLib
2. XW_DeleteLib()
3. XW_RecgB
4. XW_GetStdTuxg

#### JNA
使用JNA，C接口入参/出参如何构建是要点。C对指针的内存分配，是连续的整块地址，Java List是不连续的分散的地址，Java的Array虽然是连续的内存，但是，从Java到C的类型映射，有时并不能如文档所言顺畅调用。所以，在用JNA传参时，本项目中使用Memory构建Pointer来实现。

#### OpenAPI文档
本项目的Web接口，使用OpenAPI文档进行说明，并且，可以直接在页面上进行接口测试。

测试接口地址：

http://langhua.tech/hanzi/openapi/
