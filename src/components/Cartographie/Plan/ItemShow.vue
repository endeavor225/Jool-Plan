<template>
  <q-card 
    flat 
    class="q-ma-md card-show" 
    style="background-image: url('DroneLight.svg'); background-repeat: no-repeat; backdrop-filter: blur(3px)"
  >
    <q-btn 
      @click="onBack"
      color="secondary" 
      icon="arrow_back_ios_new"   
      round
      style="position: absolute; left: -12px; top: -2px;"
    />

    <p class="q-pa-xs q-ml-lg q-mr-lg text-h6 text-center">{{showItem.libelle}}</p>

    <div class="card-info q-ma-xs" style="border-radius: 10px; margin-top:-10px;">
      <div class="q-pa-md">
        <p class="text-weight-light">{{$dateFormat(showItem.createdAt)}}</p>
  
        <div class="row">
          <div class="col q-mb-md">
            <q-item-section class="text-secondary">
              <q-item-label>
                <img src="Duree.svg" style="height: 16px; margin-left:2px"> 3h21m16s
              </q-item-label>
              <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:23px">Diée de vol</q-item-label>
            </q-item-section>
          </div>
          <div class="col  q-mb-md">
            <q-item-section class="text-secondary">
              <q-item-label>
                <img src="NbreImgs.svg" style="height: 16px; margin-left:2px"> 120 images
              </q-item-label>
              <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:25px">Dirée de vol</q-item-label>
            </q-item-section>
          </div>
        </div>
        <div class="row">
          <div class="col q-mb-md">
            <q-item-section class="text-secondary">
              <q-item-label>
                <img src="Distance.svg" style="height: 16px; margin-left:2px"> 15 ha
              </q-item-label>
              <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:23px">Distance parcourue</q-item-label>
            </q-item-section>
          </div>
          <div class="col q-mb-md">
            <q-item-section class="text-secondary">
              <q-item-label>
                  <img src="Batterie.svg" style="height: 16px; margin-left:2px"> 75 % <span class="text-weight-light" style="font-size: 11px;">~07h</span> 
                </q-item-label>
              <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:15px">Batterie restante</q-item-label>
            </q-item-section>
          </div>
        </div>
        <div class="row">
          <div class="col">
            <q-item-section class="text-secondary">
              <q-item-label>
                <img src="Altitude.svg" style="height: 16px; margin-left:2px"> 80m
              </q-item-label>
              <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:25px">Altitude de vol</q-item-label>
            </q-item-section>
          </div>
        </div>    
      </div>
    </div>
  </q-card>
</template>

<script>
import { defineComponent, getCurrentInstance} from "vue";
export default defineComponent({
  // name: 'ComponentName',
  props: {
    item: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup () {
    const instance = getCurrentInstance()

    let onShow = (item) => {
      console.log(item);
    }
    return {
      onShow
    }
  }
})
</script>
<style  scoped>
.card-info {
  background-color: rgba(177, 177, 177, 0.1);
  backdrop-filter: blur(3px)
}

.card-show {
  height: 46vh;
  backdrop-filter: blur(3px)
}
</style>
