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
    
      <div class="flex flex-center" style="margin-top: 10%">
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

      <div class="q-ml-md q-mr-md q-mb-none" >
        <h4 align="center" class="text-weight-light" style="margin-bottom:35px">INSCRIPTION</h4>

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

          <!-- <q-select
            dense
            rounded 
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.sexe"
            input-debounce="0"
            label="Sexe"
            :options="options"
            behavior="menu"
            :rules="[ (val) => !!val || 'Le champ sexe est obligatoire']"
          /> -->
    

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


          <q-input
            dense
            rounded 
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.password"
            label="Mot de passe"
            :type="isPwd ? 'password' : 'text'"
            :rules="[ (val) => !!val || 'Le champ mot de passe est obligatoire']"
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
            label="Confirmation"
            :type="isPwd ? 'password' : 'text'"
            :rules="[ (val) => !!val || 'Le champ confirmation est obligatoire', isConfirm]"
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
              <span style="font-size: 20px">Inscription</span>  
              <template v-slot:loading>
                <q-spinner-facebook />
              </template>
            </q-btn>
          </div>
        </q-form>

        <div align="center" class="q-mt-md">
          <q-btn color="secondary" flat type="submit" size="lg" label="Connexion" rounded class="fq-mt-md" @click="$router.push('login')" />
        </div>
      </div>
    </transition>
    

  </q-page>
</template>

<script>
import { Notify } from 'quasar'
import { defineComponent, getCurrentInstance, ref, onMounted, inject} from 'vue'
export default defineComponent({
  name: 'PageInscription',

  setup (){
    const instance = getCurrentInstance
    let formData = ref({})
    let connect = ref(false)
    let isPwd = ref(true);
    const service = "users";
    let state = inject(service);
    let eForm = ref(null)
    let confirmPassword = ref();
    const submitting = ref(false)
    let options = [{ label: 'Homme', value: '1' }, { label: 'Femme', value: '0' }]

    let isValidEmail = (val) => {
      const emailPattern = /^(?=[a-zA-Z0-9@._%+-]{6,254}$)[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,63}$/;
      return emailPattern.test(val) || `Saisissez un email valide`;
    };

    let isConfirm = (val)=>{
      return val === formData.value.password || `Mot de passe non conforme`
    }

    let isValidNumber = (val)=>{
      return val >= 0 || `Merci de saisir au format correct`
    }

    let onSubmit = async () => {
      submitting.value = true
      state.createForm.value = eForm.value;
      formData.value.email = formData.value.username
      await state.register(formData.value);
      //await state.auth(formData.value);
      submitting.value = false
      
    }
    return{
      formData,
      onSubmit,
      connect,
      isPwd,
      eForm,
      confirmPassword,
      submitting,
      isValidEmail,
      isValidNumber,
      isConfirm,
      options
      
    }
  }
})
</script>
