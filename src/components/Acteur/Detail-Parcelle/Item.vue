<template>
  <div class="q-mt-xs q-pt-xs">
    <q-item class="bg-white " style="border-radius: 10px" >
      <q-item-section avatar class="q-gutter-xs">
        <q-avatar square size="20px">
          <img src="Map.svg">
        </q-avatar>
      </q-item-section>
      <q-item-section class="">
        <q-item-label class="q-pl-none" lines="1"> {{item.libelle}} </q-item-label>
      </q-item-section>
      <q-item-section side >
        <q-item-label class="q-pl-none text-secondary"> {{item.projetPlan.length}} Plan(s) </q-item-label>
        <!-- <div class="text-grey-8 q-gutter-xs">
          <q-btn size="sm" flat dense round icon="delete" color="secondary" @click="onDelete(item)"/>
        </div> -->
      </q-item-section>
    </q-item>
  </div>
</template>

<script>
import { defineComponent, getCurrentInstance, ref, onMounted, inject,} from "vue";
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

    let onDelete = (item) => {
      console.log("onDelete",item);
    }
    return {
      onDelete
    }
  }
})
</script>
