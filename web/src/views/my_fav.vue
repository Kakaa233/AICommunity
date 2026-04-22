<template>
  <div class="fav">
    <el-container>
      <el-container>
        <el-aside width="252px" style="margin-left: 40px; margin-top: 20px">
          <Aside></Aside
        ></el-aside>
        <el-main style="padding: 20px 0"><Main></Main></el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script>

import Aside from "../components/personal/aside.vue";
import Main from "../components/favContent.vue";
export default {
  name: "fav",
  components: { Aside, Main },
};
</script>
<style lang="scss" scoped>
.fav {
  width: 1278px;
  height: 610px;
  background: #f3f1f1;
}
</style>