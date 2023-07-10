<template>
  <q-page>

    <div>
      <div id="mapid" style=" width: 100%; height: 105vh;"> </div>
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
      class="bg-white text-secondary z-max"
      style="height: 60px;"
    >
      <div class="row justify-between">
        <div class="col-5">
          <div class="text-center text-grey-8" v-if="items.length !== 0">
            <div v-if="!terminer">
              <q-btn flat rounded icon="clear" color="negative" @click="deletePosition" /> <br />
              <span class="text-caption">Supprimer le dernier point</span>
            </div>

            <div v-if="items.length !== 0 && terminer">
              <q-btn flat rounded icon="cancel" color="negative" @click="onRestart" /> <br />
              <span class="text-caption">Reprendre la délimitation</span>
            </div>
          </div>
        </div>

        <div style="margin-top: -30px" class="text-center col-2">
          <q-btn
            v-if="commencer && !terminer"
            push
            color="teal"
            size="22px"
            round
            icon="pause"
            class="position-relative"
            @click="stopWatchPosition"
          />

          <q-btn
            v-if="!commencer"
            push
            color="teal"
            size="22px"
            round
            icon="play_arrow"
            class="position-relative"
            @click="getPosition"
          />
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
          <q-btn rounded text-color="black" color="grey-2" @click="onCancel">
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

    <q-dialog full-width v-model="continuModal" persistent class="z-max" >
      <div style="border-radius:15px;">
        <q-card class="continuClass q-px-sm q-pb-md">
          <q-card-section class="row items-center q-pa-none q-pt-md">
            <div class="text-h6 text-center" style="width: 100%;">Info ! </div>
          </q-card-section>
          <q-card-section>
            <div class="text-weight-light" style="font-size:14px">
            Il y'a une mission de délimitation en cours, voulez-vous la continuer ?
            </div>
          </q-card-section>
          <q-card-actions
            align="center"
            class=" q-mb-sm row justify-between"
          >
            <q-btn
              label="NON"
              class="text-weight-regular"
              color="warning"
              style="width: 45%; border-radius:5px"
              @click="onResetMission"
            />
            <q-btn
              type="submit"
              label="OUI"
              class="text-weight-regular"
              color="secondary"
              style="width: 45%; border-radius:5px"
              @click="onContinuMission"
            >
            </q-btn>
          </q-card-actions>
        </q-card>
      </div>
    </q-dialog>
  </q-page>
</template>

<script>
import { useRouter } from 'vue-router'
import { ref, defineComponent, getCurrentInstance, onMounted, inject, provide, watch} from "vue";
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
    console.log("ServiceModule", ServiceModule);
    const instance = getCurrentInstance()
    const router = useRouter()
    let mymap;
    let bachModal = ref(false)
    let helpModal = ref(true)
    let commencer = ref(false);
    let terminer = ref(false);
    let items = ref([])
    let currentPosition = ref()
    let positionCurrent = ref()
    let location = ref()
    let layers = [];
    let annulMadal = ref(false);
    let restartaMadal = ref(false);
    let compteur = ref(0)
    let continuModal = ref(false);

    let saveMadal = ref(false);
    provide("saveMadal", saveMadal)

    let point = []
    let carre = [];
    provide("carre", carre)

    var layerGroup = L.layerGroup();
    var polyline;
    var circle;

    onMounted( async () =>{
      /* mymap = L.map('mapid',{
         zoomControl: false,
      }).fitWorld(); */

      let tileMap = L.tileLayer('https://tiles.stadiamaps.com/tiles/outdoors/{z}/{x}/{y}{r}.png', {
        minZoom: 6,
        maxZoom: 20,
        attribution: '&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
      })

      mymap = L.map('mapid', {
        layers: [tileMap],
        zoomControl: false,
      })

      mymap.removeLayer(layerGroup);
      mymap.addLayer(layerGroup);

      function onLocationFound(e) {
        var radius = e.accuracy;
        let marker = L.marker(e.latlng)
          .bindPopup("Vous êtes à " + radius.toPrecision(4)  + " mètres de ce point")
          .openPopup();

        mymap.flyTo(new L.LatLng(e.latlng.lat , e.latlng.lng), 19);
        layerGroup.addLayer(marker)
      }

      L.control.zoom({
        position: 'topright'
      }).addTo(mymap);

      mymap.locate({setView: true, maxZoom: 15});
      mymap.on("locationfound", onLocationFound);

      setTimeout(() => {
        helpModal.value = false
      }, 2000)

      if (JSON.parse(localStorage.getItem('coords-list')) != null) {
        continuModal.value = true
      }
    })

    let onBack = () => {
      bachModal.value = true
    }

    let onCancel = async () => {
      mymap.removeLayer(layerGroup);
      if (polyline) {
        mymap.removeLayer(polyline);
      }
      if (circle) {
        mymap.removeLayer(circle);
      }
      await stopWatchPosition()
      router.push('accueil')
    }

    let onHelp = () => {
      helpModal.value = !helpModal.value
    }

    watch(items, async () => {
      console.log("Items taille",items.value.length);
      if (items.value.length === 0) {
        if (circle) {
            mymap.removeLayer(circle);
          }
      }
    });

    let watchPosition = async () => {
      terminer.value = false;
      try {
        await ServiceModule.watchPosition({
          enableHighAccuracy: false,
          timeout: 3,
          maximumAge: 0
        }, async (position, err) => {

          if (err) {
            console.log('watchPosition onErr ', err);
          }

          if (position) {
            commencer.value = true
            console.log("position", JSON.stringify(position));
            currentPosition.value = JSON.stringify(position);
            positionCurrent.value = JSON.parse(currentPosition.value);

            for (const key in positionCurrent.value) {
              location.value = JSON.parse(positionCurrent.value[key]);
            }

            items.value.push(location.value);
            localStorage.setItem('coords-list', JSON.stringify(items.value))

            let pointPolyline = []
            for (const coord of items.value){
              carre.push([coord.longitude, coord.latitude]);
              pointPolyline.push([coord.latitude, coord.longitude]);

              if (circle) {
                mymap.removeLayer(circle);
              }
              circle = L.circle([coord.latitude, coord.longitude], 0, {color: 'green',}).addTo(mymap);
            }

            if (polyline) {
              mymap.removeLayer(polyline);
            }
            polyline = L.polyline(pointPolyline, {color: 'green', weight: 2, opacity: 0.5}).addTo(mymap);
          }
        })
      } catch (error) {
        console.log("error",error);
      }
    }

    let getPosition = async () => {
      await stopWatchPosition()
      await watchPosition()
    }

    let stopWatchPosition = async () => {
      commencer.value = false;
      ServiceModule.stopWatchPosition().catch(err => {
        console.log( 'stopWatchPosition onErr ', err);
      }).then( res => {
        console.log('stopWatchPosition onSucces ',res);
        //valueIsWatchStop = true;
      });
    }

    let polygon = ref()
    //Pout dessiner le polygone

    let finish = async () => {
      await stopWatchPosition()
      for (const coord of items.value){
        carre.push([coord.longitude, coord.latitude]);
        point.push([coord.latitude, coord.longitude,]);
      }

      polygon.value = L.polygon(point).addTo(mymap)
      mymap.fitBounds(polygon.value.getBounds())
      terminer.value = true
    }

    let deletePosition = async () => {
      items.value.pop()
      localStorage.setItem('coords-list', JSON.stringify(items.value))
      let pointPolyline = []
      for (const coord of items.value){
        carre.push([coord.longitude, coord.latitude]);
        pointPolyline.push([coord.latitude, coord.longitude]);

        if (circle) {
          mymap.removeLayer(circle);
        }
        circle = L.circle([coord.latitude, coord.longitude], 0, {color: 'green',}).addTo(mymap);
      }

      if (polyline) {
        mymap.removeLayer(polyline);
      }
      polyline = L.polyline(pointPolyline, {color: 'green', weight: 2, opacity: 0.5}).addTo(mymap);

      if (items.value.length === 0) {
        if (circle) {
            mymap.removeLayer(circle);
          }
      }
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
      mymap.removeLayer(polyline);
      localStorage.removeItem('coords-list');
      items.value = []
      terminer.value = false
      compteur.value = 0
      carre = []
      point = []
      restartaMadal.value = false
    }

    let onResetMission = () => {
      continuModal.value = false
      localStorage.removeItem('coords-list');
    }

    let onContinuMission = () => {
      continuModal.value = false

      items.value = JSON.parse(localStorage.getItem('coords-list'))
      console.log("items.value", JSON.stringify(items.value));

      let pointPolyline = []
      for (const coord of items.value){
        carre.push([coord.longitude, coord.latitude]);
        pointPolyline.push([coord.latitude, coord.longitude]);

        if (circle) {
          mymap.removeLayer(circle);
        }
        circle = L.circle([coord.latitude, coord.longitude], 0, {color: 'green',}).addTo(mymap);
        //layerGroup.addLayer(circle)
      }

      if (polyline) {
        mymap.removeLayer(polyline);
      }

      polyline = L.polyline(pointPolyline, {color: 'green', weight: 2, opacity: 0.5}).addTo(mymap)
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
      save,
      restartaMadal,
      onOK,
      saveMadal,
      stopWatchPosition,
      onCancel,
      continuModal,
      onResetMission,
      onContinuMission
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

.continuClass {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}

</style>
