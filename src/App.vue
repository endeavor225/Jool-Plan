<template>
  <router-view />
</template>
<script>
import { defineComponent, provide, ref, getCurrentInstance } from 'vue';
import { Ressource } from 'boot/store/compos-classe'

export default defineComponent({
  name: 'App',
  setup(props, ctx){
    let self =  getCurrentInstance()

    let models = []

    for (const service of self.appContext.app.config.globalProperties.$models) {
      let ressource = new Ressource(service)
      let opts = ref({
        items: ref([]),
        item: ref({}),
        service,
        filters:ref({}),
        createForm: ref({}),
        updateForm: ref({}),
        deleteForm: ref({}),
        searchForm: ref({}),
        totalItem: ref(0),
        itemObjet: ref({}),
      })

      provide(service, ressource)
    }
  }
})
</script>
