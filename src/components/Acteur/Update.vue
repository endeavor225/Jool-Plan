<template>

  <q-card class="modal">
    <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
      <q-separator size="5px" color="grey-8" inset @click="onClose" />
    </div>

    <div class="q-ma-lg text-h6 text-center">Modification de parcelle</div>

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

        <div @click="onDeclench">
          <q-select
            dense
            rounded
            standout="bg-grey-5 text-black"
            bg-color="grey-5"
            :input-style="{ color: 'black' }"
            v-model="cultures_selected"
            use-input
            input-debounce="0"
            label="Cultures"
            :options="options"
            option-label="libelle"
            option-value="id"
            @filter="filterFn"
            behavior="menu"
            multiple
            class="q-mb-lg"
            readonly
          >
            <template v-slot:no-option>
              <q-item>
                <q-item-section class="text-grey">
                  Aucun resultat
                </q-item-section>
              </q-item>
            </template>
          </q-select>
        </div>

        <div style="">
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

  <q-dialog v-model="selectModal" class="z-max" transition-show="slide-up" transition-hide="slide-down">
    <q-card class="select" style="border-radius: 15px;">

      <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
        <q-separator size="5px" color="grey-8" inset @click="onCloseSelet"/>
      </div>

      <div style="margin-top: -5px">
        <q-input
          v-model="search"
          dense
          rounded
          standout="bg-grey-1 text-black"
          :input-style="{ color: 'black' }"
          class="q-ma-lg"
          label="Recherche ...">
          <template v-slot:append>
            <q-avatar size="40px" style="right:-13px">
                <img src="Search.svg">
            </q-avatar>
          </template>
        </q-input>
      </div>

      <div class="q-my-sm" style="height: 35px; max-width: 100%; margin-top:-20px">
        <transition
          appear
          enter-active-class="animated fadeInLeft"
          leave-active-class="animated fadeOutLeft"
        >
          <q-scroll-area class="q-mb-none"
            v-if="cultures_selected.length > 0"
            :thumb-style="{
              borderRadius: '5px',
              backgroundColor: '#26A69A',
              height: '2px', opacity: 1
            }"

            :bar-style="{ right: '3px',
              borderRadius: '2px',
              background: '#26A69A',
              height: '5px',
              opacity: 0.2
            }"
            style="height: 35px; max-width: 100%;"
          >
            <div class="row no-wrap">
              <q-chip dense v-for="culture in cultures_selected" :key="culture.id">
                <q-avatar icon="close" @click="onSupp(culture)" color="grey" text-color="white" size="xs" />
                {{ culture.libelle }}
              </q-chip>
            </div>
          </q-scroll-area>
        </transition>
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
        style="height: 460px; margin-top: -10px"
      >

        <div class="q-ma-none q-pa-none">
          <div
            v-for="culture in options"
            :key="culture"
            style="border: none; border-bottom: 1px solid #a5a5a5"
            class="item"
          >
            <q-item dense tag="label" v-ripple class="q-pa-none q-ma-none">
              <q-checkbox
                size="md"
                v-model="cultures_selected"
                :val="culture"
                :label="culture.libelle"
              />
            </q-item>
          </div>
        </div>
      </q-scroll-area>

      <div align="center" class="q-mt-sm q-ml-lg q-mr-lg">
        <q-btn label="OK" padding="xs" rounded class="full-width" color="primary" @click="onCloseSelet"/>
      </div>
    </q-card>
  </q-dialog>
</template>

<script>
import {defineComponent, getCurrentInstance, onMounted, ref, inject, watch} from "vue"
export default defineComponent({
  // name: 'ComponentName',

  props: {
    editItem: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup (props) {
    let instance = getCurrentInstance()
    let eForm = ref(null)
    let formData = ref({})
    const service = "updateActeurs";
    const serviceActeurs = "searchActeurs";
    let state = inject(service);
    let stateActeurs = inject(serviceActeurs);
    const submitting = ref(false)
    let itemsActeur = inject("itemsActeur")
    let editModal = inject("editModal")
    let libelle = ref()
    let focusSearch = inject("focusSearch")
    let itemsVarieteCulture = ref(JSON.parse(localStorage.getItem('searchVarieteParCulture')))
    const user = JSON.parse( localStorage.getItem('userAuth-session-app'))
    let selectModal = ref(false)
    let cultures_selected = ref([])
    let search = ref()
    let showItemProjet = inject("showItemProjet")

    onMounted(() => {
      console.log("props.editItem",props.editItem);
      libelle.value = props.editItem.libelle

      for(const coord of props.editItem.acteurVarieteCulture){
        const found = options.value.find(varieteCulture => varieteCulture.id === coord.varietesCultures.id);
        /* console.log("found", found); */
        cultures_selected.value.push(found)
      }
    })

    const options = ref(itemsVarieteCulture.value);

    watch(cultures_selected, () => {
      cultures_selected.value = cultures_selected.value.reverse()
      console.log("cultures_selected",  cultures_selected.value);
    })

    watch(search, async () => {
      const needle = search.value.toLowerCase()
      options.value = itemsVarieteCulture.value.filter((v) => v.libelle.toLowerCase().indexOf(needle) > -1)
    });

    let onClose = () => {
      editModal.value = false
      focusSearch.value = false
    }

    let onDeclench = () => {
      selectModal.value = true
    }

    let onCloseSelet = () => {
      selectModal.value = false
    }

    let onSupp = (culture) => {
      const i = cultures_selected.value.indexOf(culture);
      if (i > -1) {
        cultures_selected.value.splice(i, 1);
      }
    }

    let onSubmit = async () => {

      state.filters.value = `
        mutation($id:UUID, $libelle:String!, $description:String) {
          updateActeurs(newActeurs:
            {id:$id
              libelle:$libelle,
              description:$description})
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

      submitting.value = true
      formData.value.id = props.editItem.id
      formData.value.libelle = libelle.value
      formData.value.newvarieteCulture = cultures_selected.value
      formData.value.acteurVarieteCulture = props.editItem.acteurVarieteCulture

      state.item.value = formData.value;
      state.updateForm.value = eForm.value;
      let res  = await state.update()

      if (res) {
        await refresh()
        submitting.value = false
        editModal.value = false
        focusSearch.value = false
      } else {
        submitting.value = false
      }
    }

    let refresh = async() => {
        stateActeurs.filters.value = `
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
                acteurProjet(deleted: false){
                  id
                  libelle
                  description
                  projetPlan{
                    id
                    libelle
                    altitude
                    polygone
                  }
                }
                acteurVarieteCulture(deleted: false){
                  id
                  varietesCultures{
                    id
                    libelle
                  }
                }
              }
              totalCount
            }
          }
        `
        await stateActeurs.getItems()
        /* itemsProjet.value = stateActeurs.items.value[0]
        console.log(stateActeurs.items.value[0]); */
        itemsActeur.value = JSON.parse(localStorage.getItem('searchActeurs'))

        const needle = props.editItem.id
        let item = itemsActeur.value.filter((v) => v.id.toLowerCase().indexOf(needle) > -1)
        console.log(item[0]);
        showItemProjet.value = item[0]
      }

    return {
      formData,
      eForm,
      onClose,
      submitting,
      onSubmit,
      onDeclench,
      libelle,
      cultures_selected,
      options,
      onCloseSelet,
      selectModal,
      search,
      onSupp,


      filterFn (val, update) {
        if (val === '') {
          update(() => {
            options.value = itemsVarieteCulture.value
          })
          return
        }

        update(() => {
          const needle = val.toLowerCase()
          options.value = itemsVarieteCulture.value.filter((v) => v.libelle.toLowerCase().indexOf(needle) > -1)
        })
      }
    }
  }
})
</script>

<style scoped>
.modal {
  width:100%;
  border-radius: 15px;
  position: relative;
 /*  bottom: -25%; */
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}

.select {
  height: 625px;
  width: 100%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>
