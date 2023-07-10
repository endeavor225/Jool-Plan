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
        <div align="center"  class="text-weight-regular" style="font-size:20px">Vous êtes sur le point de supprimer une parcelle. Voulez-vous continuer ?</div>
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
    const service = "updateActeurs"
    let state = inject(service)
    const serviceActeurs = "searchActeurs"
    let stateActeurs = inject(serviceActeurs)
    const submitting = ref(false)
    let deleteMadal = inject("deleteMadal")
    let showModal = inject("showModal")
    let itemsActeur = inject("itemsActeur")
    const user = JSON.parse( localStorage.getItem('userAuth-session-app'))

    let onConfirme = async () => {

      state.filters.value = `
        mutation($id:UUID, $deleted:Boolean) {
          updateActeurs(newActeurs:
            {
              id:$id
              deleted:$deleted
            })
            {
              ok
              acteurs{
                id
              }
              errors {
                field
                messages
            }
          }
        }
      `
      //console.log(props.deleteItem.id);
      $q.loading.show()
      formData.value.id = props.deleteItem.id
      formData.value.deleted = true
      state.item.value = formData.value;
      let res = await state.remove()
      if(res){
        await refresh()
        $q.loading.hide()
        showModal.value = false
      }

    }

    let refresh = async () => {
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
          }
          totalCount
        }
      }
      `
      await stateActeurs.getItems()
      itemsActeur.value = stateActeurs.items.value
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
