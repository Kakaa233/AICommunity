<template>
  <div id="login">
    <div class="container">
      <div class="left-section">
        <div class="welcome">
          <h2>Hello Friend!</h2>
          <p>去注册一个账号，成为更要的粉丝会员，让我们踏入奇妙的旅途！</p>
          <el-button type="primary" class="sign-up-btn" @click="goRegister()">SIGN UP</el-button>
        </div>
      </div>
      <div class="right-section">
        <h3>登入账号</h3>
        <el-form
          :model="loginInfo"
          status-icon
          :rules="rules"
          ref="ruleForm"
          class="login-form"
        >
          <el-form-item prop="userId">
            <el-input
              v-model="loginInfo.userId"
              autocomplete="off"
              placeholder="账号名"
              class="custom-input"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              type="password"
              v-model="loginInfo.password"
              autocomplete="off"
              placeholder="密码"
              class="custom-input"
            ></el-input>
          </el-form-item>
          <div class="form-footer">
            <p class="forgot-password">忘记密码？</p>
            <el-button
              type="primary"
              @click="submitForm('ruleForm')"
              class="sign-in-btn"
              >SIGN IN</el-button
            >
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  components: {},
  data() {
    return {
      loginInfo: {
        userId: "",
        password: "",
      },
      rules: {
        userId: [
          { required: true, message: "请输入账号名字", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              var Regex = /^[a-zA-Z0-9_]{5,20}$/;
              if (!Regex.test(value) || /^\d+$/.test(value)) {
                callback(new Error("5-20个字母数字下划线，且不能是纯数字"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              var res = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/;
              if (!res.test(value)) {
                callback(new Error("8-20个字符，至少一个字母和一个数字"));
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
  methods: {
    goIndex() {
      this.$router.push({ name: "index" });
    },
    goRegister() {
      this.$router.push({ name: "register" });
    },
    submitForm(formName) {
      console.log(11111);
      const _self = this;
      const userId = _self.loginInfo.userId;
      const password = _self.loginInfo.password;
      const url = "http://38617112yi.zicp.vip/login/verifyLoginInfo";
      this.$refs[formName].validate((valid) => {
        if (valid) {
          _self.$axios
            .get(url, {
              params: { userId: userId, password: password },
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
  },
};
</script>

<style lang="scss" scoped>
#login {
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

.container {
  width: 800px;
  height: 500px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 400px;
    height: 100%;
    background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
    border-radius: 20px 0 0 20px;
    z-index: 1;
  }

  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 400px;
    height: 100%;
    background: #fff;
    border-radius: 0 20px 20px 0;
    z-index: 1;
  }
}

.left-section {
  width: 400px;
  height: 100%;
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;

  .welcome {
    text-align: center;
    padding: 40px;

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

    .sign-up-btn {
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

.right-section {
  width: 400px;
  height: 100%;
  position: relative;
  z-index: 2;
  padding: 40px;
  display: flex;
  flex-direction: column;

  h3 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 24px;
    text-align: center;
  }

  .third-party {
    text-align: center;
    margin-bottom: 32px;

    span {
      display: block;
      font-size: 13px;
      color: #999;
      margin-bottom: 16px;
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

  .login-form {
    flex: 1;

    .custom-input {
      width: 100%;
      margin-bottom: 20px;
    }

    .form-footer {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-top: 24px;

      .forgot-password {
        font-size: 13px;
        color: #666;
        margin-bottom: 20px;
        cursor: pointer;

        &:hover {
          color: #113056;
        }
      }

      .sign-in-btn {
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
        margin-top: 16px;
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
  }
}

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

::v-deep .el-form-item {
  margin-bottom: 0;
}

::v-deep .el-button--primary {
  border: none;
}
</style>

