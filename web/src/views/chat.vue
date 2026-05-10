<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 左侧会话列表 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h3>💬 消息列表</h3>
        </div>
        <div class="conversation-list" v-if="conversations.length > 0">
          <div
            v-for="(conv, index) in conversations"
            :key="index"
            class="conversation-item"
            :class="{ active: currentConv === conv.conversationId }"
            @click="switchConversation(conv)"
          >
            <div class="conv-avatar">{{ (conv.otherUserNickname || conv.otherUserId)?.charAt(0) || '?' }}</div>
            <div class="conv-info">
              <div class="conv-user">{{ conv.otherUserNickname || conv.otherUserId || '未知用户' }}</div>
              <div class="conv-preview">{{ conv.lastMessage || '' }}</div>
            </div>
            <div class="conv-time">{{ formatTime(conv.lastTime) }}</div>
          </div>
        </div>
        <div class="empty-conversations" v-else>
          <p>暂无消息</p>
          <p class="hint">在文章列表中点击"私信"开始聊天</p>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-main">
        <div class="chat-header" v-if="currentOtherUserId">
          <h2>与 {{ currentOtherUserNickname || currentOtherUserId }} 聊天中</h2>
        </div>
        <div class="chat-header empty-header" v-else>
          <h2>选择一个会话开始聊天</h2>
        </div>

        <div class="chat-body" ref="msgBox" v-if="currentOtherUserId">
          <div class="msg-box">
            <div
              v-for="(msg, index) in messageList"
              :key="index"
              class="msg"
            >
              <div class="user-msg" :class="msg.fromId == myUserId ? 'self' : 'other'">
                <div class="msg-avatar">{{ msg.fromId == myUserId ? '我' : (msg.fromId?.charAt(0) || '?') }}</div>
                <div class="msg-content-wrapper">
                  <div class="msg-bubble">{{ msg.messageContent }}</div>
                  <div class="msg-time">{{ formatTime(msg.createdTime) }}</div>
                </div>
              </div>
            </div>
            <div class="msg-loading" v-if="messageList.length === 0">
              <p>暂无消息，发送第一条消息吧 👋</p>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-box">
            <input
              type="text"
              ref="sendMsg"
              v-model="contentText"
              @keyup.enter="sendText()"
              placeholder="输入消息..."
            />
            <div
              class="btn"
              :class="{ 'btn-active': contentText }"
              @click="sendText()"
            >
              发送
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        ws: null,
        messageList: [],
        contentText: "",
        userId: 0,
        myUserId: 0,
        conversations: [],
        currentConv: null,
        currentOtherUserId: null,
        currentOtherUserNickname: '',
      };
    },
    mounted() {
        console.log('路由参数', this.$route.params)
        this.userId = this.$route.params.userId || 0;
        this.myUserId = localStorage.getItem('user') || '';

        // 加载会话列表
        this.loadConversations();

        // 如果路由传了 userId，直接打开与该用户的聊天
        if (this.userId) {
          this.currentOtherUserId = String(this.userId);
          this.loadMessages(this.currentOtherUserId);
        }

        // 建立 WebSocket 连接
        this.initWebSocket();
    },
    destroyed() {
      if (this.ws) {
        this.ws.close();
      }
    },
    methods: {
      // 加载会话列表
      async loadConversations() {
        try {
          const res = await this.$axios.get('/apis/message/conversations', {
            withCredentials: true
          });
          if (res.data && res.data.code == 0) {
            this.conversations = res.data.data || [];
          }
        } catch (e) {
          console.log('加载会话列表失败', e);
        }
      },

      // 切换会话
      switchConversation(conv) {
        this.currentConv = conv.conversationId;
        this.currentOtherUserId = conv.otherUserId;
        this.currentOtherUserNickname = conv.otherUserNickname || conv.otherUserId;
        this.loadMessages(this.currentOtherUserId);
      },

      // 加载与某用户的聊天记录
      async loadMessages(otherUserId) {
        try {
          const res = await this.$axios.get('/apis/message/conversation', {
            params: { otherUserId: otherUserId },
            withCredentials: true
          });
          if (res.data && res.data.code == 0) {
            this.messageList = res.data.data.messages || [];
            if (res.data.data.otherUserNickname) {
              this.currentOtherUserNickname = res.data.data.otherUserNickname;
            }
            this.$nextTick(() => {
              this.scrollBottom();
            });
          }
        } catch (e) {
          console.log('加载消息失败', e);
        }
      },

      // 发送聊天信息
      sendText() {
        let _this = this;
        if (!_this.contentText) {
          return;
        }
        if (!_this.currentOtherUserId) {
          _this.$message.warning('请先选择一个会话');
          return;
        }
        let params = {
          toId: _this.currentOtherUserId,
          messageContent: _this.contentText,
          fromId: _this.myUserId,
        };

        if (_this.ws && _this.ws.readyState === WebSocket.OPEN) {
          _this.ws.send(JSON.stringify(params));
        } else {
          // WebSocket 未连接，通过 HTTP 发送
          _this.sendViaHttp(params);
        }

        // 乐观添加到列表
        params.createdTime = new Date().toISOString();
        _this.messageList.push({...params});
        _this.contentText = "";
        setTimeout(() => {
          _this.scrollBottom();
        }, 200);
      },

      // 通过 HTTP 发送消息（WebSocket 不可用时的降级方案）
      async sendViaHttp(params) {
        try {
          await this.$axios.post('/apis/message/send', params, {
            withCredentials: true,
            headers: { 'Content-Type': 'application/json;charset=utf-8' }
          });
          // 发送成功后刷新会话列表
          this.loadConversations();
        } catch (e) {
          console.log('发送消息失败', e);
        }
      },

      // 进入页面创建websocket连接
      initWebSocket() {
        let _this = this;
        if (!window.WebSocket) {
          console.log('浏览器不支持WebSocket');
          return;
        }
        let url = `ws://localhost:8081/chat/${_this.myUserId}`;
        try {
          let ws = new WebSocket(url);
          _this.ws = ws;

          ws.onopen = function() {
            console.log("WebSocket连接成功: " + url);
          };

          ws.onclose = function() {
            console.log("WebSocket连接关闭");
            _this.ws = null;
          };

          ws.onerror = function() {
            console.log("WebSocket连接出错");
            _this.ws = null;
          };

          ws.onmessage = function(e) {
            try {
              let resData = JSON.parse(e.data);
              console.log('收到消息', resData);

              // 如果当前正在和该用户聊天，追加到列表
              if (resData.fromId == _this.currentOtherUserId ||
                  (resData.toId == _this.currentOtherUserId && resData.fromId == _this.myUserId)) {
                // 检查是否已存在（避免重复）
                let exists = _this.messageList.some(m =>
                  m.messageContent == resData.messageContent &&
                  m.fromId == resData.fromId &&
                  Math.abs(new Date(m.createdTime) - new Date(resData.createdTime)) < 2000
                );
                if (!exists) {
                  _this.messageList.push(resData);
                  _this.$nextTick(() => {
                    _this.scrollBottom();
                  });
                }
              }

              // 刷新会话列表（新消息可能来自其他人）
              _this.loadConversations();
            } catch (e) {
              console.log('解析消息失败', e);
            }
          };
        } catch (e) {
          console.log('WebSocket初始化失败', e);
        }
      },

      // 滚动条到底部
      scrollBottom() {
        let el = this.$refs["msgBox"];
        if (el) {
          setTimeout(() => {
            el.scrollTop = el.scrollHeight;
          }, 100);
        }
      },

      // 格式化时间
      formatTime(timeStr) {
        if (!timeStr) return '';
        try {
          let date = new Date(timeStr);
          if (isNaN(date.getTime())) return '';
          let now = new Date();
          let diff = now - date;
          // 今天显示时分
          if (diff < 86400000 && date.getDate() === now.getDate()) {
            return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
          }
          // 昨天显示"昨天"
          let yesterday = new Date(now);
          yesterday.setDate(yesterday.getDate() - 1);
          if (date.getDate() === yesterday.getDate() &&
              date.getMonth() === yesterday.getMonth() &&
              date.getFullYear() === yesterday.getFullYear()) {
            return `昨天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
          }
          // 更早显示日期
          return `${date.getMonth()+1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
        } catch (e) {
          return '';
        }
      }
    }
  };
</script>

<style lang="scss" scoped>
.chat-page {
  width: 1278px;
  margin: 0 auto;
  padding: 20px 0;
}

.chat-container {
  display: flex;
  height: 650px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 左侧会话列表 */
.chat-sidebar {
  width: 320px;
  background: #f8f9fb;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 18px 20px;
  border-bottom: 1px solid #eee;
  h3 {
    margin: 0;
    font-size: 16px;
    color: #333;
  }
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;

  &:hover {
    background: #eef1f5;
  }

  &.active {
    background: #e3e8f0;
  }
}

.conv-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #113056, #1a467a);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  flex-shrink: 0;
}

.conv-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.conv-user {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.conv-preview {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-time {
  font-size: 11px;
  color: #bbb;
  flex-shrink: 0;
  margin-left: 8px;
}

.empty-conversations {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  p {
    margin: 4px 0;
    font-size: 14px;
  }
  .hint {
    font-size: 12px;
    color: #bbb;
  }
}

/* 右侧聊天区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background: #113056;
  color: white;
  padding: 15px 24px;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }

  &.empty-header {
    background: #f5f7fa;
    h2 {
      color: #999;
      font-weight: 400;
    }
  }
}

.chat-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.msg-box {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f7fa;
}

.msg {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.user-msg {
  display: flex;
  align-items: flex-end;
  max-width: 80%;

  &.self {
    margin-left: auto;
    flex-direction: row-reverse;

    .msg-avatar {
      margin-left: 10px;
      margin-right: 0;
      background: #1a467a;
    }

    .msg-bubble {
      background: #113056;
      color: white;
      border-radius: 18px 18px 4px 18px;
    }

    .msg-time {
      text-align: right;
    }
  }

  &.other {
    .msg-avatar {
      margin-right: 10px;
      background: #7c8ea0;
    }

    .msg-bubble {
      background: white;
      color: #333;
      border-radius: 18px 18px 18px 4px;
      box-shadow: 0 1px 2px rgba(0,0,0,0.06);
    }
  }
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
  flex-shrink: 0;
}

.msg-content-wrapper {
  max-width: calc(100% - 50px);
}

.msg-bubble {
  display: inline-block;
  padding: 10px 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
  animation: messageSlide 0.3s ease-out;
}

.msg-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  padding: 0 4px;
}

.msg-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-size: 14px;
}

/* 输入区域 */
.input-box {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  background: #fff;
  display: flex;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);

  input {
    flex: 1;
    height: 42px;
    padding: 0 16px;
    border: 1px solid #dcdfe6;
    border-radius: 21px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.3s;

    &:focus {
      border-color: #113056;
    }
  }

  .btn {
    height: 42px;
    min-width: 80px;
    background: #e0e0e0;
    color: #606266;
    border: none;
    border-radius: 21px;
    font-size: 14px;
    margin-left: 10px;
    cursor: not-allowed;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;

    &.btn-active {
      background: #113056;
      color: white;
      cursor: pointer;

      &:hover {
        background: #1a467a;
      }
    }
  }
}

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

.msg-box::-webkit-scrollbar,
.conversation-list::-webkit-scrollbar {
  width: 6px;
}

.msg-box::-webkit-scrollbar-track,
.conversation-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.msg-box::-webkit-scrollbar-thumb,
.conversation-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;

  &:hover {
    background: #a8a8a8;
  }
}
</style>
