<template>
  <q-card class="acteur q-mb-none q-pb-none" :style="drawerStyle()" v-if="!editModal" style="border-radius: 15px;">
    <q-card-section class="q-pa-xs q-ma-none" >
      <div clickable class="q-mt-xs q-mb-xs q-pa-sm q-pl-lg q-pr-lg text-center" v-touch-pan.mouse="slideDrawer" style="margin-left:80px; margin-right:80px;">
        <q-separator size="5px" color="grey-8" inset @click="onClose"/>
      </div>

      <div class="q-ma-md text-h6 text-center" style="margin-top: -5px">
        {{showItemProjet.libelle}}
      </div>

      <div class="row q-ma-xs items-start q-gutter-xs" style="margin-top: -5px">
        <div class="col q-ma-xs ">
          <q-card class="text-center ">
            <q-card-section class="text-white q-pl-none q-pr-none" style="background-color: #337682">
              <div class="text-weight-light" style="font-size:10px">SUPERFICIE</div>
              <div class="text-weight-bold" style="font-size:18px">
                {{showItemProjet.acteurPoly[0].superficie.toFixed(2)}} ha
              </div>
            </q-card-section>
          </q-card>
        </div>

        <div class="col q-ma-xs" @click="onShowCulture">
          <q-card class="text-center">
            <q-card-section class="text-white q-pl-none q-pr-none" style="background-color: #F99746">
              <div class="text-weight-light" style="font-size:10px">CULTURES</div>
              <div class="text-weight-bold" style="font-size:18px">
                {{showItemProjet.acteurVarieteCulture.length}}
              </div>
            </q-card-section>
          </q-card>
        </div>

        <div class="col q-ma-xs">
          <q-card class="text-center">
            <q-card-section class="text-white q-pl-none q-pr-none" style="background-color: #158AFF">
              <div class="text-weight-regular" style="font-size:10px">CARTOGRAPHIES</div>
              <div class="text-weight-bold" style="font-size:18px">
                {{showItemProjet.acteurProjet.length}}
              </div>
            </q-card-section>
          </q-card>
        </div>
      </div>

      <q-separator class="q-ma-md" color="grey-6" size="1px" inset style="margin-top: 10px"/>

      <div class="text-center" style="margin-top: -5px;">
        <span style="font-size: 20px">Liste de cartographie</span>
      </div>
    </q-card-section>

    <q-card-section style="height: 45vh" class="scroll q-pt-none q-pb-none">
      <q-list v-if="showItemProjet.acteurProjet.length > 0">
        <item-projet  v-for="item in showItemProjet.acteurProjet" :key="item" :item="item"/>
      </q-list>

      <div v-if="!showItemProjet.acteurProjet.length" class="absolute-center" align="center">
        <div class="text-body1 text-weight-light" style="position:relative; bottom: 10px">Aucune cartographie</div>
      </div>
    </q-card-section>

    <q-footer class="text-secondary q-mb-md" style="background: rgba(255, 255, 255, 0);">
      <q-separator class="q-mt-none q-mb-none q-ma-md" color="grey-6" size="1px" inset  style="margin-bottom: 10px"/>
        <div class="row q-gutter-sm">
          <div class="col q-ml-md">
            <q-btn class="full-width" color="primary" no-caps @click="onParcellisation(showItemProjet)">
              <q-avatar square size="20px" icon="map">
              </q-avatar>
              <span style="font-size: 10px; margin-left: 2px; position: relative; top: 2px;">Parcelliser</span>
            </q-btn>
          </div>
          <div class="col">
            <q-btn class="full-width" color="primary" no-caps @click="onEdit">
              <q-avatar square size="15px">
                <img src="Edit.svg">
              </q-avatar>
              <span style="font-size: 10px; margin-left: 5px; position: relative; top: 2px;">Modifier</span>
            </q-btn>
          </div>
          <div class="col q-mr-sm">
            <q-btn  class="full-width" color="primary" no-caps @click="onDelete">
              <q-avatar square size="15px">
                <img src="Delete.svg">
              </q-avatar>
              <span style="font-size: 10px; margin-left: 3px; position: relative; top: 2px;">Supprimer</span>
            </q-btn>
          </div>
        </div>
    </q-footer>
  </q-card>

  <!-- EDITE -->
  <q-dialog
    v-model="editModal"
    full-width class="z-max"
    transition-show="slide-up"
    transition-hide="slide-down"
    seamless
  >
    <edit-actur :editItem="showItemProjet"/>
  </q-dialog>

  <!-- DELETE -->
  <q-dialog v-model="deleteMadal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down" >
    <delete-actur :deleteItem="showItem"/>
  </q-dialog>

  <!-- Culture -->
  <q-dialog v-model="cultureMadal" class="z-max" full-width transition-show="slide-up" transition-hide="slide-down" >
    <q-card style="
      border-radius: 10px;
      background: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(3px)"
    >
      <div>
        <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
          <q-separator size="5px" color="grey-8" inset  @click="cultureMadal = false"/>
        </div>

        <div v-if="showItem.acteurVarieteCulture.length > 0" class="q-mb-md">
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
            :style="lengthScrool"
          >

            <q-list dense class="rounded-borders" v-for="culture in showItemProjet.acteurVarieteCulture" :key="culture">
              <q-item class=" q-ml-sm">
                <q-item-section class="text-weight-light">
                  {{culture.varietesCultures.libelle}}
                </q-item-section>
              </q-item>
            </q-list>
          </q-scroll-area>
        </div>
        <div v-else>
          <div class="q-ma-md text-center">Aucune culture</div>
        </div>

      </div>
    </q-card>
  </q-dialog>

</template>

<script>
import { useRouter } from 'vue-router'
import { useQuasar } from 'quasar'
import $ from 'jquery'
import { defineComponent, getCurrentInstance, ref, onMounted, inject, provide, watch} from "vue";
import ItemProjet from "components/Acteur/Detail-Parcelle/Item.vue"
import NouveauActeur from "components/Acteur/Detail-Parcelle/Nouveau.vue"
import EditActur from "components/Acteur/Update.vue"
import DeleteActur from "components/Acteur/Delete.vue"
export default defineComponent({
  // name: 'ComponentName',

  components: {
    ItemProjet,
    NouveauActeur,
    EditActur,
    DeleteActur
  },

  props: {
    showItem: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup (props) {
    const instance = getCurrentInstance()
    const router = useRouter()
    let showModal = inject("showModal")
    let menu = inject("menu")
    let parcelleModal = inject("parcelleModal")
    let cartographieModal = inject("cartographieModal")
    let parametreModal = inject("parametreModal")
    let addModal = inject("addModal")
    let deleteMadal = ref(false)
    provide("deleteMadal", deleteMadal)
    let editModal = ref(false)
    provide("editModal", editModal)
    let cultureMadal = ref(false)
    let focusSearch = inject("focusSearch")
    let showItemProjet = ref(props.showItem)
    provide("showItemProjet", showItemProjet)

    let onAdd = () => {
      menu.value.add = true
      addModal.value = true
      showModal.value = false

      if (menu.value.home) {
        menu.value.home = false;
      }
      if (menu.value.parcelle) {
        menu.value.parcelle = false;
        parcelleModal.value = false;
      }
      if (menu.value.cartographie) {
        menu.value.cartographie = false;
        cartographieModal.value = false;
      }
      if (menu.value.parametre) {
        menu.value.parametre = false;
        parametreModal.value = false;
      }
    }
    let onClose = () => {
      showModal.value = false
    }

    let onParcellisation = (showItemProjet) => {
      console.log("showItem",showItemProjet);
      router.push({ name: 'parcellisation', params: { itemParcelle: JSON.stringify(showItemProjet) } })
    }

    let onEdit = () => {
      editModal.value = true
      focusSearch.value = true
    }

    let onDelete = () => {
     deleteMadal.value = true
    }

    let onShowCulture = () => {
      cultureMadal.value = true
    }

    let lengthScrool = ref()
    watch(cultureMadal, async () => {
      if (showItemProjet.value.acteurVarieteCulture.length < 3) {
        lengthScrool.value = "height: 60px; max-height: 300px;"
      }

      if (showItemProjet.value.acteurVarieteCulture.length >= 3 && showItemProjet.value.acteurVarieteCulture.length < 6) {
        lengthScrool.value = "height: 100px; max-height: 300px;"
      }

      if (showItemProjet.value.acteurVarieteCulture.length >= 6 && showItemProjet.value.acteurVarieteCulture.length < 10) {
        lengthScrool.value = "height: 150px; max-height: 300px;"
      }

      if (showItemProjet.value.acteurVarieteCulture.length >=10  && showItemProjet.value.acteurVarieteCulture.length < 15) {
        lengthScrool.value = "height: 250px; max-height: 300px;"
      }

      if (showItemProjet.value.acteurVarieteCulture.length >15 ) {
        lengthScrool.value = "height: 300px; max-height: 300px;"
      }

    });

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

    return {
      onClose,
      onAdd,
      onDelete,
      deleteMadal,
      editModal,
      onEdit,
      onShowCulture,
      cultureMadal,
      lengthScrool,
      onParcellisation,
      showItemProjet,
      drawerStyle,
      slideDrawer,
    }
  }
})
</script>

<style scoped>
.acteur {
  height: 640px;
  margin-left: 10px;
  margin-right: 10px;
  margin-bottom: 85px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}

.my-card {
  width: 100%;
}

</style>
