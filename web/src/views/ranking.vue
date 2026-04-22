<template>
  <div class="info">
    <el-container>
      <el-container>
        <el-aside width="252px" style="margin-left: 40px; margin-top: 20px">
          <Aside></Aside
        ></el-aside>
        <el-main style="padding: 20px 0; height: 500px; overflow: none"
          ><Main :rankingList="rankingList"></Main
        ></el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script>
import Aside from "../components/personal/aside.vue";
import Main from "../components/rankingContent.vue";
export default {
  //name: "info",
  components: { Aside, Main },
  data() {
    return {
      rankingList: []
    }
  },
  mounted() {
      console.log('????')
     this.getRanking();
  },
  methods: {
    getRanking() {
      var _self = this;
      var articleId = parseInt(_self.artId);
      var url = "http://38617112yi.zicp.vip/TopAchieve";

      _self.$axios
        .get(url, { params: { articleId: articleId } })
        .then((res) => {
          //console.log("删除的数据", res);
          console.log("排行榜", res);
          _self.rankingList = res.data.data;
        })
        .catch((err) => {
          console.log(err);
        });
    },
  },
};
</script>
<style lang="scss" scoped>
.info {
  width: 1278px;
  min-height: 610px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  border-radius: 12px;
  padding: 20px;
}

::v-deep .el-main {
  display: block;
  flex: 1;
  flex-basis: auto;
  overflow: auto !important;
  box-sizing: border-box;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

::v-deep .el-aside {
  margin-left: 0 !important;
  margin-top: 0 !important;
}
</style>
