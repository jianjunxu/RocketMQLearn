##一、安装
1.windows
(1) 下载 git clone git@github.com:alibaba/RocketMQ.git
(2) 打包mvn -Dmaven.test.skip=true clean package install assembly:assembly -U
    使用-U参数： 该参数能强制让Maven检查所有SNAPSHOT依赖更新，确保集成基于最新的状态，如果没有该参数，Maven默认以天为单位检查更新，而持续集成的频率应该比这高很多。
    或者执行install.bat文件
(3) 启动服务
    启动mqnamesrv >mqnamesrv.exe
    启动broker >mqbroker.exe -n localhost:9876
(4) 查看进程 jps -v

2.linux
