<template>
  <q-page>

    <div>
      <div id="mapid" style="width: 100%; height: 110vh; position:fixed"> </div>
    </div>

    <div class="fixed-top-right z-max q-mr-sm" style="margin-top:30px" >

      <transition
        appear
        enter-active-class="animated fadeInRight"
        leave-active-class="animated fadeOutRight"
      >
        <q-card v-if="menu.home">
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
        <q-card style="top: 15px" v-if="menu.home">
          <q-btn
            class="no-padding"
            unelevated
            @click="onGetMeteo"
          >
            <q-avatar size="2.5rem">
              <img src="Meteo.svg">
            </q-avatar>
          </q-btn>
        </q-card>
      </transition>
    </div>


    <!-- Parcelle scroll  -->
    <q-footer class="q-ma-md q-pa-none text-dark transparent divParcelles" reveal style v-if="menu.home && itemsActeur.length > 0">

      <q-scroll-area style="height: 12vh; max-width: 100%;"
        :thumb-style="{
          width: '1px', opacity: 0
        }"
      >
        <div class="row no-wrap">
          <q-list  v-for="item in itemsActeur" :key="item" >
            <div class="q-pa-md q-mr-md parcelle row" @click="onPolygon(item)">
              <div class="col">
                <q-item-label class="q-mb-sm" lines="1" style="font-size:18px">{{item.libelle}}</q-item-label>
                <q-item-label caption style="font-size:15px">{{ $FormatDate(item.createdAt)}}</q-item-label>
                <span caption> </span>
              </div>

              <div class="col text-right">
                <q-item-label class="text-subtitle1 q-mb-sm text-teal">{{item.acteurPoly[0].superficie.toFixed(2) }} ha</q-item-label>
                <q-item-label caption class="q-pt-xs" v-if="item.acteurProjet.length > 0">
                  <small>
                    Cartographie(s)
                    <q-badge rounded color="teal" > {{item.acteurProjet.length }} </q-badge>
                  </small>
                </q-item-label>
              </div>
            </div>
          </q-list>
        </div>
      </q-scroll-area>
    </q-footer>

    <q-dialog
      v-model="meteoModal"
      seamless
      full-width
      transition-show="slide-up"
      transition-hide="slide-down"
      position="bottom"
    >
      <q-btn
        round
        dense
        class="z-max absolute-top-right"
        size="8px" color="negative" icon="close"
        style="top:-7px; margin-right:5px"
      />
      <q-carousel
        style="border-radius: 10px; background: rgba(0,0,10, 0.6); backdrop-filter: blur(5px);"
        transition-prev="scale"
        transition-next="scale"
        swipeable
        animated
        v-model="slide"
        control-color="white"
        prev-icon="arrow_left"
        next-icon="arrow_right"
        arrows
        height="160px"
        class="carousel"
      >
        <q-carousel-slide :name="1" class="q-pa-none" style="overflow-x:hidden">
          <q-item class="text-white">
            <q-item-section>
              <q-item-label class="text-h6">
                <q-icon name="location_on" size="25px" style="position: relative; top:-3px"/>
                {{meteo.nom_departement}}
              </q-item-label>
            </q-item-section>
            <q-item-section side style="position: relative; top: 10px">
              <q-item-label class="text-weight-light text-white">{{$dateFormat(meteo.date)}}</q-item-label>
            </q-item-section>
          </q-item>

          <q-separator />

          <q-card-section horizontal class="text-white">
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

          <q-btn
            round
            dense
            class="z-max absolute-top-right"
            size="15px" icon="close"
            v-close-popup
            style="opacity:0"
          />

        </q-carousel-slide>

        <q-carousel-slide :name="2" class="q-pa-none" style="overflow-x:hidden">
          <q-item class="text-white">
            <q-item-section>
              <q-item-label class="text-h6">
               Prévision Météo
              </q-item-label>
            </q-item-section>
            <q-item-section side style="position: relative; top: 10px">
              <q-item-label class="text-weight-light text-white">
                <q-icon name="location_on" size="25px" style="position: relative; top:-3px"/>
                {{meteo.nom_departement}}
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-separator />

          <q-card-section horizontal class="text-white">
            <q-card-section class="col q-pa-none q-pt-xs" v-for="item in infoMeteoHebdo" :key="item">
              <q-item-label class="text-center " style="font-size: 10px"> {{$FormatDate(item.date)}} </q-item-label>
              <q-item-section class="text-center ">
                <div style="position:relative; right:-40px; ">
                  <lottie-player
                    :src="item.lottie_icon"
                    background="transparent"
                    speed="1"
                    style="width: 30px; height: 30px;"
                    loop autoplay>
                  </lottie-player>
                </div>
                <!-- <q-img :src="meteo.icon_url" width="60px" height="60px" /> -->
              </q-item-section>

              <q-item-section class="q-pr-xs q-pl-xs q-pt-xs text-center " >
                <q-item-label>
                  <span class="text-weight-bold" style="font-size: 20px"> {{item.avg_temperature.toFixed(1)}}</span>
                  <span style="position:relative; top:-10px; left:1px; font-size: 10px">°C</span>
                </q-item-label>
              </q-item-section>

              <q-item dense class="q-ma-none q-pt-none">
                <q-item-section class="q-ma-none q-pa-none" >
                  <q-item-label class="text-weight-regular" style="font-size: 10px">Hum:</q-item-label>
                  <q-item-label class="text-weight-regular" style="font-size: 10px;">Ven:</q-item-label>
                </q-item-section>

                <q-item-section class="q-ma-none q-pa-none" side >
                  <q-item-label class="text-weight-medium text-white" style="font-size: 10px">{{item.avg_humidity}}%</q-item-label>
                  <q-item-label class="text-weight-medium text-white" style="font-size: 10px">{{item.max_wind_kph}} km/h</q-item-label>
                </q-item-section>
              </q-item>
            </q-card-section>
            <q-separator vertical />
          </q-card-section>

          <q-btn
            round
            dense
            class="z-max absolute-top-right"
            size="15px" icon="close"
            v-close-popup
            style="opacity:0"
          />
        </q-carousel-slide>
      </q-carousel>
    </q-dialog>

    <q-dialog
      position="top"
      v-model="stateConnexion"
      seamless
      transition-show="slide-down"
      transition-hide="slide-up"
    >
      <q-banner dense class="text-white bg-red">
        <template v-slot:avatar>
          <q-icon class="q-pl-md q-pr-sm" name="signal_wifi_off" color="white" />
        </template>
          Aucune connexion internet
      </q-banner>
    </q-dialog>

    <!-- Bar de menu -->
    <MenuBar/>

    <!-- Liste de parcelle  -->
    <q-dialog
      position="bottom"
      v-model="parcelleModal"
      persistent
      full-width
      transition-show="slide-up"
      transition-hide="slide-down"
    >
      <ListActeur/>
    </q-dialog>

    <!-- Liste de projet -->
    <q-dialog
      position="bottom"
      v-model="cartographieModal"
      persistent
      full-width
      transition-show="slide-up"
      transition-hide="slide-down"
    >
      <ListCartographie/>
    </q-dialog>

    <!-- Parametre -->
    <q-dialog
      v-model="parametreModal"
      persistent
      maximized
      transition-show="slide-up"
      transition-hide="slide-down"
    >
      <Parametre/>
    </q-dialog>
  </q-page>
</template>

<script>
import civ from "./civPolygon"
import $ from "jquery";
import axios from "axios"
import { useRouter } from "vue-router"
import { defineComponent, getCurrentInstance, ref, onMounted, onBeforeMount, inject, provide, watch} from "vue";
import MenuBar from "components/MenuBar.vue"
import ListActeur from "src/components/Acteur/List.vue"
import ListCartographie from "components/Cartographie/List.vue";
import Parametre from "components/Parametre/Index.vue"

export default defineComponent({
  // name: 'PageName',

  components: {
    MenuBar,
    ListActeur,
    ListCartographie,
    Parametre
  },

  setup() {
    const token = localStorage.getItem('jool_Plan_id-session-app')
    const router = useRouter()
    const instance = getCurrentInstance()
    let mymap = ref()
    provide("mymap", mymap)
    var layerGroup = ref(L.layerGroup())
    provide("layerGroup", layerGroup)

    const service = "searchActeurs";
    let state = inject(service);
    const serviceProjet = "searchProjet";
    let stateProjet = inject(serviceProjet);
    const servicegetAccount = "getAccountGeneralInfos";
    let stategetAccount = inject(servicegetAccount);
    const serviceVarieteCulture = "searchVarieteParCulture";
    let stateVarieteCulture = inject(serviceVarieteCulture);

    const servicePolygoneFichiersKml = "searchPolygoneFichiersKml";
    let statePolygoneFichiersKml = inject(servicePolygoneFichiersKml);

    let addModal = ref(false)
    provide("addModal", addModal)
    // Parcelle
    let parcelleModal = ref(false)
    provide("parcelleModal", parcelleModal)
    let showModal = ref(false);
    provide("showModal", showModal)
    // Projet
    let cartographieModal = ref(false)
    provide("cartographieModal", cartographieModal)
    // Paramttre
    let parametreModal = ref(false)
    provide("parametreModal", parametreModal)

    let itemsActeur = ref([])
    provide("itemsActeur", itemsActeur)
    let itemsProjet = ref([])
    provide("itemsProjet", itemsProjet)
    let itemseVarieteCulture = ref([])
    provide("itemseVarieteCulture", itemseVarieteCulture)
    let getAccount = ref({})
    provide("getAccount", getAccount)
    let modalIsOpened = ref(false)
    provide("modalIsOpened", modalIsOpened)
    let focusSearch = ref(false)
    provide("focusSearch", focusSearch)

    let menu = ref({
      home : true,
      parcelle : false,
      add : false,
      cartographie : false,
      parametre : false,
    })
    provide("menu", menu)

    let meteoModal = ref(false);
    let meteo = ref([])
    let infoMeteoHebdo = ref([])
    let icon_url = ref()
    let position = ref()
    let stateConnexion = ref(false);
    let user;
    let spinnerProjet = ref(false);
    provide("spinnerProjet", spinnerProjet)
    let spinnerActeur = ref(false);
    provide("spinnerActeur", spinnerActeur)

    onBeforeMount(() => {
      if (!token) {
        router.push("login")
      }else {
        user = JSON.parse(localStorage.getItem('userAuth-session-app'))
        if (localStorage.getItem('searchActeurs')) {
          itemsActeur.value = JSON.parse(localStorage.getItem('searchActeurs'))
        }
        if (localStorage.getItem('searchProjet')) {
          itemsProjet.value = JSON.parse(localStorage.getItem('searchProjet'))
        }
      }
    })

    var centroidIcon = L.icon({
      iconUrl: 'Marqueur.svg',
      iconSize:     [32, 37],
      iconAnchor:   [16, 37],
      popupAnchor:  [0, -28]
    });

    onMounted( async () =>{

      let Stadia_Maps = L.tileLayer('https://tiles.stadiamaps.com/tiles/outdoors/{z}/{x}/{y}{r}.png', {
        minZoom: 6,
        maxZoom: 20,
        attribution: '&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
      })

      let Google_Maps = L.tileLayer('http://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',{
        minZoom: 5,
        maxZoom: 20,
        subdomains:['mt0','mt1','mt2','mt3']
      })

      let Jawg_Streets = L.tileLayer('https://{s}.tile.jawg.io/jawg-streets/{z}/{x}/{y}{r}.png?access-token={accessToken}', {
        attribution: '<a href="http://jawg.io" title="Tiles Courtesy of Jawg Maps" target="_blank">&copy; <b>Jawg</b>Maps</a> &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        minZoom: 6,
        maxZoom: 22,
        subdomains: 'abcd',
        accessToken: 'AGp14tTUsm2DI0ue1gVxtLtiLIYD3AgyKCFmntlM5d0PYbaC4q1s3QP25vnQl0Vz'
      })

      //Affichage de la map
      mymap.value = L.map('mapid', {
        layers: [Jawg_Streets],
        zoomControl: false,
      }).setView([7.674076, -5.580024], 6);

      mymap.value.addLayer(layerGroup.value);

      async function onLocationFound(e) {
        position.value = e.latlng
        await getMetoe(position)
      }

      mymap.value.locate({});
      mymap.value.on("locationfound", onLocationFound);

      await onSetPolygonMap()
      await onGetActeur()
      await onGetProjet()
      await onGetAccount()

      // get Variete Culture
      await stateVarieteCulture.getItems();
      itemseVarieteCulture.value = JSON.parse(localStorage.getItem('searchVarieteParCulture'))
    })

    // get Acteur
    let onGetActeur = async () => {
      state.filters.value = `
      query {
        searchActeurs (deleted: false, enterprise_Id: "${user.enterprise.id}") {
          results(limit:1000, ordering:"-created_at") {
            id
            libelle
            createdAt
            acteurPoly {
              id
              superficie
              geometrie
              centroid
            }
            acteurProjet(deleted: false){
              id
              libelle
              description
              projetPlan{
                id
                libelle
                altitude
                polygone
              }
            }
            acteurVarieteCulture(deleted: false){
              id
              varietesCultures{
                id
                libelle
              }
            }
          }
          totalCount
        }
      }
      `
      spinnerActeur.value = true
      await state.getItems();
      spinnerActeur.value = false
      itemsActeur.value = JSON.parse(localStorage.getItem('searchActeurs'))
      // itemsActeur.value = state.items.value
    }

    // get projets
    let onGetProjet = async() => {
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
      spinnerProjet.value = true
      await stateProjet.getItems();
      itemsProjet.value = JSON.parse(localStorage.getItem('searchProjet'))
      spinnerProjet.value = false
    }

    // get Account
    let onGetAccount = async () => {
      stategetAccount.filters.value = `
      query {
        getAccountGeneralInfos (enterprise_Id: "${user.enterprise.id}" ) {
          enterpriseSocialreason
          totalUsers
          totalParcelles
          totalParcellesArea
          totalCultures
          typeAccount
        }
      }
      `
      await stategetAccount.getItems();
      getAccount.value = JSON.parse(localStorage.getItem('get-Account-General-Infos'))
    }

    watch(itemsActeur, async () =>{
      console.log("les tableau itemsActeur a changé");
      await onSetPolygonMap()
    })

    watch( meteoModal, async () =>{
      let onMapClick = async (e) => {
        if (meteoModal.value === true) {
          position.value = e.latlng
          await getMetoe(position)
        }
      }
      mymap.value.on('click', onMapClick)
    })

    let onSetPolygonMap = async () => {
      let markers;
      let poly;

      layerGroup.value.clearLayers()

      // geoJSON CI
      let carteCI = L.geoJSON(civ,{color: 'green', fillColor: "#ff7800", fillOpacity: 0.1, weight: 2, opacity: 0.7})
      layerGroup.value.addLayer(carteCI)

      markers = L.markerClusterGroup();
      for (const item of itemsActeur.value) {
        let geometry = JSON.parse(item.acteurPoly[0].geometrie)
        let centroid = JSON.parse(item.acteurPoly[0].centroid)
        poly = L.geoJSON(geometry)

        let marker =  L.geoJSON(centroid, {
          pointToLayer: function (feature, latlng) {
            return L.marker(latlng, {icon: centroidIcon}).bindPopup(
              `<div style="margin:0px">
                <p><b style="font-size: 15px">Info</b> </p>
                <span style="padding: 10px 0px">Parcelle : <b>` + item.libelle + `</b></span><br>
                <span>Superficie : <b>` + item.acteurPoly[0].superficie.toFixed(2) + `</b> Hectare</span>

                <div style="text-align: center; margin-top:10px">
                  <button
                    type="button"
                    polygon_id="`+item.acteurPoly[0].id+`"
                    class="download";
                    style=" background-color: #26A69A;
                    border: none;
                    color: white;
                    border-radius: 8px;
                    padding: 10px 20px;
                    display: inline-block;"
                  >
                    <b>Télécharger KML</b>
                  </button>
                </div>
              </div>`
            )
          }
        })

        layerGroup.value.addLayer(poly)
        markers.addLayer(marker);
      }
      layerGroup.value.addLayer(markers)
    }

    let onPolygon = async (item) => {
      let center = JSON.parse(item.acteurPoly[0].centroid)

      if (center.features) {
        let centroid = center.features[0].geometry.coordinates
        mymap.value.flyTo(new L.LatLng(centroid[1] , centroid[0]), 15);
      } else {
        let centroid = center.coordinates
        mymap.value.flyTo(new L.LatLng(centroid[1] , centroid[0]), 15);
      }
    }

    let onZoomIn = async () => {
      mymap.value.setZoom(mymap.value.getZoom() + 1)
    }

    let onZoomOut = async () => {
      mymap.value.setZoom(mymap.value.getZoom() - 1)
    }

    let getMetoe = async (position) => {
      axios({
        method: 'post',
        url: 'https://adb.jool-tech.com/_db/db/weather_api/get_prevision_jour',
        data: {
          longitude: position.value.lng,
          latitude: position.value.lat
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
        console.log("meteo.value",meteo.value);
      });

      axios({
        method: 'post',
        url: 'https://adb.jool-tech.com/_db/db/weather_api/get_prevision_hebdo',
        data: {
          longitude: position.value.lng,
          latitude: position.value.lat
        },
        headers:{
          username: 'jool_weather_api',
          password: '95Dpms2HHwhXruU6By46KLqprfaR7'
        }
      }).then( response => {
        infoMeteoHebdo.value = response.data
        console.log("infoMeteoHebdo", infoMeteoHebdo.value);

        for(const item of  infoMeteoHebdo.value){
          let url = `http://openweathermap.org/img/wn/${item.condition_icon}@2x.png`
          item.icon_url = url
        }
      });
    }

    let onGetMeteo = async () => {
      meteoModal.value = !meteoModal.value
    }

    $(function() {
      $('body').on('click', ".download", async function() {
        let id = $(this).attr("polygon_id")

        statePolygoneFichiersKml.filters.value = `
          query {
            searchPolygoneFichiersKml(polygone_Id: "${id}") {
              results {
                kmlUrl
              }
            }
          }
        `
        await statePolygoneFichiersKml.getItems();
        console.log("Polygone Fichiers Kml", statePolygoneFichiersKml.items.value);
        let kmlUrl = statePolygoneFichiersKml.items.value[0].kmlUrl

        axios({
          url: `https://sc.preprod.jool-tech.com/graphql/${kmlUrl}`,
          method: 'GET',
          responseType: 'blob'
        }).then((response) => {
          const url = window.URL.createObjectURL(new Blob([response.data]));
          console.log("url",url);
          const link = document.createElement('a');
          link.href = url;
          console.log("link.href",link.href);
          link.setAttribute('download', 'download.kml');
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
        })
      })
    })

    return{
      parcelleModal,
      cartographieModal,
      parametreModal,
      state,
      menu,
      modalIsOpened,
      onPolygon,
      itemsActeur,
      onZoomIn,
      onZoomOut,
      onGetMeteo,
      meteo,
      infoMeteoHebdo,
      icon_url,
      meteoModal,
      stateConnexion,

      carousel: ref(true),
      slide: ref(1),
    }
  }
})
</script>

<style scoped>

.divParcelles {
  margin-bottom: 9vh;
}

.parcelle {
  width: 78vw;
  height: 11vh;
  color: black;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(5px)
}

.carousel {
  margin-left: 10px;
  margin-right: 10px;
  margin-bottom: 85px;
}
</style>
