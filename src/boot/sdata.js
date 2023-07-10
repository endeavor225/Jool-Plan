import { boot } from 'quasar/wrappers'
import { ref } from "vue"

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async (/* { app, router, ... } */  {app}) => {
  // something to do
  app.config.globalProperties.$sdata = ref({})
})
