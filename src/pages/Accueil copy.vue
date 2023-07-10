<template>
  <q-page>
    
    <div>
      <div id="mapid" style=" width: 100%; height: 100vh;"> </div>
    </div>
    
    <!-- Parcelle scroll  -->
    <q-footer class="q-ma-md q-pa-none text-dark transparent divParcelles" reveal style v-if="menu.home && state.items.value.length > 0">
      <h5 class="q-ma-xs text-h6">Parcelles</h5>

      <q-scroll-area style="height: 12vh; max-width: 100%;"
        :thumb-style="{
          width: '1px', opacity: 0
        }"
      >
        <div class="row no-wrap">
          <div v-for="item in state.items.value" :key="item" :item="item"
            class="q-pa-md q-mr-md parcelle row"
          >
            <div class="col">
              <q-item-label class="q-mb-sm" lines="1" style="font-size:18px">{{item.libelle}}</q-item-label>
              <q-item-label caption style="font-size:15px">{{ $FormatDate(item.createdAt)}}</q-item-label>
              <span caption> </span>
            </div>
            
            <div class="col text-right">
              <q-item-label class="text-subtitle1 q-mb-sm text-teal">{{item.acteurPoly[0].superficie.toFixed(2) }} ha</q-item-label>
              <q-item-label caption class="q-pt-xs">
                <small>
                  Cartographie(s)
                  <q-badge rounded color="teal"> {{item.acteurProjet.length }} </q-badge>
                </small>
              </q-item-label>
            </div>
          </div>
        </div>
      </q-scroll-area>
    </q-footer>

    <!-- Bar de menu -->
    <menu-bar/>

    <!-- Liste de parcelle  -->
    <q-dialog 
      position="bottom"
      v-model="parcelleModal" 
      persistent 
      full-width
      transition-show="slide-up" 
      transition-hide="slide-down"
      :style="style"
    >
      <list-acteur/>
    </q-dialog>

    <!-- Liste de projet -->
    <div>
      <q-dialog 
        :style="styleCartographie"
        position="bottom"
        v-model="cartographieModal" 
        persistent 
        full-width
        transition-show="slide-up" 
        transition-hide="slide-down"
      >
        <list-cartographie/>
      </q-dialog>

      <!-- <q-dialog 
        else
        style="opacity:0"
        position="bottom"
        v-model="cartographieModal" 
        persistent 
        full-width
        transition-show="slide-up" 
        transition-hide="slide-down"
      >
        <list-cartographie/>
      </q-dialog> -->

    </div>

    <q-dialog 
      position="bottom"
      v-model="parametreModal" 
      persistent 
      maximized
      transition-show="slide-up" 
      transition-hide="slide-down"
    >
      <list-cartographie/>
    </q-dialog>
    
  </q-page>
</template>

<script>
import { useRouter } from "vue-router"
import { defineComponent, getCurrentInstance, ref, onMounted, onBeforeMount, inject, provide, watch} from "vue";
import MenuBar from "components/MenuBar.vue"
import ListActeur from "components/Acteur/List.vue"
import ListCartographie from "components/Cartographie/List.vue"
export default defineComponent({
  // name: 'PageName',

  components: {
    MenuBar,
    ListActeur,
    ListCartographie
  },

  setup() {
    const token = localStorage.getItem('jool_Plan_id-session-app')
    const router = useRouter()
    const instance = getCurrentInstance()
    let mymap;
    let addModal = ref(false)
    provide("addModal", addModal)

    // Parcelle
    let parcelleModal = ref(false)
    provide("parcelleModal", parcelleModal)
    let etatShow = ref(false)
    provide("etatShow", etatShow)

    // Projet
    let cartographieModal = ref(false)
    provide("cartographieModal", cartographieModal)
    let showPlan = ref(false)
    provide("showPlan", showPlan)

    // Paramttre
    let parametreModal = ref(false)
    provide("parametreModal", parametreModal)

    let style = ref("")
    provide("style", style)

    let styleCartographie = ref("")
    provide("style", styleCartographie)


    let menu = ref({
      home : true,
      parcelle : false,
      add : false,
      cartographie : false,
      parametre : false,
    })
    provide("menu", menu)

    const service = "searchActeurs";
    let state = inject(service);

    onBeforeMount(() => {

      if (!token) {
        router.push("login")
      }
    })

    onMounted( async () =>{
      mymap = L.map('mapid',{
         zoomControl: false, 
      });

      L.tileLayer('http://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',{
        minZoom: 5,
        maxZoom: 20,
        subdomains:['mt0','mt1','mt2','mt3']
      }).addTo(mymap);
  
      function onLocationFound(e) {
        var radius = e.accuracy;
        L.marker(e.latlng).addTo(mymap) 
          .bindPopup("Vous êtes à " + radius.toPrecision(4)  + " mètres de ce point")
          .openPopup();
      }

      L.control.zoom({
        position: 'topright'
      }).addTo(mymap);
      
      
      mymap.locate({ setView: true, maxZoom: 18});
      mymap.on("locationfound", onLocationFound);


      state.filters.value = `
      id
      libelle
      createdAt
      acteurPoly {
        superficie
        geometrie
      }
      acteurProjet{
        id
        libelle
        description
        projetPlan{
          id
          libelle
          altitude
        }
      }
      acteurVarieteCulture{
        id
        varietesCultures{
          libelle
        }
      }
      `
      await state.getItems();
      //console.log("state.items",state.items.value);

      for (const item of state.items.value) {
        let geometry = JSON.parse(item.acteurPoly[0].geometrie)
        L.geoJSON(geometry).addTo(mymap).bindPopup(`Libelle : <b>` + item.libelle + ` </b> <br> Superficie : <b>` + item.acteurPoly[0].superficie.toFixed(2) + ` Hectare</b>`)
      }

    })

    if(!cartographieModal.value) {
      showPlan.value = false
    }

    watch(showPlan, async () => {
      console.log("watch", showPlan.value);
      if (showPlan.value) {
        styleCartographie.value = "opacity:0"
      } else {
        styleCartographie.value = "opacity:1"
      }
    });
    // watch(cartographieModal, async () => {
    //   if (!cartographieModal.value) {
    //     showPlan.value = false
    //   }
    // });

    watch(etatShow, async () => {
      console.log(etatShow.value);
      if (etatShow.value) {
        style.value = "opacity:0"
      } else {
        style.value = "opacity:1"
      }
    });

    return{
      parcelleModal,
      cartographieModal,
      parametreModal,
      state,
      showPlan,
      menu,
      style,
      styleCartographie
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
  background-color: white;
  color: black;
  border-radius: 10px;
}
</style>