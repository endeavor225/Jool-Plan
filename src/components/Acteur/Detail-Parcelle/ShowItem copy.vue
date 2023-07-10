<template>
  <q-card class="acteur q-mb-none q-pb-none"  v-if="!editModal">
    <q-card-section class="q-pa-xs q-ma-none" >
      <div clickable class="q-ma-md text-center" style="margin-left:90px; margin-right:90px;">
        <q-separator size="5px" color="grey-8" inset   @click="onClose"/>
      </div>

      <div class="q-ma-md text-h6 text-center" style="margin-top: -5px">
        {{showItem.libelle}}
      </div>

      <div class="row q-ma-xs items-start q-gutter-xs" style="margin-top: -5px">
        <div class="col q-ma-xs">
          <q-card class="text-center">
            <q-card-section class="text-white" style="background-color: #337682">
              <div class="text-weight-light" style="font-size:10px">SUPERFICIE</div>
              <div class="text-weight-bold" style="font-size:18px">
                {{showItem.acteurPoly[0].superficie.toFixed(2)}} ha
              </div>
            </q-card-section>
          </q-card>
        </div>

        <div class="col q-ma-xs" @click="onShowCulture">
          <q-card class="text-center">
            <q-card-section class="text-white" style="background-color: #F99746">
              <div class="text-weight-light" style="font-size:10px">CULTURES</div>
              <div class="text-weight-bold" style="font-size:18px">
                {{showItem.acteurVarieteCulture.length}}
              </div>
              <!-- <q-btn class="absolute-bottom-right q-mr-xs q-mb-xs" size="xs" round color="white" text-color="black" icon="add" @click="onShowCulture"/> -->
            </q-card-section>

          </q-card>
        </div>

        <div class="col q-ma-xs">
          <q-card class="text-center">
            <q-card-section class="text-white" style="background-color: #158AFF">
              <div class="text-weight-regular" style="font-size:10px">CARTOGRAPHIES</div>
              <div class="text-weight-bold" style="font-size:18px">
                {{showItem.acteurProjet.length}}
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


    <q-card-section style="height: 50vh" class="scroll q-pt-none q-pb-none">
      <q-list v-if="showItem.acteurProjet.length > 0">
        <item-projet  v-for="item in showItem.acteurProjet" :key="item" :item="item"/>
      </q-list>
    </q-card-section>

    <!-- <q-card-section class="q-ma-none q-pa-none"> -->
      <q-footer class="text-secondary" style="background: rgba(255, 255, 255, 0);">
      <q-separator class="q-mt-none q-mb-none q-ma-md" color="grey-6" size="1px" inset  style="margin-bottom: 10px"/>
        <div class="row q-gutter-sm">
          <div class="col q-ml-md">
            <q-btn class="full-width" color="primary" no-caps label="Parcelliser" @click="onParcellisation(showItem)"></q-btn>
          </div>
          <div class="col">
            <q-btn class="full-width" color="primary" no-caps label="Modifier" @click="onEdit"></q-btn>
          </div>
          <div class="col q-mr-sm">
            <q-btn  class="full-width" color="primary" no-caps label="Supprimer" @click="onDelete"></q-btn>
          </div>
        </div>

      <div align="center" class="q-ma-sm">
        <q-btn icon="add" size="lg" round color="black" @click="onAdd"/>
      </div>
    </q-footer>
    <!-- </q-card-section> -->
  </q-card>

  <!-- EDITE -->
  <q-dialog
    v-model="editModal"
    full-width class="z-max"
    transition-show="slide-up"
    transition-hide="slide-down"
    seamless
  >
    <edit-actur :editItem="showItem"/>
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

            <q-list dense class="rounded-borders" v-for="culture in showItem.acteurVarieteCulture" :key="culture">
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

    let onParcellisation = (showItem) => {
      console.log("showItem",showItem);
      router.push({ name: 'parcellisation', params: { itemParcelle: JSON.stringify(showItem) } })
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
      if (props.showItem.acteurVarieteCulture.length < 3) {
        lengthScrool.value = "height: 60px; max-height: 300px;"
      }

      if (props.showItem.acteurVarieteCulture.length >= 3 && props.showItem.acteurVarieteCulture.length < 6) {
        lengthScrool.value = "height: 100px; max-height: 300px;"
      }

      if (props.showItem.acteurVarieteCulture.length >= 6 && props.showItem.acteurVarieteCulture.length < 10) {
        lengthScrool.value = "height: 150px; max-height: 300px;"
      }

      if (props.showItem.acteurVarieteCulture.length >=10  && props.showItem.acteurVarieteCulture.length < 15) {
        lengthScrool.value = "height: 250px; max-height: 300px;"
      }

      if (props.showItem.acteurVarieteCulture.length >15 ) {
        lengthScrool.value = "height: 300px; max-height: 300px;"
      }

    });

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
      onParcellisation
    }
  }
})
</script>

<style scoped>
.acteur {
  height:95vh;
  border-radius: 15px;
  /* position: relative; */
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}

.my-card {
  width: 100%;
}

</style>
