<template>
  <q-card 
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
        <h4 align="center" class="text-weight-light" style="margin-bottom:35px">SUPPRESSION</h4>
      </div>

      <q-card-section class="q-ma-md" style="margin-top: 20%">
        <div align="center"  class="text-weight-regular" style="font-size:20px">Vous êtes sur le point de supprimer un projet. Voulez-vous continuer ?</div>
      </q-card-section>

      <q-card-actions align="around" style="margin-top: 10%">
        <q-btn rounded color="secondary" v-close-popup >
          <span style="font-size: 20px">Non</span> 
        </q-btn>
        <q-btn rounded text-color="black" color="grey-2" @click="onConfirme">
          <span style="font-size: 20px">Oui</span> 
        </q-btn>
      </q-card-actions>
  
    </q-card>
</template>

<script>
import {defineComponent, getCurrentInstance, ref, inject} from "vue"
import { useQuasar } from 'quasar'
export default defineComponent({
  // name: 'ComponentName',

  props: {
    deleteItem: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup (props) {
    const $q = useQuasar()
    let instance = getCurrentInstance()
    let formData = ref({})
    const service = "updateProjet";
    const serviceProjet = "searchProjet";
    let stateProjet = inject(serviceProjet);
    let state = inject(service);
    const submitting = ref(false)
    let deleteMadal = inject("deleteMadal")
    let showModal = inject("showModal")
    let itemsProjet =  inject("itemsProjet");

    let onConfirme = async () => {
      console.log("ddd");
      state.filters.value = `
      mutation($id:UUID, $deleted:Boolean) {
        updateProjet(newProjet: 
          {id:$id
          deleted:$deleted}) 
          {
            ok
            projet{
              id
            }
            errors {
              field
              messages
          }
        }
      }   
      `
      $q.loading.show()
      formData.value.id = props.deleteItem.id
      formData.value.deleted = true
      state.item.value = formData.value;
      await state.remove()
      await refresh()
      deleteMadal.value = false
      $q.loading.hide()
      showModal.value = false
    }

    let refresh = async () => {
      state.filters.value = `
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
      await stateProjet.getItems()
      // itemsProjet.value = stateProjet.items.value
      itemsProjet.value = JSON.parse(localStorage.getItem('searchProjet'))
    }

    return {
      onConfirme,
      submitting
    }
  }
})
</script>

<style scoped>
.modal {
  width:100%;
  border-radius: 15px; 
  position: relative;
  bottom: -5%;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}
</style>
