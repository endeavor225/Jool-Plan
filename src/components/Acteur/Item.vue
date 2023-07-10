<template>
  <div>
     <q-item clickable v-ripple class="bg-white q-mb-sm q-ml-lg q-mr-lg q-pa-md" style="border-radius: 10px" @click="onShow">
        <q-item-section>
          <q-item-label > {{item.libelle}} </q-item-label>
          <q-item-label caption> {{ $FormatDate(item.createdAt)}} </q-item-label>
        </q-item-section>
        <q-item-section side>
           <q-item-label class="text-secondary"> {{ item.acteurPoly[0].superficie.toFixed(2) }} ha </q-item-label>
        </q-item-section>
      </q-item>
  </div>

  <q-dialog v-model="showModal"
    position="bottom"
    full-width
    persistent
    transition-show="slide-up"
    transition-hide="slide-down"
    class="z-max"
  >
    <show-detail :showItem="item"/>
  </q-dialog>

</template>

<script>
import { defineComponent, getCurrentInstance, ref, onMounted, inject, provide, watch} from "vue";
import ShowDetail from "src/components/Acteur/Detail-Parcelle/Item.vue"
export default defineComponent({
  // name: 'ComponentName',
  components:{
    ShowDetail
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
    let showModal = ref(false);
    provide("showModal", showModal)

    let etatShow = inject("etatShow")
    console.log("etatShow",etatShow.value);

    let style = inject("style")

    watch(showModal, async () => {
      if (showModal.value) {
        etatShow.value = true
      } else {
        etatShow.value = false
      }
    });


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
