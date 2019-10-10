package edu.eci.arsw.collabpaint.handler;

import edu.eci.arsw.collabpaint.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class STOMPMessagesHandler {

    @Autowired
    SimpMessagingTemplate msgt;
    Map<String,ArrayList<Point>> valores =new  ConcurrentHashMap <String,ArrayList<Point>>();
    ArrayList<Point> lisPoligonos;
    String ant ;
    @MessageMapping("/newpoint.{numdibujo}")

    public synchronized void handlePointEvent(Point pt, @DestinationVariable String numdibujo)  {
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
            if(lisPoligonos.size() > 3){
                msgt.convertAndSend("/topic/newpolygon."+numdibujo, lisPoligonos);
            }
            else {
                System.out.println("Nuevo punto recibido en el servidor!:"+pt);
                msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
            }
        }

    }
}