<template>
  <div>
    <!-- <q-footer
        reveal
        bordered
        class="q-ma-md bg-white text-secondary z-max"
        style="height: 7%; border-radius: 10px"

      >
        <div class="row justify-between">
          <div class="col">
            <div class="text-center text-grey-8">
              <div class="q-mt-sm">
                <q-btn icon="add" size="sm" round color="black" @click="onAdd"/>
              </div>
            </div>
          </div>
        </div>
      </q-footer> -->

      <div  class="q-ma-md text-secondary">
        <div class="row justify-between">
        <div class="col">
          <div class="text-center text-grey-8">
            <div class="q-mt-sm">
              <q-btn icon="add" size="sm" round color="black" @click="onAdd"/>
            </div>
          </div>
        </div>
      </div>
      </div>



  </div>
</template>

<script>
import { ref, defineComponent, getCurrentInstance, inject, provide} from "vue";
import NouveauActeur from "src/components/Acteur/NouveauActeur.vue"
export default defineComponent({
  // name: 'ComponentName',

  components: {
    NouveauActeur
  },
 
  setup() {
    const instance = getCurrentInstance()
    let addModal = ref(false)
    // let menu = ref({
    //   home : true,
    //   parcelle : false,
    //   add : false,
    //   cartographie : false,
    //   parametre : false,
    // })

    let parcelleModal = inject("parcelleModal")
    let menu = inject("menu")
   

    // Bouton Home
    let onHome = () => {
      menu.value.home = !menu.value.home;

      if (menu.value.add) {
        menu.value.add = !menu.value.add
        addModal.value = !addModal.value;
      }
      if (menu.value.parcelle) {
        menu.value.parcelle = !menu.value.parcelle
        parcelleModal.value = !parcelleModal.value
      }
      if (menu.value.cartographie) {
        menu.value.cartographie = !menu.value.cartographie
      }
      if (menu.value.parametre) {
        menu.value.parametre = !menu.value.parametre
      }
    }

    // Bouton Ajout
    let onAdd = () => {
      menu.value.add = !menu.value.add

      if (menu.value.home) {
        menu.value.home = !menu.value.home
      }
      if (menu.value.parcelle) {
        menu.value.parcelle = !menu.value.parcelle
        parcelleModal.value = !parcelleModal.value
      }
      if (menu.value.cartographie) {
        menu.value.cartographie = !menu.value.cartographie
      }
      if (menu.value.parametre) {
        menu.value.parametre = !menu.value.parametre
      }

      addModal.value = !addModal.value;
    }

    // Bouton Parcelles
    let onParcelle = () => {
      menu.value.parcelle = !menu.value.parcelle
      parcelleModal.value = !parcelleModal.value

      if (menu.value.add) {
        menu.value.add = !menu.value.add
        addModal.value = !addModal.value;
      }
      if (menu.value.home) {
        menu.value.home = !menu.value.home
      }
      if (menu.value.cartographie) {
        menu.value.cartographie = !menu.value.cartographie
      }
      if (menu.value.parametre) {
        menu.value.parametre = !menu.value.parametre
      }
    }

    // Bouton Cartographie 
    let onCartographie = () => {
      menu.value.cartographie = !menu.value.cartographie

      if (menu.value.add) {
        menu.value.add = !menu.value.add
        addModal.value = !addModal.value;
      }
      if (menu.value.home) {
        menu.value.home = !menu.value.home
      }
      if (menu.value.parcelle) {
        menu.value.parcelle = !menu.value.parcelle
        parcelleModal.value = !parcelleModal.value
      }
      if (menu.value.parametre) {
        menu.value.parametre = !menu.value.parametre
      }
    }

    // Bouton Paramètre
    let onParametre = () => {
      menu.value.parametre = !menu.value.parametre

      if (menu.value.add) {
        menu.value.add = !menu.value.add
        addModal.value = !addModal.value;
      }
      if (menu.value.home) {
        menu.value.home = !menu.value.home
      }
      if (menu.value.parcelle) {
        menu.value.parcelle = !menu.value.parcelle
        parcelleModal.value = !parcelleModal.value
      }
      if (menu.value.cartographie) {
        menu.value.cartographie = !menu.value.cartographie
      }
    }

    let onOff = () => {
      menu.value.add = !menu.value.add
      addModal.value = !addModal.value;
      menu.value.home = true;
    }

    return{
      menu,
      onAdd,
      addModal,
      onHome,
      onParcelle,
      onCartographie,
      onParametre,
      onOff
    }
  }
})
</script>

<style scoped>
.modal {
  width: 100%;
  border-radius: 15px; 
  position: relative;
  bottom: -20%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>