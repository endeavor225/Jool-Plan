import { ref, getCurrentInstance } from 'vue'
import jwt_decode from "jwt-decode";
export class Ressource {
  limit = 10
  start = 0
  loading = false
  self = getCurrentInstance()
  api = this.self.appContext.app.config.globalProperties.$api
  user = this.self.appContext.app.config.globalProperties.$sdata.value.user
  notify = this.self.appContext.app.config.globalProperties.$notify
  constructor(service) {
    this.service = service
    this.items = ref([])
    this.item = ref({})
    this.filters = ref("")
    this.createForm = ref({})
    this.updateForm = ref({})
    this.deleteForm = ref({})
    this.searchForm = ref({})
    this.totalItem = ref(0)
    this.itemObjet = ref({})
  }

  async getItems(append) {
    const token =  localStorage.getItem('jool_Plan_id-session-app')
    try {
      if (this.service === "searchVarieteParCulture") {
        let offset = 0
        let totalCount = 0
        let results = []
        let res;
        while (offset <= totalCount) {
          this.filters.value = `
            query {
              searchVarieteParCulture (deleted: false) {
              results( offset:${offset},limit:1000, ordering:"libelle") {
                  id
                  libelle
              }
              totalCount
              }
            }
          `
          res = await this.api({
            url: `/`,
            method: "post",
            headers: {
                Authorization: `JWT ${token}`
            },
            data: {
              query: `${this.filters.value}`
            },
          })

          offset += 50
          totalCount = res.data.data.searchVarieteParCulture.totalCount
          console.log("res.data",res.data.data);

          for (let index = 0; index < res.data.data.searchVarieteParCulture.results.length; index++) {
            const element = res.data.data.searchVarieteParCulture.results[index];
            results.push(element)
          }
        }
        this.items.value = results

        if (res.data.data.searchVarieteParCulture.results) {
            localStorage.setItem('searchVarieteParCulture', JSON.stringify(this.items.value))
        }

      } else if (this.service === "getAccountGeneralInfos" || this.service === "base64ToGeojson") {
        let res = await this.api({
          url: `/`,
          method: "post",
          headers: {
              Authorization: `JWT ${token}`
          },
          data: {
            query: `${this.filters.value}`,
          },
        })
        this.itemObjet.value = res.data
        console.log("res.data", res.data.data);

        if(res.data.data.getAccountGeneralInfos){
          localStorage.setItem('get-Account-General-Infos', JSON.stringify(res.data.data.getAccountGeneralInfos))
        }
      } else  {
        let res = await this.api({
          url: `/`,
          method: "post",
          headers: {
              Authorization: `JWT ${token}`
          },
          data: {
            query: `${this.filters.value}`,

          },
        })
        console.log("res.data", res.data.data);

        if (this.service === "searchActeurs" && res.data.data.searchActeurs.results) {
          localStorage.setItem('searchActeurs', JSON.stringify(res.data.data.searchActeurs.results))
        }
        if (this.service === "searchProjet" && res.data.data.searchProjet.results) {
          localStorage.setItem('searchProjet', JSON.stringify(res.data.data.searchProjet.results))
        }

        if (res && res.data && res.data.data && res.data.data[this.service] && this.items) {
          if (append) {
            let not_in = true
            for (const element of res.data.data[this.service].results) {
              for (const item of this.items.value) {
                if (element.id === item.id) {
                  not_in = false
                  break
                }
              }
              if (not_in) {
                this.items.value.push(element)
              } else {
                not_in = true
              }
            }
          } else {
            if (Array.isArray(res.data.data[this.service].results)) {
              this.items.value = res.data.data[this.service].results
            } else {
              this.items.value = Object.values(res.data.data[this.service].results)[0]
            }
            var nb = this.items.value.length
            this.totalItem.value = Math.ceil(nb / this.limit)
          }
        }
      }
    } catch (error) {
      console.log(error);
    }
  }

  async create() {
    const token =  localStorage.getItem('jool_Plan_id-session-app')
    if (this.createForm.value.validate()) {

      if (this.service === "createActeurs") {
        console.log("this.filters.value", this.filters.value);
        try {
          let res = await this.api({
            url: `/`,
            method: "post",
            headers: {
              Authorization: `JWT ${token}`
            },
            data: {
              query: `${this.filters.value}`,
              variables: {
                libelle: this.item.value.libelle,
                description: this.item.value.description,
                enterprise : this.item.value.enterprise,
                pere : this.item.value.pere
              }
            },
          })
          console.log("Acteur create |==> ", res.data);
          if (this.item.value.varieteCulture) {
            for (let index = 0; index < this.item.value.varieteCulture.length; index++) {
              const element = this.item.value.varieteCulture[index];

              let resActeurVariete = await this.api({
                url: `/`,
                method: "post",
                headers: {
                    Authorization: `JWT ${token}`
                },
                data: {
                  query: `
                    mutation($acteur:ID, $varietesCultures:ID) {
                      createActeursVarietesCultures(newActeursvarietescultures : {acteur:$acteur, varietesCultures:$varietesCultures}) {
                        ok
                        acteursvarietescultures{
                          id
                        }
                        errors {
                          field
                          messages
                        }
                      }
                    }
                  `,
                  variables: {
                    acteur: res.data.data.createActeurs.acteurs.id,
                    varietesCultures: element.id
                  }
                },
              })
              console.log("Varietes-Cultures create |==> ", resActeurVariete.data);
            }
          }

          //Enregistrement de polygone
          if (this.item.value.polygon) {
            let resPolygone= await this.api({
              url: `/`,
              method: "post",
              headers: {
                  Authorization: `JWT ${token}`
              },
              data: {
                query: `
                  mutation($acteur:ID, $geometrie:String, $shp:String, $kml:String, $superficie:Float) {
                    createPolygone(newPolygone: {acteur:$acteur, geometrie:$geometrie, kml:$kml, shp:$shp, superficie:$superficie}) {
                      ok
                      polygone{
                        id
                      }
                      errors {
                        field
                        messages
                      }
                    }
                  }
                `,
                variables: {
                  acteur: res.data.data.createActeurs.acteurs.id,
                  geometrie: this.item.value.polygon,
                  superficie: this.item.value.superficie,
                  kml: "",
                  shp: ""
                }
              },
            })
            console.log("Polygone create |==> ", resPolygone.data);
          }

          if (this.item.value.kml) {
            console.log(this.item.value);
            let resPolygone= await this.api({
              url: `/`,
              method: "post",
              headers: {
                  Authorization: `JWT ${token}`
              },
                data: {
                  query: `
                    mutation($acteur:ID, $kml:String, $shp:String, $geometrie:String) {
                      createPolygone(newPolygone: {acteur:$acteur, kml:$kml, shp:$shp, geometrie:$geometrie}) {
                        ok
                        polygone{
                          id
                        }
                        errors {
                          field
                          messages
                        }
                      }
                    }
                  `,
                  variables: {
                    acteur: res.data.data.createActeurs.acteurs.id,
                    kml: this.item.value.kml,
                    shp: "",
                    geometrie: ""
                  }
                },
              })
              console.log("Polygone KML create |==> ", resPolygone.data);
          }
          this.notify('Opération réussie!', 'positive')
          return true
        } catch (error) {
          this.notify('Opération echouée!', 'negative')
          console.log(error);
        }
      }

      if (this.service === "createProjet") {
        try {
          let res = await this.api({
            url: `/`,
            method: "post",
            headers: {
                Authorization: `JWT ${token}`
            },
            data: {
              query: `${this.filters.value}`,
              variables: {
                libelle: this.item.value.libelle,
                description: this.item.value.description,
                acteur: this.item.value.parcelle.id,
              }
            },
          })
          console.log("Projet create |==> ", res.data);
          this.notify('Opération réussie!', 'positive')
          return res.data
        } catch (error) {
          this.notify('Opération echouée!', 'negative')
          console.log(error);
        }
      }

      if (this.service === "createPlan") {
        console.log("this.filters.value",this.filters.value);
        try {
          let res = await this.api({
            url: `/`,
            method: "post",
            headers: {
                Authorization: `JWT ${token}`
            },
            data: {
              query: `${this.filters.value}`,
              variables: {
                libelle: this.item.value.libelle,
                altitude: this.item.value.altitude,
                projet: this.item.value.projet,
                polygone: this.item.value.polygone,
              }
            },
          })
          console.log("Plan create |==> ", res.data);
          this.notify('Opération réussie!', 'positive')
          return res.data
        } catch (error) {
          this.notify('Opération echouée!', 'negative')
          console.log(error);
        }
      }
    } else {
      this.notify('Opération echouée!', 'negative')
    }
  }

  // Fonction de mis a jour
  async update() {
    const token =  localStorage.getItem('jool_Plan_id-session-app')
    if (this.service === "updatePlan") {
      try {
        let res = await this.api({
          url: `/`,
          method: "post",
          headers: {
              Authorization: `JWT ${token}`
          },
          data: {
              query: ` ${this.filters.value}`,
              variables: this.item.value
          },
        })
          console.log(this.service,"succes |==> ", res.data.data);
          return res.data.data[this.service].ok
      } catch (error) {
        console.log(error);
      }
    } else {
      if (this.updateForm.value.validate()) {
        try {
          let res = await this.api({
              url: `/`,
              method: "post",
              headers: {
                  Authorization: `JWT ${token}`
              },
              data: {
                  query: ` ${this.filters.value}`,
                  variables: this.item.value
              },
          })

            console.log(this.service,"succes |==> ", res.data.data);

            if (this.item.value.acteurVarieteCulture) {
              for(const item of this.item.value.acteurVarieteCulture) {
                let resDelete = await this.api({
                  url: `/`,
                  method: "post",
                  headers: {
                      Authorization: `JWT ${token}`
                  },
                  data: {
                    query: `mutation($id:UUID, $deleted:Boolean) {
                      updateActeursVarietesCultures(newActeursvarietescultures:
                        {
                          id:$id
                          deleted:$deleted
                        })
                        {
                          ok
                          acteursvarietescultures{
                            id
                          }
                          errors {
                            field
                            messages
                        }
                      }
                    }`,
                    variables: {
                      id : item.id,
                      deleted: true
                    }
                  },
                })
                console.log("Acteur update |==> ", resDelete.data.data);
              }
            }


            if (this.item.value.newvarieteCulture) {
              for(const item of this.item.value.newvarieteCulture){
                let resAVariete = await this.api({
                  url: `/`,
                  method: "post",
                  headers: {
                      Authorization: `JWT ${token}`
                  },
                  data: {
                    query: `
                      mutation($acteur:ID, $varietesCultures:ID) {
                        createActeursVarietesCultures(newActeursvarietescultures : {acteur:$acteur, varietesCultures:$varietesCultures}) {
                          ok
                          acteursvarietescultures{
                            id
                          }
                          errors {
                            field
                            messages
                          }
                        }
                      }
                    `,
                    variables: {
                      acteur:  this.item.value.id,
                      varietesCultures: item.id
                    }
                  },
                })
                console.log("Varietes-Cultures create |==> ", resAVariete.data);
              }
            }
            this.notify('Modification effectuée!', 'positive')
            return res.data.data[this.service].ok
        } catch (error) {
          this.notify('Modification echouée!', 'negative')
          console.log(error);
        }
      }
    }
  }

  // Fonction de supression
  async remove() {
    const token =  localStorage.getItem('jool_Plan_id-session-app')
    try {
      let res = await this.api({
        url: `/`,
        method: "post",
        headers: {
            Authorization: `JWT ${token}`
        },
        data: {
          query: ` ${this.filters.value}`,
          variables: this.item.value
        },
      })
      this.notify('Supression effectuée!', 'positive')
      console.log("Acteur update |==> ", res.data.data);
      return res.data.data[this.service].ok
    } catch (error) {
      this.notify('Supression echouée!', 'negative')
      console.log(error);
    }
  }

  // Fonction de connexion
  async auth(form) {
    if (this.createForm.value.validate()) {
      try {
        console.log("form", form);
        let res = await this.api({
          url: `/`,
          method: "post",
          data: {
            query: `
              mutation($username:String!, $password:String!){
              authToken(username: $username, password: $password){
                token
              }
            }
            `,
              // variables: form
            variables: {
                username: form.username,
                password: form.password
            }
          },
        })

        console.log("connexion", res.data);

        if (res && res.data && res.data.errors) {
          this.notify('Connexion echouée!', 'negative')
        } else {
          console.log( res.data);
          localStorage.setItem('jool_Plan_id-session-app',  res.data.data.authToken.token)
          var token_jwt = jwt_decode(res.data.data.authToken.token);
          console.log("token", token_jwt.username);

          let userAuth = await this.api({
            url: `/`,
            method: "post",
            headers: {
                Authorization: `JWT ${res.data.data.authToken.token}`
            },
            data: {
              query: `
                query($username:String){
                  searchUser(username:$username){
                    results{
                      utilisateur{
                        id
                        sexe
                        telephone
                        username
                        email
                        firstName
                        lastName
                        password
                        imageUrl
                        enterprise{
                          id
                        }
                      }
                      userPermissions{
                        id
                        codename
                      }
                    }
                  }
                }
              `,
              variables: {
                username: token_jwt.username
              }
            },
          })
          console.log("userAuth", userAuth.data.data.searchUser.results[0].utilisateur);
          localStorage.setItem('userAuth-session-app', JSON.stringify(userAuth.data.data.searchUser.results[0].utilisateur))
          //this.createForm.value.$router.push('accueil')
          return true
        }
      } catch (error) {
        this.notify('Connexion echouée!', 'negative')
        console.log(error);
      }
    }
  }

  // Fonction d'inscription
  async register(form) {
    if (this.createForm.value.validate()) {
      try {
          console.log("form", form);
          let res = await this.api({
            url: `/`,
            method: "post",
            data: {
              query: `
                mutation($lastName:String!, $firstName:String!, $username:String!, $password:String!, $email:String!, $telephone:String!) {
                  createUtilisateur(newUtilisateur: {lastName:$lastName, firstName:$firstName, username: $username, email: $email, password: $password, telephone: $telephone}) {
                    ok
                    errors {
                      field
                      messages
                    }
                    utilisateur{
                      firstName
                      lastName
                      username
                      sexe
                      telephone
                      password
                      enterprise{
                        id
                        socialreason
                        adress
                      }
                    }
                  }
                }
              `,
              variables: form
            },
          })
          console.log("inscription", res.data);
          if (res.data && res.data.data && res.data.data.createUtilisateur && res.data.data.createUtilisateur.ok) {
            this.notify('Inscription réussie!', 'positive')
            this.createForm.value.$router.push('accueil')
          } else {
            this.notify('Inscription echouée!', 'negative')
          }
      } catch (error) {
        this.notify('Inscription echouée!', 'negative')
        console.log(error);
      }
    }
  }

  // Fonction de mis a jour de l'utilisateur
  async updateUtilisateur() {
    const token =  localStorage.getItem('jool_Plan_id-session-app')
    if (this.updateForm.value.validate()) {
      try {
        let res = await this.api({
          url: `/`,
          method: "post",
          headers: {
            Authorization: `JWT ${token}`
          },
          data: {
            query: ` ${this.filters.value}`,
            variables: this.item.value
          },
        })
        this.notify('Modification effectuée!', 'positive')
        console.log("utilisateur update |==> ", res.data.data.updateUtilisateur);

        if (res.data.data.updateUtilisateur.ok) {
          let userAuth = await this.api({
            url: `/`,
            method: "post",
            headers: {
              Authorization: `JWT ${token}`
            },
            data: {
              query: `
                query($username:String){
                  searchUser(username:$username){
                    results{
                      utilisateur{
                        id
                        sexe
                        telephone
                        username
                        email
                        firstName
                        lastName
                        password
                        imageUrl
                        enterprise{
                          id
                        }
                      }
                      userPermissions{
                        id
                        codename
                      }
                    }
                  }
                }
              `,
              variables: {
                username: res.data.data.updateUtilisateur.utilisateur.username
              }
            },
          })
          localStorage.setItem('userAuth-session-app', JSON.stringify(userAuth.data.data.searchUser.results[0].utilisateur))
        }
      } catch (error) {
        //this.notify('Modification echouée!', 'negative')
        console.log(error);
      }
    }
  }

  // Fonction pour changé de mot de passe
  async changeUserPassword () {
    const token =  localStorage.getItem('jool_Plan_id-session-app')
    if (this.updateForm.value.validate()) {
      try {
        let res = await this.api({
        url: `/`,
        method: "post",
        headers: {
          Authorization: `JWT ${token}`
        },
        data: {
          query: ` ${this.filters.value}`,
          variables: this.item.value
        },
      })
      console.log("Password change |==> ", res.data.data);
      if (res.data.data.changeUserPassword.response) {
        this.notify('Modification effectuée!', 'positive')
      } else {
        this.notify('Modification echouée!', 'negative')
      }
      } catch (error) {
        this.notify('Modification echouée!', 'negative')
        console.log(error);
      }
    }
  }

  // Fonction de mot de passe oublié
  async forgotPassword(form) {
    if (this.createForm.value.validate()) {
      try {
        let res = await this.api({
          url: `/`,
          method: "post",
          data: {
            query: `
              mutation($email:String!){
                createForgotPassword(newForgotpassword:{ email:$email}){
                  forgotpassword{
                    id
                    isValidate
                  }
                }
              }
            `,
            variables: form
          },
        })
        localStorage.setItem('jool_Plan-id-email',  res.data.data.createForgotPassword.forgotpassword.id)

        if (res && res.data && res.data.errors) {
          this.notify('Operation echouée!', 'negative')
        } else {
          this.notify('Operation réussie!', 'positive')
          this.createForm.value.$router.push('reset-password')
        }
      } catch (error) {
        this.notify('Operation echouée!', 'negative')
        console.log(error);
      }
    }
  }

  // Fonction de restauration de mot de passe
  async resetPassword(form) {
    if (this.createForm.value.validate()) {
      try {
        let res = await this.api({
          url: `/`,
          method: "post",
          data: {
            query: `
              mutation($id:String!, $password:String!){
                reinitializePassword(
                  id:$id,
                  newPassword:$password
                ){
                  response
                }
              }
            `,
            variables: form
          },
        })

        console.log("Réinitialisation Mot de passe", res.data.data);
        localStorage.clear('jool_Plan-id-email')

        if (res && res.data && res.data.errors) {
          this.notify('Réinitialisation echouée!', 'negative')
        } else {
          this.notify('Réinitialisation réussie!', 'positive')
          this.createForm.value.$router.push('login')
        }
      } catch (error) {
        this.notify('Réinitialisation echouée!', 'negative')
        console.log(error);
      }
    }
  }
}
