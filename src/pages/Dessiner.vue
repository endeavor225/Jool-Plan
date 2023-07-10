<template>
  <q-page>
    <div id="mapid" style=" width: 100%; height: 100vh;"></div>

    <div class="fixed-top z-max" v-if="helpModal === false"
      style="margin-left: 15%; 
        margin-right: 15%; 
        background-color:rgba(255, 255, 255, 0.8); 
        border-radius: 0px 0px 15px 15px"
      >
      
      <div clickable class="text-center q-ma-sm" style="margin-left:65px; margin-right:65px">
        <q-separator size="5px" color="grey-8" inset @click="onHelp" />
      </div>

    </div>

    <div class="fixed-top-left z-max q-mt-lg q-ml-sm">
        <q-btn round color="secondary" icon="chevron_left" @click="onBack" />
    </div>

    <q-dialog v-model="saveMadal" transition-show="slide-up" transition-hide="slide-down">
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



      <q-dialog v-model="helpModal" seamless position="top" transition-show="slide-down" transition-hide="slide-up">
        <q-card class="" 
          style="margin-left: 15%; 
          margin-right: 15%; 
          border-radius: 15px;
          background: rgba(255, 255, 255, 0.7);
          backdrop-filter: blur(3px)"
        >

          <q-card-section class="q-ma-none">
            <div align="center"  class="text-weight-light" style="font-size:12px">
              Appuyez sur le bouton <q-avatar square size="13px"><img src="assets/rulers.svg"> </q-avatar> pour commencer a déssiner. Une fois terminer appuyer sur le bouton «Terminer» puis sur «Sauvegarder».
            </div>
            <div clickable class="q-mt-md text-center" style="margin-left:50px; margin-right:50px">
              <q-separator size="5px" color="grey-8" inset @click="onHelp"/>
            </div>
          </q-card-section>

        </q-card>
      </q-dialog>

  </q-page>
</template>

<script>
import { useRouter } from 'vue-router'
import { defineComponent, getCurrentInstance, onMounted, ref, provide} from 'vue'
import AddActeur from "components/Acteur/CreateDessin.vue";
export default defineComponent({
  // name: 'PageName',
  components: {
    AddActeur,
  },

  setup() {
    const route = useRouter()
    const instance = getCurrentInstance();
    let bachModal = ref(false)
    let helpModal = ref(true)
    const saveMadal = ref(false);
    provide("saveMadal", saveMadal)
    provide("bachModal", bachModal)

    let carre = ref([])
    provide("carre", carre)

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

        mymap.flyTo(new L.LatLng(e.latlng.lat , e.latlng.lng), 19);
      }

      L.control.zoom({
        position: 'topright'
      }).addTo(mymap)

      L.control.measure({
         primaryLengthUnit: 'kilometers', 
         secondaryLengthUnit: 'meter',
         primaryAreaUnit: 'acres', 
         secondaryAreaUnit: undefined,
      }).addTo(mymap);

      L.control.scale({
        metric: true,
        imperial: false,
        position: "bottomleft",
      }).addTo(mymap);

    
      mymap.locate({ setView: true, maxZoom: 10});
      mymap.on("locationfound", onLocationFound);

      mymap.on('measurefinish', function(evt) {
        writeResults(evt);
      });

      function writeResults(results) {
        carre.value = results
        console.log(carre.value);
      }

      mymap.on('save', function() {
        onSave()
      });

      setTimeout(() => {
        helpModal.value = false
      }, 2000)
    })

    let onSave = async () => {
      saveMadal.value = true
    }

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