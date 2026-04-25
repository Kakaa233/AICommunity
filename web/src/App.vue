<template>
  <div id="app">
    <Banner v-if="showBanner"></Banner>
    <transition :name="transitionName" mode="out-in">
      <router-view></router-view>
    </transition>
  </div>
</template>

<script>
import Banner from './components/banner.vue'

export default {
  name: "App",
  components: {
    Banner
  },
  data() {
    return {
      transitionName: 'page'
    }
  },
  computed: {
    showBanner() {
      const noBannerRoutes = ['login', 'register', 'loginPass']
      return !noBannerRoutes.includes(this.$route.name)
    }
  },
  watch: {
    $route(to, from) {
      // 讨论区内部切换无动画
      const toDiscuss = to.meta && to.meta.discuss
      const fromDiscuss = from.meta && from.meta.discuss
      
      // 个人中心内部切换无动画
      const personalRoutes = ['personal', 'my_fav', 'my_fans', 'my_info', 'ranking', 'chat']
      const toPersonal = personalRoutes.includes(to.name)
      const fromPersonal = personalRoutes.includes(from.name)
      
      if ((toDiscuss && fromDiscuss) || (toPersonal && fromPersonal)) {
        this.transitionName = 'none'
      } else {
        this.transitionName = 'page'
      }
    }
  }
};
</script>

<style lang='scss'>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #000;
  margin: 0;
  padding: 0;
  border: 0;
  box-sizing: border-box;
  width: 100vw;
  min-height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
}

.page-enter-active {
  animation: page-in 0.25s ease-out forwards;
}

.page-leave-active {
  animation: page-out 0.2s ease-in forwards;
}

@keyframes page-in {
  0% {
    opacity: 0;
    transform: translateX(20px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes page-out {
  0% {
    opacity: 1;
    transform: translateX(0);
  }
  100% {
    opacity: 0;
    transform: translateX(-10px);
  }
}

.fade-slide-enter-active {
  animation: page-in 0.25s ease-out forwards;
}

.fade-slide-leave-active {
  animation: page-out 0.2s ease-in forwards;
}

.none-enter-active,
.none-leave-active {
  transition: none;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.3s ease-out;
}

.slide-up-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

.scale-enter-active,
.scale-leave-active {
  transition: all 0.3s ease;
}

.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
