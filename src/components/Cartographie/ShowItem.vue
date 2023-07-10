<template>
  <q-card class="projet" v-if="!planModal && !updateModal && !planEditModal" :style="drawerStyle()" style="border-radius: 15px; padding-bottom: 5px">
    <q-card-section class="q-pa-xs">
      <div clickable class="q-mt-xs q-mb-xs q-pa-sm q-pl-lg q-pr-lg text-center" v-touch-pan.mouse="slideDrawer" style="margin-left:70px; margin-right:70px; ">
        <q-separator size="5px" color="grey-8" inset   @click="onClose"/>
      </div>
      <div class="q-ma-md text-center" style="margin-top: -5px;">
        <span style="font-size: 25px;">Plans de cartographie</span>
      </div>
      <div class="q-gutter-xs">
        <q-card style="border-radius: 10px; background-color: #282828">
          <q-card-section class="text-white">
            <div class="text-h6">{{itemsPlans.libelle}}</div>
            <div class="text-subtitle2"> Phantom 4 </div>
            <div class="q-mt-xs">
                <img src="Hvol.png" style="height: 16px;"> <span class="q-ml-xs"> {{tempsTotal}}</span>
            </div>
            <div class="q-mt-xs">
                <img src="Distance.png" style="height: 16px;"> <span class="q-ml-xs" v-if="itemsPlans.acteur.acteurPoly">{{itemsPlans.acteur.acteurPoly[0].superficie.toFixed(2)}} hectare</span>
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

        <q-card-section style="height: 300px;" class="scroll q-pt-none" >
          <q-list v-if="itemsPlans.projetPlan.length > 0">
            <div class="row" >
              <!-- <item-projet  v-for="item in itemsPlans.projetPlan" :key="item" :item="item"/> -->
              <div class="q-pl-xs q-pr-xs q-pt-xs col-6" v-for="item in itemsPlans.projetPlan" :key="item" >
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
                      <div class="q-mt-xs q-mb-none">
                          <img src="Altitude.svg" style="height: 15px; margin-left:-5px" >
                          <span class="q-ml-xs text-weight-light">
                            {{item.altitude}} Mètre
                          </span>
                      </div>
                    </q-card-section>
                  </q-item>
                </q-card>
              </div>
            </div>
          </q-list>

          <div v-if="!itemsPlans.projetPlan.length" class="absolute-center" align="center">
            <div class="text-body1 text-weight-light">Aucun plan</div>
          </div>
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
                        <span style="position: relative; top:-2px" class="q-ml-xs">{{planSelect.temps}} Minute(s)</span>
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
                  @click="onEditPlan"
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
                  @click="onDeletePlan"
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
    <update-projet :projet="itemsPlans"/>
  </q-dialog>

  <!-- Modal delete projet -->
  <q-dialog v-model="deleteMadal" class="z-max" maximized transition-show="slide-up" transition-hide="slide-down" >
    <delete-projet :deleteItem="itemsPlans"/>
  </q-dialog>

  <!-- Modal create plan -->
  <q-dialog v-model="planModal"
    transition-show="slide-up"
    transition-hide="slide-down"
    full-width
    seamless
  >
    <create-plan :projetID="itemsPlans"/>
  </q-dialog>

  <!-- Modal update de plan -->
  <q-dialog v-model="planEditModal"
    transition-show="slide-up"
    transition-hide="slide-down"
    full-width
    seamless
  >
    <edit-plan :itemPlan="planSelect"/>
  </q-dialog>

  <!-- Modal delete de plan -->
  <q-dialog v-model="planDeleteModal"
    transition-show="slide-up"
    transition-hide="slide-down"
    class="z-max"
    maximized
    seamless
  >
    <delete-plan :deleteItemPlan="planSelect"/>
  </q-dialog>
</template>

<script>
import { useRouter } from "vue-router";
import { useQuasar } from 'quasar'
import $ from 'jquery'
import { defineComponent, getCurrentInstance, ref, onMounted, inject, provide } from "vue";
import ItemProjet from "components/Cartographie/Plan/Item.vue"
import UpdateProjet from "components/Cartographie/Update.vue"
import DeleteProjet from "components/Cartographie/Delete.vue"
import CreatePlan from "components/Cartographie/Plan/Create.vue"
import EditPlan from "components/Cartographie/Plan/Edit.vue"
import DeletePlan from "components/Cartographie/Plan/Delete.vue"
export default defineComponent({
  // name: 'ComponentName',

  components: {
    ItemProjet,
    UpdateProjet,
    DeleteProjet,
    CreatePlan,
    EditPlan,
    DeletePlan
  },

  props: {
    showItem: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup (props) {
    const instance = getCurrentInstance();
    const router = useRouter();
    let showModal = inject("showModal")
    let formData = ref({})
    let eForm = ref(null)
    const submitting = ref(false)
    let showDetail = ref(false)
    let planSelect = ref()
    let planModal = ref(false);
    let updateModal = ref(false)
    let deleteMadal = ref(false)
    let planEditModal = ref(false)
    let planDeleteModal = ref(false)
    provide("updateModal", updateModal)
    provide("deleteMadal", deleteMadal)
    provide("planModal", planModal)
    provide("planEditModal", planEditModal)
    provide("planDeleteModal", planDeleteModal)
    provide("showDetail", showDetail)
    let focusSearch = inject("focusSearch")

    let mymap = inject("mymap")
    let layerGroup = inject("layerGroup")

    let itemsPlans = ref([])
    provide("itemsPlans", itemsPlans)
    itemsPlans.value = props.showItem
    const service = "searchPlan";
    let state = inject(service);

    let ckecklist = ref({
      acces : false,
      drone : false,
      camera : false,
      controle : false,
      planDeVol : false,
      batterie : false,
      gps : false,
    })
    let tempsTotal = ref()

    let temps = ref(0)
    onMounted( async () => {
      for(const iterator of itemsPlans.value.projetPlan){
        temps.value = temps.value + iterator.temps
      }
      if (temps.value >= 60) {
        tempsTotal.value = Math.floor(temps.value / 60) + ' heure(s) ' + temps.value % 60 + ' minute(s)'
      }else {
        tempsTotal.value =  temps.value + ' minute(s)'
      }
    })

    let onClose = () => {
      showModal.value = false
    }

    let onUpdate = () => {
      updateModal.value = true
      focusSearch.value = true
    }

    let onDelete = () => {
      deleteMadal.value = true
    }

    let onPlan = () => {
      planModal.value = true
      focusSearch.value = true
    }

    let onDemarrer = (item) => {
      mymap.value.removeLayer(layerGroup.value);
      router.push({ name: 'mission', params: { itemPlan: JSON.stringify(item) } })
    }

    let polygon = ref()
    let onShow = (item) => {
      planSelect.value = item
      showDetail.value = true
      polygon.value = JSON.parse(planSelect.value.polygone)
    }

    let onBack = (item) => {
      showDetail.value = false
    }

    let onEditPlan = () => {
      planEditModal.value = true
      focusSearch.value = true
    }

    let onDeletePlan = () => {
      planDeleteModal.value = true
    }

    const $q = useQuasar()
    let top = 0
    let drawerPos = ref(0)
    const drawerStyle = () => {
      return {
        bottom: `${drawerPos.value}px`
      }
    }

    const slideDrawer = (ev) => {
      //console.log("ev", ev);
      const { height } = $q.screen
      /* if (top == 0) {
        top = drawerPos.value
      } */
      let taille = $(ev.evt.path[2]).height()
      drawerPos.value = drawerPos.value - ev.delta.y

      if (ev.isFinal === true) {
        if (drawerPos.value > top) {
          animateDrawerTo(top)
        }
        else if (drawerPos.value < -taille / 3) {
          drawerPos.value = top
          onClose()
        }
        else {
          animateDrawerTo(top)
        }
      }
    }

    const animateDrawerTo = (height) => {
      const diff = height - drawerPos.value

      if (diff !== 0) {
        drawerPos.value = top
        setTimeout(() => {
          animateDrawerTo(height)
        }, 3000)
      }
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
      itemsPlans,
      onEditPlan,
      onDeletePlan,
      planDeleteModal,
      tempsTotal,
      drawerStyle,
      slideDrawer,
    }
  }
})
</script>

<style scoped>
.projet {
  height: 640px;
  margin-left: 10px;
  margin-right: 10px;
  margin-bottom: 85px;
  position: relative;
  /* bottom: 85px; */
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

</style>
