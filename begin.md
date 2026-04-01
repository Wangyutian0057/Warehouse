# 在 Linux 服务器上创建上传目录
mkdir -p /opt/warehouse-oracle/upload/
chmod -R 777 /opt/warehouse-oracle/upload/
chmod -R 755 /opt/warehouse-oracle/upload/

# 启动服务
nohup java -jar warehouse-0.X.X.jar > date.log 2>&1 &

#查询端口状态信息
firewall-cmd --query-port=8888/tcp

#开启端口
firewall-cmd --zone=public --add-port=888/tcp --permanent

#重新载入规则
firewall-cmd --reload

#如果要列出所有端口查看，可以使用：
firewall-cmd --list-ports