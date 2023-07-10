<template>
  <q-page>
    
    <div id="mapid" style=" width: 100%; height: 100vh;"> </div>
    
    <div class="fixed-top-left z-max q-ml-sm" style="margin-top:40px">
      <div class="row">

        <transition
          appear
          enter-active-class="animated fadeInLeft"
          leave-active-class="animated fadeOutLeft"
        >
          <q-card flat style="border-radius: 10px; height:310px" v-if="hideIcon && !checkListModal">
            
            <q-item-section class="text-white bg-dark" align="center">
              <q-item-label class="q-pa-sm" style="font-size:12px"> {{date}} </q-item-label>
            </q-item-section>

            <q-item-section class="text-secondary q-ma-sm">
              <q-item-label>
                <img src="plan/Batterie.svg" style="height: 16px;" class="q-ml-xs"> 
                <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{infoDrone.chargeRemainingInPercent}}% <span class="q-pl-md"> x{{infoMission.battery}}</span>  </span> 
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
              <q-item-label>
                <img src="plan/Duree.svg" style="height: 16px; margin-left:2px"> 
                <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{flyTime}} min(s) </span> 
              </q-item-label>
            </q-item-section>
            <q-separator spaced />

            <q-item-section class="text-secondary q-ma-sm">
              <q-item-label>
                <img src="plan/Distance.svg" style="height: 16px;"> 
                <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{ superficie}} ha  </span> 
              </q-item-label>
            </q-item-section>
            <q-separator spaced />

            <q-item-section class="text-secondary q-ma-sm">
              <q-item-label>
                <img src="plan/Altitude.svg" style="height: 16px;"> 
                <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{Math.round(infoDrone.altitudeInMeters)}} m  </span> 
              </q-item-label>
            </q-item-section>
            <q-separator spaced />

            <q-item-section class="text-secondary q-ma-sm">
              <q-item-label>
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

            <div>
              <q-btn 
              style="position: relative; bottom:218px; left: 76px" 
              icon-right="arrow_back_ios" 
              color="white" 
              size="sm" 
              padding="xs" 
              text-color="secondary" 
              @click="onShow" 
            />
            </div>
          </q-card>
          </transition>


          <div>
            <!-- <q-card style="position: relative; top:50px; left: 0px">
              <q-btn 
                v-if="!hideIcon"
                icon-right="arrow_back_ios" 
                color="white" 
                size="sm" 
                padding="xs" 
                text-color="secondary" 
                @click="onShow" 
              />
            </q-card> -->

            <q-card style="position: relative; top:50px; left: -10px" v-if="!hideIcon">
              <q-btn 
                
                icon-right="arrow_forward_ios" 
                color="white" 
                size="sm" 
                padding="xs" 
                text-color="secondary" 
                @click="onShow" 
              />
            </q-card>
              
            </div>

       

      </div> 
    </div>

    <!-- <div class="absolute-bottom z-max">
        {{infoMission.surveyPoints}}
    </div> -->

   

    <div v-if="!start && !stateMission || !start && stateMission">
      <q-page-sticky class="z-max" position="bottom" :offset="[0, 2]">
        <q-btn round class="q-pa-none q-ma-md" color="dark" @click="onBack">
          <q-avatar square size="25px">
            <img src="plan/Home.svg">
          </q-avatar>
        </q-btn>
      </q-page-sticky>
    </div>

    <div v-if="!start && !stateMission && !continuLastMission || !start && stateMission && !continuLastMission">
      <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 70]">
        <q-btn round flat class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onStart">
          <q-avatar square size="90px">
            <q-img src="plan/Decoller.gif"/>
          </q-avatar>

          <template v-slot:loading>
            <q-spinner-ios size="40px " />
          </template>
        </q-btn>
      </q-page-sticky>
    </div>

    <div v-if="continuLastMission">
      <q-page-sticky class="z-max" position="bottom-right" :offset="[35, 85]">
        <q-btn fab no-caps class="q-pa-none" color="accent" :loading="submitting" @click.prevent="onContinueMission">
          <q-item-label>
            <q-icon name="near_me" class="q-pt-sm"/><br> 
            <span style="font-size:7px; position: relative; top:-5px">Continuer</span> 
          </q-item-label>

          <template v-slot:loading>
            <q-spinner-ios />
          </template>
        </q-btn>
      </q-page-sticky>
    </div>

    <!-- <div v-if="!start && stateMission">
      <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 70]">
        <q-btn fab class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onStart">
          <q-item-label>
            <q-icon name="near_me" class="q-pt-sm"/><br> 
            <span style="font-size:7px; position: relative; top:-5px">Décoller</span> 
          </q-item-label>

          <template v-slot:loading>
            <q-spinner-ios />
          </template>
        </q-btn>
      </q-page-sticky>
    </div> -->

    <div v-if="start">
      <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 30]">
        <q-btn fab class="q-pa-none" color="negative" @click="onFinish">
          <q-item-label>
           <q-avatar square size="30px" class="q-pt-sm">
              <q-img src="plan/Atterir.svg"/>
            </q-avatar> <br> 
            <span style="font-size:7px; position: relative; top:-4px">Attérir</span> 
          </q-item-label>
        </q-btn>
      </q-page-sticky>
    </div>



    <!-- <q-form
      @submit="onSubmit"
      class="q-gutter-xs"
      ref="eForm"
    >
  
    </q-form> -->


  <!-- Modal de comfirmation de l'annulation -->
  <q-dialog v-model="bachModal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down">
    <q-card class="z-max"
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
        <div align="center"  class="text-weight-regular" style="font-size:20px">Voulez-vous vraiment annuler la mission ?</div>
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

    <!-- Modal de check list -->
    <q-dialog v-model="checkListModal" maximized persistent transition-show="slide-down" transition-hide="slide-up">
      <q-card class="" 
        style="background: rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(3px)"
      >
        <q-card 
          style="margin-left: 10%; 
            margin-right: 10%; 
            border-radius: 10px; 
            background: rgba(255, 255, 255, 0.9);
            backdrop-filter: blur(3px)"
        >
          <q-card-section class="q-ma-none">
            <div align="center" class="q-mb-sm">
              Vérification avant vol
            </div>

            <transition
              appear
              enter-active-class="animated fadeInLeft"
            >
              <div class="q-ma-sm" v-if="checkListe.acces">
                <q-icon name="check_circle_outline" size="25px" class="text-secondary"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Accès</span> 
              </div>
            </transition>

            <div class="q-ma-sm" v-if="!checkListe.acces">
              <q-icon name="check_circle_outline" size="25px" />
              <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Accès</span>
            </div>


            <transition
              appear
              enter-active-class="animated fadeInLeft"
            >
              <div class="q-ma-sm" v-if="checkListe.drone">
                <q-icon name="check_circle_outline" size="25px" class="text-secondary"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Drone</span> 
              </div>
            </transition>

            <div class="q-ma-sm" v-if="!checkListe.drone">
              <q-icon name="check_circle_outline" size="25px" />
              <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Drone</span>
            </div>


            <transition
              appear
              enter-active-class="animated fadeInLeft"
            >
              <div class="q-ma-sm" v-if="checkListe.camera">
                <q-icon name="check_circle_outline" size="25px" class="text-secondary"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Caméra</span> 
              </div>
            </transition>

            <div class="q-ma-sm" v-if="!checkListe.camera">
              <q-icon name="check_circle_outline" size="25px"/> 
              <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Caméra</span>
            </div>


            <transition
              appear
              enter-active-class="animated fadeInLeft"
            >
              <div class="q-ma-sm" v-if="checkListe.controle">
                <q-icon name="check_circle_outline" size="25px" class="text-secondary"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Contrôle</span> 
              </div>
            </transition>

            <div class="q-ma-sm" v-if="!checkListe.controle">
              <q-icon name="check_circle_outline" size="25px" />
              <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Contrôle</span>
            </div>

          
            <transition
              appear
              enter-active-class="animated fadeInLeft"
            >
              <div class="q-ma-sm" v-if="checkListe.planDeVol">
                <q-icon name="check_circle_outline" size="25px" class="text-secondary"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Plan de vol</span> 
              </div>
            </transition>

            <div class="q-ma-sm" v-if="!checkListe.planDeVol">
              <q-icon name="check_circle_outline" size="25px" />
              <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Plan de vol</span>
            </div>


            <transition
              appear
              enter-active-class="animated fadeInLeft"
            >
              <div class="q-ma-sm" v-if="checkListe.phoneBattery">
                <q-icon name="check_circle_outline" size="25px" class="text-secondary"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Batterie Téléphone</span> 
              </div>
            </transition>

            <div class="q-ma-sm" v-if="!checkListe.phoneBattery">
              <q-icon name="check_circle_outline" size="25px" /> 
              <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Batterie Téléphone</span>
            </div>

            <transition
              appear
              enter-active-class="animated fadeInLeft"
            >
              <div class="q-ma-sm" v-if="checkListe.droneBattery">
                <q-icon name="check_circle_outline" size="25px" class="text-secondary"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Batterie Drone</span> 
              </div>

              <!-- <div class="q-ma-sm" v-else>
                <q-icon name="highlight_off" size="25px" class="text-negative"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Batterie</span> 
              </div> -->
            </transition>

            <div class="q-ma-sm" v-if="!checkListe.droneBattery">
              <q-icon name="check_circle_outline" size="25px" />
              <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Batterie Drone</span>
            </div>


            <transition
              appear
              enter-active-class="animated fadeInLeft"
            >
              <div class="q-ma-sm" v-if="checkListe.gps">
                <q-icon name="check_circle_outline" size="25px" class="text-secondary"/> 
                <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">GPS</span> 
              </div>
            </transition>

            <div class="q-ma-sm" v-if="!checkListe.gps">
              <q-icon name="check_circle_outline" size="25px" /> 
              <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">GPS</span>
            </div>

            <div clickable class="q-mt-md text-center" style="margin-left:50px; margin-right:50px">
              <q-separator size="5px" color="grey-8" inset />
            </div>
          </q-card-section>
        </q-card>
      </q-card>
    </q-dialog>

    <q-dialog v-model="finishModal" position="bottom" persistent class="z-max" >
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
    </q-dialog>


    <q-dialog full-width v-model="continuModal" persistent class="z-max" >
      <div style="border-radius:15px">
        <q-card class="q-px-sm q-pb-md">
          <q-card-section class="row items-center q-pa-none q-pt-md">
            <div class="text-h6 text-center text-warning" style="width: 100%;">Confirmation ! </div>
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
              v-close-popup
              label="NON"
              class="text-weight-regular"
              color="warning"
              style="width: 45%; border-radius:5px"
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


  </q-page>
</template>

<script>
import { Notify } from 'quasar'
import { useQuasar } from 'quasar'
import { format, secondsToMinutes } from 'date-fns'
import { useRouter } from 'vue-router'
import { ref, defineComponent, getCurrentInstance, onMounted, onBeforeUnmount, inject, provide, watch} from "vue";
import { JoolMapping } from "joolmapping";
import AddActeur from "components/Acteur/Create.vue";

export default defineComponent({
  // name: 'PageName',

  components: {
    AddActeur,
  },

  props: {
    itemPlan: {
      // type: String,
      default() {
        return {};
      },
    },
  },

  setup(props) {
    const $q = useQuasar()
    const router = useRouter()
    const instance = getCurrentInstance()
    const service = "updatePlan";
    let state = inject(service);
    let formData = ref({})
    let eForm = ref("eForm")
    let infoPlan = ref()
    let mymap;
    let bachModal = ref(false)
    let checkListModal = ref(false)
    let start = ref(false)
    let hideIcon = ref(true)
    let superficie = ref()
    let date = ref()
    date.value = format(new Date(), "dd/MM/yyyy")
    let infoDrone = ref({
      chargeRemainingInPercent:0,
      uplinkSignalQuality:0,
      altitudeInMeters:0,
      speedInMeterPerSec:0,
    })

    let stateConnect = ref(true)
    let stateProgressing = ref(true)
    let stateMission = ref(false)
    const submitting = ref(false)
    let flyTime = ref(0)
    let tabDronePosition = ref([])
    let finishModal = ref(false)
    let progressing = ref()
    let phonePosition = ref({})
    let continuModal = ref(false)
    let continuLastMission = ref(false)

    let checkListe = ref({
      gps : false,
      acces : false,
      drone : false,
      camera : false,
      controle : false, 
      planDeVol : false,
      phoneBattery : false,
      droneBattery : false,
    })

    let infoMission = ref({
      flyTime: 0,
      imageCount: 0,
      battery: 0,
      speed: 0,
      //surveyPoints
    })

    let checkList = ref({})

    /* Fonction de connexion au drone */
    let initial = async () => {
      stateConnect.value = false
      setTimeout(async () => {
        const TAG = 'Index';
        await JoolMapping.ini();
        console.log(TAG, 'connect Start');
        
        let errorHappen = false;
        JoolMapping.connect().catch(err => {
          stateConnect.value = true
          console.log(TAG, 'connect onErr ', err);
          Notify.create({
            type: 'warning',
            message: 'Veuillez-vous connecter au drone',
            timeout: 5000,
            position: 'top'
          })
          errorHappen = true;
        }).then(async res => {
          if (errorHappen){
            return;
          }else {
            stateConnect.value = true
            console.log(TAG, 'connect onSucces res', res);
            Notify.create({
              type: 'positive',
              message: 'Connexion au drone réussit',
              timeout: 5000,
              position: 'top'
            })
            await onMissionExist()
            // STEP 1
            let isDroneTelemetryFindOne = false;
            await JoolMapping.watchDroneTelemetry(async (position, err) => {
              //console.log(TAG, 'watchDroneState Result Fired  ');
              if (err) {
                console.log(TAG, 'watchDroneState onErr ', err);
                return;
              }
                
              
              if (position) {
                infoDrone.value = position
                //console.log(TAG, 'watchDroneState onSucces position:', JSON.stringify(position));
                // Position du drone en temps réel
                let longitude = position.longitude
                let latitude = position.latitude

                // for (const coord of infoDrone.value){
                //   tabDronePosition.push([coord.longitude, coord.latitude]);
                // }

                await getDronePosition(latitude, longitude)
                
                if (!isDroneTelemetryFindOne){
                  isDroneTelemetryFindOne = true;
                }
              }
            });
          }
        });
      },1000)
    }
    

    onMounted( async () =>{
      infoPlan.value = JSON.parse(props.itemPlan)
      formData.value = infoPlan.value 
      //console.log("infoPlan.value  ==> ",infoPlan.value);
      let polygon = JSON.parse(infoPlan.value.polygone)
      let centroid = JSON.parse(polygon.centroid)
      let coordinate = ref({})

      let poly = JSON.parse(infoPlan.value.polygone)
      let geo = JSON.parse(poly.geometrie)
      console.log("geom", geo.coordinates);
      let coords = geo.coordinates
      console.log("coordinate ==> ", JSON.stringify(coords) );

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

      mymap = L.map('mapid',{
         zoomControl: false, 
      }).setView([ coordinate.value.latitude,  coordinate.value.longitude], 16);
   

      L.tileLayer('http://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',{
        minZoom: 5,
        maxZoom: 20,
        subdomains:['mt0','mt1','mt2','mt3']
      }).addTo(mymap);

      L.control.zoom({
        position: 'topright'
      }).addTo(mymap);

      function onLocationFound(e) {
        phonePosition.value = e.latlng
        console.log("phonePosition ==>", phonePosition.value );
      }
      
      mymap.locate({});
      mymap.on("locationfound", onLocationFound);

      let geometry = JSON.parse(polygon.geometrie)
      superficie.value = polygon.superficie.toFixed(2)
      console.log("geometry ==> ", geometry);
      console.log("centroid ==> ", centroid);
      console.log("superficie ===>", superficie.value);

      L.geoJSON(geometry).addTo(mymap)

      // Fonction de connexion au drone
      await initial()
      
    })

    watch(stateConnect, async () => {
      if (!stateConnect.value) {
        $q.loading.show({
          message: 'Connexion au drone ...',
          boxClass: 'bg-grey-2 text-grey-9',
          spinnerColor: 'primary'
        })
      }else {
        $q.loading.hide()
        await onGetInfoMissin()
      }
    })

    watch(stateProgressing, async () => {
      console.log("stateProgressing ==>", stateProgressing.value);

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


    

    /* ===> Fonction pour recuperer les differentes positiondu drone <===*/

    //Personnaliser le marker
    var droneIcon = L.icon({
      iconUrl: 'plan/Drone.png',
      iconSize:     [35, 40],
      iconAnchor:   [17, 30],
      popupAnchor:  [-1, -23]
    });

    let drone; 
    let getDronePosition = async (latitude, longitude) => {
                
      if(drone){
        mymap.removeLayer(drone);
      }
      drone = L.marker([latitude, longitude],{icon: droneIcon}).addTo(mymap)

    }

    let onBack = () => {
      bachModal.value = true
    }

    let onGetInfoMissin = async () => {
      stateProgressing.value = false
      let errorHappen = false;

      let poly = JSON.parse(infoPlan.value.polygone)
      let geo = JSON.parse(poly.geometrie)
      console.log("geom", geo.coordinates);
      let coords = geo.coordinates
      console.log("coordinate ==> ", JSON.stringify(coords) );

      let missionOptions = {};
      missionOptions.altitude = infoPlan.value.altitude
      missionOptions.exitMissionOnRCLost = true;
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

                // Fonction de Mise a jour de plan
                let status = false
                //await onUpdate(status) 

                // Convertir la chaine de caractère en tableau
                let pointsSurvol = []
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
                  pointsSurvol.push(tab2)
                });
                console.table(pointsSurvol)
                console.log("pointsSurvol", JSON.stringify(pointsSurvol));

                var polyline = L.polyline(pointsSurvol, {color: 'green'}).addTo(mymap);
                mymap.flyToBounds(polyline.getBounds(),  {'duration':0.50});
              }
            }
          });
        }
      });
    }

    let onChecklist = async () => {
      await JoolMapping.performChecklist({caseContinuMission:false},async (response) => {
        if (response.error){
          console.log("performChecklist Error Happen",response.error);
          return;
        }else {
          console.log("performChecklist response String",JSON.stringify(response));
          checkList.value = response

          if (response.phoneBattery){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            checkListe.value.phoneBattery = true
            return;
          }
          if (response.droneBattery){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            checkListe.value.droneBattery = true
            return;
          }
          if (response.acces){
            console.log("performChecklist acces isOK:"+response.acces);
            checkListe.value.acces = true
            return;
          }
          if (response.config){
            console.log("performChecklist config isOK:"+response.config);
            checkListe.value.drone = true
            return;
          }
          if (response.controle){
            console.log("performChecklist controle isOK:"+response.controle);
            checkListe.value.controle = true
            return;
          }
          if (response.camera){
            console.log("performChecklist camera isOK:"+response.camera);
            checkListe.value.camera = true
            return;
          }
          if (response.flyplan){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            checkListe.value.planDeVol = true
            return;
          }
          if (response.gps){
            console.log("performChecklist gps isOK:"+response.gps);
            checkListe.value.gps = true
            return;
          }
          if (response.checkSucces){
            setTimeout(() => {
              checkListModal.value = false
            }, 2000)
            //await onMissionExist()
            console.log("performChecklist checkSucces Mean Everything is Ok");
            await JoolMapping.startWaypointMission(async (response) => {

              if (response.error){
                console.log("startWaypointMission Error Happen",response.error);
                return;
              }
              console.log("startWaypointMission detail:",JSON.stringify(response));
              if (response.startSucces){
                console.log("startWaypointMission MissionStart successful:");
                submitting.value = false;
                start.value = true
                return;
              }
              
              if (response.finishSucces){
                console.log("startWaypointMission MissionEnd successful",response.finishSucces);
                await onMissionFinish()
                let status = true
                await onUpdate(status) 
                start.value = false;
                return;
              }
              console.log("startWaypointMission: response String",JSON.stringify(response));
            });
            return;
          }

          if(!response.checkSucces) {
            setTimeout(() => {
              submitting.value = false;
              //checkListModal.value = false
            }, 1000)
          }
        }
      });
    }


    let onStart = async () => {
      submitting.value = true
      checkListModal.value = true

      // Fonfion de check liste
      await onChecklist()

      // Write Log in file
      console.log(TAG, 'logToFile Will Write');
      await JoolMapping.logToFile({message:"Hello log from CAPACITOR APP"}).catch(err => {
        errorHappen = true;
        console.log(TAG, 'logToFile onErr ', err);
      }).then(() =>{
        if(errorHappen){
          return;
        }else{
          console.log(TAG, 'logToFile Succes ');
        }
      });
    }

    let onFinish = async () => {
      start.value = false
      checkListModal.value = false
    }

    let onShow = () => {
      hideIcon.value = !hideIcon.value
    }

    let onMissionExist = async () => {
      console.log("Entrer");
      let errorHappen = false;
      // Continu old Mission
      await JoolMapping.isMissionExist().catch(err => {
        // No mission found
        errorHappen = true;
        console.log('isMissionExist onErr ==>', err);
      }).then(async () => {
        if(errorHappen){
          return;
        }else{
          console.log('isMissionExist Case Mission Exist ==>');
          continuModal.value = true
          // case mission found
          // await JoolMapping.continuWaypointMission(async (error, succes) => {
          //   if (error) {
          //     console.log(TAG, 'continuWaypointMission onErr ', error);
          //   } else {

          //   }
          // });
        }
      });
      return;
    }

    let onMissionFinish = async () => {
      finishModal.value = true
      progressing.value = 0.01

      do {
        setInterval(() => {
            progressing.value = Math.min(1, progressing.value + 0.1)
        }, 2000)
      } while (progressing.value === 1);

      watch(progressing , async () =>{
        if (progressing.value === 1) {
            setTimeout(() => {
              finishModal.value = false
            }, 3000)

            setTimeout(() => {
              //router.push("/accueil")
            }, 3500)
        }
      })
    }


    let onChecklistContinuMission = async () => {
      console.log("onChecklistContinuMission");
      await JoolMapping.performChecklist({caseContinuMission:false},async (response) => {
        if (response.error){
          console.log("performChecklist Error Happen",response.error);
          return;
        }else {
          console.log("performChecklist response String",JSON.stringify(response));
          checkList.value = response

          if (response.phoneBattery){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            checkListe.value.phoneBattery = true
            return;
          }
          if (response.droneBattery){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            checkListe.value.droneBattery = true
            return;
          }
          if (response.acces){
            console.log("performChecklist acces isOK:"+response.acces);
            checkListe.value.acces = true
            return;
          }
          if (response.config){
            console.log("performChecklist config isOK:"+response.config);
            checkListe.value.drone = true
            return;
          }
          if (response.controle){
            console.log("performChecklist controle isOK:"+response.controle);
            checkListe.value.controle = true
            return;
          }
          if (response.camera){
            console.log("performChecklist camera isOK:"+response.camera);
            checkListe.value.camera = true
            return;
          }
          if (response.flyplan){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            checkListe.value.planDeVol = true
            return;
          }
          if (response.gps){
            console.log("performChecklist gps isOK:"+response.gps);
            checkListe.value.gps = true
            return;
          }
          if (response.checkSucces){
            setTimeout(() => {
              checkListModal.value = false
            }, 2000)
            //await onMissionExist()
            console.log("performChecklist checkSucces Mean Everything is Ok");
            await JoolMapping.continuWaypointMission(async (response) => {
              if (response.error){
                console.log("continuWaypointMission Error Happen",response.error);
                return;
              }
              console.log("continuWaypointMission detail:",response);
              if (response.startSucces){
                console.log("continuWaypointMission MissionStart successful:");
                submitting.value = false;
                continuLastMission.value = true
                return;
              }
              if (response.gridProgressing){
                console.log("continuWaypointMission Notify Loader progressing:"+response.gridProgressing);
                return;
              }
              if (response.battery){
                console.log("continuWaypointMission show Battery need for mission",response.battery);
                if (response.imageCount){
                  console.log("continuWaypointMission show Images need for mission",response.imageCount);
                }
                if (response.speed){
                  console.log("continuWaypointMission show Drone Speed(meters/seconds) for currentMission",response.speed);
                }
                if (response.surveyPoints){
                  console.log("continuWaypointMission show Fly Path",response.surveyPoints);
                }
                if (response.stepRemain){
                  console.log("continuWaypointMission show Number of step remain",response.stepRemain);
                }
                return
              }
              if (response.finishSucces){
                console.log("continuWaypointMission MissionEnd successful",response.finishSucces);
                return;
              }

              console.log("continuWaypointMission: response String",JSON.stringify(response));
            });
            return;;
          }

          if(!response.checkSucces) {
            setTimeout(() => {
              submitting.value = false;
              //checkListModal.value = false
            }, 1000)
          }
        }
      });
    }



    let onContinueMission = async () => {
      submitting.value = true
      checkListModal.value = true
      console.log("performChecklist checkSucces Mean Everything is Ok");
      // Fonfion de check liste
      await onChecklistContinuMission()
      

      

      /* setTimeout( async () => {
        await JoolMapping.continuWaypointMission(async (response) => {
        if (response.error){
          console.log("continuWaypointMission Error Happen",response.error);
          return;
        }
        console.log("continuWaypointMission detail:",response);
        if (response.startSucces){
          console.log("continuWaypointMission MissionStart successful:");
          submitting.value = false;
          continuLastMission.value = true
          return;
        }
        if (response.gridProgressing){
          console.log("continuWaypointMission Notify Loader progressing:"+response.gridProgressing);
          return;
        }
        if (response.battery){
          console.log("continuWaypointMission show Battery need for mission",response.battery);
          if (response.imageCount){
            console.log("continuWaypointMission show Images need for mission",response.imageCount);
          }
          if (response.speed){
            console.log("continuWaypointMission show Drone Speed(meters/seconds) for currentMission",response.speed);
          }
          if (response.surveyPoints){
            console.log("continuWaypointMission show Fly Path",response.surveyPoints);
          }
          if (response.stepRemain){
            console.log("continuWaypointMission show Number of step remain",response.stepRemain);
          }
          return
        }
        if (response.finishSucces){
          console.log("continuWaypointMission MissionEnd successful",response.finishSucces);
          return;
        }

        console.log("continuWaypointMission: response String",JSON.stringify(response));
      });
      return;
      },1000) */

    }

    let onComfirm = async () => {
      continuLastMission.value = true
      continuModal.value = false
    }

    let onUpdate = async (status) => {
      console.log("onUpdate",status);
      state.filters.value = `
        mutation($id:UUID, $libelle:String!, $altitude:Int, $nbrBatteries:Int, $nbrImages:Int, $temps:Int, $status:Boolean) {
          updatePlan(newPlan: 
            {
              id:$id
              libelle:$libelle, 
              altitude:$altitude,
              nbrBatteries:$nbrBatteries,
              nbrImages:$nbrImages,
              temps:$temps,
              status: $status
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

      //eForm.value = "eForm"
      formData.value.nbrBatteries = infoMission.value.battery
      formData.value.nbrImages = infoMission.value.imageCount
      formData.value.temps = flyTime.value
      formData.value.status = true
      state.item.value = formData.value;
      console.log(formData.value);
      state.updateForm.value = eForm.value;
      await state.update()

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
      onShow,
      hideIcon,
      infoPlan,
      superficie,
      date,
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
      onContinueMission,

      
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