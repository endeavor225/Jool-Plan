<template>
  <q-card class="acteur" v-if="!showModal" :style="drawerStyle()" >
    <div clickable class="q-mt-xs q-pa-sm q-pl-lg q-pr-lg text-center" v-touch-pan.mouse="slideDrawer" style="margin-left:80px; margin-right:80px;" >
      <q-separator size="5px" color="grey-8" inset @click="onClose" style=""/>
    </div>

    <div class="q-mb-xs q-mt-xs text-center">
      <span style="font-size: 30px">Parcelles</span>
    </div>

    <div style="margin-top:-15px;">
      <q-input
        v-model="search"
        @focus="onSearchFocus()"
        @blur="onSearchNoFocus()"
        dense
        rounded
        standout="bg-grey-1 text-black"
        :input-style="{ color: 'black' }"
        class="q-ma-lg q-mt-md q-mb-md"
        label="Recherche...">
        <template v-slot:append>
          <q-avatar size="40px" style="right:-13px">
              <img src="Search.svg">
          </q-avatar>
        </template>
      </q-input>
    </div>

    <q-scroll-area
      :thumb-style="{ right: '4px',
        borderRadius: '5px',
        backgroundColor: '#26A69A',
        width: '2px', opacity: 1
      }"
      :bar-style="{ right: '3px',
        borderRadius: '2px',
        background: '#26A69A',
        width: '5px',
        opacity: 0.2
      }"
      style="height: 64vh; margin-top:-12px;"
    >

      <div v-if="!itemsActeur.length || !items.length && search" align="center" style="margin-top: 50px">
        <q-spinner
          v-if="spinnerActeur"
          color="primary"
          size="3em"
        />
        <q-icon name="find_in_page" color="white" size="100px" style="opacity:0.5"/>
        <div class="text-body1 text-weight-light">Aucune données</div>
      </div>
      <!-- <q-pull-to-refresh @refresh="refresh"> -->
        <div v-if="!search">
          <q-list  v-for="item in itemsActeur" :key="item" >
            <q-item clickable v-ripple class="bg-white q-mb-sm q-ml-lg q-mr-lg q-pa-md" style="border-radius: 10px" @click="onShow(item)">
            <q-item-section>
              <q-item-label > {{item.libelle}} </q-item-label>
              <q-item-label caption> {{ $FormatDate(item.createdAt)}} </q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-item-label class="text-secondary"> {{ item.acteurPoly[0].superficie.toFixed(2) }} ha </q-item-label>
            </q-item-section>
          </q-item>
          </q-list>
        </div>
        <div v-else>
          <q-list  v-for="item in items" :key="item" >
            <q-item clickable v-ripple class="bg-white q-mb-sm q-ml-lg q-mr-lg q-pa-md" style="border-radius: 10px" @click="onShow(item)">
            <q-item-section>
              <q-item-label > {{item.libelle}} </q-item-label>
              <q-item-label caption> {{ $FormatDate(item.createdAt)}} </q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-item-label class="text-secondary"> {{ item.acteurPoly[0].superficie.toFixed(2) }} ha </q-item-label>
            </q-item-section>
          </q-item>
          </q-list>
        </div>
      <!-- </q-pull-to-refresh> -->
    </q-scroll-area>
  </q-card>

  <q-dialog v-model="showModal"
    position="bottom"
    full-width
    persistent
    transition-show="slide-up"
    transition-hide="slide-down"
    seamless
  >
    <show-detail :showItem="item_select"/>
  </q-dialog>


</template>

<script>
import { useQuasar } from 'quasar'
import $ from 'jquery'
import { defineComponent, getCurrentInstance, ref, onMounted, inject, provide, watch} from "vue";
import MenuBar from "components/MenuBar.vue"
import ShowDetail from "src/components/Acteur/Detail-Parcelle/ShowItem.vue"
export default defineComponent({
  // name: 'ComponentName',

  components: {
    MenuBar,
    ShowDetail
  },

  setup() {
    const instance = getCurrentInstance()
    let itemsActeur = inject("itemsActeur")
    let items = ref([])
    let parcelleModal = inject("parcelleModal")
    let menu = inject("menu")
    let focusSearch = inject("focusSearch")
    let search = ref()
    let item_select = ref({})
    let showModal = ref(false);
    provide("showModal", showModal)
    const service = "searchActeurs";
    let state = inject(service);
    let spinnerActeur = inject("spinnerActeur")

    const user = JSON.parse(localStorage.getItem('userAuth-session-app'))

    onMounted( async () =>{})

    const $q = useQuasar()
    let top = 0
    let drawerPos = ref(0)
    const drawerStyle = () => {
      return {
        bottom: `${drawerPos.value}px`
      }
    }

    const slideDrawer = (ev) => {
      //console.log("ev", ev);
      const { height } = $q.screen
      /* if (top == 0) {
        top = drawerPos.value
      } */
      let taille = $(ev.evt.path[2]).height()
      drawerPos.value = drawerPos.value - ev.delta.y

    console.log("eee", $(ev.evt.path[2]).height());

        drawerPos.value < taille
      if (ev.isFinal === true) {
        if (drawerPos.value > top) {
          animateDrawerTo(top)
        }
        else if (drawerPos.value < -taille / 3) {
          drawerPos.value = top
          onClose()
        }
        else {
          animateDrawerTo(top)
        }
      }
    }

    const animateDrawerTo = (height) => {
      const diff = height - drawerPos.value

      if (diff !== 0) {
        drawerPos.value = top
        setTimeout(() => {
          animateDrawerTo(height)
        }, 3000)
      }
    }

    watch(search, async () => {
      const needle = search.value.toLowerCase()
      items.value = itemsActeur.value.filter((v) => v.libelle.toLowerCase().indexOf(needle) > -1)
    });

    let refresh = async (done) => {
      state.filters.value = `
      query {
        searchActeurs (deleted: false, enterprise_Id: "${user.enterprise.id}") {
          results(limit:1000, ordering:"-created_at") {
            id
            libelle
            createdAt
            acteurPoly {
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
            acteurVarieteCulture{
              id
              varietesCultures{
                libelle
              }
            }
          }
          totalCount
        }
      }

      `
      await state.getItems();
      // itemsActeur.value = state.items.value
      itemsActeur.value = JSON.parse(localStorage.getItem('searchActeurs'))
      done()
    }

    let onShow = async (item) => {
      item_select.value = item
      showModal.value = true
      focusSearch.value = false
    }

    let onClose = () => {
      menu.value.parcelle = false
      parcelleModal.value = false
      menu.value.home = true
      focusSearch.value = false
    }

    let onSearchFocus = async () => {
      focusSearch.value = true
    }

    let onSearchNoFocus = async () =>{
      focusSearch.value = false
    }



    return{
      items,
      itemsActeur,
      onClose,
      onShow,
      showModal,
      item_select,
      search,
      refresh,
      onSearchFocus,
      onSearchNoFocus,
      spinnerActeur,
      drawerStyle,
      slideDrawer,
    }
  }
})
</script>
<style scoped>
.acteur {
  height: 100vh;
  border-radius: 15px;
  position: relative;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}
</style>
