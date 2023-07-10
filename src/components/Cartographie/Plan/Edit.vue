<template>
  <q-card class="modal">
    <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
      <q-separator size="5px" color="grey-8" inset @click="onClose" />
    </div>

    <div class="q-ma-lg text-h6 text-center">Modification du plan</div>

    <div class="q-ma-md" >
      <q-form
        @submit="onSubmit"
        class="q-gutter-xs"
        ref="eForm"
      >
        <q-input
          dense
          rounded
          standout="bg-grey-5 text-black"
          bg-color="grey-5"
          :input-style="{ color: 'black' }"
          v-model="libelle"
          label="Libéllé"
          :rules="[ (val) => !!val || 'Le champ libéllé est obligatoire']"
        />

        <q-input
          dense
          rounded
          standout="bg-grey-5 text-black"
          bg-color="grey-5"
          :input-style="{ color: 'black' }"
          v-model="altitude"
          label="Altitude de vol (en mèttre)"
          maxlength="3"
          :rules="[
            (val) => !!val || 'Le champ altitude est obligatoire',
            val => val > 49 && val < 101 || 'L\'altitude doit être compris entre 0 et 100 mètre'
          ]"
        />

        <div>
          <q-btn color="secondary" type="submit" rounded class="full-width" :loading="submitting">
            <span >Enregistrer</span>

            <template v-slot:loading>
              <q-spinner-facebook />
            </template>
          </q-btn>
        </div>
    </q-form>
    </div>
  </q-card>

</template>

<script>
import { ref, defineComponent, getCurrentInstance, inject, onMounted, provide } from "vue";
export default defineComponent({
  // name: 'ComponentName',

  props: {
    itemPlan: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup(props) {
    const instance = getCurrentInstance()
    let eForm = ref(null)
    let formData = ref({})
    let submitting = ref(false)
    const service = "updatePlan";
    let state = inject(service);
    const serviceProjets = "searchProjet";
    let stateProjet = inject(serviceProjets);
    let planEditModal = inject("planEditModal")
    let itemsProjet = inject("itemsProjet")
    let focusSearch = inject("focusSearch")

    let libelle = ref("")
    let altitude = ref("")

    onMounted(async () =>{
      //formData.value = props.itemPlan

      libelle.value = props.itemPlan.libelle
      altitude.value = props.itemPlan.altitude

    })

    let onSubmit = async () => {
      state.filters.value = `
      mutation($id:UUID, $libelle:String!,) {
        updatePlan(newPlan:
          {id:$id
          libelle:$libelle,
          })
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
      submitting.value = true
      formData.value = props.itemPlan
      formData.value.libelle = libelle
      formData.value.altitude = altitude
      state.item.value = formData.value;
      state.createForm.value = eForm.value;
      await state.update()

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
      // itemsProjet.value = stateProjet.items.value
      itemsProjet.value = JSON.parse(localStorage.getItem('searchProjet'))
      submitting.value = false
      planEditModal.value = false
      focusSearch.value = false
    }

    let onClose = () => {
      planEditModal.value = false
      focusSearch.value = false
    }

    return {
      formData,
      eForm,
      onSubmit,
      submitting,
      onClose,
      libelle,
      altitude
    };
  },
});
</script>

<style scoped>
.modal {
  width:100%;
  border-radius: 15px;
  position: relative;
  /* bottom: -25%; */
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>

