import Vue from 'vue'
import Router from 'vue-router'
import Garage from '@/components/Garage' // <1>

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Garage', // <1>
      component: Garage // <1>
    }
  ]
})
