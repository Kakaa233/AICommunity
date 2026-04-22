<template>
  <div id="aside">
    <div class="aside">
      <h4>
        <i style="font-size: 18px" class="el-icon-chat-dot-square"></i> 讨论区
      </h4>
      <el-menu
        :default-active="activeIndex"
        class="el-menu-demo"
        @select="handleSelect"
        mode="horizontal "
        style="width: 249px"
      >
        <el-menu-item
          v-for="(item, index) in asideData"
          :key="index"
          :index="item.asideIndex.toString()"
          @click="toLink(item)"
          style="width: 250px"
          ><i :class="item.icon"></i>{{ item.name }}
        </el-menu-item>
      </el-menu>
    </div>
  </div>
</template>

<script>
export default {
  name: "Aside",
  data() {
    return {
      activeIndex: "1",
      asideData: [
        {
          asideIndex: 1,
          name: "考研交流",
          icon: "el-icon-sunny",
          url: "index",
        },
        {
          asideIndex: 2,
          name: "找工作交流",
          icon: "el-icon-sunny",
          url: "JobDiscuss",
        },
        {
          asideIndex: 3,
          name: "日常学习问题",
          icon: "el-icon-sunny",
          url: "study_day",
        },
        {
          asideIndex: 4,
          name: "寻物启事",
          icon: "el-icon-sunny",
          url: "find",
        },
        {
          asideIndex: 5,
          name: "拼单拼车",
          icon: "el-icon-sunny",
          url: "put_together",
        },
        {
          asideIndex: 6,
          name: "表白墙",
          icon: "el-icon-sunny",
          url: "wall",
        },
        {
          asideIndex: 7,
          name: "生活趣事",
          icon: "el-icon-sunny",
          url: "life_intersting",
        },
        {
          asideIndex: 8,
          name: "竞赛组队",
          icon: "el-icon-sunny",
          url: "other",
        },
      ],
    };
  },
  components: {},
  mounted() {
    this.activeIndex = this.$route.params.url;
  },
  methods: {
    toLink(num) {
      console.log(num.url);
      //   this.activeIndex = num.asideIndex;
      this.$router.push({ name: num.url, query: { url: num.asideIndex } });
    },
    handleSelect(value) {
      console.log(value);
      //   this.activeIndex = value;
    },
  },
};
</script >

<style lang="scss" scoped>
#aside {
  width: 250px;
  min-height: 500px;
  border-right: 1px solid #ebeef5;
  box-sizing: border-box;
  background: #fff;
  border-radius: 12px 0 0 12px;

  .aside {
    position: relative;

    h4 {
      font-size: 16px;
      position: absolute;
      left: 25px;
      top: 15px;
      font-weight: 600;
      color: #409EFF;
      z-index: 999;
      padding-bottom: 8px;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        width: 30px;
        height: 3px;
        background: linear-gradient(135deg, #409EFF, #66b1ff);
        border-radius: 2px;
      }
    }

    ul {
      padding-top: 60px;

      li {
        height: 55px;
        font-size: 14px;
        text-align: left;
        line-height: 55px;
        padding-left: 25px;
        transition: all 0.3s;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 0;
          background: linear-gradient(135deg, #409EFF, #66b1ff);
          border-radius: 2px;
          transition: height 0.3s;
        }

        ::v-deep .el-menu-item.is-active {
          border-right: none !important;
          background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, transparent 100%);
          color: #409EFF;
          font-weight: 500;

          &::before {
            height: 24px;
          }
        }

        ::v-deep .el-menu.el-menu--horizontal {
          border-bottom: 0;
        }

        i {
          margin-right: 15px;
          font-size: 18px;
          transition: transform 0.3s;
        }

        &:hover {
          color: #409EFF;
          background: linear-gradient(90deg, rgba(64, 158, 255, 0.05) 0%, transparent 100%);
          border-right: none !important;

          &::before {
            height: 24px;
          }

          i {
            transform: scale(1.1);
          }
        }
      }
    }
  }
}
</style>
