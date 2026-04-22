<template>
  <div id="indexContent">
    <div class="content">
      <div class="article-card" v-for="(item, index) in listData" :key="index" @click="cardClick(item, $event)">
        <div class="card-header">
          <el-avatar
            :size="48"
            :src="item.avatar"
            class="user-avatar"
          ></el-avatar>
          <div class="header-info">
            <h3 class="article-title">{{ item.articleTitle }}</h3>
            <div class="user-meta">
              <span class="username">{{ item.nickname }}</span>
              <span class="time">{{ item.createdTime }}</span>
            </div>
          </div>
        </div>
        
        <div class="card-footer">
          <div class="stats">
            <div class="stat-item">
              <i class="el-icon-chat-line-round"></i>
              <span>{{ item.articleCommentCount }}</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-star-on"></i>
              <span>{{ item.articleLikeCount }}</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-view"></i>
              <span>{{ item.articleViewCount }}</span>
            </div>
          </div>
          
          <div class="actions">
            <el-button
              type="primary"
              :plain="followList.indexOf(String(item.articleUserId)) > -1"
              @click.stop="followList.indexOf(String(item.articleUserId)) > -1 ? noguanzhu(item.articleUserId) : guanzhu(item.articleUserId)"
              class="action-btn"
            >
              {{ followList.indexOf(String(item.articleUserId)) > -1 ? '已关注' : '关注' }}
              <i v-if="followList.indexOf(String(item.articleUserId)) === -1" class="el-icon-plus"></i>
            </el-button>
            <el-button
              type="default"
              @click.stop="message(item.articleUserId)"
              class="action-btn"
            >
              私信
            </el-button>
          </div>
        </div>
      </div>
      <!-- <div class="list"></div>
      <div class="list"></div> -->
      <el-dialog
        title="聊天框"
        :visible.sync="dialogVisible"
        width="80%"
      >
        <div style="position: absolute; left: 0px; top: 0px">与{{chatId}}聊天</div>

        <div class="left">
            <div class="content">

            </div>
            <textarea name="" id="" cols="30" rows="10"></textarea>
        </div>
        <!-- <div class="right">
          <h3>在线好友</h3>
          <li>

          </li>
        </div> -->


        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="dialogVisible = false"
            >确 定</el-button
          >
        </span>
      </el-dialog>
    </div>
  </div>
</template>
<script>
export default {
  name: "indexContent",
  data() {
    return {
      circleUrl: require("@/assets/img1.webp"),
      id: "1",
      display: true,
      chatId: 0,
      listData: [
        {
          articleId: 1,
          articleTitle: "2024考研数学复习计划分享，三个月从基础到强化",
          nickname: "考研达人",
          createdTime: "2024-04-15 10:30",
          articleCommentCount: 45,
          articleLikeCount: 128,
          articleViewCount: 1560,
          articleUserId: 101,
          avatar: "https://randomuser.me/api/portraits/men/32.jpg"
        },
        {
          articleId: 2,
          articleTitle: "阿里巴巴前端面试经验分享，附面经和答案",
          nickname: "前端小能手",
          createdTime: "2024-04-14 15:45",
          articleCommentCount: 67,
          articleLikeCount: 234,
          articleViewCount: 2890,
          articleUserId: 102,
          avatar: "https://randomuser.me/api/portraits/women/44.jpg"
        },
        {
          articleId: 3,
          articleTitle: "大学英语四级备考攻略，一个月轻松过425",
          nickname: "英语学霸",
          createdTime: "2024-04-13 09:20",
          articleCommentCount: 32,
          articleLikeCount: 89,
          articleViewCount: 987,
          articleUserId: 103,
          avatar: "https://randomuser.me/api/portraits/men/55.jpg"
        },
        {
          articleId: 4,
          articleTitle: "寻物启事：丢失苹果AirPods Pro，酬谢200元",
          nickname: "失主小明",
          createdTime: "2024-04-12 18:30",
          articleCommentCount: 15,
          articleLikeCount: 23,
          articleViewCount: 456,
          articleUserId: 104,
          avatar: "https://randomuser.me/api/portraits/women/22.jpg"
        },
        {
          articleId: 5,
          articleTitle: "拼单：星巴克咖啡豆，5人成团享8折",
          nickname: "咖啡爱好者",
          createdTime: "2024-04-11 14:20",
          articleCommentCount: 8,
          articleLikeCount: 15,
          articleViewCount: 234,
          articleUserId: 105,
          avatar: "https://randomuser.me/api/portraits/men/67.jpg"
        },
        {
          articleId: 6,
          articleTitle: "表白墙：感谢图书馆三楼的那个她，每次都帮我占座",
          nickname: "匿名同学",
          createdTime: "2024-04-10 20:15",
          articleCommentCount: 56,
          articleLikeCount: 189,
          articleViewCount: 1234,
          articleUserId: 106,
          avatar: "https://randomuser.me/api/portraits/men/88.jpg"
        },
        {
          articleId: 7,
          articleTitle: "生活趣事：今天在食堂遇到了辅导员，他居然在排队买奶茶",
          nickname: "校园小记者",
          createdTime: "2024-04-09 11:45",
          articleCommentCount: 28,
          articleLikeCount: 76,
          articleViewCount: 876,
          articleUserId: 107,
          avatar: "https://randomuser.me/api/portraits/women/55.jpg"
        },
        {
          articleId: 8,
          articleTitle: "竞赛组队：全国大学生数学建模竞赛，寻找队友",
          nickname: "竞赛队长",
          createdTime: "2024-04-08 16:30",
          articleCommentCount: 34,
          articleLikeCount: 56,
          articleViewCount: 678,
          articleUserId: 108,
          avatar: "https://randomuser.me/api/portraits/men/22.jpg"
        }
      ],
      followList: [],
      hasRed: false,
      dialogVisible:false,
    };
  },
  mounted() {
    this.getData();
    this.guanzhuList();

    setInterval(() => {
      this.isRed();
    }, 3000);
    //console.log('全局红点',this.$red)
  },
  methods: {
    message(id) {
      this.dialogVisible = true
      this.chatId = id

      // this.websocket = new WebSocket(`ws://localhost:8081/${id}`);
      // this.websock.onopen = this.websocketonopen;
      // this.websock.onerror = this.websocketonerror;
      // this.websock.onmessage = this.websocketonmessage;
      // this.websock.onclose = this.websocketclose;
      let _self = this;
      this.$router.push({
        name:'chat',
        params: {
          userId: _self.chatId,
        }
      })

    },
    websocketonopen: function () {
                console.log("WebSocket连接成功");
              },
    websocketonerror: function (e) {
      console.log("WebSocket连接发生错误",e);
    },
    websocketonmessage: function (e) {
      var da = JSON.parse(e.data);
      console.log(da);
      this.message = da;
    },
    websocketclose: function (e) {
      console.log("connection closed (" + e.code + ")");
    },
    isRed() {
      //console.log(777)
      var _self = this;
      var url = "/apis/hasReadNotice";
      _self.$axios
        .get(url)
        .then((res) => {
          if (res.status == 200) {
            //console.log("红点问题", res.data);
            _self.$store.amendType({ type: "isRed", blo: res.data.data });
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    guanzhuList() {
      var _self = this;
      var url = "/apis/follow/list";
      //var categoryId = this.id;

      _self.$axios
        .get(url)
        .then((res) => {
          if (res.status == 200) {
            console.log("关注列表", res.data);
            let args = res.data.data;
            let list = [];
            args.forEach((item) => {
              list.push(item.userId);
            });
            _self.followList = list;
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    guanzhu(userId) {
      this.display = !this.display;
      //console.log(userId);
      var _self = this;
      var url = "/apis/add/follow";
      //var categoryId = this.id;

      _self.$axios
        .get(url, { params: { followId: userId } })
        .then((res) => {
          console.log(res.data);
          if (res.status == 200) {
            if (res.data.code == 0) {
              //console.log(res.data.data.articleTitle);
              _self.guanzhuList();
              // let da = res.data.data;
              // _self.listData = da;
            }
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    noguanzhu(userId) {
      this.display = !this.display;
      //console.log(userId);
      var _self = this;
      var url = "/apis/cancel/follow";
      //var categoryId = this.id;

      _self.$axios
        .get(url, { params: { followId: userId } })
        .then((res) => {
          console.log(res.data);
          if (res.status == 200) {
            if (res.data.code == 0) {
              _self.guanzhuList();
              //console.log(res.data.data.articleTitle);
              // let da = res.data.data;
              // _self.listData = da;
            }
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    getData() {
      var _self = this;
      var url = "/apis/category/";
      var categoryId = this.id;
      // console.log(categoryId, "ipppppppppppp");
      _self.$axios
        .get(url + categoryId, { params: { categoryId: categoryId } })
        .then((res) => {
          console.log(res.data);
          if (res.status == 200) {
            if (res.data.code == 0) {
              //console.log(res.data.data.articleTitle);
              let da = res.data.data;
              _self.listData = da;
            }
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    goDetail(num) {
      // localStorage.setItem("articleId", num.articleId);
      this.$router.push({ name: "Detail", query: { url: num.articleId } });
    },
    cardClick(item, event) {
      // 确保点击的不是按钮或其他需要独立处理的元素
      const target = event.target;
      if (!target.closest('.action-btn') && !target.closest('.el-button')) {
        this.goDetail(item);
      }
    },
  },
};
</script>
<style scoped lang="scss">
#indexContent {
  width: 100%;

  .content {
    width: 96%;
    min-height: 500px;
    margin: 20px auto 0;
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    .article-card {
      background: #fafafa;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 20px;
      border: 1px solid #f0f0f0;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        background: #f0f7ff;
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(64, 158, 255, 0.15);
        border-color: #e6f7ff;
      }

      .card-header {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        margin-bottom: 16px;

        .user-avatar {
          flex-shrink: 0;
          cursor: pointer;
          transition: transform 0.3s ease;

          &:hover {
            transform: scale(1.05);
          }
        }

        .header-info {
          flex: 1;
          min-width: 0;

          .article-title {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
            margin: 0 0 8px 0;
            cursor: pointer;
            transition: color 0.3s ease;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;

            &:hover {
              color: #113056;
            }
          }

          .user-meta {
            display: flex;
            align-items: center;
            gap: 16px;

            .username {
              font-size: 14px;
              color: #606266;
              cursor: pointer;
              transition: color 0.3s ease;

              &:hover {
                color: #113056;
              }
            }

            .time {
              font-size: 13px;
              color: #909399;
            }
          }
        }
      }

      .card-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding-top: 16px;
        border-top: 1px solid #f0f0f0;

        .stats {
          display: flex;
          align-items: center;
          gap: 24px;

          .stat-item {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 13px;
              color: #909399;
              transition: color 0.3s ease;

              i {
                font-size: 14px;
              }

              &:hover {
                color: #113056;
              }
            }
        }

        .actions {
          display: flex;
          align-items: center;
          gap: 12px;

          .action-btn {
            font-size: 13px;
            padding: 6px 16px;
            border-radius: 20px;
            transition: all 0.3s ease;
            cursor: pointer;

            &:hover {
              transform: translateY(-2px);
            }
          }
        }
      }
    }
  }
}

::v-deep .el-button {
  display: inline-block;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
  border: none;
  color: #fff;
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  transition: all 0.3s;
  font-weight: 500;
  font-size: 13px;
  border-radius: 20px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(17, 48, 86, 0.4);
  }

  &.is-plain {
    background: #fff;
    border: 1px solid #dcdfe6;
    color: #606266;

    &:hover {
      border-color: #113056;
      color: #113056;
      background: #f0f8ff;
    }
  }
}

::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;

  .el-dialog__header {
    padding: 20px;
    background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);

    .el-dialog__title {
      color: #fff;
      font-weight: 600;
    }
  }
}
</style>
