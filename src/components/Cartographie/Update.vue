<template>
  <q-card class="modal">
    <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
      <q-separator size="5px" color="grey-8" inset @click="onClose" />
    </div>

    <div class="q-ma-lg text-h6 text-center">Modification de projet</div>

    <div class="q-ma-md" >
      <q-form
        @submit.prevent="onSubmit"
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
          type="textarea"
          standout="bg-grey-5 text-black"
          bg-color="grey-5"
          :input-style="{ color: 'black' }"
          v-model="description"
          label="Description"
        />

        <div>
          <q-btn color="secondary" type="submit" rounded class="full-width" :loading="submitting">
            <span>Sauvegarger</span>

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
import { ref, defineComponent, getCurrentInstance, inject, onMounted } from "vue";
export default defineComponent({
  // name: 'ComponentName',

  props: {
    projet: {
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
    const service = "updateProjet";
    let state = inject(service);
    let submitting = ref(false)
    let notifyModal = ref(false)
    const serviceProjets = "searchProjet";
    let stateProjet = inject(serviceProjets);
    let updateModal = inject("updateModal")
    let itemsProjet =  inject("itemsProjet");
    let focusSearch = inject("focusSearch")

    let libelle = ref("")
    let description = ref("")

    onMounted( async () => {
      //formData.value = props.projet
      libelle.value = props.projet.libelle
      description.value = props.projet.description
    })

    let onSubmit = async () => {
      console.log("fff");
      state.filters.value = `
      mutation($id:UUID, $libelle:String!, $description:String) {
        updateProjet(newProjet:
          {id:$id
          libelle:$libelle,
          description:$description})
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
      submitting.value = true
      formData.value = props.projet
      formData.value.libelle = libelle
      formData.value.description = description
      state.item.value = formData.value
      state.updateForm.value = eForm.value;
      let res = await state.update()
      if (res) {
        await refresh()
        submitting.value = false
        updateModal.value = false
        focusSearch.value = false
      } else {
        submitting.value = false
      }
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
      //itemsProjet.value = stateProjet.items.value
      itemsProjet.value = JSON.parse(localStorage.getItem('searchProjet'))
    }

    let onClose = () => {
      updateModal.value = false
      focusSearch.value = false
    }

    return {
      formData,
      eForm,
      onSubmit,
      submitting,
      onClose,
      notifyModal,
      libelle,
      description,
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

