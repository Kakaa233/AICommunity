<template>
  <div id="register">
    <div class="container">
      <div class="banner">注册账号</div>
      <div class="main">
        <div class="phone">
          <div class="sjh">
            <el-form
              :model="registerInfo"
              status-icon
              :rules="rules"
              ref="ruleForm"
              label-width="100px"
              class="demo-ruleForm"
            >
              <el-form-item label="账号名" prop="userId">
                <el-input
                  v-model="registerInfo.userId"
                  autocomplete="off"
                  placeholder="5-20位字母数字下划线，非纯数字"
                ></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="passWord">
                <el-input
                  type="password"
                  v-model="registerInfo.passWord"
                  autocomplete="off"
                  placeholder="8-20位密码，至少含有一个字母和数字"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  @click="submitForm('ruleForm')"
                  class="reg"
                  >注册</el-button
                >
              </el-form-item>
            </el-form>
          </div>
          <div
            class="txt"
            style="display: flex; justify-content: space-between"
          >
            <p style="margin-left: 95px; cursor: pointer" @click="goIndex()">
              <i class="el-icon-back"></i> 返回
            </p>
            <p style="cursor: pointer" @click="goLogin()">
              去登录<i class="el-icon-right"></i>
            </p>
          </div>
        </div>
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
        userId: "",
        passWord: "",
      },
      rules: {
        userId: [
          { required: true, message: "请输入账号名", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              var regex = /^[a-zA-Z0-9_]{5,20}$/;
              var isAllDigits = /^\d+$/;
              if (!regex.test(value) || isAllDigits.test(value)) {
                callback(new Error("5-20位字母、数字或下划线，且不能为纯数字"));
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
                callback(new Error("密码需8-20位，至少包含一个字母和一个数字"));
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
    goLogin() {
      this.$router.push({ path: "/login" });
    },
    submitForm(formName) {
      const _self = this;
      const userId = _self.registerInfo.userId;
      const password = _self.registerInfo.passWord;
      const url = "http://localhost:8081/register/verifyRegisterInfo";
      this.$refs[formName].validate((valid) => {
        if (valid) {
          _self.$axios
            .post(url, null, {
              params: { userId: userId, password: password },
            })
            .then((res) => {
              if (res.status == 200) {
                if (res.data.code == 0) {
                  _self.$message.success("注册成功");
                  setTimeout(() => {
                    _self.$router.push({ path: "/login" });
                  }, 500);
                } else {
                  _self.$message.error(res.data.msg || "注册失败");
                }
              }
            });
        }
      });
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
  .container {
    width: 1000px;
    height: 700px;
    background: #fff;
    margin: 0 auto;
    box-shadow: 0 15px 20px rgba(0, 0, 0, 0.11);
    .banner {
      width: 1000px;
      height: 100px;
      background: #409eff;
      line-height: 100px;
      font-size: 20px;
      color: #fff;
      text-align: center;
    }
    .main {
      width: 1000px;
      height: 600px;
      .phone {
        width: 50%;
        height: 250px;
        margin: 150px auto;
        position: relative;
        .txt {
          font-size: 14px;
          color: rgb(148, 144, 144);
          margin-top: 20px;
        }
      }
    }
  }
}
.reg {
  width: 100%;
}
.el-form-item {
  margin-bottom: 32px;
}
</style>
