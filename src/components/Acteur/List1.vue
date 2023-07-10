<template>
  <q-card class="acteur" v-if="!showModal" style="border-radius: 15px;">
    <div clickable class="q-mt-md text-center" style="margin-left:90px; margin-right:90px;">
      <q-separator size="5px" color="grey-8" inset   @click="onClose"/>
    </div>

    <div class="q-mb-xs q-mt-xs text-center">
      <span style="font-size: 30px">Parcelles</span>
    </div>

    <div style="margin-top:-10px;">
      <q-input
        v-model="search"
        dense
        rounded
        standout="bg-grey-1 text-black"
        :input-style="{ color: 'black' }"
        class="q-ma-lg q-mt-md q-mb-md"
        label="Recherche ...">
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
      style="height: 63vh; margin-top:-10px;"
    >
      <!-- <item-acteur v-for="item in state.items.value" :key="item" :item="item"/> -->
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

        <div v-if="!search && itemsActeur.length == 0" class="no-task absolute-center" align="center">
          <!-- <q-icon name="sentiment_dissatisfied" size="100px" color="primary" /> -->
          <div class="text-h5 text-primary">Aucune parcelle</div>
        </div>

        <div v-if="search">
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

        <div v-if="search && items.length == 0" class="no-task absolute-center" align="center">
          <!-- <q-icon name="sentiment_dissatisfied" size="100px" color="primary" /> -->
          <div class="text-h5 text-primary">Aucune parcelle</div>
        </div>

      <!-- </q-pull-to-refresh> -->
    </q-scroll-area>


    <!-- Bar de menu -->
    <!-- <menu-bar/> -->
  </q-card>

  <q-dialog v-model="showModal"
    position="bottom"
    full-width
    persistent
    transition-show="slide-up"
    transition-hide="slide-down"
    seamless
    class="z-max"
  >
    <show-detail :showItem="item_select"/>
  </q-dialog>


</template>

<script>
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
    let search = ref()
    let item_select = ref({})
    let showModal = ref(false);
    provide("showModal", showModal)



    onMounted( async () =>{
      const user = JSON.parse( localStorage.getItem('userAuth-session-app'))

    })

    watch(search, async () => {
      const needle = search.value.toLowerCase()
      items.value = itemsActeur.value.filter((v) => v.libelle.toLowerCase().indexOf(needle) > -1)
    });

    let refresh = async (done) => {
      console.log("refresh");
      //await state.getItems();
      done()
    }

    let onShow = async (item) => {
      item_select.value = item
      showModal.value = true
    }

    let onClose = () => {
      menu.value.parcelle = false
      parcelleModal.value = false
      menu.value.home = true
    }

    return{
      items,
      itemsActeur,
      onClose,
      onShow,
      showModal,
      item_select,
      search,
      refresh
    }
  }
})
</script>
<style scoped>
.acteur {
  height: 81vh;
  margin-left: 10px;
  margin-right: 10px;
  position: relative;
  bottom: 85px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}
</style>
