package es.us.lsi.dad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private List<Sensor> sensores;
    private List<Actuador> actuadores;

    public void init() throws ServletException {
        
        sensores = new ArrayList<>();
        actuadores = new ArrayList<>();
        creaDatosEjemplo(25);
        super.init();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tipo = req.getParameter("tipo");

        if (tipo == null) {
            PrintWriter out = resp.getWriter();
            out.println("<html>");
            out.println("<body>");
            out.println("<h1>Bienvenido al Servlet del entregable 1</h1><br>");
            out.println("<h2>Por favor, introduce en la url un tipo válido (sensor, actuador) y sus respectivos IDs</h2>");
            out.println("</body>");
            out.println("</html>");
        } else {
            Gson gson = new Gson();
            JsonObject respuesta = new JsonObject();

            switch (tipo) {
                
                case "sensor":
                    Integer idSensor = Integer.valueOf(req.getParameter("idSensor"));
                    Integer idPlacaSensor = Integer.valueOf(req.getParameter("idPlaca"));
                    List<Sensor> listaSensor = new ArrayList<>();
                    for (Sensor s : sensores) {
                        if (s.getIdsensor().equals(idSensor) && s.getIdplaca().equals(idPlacaSensor)) {
                            listaSensor.add(s);
                        }
                    }
                    respuesta.add("sensores", gson.toJsonTree(listaSensor));
                    break;

                case "actuador":
                    Integer idActuador = Integer.valueOf(req.getParameter("idActuador"));
                    Integer idPlacaActuador = Integer.valueOf(req.getParameter("idPlaca"));
                    List<Actuador> listaActuador = new ArrayList<>();
                    for (Actuador a : actuadores) {
                        if (a.getIdactuador().equals(idActuador) && a.getIdplaca().equals(idPlacaActuador)) {
                            listaActuador.add(a);
                        }
                    }
                    respuesta.add("actuadores", gson.toJsonTree(listaActuador));
                    break;

                default:
                    resp.getWriter().println("Tipo no válido");
                    resp.setStatus(400);
                    return;
            }

            resp.getWriter().println(respuesta);
            resp.setStatus(201);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reader = req.getReader();
        Gson gson = new Gson();
        String tipo = req.getParameter("tipo");

        if (tipo == null) {
            resp.getWriter().print("Tipo no especificado");
            resp.setStatus(400);
            return;
        }

        try {
            switch (tipo) {
                
                case "sensor":
                    Sensor sensor = gson.fromJson(reader, Sensor.class);
                    if (sensor.getIdplaca() == null || sensor.getIdsensor() == null || sensor.getIdgrupo() == null 
                    		|| sensor.getTimestamp() == null || sensor.getValueTemp() == null || sensor.getValueHum() == null) {
                        resp.getWriter().print("Formulario incompleto, faltan datos");
                        resp.setStatus(400);
                    } else {
                        sensores.add(sensor);
                        resp.getWriter().println(gson.toJson(sensor));
                        resp.setStatus(201);
                    }
                    break;

                case "actuador":
                    Actuador actuador = gson.fromJson(reader, Actuador.class);
                    if (actuador.getIdplaca() == null || actuador.getIdactuador() == null || actuador.getIdgrupo() == null || actuador.getTimestamp() == null || actuador.getValue() == null) {
                        resp.getWriter().print("Formulario incompleto, faltan datos");
                        resp.setStatus(400);
                    } else {
                        actuadores.add(actuador);
                        resp.getWriter().println(gson.toJson(actuador));
                        resp.setStatus(201);
                    }
                    break;

                default:
                    resp.getWriter().println("Tipo no válido");
                    resp.setStatus(400);
                    break;
            }
        } catch (Exception e) {
            resp.getWriter().println("Los datos facilitados no son válidos.");
            resp.setStatus(500);
        }
    }

    private void creaDatosEjemplo(Integer cantidad) {
        Random rand = new Random();
        for (int i = 0; i < cantidad; i++) {
            
            sensores.add(new Sensor(
                Integer.valueOf(rand.nextInt(20)),
                Integer.valueOf(rand.nextInt(20)),
                BigInteger.valueOf(System.currentTimeMillis()),
                rand.nextDouble() * 100,
                rand.nextDouble() * 100,
                Integer.valueOf(rand.nextInt(10)),
                Integer.valueOf(rand.nextInt(5))
            ));
            actuadores.add(new Actuador(
                Integer.valueOf(rand.nextInt(20)),
                Integer.valueOf(rand.nextInt(20)),
                BigInteger.valueOf(System.currentTimeMillis()),
                rand.nextDouble() * 100,
                Integer.valueOf(rand.nextInt(10)),
                Integer.valueOf(rand.nextInt(5))
            ));
        }
    }
}