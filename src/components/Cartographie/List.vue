<template>
  <q-card class="acteur" v-if="!addProjetModal && !showModal" :style="drawerStyle()" >
    <div clickable class="q-mt-xs q-pa-sm q-pl-lg q-pr-lg text-center" v-touch-pan.mouse="slideDrawer" style="margin-left:80px; margin-right:80px;">
      <q-separator size="5px" color="grey-8" inset   @click="onClose"/>
    </div>

    <div class="q-ma-md text-center" style="margin-top: -5px">
      <span style="font-size: 30px">Cartographies</span>
    </div>

    <div style="margin-top: -10px">
      <q-input
        v-model="search"
        @focus="onSearchFocus()"
        @blur="onSearchNoFocus()"
        dense
        rounded
        standout="bg-grey-1 text-black"
        :input-style="{ color: 'black' }"
        class="q-ma-lg"
        label="Recherche...">
        <template v-slot:append>
          <q-avatar size="40px" style="right:-13px">
              <img src="Search.svg">
          </q-avatar>
        </template>
      </q-input>
    </div>

    <div align="center" class="q-ml-lg q-mr-lg q-mb-md" style="margin-top: -10px">
      <q-btn
        @click="onCreateProjet"
        color="secondary"
        icon="add"
        type="submit"
        label="Nouveau projet"
        class="full-width"
        no-caps
        style="border-radius: 10px"
        padding="xs"
        size="lg"
      />
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
      style="height: 55vh;"
    >

    <div v-if="!itemsProjet.length || !items.length && search" align="center" style="margin-top:50px">
      <q-spinner
        v-if="spinnerProjet"
        color="primary"
        size="3em"
      />
      <q-icon name="find_in_page" color="white" size="100px" style="opacity:0.5"/>
      <div class="text-body1 q-mt-md text-weight-light">Aucune données</div>
    </div>


    <!-- <q-pull-to-refresh @refresh="refresh"> -->
      <div v-if="!search">
        <!-- <item-acteur v-for="item in state.items.value" :key="item" :item="item" /> -->
        <q-list v-for="item in itemsProjet" :key="item">
            <q-item clickable v-ripple class="bg-white q-mb-sm q-ml-lg q-mr-lg q-pa-md" style="border-radius: 10px" @click="onShow(item)">
              <q-item-section>
                <q-item-label > {{item.libelle}} </q-item-label>
                <q-item-label caption> {{ $FormatDate(item.createdAt)}} </q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-item-label class="text-secondary"> {{ item.projetPlan.length }} Plan(s)</q-item-label>
              </q-item-section>
            </q-item>

        </q-list>
      </div>
      <div v-else>
        <q-list v-for="item in items" :key="item">
          <q-item clickable v-ripple class="bg-white q-mb-sm q-ml-lg q-mr-lg q-pa-md" style="border-radius: 10px" @click="onShow(item)">
            <q-item-section>
              <q-item-label > {{item.libelle}} </q-item-label>
              <q-item-label caption> {{ $FormatDate(item.createdAt)}} </q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-item-label class="text-secondary"> {{ item.projetPlan.length }} Plan(s)</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </div>
      <!-- </q-pull-to-refresh> -->
    </q-scroll-area>
  </q-card>

  <!-- Modal de création de projet -->
  <q-dialog v-model="addProjetModal"
    transition-show="slide-up"
    transition-hide="slide-down"
    full-width
    seamless
  >
    <create-projet/>
  </q-dialog>

  <!-- Modal pour voir les details de projet  -->
  <q-dialog v-model="showModal"
    position="bottom"
    transition-show="slide-up"
    transition-hide="slide-down"
    full-width
    persistent
    seamless
  >
    <show-detail :showItem="item_select"/>
  </q-dialog>

</template>

<script>
import $ from 'jquery'
import { useQuasar } from 'quasar'
import { defineComponent, getCurrentInstance, ref, onMounted, inject, watch, provide, onBeforeUnmount} from "vue";
import ItemActeur from "components/Cartographie/Item.vue"
import CreateProjet from "components/Cartographie/Create.vue"
import ShowDetail from "components/Cartographie/ShowItem.vue"
export default defineComponent({
  // name: 'ComponentName',

  components: {
    ItemActeur,
    CreateProjet,
    ShowDetail
  },

  setup() {
    const $q = useQuasar()
    const instance = getCurrentInstance()
    const service = "updateProjet";
    let state = inject(service);
    const serviceProjet = "searchProjet";
    let stateProjet = inject(serviceProjet);
    let cartographieModal = inject("cartographieModal")
    let menu = inject("menu")
    let focusSearch = inject("focusSearch")
    let search = ref()
    let itemsProjet = inject("itemsProjet")
    provide("itemsProjet", itemsProjet)
    let items = ref([])
    let addProjetModal = ref(false)
    provide("addProjetModal", addProjetModal)
    let showModal = ref(false)
    provide("showModal", showModal)
    let item_select = ref({})
    let deleteMadal = ref(false)
    let formData = ref({})
    let spinnerProjet = inject('spinnerProjet')

    onMounted( async () =>{})

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
      items.value = itemsProjet.value.filter((v) => v.libelle.toLowerCase().indexOf(needle) > -1)
    });

    let onCreateProjet = () => {
      addProjetModal.value = true
      focusSearch.value = true
    }

    let onShow = (item) => {
      item_select.value = item
      showModal.value = true
      focusSearch.value = false
    }

    let onClose = () => {
      menu.value.cartographie = !menu.value.cartographie
      cartographieModal.value = !cartographieModal.value
      menu.value.home = !menu.value.home
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
      itemsProjet,
      onClose,
      addProjetModal,
      onCreateProjet,
      search,
      onShow,
      showModal,
      item_select,
      deleteMadal,
      onSearchFocus,
      onSearchNoFocus,
      spinnerProjet,
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
