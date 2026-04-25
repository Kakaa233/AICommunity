<template>
  <div class="ai-review-page">
    <div class="page-header">
      <h2>AI 审核管理</h2>
      <el-button type="primary" @click="fetchList" :loading="loading">
        <i class="el-icon-refresh"></i> 刷新
      </el-button>
    </div>

    <el-table
      :data="articleList"
      v-loading="loading"
      stripe
      style="width: 100%"
      empty-text="暂无待审核文章"
    >
      <el-table-column prop="articleId" label="ID" width="60" />
      <el-table-column prop="articleTitle" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="aiReviewStatus" label="审核状态" width="110">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.aiReviewStatus)" size="small">
            {{ statusLabel(scope.row.aiReviewStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="aiQualityScore" label="质量分" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.aiQualityScore != null">{{ scope.row.aiQualityScore }}</span>
          <span v-else class="no-data">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="aiReviewReason" label="标记原因" min-width="160" show-overflow-tooltip>
        <template slot-scope="scope">
          <span v-if="scope.row.aiReviewReason">{{ scope.row.aiReviewReason }}</span>
          <span v-else class="no-data">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
            type="success"
            size="mini"
            @click="handleApprove(scope.row)"
            :loading="scope.row._approving"
          >通过</el-button>
          <el-button
            type="danger"
            size="mini"
            @click="handleReject(scope.row)"
            :loading="scope.row._rejecting"
          >驳回</el-button>
          <el-button type="default" size="mini" @click="viewDetail(scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 驳回原因对话框 -->
    <el-dialog
      title="驳回原因"
      :visible.sync="rejectDialogVisible"
      width="400px"
    >
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入驳回原因（必填）"
      />
      <span slot="footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "AiReview",
  data() {
    return {
      articleList: [],
      loading: false,
      rejectDialogVisible: false,
      rejectArticleId: null,
      rejectReason: "",
    };
  },
  mounted() {
    this.fetchList();
  },
  methods: {
    fetchList() {
      this.loading = true;
      this.$axios
        .get("/apis/admin/ai/review/list")
        .then((res) => {
          if (res.status === 200 && res.data && res.data.code === 0) {
            const raw = Array.isArray(res.data.data) ? res.data.data : [];
            this.articleList = raw.map((item) => ({
              ...item,
              _approving: false,
              _rejecting: false,
            }));
          } else {
            this.$message.error("获取审核列表失败");
          }
        })
        .catch((err) => {
          console.error(err);
          this.$message.error("获取审核列表异常");
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleApprove(row) {
      row._approving = true;
      this.$axios
        .post("/apis/admin/ai/review/approve", null, {
          params: { articleId: row.articleId },
        })
        .then((res) => {
          if (res.status === 200 && res.data && res.data.code === 0) {
            this.$message.success("已通过");
            this.fetchList();
          } else {
            this.$message.error((res.data && res.data.msg) || "操作失败");
          }
        })
        .catch((err) => {
          console.error(err);
          this.$message.error("操作异常");
        })
        .finally(() => {
          row._approving = false;
        });
    },
    handleReject(row) {
      this.rejectArticleId = row.articleId;
      this.rejectReason = "";
      this.rejectDialogVisible = true;
    },
    confirmReject() {
      if (!this.rejectReason || !this.rejectReason.trim()) {
        this.$message.warning("请输入驳回原因");
        return;
      }
      const target = this.articleList.find(
        (a) => a.articleId === this.rejectArticleId
      );
      if (target) target._rejecting = true;

      this.$axios
        .post("/apis/admin/ai/review/reject", null, {
          params: {
            articleId: this.rejectArticleId,
            reason: this.rejectReason.trim(),
          },
        })
        .then((res) => {
          if (res.status === 200 && res.data && res.data.code === 0) {
            this.$message.success("已驳回");
            this.rejectDialogVisible = false;
            this.fetchList();
          } else {
            this.$message.error((res.data && res.data.msg) || "操作失败");
          }
        })
        .catch((err) => {
          console.error(err);
          this.$message.error("操作异常");
        })
        .finally(() => {
          if (target) target._rejecting = false;
        });
    },
    viewDetail(row) {
      this.$router.push({
        path: "/Detail",
        query: { url: row.articleId },
      });
    },
    statusTagType(status) {
      if (status === "flag") return "danger";
      if (status === "pending") return "warning";
      return "info";
    },
    statusLabel(status) {
      if (status === "flag") return "违规标记";
      if (status === "pending") return "待审核";
      if (status === "pass") return "已通过";
      if (status === "rejected") return "已驳回";
      return status || "未知";
    },
  },
};
</script>

<style scoped>
.ai-review-page {
  width: 1100px;
  margin: 30px auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 30px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.no-data {
  color: #c0c4cc;
}
</style>
