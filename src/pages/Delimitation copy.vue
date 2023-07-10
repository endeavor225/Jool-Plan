<template>
  <q-page>
    
    <div>
      <div id="mapid" style=" width: 100%; height: 100vh;"> </div>
    </div>


    <div class="fixed-top z-max" v-if="helpModal === false"
      style="margin-left: 15%; 
        margin-right: 15%; 
        border-radius: 0px 0px 15px 15px;
        background: rgba(255, 255, 255, 0.8);
        backdrop-filter: blur(3px)"
      >
      
      <div clickable class="text-center q-ma-sm" style="margin-left:65px; margin-right:65px">
        <q-separator size="5px" color="grey-8" inset @click="onHelp" />
      </div>

    </div>

    <div class="fixed-top-left z-max q-mt-lg q-ml-sm">
        <q-btn round color="secondary" icon="chevron_left" @click="onBack" />
    </div>


    <q-footer
        v-if="!saveMadal"
        reveal
        class="bg-grey-4 text-secondary z-max"
        style="height: 60px"
      >
        <div v-if="!commencer" class="row justify-between">
          <div class="col-4"></div>
          <div style="margin-top: -30px" class="text-center col-4">
            <q-btn
              push
              color="teal"
              size="22px"
              round
              icon="play_arrow"
              class="position-relative"
              @click="commencer = !commencer"
            />
          </div>
          <div class="col-4"></div>
        </div>

        <div v-if="commencer" class="row justify-between">
          <div class="col-5">
            <div class="text-center text-grey-8" v-if="items.length !== 0">
              <div v-if="!terminer">
                <q-btn flat rounded icon="clear" color="negative" @click="deletePosition" /> <br />
                <span class="text-caption">Supprimer le dernier point</span>
              </div>

              <div v-if="terminer">
                <q-btn flat rounded icon="cancel" color="negative" @click="onRestart" /> <br />
                <span class="text-caption">Reprendre la délimitation</span>
              </div>
            </div>
          </div>

          <div style="margin-top: -30px" class="text-center col-2">
            <q-btn
              v-if="!terminer"
              push
              color="teal"
              size="22px"
              round
              icon="location_on"
              class="position-relative"
              @click="getPosition"
            />
            <!-- <q-btn
              disable
              v-if="terminer"
              push
              color="teal"
              size="22px"
              round
              icon="location_on"
              class="position-relative"
              @click="onAnnuler"
            /> -->
          </div>
          <div class="col-5">
            <div class="text-center text-grey-8" v-if="items.length > 3">
              <div v-if="!terminer">
                <q-btn flat rounded icon="check" @click="finish"/> <br />
                <span class="text-caption">Terminer</span>
              </div>
              <div v-if="terminer">
                <q-btn flat color="teal" rounded icon="done_all" @click="save"/> <br />
                <span class="text-caption">Valider</span>
              </div>
          </div>
          </div>
        </div>
      </q-footer>

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
          background: rgba(255, 255, 255, 0.8);
          backdrop-filter: blur(3px)"
          
        >

          <q-card-section class="q-ma-none">
            <div align="center"  class="text-weight-light" style="font-size:12px">
              Appuyez sur le bouton <q-avatar square size="13px"><img src="StartStop.svg"> </q-avatar> pour commencer la délimitation et déplacez-vous dans la parcelle pour la délimiter. Une fois terminer appuyer sur le bouton «Terminer» puis sur «Valider».
            </div>
            <div clickable class="q-mt-md text-center" style="margin-left:50px; margin-right:50px">
              <q-separator size="5px" color="grey-8" inset @click="onHelp"/>
            </div>
          </q-card-section>

        </q-card>
      </q-dialog>


      <q-dialog v-model="restartaMadal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down">
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
            <h4 align="center" class="text-weight-light" style="margin-bottom:35px">RECOMMENCER</h4>
          </div>

          <q-card-section class="q-ma-md" style="margin-top: 20%">
            <div align="center"  class="text-weight-regular" style="font-size:20px">Voulez-vous reprendre la délimitation ?</div>
          </q-card-section>

          <q-card-actions align="around" style="margin-top: 10%">
            <q-btn rounded color="secondary" v-close-popup >
              <span style="font-size: 20px">Non</span> 
            </q-btn>
            <q-btn rounded text-color="black" color="grey-2" @click="onOK">
              <span style="font-size: 20px">Oui</span> 
            </q-btn>
          </q-card-actions>
      
        </q-card>
      </q-dialog>

  </q-page>
</template>

<script>
import { ref, defineComponent, getCurrentInstance, onMounted, onBeforeUnmount, inject, provide} from "vue";
import { Geolocation } from '@capacitor/geolocation';
import { Plugins } from "@capacitor/core";
import { MainServiceModule } from "WeflyTrackingNew";
import AddActeur from "components/Acteur/Create.vue";
export default defineComponent({
  // name: 'PageName',

  components: {
    AddActeur,
  },

  setup() {
    const {MainServiceModulePlugin} = Plugins
    let ServiceModule = new MainServiceModule ()
    const instance = getCurrentInstance()
    let mymap;
    let bachModal = ref(false)
    let helpModal = ref(true)
    let commencer = ref(false);
    let terminer = ref(false);
    let items = ref([])
    let currentPosition = ref()
    let positionCurrent = ref()
    let location = ref()
    let geoId;
    let layer ;
    let layers = [];
    let annulMadal = ref(false);
    let restartaMadal = ref(false);

    let saveMadal = ref(false);
    provide("saveMadal", saveMadal)

    let carre = [];
    provide("carre", carre)

    onMounted( async () =>{
      mymap = L.map('mapid',{
         zoomControl: false, 
      }).fitWorld();

      L.tileLayer('http://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',{
        minZoom: 5,
        maxZoom: 20,
        subdomains:['mt0','mt1','mt2','mt3']
      }).addTo(mymap);

      mymap.locate({setView: true, maxZoom: 15});
  
      function onLocationFound(e) {
        var radius = e.accuracy;
        L.marker(e.latlng).addTo(mymap) 
          .bindPopup("Vous êtes à " + radius.toPrecision(4)  + " mètres de ce point")
          .openPopup();

        mymap.flyTo(new L.LatLng(e.latlng.lat , e.latlng.lng), 19);
      }

      L.control.zoom({
        position: 'topright'
      }).addTo(mymap);
      
      
      //mymap.locate({ setView: true, maxZoom: 10});
      mymap.on("locationfound", onLocationFound);

      setTimeout(() => {
        helpModal.value = false
      }, 2000)

    })

    let onBack = () => {
      bachModal.value = true
    }

    let onHelp = () => {
      helpModal.value = !helpModal.value
    }

    // ========================================================

    //Personnaliser le marker
    var LeafIcon = L.icon({
      iconUrl: 'marker-icon.png',
      iconSize:     [20, 30],
      iconAnchor:   [10, 30],
      popupAnchor:  [-1, -23]
    });


    let compteur = ref(0)
    async function getCurrentPosition() {
      compteur.value++
      geoId = await Geolocation.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 3000,
        maximumAge: 0
      });
      items.value.push(geoId.coords);
      layer = L.marker([geoId.coords.latitude, geoId.coords.longitude], {icon: LeafIcon})
        .addTo(mymap).bindPopup("Position " + compteur.value)
        .openPopup();
      layers.push(layer)
    }
    
    let getCurrentLocation = async () => {
        try {
          compteur.value++
          let res = await ServiceModule.getCurrentPosition({
            enableHighAccuracy: false,
            timeout: 5000,
            maximumAge: 0
          })

          currentPosition.value = JSON.stringify(res);
          positionCurrent.value = JSON.parse(currentPosition.value);

          for (const key in positionCurrent.value) {
            location.value = JSON.parse(positionCurrent.value[key]);
          }

          items.value.push(location.value);

          layer = L.marker([location.value.latitude, location.value.longitude], 
            {icon: LeafIcon})
            .addTo(mymap).bindPopup("Position " + compteur.value)
            .openPopup();
          
          layers.push(layer)

        } catch (error) {
          console.log(error);
        }
    }

     let getPosition = async () => {
      //await getCurrentPosition()
      await getCurrentLocation()
    }

    let polygon = ref()
    //Pout dessiner le polygone
    let point = []
    let finish = () => {
      for (const coord of items.value){
        carre.push([coord.longitude, coord.latitude]);
        point.push([coord.latitude, coord.longitude,]);
      }
      console.log(point);
      polygon.value = L.polygon(point).addTo(mymap)
      mymap.fitBounds(polygon.value.getBounds())
      terminer.value = true
    }

    let deletePosition = async () => {
       items.value.pop()
       let last = layers.pop()
       mymap.removeLayer(last);
       compteur.value = compteur.value - 1
    }

    let onAnnuler =() => {
      annulMadal.value = true
    }

    let onRestart = () => {
      restartaMadal.value = true
    }

    let save = () => {
      saveMadal.value = true
    }

    let onOK = () => {
      for(const layer of layers){
        mymap.removeLayer(layer)
      }
      mymap.removeLayer(polygon.value);
      items.value = []
      terminer.value = false
      compteur.value = 0
      carre = []
      point = []
      restartaMadal.value = false
    }

  

    return{
      onBack,
      bachModal,
      helpModal,
      onHelp,
      commencer,
      terminer,
      items,
      onRestart,
      deletePosition,
      onAnnuler,
      finish,
      getPosition,
      getCurrentLocation,
      save,
      restartaMadal,
      onOK,
      saveMadal
    }
  }
})
</script>

<style scoped>
.container:before {
	content: "";
	position: absolute;
	background: inherit;
	z-index: -1;
	inset: 0;
	filter: blur(10px);
	margin: -20px;
}

</style>