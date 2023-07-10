
const routes = [
  {
    path: '/',
    component: () => import('layouts/LoginLayaout.vue'),
    children: [
      { path: '', component: () => import('pages/Index.vue') },
      { path: 'login', component: () => import('src/pages/Login.vue') },
      { path: 'inscription', component: () => import('pages/Inscription.vue') },
      { path: 'accueil', component: () => import('pages/Accueil.vue') },
      { path: 'delimitation', component: () => import('pages/Delimitation.vue') },
      { path: 'dessiner', component: () => import('pages/Dessiner.vue') },
      { path: 'delimitation-kml', component: () => import('pages/DelimitationKml.vue') },
      { path: 'mission', name: 'mission', props: true, component: () => import('src/pages/Mission.vue') },
      { path: 'forgot-password', component: () => import('pages/ForgotPassword.vue') },
      { path: 'reset-password', component: () => import('pages/ResetPassword.vue') },
      { path: 'parcellisation', name: 'parcellisation', props: true, component: () => import('pages/Parcellisation.vue') },
      { path: 'test', component: () => import('pages/test.vue') },
    ]
  },

  // {
  //   path: '/',
  //   component: () => import('layouts/MainLayout.vue'),
  //   children: [
  //     { path: '', component: () => import('pages/Index.vue') }
  //   ]
  // },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/Error404.vue')
  }
]

export default routes
