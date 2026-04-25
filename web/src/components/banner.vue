<template>
  <div id="banner">
    <div class="banner-container">
      <div class="logo" @click="goTlq()">
        <img src="../assets/buaa.png" alt="北京航空航天大学" />
        <p>社区</p>
      </div>

      <div class="tlq" @click="goTlq()">
        <i class="el-icon-chat-dot-square"></i>
        讨论区
      </div>

      <div class="search-box">
        <el-input
          v-model="inputSearch"
          prefix-icon="el-icon-search"
          @keyup.enter.native="onEnterSearch"
          placeholder="搜索内容..."
          class="search-input"
        >
        </el-input>
        <el-button type="primary" class="search-btn" @click="onEnterSearch()">
          搜索
        </el-button>
      </div>

      <div class="actions">
        <el-button round @click="check()" class="action-btn school-btn">
          <i class="el-icon-s-home"></i> 切换学校
        </el-button>

        <el-button round type="primary" class="action-btn write-btn" @click="goWrite()">
          <i class="el-icon-edit"></i>写文章
        </el-button>

        <el-avatar
          :size="36"
          :src="imgSrc"
          class="user-avatar"
          @click.native="goPersonal()"
        ></el-avatar>

        <div class="auth-links" v-if="display == true">
          <span @click="goRes()">注册</span>
          <span class="divider">|</span>
          <span @click="goLogin()">登录</span>
        </div>

        <div class="user-info" v-else @click="goPersonal()">
          <el-badge :value="store ? 1 : 0" class="item">
            <span class="nickname">个人中心</span>
          </el-badge>
          <span class="logout" @click.stop="goReturn()">退出</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Banner",
  components: {},
  data() {
    return {
      display: true,
      activeIndex: "1",
      inputSearch: "",
      imgSrc: require("@/assets/img1.webp"),
      form: {
        region: "",
      },
      formLabelWidth: "120px",
      hasRed:false,
      store: null
    };
  },
  watch: {
    $store : {
      handler: function(pre) {
        console.log('红点变了')
        console.log('红点',pre)
        this.store = pre
      },
      deep: true
    }
  },
  mounted() {
    this.refreshLoginStatus();
  },
  created() {
    this.activeIndex = this.$route.query.url;
    this.store = this.$store.state.type.isRed // 获取状态值
    //console.log('状态',this.$store.state.type.isRed)
  },
  // created () {
    
  // },
  methods: {
    // handleSelect(key) {
    //   console.log(key);
    // },
    // toLink(data) {
    //   console.log(data);
    //   this.$router.push({ name: data.url, query: { url: data.index } });
    // },
    handleClose(done) {
      this.$confirm("确认关闭？")
        .then(() => {
          done();
          // this.$router.push({ name: "writeArticle" });
        })
        .catch(() => {});
    },
    clearLocalLogin() {
      localStorage.removeItem("user");
      localStorage.removeItem("password");
      localStorage.removeItem("myuserId");
      this.display = true;
    },
    refreshLoginStatus() {
      const _self = this;
      _self.$axios
        .get("/apis/userInfo")
        .then((res) => {
          if (
            res.status === 200 &&
            res.data &&
            res.data.code === 0 &&
            res.data.data &&
            res.data.data.userId
          ) {
            localStorage.setItem("user", res.data.data.userId);
            _self.display = false;
          } else {
            _self.clearLocalLogin();
          }
        })
        .catch(() => {
          _self.clearLocalLogin();
        });
    },
    goReturn() {
      const _self = this;
      _self.$axios
        .get("/apis/logout")
        .catch(() => {})
        .finally(() => {
          _self.$cookies.remove("token");
          _self.clearLocalLogin();
          _self.$router.push({ name: "index" });
        });
    },
    goPersonal() {
      // console.log(111);
      this.$router.push({ name: "personal" });
    },
    goTlq() {
      this.$router.push({ name: "index" });
    },
    check() {
      this.$router.push({ name: "checkout" });
    },
    goWrite() {
      this.$router.push({ name: "writeArticle" });
    },
    goLogin() {
      this.$router.push({ name: "login" });
    },
    goRes() {
      this.$router.push({ name: "register" });
    },
    research() {
      this.$router.push({ name: "searchDetail" });
      console.log("search:" + this.inputSearch);
      // const inputSearch = this.inputSearch;
      // if (inputSearch) {
      //   this.$http
      //     .post("/api/路径")
      //     .then((res) => {
      //       console.log(res.data);
      //       this.piles = res.data;
      //     })
      //     .catch((err) => {
      //       console.log(err);
      //     });
      // }
    },
    onEnterSearch() {
     
      const inputSearch = this.inputSearch;
      var _self = this;
      
      var url = "/apis/article/search";

      _self.$axios
        .get(url, { params: { keyword: inputSearch } })
        .then((res) => {
          console.log("搜索的数据", res);
          this.$router.push({
            name:'searchDetail',
            params: {
              search:res.data.data
            }
          })
        })
        .catch((err) => {
          console.log(err);
        });   
    },
  },
};
</script>

<style lang="scss" scoped>
#banner {
  height: 80px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
  margin: 0;
  padding: 0;
  border: 0;
  width: 100%;
  left: 0;
  right: 0;
}

.banner-container {
  width: 95%;
  max-width: 1400px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 30px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex-shrink: 0;

  img {
    width: 50px;
    height: 50px;
    border-radius: 10px;
    transition: transform 0.3s;
    margin-right: 10px;
  }

  &:hover img {
    transform: scale(1.05);
  }

  p {
    font-size: 22px;
    color: #113056;
    font-weight: 600;
    letter-spacing: 1px;
  }
}

.tlq {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  transition: color 0.3s;
  flex-shrink: 0;
  padding: 8px 16px;
  border-radius: 20px;

  i {
    font-size: 18px;
  }

  &:hover {
    color: #113056;
    background: rgba(17, 48, 86, 0.08);
  }
}

.search-box {
  flex: 1;
  max-width: 500px;
  display: flex;
  align-items: center;
  gap: 10px;

  .search-input {
    flex: 1;

    ::v-deep .el-input__inner {
      border-radius: 20px;
      height: 40px;
      line-height: 40px;
      padding: 0 20px 0 40px;
      border: 1px solid #DCDFE6;
      transition: all 0.3s;

      &:focus {
        border-color: #113056;
        box-shadow: 0 0 0 2px rgba(17, 48, 86, 0.1);
      }

      &::placeholder {
        color: #C0C4CC;
      }
    }

    ::v-deep .el-input__prefix {
      left: 12px;

      i {
        font-size: 16px;
        color: #909399;
      }
    }
  }

  .search-btn {
    flex-shrink: 0;
    border-radius: 20px;
    padding: 10px 20px;
    background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
    border: none;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(17, 48, 86, 0.4);
    }
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-shrink: 0;
}

.action-btn {
  border-radius: 20px;
  padding: 10px 18px;
  font-size: 14px;
  transition: all 0.3s;

  &.school-btn {
    background: #fff;
    border: 1px solid #DCDFE6;
    color: #606266;

    &:hover {
      border-color: #113056;
      color: #113056;
      background: rgba(17, 48, 86, 0.05);
    }
  }

  &.write-btn {
    background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
    border: none;
    color: #fff;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(17, 48, 86, 0.4);
    }
  }
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.3s;
  border: 2px solid #f0f0f0;

  &:hover {
    transform: scale(1.1);
    border-color: #113056;
  }
}

.auth-links {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;

  span {
    color: #606266;
    cursor: pointer;
    transition: color 0.3s;
    padding: 4px 8px;
    border-radius: 4px;

    &:hover {
      color: #113056;
      background: rgba(17, 48, 86, 0.08);
    }

    &.divider {
      color: #DCDFE6;
      cursor: default;
      padding: 0;

      &:hover {
        color: #DCDFE6;
        background: none;
      }
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;

  .nickname {
    font-size: 14px;
    color: #606266;
    padding: 6px 12px;
    border-radius: 16px;
    transition: all 0.3s;

    &:hover {
      background: rgba(64, 158, 255, 0.08);
      color: #409EFF;
    }
  }

  .logout {
    font-size: 14px;
    color: #909399;
    transition: color 0.3s;

    &:hover {
      color: #F56C6C;
    }
  }
}

::v-deep .el-badge__content {
  background: #F56C6C;
  border: none;
}
</style>

