# Alfresco PDF Sign

Este proyecto es una aplicación para la gestión y firma de documentos PDF que se integra con Alfresco Community Edition
7.4. Utiliza Docker y Maven para la construcción y gestión de servicios, facilitando la implementación y administración
de la aplicación.

## Contenido

- [Requisitos](#requisitos)
- [Uso del Script](#uso-del-script)
    - [Comandos Disponibles](#comandos-disponibles)
- [Contribución](#contribución)

## Requisitos

Antes de comenzar, asegúrate de tener instalados los siguientes programas:

- [Docker](https://www.docker.com/get-started) (para la gestión de contenedores)
- [Docker Compose](https://docs.docker.com/compose/install/) (para la orquestación de contenedores)
- [Maven](https://maven.apache.org/install.html) (para la construcción del proyecto Java)

Además, este proyecto está diseñado para ser compatible con **Alfresco Community Edition 7.4**. Asegúrate de que tu
entorno esté configurado para usar esta versión de Alfresco.

## Uso del Script

El proyecto incluye un script Bash que facilita la gestión de los contenedores Docker y el proceso de construcción del
proyecto. Puedes ejecutar el script con varios comandos para realizar diferentes acciones.

### Comandos Disponibles

#### `start [options] [service_numbers]`

Inicia los contenedores especificados o todos los contenedores si no se especifica ninguno.

- `-b, --build`  Construye el proyecto antes de iniciar los contenedores.
- `-v, --verbose` Muestra los logs de los contenedores después de iniciarlos.
- `service_numbers` Números de los contenedores a iniciar.

#### `stop [options] [service_numbers]`

Detiene los contenedores especificados o todos los contenedores si no se especifica ninguno.

- `-p, --purge` Elimina los volúmenes al detener los contenedores.
- `service_numbers` Números de los contenedores a detener.

#### `restart [service_numbers]`

Reinicia los contenedores especificados o todos los contenedores si no se especifica ninguno.

- `service_numbers` Números de los contenedores a reiniciar.

#### `tail [service_numbers]`

Muestra los logs en tiempo real de los contenedores especificados.

- `service_numbers` Números de los contenedores cuyos logs se deben mostrar.

#### `test [options]`

Ejecuta las pruebas del proyecto.

- `-b, --build` Construye el proyecto antes de ejecutar las pruebas.

#### `status`

Muestra el estado de los contenedores en formato de tabla.

#### `help`

Muestra este mensaje de ayuda.

## Contribución

Si deseas contribuir a este proyecto, sigue estos pasos:

1. Realiza un fork del repositorio.
2. Crea una rama para tu característica (`git checkout -b feature/nueva-caracteristica`).
3. Realiza los cambios y haz commits (`git commit -am 'Añadir nueva característica'`).
4. Empuja tus cambios a tu fork (`git push origin feature/nueva-caracteristica`).
5. Crea un Pull Request en el repositorio original.

Para cualquier duda o consulta, no dudes en abrir un issue en el repositorio.

---

© 2024 Rober de Avila Abraira. Todos los derechos reservados.

Este proyecto se distribuye bajo la Apache License. Consulta el archivo `LICENSE` para más detalles.

¡Gracias por usar Alfresco PDF Sign para Alfresco Community Edition 7.4!
