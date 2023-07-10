<template>
  <div>
   <q-card flat v-if="!focusSearch"
        class="q-ma-md bg-white text-secondary fixed-bottom z-max"
        style="height: 7%; border-radius: 10px"
      >
        <div class="row justify-between">
          <div class="col">
            <div class="text-center text-grey-8" >
              <div class="q-mt-sm" v-if="menu.home">
                <q-btn flat class="q-pa-none">
                  <q-item-label>
                    <q-avatar square size="20px">
                      <img src="HomeActive.svg">
                    </q-avatar> <br>
                    <span class="text-secondary" style="font-size:7px;">Accueil</span>
                  </q-item-label>
                </q-btn>
              </div>
              <div class="q-mt-xs" v-else>
                <q-btn flat @click="onMenuBarChanged('home')">
                  <q-avatar square size="22px">
                    <img src="Home.svg">
                  </q-avatar>
                </q-btn>
              </div>
            </div>
          </div>

          <div class="col">
            <div class="text-center text-grey-8" >
              <div class="q-mt-sm" v-if="menu.parcelle">
                <q-btn flat class="q-pa-none">
                  <q-item-label>
                    <q-avatar square size="20px">
                      <img src="ParcellesActive.svg">
                    </q-avatar> <br>
                    <span class="text-secondary" style="font-size:7px;">Parcelles</span>
                  </q-item-label>
                </q-btn>
              </div>
              <div class="q-mt-xs" v-else>
                <q-btn flat @click="onMenuBarChanged('parcelle')">
                  <q-avatar square size="22px">
                    <img src="Parcelles.svg">
                  </q-avatar>
                </q-btn>
              </div>
            </div>
          </div>

          <div class="col">
            <div class="text-center text-grey-8" v-if="menu.add">
              <div class="" style="position:relative; top:-8px">
                <q-btn icon="close" padding="md" size="md" round color="black" @click="onMenuBarChanged('close')"/>
              </div>
            </div>
            <div class="text-center text-grey-8" v-else>
              <div class="" style="position:relative; top:6px">
                <q-btn icon="add" size="md" round color="black"  @click="onMenuBarChanged('add')"/>
              </div>
            </div>
          </div>

          <div class="col">
            <div class="text-center text-grey-8" >
              <div class="q-mt-sm" v-if="menu.cartographie">
                <q-btn flat class="q-pa-none">
                  <q-item-label>
                    <q-avatar square size="20px">
                      <img src="CartographiesActive.svg">
                    </q-avatar> <br>
                    <span class="text-secondary" style="font-size:7px;">Cartographies</span>
                  </q-item-label>
                </q-btn>
              </div>
              <div class="q-mt-xs" v-else>
                <q-btn flat @click="onMenuBarChanged('cartographie')">
                  <q-avatar square size="22px">
                    <img src="Cartographies.svg">
                  </q-avatar>
                </q-btn>
              </div>
            </div>
          </div>

          <div class="col">
            <div class="text-center text-grey-8" >
              <div class="q-mt-sm" v-if="menu.parametre">
                <q-btn flat class="q-pa-none">
                  <q-item-label>
                    <q-avatar square size="22px">
                      <img src="ParametresActive.svg">
                    </q-avatar> <br>
                    <span class="text-secondary" style="font-size:7px;">Paramètres</span>
                  </q-item-label>
                </q-btn>
              </div>
              <div class="q-mt-xs" v-else>
                <q-btn flat @click="onMenuBarChanged('parametre')">
                  <q-avatar square size="22px">
                    <img src="Parametres.svg">
                  </q-avatar>
                </q-btn>
              </div>
            </div>
          </div>
        </div>
      </q-card>

    <!-- Nouvelle parcelle -->
    <q-dialog v-model="addModal" persistent transition-show="slide-up" transition-hide="slide-down">
      <q-card class="modal" :style="drawerStyle()" >
        <div clickable class="q-mt-sm q-pa-sm q-pl-lg q-pr-lg text-center" v-touch-pan.mouse="slideDrawer" style="margin-left:60px; margin-right:60px;">
          <q-separator size="5px" color="grey-8" inset  @click="onMenuBarChanged('close')"/>
        </div>
        <nouveau-acteur />
      </q-card>
    </q-dialog>

  </div>
</template>

<script>
import { useQuasar } from 'quasar'
import $ from 'jquery'
import { defineComponent, getCurrentInstance, ref, inject, provide, watch} from "vue";
import NouveauActeur from "src/components/Acteur/NouveauActeur.vue"
export default defineComponent({
  // name: 'ComponentName',

  components: {
    NouveauActeur
  },

  setup() {
    const instance = getCurrentInstance()
    let addModal = inject("addModal")
    let parcelleModal = inject("parcelleModal")
    let cartographieModal = inject("cartographieModal")
    let parametreModal = inject("parametreModal")
    let menu = inject("menu")
    let modalIsOpened = inject("modalIsOpened")
    let focusSearch = inject("focusSearch")
    let layerGroup = inject("layerGroup")

    const onMenuBarChanged = (element) => {
      modalIsOpened.value = true
      for (let el in menu.value) {
        menu.value[el] = false
      }
      menu.value[element] = true

      addModal.value = false
      parcelleModal.value = false
      cartographieModal.value = false
      parametreModal.value = false


      switch (element) {
        case "home":
          modalIsOpened.value = false
          break;

        case "parcelle":
          parcelleModal.value = true
          break;

        case "add":
          addModal.value = true
          break;

        case "close":
          modalIsOpened.value = false
          break;

        case "cartographie":
          cartographieModal.value = true
          break;

        case "parametre":
          parametreModal.value = true
          break;
      }
    }

    watch(modalIsOpened, () => {
      if (!modalIsOpened.value) {
        onMenuBarChanged('home')
        modalIsOpened.value = false
      }
    })

    const $q = useQuasar()
    let top = 0
    let drawerPos = ref(-150)
    const drawerStyle = () => {
      return {
        bottom: `${drawerPos.value}px`
      }
    }

    const slideDrawer = (ev) => {
      //console.log("ev", ev);
      const { height } = $q.screen
      if (top == 0) {
        top = drawerPos.value
      }
      let taille = $(ev.evt.path[2]).height()
      drawerPos.value = drawerPos.value - ev.delta.y

      if (ev.isFinal === true) {
        if (drawerPos.value > top) {
          animateDrawerTo(top)
        }
        else if (drawerPos.value < -taille / 3) {
          drawerPos.value = top
          onMenuBarChanged('close')
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

    return{
      onMenuBarChanged,
      menu,
      addModal,
      focusSearch,
      drawerStyle,
      slideDrawer,
    }
  }
})
</script>

<style scoped>
.top {
  border-radius: 10px;
  min-height: 20vw;
  position: absolute;
  top: 2vh;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px);
}

.modal {
  width: 100%;
  border-radius: 15px;
  position: relative;
  bottom: -20%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>
