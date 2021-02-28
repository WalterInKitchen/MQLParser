# 该脚本用来检查副本集状态并尝试初始化副本集
# 状态ok或者创建成功返回0

#!/bin/bash

echo "start check replication set status"
echo "=================================="

## 环境变量
mongoHost=$MONGO_DB_HOST
mongoReplicaSetName=$MONGO_REPLICA_SET_NAME
mongoRoot=$MONGO_INITDB_ROOT_USERNAME
mongoPasswd=$MONGO_INITDB_ROOT_PASSWORD


## 检查副本集是否已经初始化
status="$(echo "rs.status().ok" | mongo -u $mongoRoot -p $mongoPasswd --quiet)"
echo "mongo replication status is:" $status

if [ $status = "1" ]; then
    echo "mongo replication set status is ok"
    echo "exit..."
    exit 0
fi

## 构造配置参数
echo "try to init replication set"
config="{_id : \"$mongoReplicaSetName\",members: [{ _id: 0, host: \"$mongoHost:27017\" }]}"

echo "rsConfig:" $config

## 开始配置
status="$(echo "rs.initiate("$config").ok" | mongo -u $mongoRoot -p $mongoPasswd --quiet)"

if [ $status = "1" ]; then
    echo "mongo replication set init ok"
    echo "exit..."
    exit 0
fi

## 副本集配置失败
echo "mongo replication set init failed"
exit 1