# MuPlayer
Music player library in pure Java.

# 0.1 Beta
	## Creacion de clases Player y Track.
	## Clase MP3Track probada, ideal mejorar.
	## Falta metodo seek en clase Track.
	## Falta añadir soporte para ogg y aac
# 0.1.1 Beta
	## Reestructuración de clase Track afectando a clases hijas
# 0.2 Beta
	## Añadido soporte para OGG
	## Revisar errores de memoria
# 0.2.1 Beta
	## Construida la clase Player
	## Errores de memoria revisados
	## Reproduccion eficiente y rapida
	## Validacion de tipo de archivo al leer

# 0.3 Beta
	## Añadido soporte flac
	## Se debe verificar que sucede cuando leo un archivo con x formato que termina en y formato en su nombre
	## Falta corregir soporte AAC

# 0.3.1 Beta
	## Lista de archivos sustituida por lista de rutas para optimizacion de memoria.
	## Añadido soporte para cambiar volumen de 0 a 100	
# 0.3.2 Beta
	## Error en playNext() y playPrevious() corregido
# 0.3.3 Beta
	## Ahora se puede apagar el reproductor mediante un metodo propio de la clase
	## Habilitada la opcion para obtener informacion de la cancion
# 0.3.3-1 Beta
    ## Se mantiene el mismo volumen en la cancion siguiente una vez este haya sido modificado externamente
# 0.3.4 Beta
	## Se lanza una excepcion FileNotFoundException cuando la carpeta de Música escogida no existe.
# 0.4 Beta
	## Soporte añadido para rescatar informacion a audios OGG (metodo diferente a los otro formatos)
	## Soporte añadido para reproducir archivos wav, aiff, aifc, snd y au (dependiendo del S.O)
	## Apertura de archivos de audio optimizada.
	## Mejora en soporte Flac (tiempos de carga disminuidos)
	## Soporte especial de lectura de etiquetas para ogg añadido
# 0.5 Beta
	## Soporte para archivos m4a añadido
# 0.5.1 Beta
	## Problema con reconocimiento de formato M4A en jar corregido
# 0.5.2 Beta
	## Se añaden listeners a clase Player	
	## Se optimiza el uso del procesador al momento de utilizar la clase Player, bajando de un 25% a un 1.4% aprox.
# 0.5.3 Beta
	## Se elimina ciclo principal en la clase Player, ahora todo se hace con wait y notify
	## Se corrige problema con stop en la clase Track
