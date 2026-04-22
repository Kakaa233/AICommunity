<template>
  <div class="searchDetail">
    <Content></Content>
  </div>
</template>
<script>
import Content from "../components/searchDetail/content.vue";
export default {
  name: "searchDetail",
  data() {
    return {};
  },
  components: { Content },
};
</script>
<style lang="scss" scoped>
.searchDetail {
  width: 1280px;
  height: 615px;
  background: #f8f8f8;
}
</style>