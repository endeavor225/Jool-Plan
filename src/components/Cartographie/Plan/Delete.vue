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
        <div align="center"  class="text-weight-regular" style="font-size:20px">Vous êtes sur le point de supprimer un plan. Voulez-vous continuer ?</div>
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
import {defineComponent, getCurrentInstance, ref, inject, onMounted} from "vue"
import { useQuasar } from 'quasar'
export default defineComponent({
  // name: 'ComponentName',

  props: {
    deleteItemPlan: {
      type: Object,
      default() {
        return {};
      },
    },
  },


  setup (props) {
    console.log("deleteItemPlan", props.deleteItemPlan);
    const $q = useQuasar()
    let instance = getCurrentInstance()
    let formData = ref({})
    const service = "updatePlan";
    let state = inject(service);
    const serviceProjet = "searchProjet";
    let stateProjet = inject(serviceProjet);
    const servicePlan = "searchPlan";
    let statePlan = inject(servicePlan);
    const submitting = ref(false)
    let deleteMadal = inject("deleteMadal")
    let planDeleteModal = inject("planDeleteModal")
    let itemsProjet = inject("itemsProjet");
    let itemsPlans = inject("itemsPlans")
    let idProjet = ref()
    let showDetail = inject("showDetail")

    console.log("showDetail ==>", showDetail.value);

    onMounted(async () =>{
      
    })


    let onConfirme = async () => {
      state.filters.value = `
      mutation($id:UUID, $deleted:Boolean) {
        updatePlan(newPlan: 
          {id:$id
          deleted:$deleted}) 
          {
            ok
            plan{
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
      formData.value.id = props.deleteItemPlan.id
      formData.value.deleted = true
      state.item.value = formData.value;
      await getProjet()
      await state.remove()
      await refresh()
      await refreshProjet()
      deleteMadal.value = false
      $q.loading.hide()
      showDetail.value = false
      planDeleteModal.value = false
    }

    let getProjet = async () => {
      statePlan.filters.value = `
      query {
        searchPlan (deleted: false, id: "${props.deleteItemPlan.id}") {
          results(limit:1) {
            id
            libelle
            createdAt
            deleted
            projet{
              id
              libelle
            }	
          }
          totalCount
        }
      }
      `
      await statePlan.getItems()
      idProjet.value = statePlan.items.value[0].projet.id
    }

    let refresh = async () => {
      stateProjet.filters.value = `
      query {
        searchProjet (deleted: false, id: "${idProjet.value}") {
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
      await stateProjet.getItems()
      itemsPlans.value = stateProjet.items.value[0]
    }

    let refreshProjet = async () => {
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
      //itemsProjet.value = stateProjet.items.value
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
