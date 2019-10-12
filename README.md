## ARSW-Laboratorio6

```
Daniel Felipe Rodriguez Villalba
```

# Broker de Mensajes STOMP

### Parte I
Para las partes I y II, usted va a implementar una herramienta de dibujo colaborativo Web, basada en el siguiente diagrama de actividades:

Para esto, realice lo siguiente:
1. Haga que la aplicación HTML5/JS al ingresarle en los campos de X y Y, además de graficarlos, los publique en el tópico: /topic/newpoint . Para esto tenga en cuenta (1) usar el cliente STOMP creado en el módulo de JavaScript y (2) enviar la representación textual del objeto JSON (usar JSON.stringify). Por ejemplo:

La publicacion de el punto se realizo de la siguiente manera:
```
stompClient.send("/app/newpoint."+idBlueprint, {}, JSON.stringify(pt));
```
2. Dentro del módulo JavaScript modifique la función de conexión/suscripción al WebSocket, para que la aplicación se suscriba al tópico "/topic/newpoint" (en lugar del tópico /TOPICOXX). Asocie como 'callback' de este suscriptor una función que muestre en un mensaje de alerta (alert()) el evento recibido. Como se sabe que en el tópico indicado se publicarán sólo puntos, extraiga el contenido enviado con el evento (objeto JavaScript en versión de texto), conviértalo en objeto JSON, y extraiga de éste sus propiedades (coordenadas X y Y). Para extraer el contenido del evento use la propiedad 'body' del mismo, y para convertirlo en objeto, use JSON.parse.

Este paso se realizo con el siguiente
```
var theObject = JSON.parse(eventbody.body);
```

3. Compile y ejecute su aplicación. Abra la aplicación en varias pestañas diferentes (para evitar problemas con el caché del navegador, use el modo 'incógnito' en cada prueba).

4. Ingrese los datos, ejecute la acción del botón, y verifique que en todas la pestañas se haya lanzado la alerta con los datos ingresados.

![imagen](https://github.com/danielrodriguezvillalba/ARSW-Laboratorio6/blob/master/imagenes/alert.PNG)

### Parte II

Para hacer mas útil la aplicación, en lugar de capturar las coordenadas con campos de formulario, las va a capturar a través de eventos sobre un elemento de tipo <canvas>. De la misma manera, en lugar de simplemente mostrar las coordenadas enviadas en los eventos a través de 'alertas', va a dibujar dichos puntos en el mismo canvas. Haga uso del mecanismo de captura de eventos de mouse/táctil usado en ejercicios anteriores con este fin.

Se agrego el respectivo listener en la funcion init 
```
  init: function () {
            var can = document.getElementById("canvas");
            //_funcListener();
            var c2 = canvas.getContext('2d');
            c2.clearRect(0, 0, 800, 600);
            idBlueprint = $("#numDib").val();
            if (idBlueprint != null ){
                if(flag == true){
                    flag = true;
                    can.removeEventListener("click",fun)
                }
                can.addEventListener("click", fun = function (evt) {
                    var mousePos = getMousePosition(evt);
                    app.publishPoint(mousePos.x, mousePos.y);
                });
                flag = true;

            };
            //websocket connection
            connectAndSubscribe();
```
1. Haga que el 'callback' asociado al tópico /topic/newpoint en lugar de mostrar una alerta, dibuje un punto en el canvas en las coordenadas enviadas con los eventos recibidos. Para esto puede dibujar un círculo de radio 1.

```
stompClient.subscribe('/topic/newpoint', function (eventbody) {
                var theObject = JSON.parse(eventbody.body);
                var punto = new Point(theObject.x, theObject.y);
                var canvas = document.getElementById("canvas");
                var ctx = canvas.getContext("2d");
                ctx.beginPath();
                ctx.arc(punto.x, punto.y, 3, 0, 2 * Math.PI);
                ctx.stroke();
            });
```

2. Ejecute su aplicación en varios navegadores (y si puede en varios computadores, accediendo a la aplicación mendiante la IP donde corre el servidor). Compruebe que a medida que se dibuja un punto, el mismo es replicado en todas las instancias abiertas de la aplicación.

![imagen](https://github.com/danielrodriguezvillalba/ARSW-Laboratorio6/blob/master/imagenes/circles.PNG)

### Parte III

Ajuste la aplicación anterior para que pueda manejar más de un dibujo a la vez, manteniendo tópicos independientes. Para esto:

1. Agregue un campo en la vista, en el cual el usuario pueda ingresar un número. El número corresponderá al identificador del dibujo que se creará.

![imagen](https://github.com/danielrodriguezvillalba/ARSW-Laboratorio6/blob/master/imagenes/inputid.PNG)

2. Modifique la aplicación para que, en lugar de conectarse y suscribirse automáticamente (en la función init()), lo haga a través de botón 'conectarse'. Éste, al oprimirse debe realizar la conexión y suscribir al cliente a un tópico que tenga un nombre dinámico, asociado el identificador ingresado, por ejemplo: /topic/newpoint.25, topic/newpoint.80, para los dibujos 25 y 80 respectivamente.

![imagen](https://github.com/danielrodriguezvillalba/ARSW-Laboratorio6/blob/master/imagenes/connectBoton.PNG)

3. De la misma manera, haga que las publicaciones se realicen al tópico asociado al identificador ingresado por el usuario.

```
stompClient.subscribe('/topic/newpoint.'+idBlueprint, function (eventbody) {
                var theObject = JSON.parse(eventbody.body);
                var punto = new Point(theObject.x, theObject.y);
                var canvas = document.getElementById("canvas");
                var ctx = canvas.getContext("2d");
                ctx.beginPath();
                ctx.arc(punto.x, punto.y, 3, 0, 2 * Math.PI);
                ctx.stroke();
            });
```

### Parte IV

Para la parte IV, usted va a implementar una versión extendida del modelo de actividades y eventos anterior, en la que el servidor (que hasta ahora sólo fungía como Broker o MOM -Message Oriented Middleware-) se volverá también suscriptor de ciertos eventos, para a partir de los mismos agregar la funcionalidad de 'dibujo colaborativo de polígonos':

Para esto, se va a hacer una configuración alterna en la que, en lugar de que se propaguen los mensajes 'newpoint.{numdibujo}' entre todos los clientes, éstos sean recibidos y procesados primero por el servidor, de manera que se pueda decidir qué hacer con los mismos.
Para ver cómo manejar esto desde el manejador de eventos STOMP del servidor, revise puede revisar la documentación de Spring.

1. Cree una nueva clase que haga el papel de 'Controlador' para ciertos mensajes STOMP (en este caso, aquellos enviados a través de "/app/newpoint.{numdibujo}"). A este controlador se le inyectará un bean de tipo SimpMessagingTemplate, un Bean de Spring que permitirá publicar eventos en un determinado tópico. Por ahora, se definirá que cuando se intercepten los eventos enviados a "/app/newpoint.{numdibujo}" (que se supone deben incluir un punto), se mostrará por pantalla el punto recibido, y luego se procederá a reenviar el evento al tópico al cual están suscritos los clientes "/topic/newpoint".

Se creo la respectiva clase en el paquete de handlers
```
@Controller
public class STOMPMessagesHandler {
	
	@Autowired
	SimpMessagingTemplate msgt;
    
	@MessageMapping("/newpoint.{numdibujo}")    
	public void handlePointEvent(Point pt,@DestinationVariable String numdibujo) throws Exception {
		System.out.println("Nuevo punto recibido en el servidor!:"+pt);
		msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
	}
}
```

2. Ajuste su cliente para que, en lugar de publicar los puntos en el tópico /topic/newpoint.{numdibujo}, lo haga en /app/newpoint.{numdibujo}. Ejecute de nuevo la aplicación y rectifique que funcione igual, pero ahora mostrando en el servidor los detalles de los puntos recibidos.

![imagen](https://github.com/danielrodriguezvillalba/ARSW-Laboratorio6/blob/master/imagenes/dibuTer.PNG)

3. Una vez rectificado el funcionamiento, se quiere aprovechar este 'interceptor' de eventos para cambiar ligeramente la funcionalidad:

La metodo handlePointEvent de la clase STOMPMessagesHandler al final, despues de realizar todo lo pedido en este punto, quedo de la siguiente manera :
```
public void handlePointEvent(Point pt, @DestinationVariable String numdibujo)  {
        if (!valores.containsKey(numdibujo)){
            ArrayList <Point> temp = new ArrayList<Point>();
            temp.add(pt);
            valores.put(numdibujo,temp);
            msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
        }
        else{
            lisPoligonos = valores.get(numdibujo);
            lisPoligonos.add(pt);
            valores.replace(numdibujo,lisPoligonos);
            if(lisPoligonos.size() %4 == 0){
                ArrayList<Point> temp = new ArrayList<>();
                for (int i = 1; i<lisPoligonos.size()+1; i++){
                    if(i % 4 == 0 && i != 0){
                        temp.add(lisPoligonos.get(i-1));
                        msgt.convertAndSend("/topic/newpolygon."+numdibujo, temp);
                        //System.out.println("ENTROOOOOOO");
                        temp = new ArrayList<>();
                    }
                    else{
                        temp.add(lisPoligonos.get(i-1));
                    }
                }


            }
            else {
                System.out.println("Nuevo punto recibido en el servidor!:"+pt);
                msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
            }
        }

    }
```

La prueba del funcionamiento de esta funcion se presenta a continuacion :
![imagen](https://github.com/danielrodriguezvillalba/ARSW-Laboratorio6/blob/master/imagenes/poligon.PNG)
