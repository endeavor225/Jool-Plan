import { Notify } from 'quasar'
import { boot } from 'quasar/wrappers'

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async( /* { app, router, ... } */ { app }) => {
    // something to do
    app.config.globalProperties.$notify = (message, color) => {
        Notify.create({
            message,
            color,
            timeout: 1500,
            position: 'top',
            actions: [{ icon: 'close', color: 'white' }]
        })
    }
})