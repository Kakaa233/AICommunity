<template>
  <div class="write-article-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-title">
        <i class="el-icon-edit"></i> 写文章
      </div>
      <div class="header-actions">
        <el-button
          type="warning"
          class="ai-btn ai-polish-btn"
          @click="aiPolish"
          :loading="polishLoading"
          icon="el-icon-edit"
        >
          AI润色
        </el-button>
        <el-button
          type="success"
          class="ai-btn ai-draft-btn"
          @click="aiDraftDialogVisible = true"
          icon="el-icon-plus"
        >
          AI写稿
        </el-button>
        <el-button 
          type="default" 
          class="cancel-btn"
          @click="Return()"
        >
          取消
        </el-button>
        <el-button 
          v-if="!isEdit" 
          type="primary" 
          class="publish-btn"
          @click="Publish()"
        >
          发布
        </el-button>
        <el-button 
          v-else 
          type="primary" 
          class="publish-btn"
          @click="mode()"
        >
          编辑完成
        </el-button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-wrapper">
      <!-- 标题输入 -->
      <div class="title-section">
        <el-input
          v-model="input"
          placeholder="文章标题：一句话说明你遇到的问题或你想分享的经验"
          class="title-input"
        ></el-input>
      </div>

      <!-- 文章分类 -->
      <div class="category-section">
        <h3 class="section-title">文章分类</h3>
        <div class="category-buttons">
          <el-button 
            v-for="category in categories" 
            :key="category"
            :type="ruleForm.resource === category ? 'primary' : 'default'"
            :plain="ruleForm.resource !== category"
            @click="ruleForm.resource = category"
            class="category-btn"
          >
            {{ category }}
          </el-button>
        </div>
      </div>

      <!-- 编辑器 -->
      <div class="editor-section">
        <h3 class="section-title">文章内容</h3>
        <div class="editor-card">
          <quill-editor
            v-model="content"
            ref="myQuillEditor"
            style="height: 500px"
            :options="editorOption"
          >
            <!-- 自定义toolar -->
            <div id="toolbar" slot="toolbar">
              <!-- Add a bold button -->
              <button class="ql-bold" title="加粗">Bold</button>
              <button class="ql-italic" title="斜体">Italic</button>
              <button class="ql-underline" title="下划线">underline</button>
              <button class="ql-strike" title="删除线">strike</button>
              <button class="ql-blockquote" title="引用"></button>
              <button class="ql-code-block" title="代码"></button>
              <button class="ql-header" value="1" title="标题1"></button>
              <button class="ql-header" value="2" title="标题2"></button>
              <!--Add list -->
              <button class="ql-list" value="ordered" title="有序列表"></button>
              <button class="ql-list" value="bullet" title="无序列表"></button>
              <!-- Add font size dropdown -->
              <select class="ql-header" title="段落格式">
                <option selected>段落</option>
                <option value="1">标题1</option>
                <option value="2">标题2</option>
                <option value="3">标题3</option>
                <option value="4">标题4</option>
                <option value="5">标题5</option>
                <option value="6">标题6</option>
              </select>
              <select class="ql-size" title="字体大小">
                <option value="10px">10px</option>
                <option value="12px">12px</option>
                <option value="14px">14px</option>
                <option value="16px" selected>16px</option>
                <option value="18px">18px</option>
                <option value="20px">20px</option>
              </select>
              <select class="ql-font" title="字体">
                <option value="SimSun">宋体</option>
                <option value="SimHei">黑体</option>
                <option value="Microsoft-YaHei">微软雅黑</option>
                <option value="KaiTi">楷体</option>
                <option value="FangSong">仿宋</option>
                <option value="Arial">Arial</option>
              </select>
              <!-- Add subscript and superscript buttons -->
              <select class="ql-color" value="color" title="字体颜色"></select>
              <select
                class="ql-background"
                value="background"
                title="背景颜色"
              ></select>
              <select class="ql-align" value="align" title="对齐"></select>
              <button class="ql-clean" title="还原"></button>
              <!-- You can also add your own -->
            </div>
          </quill-editor>
        </div>
        <!-- AI 质量分预览 -->
        <div class="quality-section">
          <el-button
            type="info"
            size="small"
            class="quality-btn"
            @click="checkQuality"
            :loading="qualityLoading"
            icon="el-icon-data-analysis"
            plain
          >
            {{ qualityScore !== null ? '刷新评分' : 'AI质量评估' }}
          </el-button>
          <div v-if="qualityScore !== null" class="quality-result">
            <span class="quality-badge" :class="qualityScore >= 80 ? 'high' : qualityScore >= 60 ? 'medium' : 'low'">
              {{ qualityScore }}分
            </span>
            <span class="quality-suggestions" v-if="qualitySuggestions">
              💡 {{ qualitySuggestions }}
            </span>
          </div>
        </div>
      </div>
    </div>
  <!-- AI 写稿对话框 -->
  <el-dialog
    title="AI 写稿助手"
    :visible.sync="aiDraftDialogVisible"
    width="500px"
    top="30vh"
    class="ai-draft-dialog"
  >
    <el-form label-width="80px">
      <el-form-item label="文章标题">
        <el-input v-model="draftTitle" placeholder="输入文章的标题主题" />
      </el-form-item>
      <el-form-item label="关键词">
        <el-input
          v-model="draftKeywords"
          type="textarea"
          :rows="3"
          placeholder="可选，用逗号分隔多个关键词"
        />
      </el-form-item>
    </el-form>
    <span slot="footer">
      <el-button @click="aiDraftDialogVisible = false">取消</el-button>
      <el-button
        type="success"
        @click="aiDraft"
        :loading="draftLoading"
      >开始生成</el-button>
    </span>
  </el-dialog>
</div>
</template>

<script>
import { Quill, quillEditor } from "vue-quill-editor";
import "quill/dist/quill.core.css";
import "quill/dist/quill.snow.css";
import "quill/dist/quill.bubble.css";

//引入font.css
import "@/assets/css/font.css";

// 自定义字体大小
let Size = Quill.import("attributors/style/size");
Size.whitelist = ["10px", "12px", "14px", "16px", "18px", "20px"];
Quill.register(Size, true);

// 自定义字体类型
var fonts = [
  "SimSun",
  "SimHei",
  "Microsoft-YaHei",
  "KaiTi",
  "FangSong",
  "Arial",
  "Times-New-Roman",
  "sans-serif",
  "宋体",
  "黑体",
];
var Font = Quill.import("formats/font");
Font.whitelist = fonts;
Quill.register(Font, true);

export default {
  name: "FuncFormsEdit",
  components: {
    quillEditor,
  },
  data() {
    return {
      content: null,
      input: "",
      ruleForm: {
        resource: "",
      },
      categories: [
        "考研交流",
        "找工作交流",
        "日常学习",
        "寻物启事",
        "拼单拼车",
        "表白墙",
        "生活趣事",
        "竞赛组队"
      ],
      isEdit: false,
      editorOption: {
        placeholder: "请输入",
        theme: "snow", // or 'bubble'
        modules: {
          toolbar: {
            container: "#toolbar",
          },
        },
      },
      rules: {},
      myCookie: '',
      articleId: '',
      // AI 功能
      polishLoading: false,
      aiDraftDialogVisible: false,
      draftTitle: '',
      draftKeywords: '',
      draftLoading: false,
      // AI 质量分
      qualityScore: null,
      qualitySuggestions: '',
      qualityLoading: false,
      // 发布后 AI 状态
      publishAiResult: null
    };
  },
  mounted() {
    this.getCookie();
    console.log('页面传参',this.$route)

    this.input = this.$route.params.articleTitle;
    this.content = this.$route.params.articleContent;
    this.ruleForm.resource = this.$route.params.articleCategoryName;
    this.isEdit = this.$route.params.isEdit;
    this.articleId = this.$route.params.articleId;
  },
  methods: {
    radioChange() {
      //console.log(this.ruleForm.resource)
    },
    getCookie() {
      let cookie = document.cookie;
     
      let myCookie = cookie.split(';');
      let cookieObj = {}
      for(let i = 0;i < myCookie.length;i++) {
        cookieObj[myCookie[i].split('=')[0]] = myCookie[i].split('=')[1]
      }
      console.log(11123132,cookieObj)
      this.myCookie = cookieObj['Admin-Token']
    },
    Return() {
      // console.log(this.input);
      this.$router.go(-1);
    },
    mode() {
      var reg = /<\/?.+?\/?>/g;
        // console.log(this.content.replace(reg, ""));
        var url = "/apis/article/edit";
        // var url = "/insert";
        var _self = this;
        let articleTitle = this.input;
        let articleContent = this.content.replace(reg, "");
        // let articleCategoryId = "1";
        var obj = {
          articleTitle: articleTitle,
          articleContent: articleContent,
          articleCategoryName: _self.ruleForm.resource,
          //cookie: this.myCookie
          articleId: _self.articleId
        };
        var article = JSON.stringify(obj);
       
        
         _self.$axios.defaults.withCredentials = true 
        _self.$axios
          .post(url ,article, {
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
                 _self.input = '';
                 _self.content = '';
                _self.$message.success("发布成功～");
              }
              this.$router.push({ 
              name:'index',
            })
            }
          })
          .catch((err) => {
            console.log(err);
          });
    },
    Publish() {
      if (this.input && this.content) {
        var reg = /<\/?.+?\/?>/g;
        // console.log(this.content.replace(reg, ""));
        var url = "/apis/article/insert";
        // var url = "/insert";
        var _self = this;
        let articleTitle = this.input;
        let articleContent = this.content.replace(reg, "");
        // let articleCategoryId = "1";
        var obj = {
          articleTitle: articleTitle,
          articleContent: articleContent,
          articleCategoryName: _self.ruleForm.resource,
          //cookie: this.myCookie
        };
        var article = JSON.stringify(obj);
        // let cookieParams = JSON.stringify({
        //     cookie: this.myCookie
        // })
        
         _self.$axios.defaults.withCredentials = true 
        _self.$axios
          .post(url ,article, {
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
                 _self.input = '';
                 _self.content = '';
                _self.$message.success("发布成功～");
                // 保存 AI 处理结果，用于发布后反馈
                if (res.data.data && typeof res.data.data === 'object') {
                  _self.publishAiResult = res.data.data;
                  _self.showPublishAiFeedback();
                }
              }
            }
          })
          .catch((err) => {
            console.log(err);
          });
      } else {
        this.$message.error("发布内容不能为空");
      }
      // console.log("11111");
    },
    // ===== AI 辅助功能 =====

    /** AI 润色：将当前内容发送到后端，用返回的润色结果替换编辑器内容 */
    aiPolish() {
      if (!this.content) {
        this.$message.warning('请先输入文章内容');
        return;
      }
      // 提取纯文本
      var reg = /<\/?.+?\/?>/g;
      var text = this.content.replace(reg, '');
      if (text.length < 10) {
        this.$message.warning('内容太短，至少10个字符才能润色');
        return;
      }
      this.polishLoading = true;
      this.$axios
        .post('/apis/article/ai/polish', { text: text }, {
          headers: { 'Content-Type': 'application/json;charset=utf-8' },
          withCredentials: true
        })
        .then((res) => {
          if (res.data && res.data.code == 0 && res.data.data) {
            var polished = res.data.data.polishedText;
            // 替换编辑器内容（保持富文本格式：用 <p> 包裹）
            this.content = '<p>' + polished.replace(/\n/g, '</p><p>') + '</p>';
            this.$message.success('AI润色完成 ✨');
          } else {
            this.$message.error(res.data.msg || '润色服务暂不可用');
          }
        })
        .catch((err) => {
          console.error('AI润色失败', err);
          this.$message.error('AI润色服务异常，请稍后重试');
        })
        .finally(() => {
          this.polishLoading = false;
        });
    },

    /** AI 写稿：发送标题+关键词，生成内容填入编辑器 */
    aiDraft() {
      if (!this.draftTitle) {
        this.$message.warning('请输入文章标题');
        return;
      }
      this.draftLoading = true;
      this.$axios
        .post('/apis/article/ai/draft', {
          title: this.draftTitle,
          keywords: this.draftKeywords
        }, {
          headers: { 'Content-Type': 'application/json;charset=utf-8' },
          withCredentials: true
        })
        .then((res) => {
          if (res.data && res.data.code == 0 && res.data.data) {
            var draftContent = res.data.data.draftContent;
            this.content = '<p>' + draftContent.replace(/\n/g, '</p><p>') + '</p>';
            this.input = this.draftTitle;
            this.aiDraftDialogVisible = false;
            this.draftTitle = '';
            this.draftKeywords = '';
            this.$message.success('AI写稿完成 📝');
          } else {
            this.$message.error(res.data.msg || '写稿服务暂不可用');
          }
        })
        .catch((err) => {
          console.error('AI写稿失败', err);
          this.$message.error('AI写稿服务异常，请稍后重试');
        })
        .finally(() => {
          this.draftLoading = false;
        });
    },

    /** AI 质量分预览：发送内容获取质量评分 */
    checkQuality() {
      if (!this.content) {
        this.$message.warning('请先输入文章内容');
        return;
      }
      var reg = /<\/?.+?\/?>/g;
      var text = this.content.replace(reg, '');
      if (text.length < 20) {
        this.$message.warning('内容太短，至少20个字符才能评估');
        return;
      }
      this.qualityLoading = true;
      this.$axios
        .post('/apis/article/ai/quality', { content: text }, {
          headers: { 'Content-Type': 'application/json;charset=utf-8' },
          withCredentials: true
        })
        .then((res) => {
          if (res.data && res.data.code == 0 && res.data.data) {
            this.qualityScore = res.data.data.qualityScore;
            this.qualitySuggestions = res.data.data.suggestions || '';
            this.$message.success('质量评估完成，评分: ' + this.qualityScore + '分');
          } else {
            this.$message.error(res.data.msg || '质量评估暂不可用');
          }
        })
        .catch((err) => {
          console.error('质量评估失败', err);
          this.$message.error('质量评估服务异常');
        })
        .finally(() => {
          this.qualityLoading = false;
        });
    },

    /** 发布后展示 AI 处理结果反馈 */
    showPublishAiFeedback() {
      if (!this.publishAiResult) return;
      var result = this.publishAiResult;
      var messages = [];
      // 质量分
      if (result.qualityScore) {
        var score = result.qualityScore;
        var scoreText = score >= 80 ? '优秀' : score >= 60 ? '良好' : '一般';
        messages.push('AI评分: ' + score + '分 (' + scoreText + ')');
      }
      // 审核状态
      if (result.reviewStatus) {
        var statusMap = { pass: '✅ 已通过', flag: '⚠️ 需人工复审', pending: '⏳ 审核中' };
        messages.push('内容审核: ' + (statusMap[result.reviewStatus] || result.reviewStatus));
      }
      // 摘要
      if (result.summary) {
        messages.push('AI摘要已生成');
      }
      if (messages.length > 0) {
        this.$notify({
          title: 'AI 处理报告',
          message: messages.join('<br>'),
          duration: 6000,
          dangerouslyUseHTMLString: true,
          type: 'success'
        });
      }
    },
  },
};
</script>

<style scoped lang="scss">
.write-article-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  padding-bottom: 40px;
  overflow-x: hidden;
  // 移除固定的overflow-y设置，让浏览器默认处理滚动
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 20px 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #113056;
    display: flex;
    align-items: center;
    gap: 8px;
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
  }
  
  .header-actions {
    display: flex;
    gap: 16px;
    position: absolute;
    right: 40px;
    
    .cancel-btn {
      border-color: #dcdfe6;
      color: #606266;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: #113056;
        color: #113056;
      }
    }
    
    .publish-btn {
      background: linear-gradient(135deg, #113056 0%, #91CFD5 100%);
      border: none;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(17, 48, 86, 0.3);
      }
    }
  }
}

/* 内容区域 */
.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 标题输入 */
.title-section {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  
  .title-input {
    width: 100%;
    
    ::v-deep .el-input__inner {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      border: none;
      border-bottom: 2px solid #ebeef5;
      padding: 0;
      height: 60px;
      transition: all 0.3s ease;
      
      &:focus {
        border-bottom-color: #113056;
      }
    }
  }
}

/* 文章分类 */
.category-section {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 20px 0;
  }
  
  .category-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .category-btn {
      padding: 8px 20px;
      border-radius: 20px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
  }
}

/* 编辑器 */
.editor-section {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 20px 0;
  }
  
  .editor-card {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    overflow: hidden;
    
    ::v-deep .ql-toolbar {
      border-bottom: 1px solid #ebeef5;
      background: #f7f8f9;
      
      button, select {
        margin: 4px;
        padding: 4px 8px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        background: #fff;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          background: #ecf5ff;
          border-color: #113056;
        }
      }
    }
    
    ::v-deep .ql-container {
      min-height: 500px;
      border: none;
    }
    
    ::v-deep .ql-editor {
      font-size: 16px;
      line-height: 1.6;
      min-height: 500px;
    }
  }
}

/* AI 按钮样式 */
.ai-btn {
  transition: all 0.3s ease;
  font-weight: 500;
  border: none;
}
.ai-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.ai-polish-btn {
  background: linear-gradient(135deg, #f39c12, #e67e22);
  color: #fff;
}
.ai-draft-btn {
  background: linear-gradient(135deg, #27ae60, #2ecc71);
  color: #fff;
}

/* AI 质量分区域 */
.quality-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  margin-top: 16px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 8px;
}
.quality-btn {
  flex-shrink: 0;
}
.quality-result {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.quality-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
}
.quality-badge.high {
  background: linear-gradient(135deg, #27ae60, #2ecc71);
}
.quality-badge.medium {
  background: linear-gradient(135deg, #f39c12, #e67e22);
}
.quality-badge.low {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
}
.quality-suggestions {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

/* AI 写稿对话框 */
.ai-draft-dialog ::v-deep .el-dialog__title {
  font-weight: 600;
  color: #27ae60;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .content-wrapper {
    padding: 0 15px;
  }
  
  .title-section,
  .category-section,
  .editor-section {
    padding: 20px;
  }
  
  .category-buttons {
    justify-content: center;
  }
}
</style>

