<template>
  <q-page padding style="overflow-x:hidden">

    <div class="flex flex-center" style="margin-top: 70px">
      <img
        alt="Jool Plan logo"
        src="JooL_Plan.svg"
      >
    </div>

    <div class="flex flex-center">
      <img
        alt="DronePilot"
        src="Animation-JooL-Plan.gif"
        style="width: 450px; height: 450px;"
      >
    </div>

    <div class="q-ma-lg absolute-bottom" align="center" >
      <q-btn color="secondary" label="Décoller" size="lg" rounded class="full-width q-mb-md" @click="$router.push('accueil')"  v-if="token" />
      <q-btn color="secondary" label="Décoller" size="lg" rounded class="full-width q-mb-md" @click="$router.push('/login')" v-else />
    </div>


  </q-page>
</template>

<script>
import { defineComponent, onBeforeMount } from 'vue';
import { useRouter } from "vue-router"
export default defineComponent({
  // name: 'PageIndex',

  setup (){
    const router = useRouter();
    const token = localStorage.getItem('jool_Plan_id-session-app')

    // onBeforeMount(() => {
    //   console.log(token);

    //   if (token) {
    //     router.push("accueil")
    //   }
    // })

    return{
      token
    }
  }
})
</script>
