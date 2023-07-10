<template>
  <q-card class="acteur1" v-if="!planModal && !updateModal">
    <q-card-section class="q-pa-xs">
      <div clickable class="q-ma-md text-center" style="margin-left:70px; margin-right:70px; margin-top: 10px;">
        <q-separator size="5px" color="grey-8" inset   @click="onClose"/>
      </div>
      <div class="q-ma-md text-center" style="margin-top: -5px;">
        <span style="font-size: 25px;">Cartographie</span>
      </div>
      <div class="q-gutter-xs">
        <q-card style="border-radius: 10px; background-color: #282828">
          <q-card-section class="text-white">
            <div class="text-h6">{{showItem.libelle}}</div>
            <div class="text-subtitle2"> Phantom 4 </div>
            <div class="q-mt-xs">
                <img src="Hvol.png" style="height: 16px;"> <span class="q-ml-xs"> 12 heures</span>
            </div>
            <div class="q-mt-xs">
                <img src="Distance.png" style="height: 16px;"> <span class="q-ml-xs" v-if="showItem.acteur.acteurPoly">{{showItem.acteur.acteurPoly[0].superficie.toFixed(2)}} ha</span>
            </div>
            <div class="absolute-right">
                <img src="Drone.png" style="height: 138px;">
            </div>
          </q-card-section>
        </q-card>
      </div>
    </q-card-section>

    <transition
      appear
      enter-active-class="animated fadeInUp"
    >

      <div v-if="!showDetail">
        <q-card-section style="margin-top: -5px">
          <q-btn 
            @click="onPlan"
            color="secondary" 
            icon="add" 
            type="submit" 
            label="Nouveau plan" 
            class="full-width" 
            no-caps 
            style="border-radius: 5px"
            padding="xs"
            size="lg"
          />
        </q-card-section>


        <q-card-actions class="q-ml-sm q-mr-sm" align="around" style="margin-top: -10px">
          <q-btn 
            @click="onUpdate"
            color="secondary" 
            type="submit" 
            no-caps 
            style="border-radius: 5px; width:45%"
          > 
            <q-avatar square size="20px">
              <img src="Edit.svg">
            </q-avatar>
            <span style="font-size: 10px; margin-left: 10px"> Modifier </span>
          </q-btn>

          <q-btn 
            @click="onDelete"
            color="secondary" 
            type="submit" 
            no-caps 
            style="border-radius: 5px; width:45%"
          > 
            <q-avatar square size="20px">
              <img src="Delete.svg">
            </q-avatar>
            <span style="font-size: 10px; margin-left: 10px"> Supprimer </span>
          </q-btn>
        </q-card-actions>

        <q-card-section style="height: 37vh;" class="scroll q-pt-none" >
          <q-list v-if="showItem.projetPlan.length > 0">
            <div class="row">
              <!-- <item-projet  v-for="item in showItem.projetPlan" :key="item" :item="item"/> -->
              <div class="q-pl-xs q-pr-xs q-pt-xs col-6" v-for="item in showItem.projetPlan" :key="item" >
                <q-card flat class="my-card" style="border-radius: 10px;">
                  <q-item clickable v-ripple class="q-pa-none q-ma-none" @click="onShow(item)">
                    <q-card-section class="text-black q-pa-md" style="width: 100%; padding-top: 10px; padding-bottom: 10px">
                      <div class="q-mt-none items-center">
                        <img src="Plan.png" style="height: 20px; margin-left:-10px">
                        <span class="q-ml-xs" style="position: relative; top:-5px">
                          {{item.libelle}}
                        </span>

                        <!-- <q-item-label class="q-mb-sm" lines="1" style="font-size:15px">{{item.libelle}}</q-item-label> -->
                      </div>
                      <div class="text-weight-light q-mt-xs" style="font-size:12px">
                        {{$FormatDate(item.createdAt)}}
                      </div>
                      <div class="q-mt-xs">
                          <img src="Duree.png" style="height: 13px;">
                          <span class="q-ml-xs text-weight-light">
                             {{item.temps}} Minute(s)
                          </span>
                      </div>
                      <div class="q-mt-xs q-mb-none">
                          <img src="NbreImgs.png" style="height: 12px;">
                          <span class="q-ml-xs text-weight-light">
                            {{item.nbrImages}} Images
                          </span>
                      </div>
                    </q-card-section>
                  </q-item>
                </q-card>
              </div>
            </div>
          </q-list>
        </q-card-section>
      </div>
    </transition >

    <transition
      appear
      enter-active-class="animated fadeInUp"
      leave-active-class="animated fadeOutDown"
    >
      <div v-if="showDetail" >

        <q-card flat class="q-ma-md card-show" 
          style="background-image: url('bg-plan.jpg'); background-repeat: no-repeat; border-radius: 15px;"
        >
          <div class="card-show2">

            <q-btn 
              @click="onBack"
              color="secondary" 
              icon="arrow_back_ios_new"   
              round
              style="position: absolute; left: -12px; top: -2px;"
            />

            <p class="q-pa-none q-ml-lg q-mr-lg text-h6 text-center">{{planSelect.libelle}}</p>

            <div class="card-info q-ma-xs" style="border-radius: 10px; margin-top:-7px;">
              <div class="q-pa-md">
                <p class="text-weight-light">{{$dateFormat(planSelect.createdAt)}}</p>
          
                <div class="row">
                  <div class="col q-mb-md">
                    <q-item-section class="text-secondary">
                      <q-item-label>
                        <img src="Duree.svg" style="height: 16px; margin-left:2px">
                        <span style="position: relative; top:-2px" class="q-ml-xs">{{planSelect.temps}} m</span>
                      </q-item-label>
                      <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:23px">Durée de vol</q-item-label>
                    </q-item-section>
                  </div>
                  <div class="col  q-mb-md">
                    <q-item-section class="text-secondary">
                      <q-item-label>
                        <img src="NbreImgs.svg" style="height: 16px; margin-left:2px">
                        <span style="position: relative; top:-2px" class="q-ml-xs">{{planSelect.nbrImages}} images </span> 
                      </q-item-label>
                      <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:25px">Nombre d'image</q-item-label>
                    </q-item-section>
                  </div>
                </div>
                <div class="row">
                  <div class="col q-mb-md">
                    <q-item-section class="text-secondary">
                      <q-item-label>
                        <img src="Distance.svg" style="height: 16px; margin-left:2px"> 
                        <span style="position: relative; top:-2px" class="q-ml-xs"> {{ polygon.superficie.toFixed(2)}} ha </span>
                      </q-item-label>
                      <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:23px">Distance parcourue</q-item-label>
                    </q-item-section>
                  </div>
                  <div class="col q-mb-md">
                    <q-item-section class="text-secondary">
                      <q-item-label>
                          <img src="Batterie.svg" style="height: 16px; margin-left:2px"> 
                          <span style="position: relative; top:-2px" class="q-ml-xs">{{planSelect.nbrBatteries}} 
                            <span class="text-weight-light" style="font-size: 11px;">Batterie</span>
                          </span> 
                        </q-item-label>
                      <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:15px">Nombre de Batterie</q-item-label>
                    </q-item-section>
                  </div>
                </div>
                <div class="row">
                  <div class="col q-mb-md">
                    <q-item-section class="text-secondary">
                      <q-item-label>
                        <img src="Altitude.svg" style="height: 16px; margin-left:2px"> 
                        <span style="position: relative; top:-2px" class="q-ml-xs"> {{ planSelect.altitude}} m </span>
                      </q-item-label>
                      <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:25px">Altitude de vol</q-item-label>
                    </q-item-section>
                  </div>
                  <div class="col q-mb-md">
                    <q-item-section class="text-secondary">
                      <q-item-label v-if="planSelect.status === null">
                        <img src="Altitude.svg" style="height: 16px; margin-left:2px"> 
                        <span style="position: relative; top:-2px" class="q-ml-xs">En attente </span>
                      </q-item-label>
                      <q-item-label v-if="planSelect.status">
                        <img src="Altitude.svg" style="height: 16px; margin-left:2px"> 
                        <span style="position: relative; top:-2px" class="q-ml-xs">Terminée </span>
                      </q-item-label>
                      <q-item-label caption class="text-secondary text-weight-light" style="font-size: 9px; margin-top:0px; margin-left:25px">Statut</q-item-label>
                    </q-item-section>
                  </div>
                </div>    
              </div>
            </div>

            <div style="margin-top: 0px">
              <q-card-section >
                <q-btn 
                  @click="onDemarrer(planSelect)"
                  color="secondary" 
                  icon="near_me" 
                  type="submit" 
                  class="full-width" 
                  no-caps 
                  style="border-radius: 5px"
                  
                >
                  <span style="font-size: 15px;">Démarrer la mission</span>
                </q-btn>
              </q-card-section>

              <q-card-actions class="" align="around" style="margin-top: -10px">
                <q-btn 
                  color="secondary" 
                  type="submit" 
                  no-caps 
                  style="border-radius: 5px; width:45%"
                  padding="xs"
                > 
                  <q-avatar square size="16px">
                    <img src="Edit.svg">
                  </q-avatar>
                  <span style="font-size: 10px; margin-left: 10px"> Modifier </span>
                </q-btn>

                <q-btn 
                  color="secondary" 
                  type="submit" 
                  no-caps 
                  style="border-radius: 5px; width:45%"
                  padding="xs"
                > 
                  <q-avatar square size="16px">
                    <img src="Delete.svg">
                  </q-avatar>
                  <span style="font-size: 10px; margin-left: 10px"> Supprimer </span>
                </q-btn>
              </q-card-actions>
            </div>
          </div>
        </q-card>
      </div>
    </transition>
  </q-card>

  <!-- Modal update projet -->
  <q-dialog v-model="updateModal"
    transition-show="slide-up" 
    transition-hide="slide-down"
    full-width
    seamless
  >
    <update-projet :projet="showItem"/>
  </q-dialog>

  <!-- Modal delete projet -->
  <q-dialog v-model="deleteMadal" class="z-max" maximized transition-show="slide-up" transition-hide="slide-down" >
    <delete-projet :deleteItem="showItem"/>
  </q-dialog>

  <!-- Modal create plan -->
  <q-dialog v-model="planModal"
    transition-show="slide-up" 
    transition-hide="slide-down"
    full-width
    seamless
  >
    <!-- <create-plan :projetID="showItem"/> -->
    <q-card class="modal">
    <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
      <q-separator size="5px" color="grey-8" inset @click="onClosePlan"  />
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
              <!-- <span class="text-weight-bold text-grey-8 q-ml-xs" style="font-size: 12px; position:relative; top:-2px"> {{nbreBatterie.toFixed(0)}} </span>  -->
              <span class="text-weight-light text-grey-8" style="font-size: 12px; position:relative; top:-2px"> Batterie(s) </span> 
            </span>

            <span class="q-mr-xs">
              <img 
                src="plan/NbreImg.svg" 
                style="height: 15px; margin-left:2px"
              > 
              <!-- <span class="text-weight-bold text-grey-8 q-ml-xs" style="font-size: 12px; position:relative; top:-2px"> {{ nbreImage}} </span>  -->
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

    <!-- <div class="q-ma-md" >
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

        <div>
          <q-btn color="secondary" type="submit" rounded class="full-width" :loading="submitting">
            <span >Enregistrer</span> 

            <template v-slot:loading>
              <q-spinner-facebook />
            </template>
          </q-btn>
        </div>
    </q-form>
    </div> -->
  </q-card>

  </q-dialog>

  <!-- Modal update de plan -->
  <q-dialog v-model="planEditModal"
    transition-show="slide-up" 
    transition-hide="slide-down"
    full-width
    seamless
  >
    <create-plan :projetID="showItem"/>
  </q-dialog>
</template>

<script>
import { useRouter } from "vue-router";
import { defineComponent, getCurrentInstance, ref, onMounted, inject, provide, onBeforeMount } from "vue";
import ItemProjet from "components/Cartographie/Plan/Item.vue"
import UpdateProjet from "components/Cartographie/Update.vue"
import DeleteProjet from "components/Cartographie/Delete.vue"
import CreatePlan from "components/Cartographie/Plan/Create.vue"
import EditPlan from "components/Cartographie/Plan/Edit.vue"
export default defineComponent({
  // name: 'ComponentName',

  components: {
    ItemProjet,
    UpdateProjet,
    DeleteProjet,
    CreatePlan,
    EditPlan
  },

  props: {
    showItem: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup () {
    const instance = getCurrentInstance();
    const router = useRouter();
    let showModal = inject("showModal")
    let formData = ref({})
    let eForm = ref(null)
    const submitting = ref(false)
    let showDetail = ref(false)
    let planSelect = ref()
    let planEditModal = ref(false)
    let planModal = ref(false);
    let updateModal = ref(false)
    let deleteMadal = ref(false)
    provide("planModal", planModal)
    provide("updateModal", updateModal)
    provide("deleteMadal", deleteMadal)

    let ckecklist = ref({
      acces : false,
      drone : false,
      camera : false,
      controle : false,
      planDeVol : false,
      batterie : false,
      gps : false,
    })

    // let eForm = ref(null)
    // let formData = ref({})
    let polygone = ref({})
    // let submitting = ref(false)
    let notifyModal = ref(false)
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

    
    
    let onClose = () => {
      showModal.value = false
    }

    let onClosePlan = () => {
      planModal.value = false
    }

    let onUpdate = () => {
      updateModal.value = true
    }

    let onDelete = () => {
      deleteMadal.value = true
    }

    let onPlan = () => {
      planModal.value = true
    }

    let onDemarrer = (item) => {
      router.push({ name: 'plan', params: { itemPlan: JSON.stringify(item) } })
    }


    let polygon = ref()
    let onShow = (item) => {
      planSelect.value = item
      showDetail.value = true
      polygon.value = JSON.parse(planSelect.value.polygone)
      console.log(planSelect.value);
    }

    let onBack = (item) => {
      showDetail.value = false
    }
    return {
      onClose,
      planModal,
      formData,
      eForm,
      submitting,
      onPlan,
      onShow,
      showDetail,
      onBack,
      planSelect,
      updateModal,
      planEditModal,
      onUpdate,
      deleteMadal,
      onDelete,
      polygon,
      onDemarrer,
      ckecklist,
      onClosePlan,
      date,
    }
  }
})
</script>

<style scoped>
.acteur1 {
  /* height: 80vh; */
  height: 650px;
  border-radius: 15px; 
  /* position: relative;
  bottom: 0%; */
  /* background: rgba(255, 255, 255, 0.8); */
  backdrop-filter: blur(3px)
}

.my-card {
  width: 100%;
  background-color: rgba(177, 177, 177, 0.1)
}

.card-show {
  /* height: 46vh; */
  height: 370px;
  backdrop-filter: blur(3px)
}

.card-show2 {
  height: 370px;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px)
}

.card-info {
  background-color: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(3px)
}

.modal {
  width:100%;
  border-radius: 15px; 
  position: relative;
  /* bottom: -10%; */
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>
