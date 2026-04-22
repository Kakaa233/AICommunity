<template>
  <div class="checkout">
    <Content></Content>
  </div>
</template>
<script>
import Content from "../components/checkout/content.vue";
export default {
  name: "checkout",
  data() {
    return {};
  },
  components: { Content },
};
</script>
<style lang="scss" scoped>
.checkout {
  width: 100%;
  background: #f8f8f8;
  height: 1000px;
}
</style>