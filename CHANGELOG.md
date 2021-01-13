## Changelog

- 0.1 Beta
	- Creacion de clases Player y Track.
	- Clase MP3Track probada, ideal mejorar.
	- Falta metodo seek en clase Track.
	- Falta añadir soporte para ogg y aac


- 0.1.1 Beta
	- Reestructuración de clase Track afectando a clases hijas


- 0.2 Beta
	- Añadido soporte para OGG
	- Revisar errores de memoria


- 0.2.1 Beta
	- Construida la clase Player
	- Errores de memoria revisados
	- Reproduccion eficiente y rapida
	- Validacion de tipo de archivo al leer


- 0.3 Beta
	- Añadido soporte flac
	- Se debe verificar que sucede cuando leo un archivo con x formato que termina en y formato en su nombre
	- Falta corregir soporte AAC


- 0.3.1 Beta
	- Lista de archivos sustituida por lista de rutas para optimizacion de memoria.
	- Añadido soporte para cambiar volumen de 0 a 100


- 0.3.2 Beta
	- Error en playNext() y playPrevious() corregido


- 0.3.3 Beta
	- Ahora se puede apagar el reproductor mediante un metodo propio de la clase
	- Habilitada la opcion para obtener informacion de la cancion


- 0.3.3-1 Beta
    - Se mantiene el mismo volumen en la cancion siguiente una vez este haya sido modificado externamente


- 0.3.4 Beta
	- Se lanza una excepcion FileNotFoundException cuando la carpeta de Música escogida no existe.


- 0.4 Beta
	- Soporte añadido para rescatar informacion a audios OGG (metodo diferente a los otro formatos)
	- Soporte añadido para reproducir archivos wav, aiff, aifc, snd y au (dependiendo del S.O)
	- Apertura de archivos de audio optimizada.
	- Mejora en soporte Flac (tiempos de carga disminuidos)
	- Soporte especial de lectura de etiquetas para ogg añadido


- 0.5 Beta
	- Soporte para archivos m4a añadido


- 0.5.1 Beta
	- Problema con reconocimiento de formato M4A en jar corregido


- 0.5.2 Beta
	- Se añaden listeners a clase Player
	- Se optimiza el uso del procesador al momento de utilizar la clase Player, bajando de un 25% a un 1.4% aprox.


- 0.5.3 Beta
	- Se elimina ciclo principal en la clase Player, ahora todo se hace con wait y notify
	- Se corrige problema con stop en la clase Track


- 0.5.4 Beta
	- Optimizacion de memoria al leer archivos FLAC
	- Falta corregir lectura de etiquetas FLAC
	- Falta corregir lectura de archivos AAC (de otra forma retirar codec)


- 0.5.5 Beta
	- Se optimiza la memoria para la ejecucion de la libreria
	- Se logra obtener duracion de archivos flac y ogg
	- Se permite realizar un seek en archivos mp3, ogg y flac por segundos de audio


- 0.6 Beta
	- Se lee duracion de archivos PCM correctamente.
	- Error en metodo shutdown del Player corregido.
	- Compatibilidad mejorada en formato M4A
	- Se obtiene la duracion de la cancion a traves de la sobreescritura de un metodo
		abstracto de la clase padre Track
	- Se añade opcion de ver tiempo actual de la cancion
	- Se añade opcion de dirigirse a un segundo especifico de la cancion (No disponible correctamente en archivos FLAC y M4A)


- 0.6.1 Beta
	- Se añade funcion para agregar más musica al reproductor


- 0.7 Release
	- Se logra obtener duracion de archivos de audio para todos los formatos disponibles
	- Se añade opcion para obtener la caratula de un archivo de audio cualquiera


- 0.7.1 Release
	- Se corrige el error que se produce al momento de dirigirse a una carpeta sin canciones


- 0.7.2 Beta
	- Se corrige error al hacer un seek con el formato FLAC
	- Metodos seek y getDuration globales con algunas sobreescrituras especificas segun sea el caso


- 0.8 Beta
	- Se corrige error al hacer seek en M4A
	- Se mejora seek en FLAC y M4A
	- Funcion mute y unmute en Player añadida


- 0.8.1 Beta
	- Se renombran paquetes internos
	- Se valida el metodo setGain, solo se permiten volumenes entre 0 y 100
	- Se crea clase ListenersNames para manejar los nombres de los metodos en los listeners genericamente
	- Se corrige problema con playPrevious, se hace un poco mas optimo
	- Se agrega funcion seekFolder para saltar entre carpetas cuando se cargan las canciones


- 0.8.2 Beta
	- Se corrige error al leer equivocadamente una imagen simplificando lectura de audio
	- Dependencias actualizadas a ultimas versiones
	- Se crea clase TrackHandler para manejar objetos Track utilizando solo los controles de audio
	- Se crea interfaz TrackInfo para solo rescatar la informacion de la cancion como un objeto aparte


- 0.9 Beta
	- Se corrige error en volumen, ahora si el reproductor esta en mute tambien lo estará cuando pase a una siguiente canción.
	- Se agrega metodo para obtener duracion formateada en los Track
	- Se eliminan clases innecesarias
	- Se añade logger propio para mensajes a consola
	- Se optimiza la forma en mostrar la info de la cancion por consola, para ambitos de produccion
	- Problema al mostrar titulo de la cancion corregido
	- Problema al reproducir primera cancion al inicio corregido.
	- Se añade funcionalidad que permite visualizar un listado de las subcarpetas en nuestra carpeta de musica


- 0.9.1 Beta
	- Se mejora la comprobacion de formatos soportados segun sistema operativo
	- Se añaden funcionalidades para evaluar si existen carpetas registradas
	- Eliminacion de librerias innecesarias al momento de compilar y generar el jar
	- Optimizacion de lectura de archivos .m4a (aac), falta comprobar seek
	- Correccion de errores menores en coherencia de codigo en la clase Player


- 0.9.2 Beta
	- Creacion de TestingManager para definir carpetas para la prueba de formatos de audio
	- Problema en seek en formato M4A random corregido y mejorado
	- Se obtiene un tiempo mas exacto al consultar cuantos segundos se han reproducido para
	el formato m4a random
	- Seek corregido en formato mp3
	- Falta corregir seek en m4a normal (quedan detalles minimos de incoherencia)
	- Estructura de paquetes modificada


- 0.9.3 Beta
	- Optimización de funcionalidades en clase Player
	- Se corrige error con primera cancion reproducida al cargar carpeta, se estaba reproduciendo la segunda
	- Se disminuye la carga de la CPU al pausar y parar canciones mientras se espera respuesta
	- Se mejora lectura de tiempo transcurrido de reproduccion de cancion, se espera revisar y mejorar mas
	- Se añaden mas funcionalidades al reproductor de pruebas


- 0.9.4 Beta
	- Mejora el seek m4a random, se corrige problema de incoherencia con los segundos adelantados
	- Thread de reproduccion de canciones optimizado al pausar cancion, se disminuye carga en CPU
	- Se obtiene el progreso de la cancion en segundos directamente desde la clase Track para todos los formatos
	- Se añade funcion en clase Player para obtener a traves de la interface el progreso de la cancion actual
	- Se corrige accion al encontrar errores en las canciones, ahora simplemente se saltan


- 0.9.4-1 Beta
	- Dependencia faltante subida


- 0.10 Beta
	- Se corrige error al leer track, se cargaba stream dos veces
	- Se corrige reconocimiento de formatos en archivos pcm
	- Se obtiene el formato de audio de los archivos de audio
	- Se define un algoritmo seek funcional por defecto pero con excepcion en flac
	- Seek corregido en todos los formatos de prueba, falta mejorar velocidad y desempeño en seek ogg y flac
	- Se espera revisar todas las canciones en busca de errores
	- Falta averiguar el por que en el jar algunas librerias no funcionan
	- Se volvio al algoritmo de lectura antiguo de m4a random permitiendo mas lentitud ya que se debe corregir stream propio


- 0.11 Beta
	- Error con mute y unmute corregido
	- Se revisa seek y gotoseconds en canciones reales, se corrige seek y goto en todos los formatos, falta revisar goto prev de mp3


- 0.12 Beta
    - Se realiza una limpieza de código eliminando funciones sin utilidad.
    - Se añade nuevo constructor a clase Player para recibir directamente la ruta de la 
    carpeta a cargar
    - Se corrigen errores probables con metodos de reproduccion en clase track
    - Se mejora la muestra de informacion de sonidos


- 0.13 Beta
    - Se añade nueva funcionalidad de reproducción en consola(falta mejorar).
    

- 0.14 Beta
    - Se mejora tester de libreria
    - Se corrige goto prev en mp3, flac y ogg
    - Metodo goto en comun para todos los formatos excepto m4a
    
- 1.0 RC
    - Se mejora funcion ConsolePlayer permitiendo a la librería actuar por si sola
    - Se deben corregir detalles en formato mp3 al saltar y ir a un segundo específico, los
    otros formatos si funcionan bien en ese aspecto.
    - Se realiza limpieza de código en algunas funcionalidades
    - Se consigue desactivar loggers de librerias innecesarios
    
- 1.0.1 RC
    - Se compacta jar con todas las dependencias dentro al generarlo, corrigiendo los errores subyacentes
    - Se cargan librerias externas a mavencentral desde el repositorio jitpack.io
    - Soporte añadido para aac (no mencionado).
        
- 1.0.2 RC
    - Se corrige funcion de testing MusicPlayer
    - Se limpia un poco el código
    
- 1.1 RC
    - Se realiza limpieza de código en ConsolePlayer
    - Se añade funcion clear y cls para limpiar pantalla en ConsolePlayer
    - Se limpia código en clase Track.

- 1.1.1 RC
    - Se cambia color en logging de informacion

- 1.2 RC
    - Se añade opcion de reproducir carpeta según indice
    - Se añade opcion de formateo en minutos para obtener el progreso utilizando la opcion "h" en el comando respectivo
    - Se agregan los comandos sys y system para ejecutar ordenes del sistema operativo
    - Se corrige error al cambiar volumen antes de iniciar player
    - Se agrega metodo getTracksInfo para solo obtener la informacion de las canciones
    - Se corrige metodo reloadTracks para el un futuro recargar desde rutas en un archivo
    - Se agrega opcion de compatibilidad multicomando en una linea (&&)

- 1.3-RC
    - Se agrega metodo gotoSecond en clase Player
    - Se mejora el algoritmo de cambio de volumen
    - Funcionalidad para modificar volumen del sistema agregada
    - Problema goto corregido
    - Metodo getFormattedProgress disponible en clase Player directamente
    
- 1.3.1-RC
    - Se corrige error en getTracksInfo
    
- 1.3.2-RC
    - Se corrige metodo getTracksInfo

- 1.3.3-RC
    - Se corrige error al reproducir por indice
    
- 1.3.4-RC
    - Correcciones menores
    
- 1.3.5-RC
    - Corregido comando goto en consolePlayer

- 1.4-RC
    - Se agregan comandos title y name
    - Se corrige parcialmente goto en ogg

- 1.4.1-RC
    - Correccion de carga de listeners, ahora se ejecutan en paralelo

- 1.4.2-RC
    - Se agrega nombre a ListenerRunner
    - Metodo isActive agregado
    - Metodo getTracksTag optimizado
    
- 1.5-RC
    - Se actualizan versiones de dependencias
    - Se agrega metodo onPlaying a PlayerListener
    - Se agregan comandos ln y lp para mostrar la cancion siguiente
	y anterior respectivamente en ConsolePlayer
    - Mejoras pequeñas aplicadas
	
- 1.5.1-RC
    - Se muestra con color distintivo carpeta de la cancion actual en ConsolePlayer
    - Codigo reordenado

- 1.6-RC
    - Optimizaciones y correcciones menores
    - Al cambiar de cancion ahora se silencia la actual para no demorar la siguiente reproduccion
    - Nuevo comando en ConsolePlayer que permite reproducir carpeta por indice: pf

- 1.6.1-RC
    - ListenerRunner se separa a una clase aparte
    - Se agrega un listener a ConsolePlayer

- 1.7-RC
    - Se separa interpreter como objeto aparte para ser reutilizado en proyecto externo
    - Se agrega comando load en ConsolePlayer para reiniciar el reproductor cargando otra carpeta

- 1.7.1-RC
    - Correccion tipo hotfix en PlayerHandler

- 1.8-RC
    - Soporte inicial para carga de Tracks desde InputStream, habra errores al cargar informacion de cancion
    - En el inicio del modo consola se muestra la version actual de MuPlayer
    - Se mejora velocidad de reproduccion, ya no se escucha acelerado

- 1.8.1-RC
    - Cambios menores en comandos prog y d
    - Cambios menores en seek mp3
    - Optimizaciones menores en clase Track

- 1.9-RC
    - Coreccion en comando exit en ConsolePlayer
    - Se agrega clase TaskRunner para la ejecucion de hilos
    - Se elimina PlayerHandler, ahora se puede utilizar mas de un reproductor a la vez y las Track pueden o no pertenecer a un objeto
    reproductor Player
    - Se agrega la opcion de manejar una carpeta padre por defecto configurandola en un archivo llamado config.properties, 
    la variable debe llamarse root_folder.
    - En el comando pf ahora se muestra la cancion actual reproducida

- 1.9.1
    - Se corrige error con nueva funcionalidad que reemplaza a PlayerHandler

- 1.9.2
    - Correccion de errores y eliminacion de mensajes de debug innecesarios

- 1.9.3
    - Fix AudioDataInputStream and AudioDataOutputStream

- 1.9.4
    - Se corrige forma de acceder al archivo config.properties, ahora si se accede al jar desde otra carpeta se lee correctamente

- 2.0
    - Se puede obtener informacion de bitrate de la cancion directamente desde interfaz TrackInfo
    - Se corrige error de congelamiento cuando se inserta el comando info y no existen canciones
    - Correccion de error accidental al decodificar flac
    - Ahora es posible obtener un listado de artistas y albums desde la clase Player
    - Nuevos comandos para listar artistas y albums en ConsolePlayer(consulte comando 'h')
    - Se ordenan algunas clases
    - El menu de ayuda es guardado en un archivo help.properties completamente editable

- 2.0.1
    - Correccion de errores para sileciar y quitar silencio al reproductor
    
- 2.0.2
    - Se aplican pequeñas optimizaciones de código

- 2.0.3
    - Pequeñas mejoras y optimizaciones de código
    - Se ordena menu help por orden alfabetico
    
- 2.1
    - Se implementa patrón de diseño State para los estados de Track

- 2.1.1
    - Optimizaciones y correcciones de errores menores

- 2.2
    - Se aplican algunas mejoras de codigo y limpiezas menores
    - Nuevos metodos de conversion entre bytes y segundos sobreescrito por cada subclase de Track
	- Se corrige gotoSec implementando un nuevo TrackState
	
- 2.2.1
	- Menores correcciones de codigo
	
- X
	- Mejoras de codigo