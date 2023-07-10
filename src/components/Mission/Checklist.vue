<template>
  <q-card class=""
    style="background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(3px)"
  >
    <q-card
      style="margin-left: 10%;
        margin-right: 10%;
        border-radius: 10px;
        background: rgba(255, 255, 255, 0.9);
        backdrop-filter: blur(3px)"
    >
      <q-card-section class="q-ma-none">
        <div align="center" class="q-mb-sm">
          Vérification avant vol
        </div>

        <!-- Batterie phone -->
        <div class="q-ma-sm" v-if="checkListe.phoneBattery === null">
          <q-icon name="radio_button_unchecked" size="25px" />
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Batterie Téléphone</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.phoneBattery === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Batterie Téléphone</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.phoneBattery === false">
            <q-icon name="highlight_off" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Batterie Téléphone</span>
          </div>
        </transition>

        <!-- ACCES -->
        <div class="q-ma-sm" v-if="checkListe.acces === null">
          <q-icon name="radio_button_unchecked" size="25px" />
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Accès</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.acces === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Accès</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.acces === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Accès</span>
          </div>
        </transition>

        <!-- DRONE -->
        <div class="q-ma-sm" v-if="checkListe.drone === null">
          <q-icon name="radio_button_unchecked" size="25px" />
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Drone</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.drone === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Drone</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.drone === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Drone</span>
          </div>
        </transition>

        <!-- CONTROLE -->
        <div class="q-ma-sm" v-if="checkListe.controle === null">
          <q-icon name="radio_button_unchecked" size="25px" />
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Contrôle</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.controle === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Contrôle</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.controle === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Contrôle</span>
          </div>
        </transition>

        <!-- CARD SD -->
        <div class="q-ma-sm" v-if="checkListe.sdcard === null">
          <q-icon name="radio_button_unchecked" size="25px"/>
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Carte mémoire</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.sdcard === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Carte mémoire</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.sdcard === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Carte mémoire</span>
          </div>
        </transition>

        <!-- DIAGNOSTIC -->
        <div class="q-ma-sm" v-if="checkListe.diagnostic === null">
          <q-icon name="radio_button_unchecked" size="25px"/>
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Diagnostique</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.diagnostic === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Diagnostique</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.diagnostic === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Diagnostique</span>
          </div>
        </transition>

        <!-- CAMERA -->
        <div class="q-ma-sm" v-if="checkListe.camera === null">
          <q-icon name="radio_button_unchecked" size="25px"/>
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Caméra</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.camera === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Caméra</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.camera === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Caméra</span>
          </div>
        </transition>

        <!-- PLAN DE VOL -->
        <div class="q-ma-sm" v-if="checkListe.planDeVol === null">
          <q-icon name="radio_button_unchecked" size="25px" />
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Plan de vol</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.planDeVol === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Plan de vol</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.planDeVol === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Plan de vol</span>
          </div>
        </transition>

        <!-- Batterie drone -->
        <div class="q-ma-sm" v-if="checkListe.droneBattery === null">
          <q-icon name="radio_button_unchecked" size="25px" />
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">Batterie Drone</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.droneBattery === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">Batterie Drone</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.droneBattery === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">Batterie Drone</span>
          </div>
        </transition>

        <!-- GPS -->
        <div class="q-ma-sm" v-if="checkListe.gps === null">
          <q-icon name="radio_button_unchecked" size="25px" />
          <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-grey-7 q-ml-xs">GPS</span>
        </div>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.gps === true">
            <q-icon name="check_circle_outline" size="25px" class="text-secondary"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-secondary q-ml-xs">GPS</span>
          </div>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInLeft"
        >
          <div class="q-ma-sm" v-if="checkListe.gps === false">
            <q-icon name="highlight_off" size="25px" class="text-negative"/>
            <span style="position: relative; top:2px; font-size:13px" class="text-weight-light text-negative q-ml-xs">GPS</span>
          </div>
        </transition>

        <div clickable class="q-mt-md text-center" style="margin-left:50px; margin-right:50px">
          <q-separator size="5px" color="grey-8" inset v-if="submitting"/>
          <q-separator size="5px" color="grey-8" inset @click="hideControl" v-else/>
        </div>
      </q-card-section>
    </q-card>
  </q-card>
</template>

<script>
import { ref, defineComponent, inject} from "vue";
export default defineComponent({
  // name: 'ComponentName',
  setup () {
    let checkListe = inject("checkListe")
    let checkListModal = inject("checkListModal")
    const submitting = inject("submitting")

    let hideControl = async () => {
      checkListModal.value = false
    }
    return {
      checkListe,
      hideControl,
      checkListModal,
      submitting
    }
  }
})
</script>
