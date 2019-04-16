# 文件资源服务器读取服务端
> 之前在自己的博客中提到如何搭建文件存储服务器，[搭建File Browser文件资源服务器](https://blog.csdn.net/m0_37701381/article/details/89243516)，在实际部署时，应该将文件存储服务器部署在内网，禁止外网访问，再部署一个读取服务，将这个服务接口映射出去，用于读取

而这个读取服务就是为这篇博客中的文件资源服务器所建的

## 使用说明
### 1. pom
服务端的pom文件很简单，就引入了两个依赖，一个是Spring Boot的Web依赖，一个是[fm-client](https://github.com/Chenbin1996/fm-client)客户端jar包

### 2. 修改配置
在`resources`目录下有个`fm-config.properties`文件，修改`fm.url`这一行，改成文件资源服务器所在地址（IP + 端口号）

另外在`application.properties`中修改自定义端口号，特别注意的是`fm.agent.isDownload`这一行，控制是要访问文件资源服务器后要下载还是在线预览，true为下载，false为预览

### 3. 运行程序
运行`Application`将程序跑起来后，只要访问服务端IP + 资源服务器上文件相对路径就可以访问，实现读写分离

也可以实现缩略图预览，比如：读取服务端IP + 端口 + 资源服务器文件相对路径 + 高度 + 宽度（127.0.0.1:8088/text/text.jpg?height=200&width=200）

宽度和高度自定义即可实现不同数值的缩略图
