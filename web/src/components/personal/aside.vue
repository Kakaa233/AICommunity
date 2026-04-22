<template>
  <div class="aside">
    <div
      style="
        width: 100%;
        height: 50px;
        text-align: left;
        padding-left: 15px;
        line-height: 50px;
        font-size: 15px;
      "
    >
      <i class="el-icon-user-solid"></i> 个人中心
    </div>
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="vertical"
      @select="handleSelect"
    >
      <el-menu-item
        v-for="(item, index) in listData"
        :key="index"
        :index="item.asideIndex.toString()"
        ><i :class="item.icon"></i>
        <el-badge  class="item"
          >{{ item.name }}
        </el-badge></el-menu-item
      >
    </el-menu>
  </div>
</template>
<script>
export default {
  name: "Aside",
  data() {
    return {
      activeIndex: "1",
      listData: [
        {
          asideIndex: 1,
          name: "我的资料",
          icon: "el-icon-user-solid",
          url: "personal",
        },
        {
          asideIndex: 2,
          name: "我的关注",
          icon: "el-icon-star-on",
          url: "my_fav",
        },
        {
          asideIndex: 3,
          name: "我的粉丝",
          icon: "el-icon-lollipop",
          url: "my_fans",
        },
        {
          asideIndex: 4,
          name: "我的消息",
          icon: "el-icon-s-home",
          url: "my_info",
        },
        {
          asideIndex: 5,
          name: "成就值排行榜",
          icon: "el-icon-s-home",
          url: "ranking",
        },
        {
          asideIndex: 6,
          name: "我的私信",
          icon: "el-icon-s-home",
          url: "chat",
        },
      ],
    };
  },
  created() {
    // 初始化时根据当前路由设置activeIndex
    this.updateActiveIndex();
  },
  watch: {
    // 监听路由变化，更新activeIndex
    $route: {
      handler() {
        this.updateActiveIndex();
      },
      immediate: true
    }
  },
  methods: {
    handleSelect(value) {
      this.activeIndex = value;
      var url = this.listData[value - 1].url;
      this.$router.push({ 
        name: url,
        params: {
          userId: '18392710807'
        }
      });
    },
    updateActiveIndex() {
      // 根据当前路由名称查找对应的索引
      const currentRoute = this.$route.name;
      const item = this.listData.find(item => item.url === currentRoute);
      if (item) {
        this.activeIndex = item.asideIndex.toString();
      } else {
        // 默认选中第一个
        this.activeIndex = "1";
      }
    }
  },
};
</script>
<style lang="scss" scoped>
.aside {
  width: 230px;
  min-height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

::v-deep .el-menu {
  border-right: 0 !important;

  .el-menu-item {
    height: 55px;
    line-height: 55px;
    font-size: 14px;
    color: #606266;
    transition: all 0.3s;
    position: relative;
    padding-left: 20px !important;

    i {
      margin-right: 12px;
      font-size: 18px;
      color: #909399;
      transition: all 0.3s;
    }

    &:hover {
      background: linear-gradient(90deg, rgba(17, 48, 86, 0.08) 0%, transparent 100%);
      color: #113056;

      i {
        color: #113056;
        transform: scale(1.1);
      }
    }
  }

  .el-menu-item.is-active {
    background: linear-gradient(90deg, rgba(17, 48, 86, 0.12) 0%, transparent 100%);
    color: #113056;
    font-weight: 500;
    border-right: none;

    i {
      color: #113056;
    }

    &::after {
      content: '';
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 24px;
      background: linear-gradient(180deg, #113056, #91CFD5);
      border-radius: 2px 0 0 2px;
    }
  }
}

::v-deep .el-menu--horizontal > .el-menu-item {
  float: none;
}
</style>