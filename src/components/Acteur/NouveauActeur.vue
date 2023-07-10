<template>
  <div>
    <div class="q-ma-md text-h6 text-center">Nouvelle parcelle</div>

    <div>
      <q-item clickable v-ripple class="bg-white text-grey-8 q-mb-sm q-ml-sm q-mr-sm" @click="onDelimitation">
        <q-item-section avatar >
          <q-avatar square>
            <img src="Delimiter.svg">
          </q-avatar>
        </q-item-section>
        <q-item-section>
          <q-item-label>Delimiter</q-item-label>
        </q-item-section>
        <q-item-section side>
          <q-icon name="play_circle" color="secondary" />
        </q-item-section>
      </q-item>

      <q-item clickable v-ripple class="bg-white text-grey-8 q-mb-sm q-ml-sm q-mr-sm" @click="onDessin">
        <q-item-section avatar >
          <q-avatar square>
            <img src="Dessiner.svg">
          </q-avatar>
        </q-item-section>
        <q-item-section>
          <q-item-label>Dessiner</q-item-label>
        </q-item-section>
        <q-item-section side>
          <q-icon name="play_circle" color="secondary" />
        </q-item-section>
      </q-item>
    
      <q-item clickable v-ripple class="bg-white text-grey-8 q-mb-md q-ml-sm q-mr-sm" @click="onUpload">
        <q-item-section avatar >
          <q-avatar square>
            <img src="KML.svg">
          </q-avatar>
        </q-item-section>
        <q-item-section>
          <q-item-label>Utiliser un fichier KML</q-item-label>
        </q-item-section>
        <q-item-section side>
          <q-icon name="play_circle" color="secondary" />
        </q-item-section>
      </q-item>
    </div>
  </div>
</template>

<script>
import { useRouter } from "vue-router"
import {defineComponent, getCurrentInstance, ref, onMounted, inject, provide} from "vue";
export default defineComponent({
  // name: 'ComponentName',
  setup () {
    let instance = getCurrentInstance();
    const router = useRouter()
    let mymap = inject("mymap")
    let layerGroup = inject("layerGroup")
    provide("layerGroup", layerGroup)

    let onDelimitation = () => {
      mymap.value.removeLayer(layerGroup.value);
      router.push('delimitation')
    }

    let onDessin = () => {
      mymap.value.removeLayer(layerGroup.value);
      router.push('dessiner')
    }

    let onUpload = () => {
      mymap.value.removeLayer(layerGroup.value);
      router.push('delimitation-kml')
    }
    return {
      onDelimitation,
      onDessin,
      onUpload,
    }
  }
})
</script>

