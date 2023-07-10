<template>
  <div class="q-pl-xs q-pr-xs q-pt-xs col-6" >
    <q-card flat class="my-card" style="border-radius: 10px;">
      <q-item clickable v-ripple class="q-pa-none q-ma-none" @click="onShow(item)">
        <q-card-section class="text-black q-pa-md">
          <div class="q-mt-xs items-center">
            <img src="Plan.png" style="height: 25px;">
            <span class="q-ml-xs" style="position: relative; top:-5px">
              {{item.libelle}}
            </span>
          </div>
          <div class="text-weight-light q-mt-xs" style="font-size:12px">
            {{$FormatDate(item.createdAt)}}
          </div>
          <div class="q-mt-xs">
            <img src="Duree.png" style="height: 13px;">
            <span class="q-ml-xs text-weight-light">
              03 h
            </span>
          </div>
          <div class="q-mt-xs">
            <img src="NbreImgs.png" style="height: 12px;">
            <span class="q-ml-xs text-weight-light">
              20 images
            </span>
          </div>
        </q-card-section>
      </q-item>
    </q-card>
  </div>
</template>

<script>
import { defineComponent, getCurrentInstance} from "vue";
export default defineComponent({
  // name: 'ComponentName',
  props: {
    item: {
      type: Object,
      default() {
        return {};
      },
    },
  },

  setup () {
    const instance = getCurrentInstance()

    let onShow = (item) => {
      console.log(item);
    }
    return {
      onShow
    }
  }
})
</script>
<style  scoped>
.my-card {
  /* width: 40vw;
  max-width: 40vw; */
  background-color: rgba(177, 177, 177, 0.1)
}
</style>
