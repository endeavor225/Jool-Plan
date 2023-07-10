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
        <h4 align="center" class="text-weight-light">CONNEXION</h4>

        <q-form
          @submit.prevent="onSubmit"
          class="q-gutter-sm"
          ref="eForm"
        >
          <q-input
            dense
            rounded
            standout="bg-grey-3 text-black"
            bg-color="grey-3"
            :input-style="{ color: 'black' }"
            v-model="formData.username"
            type="email"
            label="Email"
            :rules="[ (val) => !!val || 'Le champ nom d\'utilisateur est obligatoire', isValidEmail]"
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

          <div class="q-mt-md">
            <q-checkbox v-model="connect" color="secondary" label="Rester connecté" class="" style="position:relative; top: -11px; float: left;"/>

            <a style="text-decoration: none; float: right;" class="text-secondary" href="/#/forgot-password">
              <b>Mot de passe oublié ?</b>
            </a>
          </div>

          <div>
            <q-btn color="secondary" type="submit" rounded class="full-width" :loading="submitting">
              <span style="font-size: 20px">Connexion</span>

              <template v-slot:loading>
                <q-spinner-facebook />
              </template>
            </q-btn>
          </div>
        </q-form>

        <div align="center" class="q-mt-md">
          <q-btn color="secondary" flat size="lg" label="Inscription" rounded class="fq-mt-md"  @click="$router.push('inscription')" />
        </div>
      </div>
    </transition>
  </q-page>
</template>

<script>
import { useRouter } from "vue-router"
import { useQuasar } from "quasar"
import { defineComponent, getCurrentInstance, ref, onMounted, inject, onBeforeMount} from 'vue'
export default defineComponent({
  name: 'PageLogin',

  setup (){
    const $q = useQuasar()
    const router = useRouter()
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

      /* JoolMapping.ini().catch(err => {
        console.log("error ini", err);
      }).then(async () => {
        console.log("succes ini",);
      }); */

      submitting.value = true
      state.createForm.value = eForm.value;
      let res = await state.auth(formData.value);
      if (res) {
        router.push("accueil")
      }
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

<style scoped>

.loading {
  width: 65px;
  height: 65px;
  position: relative;
  top: -18px
}

</style>

