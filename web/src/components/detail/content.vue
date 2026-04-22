<template>
  <div class="content">
    <div class="main-container">
      <div class="left">
        <!-- 文章标题 -->
        <div class="article-header">
          <h1 class="article-title">
            <i class="el-icon-chat-line-round"></i>
            {{ listData.articleTitle }}
          </h1>
          
          <!-- 操作按钮 -->
          <div class="article-actions">
            <el-button @click="changeArt1" class="action-btn">编辑文章</el-button>
            <el-button @click="deleteArt" class="action-btn delete-btn">删除文章</el-button>
          </div>
        </div>
        
        <!-- 用户信息 -->
        <div class="user-info">
          <el-avatar :size="55" :src="listData.avatar" class="user-avatar"></el-avatar>
          <div class="user-meta">
            <span class="username">{{ listData.nickname }}</span>
            <span class="publish-time">编辑于 {{ listData.createdTime }}</span>
          </div>
          <div class="article-stats">
            <div class="stat-item">
              <i class="el-icon-star-on"></i>
              <span>{{ listData.articleLikeCount }}</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <i class="el-icon-chat-line-round"></i>
              <span>{{ listData.articleCommentCount }}</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <i class="el-icon-view"></i>
              <span>{{ listData.articleViewCount }}</span>
            </div>
          </div>
        </div>
        
        <!-- 文章内容 -->
        <div class="article-content">
          {{ listData.articleContent }}
        </div>
        
        <!-- 互动按钮 -->
        <div class="interaction-buttons">
          <el-button type="primary" plain @click="zan()" class="interaction-btn like-btn">
            <i class="el-icon-star-on"></i> 点赞 {{ listData.articleLikeCount }}
          </el-button>
          <el-button type="default" plain @click="nozan()" class="interaction-btn">
            <i class="el-icon-star-off"></i> 取消点赞
          </el-button>
          <div class="interaction-btn reply-btn" @click="reply">
            <i class="el-icon-chat-line-round"></i>
            <span>回复</span>
          </div>
        </div>
      </div>
      
      <!-- 侧边栏 -->
      <div class="right">
        <div class="sidebar-section">
          <div class="section-title">实时热点</div>
          <div class="hot-list">
            <div 
              class="hot-item" 
              v-for="(item, index) in hotData" 
              :key="index" 
              @click="goHot(item.articleId)"
            >
              <div class="hot-rank">{{ index + 1 }}</div>
              <div class="hot-content">
                <p class="hot-title">{{ item.articleTitle }}</p>
                <p class="hot-stats">{{ item.articleViewCount }}次浏览</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评论区 -->
    <div class="comment-section">
      <h2 class="comment-title">评论区</h2>
      <div class="comment-list">
        <div class="comment-item" v-for="(item, index) in commentList" :key="index">
          <div class="comment-content">{{ item.commentContent }}</div>
          <div class="comment-meta">
            <span class="comment-time">{{ item.commentCreatedTime }}</span>
          </div>
        </div>
        <div v-if="commentList.length === 0" class="no-comments">
          暂无评论，快来发表第一条评论吧！
        </div>
      </div>
    </div>
    
    <!-- 评论对话框 -->
    <el-dialog 
      title="发表评论" 
      :visible.sync="dialogFormVisible" 
      width="500px"
      custom-class="comment-dialog"
    >
      <textarea 
        v-model="textarea" 
        placeholder="发表你的评论" 
        class="comment-textarea"
      ></textarea>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="submitComment" class="submit-btn">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: "Content",
  data() {
    return {
      imgSrc: require("@/assets/img1.webp"),
      artId: "",
      listData: {},
      hotData: [],
      display: true,
      dialogFormVisible: false,
      textarea: '',
      commentList: [],
    };
  },
  mounted() {
    this.getData();
    this.getHotPot();
    this.getComment();
  },
  created() {
    this.artId = this.$route.query.url;
    console.log(this.artId, "22222");
  },
  methods: {
    goHot(articleId) {
      this.getData(articleId);
    },
    nozan() {
      var _self = this;
      var articleId = parseInt(_self.artId);
      var url = "/apis/dislike";

      _self.$axios
        .get(url, { params: { articleId: articleId } })
        .then((res) => {
          if(res.data.msg == 'success') {
            this.$message.success('取消成功');
            _self.listData.articleLikeCount = res.data.data;
          } else {
            this.$message.fail('没有权限');
          }
        })
        .catch((err) => {
          console.log(err);
        });   
    },
    zan() {
      var _self = this;
      var articleId = parseInt(_self.artId);
      var url = "/apis/like";

      _self.$axios
        .get(url, { params: { articleId: articleId } })
        .then((res) => {
          if(res.data.msg == 'success') {
            this.$message.success('点赞成功');
            _self.listData.articleLikeCount = res.data.data;
          } else {
            this.$message.fail('没有权限');
          }
        })
        .catch((err) => {
          console.log(err);
        });   
    },
    reply() {
      this.dialogFormVisible = true;
    },
    changeArt1() {
      var _self = this;
      var articleId = parseInt(_self.artId);
      var url = "/apis/article/can/edit";

      _self.$axios
        .get(url, { params: { articleId: articleId } })
        .then((res) => {
          if(res.data.data) {
            this.$router.push({ 
              name: 'writeArticle',
              params:{
                articleTitle: _self.listData.articleTitle,
                articleContent: _self.listData.articleContent,
                id: 132132,
                articleCategoryName: _self.listData.articleCategoryName,
                isEdit: true,
                articleId,
              }   
            });
          } else {
            this.$message.success('没有权限');
          }
        })
        .catch((err) => {
          console.log(err);
        });    
    },
    deleteArt() {
      var _self = this;
      var articleId = parseInt(_self.artId);
      var url = "/apis/article/delete";

      _self.$axios
        .get(url, { params: { articleId: articleId } })
        .then((res) => {
          if(res.data.msg == 'success') {
            this.$message.success('删除成功');
            this.$router.push('/index');
          } else {
            this.$message.fail('没有权限');
          }
        })
        .catch((err) => {
          console.log(err);
        });      
    },
    submitComment() {
      var _self = this;
      var articleId = parseInt(_self.artId);
      var url = "/apis/insert/comment";
      let params = `articleId=${articleId}&content=${_self.textarea}`;
      _self.$axios
        .post(url, params, {
          headers: {
            'content-type': 'application/x-www-form-urlencoded'
          }
        })
        .then(() => {
          this.dialogFormVisible = false;
          this.textarea = '';
          this.$message.success('评论成功');
          this.getComment();
        })
        .catch((err) => {
          console.log(err);
        });
    },
    getData(id) {
      var _self = this;
      var url = "/apis/detail/";
      let articleId = id || _self.artId;
      
      console.log(articleId, "iddddd");
      _self.$axios
        .get(url + articleId, { params: { articleId: articleId } })
        .then((res) => {
          console.log("detail的数据", res.data);
          if (res.status == 200) {
            if (res.data.code == 0) {
              let da = res.data.data;
              _self.listData = da;
            }
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    getComment() {
      var _self = this;
      var url = "/apis/comment/list";
      var articleId = parseInt(_self.artId);
      _self.$axios
        .get(url, {
          params: {
            articleId,
          }
        })
        .then((res) => {
          console.log('评论区', res.data);
          _self.commentList = res.data.data;
        })
        .catch((err) => {
          console.log(err);
        });
    },
    getHotPot() {
      var _self = this;
      var url = "/apis/article/hotspot";
      _self.$axios
        .get(url)
        .then((res) => {
          console.log(res.data);
          if (res.status == 200) {
            if (res.data.code == 0) {
              let da = res.data.data;
              _self.hotData = da;
            }
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
  },
};
</script>
<style lang="scss" scoped>
.content {
  width: 1100px;
  margin: 30px auto;
  
  .main-container {
    display: flex;
    justify-content: space-between;
    margin-bottom: 40px;
    
    .left {
      width: 69%;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      padding: 30px;
      
      // 文章标题
      .article-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 24px;
        
        .article-title {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          line-height: 1.3;
          margin: 0;
          flex: 1;
          
          i {
            color: #113056;
            margin-right: 12px;
            font-size: 20px;
          }
        }
        
        .article-actions {
          display: flex;
          gap: 12px;
          
          .action-btn {
            padding: 6px 16px;
            font-size: 14px;
            border-radius: 4px;
            transition: all 0.3s ease;
            
            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }
            
            &.delete-btn {
              color: #f56c6c;
              border-color: #f56c6c;
              
              &:hover {
                background: rgba(245, 108, 108, 0.1);
              }
            }
          }
        }
      }
      
      // 用户信息
      .user-info {
        display: flex;
        align-items: center;
        padding-bottom: 20px;
        border-bottom: 1px solid #ebeef5;
        margin-bottom: 24px;
        
        .user-avatar {
          margin-right: 16px;
          border: 2px solid #f0f0f0;
        }
        
        .user-meta {
          flex: 1;
          text-align: left;
          
          .username {
            display: block;
            font-size: 16px;
            font-weight: 500;
            color: #113056;
            margin-bottom: 4px;
          }
          
          .publish-time {
            font-size: 14px;
            color: #909399;
          }
        }
        
        .article-stats {
          display: flex;
          align-items: center;
          gap: 16px;
          
          .stat-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 14px;
            color: #606266;
            
            i {
              font-size: 16px;
              color: #909399;
            }
          }
          
          .stat-divider {
            width: 1px;
            height: 14px;
            background: #ebeef5;
          }
        }
      }
      
      // 文章内容
      .article-content {
        text-align: left;
        font-size: 16px;
        line-height: 1.6;
        color: #303133;
        margin-bottom: 32px;
        white-space: pre-wrap;
        word-break: break-word;
      }
      
      // 互动按钮
      .interaction-buttons {
        display: flex;
        align-items: center;
        gap: 16px;
        padding-top: 20px;
        border-top: 1px solid #ebeef5;
        
        .interaction-btn {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 8px 16px;
          border-radius: 20px;
          font-size: 14px;
          transition: all 0.3s ease;
          
          &.like-btn {
            color: #113056;
            border-color: #113056;
            
            &:hover {
              background: rgba(17, 48, 86, 0.05);
            }
          }
          
          &.reply-btn {
            cursor: pointer;
            color: #606266;
            
            &:hover {
              color: #113056;
            }
          }
        }
      }
    }
    
    // 侧边栏
    .right {
      width: 29%;
      
      .sidebar-section {
        background: #fff;
        border-radius: 8px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        overflow: hidden;
        
        .section-title {
          height: 60px;
          line-height: 60px;
          padding-left: 20px;
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          border-bottom: 1px solid #ebeef5;
          position: relative;
          
          &:before {
            content: "";
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 4px;
            height: 20px;
            background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
          }
        }
        
        .hot-list {
          padding: 16px 0;
          
          .hot-item {
            display: flex;
            align-items: flex-start;
            padding: 12px 20px;
            cursor: pointer;
            transition: background-color 0.3s ease;
            
            &:hover {
              background: #f7f8f9;
            }
            
            .hot-rank {
              width: 24px;
              height: 24px;
              line-height: 24px;
              text-align: center;
              font-size: 14px;
              font-weight: 600;
              color: #909399;
              margin-right: 12px;
              flex-shrink: 0;
              
              &:nth-child(-n+3) {
                color: #f56c6c;
              }
            }
            
            .hot-content {
              flex: 1;
              
              .hot-title {
                font-size: 14px;
                color: #303133;
                line-height: 1.4;
                margin: 0 0 4px 0;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }
              
              .hot-stats {
                font-size: 12px;
                color: #909399;
                margin: 0;
              }
            }
          }
        }
      }
    }
  }
  
  // 评论区
  .comment-section {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    padding: 30px;
    
    .comment-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 24px 0;
      padding-bottom: 12px;
      border-bottom: 1px solid #ebeef5;
    }
    
    .comment-list {
      .comment-item {
        padding: 20px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:first-child {
          padding-top: 0;
        }
        
        .comment-content {
          font-size: 15px;
          color: #303133;
          line-height: 1.6;
          margin-bottom: 12px;
          text-align: left;
        }
        
        .comment-meta {
          display: flex;
          justify-content: flex-end;
          
          .comment-time {
            font-size: 13px;
            color: #909399;
          }
        }
      }
      
      .no-comments {
        text-align: center;
        padding: 40px 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
}

// 评论对话框样式
.comment-dialog {
  border-radius: 8px;
  overflow: hidden;
  
  ::v-deep .el-dialog__header {
    padding: 20px 24px;
    border-bottom: 1px solid #ebeef5;
  }
  
  ::v-deep .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  
  ::v-deep .el-dialog__body {
    padding: 24px;
  }
  
  ::v-deep .el-dialog__footer {
    padding: 0 24px 24px;
    text-align: right;
  }
}

.comment-textarea {
  width: 100%;
  height: 120px;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: vertical;
  font-size: 14px;
  line-height: 1.5;
  transition: border-color 0.3s ease;
  
  &:focus {
    outline: none;
    border-color: #91CFD5;
    box-shadow: 0 0 0 2px rgba(145, 207, 213, 0.2);
  }
  
  &::placeholder {
    color: #c0c4cc;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  
  .cancel-btn {
    padding: 8px 16px;
    font-size: 14px;
    border-radius: 4px;
    transition: all 0.3s ease;
  }
  
  .submit-btn {
    padding: 8px 16px;
    font-size: 14px;
    border-radius: 4px;
    background: #91CFD5 !important;
    border: none !important;
    color: #fff !important;
    transition: all 0.3s ease;
    
    &:hover {
      background: #7cc5ca !important;
      box-shadow: 0 2px 8px rgba(145, 207, 213, 0.4) !important;
    }
  }
}

// 按钮样式覆盖
::v-deep .el-button {
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

::v-deep .el-button--primary {
  background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
  border: none;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(17, 48, 86, 0.4);
  }
  
  &.is-plain {
    background: #fff;
    color: #113056;
    border-color: #113056;
    
    &:hover {
      background: rgba(17, 48, 86, 0.05);
    }
  }
}
</style>