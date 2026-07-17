import type { DriveStep } from 'driver.js';

export const TOUR_STEPS: Record<string, DriveStep[]> = {
  '/internal/bandeja': [
    {
      element: '[data-tour="bandeja-toolbar"]',
      popover: {
        title: 'Búsqueda y filtros',
        description: 'Buscá por nombre del niño/a o tutor. Filtrá por estado del formulario para ver solo los pendientes, contactados, etc. Presioná / para enfocar la búsqueda desde el teclado.',
      },
    },
    {
      element: '[data-tour="bandeja-tabla"]',
      popover: {
        title: 'Formularios recibidos',
        description: 'Cada fila es un formulario de interés enviado por una familia. Hacé clic en el nombre para ver el detalle completo.',
      },
    },
    {
      element: '[data-tour="bandeja-acciones"]',
      popover: {
        title: 'Acciones',
        description: 'Desde el menú ⋮ podés contactar, admitir o descartar cada formulario según su estado actual.',
      },
    },
    {
      element: '[data-tour="bandeja-paginador"]',
      popover: {
        title: 'Paginación',
        description: 'Navegá entre páginas de resultados. La tabla muestra 20 formularios por página.',
      },
    },
  ],

  '/internal/pacientes': [
    {
      element: '[data-tour="pacientes-nuevo"]',
      popover: {
        title: 'Registrar paciente',
        description: 'Abre el formulario para registrar un nuevo paciente en el sistema. También podés usar Alt+N.',
      },
    },
    {
      element: '[data-tour="pacientes-toolbar"]',
      popover: {
        title: 'Búsqueda y filtros',
        description: 'Buscá por nombre, apellido o código. Filtrá por estado clínico o tipo (caso problema / control). Presioná / para enfocar la búsqueda desde el teclado.',
      },
    },
    {
      element: '[data-tour="pacientes-tabla"]',
      popover: {
        title: 'Listado de pacientes',
        description: 'Cada fila es un paciente. Hacé clic en el nombre para ver su historia clínica completa.',
      },
    },
    {
      element: '[data-tour="pacientes-acciones"]',
      popover: {
        title: 'Acciones',
        description: 'Desde el menú ⋮ podés ver el detalle, editar los datos o dar de baja al paciente.',
      },
    },
    {
      element: '[data-tour="pacientes-paginador"]',
      popover: {
        title: 'Paginación',
        description: 'Navegá entre páginas. La tabla muestra 20 pacientes por página.',
      },
    },
  ],

  '/internal/sueros': [
    {
      element: '[data-tour="sueros-nuevo"]',
      popover: {
        title: 'Registrar suero',
        description: 'Registrá un nuevo suero con su valor de anticuerpos, volumen y ubicación en freezer. También podés usar Alt+N.',
      },
    },
    {
      element: '[data-tour="sueros-crear-pool"]',
      popover: {
        title: 'Crear pool',
        description: 'Seleccioná sueros del mismo tipo y rango BTU para habilitarlo. El pool combina varios sueros para inocular modelos animales.',
      },
    },
    {
      element: '[data-tour="sueros-toolbar"]',
      popover: {
        title: 'Búsqueda y filtros',
        description: 'Buscá por código de suero. Filtrá por rango BTU, tipo de uso u otros criterios. Presioná / para enfocar la búsqueda desde el teclado.',
      },
    },
    {
      element: '[data-tour="sueros-tabla"]',
      popover: {
        title: 'Listado de sueros',
        description: 'Cada fila muestra el código, el rango BTU, el valor de anticuerpos y el volumen restante del suero.',
      },
    },
    {
      element: '[data-tour="sueros-acciones"]',
      popover: {
        title: 'Acciones',
        description: 'Ver detalle, editar o dar de baja el suero desde el menú ⋮.',
      },
    },
    {
      element: '[data-tour="sueros-paginador"]',
      popover: {
        title: 'Paginación',
        description: 'Navegá entre páginas. La tabla muestra 20 sueros por página.',
      },
    },
  ],

  '/internal/pools': [
    {
      element: '[data-tour="pools-registrar"]',
      popover: {
        title: 'Registrar pool',
        description: 'Creá un nuevo pool de sueros del mismo tipo y rango BTU. También podés usar Alt+N.',
      },
    },
    {
      element: '[data-tour="pools-crear-raton"]',
      popover: {
        title: 'Crear ratón',
        description: 'Se habilita cuando todos los pools seleccionados son del mismo tipo y el mismo rango BTU.',
      },
    },
    {
      element: '[data-tour="pools-disponibilidad"]',
      popover: {
        title: 'Disponibilidad de sueros',
        description: 'Resumen de sueros disponibles por tipo y rango BTU, con los ml disponibles y la cantidad de ratones posibles a inocular.',
      },
    },
    {
      element: '[data-tour="pools-toolbar"]',
      popover: {
        title: 'Búsqueda y filtros',
        description: 'Buscá por código de pool. Filtrá por tipo, rango u otros criterios. Presioná / para enfocar la búsqueda desde el teclado.',
      },
    },
    {
      element: '[data-tour="pools-tabla"]',
      popover: {
        title: 'Listado de pools',
        description: 'Cada fila muestra el código, tipo, rango, volumen restante y cantidad de aportes y ratones asociados.',
      },
    },
    {
      element: '[data-tour="pools-acciones"]',
      popover: {
        title: 'Acciones',
        description: 'Ver detalle, editar o dar de baja el pool desde el menú ⋮.',
      },
    },
    {
      element: '[data-tour="pools-paginador"]',
      popover: {
        title: 'Paginación',
        description: 'Navegá entre páginas. La tabla muestra 20 pools por página.',
      },
    },
  ],

  '/internal/modelos-animales': [
    {
      element: '[data-tour="modelos-nuevo"]',
      popover: {
        title: 'Registrar ratón',
        description: 'Registrá un nuevo modelo animal con su camada, pool asignado e identificador. También podés usar Alt+N.',
      },
    },
    {
      element: '[data-tour="modelos-toolbar"]',
      popover: {
        title: 'Búsqueda y filtros',
        description: 'Buscá por código identificador o nombre de camada. Filtrá por estado del protocolo o rango. Presioná / para enfocar la búsqueda desde el teclado.',
      },
    },
    {
      element: '[data-tour="modelos-tabla"]',
      popover: {
        title: 'Listado de modelos animales',
        description: 'Cada fila muestra el identificador, camada, tipo, rango, estado del protocolo y próximo evento.',
      },
    },
    {
      element: '[data-tour="modelos-acciones"]',
      popover: {
        title: 'Acciones',
        description: 'Ver detalle, editar o dar de baja el modelo desde el menú ⋮.',
      },
    },
    {
      element: '[data-tour="modelos-paginador"]',
      popover: {
        title: 'Paginación',
        description: 'Navegá entre páginas. La tabla muestra 20 modelos animales por página.',
      },
    },
  ],

  '/internal/profesionales': [
    {
      element: '[data-tour="profesionales-nuevo"]',
      popover: {
        title: 'Registrar profesional',
        description: 'Registrá un nuevo miembro del equipo y asignale un rol en el sistema. También podés usar Alt+N.',
      },
    },
    {
      element: '[data-tour="profesionales-toolbar"]',
      popover: {
        title: 'Búsqueda y filtros',
        description: 'Buscá por nombre o email. Filtrá por rol (cuerpo médico, técnico, investigador principal). Presioná / para enfocar la búsqueda desde el teclado.',
      },
    },
    {
      element: '[data-tour="profesionales-tabla"]',
      popover: {
        title: 'Listado de profesionales',
        description: 'Cada fila muestra nombre, email, teléfono y rol asignado.',
      },
    },
    {
      element: '[data-tour="profesionales-acciones"]',
      popover: {
        title: 'Acciones',
        description: 'Editar datos o dar de baja el profesional desde el menú ⋮.',
      },
    },
    {
      element: '[data-tour="profesionales-paginador"]',
      popover: {
        title: 'Paginación',
        description: 'Navegá entre páginas. La tabla muestra 20 profesionales por página.',
      },
    },
  ],

  '/internal/cajas': [
    {
      element: '[data-tour="cajas-nuevo"]',
      popover: {
        title: 'Registrar caja',
        description: 'Registrá una nueva caja de freezer con su número de freezer, cajón y posición. También podés usar Alt+N.',
      },
    },
    {
      element: '[data-tour="cajas-toolbar"]',
      popover: {
        title: 'Búsqueda y filtros',
        description: 'Buscá por número de freezer o número de caja. Presioná / para enfocar la búsqueda desde el teclado.',
      },
    },
    {
      element: '[data-tour="cajas-tabla"]',
      popover: {
        title: 'Listado de cajas',
        description: 'Cada fila muestra el freezer, cajón y número de caja. Los sueros se almacenan en estas posiciones.',
      },
    },
    {
      element: '[data-tour="cajas-acciones"]',
      popover: {
        title: 'Acciones',
        description: 'Editar o dar de baja la caja desde el menú ⋮.',
      },
    },
    {
      element: '[data-tour="cajas-paginador"]',
      popover: {
        title: 'Paginación',
        description: 'Navegá entre páginas. La tabla muestra 20 cajas por página.',
      },
    },
  ],

  '/internal/camadas': [
    {
      element: '[data-tour="camadas-nuevo"]',
      popover: {
        title: 'Registrar camada',
        description: 'Registrá una nueva camada de ratones con su nombre y fecha de nacimiento. También podés usar Alt+N.',
      },
    },
    {
      element: '[data-tour="camadas-toolbar"]',
      popover: {
        title: 'Búsqueda y filtros',
        description: 'Buscá por nombre de camada. Presioná / para enfocar la búsqueda desde el teclado.',
      },
    },
    {
      element: '[data-tour="camadas-tabla"]',
      popover: {
        title: 'Listado de camadas',
        description: 'Cada fila muestra el nombre de la camada y su fecha de nacimiento. Las camadas agrupan los modelos animales.',
      },
    },
    {
      element: '[data-tour="camadas-acciones"]',
      popover: {
        title: 'Acciones',
        description: 'Editar o dar de baja la camada desde el menú ⋮.',
      },
    },
    {
      element: '[data-tour="camadas-paginador"]',
      popover: {
        title: 'Paginación',
        description: 'Navegá entre páginas. La tabla muestra 20 camadas por página.',
      },
    },
  ],
};
