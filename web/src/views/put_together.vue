<template>
  <div id="put_together">
    <el-container style="background-color: #f7f8f9">
      <el-container style="width: 95%; margin: 15px auto 0; background: #fff">
        <el-aside width="250px">
          <Aside></Aside>
        </el-aside>
        <el-main style="padding: 0"><Content></Content></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Aside from "../components/discuss/aside.vue";
import Content from "../components/together/content.vue";
export default {
  name: "discuss",
  components: {
    Aside,
    Content,
  },
};
</script>

<style lang="scss" scoped>
</style>>

