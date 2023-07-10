<template>
  <q-card class="modal">
    <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
      <q-separator size="5px" color="grey-8" inset @click="onClose" />
    </div>

    <div class="q-ma-lg text-h6 text-center">Nouveau plan</div>

    <div class="q-ml-lg text-weight-light" style="position: relative; bottom:15px; padding-top: 15px">
      {{$dateFormat(date)}}
    </div>

    <div class="q-mr-md" align="right">
      <q-item-section class="text-secondary">
        <q-item-label>
            <span class="q-mr-xs">
              <img
              src="plan/Batterie.svg"
              style="height: 15px;"
              >
              <span class="text-weight-bold text-grey-8 q-ml-xs" style="font-size: 12px; position:relative; top:-2px"> {{nbreBatterie.toFixed(0)}} </span>
              <span class="text-weight-light text-grey-8" style="font-size: 12px; position:relative; top:-2px"> Batterie(s) </span>
            </span>

            <span class="q-mr-xs">
              <img
                src="plan/NbreImg.svg"
                style="height: 15px; margin-left:2px"
              >
              <span class="text-weight-bold text-grey-8 q-ml-xs" style="font-size: 12px; position:relative; top:-2px"> {{ nbreImage}} </span>
              <span class="text-weight-light text-grey-8" style="font-size: 12px; position:relative; top:-2px"> image(s) </span>
            </span>

            <span class="q-mr-xs">
              <img
                src="plan/Duree.svg"
                style="height: 15px; margin-left:2px"
              >
              <span class="text-weight-bold text-grey-8" style="font-size: 12px; position:relative; top:-2px"> 1h00</span>
            </span>
        </q-item-label>
      </q-item-section>
    </div>

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
          v-model="formData.libelle"
          label="Nom du plan"
          :rules="[ (val) => !!val || 'Le champ libéllé est obligatoire']"
        >
          <template v-slot:prepend>
            <q-avatar square size="20px">
              <img src="plan/Plan.svg">
            </q-avatar>
          </template>
        </q-input>

        <q-input
          dense
          rounded

          standout="bg-grey-5 text-black"
          bg-color="grey-5"
          :input-style="{ color: 'black' }"
          v-model="formData.altitude"
          label="Altitude de vol (en mèttre)"
          maxlength="3"
          :rules="[
            (val) => !!val || 'Le champ altitude est obligatoire',
            val => val > 49 && val < 101 || 'L\'altitude doit être compris entre 0 et 100 mètre'
          ]"
        >
          <template v-slot:prepend>
            <q-avatar square size="20px">
              <img src="plan/Altitude.svg">
            </q-avatar>
          </template>
        </q-input>

        <q-card flat class="bg-grey-5 q-mb-lg" style="border-radius: 15px;">
          <q-card-section>
            <q-avatar square size="20px">
              <img src="plan/Select.svg">
            </q-avatar>
            <span class="q-pl-sm text-grey-9" style="position: relative; top:3px">Choix</span>

            <div class="column" style="position: relative; left:-10px">

              <!-- <q-toggle
                label="Parcelle projet"
                color="green"
                v-model="choixProjet"
                checked-icon="check"
                size="30px"
              />

              <q-toggle
                label="Fichier KML"
                color="green"
                v-model="choixKml"
                checked-icon="check"
                size="30px"
              />

              <q-toggle
                label="Dessiner sur la carte"
                color="green"
                v-model="choixDessiner"
                checked-icon="check"
                size="30px"
              /> -->


            </div>

            <div style="position: relative; left:-10px">
              <q-option-group
                :options="options"
                v-model="group"
                size="30px"
              />
            </div>
          </q-card-section>
        </q-card>

        <q-slide-transition>
          <q-file
            v-if="choixKml"
            dense
            rounded
            standout="bg-grey-5 text-black"
            bg-color="grey-5"
            :input-style="{ color: 'black' }"
            label="Fichier KML"
            v-model="file"
            use-chips
            :rules="[(val) => !!val || 'Ce champ est obligatoire']"
            style="margin-top: 20px"
          >
            <template v-slot:prepend>
              <q-avatar square size="20px">
                <img src="KML.svg">
              </q-avatar>
            </template>
          </q-file>
        </q-slide-transition>


          <!-- <q-card class="q-ma-none">
            <div id="map" style=" width: 100%; height: 50vh; "></div>
          </q-card> -->

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
import { useQuasar } from 'quasar'
import { Plugins } from '@capacitor/core';
import { ref, defineComponent, getCurrentInstance, inject, onMounted, watch} from "vue";
import { blobToBase64String } from "blob-util";
export default defineComponent({
  // name: 'ComponentName',



  props: {
    projetID: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup(props) {
    //console.log(props.projetID);
    const instance = getCurrentInstance()
    const $q = useQuasar()
    const service = "createPlan";
    let state = inject(service);
    const serviceProjets = "searchProjet";
    let stateProjet = inject(serviceProjets);
    const serviceBase64ToGeojson = "base64ToGeojson";
    let stateBase64ToGeojson = inject(serviceBase64ToGeojson);

    let itemsProjet = inject("itemsProjet");
    let planModal = inject("planModal")
    let itemsPlans = inject("itemsPlans")
    let focusSearch = inject("focusSearch")

    let eForm = ref(null)
    let formData = ref({})
    let polygone = ref({})
    let submitting = ref(false)
    let choixProjet = ref(false)
    let choixKml = ref(false)
    let choixDessiner = ref(false)
    let group = ref('choixProjet')
    let choix = ref(false)
    let file = ref(null)
    let base64String = ref()
    let nbreImage = ref(0)
    let nbreBatterie = ref(0)
    let date = ref()
    date.value = new Date();

    let mapModal = ref(false)
    let visibility = ref("display:none;")

    let isValidNumber = (val)=>{
      return val > 0 || `Merci de saisir au format correct`
    }


    onMounted(async () =>{

      polygone.value = props.projetID.acteur.acteurPoly[0]
      console.log(polygone.value);
      // formData.value.polygone = `"${props.projetID.acteur.acteurPoly[0].geometrie}"`
      nbreImage.value = props.projetID.acteur.acteurPoly[0].superficie.toFixed(2)*100
      nbreBatterie.value = props.projetID.acteur.acteurPoly[0].superficie.toFixed(0)*0.5
    })

    watch(group, async () => {

      switch (group.value) {

        case "choixProjet":
         // group.value = true
          polygone.value = props.projetID.acteur.acteurPoly[0]
          nbreImage.value = props.projetID.acteur.acteurPoly[0].superficie.toFixed(2)*100
          nbreBatterie.value = props.projetID.acteur.acteurPoly[0].superficie.toFixed(0)*0.5
        //formData.value.polygon = JSON.stringify(props.projetID.acteur.acteurPoly[0].geometrie)
         choixKml.value = false
        break;

        case "choixKml":
          file.value = null
         // group.value = true
          choixKml.value = true
          //formData.value.polygon = "choisir Kml"

          nbreImage.value = 0
          nbreBatterie.value = 0

          watch(file, async() => {
            if (file.value) {
              $q.loading.show()
              base64String.value = await blobToBase64String(file.value)
              //console.log( base64String.value);

              stateBase64ToGeojson.filters.value = `
                query {
                  base64ToGeojson (base64: "${base64String.value}" ) {
                    geometrie
                    centroid
                    superficie
                  }
                }
                `
              await stateBase64ToGeojson.getItems();
              polygone.value = stateBase64ToGeojson.itemObjet.value.data.base64ToGeojson
              console.log(polygone.value);

              /* let geo = JSON.parse(polygone.value.geometrie)
              console.log("geometrie", geo.coordinates);

              let coord = JSON.stringify(geo.coordinates)
              let first = coord.substring(1)
              let last = first.substring(0, first.length - 1)
              coord = last

              console.log("coord",coord); */

              nbreImage.value = stateBase64ToGeojson.itemObjet.value.data.base64ToGeojson.superficie.toFixed(2)*100
              nbreBatterie.value = stateBase64ToGeojson.itemObjet.value.data.base64ToGeojson.superficie.toFixed(0)*0.5
              $q.loading.hide()
            }
            else {
              nbreImage.value = 0
              nbreBatterie.value = 0
            }


          })
          break;

        case "choixDessiner":
          formData.value.polygon = "choixDessiner"
          choixKml.value = false
          //mapModal.value = true

          //visibility.value ="display:initial"

          setTimeout(() => {
            mapModal.value = false
          }, 2000)
         // group.value = true
          break;
      }
    })


    let onSubmit = async () => {

      state.filters.value = `
        mutation($altitude:Int!, $libelle:String!, $projet:ID, $polygone:String!) {
          createPlan(newPlan: {altitude:$altitude, libelle:$libelle, projet:$projet, polygone:$polygone}) {
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
      if (polygone.value) {
        submitting.value = true
        formData.value.projet = props.projetID.id
        formData.value.polygone = JSON.stringify(polygone.value)
        state.item.value = formData.value;
        state.createForm.value = eForm.value;
        let res = await state.create()

        if (res) {
          await refresh()
          await refreshProjet()
          submitting.value = false
          planModal.value = false
          focusSearch.value = false
        } else {
          submitting.value = false
        }
      }
    }

    let refresh = async () => {
      stateProjet.filters.value = `
      query {
        searchProjet (deleted: false,  id: "${props.projetID.id}") {
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
      console.log("stateProjet.items.value ==>", stateProjet.items.value[0]);
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
      // console.log("stateProjet.items.value ==>", stateProjet.items.value);
      itemsProjet.value = JSON.parse(localStorage.getItem('searchProjet'))
    }

    let onClose = () => {
      planModal.value = false
      focusSearch.value = false
    }

    return {
      formData,
      eForm,
      onSubmit,
      submitting,
      onClose,
      choixProjet,
      choixKml,
      choixDessiner,
      group,
      choix,
      file,
      mapModal,
      visibility,
      isValidNumber,
      nbreImage,
      nbreBatterie,
      date,


      options: [
        { label: 'Parcelle projet', value: 'choixProjet',  color: 'green' },
        { label: 'Fichier KML', value: 'choixKml', color: 'green' },
        // { label: 'Dessiner sur la carte', value: 'choixDessiner',  color: 'green' }
      ]
    };
  },
});
</script>

<style scoped>
.modal {
  width:100%;
  border-radius: 15px;
  position: relative;
  /* bottom: -10%; */
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>

