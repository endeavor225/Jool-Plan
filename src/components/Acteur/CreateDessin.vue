<template>
  <q-card class="modal" v-if="!selectModal">

    <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
      <q-separator size="5px" color="grey-8" inset @click="onClose" />
    </div>

    <div class="q-ma-lg text-h6 text-center">Nouvelle parcelle</div>

    <div class="q-ma-md" >
      <q-form
        @submit="onSubmit"
        class="q-gutter-xs"
        ref="eForm"
      >
        <q-input
          dense
          rounded
          standout="bg-grey-3 text-black"
          bg-color="grey-3"
          :input-style="{ color: 'black' }"
          v-model="formData.libelle"
          label="Libéllé"
          :rules="[ (val) => !!val || 'Le champ libéllé est obligatoire']"
        />


        <div @click="onDeclench">
          <q-select
            dense
            rounded
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.varieteCulture"
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

        <div style="margin-bottom: 35px; margin-top: 20px">
          <q-btn color="secondary" type="submit" rounded class="full-width" :loading="submitting">
            <span>Enregistrer</span>

            <template v-slot:loading>
              <q-spinner-facebook />
            </template>
          </q-btn>
        </div>
      </q-form>
    </div>
  </q-card>

  <q-dialog v-model="notifyModal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down">
    <q-card
      style="
        background-image: url(DroneLight.svg);
        background-repeat: no-repeat;
        background-position: left bottom;
      ">
      <div align="center" style="margin-top: 70px">
        <img
          alt="Jool Plan logo"
          src="JooL_Plan.svg"
        >
      </div>

      <q-card-section class="q-ma-md" style="margin-top: 20%">
        <div>
          <h4 align="center" class="text-weight-light">FÉLICITATION !</h4>
        </div>

        <div align="center"  class="text-weight-regular" style="font-size:20px">La parcelle « {{formData.libelle}} » a été délimitée avec succès</div>
      </q-card-section>
    </q-card>
  </q-dialog>


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
                <q-avatar icon="close" @click="sup(culture)" color="grey" text-color="white" size="xs" />
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
        style="height: 57vh; margin-top: -10px"
      >

        <div class="q-ma-none q-pa-none">
          <div
            v-for="culture in options"
            :key="culture"
            style="border: none; border-bottom: 1px solid #a5a5a5"
            class="item"
          >
            <q-checkbox
              size="md"
              v-model="cultures_selected"
              :val="culture"
              :label="culture.libelle"
            />
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
import { useQuasar } from 'quasar'
import { useRouter } from 'vue-router'
import { defineComponent, getCurrentInstance, ref, onMounted, inject, watch} from 'vue'

export default defineComponent({
  name: 'PageLogin',

  setup (){
    const $q = useQuasar()
    const route = useRouter()
    const instance = getCurrentInstance
    let formData = ref({})
    const service = "createActeurs";
    let state = inject(service);
    let eForm = ref(null)
    const submitting = ref(false)

    const serviceActeurs = "searchVarieteParCulture";
    let stateActeurs = inject(serviceActeurs);
    let saveMadal = inject("saveMadal")
    let notifyModal = ref(false)
    const hactare =  0.40468564224037
    let selectModal = ref(false)
    let cultures_selected = ref([])
    let search = ref()

    let carre = inject("carre")
    console.log("carre", carre.value);
    //console.log("carre.value.areaDisplay", carre.value.areaDisplay.split(" Acres").join(""));
    let coordinate = []
    let polygon = ref()
    let itemsVarieteCulture = ref([])
    itemsVarieteCulture.value = JSON.parse(localStorage.getItem('searchVarieteParCulture'))
    const user = JSON.parse( localStorage.getItem('userAuth-session-app'))


    for (const coord of carre.value.points){
      coordinate.push([coord.lng, coord.lat]);
    }

    console.log("coordinate", coordinate);

    polygon.value = {
      "type": "Feature",
      "properties": {},
      "geometry": {
        "type": "Polygon",
        "coordinates": [
          coordinate
        ]
      }
    }

    onMounted( async() => {

    })

    const options = ref(itemsVarieteCulture.value);

    watch(cultures_selected, ()=>{
      console.log("watch cultures_selected ", cultures_selected.value)
      cultures_selected.value = cultures_selected.value.reverse()
      formData.value.varieteCulture = cultures_selected.value
    })

    watch(search, async () => {
      const needle = search.value.toLowerCase()
      options.value = itemsVarieteCulture.value.filter((v) => v.libelle.toLowerCase().indexOf(needle) > -1)
    });

    let onSubmit = async () => {

      state.filters.value = `
        mutation($libelle:String!, $description:String, $enterprise: ID!, $pere:ID) {
          createActeurs(newActeurs : {libelle:$libelle, description:$description, enterprise:$enterprise, pere:$pere}) {
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
      formData.value.enterprise = user.enterprise.id
      formData.value.polygon = JSON.stringify(polygon.value)
      state.item.value = formData.value;
      state.createForm.value = eForm.value;
      let res = await state.create()
      submitting.value = false

      if(res) {
        notifyModal.value = true
        setTimeout(() => {
          notifyModal.value = false
          route.push('accueil')
        }, 2000)
      }
    }

    let onDeclench = () => {
      selectModal.value = true
      console.log("onDeclench");
    }

    let onCloseSelet = () => {
      console.log("onCloseSelet");
      selectModal.value = false
    }

    let sup = (culture) => {
      const i = cultures_selected.value.indexOf(culture);
      if (i > -1) {
        cultures_selected.value.splice(i, 1);
      }
    }

    let onClose = () => {
      saveMadal.value = false
    }

    return{
      formData,
      onSubmit,
      eForm,
      submitting,
      options,
      onClose,
      notifyModal,
      onDeclench,
      selectModal,
      cultures_selected,
      onCloseSelet,
      search,
      sup,



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
/*   bottom: -5%; */
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}

.select {
  height: 650px;
  width: 100%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>

