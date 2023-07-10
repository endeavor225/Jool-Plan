<template>
  <q-card class="modal">
    <div clickable class="q-ma-md text-center" style="margin-left:80px; margin-right:80px">
      <q-separator size="5px" color="grey-8" inset @click="onClose" />
    </div>

    <div class="q-ma-lg text-h6 text-center">Modification mot de passe</div>

    <div class="q-ma-md" >
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
          v-model="formData.actualPassword"
          label="Ancien mot de passe"
          :type="isPwd ? 'password' : 'text'"
          :rules="[ (val) => !!val || 'Le champ ancien mot de passe est obligatoire']"
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
          v-model="formData.newPassword"
          label="Nouveau mot de passe"
          :type="isPwd ? 'password' : 'text'"
          :rules="[ (val) => !!val || 'Le champ nouveau mot de passe est obligatoire']"
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
          label="Confirmer mot de passe"
          :type="isPwd ? 'password' : 'text'"
          :rules="[ (val) => !!val || 'Le champ confirmer mot de passe est obligatoire', isConfirm]"
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
import { useRouter } from 'vue-router'
import { ref, defineComponent, getCurrentInstance, inject, onMounted } from "vue";
export default defineComponent({
  // name: 'ComponentName',
  setup() {
    const route = useRouter()
    const instance = getCurrentInstance()
    let eForm = ref(null)
    let formData = ref({})
    const service = "changeUserPassword";
    let state = inject(service);
    let submitting = ref(false)
    let isPwd = ref(true)
    let confirmPassword = ref()
    let changePasswordModal = inject("changePasswordModal")
    const user = JSON.parse( localStorage.getItem('userAuth-session-app'))

    let isConfirm = (val)=>{
      return val === formData.value.newPassword || `Mot de passe non conforme`
    }

    let onSubmit = async () => {
      state.filters.value = `
      mutation($username:String!, $actualPassword:String!, $newPassword:String!) {
        changeUserPassword(
          username:$username,
          actualPassword:$actualPassword,
          newPassword:$newPassword
          ) 
          {
            response
        }
      } 
      `

      submitting.value = true
      formData.value.username = user.username
      state.item.value = formData.value;
      state.updateForm.value = eForm.value;
      await state.changeUserPassword()
      submitting.value = false
      changePasswordModal.value = false
    }

    let onClose = () => {
      changePasswordModal.value = false
    }

    return {
      formData,
      eForm,
      onSubmit,
      submitting,
      onClose,
      isPwd,
      isConfirm,
      confirmPassword
    };
  },
});
</script>

<style scoped>
.modal {
  width:100%;
  border-radius: 15px; 
  position: relative;
  bottom: -10%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>

