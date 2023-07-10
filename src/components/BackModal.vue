<template>
  <q-card class="z-max"
    style="
      background-image: url(SadBg.svg);
      background-repeat: no-repeat;
      background-position: center;
    ">
    <div align="center" style="margin-top: 70px">
      <img alt="Jool Plan logo"  src="JooL_Plan.svg">
    </div>

    <div>
      <h4 align="center" class="text-weight-light" style="margin-bottom:35px">ANNULATION</h4>
    </div>

    <q-card-section class="q-ma-md" style="margin-top: 20%">
      <div align="center"  class="text-weight-regular" style="font-size:20px">Voulez-vous vraiment annuler la mission ?</div>
    </q-card-section>

    <q-card-actions align="around" style="margin-top: 10%">
      <q-btn rounded color="secondary" v-close-popup >
        <span style="font-size: 20px">Non</span>
      </q-btn>
      <q-btn rounded text-color="black" color="grey-2" @click="onStopMission">
        <span style="font-size: 20px">Oui</span>
      </q-btn>
    </q-card-actions>
  </q-card>
</template>

<script>
defineComponent
export default defineComponent({
  // name: 'ComponentName',
  setup () {

    let onStopMission = () => {

    }
    return {
      onStopMission,
    }
  }
})
</script>
