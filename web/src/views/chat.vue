<template>
  <div class="chat-container">
    <div class="chat-header">
      <h2>与{{userId}}聊天</h2>
    </div>
    <div class="chat-body">
      <div class="msg-box" ref="msg-box">
        <div
          v-for="(i,index) in list"
          :key="index"
          class="msg"
        >
          <div class="user-msg">
            <span :class="i.toId == userId?'msg-right':'msg-left'">{{i.messageContent}}</span>
          </div>
        </div>
      </div>
      <!-- 半常驻输入区域 -->
      <div class="input-box">
        <input type="text" ref="sendMsg" v-model="contentText" @keyup.enter="sendText()" placeholder="输入消息..." />
        <div class="btn" :class="{['btn-active']:contentText}" @click="sendText()">发送</div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        ws: null,
        count: 0,
        // userId: this.$store.getters.id, // 当前用户ID
        // username: this.$store.getters.name, // 当前用户昵称
        // avatar: this.$store.getters.avatar, // 当前用户头像
        list: [], // 聊天记录的数组
        contentText: "", // input输入的值
        userId: 0,
        myUserId: 0
      };
    },
    mounted() {
        console.log(this.$route.params)
        this.userId = this.$route.params.userId,
        this.initWebSocket();
        this.myUserId = localStorage.getItem('user')
        console.log(123,this.myUserId)
    },

    destroyed() {
      // 离开页面时关闭websocket连接
      this.ws.onclose(undefined);
    },
    methods: {
      // 发送聊天信息
      sendText() {
        let _this = this;
        _this.$refs["sendMsg"].focus();
        if (!_this.contentText) {
          return;
        }
        let params = {
          toId: _this.userId,
          messageContent: _this.contentText,
          fromId: _this.myUserId,
        };
        _this.ws.send(JSON.stringify(params)); //调用WebSocket send()发送信息的方法
        _this.list.push(params)
        _this.contentText = "";
        setTimeout(() => {
          _this.scrollBottm();
        }, 500);
      },
      // 进入页面创建websocket连接
      initWebSocket() {
        let _this = this;
        // 判断页面有没有存在websocket连接
        if (window.WebSocket) {
          //var serverHot =  window.location.hostname;
          //let sip = '房间号'
          // 填写本地IP地址 此处的 :9101端口号 要与后端配置的一致！
          //var url = 'ws://' + serverHot + ':9101' + '/groupChat/' + sip + '/' + this.userId; // `ws://127.0.0.1/9101/groupChat/10086/聊天室`
          //this.websocket = new WebSocket(`ws://localhost:8081/${id}`);
          let url = `ws://localhost:8081/chat/${_this.userId}`;
          let ws = new WebSocket(url);
          _this.ws = ws;
          ws.onopen = function(e) {
            console.log("服务器连接成功: " + url,e);
            //ws.send(_this.userId)
          };
          ws.onclose = function(e) {
            console.log("服务器连接关闭: " + url,e);
          };
          ws.onerror = function() {
            console.log("服务器连接出错: " + url);
          };
          ws.onmessage = function(e) {
            //接收服务器返回的数据
            let resData = JSON.parse(e.data)
            console.log('接收数据',resData)
            // _this.list = [
            //   ..._this.list,
            //   { userId: resData.userId, username: resData.username, avatar: resData.avatar, content: resData.msg }
            // ];
            _this.list.push(resData)
            console.log('消息列表',_this.list)
          };
        }
      },
      // 滚动条到底部
      scrollBottm() {
          let el = this.$refs["msg-box"];
          el.scrollTop = el.scrollHeight;
      }
    }
  };
</script>

<style lang="scss" scoped>
.chat-container {
  width: 1278px;
  min-height: 610px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  border-radius: 12px;
  padding: 20px;
  margin: 0 auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chat-header {
  background: #113056;
  color: white;
  padding: 15px 20px;
  border-radius: 8px 8px 0 0;
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
}

.chat-body {
  background: #fff;
  border-radius: 0 0 8px 8px;
  display: flex;
  flex-direction: column;
  min-height: 500px;
}

.msg-box {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  .msg {
    margin-bottom: 15px;
    display: flex;
    &:last-child {
      margin-bottom: 0;
    }
  }
  .user-msg {
    max-width: 70%;
    word-break: break-all;
    .msg-left {
      background: #f5f7fa;
      color: #333;
      border-radius: 18px 18px 18px 4px;
      align-self: flex-start;
    }
    .msg-right {
      background: #113056;
      color: white;
      border-radius: 18px 18px 4px 18px;
      align-self: flex-end;
    }
    span {
      display: inline-block;
      padding: 10px 16px;
      font-size: 14px;
      animation: messageSlide 0.3s ease-out;
    }
  }
}

// 半常驻输入区域
.input-box {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  background: #fff;
  border-radius: 0 0 8px 8px;
  display: flex;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  input {
    flex: 1;
    height: 40px;
    padding: 0 15px;
    border: 1px solid #dcdfe6;
    border-radius: 20px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.3s;
    &:focus {
      border-color: #91CFD5;
    }
  }
  .btn {
    height: 40px;
    min-width: 80px;
    background: #e0e0e0;
    color: #606266;
    border: none;
    border-radius: 20px;
    font-size: 14px;
    margin-left: 10px;
    cursor: not-allowed;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .btn-active {
    background: #113056;
    color: white;
    cursor: pointer;
    &:hover {
      background: #1a467a;
    }
  }
}

// 消息动画
@keyframes messageSlide {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 滚动条样式
.msg-box::-webkit-scrollbar {
  width: 6px;
}

.msg-box::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.msg-box::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
  &:hover {
    background: #a8a8a8;
  }
}
</style>
