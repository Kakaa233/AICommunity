<template>
  <div class="content">
    <div class="info-header">
      <h2 class="info-title">基本信息</h2>
      <el-button 
        type="primary" 
        plain 
        @click="changeStatus()"
        class="edit-btn"
      >
        <i class="el-icon-edit"></i> 编辑资料
      </el-button>
    </div>
    
    <!-- 查看模式 -->
    <div class="view-mode" v-if="!display">
      <div class="user-profile">
        <div class="avatar-section">
          <el-avatar :size="80" :src="userObj.avatar" class="user-avatar"></el-avatar>
          <h3 class="user-nickname">{{userObj.nickname}}</h3>
          <p class="user-signature" v-if="userObj.signature">{{userObj.signature}}</p>
          <p class="user-signature" v-else>暂无个性签名</p>
        </div>
        
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">性别</span>
            <span class="info-value">{{userObj.sex === 1 ? '男' : '女'}}</span>
          </div>
          <div class="info-item">
            <span class="info-label">学校</span>
            <span class="info-value">{{userObj.school || '未设置'}}</span>
          </div>
          <div class="info-item">
            <span class="info-label">用户ID</span>
            <span class="info-value">{{userObj.userId}}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑模式 -->
    <div class="edit-mode" v-if="display">
      <el-form ref="form" :model="form" label-width="100px" class="edit-form">
        <el-form-item label="头像" prop="pic">
          <el-upload
            class="avatar-uploader"
            action="http://38617112yi.zicp.vip/upload/images"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            name="file"
          >
            <img v-if="imageUrl" :src="imageUrl" class="avatar" />
            <img v-else-if="userObj.avatar" :src="userObj.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input 
            v-model="form.title" 
            placeholder="请输入昵称"
            class="custom-input"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="学校">
          <el-input 
            v-model="form.school" 
            placeholder="请输入学校"
            class="custom-input"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex" class="sex-radio-group">
            <el-radio label="男"></el-radio>
            <el-radio label="女"></el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="个性签名">
          <el-input
            type="textarea"
            v-model="form.signature"
            placeholder="懒的连签名都没有...."
            class="custom-textarea"
          ></el-input>
        </el-form-item>

        <div class="form-actions">
          <el-button @click="display = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="submit()" class="submit-btn">保存修改</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>
<script>
export default {
  name: "Content",
  data() {
    return {
      display: false,
      imgSrc: require("@/assets/img1.webp"),
      form: {
        pic: "",
        title: "",
        name: "",
        sex: "",
        date: "",
        signature: "",
        school: "",
      },
      imageUrl: "",
      userObj: {},
      userId: '',
      img1: '',
    };
  },
  mounted() {
    this.getUserInfo();
   // this.isRed()
   this.newLikes()
    
  },
  methods: {
    newLikes() {
      var _self = this;
      var url = "http://38617112yi.zicp.vip/notice/list";
      _self.$axios
        .get(url)
        .then((res) => {
          if (res.status == 200) {
            
              console.log('新消息',res.data)
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    getUserInfo() {

      var _self = this;
      var url = "http://38617112yi.zicp.vip/userInfo";
      _self.$axios
        .get(url)
        .then((res) => {
          if (res.status == 200) {
            
              console.log('用户信息',res.data)
              _self.userObj = res.data.data;
              localStorage.setItem('myuserId',res.data.data.userId)
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    changeStatus() {
      // 初始化表单数据
      this.form.title = this.userObj.nickname || '';
      this.form.school = this.userObj.school || '';
      this.form.sex = this.userObj.sex === 1 ? '男' : '女';
      this.form.signature = this.userObj.signature || '';
      this.imageUrl = '';
      this.display = true;
    },
    submitForm() {
      console.log("提交修改内容");
      this.display = false;
    },
    handleAvatarSuccess(res, file) {
      //console.log('图片路径',res)
      this.img1 = res.data;
      this.imageUrl = URL.createObjectURL(file.raw);
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === "image/jpeg";
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error("上传头像图片只能是 JPG 格式!");
      }
      if (!isLt2M) {
        this.$message.error("上传头像图片大小不能超过 2MB!");
      }
      return isJPG && isLt2M;
    },
    submit() {
      // let config = {
      //   timeout: 30000,
      //   headers: {
      //     "Content-Type": "multipart/form-data", //设置headers
      //   },
      // };
      const formData = new FormData();
      var that = this;
      // 首先判断是否上传了图片，如果上传了图片，将图片存入到formData中
      console.log(this.imageUrl);
      if (this.dataList) {
        that.dataList.forEach((item, index) => {
          // console.log(item)
          formData.append(index, item);
        });
      }
      // console.log(formData.get(0));
      // that.$axios
      //   .post(
      //     'http://38617112yi.zicp.vip/upload/images', //请求后端的url
      //     formData,
      //     config
      //   )
      //   .then((res) => {
      //     console.log(res);
      //     if (res.data.status == "ok") {
      //       //上传成功
      //       console.log("上传成功");
      //       this.$router.push({
      //         path: "./",
      //       });
      //     } else {
      //       alert("上传失败");
      //     }
      //   })
      //   .catch((error) => {
      //     console.log("请求失败");
      //   });

      let sex = that.form.sex == '男' ? 1 : 0;
        let user = {
          nickname:that.form.title, 
          sex: sex,
          school: that.form.school,
          signature: that.form.signature,
          userId: that.userObj.userId,
          avatar: that.img1,
        }
        let url = 'http://38617112yi.zicp.vip/update/userInfo'
        that.$axios
          .post(url ,user, {
            headers: { "Content-Type": "application/json;charset=utf-8" ,
              //"Cookie": this.myCookie
            },
            withCredentials: true,
            //baseURL: '/apis'
          })
          .then((res) => {
            console.log(res.data);
            if (res.status == 200) {
              if (res.data.code == 0) {
                 that.input = '';
                 that.content = '';
                that.$message.success("编辑信息成功");
              }
              this.$router.push({ 
              name:'index',
            })
            }
          })
          .catch((err) => {
            console.log(err);
          });
      //用户可以在上传完成之后将数组给清空，这里直接跳转到首页了，没有做清空的操作
    },
  },
};
</script>
<style lang="scss" scoped>
.content {
  width: 950px;
  min-height: 500px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 30px;
  margin-left: 20px;
  margin-top: 20px;
  
  .info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;
    
    .info-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin: 0;
      position: relative;
      padding-left: 16px;
      
      &:before {
        content: "";
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 20px;
        background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
        border-radius: 2px;
      }
    }
    
    .edit-btn {
      color: #113056;
      border-color: #113056;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(17, 48, 86, 0.05);
        transform: translateY(-1px);
      }
    }
  }
  
  // 查看模式样式
  .view-mode {
    .user-profile {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 0;
      
      .avatar-section {
        text-align: center;
        margin-bottom: 30px;
        
        .user-avatar {
          border: 3px solid #f0f0f0;
          margin-bottom: 16px;
          transition: transform 0.3s ease;
          
          &:hover {
            transform: scale(1.05);
          }
        }
        
        .user-nickname {
          font-size: 20px;
          font-weight: 600;
          color: #113056;
          margin: 0 0 8px 0;
        }
        
        .user-signature {
          font-size: 14px;
          color: #909399;
          margin: 0;
          max-width: 400px;
          line-height: 1.4;
        }
      }
      
      .info-grid {
        width: 100%;
        max-width: 600px;
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 20px;
        margin-top: 20px;
        
        .info-item {
          background: #f7f8f9;
          border-radius: 8px;
          padding: 20px;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
          }
          
          .info-label {
            display: block;
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }
          
          .info-value {
            display: block;
            font-size: 16px;
            font-weight: 500;
            color: #303133;
          }
        }
      }
    }
  }
  
  // 编辑模式样式
  .edit-mode {
    .edit-form {
      max-width: 600px;
      margin: 0 auto;
      
      .avatar-uploader {
        margin-bottom: 20px;
        
        .el-upload {
          border: 1px dashed #dcdfe6;
          border-radius: 50%;
          width: 100px;
          height: 100px;
          cursor: pointer;
          position: relative;
          overflow: hidden;
          transition: all 0.3s ease;
          
          &:hover {
            border-color: #91CFD5;
            transform: scale(1.05);
          }
        }
        
        .avatar-uploader-icon {
          font-size: 32px;
          color: #c0c4cc;
          width: 100px;
          height: 100px;
          line-height: 100px;
          text-align: center;
        }
        
        .avatar {
          width: 100px;
          height: 100px;
          display: block;
          border-radius: 50%;
        }
      }
      
      .custom-input {
        width: 100%;
        height: 44px;
        border-radius: 6px;
        transition: all 0.3s ease;
        
        &:focus {
          border-color: #91CFD5;
          box-shadow: 0 0 0 2px rgba(145, 207, 213, 0.2);
        }
      }
      
      .custom-textarea {
        width: 100%;
        min-height: 100px;
        border-radius: 6px;
        transition: all 0.3s ease;
        
        &:focus {
          border-color: #91CFD5;
          box-shadow: 0 0 0 2px rgba(145, 207, 213, 0.2);
        }
      }
      
      .sex-radio-group {
        display: flex;
        gap: 20px;
        
        .el-radio {
          font-size: 14px;
          
          .el-radio__label {
            padding-left: 8px;
          }
        }
      }
      
      .form-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        margin-top: 30px;
        padding-top: 20px;
        border-top: 1px solid #ebeef5;
        
        .cancel-btn {
          padding: 10px 20px;
          border-radius: 6px;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }
        }
        
        .submit-btn {
          padding: 10px 20px;
          border-radius: 6px;
          background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
          border: none;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(17, 48, 86, 0.4);
          }
        }
      }
    }
  }
}

// 表单样式覆盖
::v-deep .el-form-item {
  margin-bottom: 20px;
  
  &__label {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
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
