<template>
  <transition
    appear
    enter-active-class="animated fadeInRight"
    leave-active-class="animated fadeOutRight"
  >
    <q-page>
      <div id="mapid" style="width: 100%; height: 105vh; position:fixed"> </div>

      <div class="fixed-top-right z-max q-mr-sm" style="margin-top:40px" >
        <transition
          appear
          enter-active-class="animated fadeInRight"
          leave-active-class="animated fadeOutRight"
        >
          <q-card v-if="!checkListModal">
            <q-btn
              class="no-padding"
              unelevated
              @click="onZoomIn"
            >
              <q-avatar size="2.5rem">
                <img src="Plus.svg">
              </q-avatar>
            </q-btn>
            <br>

            <q-btn
              class="no-padding"
              unelevated
              @click="onZoomOut"
            >
              <q-avatar size="2.5rem">
                <img src="Moins.svg">
              </q-avatar>
            </q-btn>
          </q-card>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInRight"
          leave-active-class="animated fadeOutRight"
        >
          <q-card v-if="!checkListModal" style="top: 15px">
            <q-btn
              class="no-padding"
              unelevated
              @click="onGetMeteo"
            >
              <q-avatar size="2.5rem">
                <img src="Meteo.svg">
              </q-avatar>
            </q-btn>
            <br>
          </q-card>
        </transition>
      </div>

      <div class="fixed-top-left z-max q-ml-sm" style="margin-top:60px">
        <InfoMission/>
      </div>

      <div>
        <q-page-sticky class="z-max" position="bottom" :offset="[0, 2]">
          <q-btn round class="q-pa-none q-ma-md" color="dark" @click="onBack">
            <q-avatar square size="25px">
              <img src="plan/Home.svg">
            </q-avatar>
          </q-btn>
        </q-page-sticky>
      </div>

      <div v-if="showStartBouton && !startMission && droneConnection">
        <q-page-sticky class="z-max" position="bottom-right" :offset="[33, 86]">
          <q-btn round flat class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onChecklist">
            <q-avatar square size="59px">
              <q-img src="plan/Start.svg"/>
            </q-avatar>
              <template v-slot:loading>
                <q-spinner-ios size="40px"/>
              </template>
          </q-btn>
        </q-page-sticky>
      </div>

      <div v-if="checkSucces && !startMission">
        <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 70]">
          <q-btn round flat class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onStart">
            <q-avatar square size="90px">
              <q-img src="plan/Decoller.gif"/>
            </q-avatar>
              <template v-slot:loading>
                <q-spinner-ios size="40px"/>
              </template>
          </q-btn>
        </q-page-sticky>
      </div>

      <div v-if="showContinuBouton && !continuLastMission && droneConnection">
        <q-page-sticky class="z-max" position="bottom-right" :offset="[33, 86]">
          <q-btn round flat class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onChecklistContinuMission">
            <q-avatar square size="59px">
              <q-img src="plan/Restart.svg"/>
            </q-avatar>
              <template v-slot:loading>
                <q-spinner-ios size="40px"/>
              </template>
          </q-btn>
        </q-page-sticky>
      </div>

      <div v-if="checkSuccesContinu && !continuLastMission">
        <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 70]">
          <q-btn round flat class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onStartContinuMission">
            <q-avatar square size="90px">
                <q-img src="plan/Continue.gif"/>
              </q-avatar>

            <template v-slot:loading>
              <q-spinner-ios size="35px"/>
            </template>
          </q-btn>
        </q-page-sticky>
      </div>

      <div v-if="startMission && !continuLastMission || !startMission && continuLastMission ">
        <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 30]">
          <q-btn fab class="q-pa-none" color="negative" :loading="submitting" @click="atterrirModal = true">
            <q-item-label>
            <q-avatar square size="30px" class="q-pt-sm">
                <q-img src="plan/Atterir.svg"/>
              </q-avatar> <br>
              <span style="font-size:7px; position: relative; top:-4px">Attérir</span>
            </q-item-label>

            <template v-slot:loading>
              <q-spinner-ios size="35px"/>
            </template>
          </q-btn>
        </q-page-sticky>
      </div>

      <!-- Modal de comfirmation de l'annulation -->
      <q-dialog v-model="bachModal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down">
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
      </q-dialog>

      <!-- Modal de check list -->
      <q-dialog v-model="checkListModal" maximized persistent transition-show="slide-down" transition-hide="slide-up">
        <CheckList />
      </q-dialog>

      <!-- Modal de mission en cours -->
      <q-dialog full-width v-model="continuModal" persistent class="z-max" >
        <div style="border-radius:15px">
          <q-card class="continuClass q-px-sm q-pb-md">
            <q-card-section class="row items-center q-pa-none q-pt-md">
              <div class="text-h6 text-center" style="width: 100%;">Confirmation ! </div>
            </q-card-section>
            <q-card-section>
              <div>
                Il y'a une mission en cours, voulez-vous la continuer ?
              </div>
            </q-card-section>
            <q-card-actions
              align="center"
              class=" q-mb-sm row justify-between"
            >
              <q-btn
                label="NON"
                color="white"
                class="text-weight-regular text-black"
                style="width: 45%; border-radius:5px"
                @click="noComfirm"
              />
              <q-btn
                type="submit"
                label="OUI"
                class="text-weight-regular"
                color="secondary"
                style="width: 45%; border-radius:5px"
                @click="onComfirm"
              >
              </q-btn>
            </q-card-actions>
          </q-card>
        </div>
      </q-dialog>

      <!-- Modal d'annulation de mission-->
      <q-dialog full-width v-model="atterrirModal" persistent class="z-max" >
        <div style="border-radius:15px">
          <q-card class="continuClass q-px-sm q-pb-md">
            <q-card-section class="row items-center q-pa-none q-pt-md">
              <div class="text-h6 text-center" style="width: 100%;">Confirmation ! </div>
            </q-card-section>
            <q-card-section>
              <div>
                Souhaitez-vous faire atterrir le drone ?
              </div>
            </q-card-section>
            <q-card-actions
              align="center"
              class=" q-mb-sm row justify-between"
            >
              <q-btn
                label="NON"
                color="white"
                class="text-weight-regular text-black"
                style="width: 45%; border-radius:5px"
                v-close-popup
              />
              <q-btn
                type="submit"
                label="OUI"
                class="text-weight-regular"
                color="secondary"
                style="width: 45%; border-radius:5px"
                @click="onReturnToHome"
              >
              </q-btn>
            </q-card-actions>
          </q-card>
        </div>
      </q-dialog>

      <!-- Modal fin mission -->
      <q-dialog v-model="finishModal" position="bottom" persistent class="z-max" >
        <MissionFinish/>
      </q-dialog>

      <!-- Modal de météo -->
      <q-dialog v-model="meteoModal" full-width transition-show="slide-up" transition-hide="slide-down">
        <Meteo :propsCoords="coordinate"/>
      </q-dialog>

    </q-page>
  </transition>
</template>

<script>
import { Notify, useQuasar } from 'quasar'
import { format, secondsToMinutes } from 'date-fns'
import { useRouter } from 'vue-router'
import { Plugins } from "@capacitor/core";
import { MainServiceModule } from "WeflyTrackingNew";
import { JoolMapping } from "joolmapping";
import { ref, defineComponent, getCurrentInstance, onMounted, inject, watch, provide} from "vue";
import AddActeur from "components/Acteur/Create.vue";
import MissionFinish from "src/components/Mission/MissionFinish.vue"
import Meteo from "src/components/Mission/Meteo.vue"
import CheckList from "src/components/Mission/Checklist.vue"
import InfoMission from "src/components/Mission/InfoMission.vue"

export default defineComponent({
  // name: 'PageName',

  components: {
    AddActeur,
    MissionFinish,
    Meteo,
    CheckList,
    InfoMission
  },

  props: {
    itemPlan: {
      default() {
        return {};
      },
    },
  },

  setup(props) {
    const {MainServiceModulePlugin} = Plugins
    let ServiceModule = new MainServiceModule ()
    const $q = useQuasar()
    const router = useRouter()
    const instance = getCurrentInstance()
    const service = "updatePlan";
    let state = inject(service);
    const serviceProjets = "searchProjet";
    let stateProjet = inject(serviceProjets);
    let formData = ref({})
    let eForm = ref("eForm")
    let infoPlan = ref()
    let mymap;
    let groupLayers = L.layerGroup()
    let bachModal = ref(false)
    let checkListModal = ref(false)
    provide("checkListModal", checkListModal)
    let start = ref(false)
    let superficie = ref()
    provide("superficie", superficie)
    let infoDrone = ref({
      chargeRemainingInPercent:0,
      uplinkSignalQuality:0,
      altitudeInMeters:0,
      speedInMeterPerSec:0,
    })
    provide("infoDrone", infoDrone)
    let stateConnect = ref(true)
    let stateProgressing = ref(true)
    let stateMission = ref(false)
    const submitting = ref(false)
    provide("submitting", submitting)
    let flyTime = ref(0)
    provide("flyTime", flyTime)
    let finishModal = ref(false)
    let progressing = ref()
    provide("progressing", progressing)
    let phonePosition = ref({})
    let startMission = ref(false)
    let showStartBouton = ref(true)
    let continuModal = ref(false)
    let continuLastMission = ref(false)
    let showContinuBouton = ref(false)
    let coordinate = ref({})
    let checkSucces = ref(false)
    let checkSuccesContinu = ref(false)
    let atterrirModal =  ref(false)
    let drone;
    let layerPolygon;
    let currentPosition = ref()
    let positionCurrent = ref()
    let location = ref()
    let positionDrone = ref()
    let meteoModal = ref(false)
    provide("meteoModal", meteoModal)
    let infoMeteo = ref([])
    provide("infoMeteo", infoMeteo)
    let icon_url = ref("")
    let checkListe = ref({
      gps : null,
      acces : null,
      drone : null,
      sdcard : null,
      diagnostic: null,
      camera : null,
      controle : null,
      planDeVol : null,
      phoneBattery : null,
      droneBattery : null,
    })
    provide("checkListe", checkListe)
    let infoMission = ref({
      flyTime: 0,
      imageCount: 0,
      battery: 0,
      speed: 0,
      //surveyPoints
    })
    provide("infoMission", infoMission)
    let checkList = ref({})
    let pointsSurvol = ref([])
    let inforeturn = ref()
    let readyToTakeOff = ref(false)
    let droneConnection = ref(false)

    const token = localStorage.getItem('jool_Plan_id-session-app')

    JoolMapping.addListener('onReadyToTakeOff', (info) => {
      readyToTakeOff.value = info.isDroneReady
    });

    /* ############################ ===> Fonction de connexion au drone <=== ############################*/
    let initial = async () => {
      stateConnect.value = false
      stateConnect.value = false

      JoolMapping.ini()
      setTimeout(async () => {
        console.log('connect Start');
        let errorHappen = false;
        JoolMapping.connect().catch(err => {
          stateConnect.value = true
          console.log('connect onErr ', err);
          Notify.create({
            type: 'warning',
            message: 'Veuillez-vous connecter au drone',
            timeout: 10000,
            position: 'top',
          })
          errorHappen = true;
        }).then(async res => {
          if (errorHappen){
            return;
          }else {
            droneConnection.value = true
            stateConnect.value = true
            console.log('connect onSucces res', res);
            Notify.create({
              type: 'positive',
              message: 'Connexion au drone réussit',
              timeout: 5000,
              position: 'top'
            })
            let isDroneTelemetryFindOne = false;
            await JoolMapping.watchDroneTelemetry(async (position, err) => {

              if (err) {
                console.log('watchDroneState onErr ', err);
                return;
              }
              if (position) {
                //console.log('watchDroneState onSucces position:', JSON.stringify(position));
                infoDrone.value = position
                positionDrone.value = position

                // Position du drone en temps réel
                let longitude = position.longitude
                let latitude = position.latitude
                await getDronePosition(latitude, longitude)

                // Coordonnées du téléphone
                let lng = location.value.longitude
                let lat = location.value.latitude
                await getPhonePosition(lat, lng)

                if (!isDroneTelemetryFindOne){
                  isDroneTelemetryFindOne = true;
                }
              }
            });
          }
        });
      },1000)
    }

    /* ##################### ===> Fonction pour prendre la position du téléphone <=== ##################*/
    let watchPosition = async () => {
      try {
        await ServiceModule.watchPosition({
          enableHighAccuracy: false,
          timeout: 5,
          maximumAge: 0
        }, async (position, err) => {
          if (err) {
            console.log('watchPosition onErr ', err);
          }
          if (position) {
            //console.log("watch position", JSON.stringify(position));
            currentPosition.value = JSON.stringify(position);
            positionCurrent.value = JSON.parse(currentPosition.value);

            for (const key in positionCurrent.value) {
              location.value = JSON.parse(positionCurrent.value[key]);
            }
            // console.log("watch location", JSON.stringify(location));
            let lng = location.value.longitude
            let lat = location.value.latitude
            await getPhonePosition(lat, lng)
          }
        })

      } catch (error) {
        console.log("error",error);
      }
    }

    /* ############################ ===> Fonction ajouter la position du phone sur la map  <=== ############################*/
    //Personnaliser le marker du telephone
    var phoneIcon = L.icon({
      iconUrl: 'plan/Position.png',
      iconSize:     [25, 40],
      iconAnchor:   [13, 40],
      popupAnchor:  [-1, -35]
    });

    var phone;
    let getPhonePosition = async (lat, lng) => {

      let positionTel = L.latLng(location.value.latitude, location.value.longitude);
      let centrePolygone = L.latLng(coordinate.value.latitude,  coordinate.value.longitude)

      // Distance phone - Centroid
      let disCentroid = positionTel.distanceTo(centrePolygone);
      console.log(disCentroid, 'metre');
      let uniCentroid = "Mètre"
      if (disCentroid >= 1000) {
        disCentroid = disCentroid / 1000
        uniCentroid = "Km"
      }

      if (positionDrone.value) {
        let dronPosition = L.latLng(positionDrone.value.latitude, positionDrone.value.longitude);

        // Distance phone - drone
        var distance = positionTel.distanceTo(dronPosition);
        console.log(distance, 'metre');
        let unite = "Mètre"
        if (distance >= 1000) {
          distance = distance / 1000
          unite = "Km"
        }

        if(phone){
          mymap.removeLayer(phone);
        }
        phone = L.marker([lat, lng],{icon: phoneIcon})
        .bindPopup("<b>Distance</b><br>" + "Vous êtes à : <br>" + distance.toFixed(2) + " " + unite + " du drone <br>" + disCentroid.toFixed(2) + " " + uniCentroid + " centre du polygone" );
        groupLayers.addLayer(phone)
      }else {
        if(phone){
          mymap.removeLayer(phone);
        }
        phone = L.marker([lat, lng],{icon: phoneIcon})
        .bindPopup("Vous êtes à " + disCentroid.toFixed(2) + " " + uniCentroid + " du centre du polygone")
        groupLayers.addLayer(phone)
      }
    }

    /* ############################ ===> Fonction onMunted <=== ############################*/
    onMounted( async () =>{
      infoPlan.value = JSON.parse(props.itemPlan)
      formData.value = infoPlan.value
      let polygon = JSON.parse(infoPlan.value.polygone)
      let centroid = JSON.parse(polygon.centroid)

      //Formatage pour centrer la map
      if (centroid.features) {
        for (const coord of centroid.features[0].geometry.coordinates){
          if (coord > 0) {
            console.log("latitude features ==>",coord);
            coordinate.value.latitude = coord
          } else{
            console.log("longitude features ==>", coord);
            coordinate.value.longitude = coord
          }
        }
      }

      if (centroid.coordinates) {
        for (const coord of centroid.coordinates){
          if (coord > 0) {
            console.log("latitude coordinates ==>",coord);
            coordinate.value.latitude = coord
          } else{
            console.log("longitude coordinates ==>", coord);
            coordinate.value.longitude = coord
          }
        }
      }

      let tileMap = L.tileLayer('https://tiles.stadiamaps.com/tiles/outdoors/{z}/{x}/{y}{r}.png', {
        minZoom: 6,
        maxZoom: 20,
        attribution: '&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
      })

      mymap = L.map('mapid', {
        layers: [tileMap],
        zoomControl: false,
      }).setView([ coordinate.value.latitude,  coordinate.value.longitude], 16);

      /* L.control.zoom({
        position: 'topright'
      }).addTo(mymap); */

      function onLocationFound(e) {
        phonePosition.value = e.latlng
        console.log("phonePosition.value", phonePosition.value);
      }

      mymap.locate({});
      mymap.on("locationfound", onLocationFound);

      mymap.removeLayer(groupLayers);
      mymap.addLayer(groupLayers);

      let geometry = JSON.parse(polygon.geometrie)
      superficie.value = polygon.superficie.toFixed(2)

      if (layerPolygon) {
        mymap.removeLayer(layerPolygon);
      }
      layerPolygon = L.geoJSON(geometry)
      groupLayers.addLayer(layerPolygon)

      /* Position du telephone */
      await watchPosition()

      // Fonction de connexion au drone
      await initial()
    })

    /* ###################### ===> Fonction de vérification de mision en cours <=== ###################*/
    let onMissionExist = async () => {
      let errorHappen = false;
      // Continu old Mission
      await JoolMapping.isMissionExist().catch(async err => {
        errorHappen = true;
        console.log('isMissionExist onErr ==>',err);
      }).then(async (response) => {
        console.log('isMissionExist response',JSON.stringify(response));
        if(errorHappen){
          return;
        }else{
          if(!response.isExist){
            console.log('isMissionExist Not found mean isExist = false');
            await onGetInfoMissin()
            return;
          }else {
            console.log('isMissionExist Found mean isExist = true');
            continuModal.value = true
          }
        }
      });
      return;
    }

    // Si le drone n'est pas connect
    watch(stateConnect, async () => {
      if (!stateConnect.value) {
        $q.loading.show({
          message: 'Connexion au drone ...',
          boxClass: 'bg-grey-2 text-grey-9',
          spinnerColor: 'primary'
        })
      }else {
        $q.loading.hide()
        await onMissionExist()
      }
    })

    watch(stateProgressing, async () => {
      if(!stateProgressing.value){
        $q.loading.show({
          message: 'Configuration de mission ...',
          boxClass: 'bg-grey-2 text-grey-9',
          spinnerColor: 'primary'
        })
      }else{
        $q.loading.hide()
      }
    })

    /* ###################### ===> Fonction d'ajout du drone sur la map <=== ################### */
    //Personnaliser le marker du drone
    var droneIcon = L.icon({
      iconUrl: 'plan/Drone1.png',
      iconSize:     [45, 20],
      iconAnchor:   [22, 15],
      popupAnchor:  [-1, -12]
    });

    let getDronePosition = async (latitude, longitude) => {
      if(drone){
        mymap.removeLayer(drone);
      }
      drone = L.marker([latitude, longitude],{icon: droneIcon})
      groupLayers.addLayer(drone)
    }

    /* ###################### ===> Fonction pour configurer le drone <=== ################### */
    let onGetInfoMissin = async () => {
      stateProgressing.value = false
      let errorHappen = false;
      let coords;
      let poly = JSON.parse(infoPlan.value.polygone)
      let geo = JSON.parse(poly.geometrie)
      console.log("geo =======>", JSON.stringify(geo));

      if (geo.features) {
        let coord = JSON.stringify(geo.features[0].geometry.coordinates)
        let first = coord.substring(1)
        let last = first.substring(0, first.length - 1)
        coord = last

        coords = JSON.parse(coord)
        console.log("coordinate 1 ==> ", JSON.stringify(coords) );
      }

      if (geo.coordinates) {
        coords = geo.coordinates
        console.log("coordinate 2 ==> ", JSON.stringify(coords) );
      }

      let missionOptions = {};
      missionOptions.altitude = infoPlan.value.altitude
      missionOptions.exitMissionOnRCLost = false;
      missionOptions.autoGoHomeOnLowBattery = true;
      missionOptions.daylight = 1; // 2=SUNNY , 1=NORMAL
      missionOptions.phoneLatitude = phonePosition.value.lat // YOU PHONE latitude
      missionOptions.phoneLongitude = phonePosition.value.lng  // YOU PHONE longitude
      missionOptions.polygonPoints = {
        "type": "Feature",
        "properties": {},
        "geometry": {
          "type": "Polygon",
          "coordinates": coords
        }
      };

      await JoolMapping.setMissionSetting(missionOptions).catch(err =>{
        errorHappen = true;
        stateProgressing.value = true
        console.log("setMissionSetting  err",err);
        Notify.create({
          type: 'negative',
          message: 'Une erreur s\'est produite lors de la configuration de la mission',
          timeout: 5000,
          position: 'top'
        })
      }).then(async ()=>{
        if(errorHappen){
          return;
        }else {
          console.log("setMissionSetting  Succes");
          await JoolMapping.getMissionInfo(async (response) => {

            if (response.error){
              console.log("getMissionInfo Error Happen",response.error);
              return;
            }else {
              stateProgressing.value = true
              stateMission.value = true
              console.log("getMissionInfo  Succes");
              console.log("getMissionInfo response String",JSON.stringify(response));

              if (response.gridProgressing){
                console.log("getMissionInfo gridProgressing:",response.gridProgressing);
                return;
              }

              if (response.flyTime && response.imageCount
              && response.battery
              && response.speed
              && response.surveyPoints){
                console.log("getMissionInfo flyTime:",response.flyTime);
                console.log("getMissionInfo imageCount:",response.imageCount);
                console.log("getMissionInfo battery:",response.battery);
                console.log("getMissionInfo speed:",response.speed);
                console.log("getMissionInfo surveyPoints:",response.surveyPoints);
                console.log("performChecklist  Fired", JSON.stringify(response));

                infoMission.value = response
                console.log("infoMission.value Fired", JSON.stringify(infoMission.value));

                flyTime.value = secondsToMinutes(infoMission.value.flyTime) + 1

                // Convertir la chaine de caractère en tableau
                let string = infoMission.value.surveyPoints
                string = string.replace('[', '')
                string = string.replace(']', '')
                while (string.search('lat/lng: ') != -1){
                    string = string.replace('lat/lng: ', '')
                }
                let tab = string.split(', ')

                tab.forEach(element => {
                  element = element.replace('(', '')
                  element = element.replace(')', '')
                  let tab2 = element.split(',')
                  //console.table(tab2)

                  for (let index = 0; index < tab2.length; index++) {
                    tab2[index] = +tab2[index];
                  }
                  pointsSurvol.value.push(tab2)
                });
                console.table(pointsSurvol.value)
                console.log("pointsSurvol", JSON.stringify(pointsSurvol.value));

                var polyline = L.polyline(pointsSurvol.value, {color: 'green'})
                groupLayers.addLayer(polyline)
                mymap.flyToBounds(polyline.getBounds(),  {'duration':0.50});

                // Fonction de Mise a jour de plan
                await onUpdate()
              }
            }
          });
        }
      });
    }

    /* ###################### ===> Fonction de control avant le survol <=== ################### */
    let onChecklist = async () => {
      submitting.value = true
      checkListModal.value = true
      await JoolMapping.performChecklist({caseContinuMission:false},async (response) => {
        if (response.error){
          console.log("performChecklist Error Happen",response.error);
          return;
        }else {
          console.log("performChecklist response String",JSON.stringify(response));
          checkList.value = response

          if (response.phoneBattery === true){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            setTimeout(() =>{
              checkListe.value.phoneBattery = true
            }, 1000)
            return;
          }
          if (response.phoneBattery === false){
            console.log("performChecklist phoneBattery isError:"+response.phoneBattery);
            setTimeout(() =>{
              checkListe.value.phoneBattery = false
              submitting.value = false;
            }, 1000)
          }

          if (response.acces === true){
            console.log("performChecklist acces isOK:"+response.acces);
            setTimeout(() =>{
              checkListe.value.acces = true
            }, 2000)
            return;
          }
          if (response.acces === false){
            console.log("performChecklist acces isError:"+response.acces);
            setTimeout(() =>{
              checkListe.value.acces = false
              submitting.value = false;
            }, 2000)
          }

          if (response.config === true){
            console.log("performChecklist config isOK:"+response.config);
            setTimeout(() =>{
              checkListe.value.drone = true
            }, 3000)
            return;
          }
          if (response.config === false){
            console.log("performChecklist config isError:"+response.config);
            setTimeout(() =>{
              checkListe.value.drone = false
              submitting.value = false;
            }, 3000)
          }

          if (response.controle === true){
            console.log("performChecklist controle isOK:"+response.controle);
            setTimeout(() =>{
              checkListe.value.controle = true
            }, 4000)
            return;
          }
          if (response.controle === false){
            console.log("performChecklist controle isError:"+response.controle);
            setTimeout(() =>{
              checkListe.value.controle = false
              submitting.value = false;
            }, 4000)
          }

          if (response.sdcard === true){
            console.log("performChecklist sdcard isOK:"+response.sdcard);
            setTimeout(() =>{
              checkListe.value.sdcard = true
            }, 5000)
            return;
          }
          if (response.sdcard === false){
            console.log("performChecklist sdcard isError:"+response.sdcard);
            setTimeout(() =>{
              checkListe.value.sdcard = false
              submitting.value = false;
            }, 5000)
          }

          if (response.diagnostic){
            console.log("performChecklist diagnostic result:"+response.diagnostic);
            if (response.diagnostic.toString().includes("Ready to GO (Vision)") || response.diagnostic.toString().includes("Prêt à partir (GPS)")){
              console.log("performChecklist diagnostic result: DRONE IS READY TO TAKE OFF");
              setTimeout(() =>{
              checkListe.value.diagnostic = true
            }, 6000)
              return;
            }else {
              console.log("performChecklist diagnostic result: DRONE NOT READY");
              setTimeout(() =>{
                checkListe.value.diagnostic = false
                submitting.value = false;
              }, 6000)
            }
            return;
          }

          if (response.camera === true){
            console.log("performChecklist camera isOK:"+response.camera);
            setTimeout(() =>{
              checkListe.value.camera = true
            }, 7000)
            return;
          }
          if (response.camera === false){
            console.log("performChecklist camera isError:"+response.camera);
            setTimeout(() =>{
              checkListe.value.camera = false
              submitting.value = false;
            }, 7000)
          }

          if (response.flyplan === true){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            setTimeout(() =>{
              checkListe.value.planDeVol = true
            }, 8000)
            return;
          }
          if (response.flyplan === false){
            console.log("performChecklist flyplan isError:"+response.flyplan);
            setTimeout(() =>{
              checkListe.value.planDeVol = false
              submitting.value = false;
            }, 8000)
          }

          if (response.droneBattery === true){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            setTimeout(() =>{
              checkListe.value.droneBattery = true
            }, 9000)
            return;
          }
          if (response.droneBattery === false){
            console.log("performChecklist droneBattery isError:"+response.droneBattery);
            setTimeout(() =>{
              checkListe.value.droneBattery = false
              submitting.value = false;
            }, 9000)
          }

          if (response.gps === true){
            console.log("performChecklist gps isOK:"+response.gps);
            setTimeout(() =>{
              checkListe.value.gps = true
            }, 10000)
            return;
          }
          if (response.gps === false){
            console.log("performChecklist gps isError:"+response.gps);
            setTimeout(() =>{
              checkListe.value.gps = false
              submitting.value = false;
            }, 10000)
          }

          if (response.checkSucces){
            setTimeout(() => {
              submitting.value = false
              start.value = true
              checkListModal.value = false
              showStartBouton.value = false
              checkSucces.value = true

              Notify.create({
              type: 'positive',
              message: 'Prêt a décoller !',
              position: 'center',
              timeout: 2000,
            })
            }, 12000)

            setTimeout(() => {
              checkListe.value.gps = null
              checkListe.value.acces = null
              checkListe.value.drone = null
              checkListe.value.sdcard = null
              checkListe.value.camera = null
              checkListe.value.controle = null
              checkListe.value.planDeVol = null
              checkListe.value.phoneBattery = null
              checkListe.value.droneBattery = null
            },14000)
          }

          if(!response.checkSucces) {
            setTimeout(() => {
              submitting.value = false;
              console.log("checkSucces erreur", response.checkSucces);
              //checkListModal.value = false
            }, 10000)
          }
        }
      });
    }

    /* ###################### ===> Fonction pour demarrer la mission <=== ################### */
    let onStart = async () => {
      submitting.value = true

      setTimeout(async () => {
        console.log("performChecklist checkSucces Mean Everything is Ok");
        await JoolMapping.startWaypointMission(async (response) => {

          console.log("startWaypointMission detail:",JSON.stringify(response));

          if (response.error){
            console.log("startWaypointMission Error Happen",response.error);
            return;
          }

          if (response.startSucces){
            console.log("startWaypointMission MissionStart successful:", response.startSucces);
            setTimeout(() => {
              submitting.value = false;
              startMission.value = true;
              checkSucces.value = false
            },2000)
            return;
          }

          if (response.finishSucces){
            console.log("startWaypointMission MissionEnd successful",response.finishSucces);
            startMission.value = false
            showStartBouton.value = false
            await onMissionFinish()
            // await onUpdate(status)


            Notify.create({
              type: 'positive',
              message: 'Mission terminée avec succès',
              position: 'top',
              timeout: 11000,
            })
            return;
          }

          if (response.lowBattery){
            showContinuBouton.value = true
            startMission.value = false

            Notify.create({
              type: 'warning',
              message: 'La batterie du drone est faible !',
              position: 'top',
              timeout: 10000,
            })
            return;
          }
        });
        return;
      }, 1000)

      // Write Log in file
      console.log('logToFile Will Write');
      await JoolMapping.logToFile({message:"Hello log from CAPACITOR APP"}).catch(err => {
        errorHappen = true;
        console.log('logToFile onErr ', err);
      }).then(() =>{
        if(errorHappen){
          return;
        }else{
          console.log('logToFile Succes ');
        }
      });
    }

    let onFinish = async () => {
      start.value = false
      checkListModal.value = false
    }

    let onMissionFinish = async () => {
      finishModal.value = true
      progressing.value = 0.01

      do {
        setInterval(() => {
            progressing.value = Math.min(1, progressing.value + 0.1)
        }, 2000)
      } while (progressing.value === 1);

      watch(progressing, async () => {
        if (progressing.value === 1) {
          setTimeout(() => {
            finishModal.value = false
          }, 2000)
        }
      })

      watch(readyToTakeOff, async () => {
        if(readyToTakeOff.value === true){
          $q.loading.show({
            message: 'Finalisation de la mission ...',
          })

          setTimeout(() => {
            $q.loading.hide()
            groupLayers.clearLayers()
            router.push("/accueil")
            readyToTakeOff.value = false
          }, 5000)
        }
      })
    }

    /* let stateConnectToContinu = ref(true)
    // Si le drone n'est pas connect
    watch(stateConnectToContinu, async () => {
      if (!stateConnectToContinu.value) {
        $q.loading.show({
          message: 'Connexion au drone ...',
          boxClass: 'bg-grey-2 text-grey-9',
          spinnerColor: 'primary'
        })
      }else {
        $q.loading.hide()
        await onStartContinuMission()
      }
    }) */

    /* ###################### ===> Fonction pour continuer la mission <=== ################### */
    let onContinueMission = async () => {
      await onStartContinuMission()
    }

    let onChecklistContinuMission = async () => {
      submitting.value = true
      checkListModal.value = true

      console.log("performChecklist checkSucces Mean Everything is Ok");
      await JoolMapping.performChecklist({caseContinuMission:true},async (response) => {
        if (response.error){
          console.log("performChecklist Error Happen",response.error);
          return;
        }else {
          console.log("performChecklist response String",JSON.stringify(response));

          if (response.phoneBattery === true){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            setTimeout(() =>{
              checkListe.value.phoneBattery = true
            }, 1000)
            return;
          }
          if (response.phoneBattery === false){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            setTimeout(() =>{
              checkListe.value.phoneBattery = false
            }, 1000)
          }

          if (response.acces === true){
            console.log("performChecklist acces isOK:"+response.acces);
            setTimeout(() =>{
              checkListe.value.acces = true
            }, 2000)
            return;
          }
          if (response.acces === false){
            console.log("performChecklist acces isOK:"+response.acces);
            setTimeout(() =>{
              checkListe.value.acces = false
            }, 2000)
          }

          if (response.config === true){
            console.log("performChecklist config isOK:"+response.config);
            setTimeout(() =>{
              checkListe.value.drone = true
            }, 3000)
            return;
          }
          if (response.config === false){
            console.log("performChecklist config isOK:"+response.config);
            setTimeout(() =>{
              checkListe.value.drone = false
            }, 3000)
          }

          if (response.controle === true){
            console.log("performChecklist controle isOK:"+response.controle);
            setTimeout(() =>{
              checkListe.value.controle = true
            }, 4000)
            return;
          }
          if (response.controle === false){
            console.log("performChecklist controle isOK:"+response.controle);
            setTimeout(() =>{
              checkListe.value.controle = false
            }, 4000)
          }

          if (response.sdcard === true){
            console.log("performChecklist sdcard isOK:"+response.sdcard);
            setTimeout(() =>{
              checkListe.value.sdcard = true
            }, 5000)
            return;
          }
          if (response.sdcard === false){
            console.log("performChecklist sdcard isOK:"+response.sdcard);
            setTimeout(() =>{
              checkListe.value.sdcard = false
            }, 5000)
          }

          if (response.diagnostic){
            console.log("performChecklist diagnostic result:"+response.diagnostic);
            if (response.diagnostic.toString().includes("Ready to GO (Vision)") || response.diagnostic.toString().includes("Prêt à partir (GPS)")){
              console.log("performChecklist diagnostic result: DRONE IS READY TO TAKE OFF");
              setTimeout(() =>{
              checkListe.value.diagnostic = true
            }, 6000)
              return;
            }else {
              console.log("performChecklist diagnostic result: DRONE NOT READY");
              setTimeout(() =>{
                checkListe.value.diagnostic = false
                submitting.value = false;
              }, 6000)
            }
            return;
          }

          if (response.camera === true){
            console.log("performChecklist camera isOK:"+response.camera);
            setTimeout(() =>{
              checkListe.value.camera = true
            }, 7000)
            return;
          }
          if (response.camera === false){
            console.log("performChecklist camera isOK:"+response.camera);
            setTimeout(() =>{
              checkListe.value.camera = false
            }, 7000)
          }

          if (response.flyplan === true){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            setTimeout(() =>{
              checkListe.value.planDeVol = true
            }, 8000)
            return;
          }
          if (response.flyplan === false){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            setTimeout(() =>{
              checkListe.value.planDeVol = false
            }, 8000)
          }

          if (response.droneBattery === true){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            setTimeout(() =>{
              checkListe.value.droneBattery = true
            }, 9000)
            return;
          }
          if (response.droneBattery === false){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            setTimeout(() =>{
              checkListe.value.droneBattery = false
            }, 9000)
          }

          if (response.gps === true){
            console.log("performChecklist gps isOK:"+response.gps);
            setTimeout(() =>{
              checkListe.value.gps = true
            }, 10000)
            return;
          }
          if (response.gps === false){
            console.log("performChecklist gps error:"+response.gps);
            setTimeout(() =>{
              checkListe.value.gps = false
            }, 10000)
          }

          if (response.checkSucces){
            setTimeout(() => {
              submitting.value = false
              checkListModal.value = false
              showContinuBouton.value = false
              checkSuccesContinu.value = true

              Notify.create({
                type: 'positive',
                message: 'Prêt a décoller !',
                position: 'center',
                timeout: 2000,
              })
            }, 12000)

            setTimeout(() => {
              checkListe.value.gps = null
              checkListe.value.acces = null
              checkListe.value.drone = null
              checkListe.value.sdcard = null
              checkListe.value.camera = null
              checkListe.value.controle = null
              checkListe.value.planDeVol = null
              checkListe.value.phoneBattery = null
              checkListe.value.droneBattery = null
            },14000)
          }
          if(!response.checkSucces) {
            setTimeout(() => {
              submitting.value = false;
            }, 10000)
          }
        }
      });
    }

    let onStartContinuMission = async () => {
      submitting.value = true
      setTimeout(async () => {
        await JoolMapping.continuWaypointMission(async (response) => {
          console.log("continuWaypointMission detail:",JSON.stringify(response));

          if (response.error){
            console.log("continuWaypointMission Error Happen",response.error);
            return;
          }
          if (response.startSucces){
            console.log("continuWaypointMission MissionStart successful:");
            submitting.value = false;
            continuLastMission.value = true
            checkSuccesContinu.value = false
            return;
          }
          if (response.finishSucces){
            console.log("continuWaypointMission MissionEnd successful",response.finishSucces);
            continuLastMission.value = false
            showContinuBouton.value = false
            await onMissionFinish()
            // let status = true
            // await onUpdate(status)
            Notify.create({
              type: 'positive',
              message: 'Mission terminée avec succès',
              position: 'top',
              timeout: 10000,
            })
            return;
          }
          if (response.lowBattery){
            showContinuBouton.value = true
            continuLastMission.value = false
            showStartBouton.value = false

            Notify.create({
              type: 'warning',
              message: 'La batterie du drone est faible !',
              position: 'top',
              timeout: 10000,
            })
            return;
          }
          console.log("continuWaypointMission: response String",JSON.stringify(response));
        });
        return;
      }, 1000)


      // Write Log in file
      console.log('logToFile Will Write');
      await JoolMapping.logToFile({message:"Hello log from CAPACITOR APP"}).catch(err => {
        errorHappen = true;
        console.log('logToFile onErr ', err);
      }).then(() =>{
        if(errorHappen){
          return;
        }else{
          console.log('logToFile Succes ');
        }
      });
    }

    let onReturnToHome = async () => {
      submitting.value = true
      console.log("returnToHome");
      let errorHappen = false;
      await JoolMapping.returnToHome().catch(err => {
        errorHappen = true;
        submitting.value = false
        atterrirModal.value = false
        console.log('returnToHome onErr ', err);

        Notify.create({
          type: 'negative',
          message: 'Echec de demande d\'atterrissage !',
          position: 'top',
          timeout: 5000,
        })
      }).then(() =>{
        if(errorHappen){
          return;
        }else{
          setTimeout(() =>{
            submitting.value = false
            atterrirModal.value = false
            console.log('returnToHome Succes ');
            Notify.create({
              type: 'positive',
              message: 'Demande de l\'atterrissage',
              position: 'top',
              timeout: 5000,
            })
            startMission.value = false
            continuLastMission.value = false
          }, 1000)
        }
      });
    }

    let onComfirm = async () => {
      if(infoPlan.value.flyPlan) {
        let flyPlan = JSON.parse(infoPlan.value.flyPlan)
        var polyline = L.polyline(flyPlan, {color: 'green'})
        groupLayers.addLayer(polyline)
        mymap.flyToBounds(polyline.getBounds(),  {'duration':0.50});
      }

      let isDroneTelemetryFindOne = false;
      await JoolMapping.watchDroneTelemetry(async (position, err) => {
        if (err) {
          console.log('watchDroneState onErr ', err);
          return;
        }
        if (position) {
          infoDrone.value = position
          positionDrone.value = position

          // Position du drone en temps réel
          let longitude = position.longitude
          let latitude = position.latitude
          await getDronePosition(latitude, longitude)

          /* Coordonnées du téléphone */
          let lng = location.value.longitude
          let lat = location.value.latitude
          await getPhonePosition(lat, lng)

          if (!isDroneTelemetryFindOne){
            isDroneTelemetryFindOne = true;
          }
        }
      });

      continuModal.value = false
      showStartBouton.value = false
      showContinuBouton.value = true
    }

    let noComfirm = async () => {
      continuModal.value = false
      await onGetInfoMissin()
    }

    let onBack = () => {
      bachModal.value = true
    }

    let onStopMission = () => {
      console.log("onStopMission ====");
      groupLayers.clearLayers()
      JoolMapping.disconnect()
      router.push('accueil')
      ServiceModule.stopWatchPosition().catch(err => {
        console.log( 'stopWatchPosition onErr ', err);
      }).then( res => {
        console.log('stopWatchPosition onSucces ',res);
        //valueIsWatchStop = true;
      });
    }

    let onUpdate = async (status) => {
      console.log("onUpdate",status);
      state.filters.value = `
        mutation($id:UUID, $libelle:String!, $altitude:Int, $nbrBatteries:Int, $nbrImages:Int, $temps:Int, $status:Boolean, $flyPlan:String) {
          updatePlan(newPlan:
            {
              id:$id
              libelle:$libelle,
              altitude:$altitude,
              nbrBatteries:$nbrBatteries,
              nbrImages:$nbrImages,
              temps:$temps,
              status: $status,
              flyPlan:$flyPlan,
            })
            {
              ok
              plan{
                id
              }
              errors {
                field
                messages
              }
            }
          }
        `
        if (status) {
          //eForm.value = "eForm"
          formData.value.nbrBatteries = infoMission.value.battery
          formData.value.nbrImages = infoMission.value.imageCount
          formData.value.temps = flyTime.value
          formData.value.status = true
          state.item.value = formData.value;
          console.log(formData.value);
          state.updateForm.value = eForm.value;
          await state.update()
        } else {
          formData.value.flyPlan = pointsSurvol.value
          formData.value.nbrBatteries = infoMission.value.battery
          formData.value.nbrImages = infoMission.value.imageCount
          formData.value.temps = flyTime.value
          state.item.value = formData.value;
          console.log(formData.value);
          state.updateForm.value = eForm.value;
          await state.update()
        }

        stateProjet.filters.value = `
          query {
            searchProjet (deleted: false) {
              results(limit:1000, ordering:"-created_at") {
                id
                libelle
                description
                createdAt
                projetPlan(deleted: false){
                  id
                  libelle
                  altitude
                  nbrBatteries
                  nbrImages
                  temps
                  status
                  polygone
                  flyPlan
                  deleted
                  createdAt
                }
                acteur{
                  id
                  libelle
                  acteurPoly{
                    superficie
                    geometrie
                    centroid
                  }
                }
              }
              totalCount
            }
          }
          `
          await stateProjet.getItems()
          itemsProjet.value = JSON.parse(localStorage.getItem('searchProjet'))
    }

    let updateMission = async () => {
      try {
        let res = await instance.appContext.app.config.globalProperties.$api({
            url: `/`,
            method: "post",
            headers: {
                Authorization: `JWT ${token}`
            },
            data: {
                query: `
                  mutation($id:UUID, $status:Boolean,) {
                    updatePlan(newPlan:
                      {
                        id:$id
                        status: $status,
                      })
                      {
                        ok
                        plan{
                          id
                        }
                        errors {
                          field
                          messages
                        }
                      }
                    }
                `,

                variables: {
                  id: infoPlan.value.id,
                  status: true,
                }
            },
        })
        console.log("Mission update  |==> ", res.data);
      } catch (error) {
        console.log(error);
      }
    }

    let onZoomIn = async () => {
      mymap.setZoom(mymap.getZoom() + 1)
    }

    let onZoomOut = async () => {
      mymap.setZoom(mymap.getZoom() - 1)
    }

    let onGetMeteo = async () => {
      meteoModal.value = !meteoModal.value
    }

    return{
      initial,
      onGetInfoMissin,
      getDronePosition,
      onBack,
      onChecklist,
      onMissionFinish,
      onMissionExist,
      onUpdate,
      bachModal,
      checkListModal,
      onStart,
      onFinish,
      start,
      infoPlan,
      superficie,
      infoDrone,
      checkList,
      checkListe,
      infoMission,
      stateMission,
      submitting,
      flyTime,
      finishModal,
      progressing,
      continuModal,
      onComfirm,
      continuLastMission,
      onStartContinuMission,
      noComfirm,
      showContinuBouton,
      startMission,
      onReturnToHome,
      onStopMission,
      onZoomIn,
      onZoomOut,
      onGetMeteo,
      meteoModal,
      infoMeteo,
      icon_url,
      checkSucces,
      showStartBouton,
      onChecklistContinuMission,
      checkSuccesContinu,
      atterrirModal,
      inforeturn,
      coordinate,
      droneConnection
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
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}

</style>
