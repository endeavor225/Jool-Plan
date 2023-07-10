<template>

  <q-card class="my-card" flat style="border-radius: 10px; background: rgba(0,0,10, 0.4); backdrop-filter: blur(5px); overflow-x:hidden">

    <q-btn
      round
      dense
      class="z-max absolute-top-right"
      size="8px" icon="close"
      color="negative"
      v-close-popup
    />

    <div class="absolute-center" v-if="loading">
      <q-spinner
        color="white"
        size="3em"
      />
    </div>

    <q-item class="text-white">
      <q-item-section>
        <q-item-label class="text-h6">
          <q-icon name="location_on" size="25px" style=""/>
          {{meteo.nom_departement}}
        </q-item-label>
      </q-item-section>
      <q-item-section side style="position: relative; top: 10px">
        <q-item-label class="text-weight-light text-white">{{$dateFormat(meteo.date)}}</q-item-label>
      </q-item-section>
    </q-item>

    <q-separator />

    <q-card-section horizontal class="text-white" >
      <q-card-section class="col-8 q-pa-none q-pt-md q-pb-md">
        <q-item class="q-pl-xs q-pr-xs q-pt-md q-pb-md">
          <q-item-section avatar class="q-pr-sm q-pl-xs q-pt-md" >
            <q-item-label>
              <span class="text-weight-bold" style="font-size: 45px"> {{meteo.temperature_c}}</span>
              <span style="position:relative; top:-20px; left:0px; font-size: 18px">°C</span>
            </q-item-label>
          </q-item-section>

          <q-item-section class="q-pt-md">
            <q-item-label class="text-weight-regular" style="font-size: 10px">Humidité: </q-item-label>
            <q-item-label class="text-weight-regular" style="font-size: 10px">Vent: </q-item-label>
          </q-item-section>

          <q-item-section side class="q-pt-md">
            <q-item-label class="text-weight-medium text-white" style="font-size: 10px">{{meteo.humidity}}%</q-item-label>
            <q-item-label class="text-weight-medium text-white" style="font-size: 10px">{{meteo.wind_kph}} km/h</q-item-label>
          </q-item-section>
        </q-item>
      </q-card-section>

      <q-separator vertical />

      <q-card-section class="col text-center q-pa-none q-pt-md q-pb-xs">
        <q-img :src="meteo.icon_url" width="60px" height="60px" />
        <q-item-label class="" style="font-size: 12px; text-transform: capitalize"> {{meteo.condition_text}} </q-item-label>
      </q-card-section>
    </q-card-section>
  </q-card>
</template>

<script>
import axios from "axios"
import { ref, defineComponent, inject, onBeforeMount, onMounted, watch} from "vue";
export default defineComponent({
  // name: 'ComponentName',

  props: {
    propsCoords: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup (props) {
    let meteo = ref([])
    let loading = ref()

    onMounted( () => {
      loading.value = true
      axios({
        method: 'post',
        url: 'https://adb.jool-tech.com/_db/db/weather_api/get_prevision_jour',
        data: {
          latitude: props.propsCoords.latitude,
          longitude: props.propsCoords.longitude
        },
        headers:{
          username: 'jool_weather_api',
          password: '95Dpms2HHwhXruU6By46KLqprfaR7'
        }
      }).then( response => {
        let tab = response.data
        meteo.value = tab[0]
        let url = `http://openweathermap.org/img/wn/${meteo.value.icon}@2x.png`
        meteo.value.icon_url = url
        loading.value = false
      }).catch( error => {
        console.log("error: " + error);
      });
    })



    return {
      meteo,
      loading
    }
  }
})
</script>

<style scoped>
/* .my-card {
  height: "160px"
} */
</style>
