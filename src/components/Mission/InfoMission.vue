<template>
  <div class="row">

    <transition
      appear
      enter-active-class="animated fadeInLeft"
      leave-active-class="animated fadeOutLeft"
    >
      <q-card class="hideIcon" :class="{ hide: !isActive }" v-if="!meteoModal && !checkListModal">

        <q-item-section class="text-white bg-dark" align="center" style="border-radius:10px 10px 0 0;">
          <q-item-label class="q-pa-sm" style="font-size:12px"> {{date}} </q-item-label>
        </q-item-section>

        <q-item-section class="text-secondary q-ma-sm">
          <q-item-label>
            <img src="plan/Batterie.svg" style="height: 16px;" class="q-ml-xs">
            <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{infoDrone.chargeRemainingInPercent}}% </span>
          </q-item-label>
        </q-item-section>
        <q-separator spaced />

        <q-item-section class="text-secondary q-ma-sm">
          <q-item-label>
            <q-icon name="wifi" size="20px" />
            <span style="position: relative; top:3px; font-size:11px" class="text-weight-regular q-pl-xs"> {{infoDrone.uplinkSignalQuality}} </span>
          </q-item-label>
        </q-item-section>
        <q-separator spaced />

        <q-item-section class="text-secondary q-ma-sm">
          <q-item-label lines="1">
            <img src="plan/Duree.svg" style="height: 16px; margin-left:2px">
            <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{flyTime}} Min</span>
          </q-item-label>
        </q-item-section>
        <q-separator spaced />

        <q-item-section class="text-secondary q-ma-sm">
          <q-item-label lines="1">
            <img src="plan/Distance.svg" style="height: 16px;">
            <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{ superficie}} ha  </span>
          </q-item-label>
        </q-item-section>
        <q-separator spaced />

        <q-item-section class="text-secondary q-ma-sm">
          <q-item-label lines="1">
            <img src="plan/Altitude.svg" style="height: 16px;">
            <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{Math.round(infoDrone.altitudeInMeters)}} M </span>
          </q-item-label>
        </q-item-section>
        <q-separator spaced />

        <q-item-section class="text-secondary q-ma-sm">
          <q-item-label lines="1">
            <img src="plan/NbreImg.svg" style="height: 16px;">
            <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{infoMission.imageCount}} Img </span>
          </q-item-label>
        </q-item-section>
        <q-separator spaced />

        <q-item-section class="text-secondary q-ma-sm">
          <q-item-label>
            <img src="plan/Vitesse.svg" style="height: 13px;">
            <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{Math.round(infoDrone.speedInMeterPerSec)}} m/s </span>
          </q-item-label>
        </q-item-section>
        <q-separator spaced />

        <q-btn
          style="position: relative; bottom:225px; left: 85px"
          color="white"
          size="sm"
          padding="xs"
          text-color="secondary"
          @click="onHide"
          unelevated
        >
          <q-icon class="" :name="isActive ? 'chevron_left' : 'chevron_right'" />
        </q-btn>
      </q-card>
    </transition>
  </div>
</template>

<script>
import { format } from 'date-fns'
import { defineComponent, ref, inject, } from "vue";
export default defineComponent({
  // name: 'ComponentName',
  setup () {
    let checkListModal = inject("checkListModal")
    let infoDrone = inject("infoDrone")
    let meteoModal = inject("meteoModal")
    let infoMission = inject("infoMission")
    let flyTime = inject("flyTime")
    let superficie = inject("superficie")
    let isActive = ref(true)
    let date = ref(format(new Date(), "dd/MM/yyyy"))

    let onHide = () => {
      isActive.value = !isActive.value
    }
    return {
      onHide,
      checkListModal,
      date,
      isActive,
      infoDrone,
      meteoModal,
      infoMission,
      flyTime,
      superficie
    }
  }
})
</script>
<style scoped>
.hideIcon {
  background: rgb(255, 255, 255);
  border-radius: 10px;
  height:310px;
  width:85px;
  position: absolute;
	left: 0;
	transition: left 1s, transform 1s;
}

.hide {
  left: -94px;
}
</style>
