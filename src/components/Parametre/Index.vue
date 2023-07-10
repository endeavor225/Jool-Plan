<template>
  <q-card class="acteur" v-if="!changePasswordModal" :style="drawerStyle()">

    <div clickable class="q-mt-xs q-pa-sm q-pl-lg q-pr-lg text-center" v-touch-pan.mouse="slideDrawer" style="margin-left:80px; margin-right:80px;">
      <q-separator size="5px" color="grey-8" inset   @click="onClose"/>
    </div>

    <div class="absolute-top-right q-mt-md">
      <q-btn flat label="Déconnexion" no-caps color="teal" @click="onLogout"/>
    </div>

    <q-card
        class="bg-white absolute-bottom"
        style="height: 90vh; border-radius: 15px 15px 0 0"
      >
        <div style="text-align:center" >
          <div style="margin-top: -30px;">
            <div v-if="!modifier">
              <q-avatar size="120px" v-if="user.imageUrl || file && modifier">
                <q-img :src="base64String" />
              </q-avatar>
              <q-avatar size="120px" v-else>
                <img src="avatar.jpg">
              </q-avatar>
            </div>

            <div v-if="modifier">
               <q-btn round flat @click="onUpload">
                <div v-if="user.imageUrl || file && modifier">
                  <q-avatar size="120px" style="position: absolute;">
                    <q-img :src="base64String" />
                  </q-avatar>
                  <q-avatar size="120px" font-size="30px" color="grey-6" text-color="white" icon="photo_camera"  style="opacity: 0.4" />
                </div>
                <div v-else>
                  <q-avatar size="120px" >
                    <img src="avatar.jpg">
                  </q-avatar>
                </div>
              </q-btn>
            </div>

          </div>
        </div>

        <div style="text-align:center; margin-top:20px">
          <span class="text-weight-bold">{{user.lastName}}</span> <span class="text-weight-regular"> {{user.firstName}} ({{user.username}})</span> <br>
          <span class="text-grey-7" >Participant <span v-if="getAccount">({{getAccount.totalUsers}}</span> utilisateurs) </span>
        </div>

        <div class="row q-ma-xs q-gutter-xs" style="margin-top: 15px">
        <div class="col q-ma-xs">
          <q-card class="text-center">
            <q-card-section class="text-white q-pl-none q-pr-none" style="background-color: #337682">
              <div class="text-weight-light" style="font-size:10px">CULTURES</div>
              <div class="text-weight-bold" style="font-size:18px">
                <span v-if="getAccount">
                  {{getAccount.totalCultures}}
                </span>
              </div>
            </q-card-section>
          </q-card>
        </div>

        <div class="col q-ma-xs" >
          <q-card class="text-center">
            <q-card-section class="text-white q-pl-none q-pr-none" style="background-color: #F99746">
              <div class="text-weight-light" style="font-size:10px">N° DE PARCELLE</div>
              <div class="text-weight-bold" style="font-size:18px">
                <span v-if="getAccount">
                  {{getAccount.totalParcelles}}
                </span>
              </div>
            </q-card-section>

          </q-card>
        </div>

        <div class="col q-ma-xs">
          <q-card class="text-center">
            <q-card-section class="text-white q-pl-none q-pr-none" style="background-color: #158AFF">
              <div class="text-weight-regular" style="font-size:10px">SURFACE</div>
              <div class="text-weight-bold" style="font-size:18px">
                <span v-if="getAccount.totalParcellesArea">
                  {{getAccount.totalParcellesArea.toFixed(2)}}<span style="font-size:13px" class="text-weight-regular"> ha</span>
                </span>
              </div>
            </q-card-section>
          </q-card>
        </div>
      </div>

      <q-separator class="q-ma-md" color="grey-6" size="1px" inset style="margin-top: 15px"/>

      <div style="text-align:center;" class="q-mt-md">
        <span class="text-body1" style="font-size:18px">Informations personnelles</span>
      </div>

      <div class="q-mt-lg" v-if="!modifier">
        <q-item>
          <q-item-section>
            <q-item-label class="text-secondary">{{user.lastName}}</q-item-label>
            <q-item-label caption style="margin-top: 2px">Nom</q-item-label>
          </q-item-section>
        </q-item>
        <q-item>
          <q-item-section>
            <q-item-label class="text-secondary">{{user.firstName}}</q-item-label>
            <q-item-label caption style="margin-top: 2px">Prenom</q-item-label>
          </q-item-section>
        </q-item>
        <q-item>
          <q-item-section>
            <q-item-label class="text-secondary">+225 {{user.telephone}}</q-item-label>
            <q-item-label caption style="margin-top: 2px">Téléphone</q-item-label>
          </q-item-section>
        </q-item>
        <q-item>
          <q-item-section>
            <q-item-label class="text-secondary">{{user.username}}</q-item-label>
            <q-item-label caption style="margin-top: 2px">Email</q-item-label>
          </q-item-section>
        </q-item>

        <div class="q-ma-md">
          <q-btn color="secondary" type="submit" padding="xs" label="Modifier" rounded class="full-width" @click="onEdit"/>
        </div>

        <div class="q-ma-md" align="center">
          <q-btn  flat color="secondary" type="submit" padding="xs" no-caps label="Modifier mot de passe" @click="onChange"/>
        </div>
      </div>

      <div class="q-mt-lg" v-if="modifier">
        <q-form
          @submit="onUpdate"
          class="q-gutter-xs q-ma-sm"
          ref="eForm"
        >
          <q-input
            dense
            rounded
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.lastName"
            label="Nom"
            :rules="[ (val) => !!val || 'Le champ nom est obligatoire']"
          />

          <q-input
            dense
            rounded
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.firstName"
            label="Prenom"
            :rules="[ (val) => !!val || 'Le champ prenom est obligatoire']"
          />


          <q-input
            dense
            rounded
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.telephone"
            label="Téléphone"
            maxlength="10"
            :rules="[
              (val) => !!val || 'Le champ téléphone est obligatoire',
              (val) => val.length > 9 || 'ce champ doit contenir 10 caractères',
              isValidNumber
            ]"
          />


          <q-input
            dense
            rounded
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.username"
            label="Email"
            :rules="[ (val) => !!val || 'Le champ email est obligatoire', isValidEmail]"
          />

          <q-file
            style="display: none"
            ref="fileUpload"
            dense
            rounded
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            label="Photo de profil"
            v-model="file"
            use-chips
            accept=".png, .jpg, .jpeg, .svg"
          />



            <q-card-actions align="around" style="margin-top: -10px">
              <q-btn
                @click="onCancel"
                color="white"
                text-color="negative"
                no-caps
                style="border-radius: 5px; width:45%"
              >
                <q-avatar square size="30px">
                  <q-icon name="cancel"></q-icon>
                </q-avatar>
                <span style="font-size: 15px; margin-left: 10px"> Annuler </span>

                <template v-slot:loading>
                  <q-spinner-facebook />
                </template>
              </q-btn>

              <q-btn
                color="secondary"
                type="submit"
                no-caps
                style="border-radius: 5px; width:45%"
                :loading="submitting"
              >
                <q-avatar square size="30px">
                  <q-icon name="save"/>
                </q-avatar>
                <span style="font-size: 15px; margin-left: 10px"> Sauvegarder </span>
                <template v-slot:loading>
                  <q-spinner-facebook />
                </template>
              </q-btn>
            </q-card-actions>

          <div class="q-ma-sm">

          </div>
        </q-form>
      </div>

    </q-card>

    <!-- Bar de menu -->
    <!-- <MenuBar/> -->

  </q-card>


  <q-dialog v-model="deconnexionMadal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down">
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
        <h4 align="center" class="text-weight-light" style="margin-bottom:35px">DÉCONNEXION</h4>
      </div>

      <q-card-section class="q-ma-md" style="margin-top: 20%">
        <div align="center"  class="text-weight-regular" style="font-size:20px">Voulez-vous vraiment vous déconnecter ?</div>
      </q-card-section>

      <q-card-actions align="around" style="margin-top: 10%">
        <q-btn rounded color="secondary" v-close-popup >
          <span style="font-size: 20px">Non</span>
        </q-btn>
        <q-btn rounded text-color="black" color="grey-2" @click="onOK">
          <span style="font-size: 20px">Oui</span>
        </q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>


  <q-dialog v-model="infoMadal" class="z-max" maximized persistent transition-show="slide-up" transition-hide="slide-down">
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
        <h4 align="center" class="text-weight-light" style="margin-bottom:35px">CONFIRMATION !</h4>
      </div>

      <q-card-section class="q-ma-md" style="margin-top: 20%">
        <div align="center"  class="text-weight-regular" style="font-size:20px">Vous allez etre deconnecté après la modification de votre adresse email.</div>
      </q-card-section>
    </q-card>
  </q-dialog>


  <q-dialog v-model="changePasswordModal"
    transition-show="slide-up"
    transition-hide="slide-down"
    full-width
    seamless
  >
    <change-password/>
  </q-dialog>


</template>

<script>
import { blobToBase64String } from "blob-util";
import { JoolMapping } from "joolmapping";
import { useRouter } from "vue-router"
import { useQuasar } from 'quasar'
import $ from 'jquery'
import { defineComponent, getCurrentInstance, ref, onMounted, onBeforeMount, inject, provide, watch} from "vue";
import ItemActeur from "components/Acteur/Item.vue"
import ChangePassword from "components/Parametre/ChangePassword.vue"
import MenuBar from "components/MenuBar.vue"
export default defineComponent({
  // name: 'ComponentName',

  components: {
    ItemActeur,
    ChangePassword,
    MenuBar
  },

  setup() {
    const instance = getCurrentInstance()
    const router = useRouter()
    const $q = useQuasar()
    const service = "updateUtilisateur";
    let state = inject(service);
    let formData = ref({})
    let eForm = ref(null)
    let deconnexionMadal = ref(false)
    let modifier = ref(false)
    const submitting = ref(false)
    let changePasswordModal = ref(false)
    provide("changePasswordModal", changePasswordModal)
    let file = ref(null)
    let base64String = ref()
    let fileUpload = ref(null)
    let infoMadal = ref(false)
    let getAccount = inject("getAccount")
    let parametreModal = inject("parametreModal")
    let menu = inject("menu")
    let user = ref()


    onBeforeMount( async () => {
      await userInfo()
    })

    let userInfo = async () => {
      user.value = JSON.parse( localStorage.getItem('userAuth-session-app'))
      formData.value = JSON.parse( localStorage.getItem('userAuth-session-app'))
      base64String.value = instance.appContext.app.config.globalProperties.$serverImage+ user.value.imageUrl
    }

    let top = 0
    let drawerPos = ref(0)
    const drawerStyle = () => {
      return {
        bottom: `${drawerPos.value}px`
      }
    }

    const slideDrawer = (ev) => {
      console.log("ev", ev);
      const { height } = $q.screen
      if (top == 0) {
        top = drawerPos.value
      }
      let taille = $(ev.evt.path[2]).height()
      drawerPos.value = drawerPos.value - ev.delta.y

      if (ev.isFinal === true) {
        if (drawerPos.value > top) {
          animateDrawerTo(top)
        }
        else if (drawerPos.value < -taille / 3) {
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

    // Fonction de convertion de base 64
    const getBase64 = async (file) => {
        if (file) {
          let reader = new FileReader();
          let toConvert = '';
          reader.readAsDataURL(file);
          file = null
          return new Promise((resolve, reject) => {
            reader.onload = function () {
              toConvert = reader.result
              reader.abort()
              resolve(toConvert)
            };
          })
        }
      }

    watch(file, async() =>{
      if (file.value) {
        base64String.value = await getBase64(file.value)
      } else {
        base64String.value = app.config.globalProperties.$serverImage + user.imageUrl
      }
    })

    let isValidEmail = (val) => {
      const emailPattern = /^(?=[a-zA-Z0-9@._%+-]{6,254}$)[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,63}$/;
      return emailPattern.test(val) || `Saisissez un email valide`;
    }

    let isValidNumber = (val)=>{
      return val >= 0 || `Merci de saisir au format correct`
    }

    let onLogout = () => {
      console.log("onLogout");
      deconnexionMadal.value = true;
    }

    let onCleanData = async () => {
      let errorHappen = false;
      // Clean user data
      JoolMapping.cleanUserData().catch(err => {
        errorHappen = true;
        console.log('cleanUserData onErr ', err);
      }).then(async () =>{
        if(errorHappen){
          return;
        }else{
          // Cleaning Done
          console.log('cleanUserData succes ');
        }
      });
      return;
    }

    let onOK = async () => {
      $q.loading.show()
      localStorage.clear()
      await onCleanData()
      $q.loading.hide()
      router.push("login")
    }

    let onEdit = () => {
      modifier.value = true
    }

    let onCancel = async () => {
      modifier.value = false
      file.value = null
    }

    let onUpdate = async () => {
      if (file.value) {
        state.filters.value = `
        mutation($id:ID!, $lastName:String, $firstName:String, $telephone:String, $username:String, $imgToBase64:String) {
        updateUtilisateur(newUtilisateur:
          {id:$id
          lastName:$lastName,
          firstName:$firstName,
          username:$username,
          telephone:$telephone,
          imgToBase64:$imgToBase64,
          })
          {
            ok
            utilisateur{
              firstName
              lastName
              username
              sexe
              telephone
              password
              imageUrl
              enterprise{
                id
                socialreason
                adress
              }
            }
            errors {
              field
              messages
            }
          }
        }
      `

      if (formData.value.username ===  user.value.username) {
        submitting.value = true
        base64String.value = await getBase64(file.value)
        formData.value.id = user.value.id
        formData.value.imgToBase64 = base64String
        state.item.value = formData.value;
        state.updateForm.value = eForm.value;
        await state.updateUtilisateur()
        await userInfo()
        submitting.value = false
        modifier.value = false
      } else {
        submitting.value = true
        infoMadal.value = true
        setTimeout(() => {
          infoMadal.value = false
        },2000)
        base64String.value = await getBase64(file.value)
        formData.value.id = user.value.id
        formData.value.imgToBase64 = base64String
        state.item.value = formData.value;
        state.updateForm.value = eForm.value;
        await state.updateUtilisateur()
        submitting.value = false
        modifier.value = false
        localStorage.clear()
        router.push("login")
      }


      } else {
        state.filters.value = `
        mutation($id:ID!, $lastName:String, $firstName:String, $telephone:String, $username:String) {
        updateUtilisateur(newUtilisateur:
          {id:$id
          lastName:$lastName,
          firstName:$firstName,
          username:$username,
          telephone:$telephone,
          })
          {
            ok
            utilisateur{
              firstName
              lastName
              username
              sexe
              telephone
              password
              imageUrl
              enterprise{
                id
                socialreason
                adress
              }
            }
            errors {
              field
              messages
            }
          }
        }
      `
        if ( formData.value.username ===  user.value.username) {
          console.log("Même chose");
          submitting.value = true
          formData.value.id = user.value.id
          state.item.value = formData.value;
          state.updateForm.value = eForm.value;
          await state.updateUtilisateur()
           await userInfo()
          submitting.value = false
          modifier.value = false
        } else {
          console.log("Changé");
          submitting.value = true
          infoMadal.value = true
          setTimeout(() => {
            infoMadal.value = false
          },2000)
          formData.value.id = user.value.id
          state.item.value = formData.value;
          state.updateForm.value = eForm.value;
          await state.updateUtilisateur()
          submitting.value = false
          modifier.value = false
          localStorage.clear()
          router.push("login")
        }

      }
    }

    let onChange = () => {
      console.log("onChange");
      changePasswordModal.value = true
    }

    let onClose = () => {
      menu.value.parametre = false
      parametreModal.value = false
      menu.value.home = true
    }


    // let onUpload = () => {
    //   $refs.file.pickFiles()
    // }

    let captureImage = async () => {
      const image = await Camera.getPhoto({
        quality: 90,
        allowEditing: true,
        resultType: CameraResultType.Uri
      })

      imageSrc.value = image.webPath

      console.log(imageSrc.value);
    }


    return{
      state,
      user,
      formData,
      onLogout,
      deconnexionMadal,
      onOK,
      modifier,
      onEdit,
      isValidEmail,
      onUpdate,
      isValidNumber,
      submitting,
      eForm,
      changePasswordModal,
      onChange,
      base64String,
      file,
      fileUpload,
      onCancel,
      infoMadal,
      getAccount,
      onClose,
      captureImage,
      drawerStyle,
      slideDrawer
      // onUpload
    }
  },

  methods:{
    onUpload(){
      this.$refs.fileUpload.pickFiles();
    }
  }
})
</script>
<style scoped>
.acteur {

  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(5px)
}
</style>
