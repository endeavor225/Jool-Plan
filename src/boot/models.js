import { boot } from 'quasar/wrappers'

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async( /* { app, router, ... } */ { app }) => {
    // something to do
    app.config.globalProperties.$models = [
        "users",
        "searchVarieteParCulture",
        "searchActeurs",
        "createActeurs",
        "updateActeurs",
        "searchProjet",
        "createProjet",
        "updateProjet",
        "searchPlan",
        "createPlan",
        "updatePlan",
        "updateUtilisateur",
        "changeUserPassword",
        "getAccountGeneralInfos",
        "base64ToGeojson",
        "searchPolygoneFichiersKml",
    ]
})
