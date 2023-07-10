<template>
  <div>
     <q-item clickable v-ripple class="bg-white q-mb-sm q-ml-lg q-mr-lg q-pa-md" style="border-radius: 10px" @click="onShow">
        <q-item-section>
          <q-item-label > {{item.libelle}} </q-item-label>
          <q-item-label caption> {{ $FormatDate(item.createdAt)}} </q-item-label>
        </q-item-section>
        <q-item-section side>
           <q-item-label class="text-secondary"> {{ item.projetPlan.length }} Plan(s)</q-item-label>
        </q-item-section>
      </q-item>
  </div>


  <q-dialog v-model="showModal"
    transition-show="slide-up" 
    transition-hide="slide-down"
    full-width
    persistent
  >
    <show-detail :showItem="item"/>
  </q-dialog>

</template>

<script>
import { defineComponent, getCurrentInstance, ref, onMounted, inject, provide, watch} from "vue";
import ShowDetail from "components/Cartographie/ShowItem.vue"
export default defineComponent({
  // name: 'ComponentName',
  components:{
    ShowDetail,
  },
  
  props: {
    item: {
      type: Object,
      default() {
        return {};
      },
    },
  },
  
  setup () {
    let instance = getCurrentInstance()
    let showModal = ref(false)
    provide("showModal", showModal)


    let onShow = () => {
      showModal.value = true
    }
    return {
      onShow,
      showModal,
      
    }
  }
})
</script>
<style scoped>
.modal {
  width: 100%;
  border-radius: 15px; 
  position: relative;
  bottom: -20%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(3px)
}
</style>