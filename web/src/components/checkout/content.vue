<template>
  <div class="content">
    <div class="scholl">
      <div
        class="detail"
        v-for="(item, index) in dataList"
        :key="index"
        @click="checkout()"
      >
        <img
          style="
            width: 70px;
            height: 70px;
            border-radius: 50%;
            position: absolute;
            left: 15px;
            top: 10px;
          "
          :src="item.imgSrc"
          alt=""
        />
        <div class="name">{{ item.name }}</div>
        <div class="address">{{ item.address }}</div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "checkout",
  data() {
    return {
      dataList: [
        {
          imgSrc: require("@/assets/zylogo.png"),
          name: "所有学校",
          address: "全国高校，一览无遗",
        },
        {
          imgSrc: require("@/assets/buaa.png"),
          name: "北京航空航天大学",
          address: "北京市海淀区学院路37号",
        },
      ],
    };
  },
  methods: {
    checkout() {
      this.$router.push({ name: "index" });
    },
  },
};
</script>
<style lang="scss" scoped>
.content {
  width: 1000px;
  height: 500px;
  margin: 30px auto;
  background: #fff;
  //   border: 1px #d4d4d4 solid;
  border-radius: 5px;
  .scholl {
    padding-top: 10px;
    display: flex;
    justify-content: space-around;
    flex-wrap: wrap;
    .detail {
      width: 300px;
      height: 90px;
      position: relative;
      background: #e6e6e680;
      border-radius: 15px;
      margin-bottom: 10px;
      &:hover {
        background: #d5eee6;
      }
      .name {
        font-size: 18px;
        position: absolute;
        left: 90px;
        top: 18px;
        font-weight: 700;
        color: #444343;
      }
      .address {
        font-size: 14px;
        color: #575656;
        position: absolute;
        left: 90px;
        top: 55px;
      }
    }
  }
}
</style>