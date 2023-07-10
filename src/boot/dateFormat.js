import { boot } from 'quasar/wrappers'
import { date } from 'quasar'


// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files

  export default async ({app}) => {

    const dateFormater = (params) => {
      return date.formatDate(params, 'DD/MM/YYYY')
    }
    app.config.globalProperties.$FormatDate = dateFormater


    const dateFormat = (params) => {
      return date.formatDate(params, "ddd. DD MMM. HH:mm")
    }
    app.config.globalProperties.$dateFormat = dateFormat

  }

  