<template>
  <q-page padding style="
    background-image: url(DroneLight.svg);
    background-repeat: no-repeat;
    background-position: left bottom;
   ">

    <transition
      appear
      enter-active-class="animated fadeInDown"
      leave-active-class="animated zoomOut"
    >
    
      <div class="flex flex-center" style="margin-top: 20%">
        <img
          alt="Jool Plan logo"
          src="JooL_Plan.svg"
        >
      </div>

    </transition>

    <transition
      appear
      enter-active-class="animated zoomIn"
      leave-active-class="animated zoomOut"
    >

      <div class="q-ma-md" >
        <h5 align="center" class="text-weight-light">Réinitialisation <br> de mot de passe </h5>

        <q-form
          @submit="onSubmit"
          class="q-gutter-sm"
          ref="eForm"
        >
          <q-input
            disable
            dense
            rounded 
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.id"
            label="Code email"
            :rules="[ (val) => !!val || 'Le champ ode email est obligatoire']"
          />

          <q-input
            dense
            rounded 
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.password"
            label="Nouveau mot de passe"
            :type="isPwd ? 'password' : 'text'"
            :rules="[ 
              (val) => !!val || 'Le champ nouveau mot de passe est obligatoire',
              (val) => val.length > 7 || 'ce champ doit contenir au moins 8 caractères']"
          >
            <template v-slot:append>
              <q-icon
                :name="isPwd ? 'visibility_off' : 'visibility'"
                class="cursor-pointer text-black"
                @click="isPwd = !isPwd"
              />
            </template>
          </q-input>

          <q-input
            dense
            rounded 
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="confirmPassword"
            label="Confirmer le mot de passe"
            :type="isPwd ? 'password' : 'text'"
            :rules="[ 
              (val) => !!val || 'Le champ confirmation est obligatoire',
              (val) => val.length > 7 || 'ce champ doit contenir au moins 8 caractères']"
          >
            <template v-slot:append>
              <q-icon
                :name="isPwd ? 'visibility_off' : 'visibility'"
                class="cursor-pointer text-black"
                @click="isPwd = !isPwd"
              />
            </template>
          </q-input>

          <div>
            <q-btn color="secondary" type="submit" rounded class="full-width" :loading="submitting">
              <span style="font-size: 20px">Reinitialisation</span> 

              <template v-slot:loading>
                <q-spinner-facebook />
              </template>
            </q-btn>
          </div>
        </q-form>

        <div style="margin-top: 25px">
          <a style="text-decoration: none; float: right;" class="text-secondary" href="/#/login">
            <b>Voulez-vous annuler ?</b>
          </a>
        </div>
      </div>
    </transition>
    

  </q-page>
</template>

<script>
import { useQuasar } from 'quasar'
import { defineComponent, getCurrentInstance, ref, onMounted, inject} from 'vue'
export default defineComponent({
  name: 'PageLogin',

  setup (){
    const $q = useQuasar()
    const instance = getCurrentInstance()
    let formData = ref({})
    let connect = ref(false)
    let isPwd = ref(true);
    const service = "users";
    let state = inject(service);
    let eForm = ref(null)
    const submitting = ref(false)
    let confirmPassword =ref("");

    // ID reçu pas email
    const emailID = localStorage.getItem('jool_Plan-id-email')
    formData.value.id = emailID

    let onSubmit = async () => {
      if (formData.value.password === confirmPassword.value) {
        submitting.value = true
        state.createForm.value = eForm.value;
        await state.resetPassword(formData.value);
        submitting.value = false
      } else {
        $q.notify({
          message: 'Mot de passes differents',
          color: 'negative',
          timeout: 1500,
          position: 'top',
        })
      }
    }
    return{
      formData,
      onSubmit,
      connect,
      isPwd,
      eForm,
      submitting,
      confirmPassword
    }
  }
})
</script>

