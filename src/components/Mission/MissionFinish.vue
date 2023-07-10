<template>
  <q-card class="q-ml-md q-mr-md q-pa-md" style="border-radius: 15px 15px 0 0" >
    <q-item class="q-ma-none q-pa-none" style="margin-bottom: -10px; margin-top: -10px">
      <q-item-section>Chargement des images</q-item-section>
      <q-item-section side class="text-secondary">
        ~{{infoMission.imageCount}} Images
      </q-item-section>
    </q-item>
    <q-linear-progress rounded size="15px" :value="progressing" color="secondary" class="q-mt-sm q-ml-xs q-mr-xs">
      <!-- <div class="absolute-full flex flex-center">
        <q-badge color="white" text-color="accent" :label="progressLabel1" />
      </div> -->
    </q-linear-progress>
    <div class="text-weight-light q-mt-xs q-ml-xs" style="font-size: 10px">
      Temps de chargement estimé à 20ms
    </div>
  </q-card>
</template>

<script>
import { ref, defineComponent, inject} from "vue";
export default defineComponent({
  // name: 'ComponentName',
  setup () {
    let infoMission = inject("infoMission")
    let progressing = inject("progressing")
    return {
      infoMission,
      progressing
    }
  }
})
</script>
