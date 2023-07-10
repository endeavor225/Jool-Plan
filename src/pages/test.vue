<template>
 

   <q-btn
      class="q-mt-xl"
      color="white"
      text-color="blue"
      unelevated
      @click="onShow"
      label="Go Home"
      no-caps
    />

  <q-dialog v-model="bottom" position="bottom" persistent >
      <q-card class="q-ml-md q-mr-md q-pa-md" style="border-radius: 10px ; margin-left: 10px; margin-right: 10px; position:relative; bottom: 20%;" >

        <q-item class="q-ma-none q-pa-none" style="margin-bottom: -10px; margin-top: -10px">
          <q-item-section>Chargement des images</q-item-section>
          <q-item-section side class="text-secondary">
            ~245 Images
          </q-item-section>
        </q-item>
        <q-linear-progress rounded size="15px" :value="progress" color="secondary" class="q-mt-sm q-ml-xs q-mr-xs">
          <!-- <div class="absolute-full flex flex-center">
            <q-badge color="white" text-color="accent" :label="progressLabel1" />
          </div> -->
        </q-linear-progress>
        <div class="text-weight-light q-mt-xs q-ml-xs" style="font-size: 10px">
          Temps de chargement estimé à 20ms
        </div>
          
      </q-card>
  </q-dialog>
</template>

<script>
import { defineComponent, ref, watch, computed, onMounted} from 'vue'
import { Plugins } from '@capacitor/core';

export default defineComponent({
  name: 'Error404',

  setup(){
    const { Device } = Plugins;

    let bottom = ref(false)
    let progress = ref()

    const info = ref()

    onMounted( async () =>{
      
    })

    let onShow = () => {
      bottom.value = true

    }

    return{
        bottom,
        onShow,
        progress,
        info,
  
    }
  }
})
</script>
