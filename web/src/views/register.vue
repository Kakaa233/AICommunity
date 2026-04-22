<template>
  <div id="register">
    <div class="container">
      <div class="left-section">
        <div class="welcome">
          <h2>Welcome Back!</h2>
          <p>已经有账号了啊，去登入账号来进入奇妙世界吧！！</p>
          <el-button type="primary" class="sign-in-btn" @click="goLogin()">SIGN IN</el-button>
        </div>
      </div>
      <div class="right-section">
        <h3>创建账号</h3>
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
          :rules="rules"
          ref="ruleForm"
          class="register-form"
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
            <el-button
              type="primary"
              @click="submitForm('ruleForm')"
              class="sign-up-btn"
              >SIGN UP</el-button
            >
            <el-button type="primary" class="get-code-btn"
              @click="getCheckPass()">获取验证码</el-button
            >
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  components: {},
  data() {
    return {
      registerInfo: {
        phoneNumber: "",
        passWord: "",
        checkPass: "",
      },
      rules: {
        phoneNumber: [
          { required: true, message: "请输入账号", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              var MobileRegex = /^[a-zA-Z0-9_]{5,20}$/;
              if (!MobileRegex.test(value) || /^\d+$/.test(value)) {
                callback(new Error("5-20个字母数字下划线，且不能是纯数字"));
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
    goLogin() {
      this.$router.push({ name: "login" });
    },
    getCheckPass() {
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
    submitForm(formName) {
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
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
  },
};
</script>

<style lang="scss" scoped>
#register {
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
    background: #F8F9FA;
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
    background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
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
  color: #333;

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
      color: #666;
    }

    .sign-in-btn {
      width: 160px;
      height: 48px;
      background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
      color: #fff;
      border: none;
      border-radius: 24px;
      font-size: 16px;
      font-weight: 600;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(17, 48, 86, 0.3);
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
  color: #fff;

  h3 {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 24px;
    text-align: center;
  }

  .third-party {
    text-align: center;
    margin-bottom: 32px;

    span {
      display: block;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.8);
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
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        color: #fff;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }
  }

  .register-form {
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
          box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
        }
      }

      .get-code-btn {
        margin-top: 16px;
        width: 160px;
        height: 40px;
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
        border: none;
        border-radius: 20px;
        font-size: 14px;
        transition: all 0.3s;

        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }
  }
}

::v-deep .el-input {
  .el-input__inner {
    border-radius: 12px;
    height: 48px;
    border: 1px solid rgba(255, 255, 255, 0.3);
    background: rgba(255, 255, 255, 0.1);
    font-size: 14px;
    color: #fff;
    transition: all 0.3s ease;

    &:focus {
      border-color: #fff;
      background: rgba(255, 255, 255, 0.2);
      box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.2);
    }

    &::placeholder {
      color: rgba(255, 255, 255, 0.6);
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

