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
        <h4 align="center" class="text-weight-light">MOT DE PASSE OUBLIÉ !</h4>

        <q-form
          style="margin-top: 10%"
          @submit="onSubmit"
          class="q-gutter-sm"
          ref="eForm"
        >
          <q-input
            dense
            rounded 
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.email"
            label="Entrer votre Email"
            :rules="[ (val) => !!val || 'Le champ email est obligatoire', isValidEmail]"
          />

          <div>
            <q-btn color="secondary" type="submit" rounded class="full-width" :loading="submitting">
              <span style="font-size: 20px">Envoyez</span> 

              <template v-slot:loading>
                <q-spinner-facebook />
              </template>
            </q-btn>
          </div>
        </q-form>

        <div style="margin-top: 25px">
          <a style="text-decoration: none; float: right;" class="text-secondary" href="/#/login">
            <b>Retour à la page de connexion ?</b>
          </a>
        </div>

      </div>
    </transition>
    

  </q-page>
</template>

<script>
import { useQuasar } from 'quasar'
import { defineComponent, getCurrentInstance, ref, onMounted, inject, onBeforeMount} from 'vue'
export default defineComponent({
  //name: 'PageLogin',

  setup (){
    const $q = useQuasar()
    const instance = getCurrentInstance
    let formData = ref({})
    let connect = ref(false)
    let isPwd = ref(true);
    const service = "users";
    let state = inject(service);
    let eForm = ref(null)
    const submitting = ref(false)


    let isValidEmail = (val) => {
      const emailPattern = /^(?=[a-zA-Z0-9@._%+-]{6,254}$)[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,63}$/;
      return emailPattern.test(val) || `Saisissez un email valide`;
    };

    let onSubmit = async () => {
      submitting.value = true
      state.createForm.value = eForm.value;
      await state.forgotPassword(formData.value);
      submitting.value = false
    }
    return{
      formData,
      onSubmit,
      connect,
      isPwd,
      eForm,
      submitting,
      isValidEmail
    }
  }
})
</script>

