<template>
  <q-page>

    <div id="mapid" style=" width: 100%; height: 105vh;"></div>

    <div class="fixed-top-left z-max q-mt-lg q-ml-sm">
      <q-btn round color="secondary" icon="chevron_left" @click="onBack" />
    </div>

    <q-dialog v-model="saveMadal" persistent transition-show="slide-up" transition-hide="slide-down">
       <add-acteur />
    </q-dialog>

    <q-dialog v-model="bachModal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down">
      <q-card
        style="
          background-image: url(SadBg.svg);
          background-repeat: no-repeat;
          background-position: center;
        ">
        <div align="center" style="margin-top: 70px">
          <img
            alt="Jool Plan logo"
            src="JooL_Plan.svg"
          >
        </div>

        <div>
          <h4 align="center" class="text-weight-light" style="margin-bottom:35px">ANNULATION</h4>
        </div>

        <q-card-section class="q-ma-md" style="margin-top: 20%">
          <div align="center"  class="text-weight-regular" style="font-size:20px">Voulez-vous vraiment annuler la délimitation ?</div>
        </q-card-section>

        <q-card-actions align="around" style="margin-top: 10%">
          <q-btn rounded color="secondary" v-close-popup >
            <span style="font-size: 20px">Non</span>
          </q-btn>
          <q-btn rounded text-color="black" color="grey-2" @click="$router.push('accueil')">
            <span style="font-size: 20px">Oui</span>
          </q-btn>
        </q-card-actions>

      </q-card>
    </q-dialog>

  </q-page>
</template>

<script>
import { defineComponent, getCurrentInstance, onMounted, ref, provide} from 'vue'
import AddActeur from "components/Acteur/CreateKML.vue";
export default defineComponent({
  // name: 'PageName',
  components: {
    AddActeur,
  },

  setup() {
    const instance = getCurrentInstance();
    let bachModal = ref(false)
    let helpModal = ref(true)
    const saveMadal = ref(true);
    provide("saveMadal", saveMadal)


    onMounted(async () =>{
      var mymap = L.map('mapid', {
          zoomControl: false,
      });

      L.tileLayer('http://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',{
        minZoom: 5,
        maxZoom: 20,
        subdomains:['mt0','mt1','mt2','mt3']
      }).addTo(mymap);

      function onLocationFound(e) {
        var radius = e.accuracy;
        //console.log(e.latlng);
        L.marker(e.latlng)
          .addTo(mymap)
          .bindPopup("Vous êtes à " + radius.toPrecision(4) + " mètres de ce point")
          .openPopup();
      }

      L.control.zoom({
        position: 'topright'
      }).addTo(mymap)

      L.control.scale({
        metric: true,
        imperial: false,
        position: "bottomleft",
      }).addTo(mymap);

      mymap.locate({ setView: true, maxZoom: 22});
      mymap.on("locationfound", onLocationFound);
    })

    let onBack = () => {
      bachModal.value = true
    }

    let onHelp = () => {
      helpModal.value = !helpModal.value
    }

    return {
      saveMadal,
      onBack,
      bachModal,
      helpModal,
      onHelp
    }
  }

})
</script>

<style scoped>

</style>
