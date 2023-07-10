<template>
  <transition
    appear
    enter-active-class="animated fadeInRight"
    leave-active-class="animated fadeOutRight"
  >
    <q-page>
      <div id="mapid" style=" width: 100%; height: 110vh;"> </div>

      <div class="fixed-top-right z-max q-mr-sm" style="margin-top:40px" >
        <transition
          appear
          enter-active-class="animated fadeInRight"
          leave-active-class="animated fadeOutRight"
        >
          <q-card v-if="!checkListModal">
            <q-btn
              class="no-padding"
              unelevated
              @click="onZoomIn"
            >
              <q-avatar size="2.5rem">
                <img src="Plus.svg">
              </q-avatar>
            </q-btn>
            <br>

            <q-btn
              class="no-padding"
              unelevated
              @click="onZoomOut"
            >
              <q-avatar size="2.5rem">
                <img src="Moins.svg">
              </q-avatar>
            </q-btn>
          </q-card>
        </transition>

        <transition
          appear
          enter-active-class="animated fadeInRight"
          leave-active-class="animated fadeOutRight"
        >
          <q-card v-if="!checkListModal" style="top: 15px">
            <q-btn
              class="no-padding"
              unelevated
              @click="onGetMeteo"
            >
              <q-avatar size="2.5rem">
                <img src="Meteo.svg">
              </q-avatar>
            </q-btn>
            <br>
          </q-card>
        </transition>
      </div>

      <div class="fixed-top-left z-max q-ml-sm" style="margin-top:60px">
        <div class="row">

          <transition
            appear
            enter-active-class="animated fadeInLeft"
            leave-active-class="animated fadeOutLeft"
          >
            <q-card flat style="border-radius: 10px; height:310px" v-if="hideIcon && !checkListModal">

              <q-item-section class="text-white bg-dark" align="center">
                <q-item-label class="q-pa-sm" style="font-size:12px"> {{date}} </q-item-label>
              </q-item-section>

              <q-item-section class="text-secondary q-ma-sm">
                <q-item-label>
                  <img src="plan/Batterie.svg" style="height: 16px;" class="q-ml-xs">
                  <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{infoDrone.chargeRemainingInPercent}}% </span>
                </q-item-label>
              </q-item-section>
              <q-separator spaced />

              <q-item-section class="text-secondary q-ma-sm">
                <q-item-label>
                  <q-icon name="wifi" size="20px" />
                  <span style="position: relative; top:3px; font-size:11px" class="text-weight-regular q-pl-xs"> {{infoDrone.uplinkSignalQuality}} </span>
                </q-item-label>
              </q-item-section>
              <q-separator spaced />

              <q-item-section class="text-secondary q-ma-sm">
                <q-item-label>
                  <img src="plan/Duree.svg" style="height: 16px; margin-left:2px">
                  <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{flyTime}} Min(s) </span>
                </q-item-label>
              </q-item-section>
              <q-separator spaced />

              <q-item-section class="text-secondary q-ma-sm">
                <q-item-label>
                  <img src="plan/Distance.svg" style="height: 16px;">
                  <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{ superficie}} ha  </span>
                </q-item-label>
              </q-item-section>
              <q-separator spaced />

              <q-item-section class="text-secondary q-ma-sm">
                <q-item-label>
                  <img src="plan/Altitude.svg" style="height: 16px;">
                  <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{Math.round(infoDrone.altitudeInMeters)}} Mètre </span>
                </q-item-label>
              </q-item-section>
              <q-separator spaced />

              <q-item-section class="text-secondary q-ma-sm">
                <q-item-label>
                  <img src="plan/NbreImg.svg" style="height: 16px;">
                  <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{infoMission.imageCount}} Img </span>
                </q-item-label>
              </q-item-section>
              <q-separator spaced />

              <q-item-section class="text-secondary q-ma-sm">
                <q-item-label>
                  <img src="plan/Vitesse.svg" style="height: 13px;">
                  <span style="position: relative; top:-2px; font-size:11px" class="text-weight-regular q-pl-xs"> {{Math.round(infoDrone.speedInMeterPerSec)}} m/s </span>
                </q-item-label>
              </q-item-section>
              <q-separator spaced />

              <div>
                <q-btn
                  style="position: relative; bottom:225px; left: 76px"
                  icon-right="arrow_back_ios"
                  color="white"
                  size="sm"
                  padding="xs"
                  text-color="secondary"
                  @click="onShow"
                />
              </div>
            </q-card>
          </transition>


          <div>
            <q-card style="position: relative; top:80px; left: -10px" v-if="!hideIcon">
              <q-btn
                icon-right="arrow_forward_ios"
                color="white"
                size="sm"
                padding="xs"
                text-color="secondary"
                @click="onShow"
              />
            </q-card>
          </div>
        </div>
      </div>

      <!-- <div class="absolute-bottom z-max">
        lowBattery ==>  {{lowBattery}}
      </div> -->

      <div>
        <q-page-sticky class="z-max" position="bottom" :offset="[0, 2]">
          <q-btn round class="q-pa-none q-ma-md" color="dark" @click="onBack">
            <q-avatar square size="25px">
              <img src="plan/Home.svg">
            </q-avatar>
          </q-btn>
        </q-page-sticky>
      </div>

      <div v-if="!stateBouton">
        <div v-if="!start && !stateMission && !continuLastMission || !start && stateMission && !continuLastMission">
          <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 70]">
            <q-btn round flat class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onStart">
              <q-avatar square size="90px">
                <q-img src="plan/Decoller.gif"/>
              </q-avatar>
                <template v-slot:loading>
                  <q-spinner-ios size="40px"/>
                </template>
            </q-btn>
          </q-page-sticky>
        </div>
      </div>

      <!-- <div v-if="!stateBouton">
        <div v-if="!start && !stateMission && !continuLastMission || !start && stateMission && !continuLastMission">
          <q-page-sticky class="z-max" position="bottom-right" :offset="[35, 86]">
            <q-btn fab class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onStart">
              <q-item-label>
              <q-avatar square size="30px" class="q-pt-xs">
                  <q-img src="plan/Decoller.svg"/>
                </q-avatar> <br>
                <span style="font-size:6px; position: relative; top:-2px">Decoller</span>
              </q-item-label>

              <template v-slot:loading>
                <q-spinner-ios size="40px"/>
              </template>
            </q-btn>
          </q-page-sticky>
        </div>
      </div> -->

      <div v-if="continuLastMission && stateBouton">
        <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 70]">
          <q-btn round flat class="q-pa-none" color="secondary" :loading="submitting" @click.prevent="onContinueMission">
            <q-avatar square size="90px">
                <q-img src="plan/Continue.gif"/>
              </q-avatar>

            <template v-slot:loading>
              <q-spinner-ios size="35px"/>
            </template>
          </q-btn>
        </q-page-sticky>
      </div>

      <!-- <div v-if="start">
        <q-page-sticky class="z-max" position="bottom-right" :offset="[18, 30]">
          <q-btn fab class="q-pa-none" color="negative" @click="onFinish">
            <q-item-label>
            <q-avatar square size="30px" class="q-pt-sm">
                <q-img src="plan/Atterir.svg"/>
              </q-avatar> <br>
              <span style="font-size:7px; position: relative; top:-4px">Attérir</span>
            </q-item-label>
          </q-btn>
        </q-page-sticky>
      </div> -->


    <!-- Modal de comfirmation de l'annulation -->
    <q-dialog v-model="bachModal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down">
      <q-card class="z-max"
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
          <h4 align="center" class="text-weight-light" style="margin-bottom:35px">ANNULATION</h4>
        </div>

        <q-card-section class="q-ma-md" style="margin-top: 20%">
          <div align="center"  class="text-weight-regular" style="font-size:20px">Voulez-vous vraiment annuler la mission ?</div>
        </q-card-section>

        <q-card-actions align="around" style="margin-top: 10%">
          <q-btn rounded color="secondary" v-close-popup >
            <span style="font-size: 20px">Non</span>
          </q-btn>
          <q-btn rounded text-color="black" color="grey-2" @click="onStopMission">
            <span style="font-size: 20px">Oui</span>
          </q-btn>
        </q-card-actions>

        </q-card>
      </q-dialog>

      <!-- Modal de check list -->
      <q-dialog v-model="checkListModal" maximized persistent transition-show="slide-down" transition-hide="slide-up">
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

              <!-- Acces -->
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
      </q-dialog>

      <q-dialog v-model="finishModal" position="bottom" persistent class="z-max" >
        <q-card class="q-ml-md q-mr-md q-pa-md" style="border-radius: 15px 15px 0 0" >

          <q-item class="q-ma-none q-pa-none" style="margin-bottom: -10px; margin-top: -10px">
            <q-item-section>Chargement des images</q-item-section>
            <q-item-section side class="text-secondary">
              ~{{infoMission.imageCount}} Images
            </q-item-section>
          </q-item>
          <q-linear-progress rounded size="15px" :value="progressing" color="secondary" class="q-mt-sm q-ml-xs q-mr-xs">
            <!-- <div class="absolute-full flex flex-center">
              <q-badge color="white" text-color="accent" :label="progressLabel1" />
            </div> -->
          </q-linear-progress>
          <div class="text-weight-light q-mt-xs q-ml-xs" style="font-size: 10px">
            Temps de chargement estimé à 20ms
          </div>

        </q-card>
      </q-dialog>


      <q-dialog full-width v-model="continuModal" persistent class="z-max" >
        <div style="border-radius:15px">
          <q-card class="continuClass q-px-sm q-pb-md">
            <q-card-section class="row items-center q-pa-none q-pt-md">
              <div class="text-h6 text-center text-warning" style="width: 100%;">Confirmation ! </div>
            </q-card-section>
            <q-card-section>
              <div>
                Il y'a une mission en cours, voulez-vous la continuer ?
              </div>
            </q-card-section>
            <q-card-actions
              align="center"
              class=" q-mb-sm row justify-between"
            >
              <q-btn
                label="NON"
                class="text-weight-regular"
                color="warning"
                style="width: 45%; border-radius:5px"
                @click="noComfirm"
              />
              <q-btn
                type="submit"
                label="OUI"
                class="text-weight-regular"
                color="secondary"
                style="width: 45%; border-radius:5px"
                @click="onComfirm"
              >
              </q-btn>
            </q-card-actions>
          </q-card>
        </div>
      </q-dialog>

      <q-dialog
        seamless
        v-model="meteoModal"
        full-width
        transition-show="slide-up"
        transition-hide="slide-down"
      >
        <!-- <q-btn icon="close"
          color="black"
          class="absolute"
          round dense v-close-popup
          style="margin-top:-155px; margin-right:-155px;" /> -->

        <q-card class="my-card" flat style="border-radius: 10px; background: rgba(0,0,10, 0.6); backdrop-filter: blur(5px); overflow-x:hidden">
          <q-item class="text-white">
            <q-item-section>
              <q-item-label class="text-h6">
                <q-icon name="location_on" size="25px" style=""/>
                {{infoMeteo.nom_departement}}
              </q-item-label>
            </q-item-section>
            <q-item-section side style="position: relative; top: 10px">
              <q-item-label class="text-weight-light text-white">{{$dateFormat(infoMeteo.date)}}</q-item-label>
            </q-item-section>
          </q-item>

          <q-separator />

          <q-card-section horizontal class="text-white">
            <q-card-section class="col-8 q-pa-none q-pt-md q-pb-md">
              <q-item class="q-pl-xs q-pr-xs q-pt-md q-pb-md">
                <q-item-section avatar class="q-pr-xs q-pl-xs q-pt-md" >
                  <q-item-label>
                    <span class="text-weight-bold" style="font-size: 45px"> {{infoMeteo.temperature_c}}</span>
                    <span style="position:relative; top:-20px; left:-5px; font-size: 18px">°C</span>
                  </q-item-label>
                </q-item-section>

                <q-item-section class="q-ma-none q-pa-none">
                  <q-item-label class="text-weight-regular" style="font-size: 10px">Précipitation: </q-item-label>
                  <q-item-label class="text-weight-regular" style="font-size: 10px">Humidité: </q-item-label>
                  <q-item-label class="text-weight-regular" style="font-size: 10px">Vent: </q-item-label>
                </q-item-section>

                <q-item-section side>
                  <q-item-label class="text-weight-medium text-white" style="font-size: 10px">{{infoMeteo.precip_mm}}%</q-item-label>
                  <q-item-label class="text-weight-medium text-white" style="font-size: 10px">{{infoMeteo.humidity}}%</q-item-label>
                  <q-item-label class="text-weight-medium text-white" style="font-size: 10px">{{infoMeteo.wind_kph}} km/h</q-item-label>
                </q-item-section>
              </q-item>
            </q-card-section>

            <q-separator vertical />

            <q-card-section class="col text-center q-pa-none q-pt-md q-pb-xs">
              <div style="position:relative; right:-25px; ">
                <lottie-player
                  :src="infoMeteo.lottie_icon"
                  background="transparent"
                  speed="1"
                  style="width: 50px; height: 50px;"
                  loop autoplay>
                </lottie-player>
              </div>
              <q-item-label class="" style="font-size: 12px"> {{infoMeteo.condition_text}} </q-item-label>
            </q-card-section>
          </q-card-section>
        </q-card>
      </q-dialog>

    </q-page>
  </transition>
</template>

<script>
import axios from "axios"
import { Notify, useQuasar } from 'quasar'
import { format, secondsToMinutes } from 'date-fns'
import { useRouter } from 'vue-router'
import { Plugins } from "@capacitor/core";
import { MainServiceModule } from "WeflyTrackingNew";
import { ref, defineComponent, getCurrentInstance, onMounted, onBeforeUnmount, inject, provide, watch} from "vue";
import { JoolMapping } from "joolmapping";
import AddActeur from "components/Acteur/Create.vue";

export default defineComponent({
  // name: 'PageName',

  components: {
    AddActeur,
  },

  props: {
    itemPlan: {
      // type: String,
      default() {
        return {};
      },
    },
  },

  setup(props) {
    const {MainServiceModulePlugin} = Plugins
    let ServiceModule = new MainServiceModule ()
    const $q = useQuasar()
    const router = useRouter()
    const instance = getCurrentInstance()
    const service = "updatePlan";
    let state = inject(service);
    const serviceProjets = "searchProjet";
    let stateProjet = inject(serviceProjets);
    let formData = ref({})
    let eForm = ref("eForm")
    let infoPlan = ref()
    let mymap;
    let groupLayers = L.layerGroup()
    let bachModal = ref(false)
    let checkListModal = ref(false)
    let start = ref(false)
    let hideIcon = ref(true)
    let superficie = ref()
    let date = ref()
    date.value = format(new Date(), "dd/MM/yyyy")
    let infoDrone = ref({
      chargeRemainingInPercent:0,
      uplinkSignalQuality:0,
      altitudeInMeters:0,
      speedInMeterPerSec:0,
    })

    let stateConnect = ref(true)
    let stateProgressing = ref(true)
    let stateMission = ref(false)
    const submitting = ref(false)
    let flyTime = ref(0)
    let finishModal = ref(false)
    let progressing = ref()
    let phonePosition = ref({})
    let startMission = ref(false)
    let continuModal = ref(false)
    let continuLastMission = ref(false)
    let stateBouton = ref(false)
    let lowBattery = ref()
    let coordinate = ref({})

    let drone;
    let layerPolygon;
    let currentPosition = ref()
    let positionCurrent = ref()
    let location = ref()
    let positionDrone = ref()
    let meteoModal = ref(false)
    let infoMeteo = ref([])
    let icon_url = ref("")


    let checkListe = ref({
      gps : null,
      acces : null,
      drone : null,
      camera : null,
      controle : null,
      planDeVol : null,
      phoneBattery : null,
      droneBattery : null,
      sdcard: null,
    })

    let infoMission = ref({
      flyTime: 0,
      imageCount: 0,
      battery: 0,
      speed: 0,
      //surveyPoints
    })

    let checkList = ref({})
    let pointsSurvol = ref([])

    const token = localStorage.getItem('jool_Plan_id-session-app')

    /* ############################ ===> Fonction de connexion au drone <=== ############################*/
    let initial = async () => {
      stateConnect.value = false
      JoolMapping.ini();
      setTimeout(async () => {
        console.log('connect Start');
        let errorHappen = false;
        JoolMapping.connect().catch(err => {
          stateConnect.value = true
          console.log('connect onErr ', err);
          Notify.create({
            type: 'warning',
            message: 'Veuillez-vous connecter au drone',
            timeout: 10000,
            position: 'top',
          })
          errorHappen = true;
        }).then(async res => {
          if (errorHappen){
            return;
          }else {
            stateConnect.value = true
            console.log('connect onSucces res', res);
            Notify.create({
              type: 'positive',
              message: 'Connexion au drone réussit',
              timeout: 5000,
              position: 'top'
            })
            let isDroneTelemetryFindOne = false;
            await JoolMapping.watchDroneTelemetry(async (position, err) => {

              if (err) {
                console.log('watchDroneState onErr ', err);
                return;
              }

              if (position) {
                infoDrone.value = position
                positionDrone.value = position

                //console.log('watchDroneState onSucces position:', JSON.stringify(position));

                // Position du drone en temps réel
                let longitude = position.longitude
                let latitude = position.latitude

                await getDronePosition(latitude, longitude)

                /* Coordonnées du téléphone */
                let lng = location.value.longitude
                let lat = location.value.latitude

                await getPhonePosition(lat, lng)

                if (!isDroneTelemetryFindOne){
                  isDroneTelemetryFindOne = true;
                }
              }
            });
            //await onMissionExist()
          }
        });
      },1000)
    }

    /* ##################### ===> Fonction pour prendre la position du téléphone <=== ##################*/
    let watchPosition = async () => {
        try {
          console.log("Entrer");
          let res = await ServiceModule.watchPosition({
            enableHighAccuracy: false,
            timeout: 5000,
            maximumAge: 0
          }, async (position, err) => {

            if (err) {
              console.log('watchPosition onErr ', err);
            }

            if (position) {
              console.log("watch position", JSON.stringify(position));
              currentPosition.value = JSON.stringify(position);
              positionCurrent.value = JSON.parse(currentPosition.value);

              for (const key in positionCurrent.value) {
                location.value = JSON.parse(positionCurrent.value[key]);
              }
              console.log("watch location", JSON.stringify(location));
              let lng = location.value.longitude
              let lat = location.value.latitude

              await getPhonePosition(lat, lng)
            }

          })

        } catch (error) {
          console.log("error",error);
        }
    }

    /* ############################ ===> Fonction ajouter la position du phone sur la map  <=== ############################*/
    //Personnaliser le marker du telephone
    var phoneIcon = L.icon({
      iconUrl: 'plan/Position.png',
      iconSize:     [25, 40],
      iconAnchor:   [13, 40],
      popupAnchor:  [-1, -35]
    });

    var phone;
    let getPhonePosition = async (lat, lng) => {

      console.log("location tel", JSON.stringify(location.value));
      let positionTel = L.latLng(location.value.latitude, location.value.longitude);
      let centrePolygone = L.latLng(coordinate.value.latitude,  coordinate.value.longitude)
      console.log('centrePolygone', JSON.stringify(centrePolygone));

      // Distance phone - Centroid
      let disCentroid = positionTel.distanceTo(centrePolygone);
      console.log(disCentroid, 'metre');
      let uniCentroid = "Mètre"
      if (disCentroid >= 1000) {
        disCentroid = disCentroid / 1000
        uniCentroid = "Km"
      }

      if (positionDrone.value) {
        let dronPosition = L.latLng(positionDrone.value.latitude, positionDrone.value.longitude);

        // Distance phone - drone
        var distance = positionTel.distanceTo(dronPosition);
        console.log(distance, 'metre');
        let unite = "Mètre"
        if (distance >= 1000) {
          distance = distance / 1000
          unite = "Km"
        }

        if(phone){
          mymap.removeLayer(phone);
        }
        phone = L.marker([lat, lng],{icon: phoneIcon})
        .bindPopup("<b>Distance</b><br>" + "Vous êtes à : <br>" + distance.toFixed(2) + " " + unite + " du drone <br>" + disCentroid.toFixed(2) + " " + uniCentroid + " centre du polygone" );
        groupLayers.addLayer(phone)
      }else {
        if(phone){
          mymap.removeLayer(phone);
        }
        phone = L.marker([lat, lng],{icon: phoneIcon})
        .bindPopup("Vous êtes à " + disCentroid.toFixed(2) + " " + uniCentroid + " du centre du polygone")
        groupLayers.addLayer(phone)
      }
    }

    let getMetoe = async () => {
      const icons = {
        "1000": "https://assets2.lottiefiles.com/temp/lf20_Stdaec.json",
        "1003": "https://assets4.lottiefiles.com/datafiles/ugFV3T9Zi676bvx/data.json",
        "1006": "https://assets6.lottiefiles.com/temp/lf20_VAmWRg.json",
        "1009": "https://assets6.lottiefiles.com/temp/lf20_VAmWRg.json",
        "1030": "https://assets6.lottiefiles.com/temp/lf20_VAmWRg.json",
        "1063": "https://assets2.lottiefiles.com/temp/lf20_rpC1Rd.json",
        "1180": "https://assets2.lottiefiles.com/temp/lf20_rpC1Rd.json",
        "1240": "https://assets2.lottiefiles.com/temp/lf20_rpC1Rd.json",
        "1243": "https://assets2.lottiefiles.com/temp/lf20_rpC1Rd.json",
        "1246": "https://assets2.lottiefiles.com/temp/lf20_rpC1Rd.json",
        "1186": "https://assets2.lottiefiles.com/temp/lf20_rpC1Rd.json",
        "1192": "https://assets2.lottiefiles.com/temp/lf20_rpC1Rd.json",
        "1087": "https://assets6.lottiefiles.com/temp/lf20_Kuot2e.json",
        "1273": "https://assets6.lottiefiles.com/temp/lf20_Kuot2e.json",
        "1276": "https://assets6.lottiefiles.com/temp/lf20_Kuot2e.json",
        "1135": "https://assets6.lottiefiles.com/temp/lf20_kOfPKE.json",
        "1072": "https://assets6.lottiefiles.com/temp/lf20_kOfPKE.json",
        "1198": "https://assets6.lottiefiles.com/temp/lf20_kOfPKE.json",
        "1201": "https://assets6.lottiefiles.com/temp/lf20_kOfPKE.json",
        "1147": "https://assets6.lottiefiles.com/temp/lf20_kOfPKE.json",
        "1150": "https://assets10.lottiefiles.com/private_files/lf30_orqfuyox.json",
        "1153": "https://assets10.lottiefiles.com/private_files/lf30_orqfuyox.json",
        }

        axios({
        method: 'post',
        url: 'https://adb.jool-tech.com/_db/db/weather_api/get_prevision_jour',
        data: {
          longitude: coordinate.value.longitude,
          latitude: coordinate.value.latitude
        },
        headers:{
            username: 'jool_weather_api',
            password: '95Dpms2HHwhXruU6By46KLqprfaR7'
        }
      }).then( response => {
        let tab = response.data
        infoMeteo.value = tab[0]
        icon_url.value = icons[infoMeteo.value.code]
        console.log("infoMeteo.value", infoMeteo.value);
      });
    }


    /* ############################ ===> Fonction onMunted <=== ############################*/
    onMounted( async () =>{
      infoPlan.value = JSON.parse(props.itemPlan)
      formData.value = infoPlan.value
      let polygon = JSON.parse(infoPlan.value.polygone)
      let centroid = JSON.parse(polygon.centroid)

      //Formatage pour centrer la map
      if (centroid.features) {
        for (const coord of centroid.features[0].geometry.coordinates){
          if (coord > 0) {
            console.log("latitude features ==>",coord);
            coordinate.value.latitude = coord
          } else{
            console.log("longitude features ==>", coord);
            coordinate.value.longitude = coord
          }
        }
      }

      if (centroid.coordinates) {
        for (const coord of centroid.coordinates){
          if (coord > 0) {
            console.log("latitude coordinates ==>",coord);
            coordinate.value.latitude = coord
          } else{
            console.log("longitude coordinates ==>", coord);
            coordinate.value.longitude = coord
          }
        }
      }

      let tileMap = L.tileLayer('https://tiles.stadiamaps.com/tiles/outdoors/{z}/{x}/{y}{r}.png', {
        minZoom: 6,
        maxZoom: 20,
        attribution: '&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
      })

      mymap = L.map('mapid', {
        layers: [tileMap],
        zoomControl: false,
      }).setView([ coordinate.value.latitude,  coordinate.value.longitude], 16);

      /* L.control.zoom({
        position: 'topright'
      }).addTo(mymap); */

      function onLocationFound(e) {
        phonePosition.value = e.latlng
        console.log("phonePosition.value", phonePosition.value);
        let lat =  phonePosition.value.lat
        let longitude = phonePosition.value.lng
        //await getMetoe(lat, lng)
      }

      mymap.locate({});
      mymap.on("locationfound", onLocationFound);

      await getMetoe()

      mymap.removeLayer(groupLayers);
      mymap.addLayer(groupLayers);

      let geometry = JSON.parse(polygon.geometrie)
      superficie.value = polygon.superficie.toFixed(2)

      if (layerPolygon) {
        mymap.removeLayer(layerPolygon);
      }
      layerPolygon = L.geoJSON(geometry)
      groupLayers.addLayer(layerPolygon)

      /* Position du telephone */
      await watchPosition()

      // Fonction de connexion au drone
      await initial()


    })

    /* ###################### ===> Fonction de vérification de mision en cours <=== ###################*/
    let onMissionExist = async () => {
      let errorHappen = false;
      // Continu old Mission
      await JoolMapping.isMissionExist().catch(async (err) => {
        // No mission found
        await onGetInfoMissin()
        errorHappen = true;
        console.log('isMissionExist onErr ==>',err);
      }).then(async () => {
        if(errorHappen){
          return;
        }else{
          console.log('isMissionExist Case Mission Exist ==>');
          continuModal.value = true
        }
      });
      return;
    }

    // Si le drone n'est pas connect
    watch(stateConnect, async () => {
      if (!stateConnect.value) {
        $q.loading.show({
          message: 'Connexion au drone ...',
          boxClass: 'bg-grey-2 text-grey-9',
          spinnerColor: 'primary'
        })
      }else {
        $q.loading.hide()
        await onMissionExist()
        //await onGetInfoMissin()
      }
    })

    watch(stateProgressing, async () => {
      console.log("stateProgressing ==>", stateProgressing.value);

      if(!stateProgressing.value){
        $q.loading.show({
          message: 'Configuration de mission ...',
          boxClass: 'bg-grey-2 text-grey-9',
          spinnerColor: 'primary'
        })
      }else{
        $q.loading.hide()
      }
    })

    /* ###################### ===> Fonction d'ajout du drone sur la map <=== ################### */
    //Personnaliser le marker du drone
    var droneIcon = L.icon({
      iconUrl: 'plan/Drone1.png',
      iconSize:     [45, 20],
      iconAnchor:   [22, 15],
      popupAnchor:  [-1, -12]
    });

    let getDronePosition = async (latitude, longitude) => {
      if(drone){
        mymap.removeLayer(drone);
      }
      drone = L.marker([latitude, longitude],{icon: droneIcon})
      groupLayers.addLayer(drone)
    }


    /* ###################### ===> Fonction pour configurer le drone <=== ################### */
    let onGetInfoMissin = async () => {
      stateProgressing.value = false
      let errorHappen = false;
      let coords;
      let poly = JSON.parse(infoPlan.value.polygone)
      let geo = JSON.parse(poly.geometrie)
      console.log("geo =======>", JSON.stringify(geo));

      if (geo.features) {
        //coords = geo.features[0].geometry.coordinates
        let coord = JSON.stringify(geo.features[0].geometry.coordinates)
        let first = coord.substring(1)
        let last = first.substring(0, first.length - 1)
        coord = last

        coords = JSON.parse(coord)
        console.log("coordinate 1 ==> ", JSON.stringify(coords) );
      }

      if (geo.coordinates) {
        coords = geo.coordinates
        console.log("coordinate 2 ==> ", JSON.stringify(coords) );
      }

      let missionOptions = {};
      missionOptions.altitude = infoPlan.value.altitude
      missionOptions.exitMissionOnRCLost = false;
      missionOptions.autoGoHomeOnLowBattery = true;
      missionOptions.daylight = 1; // 2=SUNNY , 1=NORMAL
      missionOptions.phoneLatitude = phonePosition.value.lat // YOU PHONE latitude
      missionOptions.phoneLongitude = phonePosition.value.lng  // YOU PHONE longitude
      missionOptions.polygonPoints = {
        "type": "Feature",
        "properties": {},
        "geometry": {
          "type": "Polygon",
          "coordinates": coords
        }
      };

      await JoolMapping.setMissionSetting(missionOptions).catch(err =>{
        errorHappen = true;
        stateProgressing.value = true
        console.log("setMissionSetting  err",err);
        Notify.create({
          type: 'negative',
          message: 'Une erreur s\'est produite lors de la configuration de la mission',
          timeout: 5000,
          position: 'top'
        })
      }).then(async ()=>{
        if(errorHappen){
          return;
        }else {
          console.log("setMissionSetting  Succes");
          await JoolMapping.getMissionInfo(async (response) => {

            if (response.error){
              console.log("getMissionInfo Error Happen",response.error);
              return;
            }else {
              stateProgressing.value = true
              stateMission.value = true
              console.log("getMissionInfo  Succes");
              console.log("getMissionInfo response String",JSON.stringify(response));

              if (response.gridProgressing){
                console.log("getMissionInfo gridProgressing:",response.gridProgressing);
                return;
              }

              if (response.flyTime && response.imageCount
              && response.battery
              && response.speed
              && response.surveyPoints){
                console.log("getMissionInfo flyTime:",response.flyTime);
                console.log("getMissionInfo imageCount:",response.imageCount);
                console.log("getMissionInfo battery:",response.battery);
                console.log("getMissionInfo speed:",response.speed);
                console.log("getMissionInfo surveyPoints:",response.surveyPoints);
                console.log("performChecklist  Fired", JSON.stringify(response));

                infoMission.value = response
                console.log("infoMission.value Fired", JSON.stringify(infoMission.value));

                flyTime.value = secondsToMinutes(infoMission.value.flyTime) + 1

                // Convertir la chaine de caractère en tableau
                let string = infoMission.value.surveyPoints
                string = string.replace('[', '')
                string = string.replace(']', '')
                while (string.search('lat/lng: ') != -1){
                    string = string.replace('lat/lng: ', '')
                }
                let tab = string.split(', ')

                tab.forEach(element => {
                  element = element.replace('(', '')
                  element = element.replace(')', '')
                  let tab2 = element.split(',')
                  //console.table(tab2)

                  for (let index = 0; index < tab2.length; index++) {
                    tab2[index] = +tab2[index];
                  }
                  pointsSurvol.value.push(tab2)
                });
                console.table(pointsSurvol.value)
                console.log("pointsSurvol", JSON.stringify(pointsSurvol.value));

                var polyline = L.polyline(pointsSurvol.value, {color: 'green'})
                groupLayers.addLayer(polyline)
                mymap.flyToBounds(polyline.getBounds(),  {'duration':0.50});

                // Fonction de Mise a jour de plan
                await onUpdate()
              }
            }
          });
        }
      });
    }

    /* ###################### ===> Fonction de control avant le survol <=== ################### */
    let onChecklist = async () => {
      await JoolMapping.performChecklist({caseContinuMission:false},async (response) => {
        if (response.error){
          console.log("performChecklist Error Happen",response.error);
          return;
        }else {
          console.log("performChecklist response String",JSON.stringify(response));
          checkList.value = response

          if (response.acces === true){
            console.log("performChecklist acces isOK:"+response.acces);
            setTimeout(() =>{
              checkListe.value.acces = true
            }, 1000)
            return;
          }
          if (response.acces === false){
            console.log("performChecklist acces isOK:"+response.acces);
            setTimeout(() =>{
              checkListe.value.acces = false
            }, 1000)
          }

          if (response.config === true){
            console.log("performChecklist config isOK:"+response.config);
            setTimeout(() =>{
              checkListe.value.drone = true
            }, 2000)
            return;
          }
          if (response.config === false){
            console.log("performChecklist config isOK:"+response.config);
            setTimeout(() =>{
              checkListe.value.drone = false
            }, 2000)
          }

          if (response.controle === true){
            console.log("performChecklist controle isOK:"+response.controle);
            setTimeout(() =>{
              checkListe.value.controle = true
            }, 3000)
            return;
          }
          if (response.controle === false){
            console.log("performChecklist controle isOK:"+response.controle);
            setTimeout(() =>{
              checkListe.value.controle = false
            }, 3000)
          }

          if (response.sdcard === true){
            console.log("performChecklist sdcard isOK:"+response.sdcard);
            setTimeout(() =>{
              checkListe.value.sdcard = true
            }, 4000)
            return;
          }
          if (response.sdcard === false){
            console.log("performChecklist sdcard isOK:"+response.sdcard);
            setTimeout(() =>{
              checkListe.value.sdcard = false
            }, 4000)
          }

          if (response.camera === true){
            console.log("performChecklist camera isOK:"+response.camera);
            setTimeout(() =>{
              checkListe.value.camera = true
            }, 5000)
            return;
          }
          if (response.camera === false){
            console.log("performChecklist camera isOK:"+response.camera);
            setTimeout(() =>{
              checkListe.value.camera = false
            }, 5000)
          }

          if (response.flyplan === true){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            setTimeout(() =>{
              checkListe.value.planDeVol = true
            }, 6000)
            return;
          }
          if (response.flyplan === false){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            setTimeout(() =>{
              checkListe.value.planDeVol = false
            }, 6000)
          }

          if (response.phoneBattery === true){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            setTimeout(() =>{
              checkListe.value.phoneBattery = true
            }, 7000)
            return;
          }
          if (response.phoneBattery === false){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            setTimeout(() =>{
              checkListe.value.phoneBattery = false
            }, 7000)
          }

          if (response.droneBattery === true){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            setTimeout(() =>{
              checkListe.value.droneBattery = true
            }, 8000)
            return;
          }
          if (response.droneBattery === false){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            setTimeout(() =>{
              checkListe.value.droneBattery = false
            }, 8000)
          }

          if (response.gps === true){
            console.log("performChecklist gps isOK:"+response.gps);
            setTimeout(() =>{
              checkListe.value.gps = true
            }, 9000)
            return;
          }
          if (response.gps === false){
            console.log("performChecklist gps error:"+response.gps);
            setTimeout(() =>{
              checkListe.value.gps = false
            }, 9000)
          }

          if (response.checkSucces){
            setTimeout(() => {
              checkListModal.value = false
            }, 10000)

            setTimeout(() => {
              checkListe.value.gps = null
              checkListe.value.acces = null
              checkListe.value.drone = null
              checkListe.value.sdcard = null
              checkListe.value.camera = null
              checkListe.value.controle = null
              checkListe.value.planDeVol = null
              checkListe.value.phoneBattery = null
              checkListe.value.droneBattery = null
            },12000)
            //await onMissionExist()
            setTimeout(async () => {
              console.log("performChecklist checkSucces Mean Everything is Ok");
              await JoolMapping.startWaypointMission(async (response) => {

                console.log("startWaypointMission detail:",JSON.stringify(response));

                if (response.error){
                  console.log("startWaypointMission Error Happen",response.error);
                  return;
                }

                if (response.startSucces){
                  console.log("startWaypointMission MissionStart successful:");
                  submitting.value = false;
                  start.value = true
                  return;
                }

                if (response.finishSucces){
                  console.log("startWaypointMission MissionEnd successful",response.finishSucces);
                  await onMissionFinish()
                  let status = true
                  await onUpdate(status)
                  start.value = false;

                  Notify.create({
                    type: 'positive',
                    message: 'Mission terminée avec succès',
                    position: 'top',
                    timeout: 11000,
                  })
                  return;
                }

                if (response.lowBattery){
                  continuLastMission.value = true
                  stateBouton.value = true
                  console.log("lowBattery",response.finishSucces);
                  lowBattery.value = response.lowBattery

                  Notify.create({
                    type: 'warning',
                    message: 'La batterie du drone est faible !',
                    position: 'top',
                    timeout: 10000,
                  })
                  return;
                }

                console.log("startWaypointMission: response String",JSON.stringify(response));
              });
              return;
            }, 12000)
          }

          if(!response.checkSucces) {
            setTimeout(() => {
              submitting.value = false;
              console.log("checkSucces erreur");
              //checkListModal.value = false
            }, 5000)
          }
        }
      });
    }

    /* ###################### ===> Fonction pour demarrervla mission <=== ################### */
    let onStart = async () => {
      submitting.value = true
      checkListModal.value = true

      // Fonfion de check liste
      await onChecklist()

      // Write Log in file
      console.log('logToFile Will Write');
      await JoolMapping.logToFile({message:"Hello log from CAPACITOR APP"}).catch(err => {
        errorHappen = true;
        console.log('logToFile onErr ', err);
      }).then(() =>{
        if(errorHappen){
          return;
        }else{
          console.log('logToFile Succes ');
        }
      });
    }

    let onFinish = async () => {
      start.value = false
      checkListModal.value = false
    }

    let onShow = () => {
      hideIcon.value = !hideIcon.value
    }

    let onMissionFinish = async () => {
      finishModal.value = true
      progressing.value = 0.01

      do {
        setInterval(() => {
            progressing.value = Math.min(1, progressing.value + 0.1)
        }, 2000)
      } while (progressing.value === 1);

      watch(progressing , async () =>{
        if (progressing.value === 1) {
            setTimeout(() => {
              finishModal.value = false
            }, 3000)

            setTimeout(() => {
              //router.push("/accueil")
            }, 3500)
        }
      })
    }

    let stateConnectToContinu = ref(true)
    // Si le drone n'est pas connect
    watch(stateConnectToContinu, async () => {
      if (!stateConnectToContinu.value) {
        $q.loading.show({
          message: 'Connexion au drone ...',
          boxClass: 'bg-grey-2 text-grey-9',
          spinnerColor: 'primary'
        })
      }else {
        $q.loading.hide()
        await startContinuMission()
      }
    })

    /* ###################### ===> Fonction pour continuer la mission <=== ################### */
    let onContinueMission = async () => {
      await startContinuMission()
    }

    let startContinuMission = async () => {
      submitting.value = true
      checkListModal.value = true
      console.log("isMissionExist Case Mission Exist ");
      // Fonfion de check liste
      // await onChecklistContinuMission()
      await JoolMapping.performChecklist({caseContinuMission:true},async (response) => {
        if (response.error){
          console.log("performChecklist Error Happen",response.error);
          return;
        }else {
          console.log("performChecklist  Succes");
          console.log("performChecklist response String",JSON.stringify(response));

          if (response.acces === true){
            console.log("performChecklist acces isOK:"+response.acces);
            setTimeout(() =>{
              checkListe.value.acces = true
            }, 1000)
            return;
          }
          if (response.acces === false){
            console.log("performChecklist acces isOK:"+response.acces);
            setTimeout(() =>{
              checkListe.value.acces = false
            }, 1000)
          }

          if (response.config === true){
            console.log("performChecklist config isOK:"+response.config);
            setTimeout(() =>{
              checkListe.value.drone = true
            }, 2000)
            return;
          }
          if (response.config === false){
            console.log("performChecklist config isOK:"+response.config);
            setTimeout(() =>{
              checkListe.value.drone = false
            }, 2000)
          }

          if (response.controle === true){
            console.log("performChecklist controle isOK:"+response.controle);
            setTimeout(() =>{
              checkListe.value.controle = true
            }, 3000)
            return;
          }
          if (response.controle === false){
            console.log("performChecklist controle isOK:"+response.controle);
            setTimeout(() =>{
              checkListe.value.controle = false
            }, 3000)
          }

          if (response.sdcard === true){
            console.log("performChecklist sdcard isOK:"+response.sdcard);
            setTimeout(() =>{
              checkListe.value.sdcard = true
            }, 4000)
            return;
          }
          if (response.sdcard === false){
            console.log("performChecklist sdcard isOK:"+response.sdcard);
            setTimeout(() =>{
              checkListe.value.sdcard = false
            }, 4000)
          }

          if (response.camera === true){
            console.log("performChecklist camera isOK:"+response.camera);
            setTimeout(() =>{
              checkListe.value.camera = true
            }, 5000)
            return;
          }
          if (response.camera === false){
            console.log("performChecklist camera isOK:"+response.camera);
            setTimeout(() =>{
              checkListe.value.camera = false
            }, 5000)
          }

          if (response.flyplan === true){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            setTimeout(() =>{
              checkListe.value.planDeVol = true
            }, 6000)
            return;
          }
          if (response.flyplan === false){
            console.log("performChecklist flyplan isOK:"+response.flyplan);
            setTimeout(() =>{
              checkListe.value.planDeVol = false
            }, 6000)
          }

          if (response.phoneBattery === true){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            setTimeout(() =>{
              checkListe.value.phoneBattery = true
            }, 7000)
            return;
          }
          if (response.phoneBattery === false){
            console.log("performChecklist phoneBattery isOK:"+response.phoneBattery);
            setTimeout(() =>{
              checkListe.value.phoneBattery = false
            }, 7000)
          }

          if (response.droneBattery === true){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            setTimeout(() =>{
              checkListe.value.droneBattery = true
            }, 8000)
            return;
          }
          if (response.droneBattery === false){
            console.log("performChecklist droneBattery isOK:"+response.droneBattery);
            setTimeout(() =>{
              checkListe.value.droneBattery = false
            }, 8000)
          }

          if (response.gps === true){
            console.log("performChecklist gps isOK:"+response.gps);
            setTimeout(() =>{
              checkListe.value.gps = true
            }, 9000)
            return;
          }
          if (response.gps === false){
            console.log("performChecklist gps error:"+response.gps);
            setTimeout(() =>{
              checkListe.value.gps = false
            }, 9000)
          }

          if (response.checkSucces){
            setTimeout(() => {
              checkListModal.value = false
            }, 10000)

            setTimeout(() => {
              checkListe.value.gps = null
              checkListe.value.acces = null
              checkListe.value.drone = null
              checkListe.value.sdcard = null
              checkListe.value.camera = null
              checkListe.value.controle = null
              checkListe.value.planDeVol = null
              checkListe.value.phoneBattery = null
              checkListe.value.droneBattery = null
            },12000)


          setTimeout(async () => {
              // case mission found
            console.log("performChecklist checkSucces Mean Everything is Ok");
            await JoolMapping.continuWaypointMission(async (response) => {
              if (response.error){
                console.log("continuWaypointMission Error Happen",response.error);
                return;
              }
              console.log("continuWaypointMission detail:",response);
              if (response.startSucces){
                console.log("continuWaypointMission MissionStart successful:");
                submitting.value = false;
                continuLastMission.value = false
                startMission = true;
                return;
              }
              if (response.finishSucces){
                console.log("continuWaypointMission MissionEnd successful",response.finishSucces);
                await onMissionFinish()
                let status = true
                await onUpdate(status)
                continuLastMission.value = false;

                Notify.create({
                  type: 'positive',
                  message: 'Mission terminée avec succès',
                  position: 'top',
                  timeout: 10000,
                })

                return;
              }

              if (response.lowBattery){
                continuLastMission.value = true
                stateBouton.value = true
                console.log("lowBattery",response.finishSucces);
                lowBattery.value = response.lowBattery

                Notify.create({
                  type: 'warning',
                  message: 'La batterie du drone est faible !',
                  position: 'top',
                  timeout: 10000,
                })
                return;
              }

              console.log("continuWaypointMission: response String",JSON.stringify(response));
            });
            return;
           }, 11000)
          }

          if(!response.checkSucces) {
            setTimeout(() => {
              submitting.value = false;
              //checkListModal.value = false
            }, 5000)
          }
        }
      });

      // Write Log in file
      console.log('logToFile Will Write');
      await JoolMapping.logToFile({message:"Hello log from CAPACITOR APP"}).catch(err => {
        errorHappen = true;
        console.log('logToFile onErr ', err);
      }).then(() =>{
        if(errorHappen){
          return;
        }else{
          console.log('logToFile Succes ');
        }
      });
    }

    let onReturnToHome = async () => {
      console.log("returnToHome");
      let errorHappen = false;
      await JoolMapping.returnToHome().catch(err => {
        errorHappen = true;
        console.log('returnToHome onErr ', err);
      }).then(() =>{
        if(errorHappen){
          return;
        }else{
          console.log('returnToHome Succes ');
        }
      });
    }

    let onComfirm = async () => {
      if(infoPlan.value.flyPlan) {
        let flyPlan = JSON.parse(infoPlan.value.flyPlan)
        var polyline = L.polyline(flyPlan, {color: 'green'})
        groupLayers.addLayer(polyline)
        mymap.flyToBounds(polyline.getBounds(),  {'duration':0.50});
      }

      let isDroneTelemetryFindOne = false;
      await JoolMapping.watchDroneTelemetry(async (position, err) => {
        if (err) {
          console.log('watchDroneState onErr ', err);
          return;
        }

        if (position) {
          infoDrone.value = position
          positionDrone.value = position

          // Position du drone en temps réel
          let longitude = position.longitude
          let latitude = position.latitude

          await getDronePosition(latitude, longitude)

          /* Coordonnées du téléphone */
          let lng = location.value.longitude
          let lat = location.value.latitude

          await getPhonePosition(lat, lng)

          if (!isDroneTelemetryFindOne){
            isDroneTelemetryFindOne = true;
          }
        }
      });

      continuLastMission.value = true
      continuModal.value = false
      stateBouton.value = true
    }

    let noComfirm = async () => {
      continuModal.value = false
      await onGetInfoMissin()
    }

    let onBack = () => {
      bachModal.value = true
    }

    let onStopMission = () => {
      console.log("onStopMission ====");
      router.push('accueil')
      mymap.removeLayer(groupLayers);
      ServiceModule.stopWatchPosition().catch(err => {
        console.log( 'stopWatchPosition onErr ', err);
      }).then( res => {
        console.log('stopWatchPosition onSucces ',res);
        //valueIsWatchStop = true;
      });
    }

    let onUpdate = async (status) => {
      console.log("onUpdate",status);
      state.filters.value = `
        mutation($id:UUID, $libelle:String!, $altitude:Int, $nbrBatteries:Int, $nbrImages:Int, $temps:Int, $status:Boolean, $flyPlan:String) {
          updatePlan(newPlan:
            {
              id:$id
              libelle:$libelle,
              altitude:$altitude,
              nbrBatteries:$nbrBatteries,
              nbrImages:$nbrImages,
              temps:$temps,
              status: $status,
              flyPlan:$flyPlan,
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

        if (status) {
          //eForm.value = "eForm"
          formData.value.nbrBatteries = infoMission.value.battery
          formData.value.nbrImages = infoMission.value.imageCount
          formData.value.temps = flyTime.value
          formData.value.status = true
          state.item.value = formData.value;
          console.log(formData.value);
          state.updateForm.value = eForm.value;
          await state.update()
        } else {
          formData.value.flyPlan = pointsSurvol.value
          formData.value.nbrBatteries = infoMission.value.battery
          formData.value.nbrImages = infoMission.value.imageCount
          formData.value.temps = flyTime.value
          state.item.value = formData.value;
          console.log(formData.value);
          state.updateForm.value = eForm.value;
          await state.update()
        }


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
          itemsProjet.value = JSON.parse(localStorage.getItem('searchProjet'))
    }


    let updateMission = async () => {
      try {
        let res = await instance.appContext.app.config.globalProperties.$api({
            url: `/`,
            method: "post",
            headers: {
                Authorization: `JWT ${token}`
            },
            data: {
                query: `
                  mutation($id:UUID, $status:Boolean,) {
                    updatePlan(newPlan:
                      {
                        id:$id
                        status: $status,
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
                `,

                variables: {
                  id: infoPlan.value.id,
                  status: true,
                }
            },
        })
        console.log("Mission update  |==> ", res.data);
      } catch (error) {
        console.log(error);
      }
    }

    let onZoomIn = async () => {
      mymap.setZoom(mymap.getZoom() + 1)
    }

    let onZoomOut = async () => {
      mymap.setZoom(mymap.getZoom() - 1)
    }

    let onGetMeteo = async () => {
      meteoModal.value = !meteoModal.value
    }

    let hideControl = async () => {
      checkListModal.value = false
    }

    return{
      initial,
      onGetInfoMissin,
      getDronePosition,
      onBack,
      onChecklist,
      onMissionFinish,
      onMissionExist,
      onUpdate,
      bachModal,
      checkListModal,
      onStart,
      onFinish,
      start,
      onShow,
      hideIcon,
      infoPlan,
      superficie,
      date,
      infoDrone,
      checkList,
      checkListe,
      infoMission,
      stateMission,
      submitting,
      flyTime,
      finishModal,
      progressing,
      continuModal,
      onComfirm,
      continuLastMission,
      onContinueMission,
      noComfirm,
      stateBouton,
      startMission,
      onReturnToHome,
      lowBattery,
      onStopMission,
      onZoomIn,
      onZoomOut,
      onGetMeteo,
      meteoModal,
      infoMeteo,
      icon_url,
      hideControl

    }
  }
})
</script>

<style scoped>
.container:before {
	content: "";
	position: absolute;
	background: inherit;
	z-index: -1;
	inset: 0;
	filter: blur(10px);
	margin: -20px;
}

.continuClass {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}

.hide {
  top: -26vh;
}

</style>
