<template>
  <div id="auth">
    <!-- 返回首页按钮 -->
    <div class="back-home">
      <router-link to="/" class="back-btn">
        <i class="el-icon-arrow-left"></i> 返回首页
      </router-link>
    </div>
    
    <div class="auth-container" :class="{ 'is-register': activeTab === 'register' }">
      <!-- 背景色块 -->
      <div class="background-block" :class="{ 'slide-right': activeTab === 'register' }">
        <div class="welcome login-welcome" v-if="activeTab === 'login'">
          <h2>Hello Friend!</h2>
          <p>去注册一个账号，成为更要的粉丝会员，让我们踏入奇妙的旅途！</p>
          <el-button type="primary" class="switch-btn" @click="switchTab('register')">SIGN UP</el-button>
        </div>
        <div class="welcome register-welcome" v-if="activeTab === 'register'">
          <h2>Welcome Back!</h2>
          <p>已经有账号了啊，去登入账号来进入奇妙世界吧！！</p>
          <el-button type="primary" class="switch-btn" @click="switchTab('login')">SIGN IN</el-button>
        </div>
      </div>
      
      <!-- 表单区块 -->
      <div class="form-block">
        <!-- 登录表单 -->
        <div class="form-content login-form" v-if="activeTab === 'login'">
          <h3>登入账号</h3>
          <div class="third-party">
            <span>选择登录方式或电子邮箱登录</span>
            <div class="icons">
              <a href="http://38617112yi.zicp.vip/oauth/login" class="icon">
                <i class="el-icon-mobile-phone"></i>
              </a>
              <a href="http://38617112yi.zicp.vip/oauth/qq" class="icon">
                <i class="el-icon-chat-dot-square"></i>
              </a>
              <div class="icon">
                <i class="el-icon-message"></i>
              </div>
            </div>
          </div>
          <el-form
            :model="loginInfo"
            status-icon
            :rules="loginRules"
            ref="loginForm"
            class="login-form-content"
          >
            <el-form-item prop="phoneNumber">
              <el-input
                v-model="loginInfo.phoneNumber"
                autocomplete="off"
                placeholder="手机号"
                class="custom-input"
              ></el-input>
            </el-form-item>
            <el-form-item prop="checkPass">
              <el-input
                v-model="loginInfo.checkPass"
                autocomplete="off"
                placeholder="验证码"
                class="custom-input"
              ></el-input>
            </el-form-item>
            <div class="form-footer">
              <p class="forgot-password">忘记密码？</p>
              <el-button
                type="primary"
                @click="submitLogin('loginForm')"
                class="sign-btn"
                >SIGN IN</el-button
              >
              <el-button type="primary" class="get-code-btn"
                @click="getLoginCode">获取验证码</el-button
              >
            </div>
          </el-form>
        </div>
        
        <!-- 注册表单 -->
        <div class="form-content register-form" v-if="activeTab === 'register'">
          <h3 style="font-size: 24px; font-weight: 600; color: #333; margin-bottom: 24px; text-align: center; display: block; visibility: visible; opacity: 1;">创建账号</h3>
          <div class="third-party">
            <span>选择已有方式或电子邮箱注册</span>
            <div class="icons">
              <a href="http://38617112yi.zicp.vip/oauth/login" class="icon">
                <i class="el-icon-mobile-phone"></i>
              </a>
              <a href="http://38617112yi.zicp.vip/oauth/qq" class="icon">
                <i class="el-icon-chat-dot-square"></i>
              </a>
              <div class="icon">
                <i class="el-icon-message"></i>
              </div>
            </div>
          </div>
          <el-form
            :model="registerInfo"
            status-icon
            :rules="registerRules"
            ref="registerForm"
            class="register-form-content"
          >
            <el-form-item prop="phoneNumber">
              <el-input
                v-model="registerInfo.phoneNumber"
                autocomplete="off"
                placeholder="Name"
                class="custom-input"
              ></el-input>
            </el-form-item>
            <el-form-item prop="checkPass">
              <el-input
                v-model="registerInfo.checkPass"
                autocomplete="off"
                placeholder="Email"
                class="custom-input"
              ></el-input>
            </el-form-item>
            <el-form-item prop="passWord">
              <el-input
                type="password"
                v-model="registerInfo.passWord"
                autocomplete="off"
                placeholder="Password"
                class="custom-input"
              ></el-input>
            </el-form-item>
            <div class="form-footer">
              <p class="forgot-password">&nbsp;</p>
              <el-button
                type="primary"
                @click="submitRegister('registerForm')"
                class="sign-btn"
                >SIGN UP</el-button
              >
              <el-button type="primary" class="get-code-btn"
                @click="getRegisterCode">获取验证码</el-button
              >
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Auth",
  data() {
    return {
      activeTab: this.$route.name === 'register' ? 'register' : 'login', // 根据路由自动显示
      loginInfo: {
        phoneNumber: "",
        checkPass: "",
      },
      registerInfo: {
        phoneNumber: "",
        passWord: "",
        checkPass: "",
      },
      loginRules: {
        phoneNumber: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              var MobileRegex = /^1[3|4|5|7|8][0-9]{9}$/;
              if (!MobileRegex.test(value)) {
                callback(new Error("请输入正确的手机号码！"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
        checkPass: [
          { required: true, message: "请输入验证码", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              if (!/^[0-9]{6}$/.test(value)) {
                callback(new Error("验证码必须是六位数字"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
      },
      registerRules: {
        phoneNumber: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              var MobileRegex = /^1[3|4|5|7|8][0-9]{9}$/;
              if (!MobileRegex.test(value)) {
                callback(new Error("请输入正确的手机号码！"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
        checkPass: [
          { required: true, message: "请输入验证码", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              if (!/^[0-9]{6}$/.test(value)) {
                callback(new Error("验证码必须是六位数字"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
        passWord: [
          { required: true, message: "请输入密码", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              var res = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$/;
              if (!res.test(value)) {
                callback(new Error("请输入正确的密码格式~"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
      },
    };
  },
  watch: {
    '$route'(to) {
      this.activeTab = to.name === 'register' ? 'register' : 'login';
    }
  },
  methods: {
    switchTab(tab) {
      this.$router.push({ name: tab });
    },
    goIndex() {
      this.$router.push({ name: "index" });
    },
    // 登录相关方法
    getLoginCode() {
      var _self = this;
      var url = "http://38617112yi.zicp.vip/login/sendSMSCode";
      var userId = _self.loginInfo.phoneNumber;
      _self.$message.success("验证码已发送到您的手机，请您注意查收~");
      _self.$axios
        .get(url, {
          params: { 
            userId: userId,
          },
          withCredentials:true
        })
        .then((res) => {
          console.log(res.data);
          if (res.status == 200) {
            if (res.data.code == 0) {
              //_self.$message.success("验证码已发送到您的手机，请您注意查收~");
            }
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    submitLogin(formName) {
      console.log(11111);
      const _self = this;
      const userId = _self.loginInfo.phoneNumber;
      const code = _self.loginInfo.checkPass;
      const url = "http://38617112yi.zicp.vip/login/verifyLoginInfo";
      this.$refs[formName].validate((valid) => {
        if (valid) {
          _self.$axios
            .get(url, {
              //将对象 序列化成URL的形式，以&进行拼接
              params: { userId: userId, code: code },
            })
            .then((res) => {
              console.log(res);
              if (res.status == 200) {
                if (res.data.code == 0) {
                  _self.$message.success("登录成功");
                  setTimeout(() => {
                    _self.$router.push({ name: "index" });
                    localStorage.setItem("user", userId);
                  }, 500);
                } else if (res.data.code == 100000) {
                  _self.$message.success(res.data.msg);
                } else if (res.data.code == 100002) {
                  _self.$message.success(res.data.msg);
                }
              }
            });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    // 注册相关方法
    getRegisterCode() {
      var _self = this;
      var url = "http://38617112yi.zicp.vip/register/sendSMSCode";
      var userId = _self.registerInfo.phoneNumber;
      _self.$axios
        .get(url, {
          params: { userId: userId },
        })
        .then((res) => {
          console.log(res.data);
          if (res.status == 200) {
            if (res.data.code == 0) {
              _self.$message.success("验证码已发送到您的手机，请您注意查收~");
              // _self.check = res.data.message;
              // console.log(_self.check);
            }
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    submitRegister(formName) {
      const _self = this;
      const userId = _self.registerInfo.phoneNumber;
      const password = _self.registerInfo.passWord;
      const code = _self.registerInfo.checkPass;
      const url = "http://38617112yi.zicp.vip/register/verifyRegisterInfo";
      this.$refs[formName].validate((valid) => {
        if (valid) {
          _self.$axios
            .get(url, {
              //将对象 序列化成URL的形式，以&进行拼接
              params: { userId: userId, password: password, code: code },
            })
            .then((res) => {
              console.log(res);
              if (res.status == 200) {
                if (res.data.code == 0) {
                  _self.$message.success("注册成功");
                  setTimeout(() => {
                    _self.$router.push({ name: "login" });
                  }, 500);
                } else if (res.data.code == 100000) {
                  _self.$message.success(res.data.msg);
                } else if (res.data.code == 100001) {
                  _self.$message.success(res.data.msg);
                }
              }
            });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
#auth {
  padding: 0;
  margin: 0;
  width: 100vw;
  height: 100vh;
  box-sizing: border-box;
  background: #F8F9FA;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-container {
  width: 800px;
  height: 500px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  transition: all 0.5s ease;
}

/* 背景色块 */
.background-block {
  position: absolute;
  top: 0;
  left: 0;
  width: 400px;
  height: 100%;
  background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
  border-radius: 20px 0 0 20px;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;

  &.slide-right {
    transform: translateX(400px);
    border-radius: 0 20px 20px 0;
  }

  .welcome {
    text-align: center;
    padding: 40px;
    color: #fff;
    transition: all 0.5s ease;

    h2 {
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    p {
      font-size: 14px;
      line-height: 1.5;
      margin-bottom: 32px;
      opacity: 0.9;
    }

    .switch-btn {
      width: 160px;
      height: 48px;
      background: #fff;
      color: #113056;
      border: none;
      border-radius: 24px;
      font-size: 16px;
      font-weight: 600;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
      }
    }
  }
}

/* 返回首页按钮 */
.back-home {
  position: fixed;
  top: 30px;
  left: 30px;
  z-index: 1000;
  
  .back-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background: rgba(255, 255, 255, 0.9);
    color: #113056;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 500;
    text-decoration: none;
    transition: all 0.3s ease;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    
    &:hover {
      background: #fff;
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
    }
  }
}

/* 表单区块 */
.form-block {
  position: absolute;
  top: 0;
  left: 400px;
  width: 400px;
  height: 100%;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);

  .auth-container.is-register & {
    transform: translateX(-400px);
  }

  .form-content {
    width: 100%;
    transition: all 0.5s ease;
    z-index: 10;
    position: relative;
    background: #fff;
    border-radius: 12px;
    padding: 80px 40px 20px;

    h3 {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 40px;
      text-align: center;
    }

    .third-party {
      text-align: center;
      margin-bottom: 50px;

      span {
        display: block !important;
        font-size: 13px !important;
        color: #999 !important;
        margin-bottom: 16px !important;
        z-index: 10 !important;
        position: relative !important;
      }

      .icons {
        display: flex;
        justify-content: center;
        gap: 20px;

        .icon {
          width: 44px;
          height: 44px;
          border-radius: 50%;
          background: #F8F9FA;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          color: #666;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            transform: translateY(-3px);
            box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
          }
        }
      }
    }

    .form-footer {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-top: 8px;

      .forgot-password {
        font-size: 13px;
        color: #666;
        margin-bottom: 12px;
        cursor: pointer;

        &:hover {
          color: #113056;
        }
      }

      .sign-btn {
        width: 160px;
        height: 48px;
        background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
        border: none;
        border-radius: 24px;
        font-size: 16px;
        font-weight: 600;
        color: #fff;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 20px rgba(17, 48, 86, 0.3);
        }
      }

      .get-code-btn {
        margin-top: 8px;
        width: 160px;
        height: 40px;
        background: #F8F9FA;
        color: #666;
        border: none;
        border-radius: 20px;
        font-size: 14px;
        transition: all 0.3s;

        &:hover {
          background: #E9ECEF;
        }
      }
    }

    /* 注册表单样式 */
    &.register-form {
      color: #333;

      h3 {
        font-size: 24px;
        font-weight: 600;
        color: #333;
        margin-bottom: 32px;
        text-align: center;
        display: block;
        visibility: visible;
        opacity: 1;
      }

      .third-party {
        span {
          color: #999;
        }

        .icons {
          .icon {
            background: #F8F9FA;
            color: #666;

            &:hover {
              background: #E9ECEF;
            }
          }
        }
      }

      .form-footer {
        .sign-btn {
          background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
          color: #fff;

          &:hover {
            box-shadow: 0 8px 20px rgba(17, 48, 86, 0.3);
          }
        }

        .get-code-btn {
          background: #F8F9FA;
          color: #666;

          &:hover {
            background: #E9ECEF;
          }
        }
      }
    }
  }
}

/* 输入框样式 */
::v-deep .el-input {
  .el-input__inner {
    border-radius: 12px;
    height: 48px;
    border: 1px solid #E9ECEF;
    background: #F8F9FA;
    font-size: 14px;
    transition: all 0.3s ease;

    &:focus {
      border-color: #113056;
      background: #fff;
      box-shadow: 0 0 0 3px rgba(17, 48, 86, 0.1);
    }

    &::placeholder {
      color: #999;
    }
  }
}

/* 注册表单输入框样式 */
.auth-container.is-register ::v-deep .el-input {
  .el-input__inner {
    border: 1px solid #E9ECEF;
    background: #F8F9FA;
    color: #333;

    &:focus {
      border-color: #113056;
      background: #fff;
      box-shadow: 0 0 0 3px rgba(17, 48, 86, 0.1);
    }

    &::placeholder {
      color: #999;
    }
  }
}

::v-deep .el-form-item {
  margin-bottom: 0;
}

::v-deep .el-button--primary {
  border: none;
}

.custom-input {
  width: 100%;
  margin-bottom: 20px;
}
</style>