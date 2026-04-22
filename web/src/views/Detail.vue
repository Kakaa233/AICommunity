<template>
  <div id="Detail">
    <Content></Content>
  </div>
</template>
<script>
import Content from "../components/detail/content.vue";
export default {
  name: "detail",
  data() {
    return {};
  },
  components: {
    Content,
  },
  methods: {},
};
</script>
<style lang="scss" scoped>
#Detail {
  width: 100%;
  background: #f7f8f9;
  height: 611px;
}
</style>