package logica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class SocketController implements Runnable {

    public String code;
    public String mensaje;
    private Thread theThread = null;
    private Socket theSocket = null;
    private PrintWriter theout = null;
    private BufferedReader theIn = null;

    private JTextArea txtOutputPublico = null;
    private JTextArea txtOutputPrivado = null;
    private JTextArea txtOutputUsuarios = null;
    private JComboBox listSeleccionarUsuario = null;
    private JTextField Mensajes = null;
    public String usuarionorepet = "";
    public String captarmio = "";

    public LinkedList<String> usuarios;
    public String[] list;

    public SocketController(String newHostname, int newPort, JTextArea txtOutputPublico, JTextArea txtOutputPrivado,
            JTextArea txtOutputUsuarios, JComboBox SeleccionarUsuario, JTextField MensajesEnviados) throws IOException {
        theSocket = new Socket(newHostname, newPort);
        theout = new PrintWriter(theSocket.getOutputStream(), true);
        theIn = new BufferedReader(new InputStreamReader(theSocket.getInputStream(), "UTF-8"));
        this.txtOutputPublico = txtOutputPublico;
        this.txtOutputPrivado = txtOutputPrivado;
        this.txtOutputUsuarios = txtOutputUsuarios;
        this.listSeleccionarUsuario = SeleccionarUsuario;
        this.Mensajes = MensajesEnviados;
        this.usuarios = new LinkedList<>();
    }

    public SocketController(Socket newSocket) throws IOException {
        theSocket = newSocket;
        theout = new PrintWriter(theSocket.getOutputStream(), true);
        theIn = new BufferedReader(new InputStreamReader(theSocket.getInputStream(), "UTF-8"));
    }

    public void start() {
        theThread = new Thread(this);
        theThread.start();
    }

    public void close() throws IOException {
        theout.close();
        theIn.close();
        theSocket.close();
    }

    public void writeText(String text) {
        theout.println(text);
    }

    public String readText() {
        String text = null;
        String privado = "";
        String usuarios = "";
        try {
            text = theIn.readLine();
            code = text.substring(0, 4); //substraigo los 4 primeros caracteres que son los 4 numeros del servidor
            mensaje = text.substring(6, text.length()); // substrae el resto del mensaje del servidor

        } catch (Exception e) {
            Logger.getLogger(SocketController.class.getName()).log(Level.SEVERE, null, e);
        }
        return text;
    }

    public void Registrarse(String username) {
        usuarionorepet = username;
        username = "register " + username;
        
        //aqui vamos
        


        //this.usuarios.add(username);
        theout.println(username);

        //theout.println es con el que uno manda el mensaje al servidor
        //theIn.println es el mensaje que el servidor envía
    }

    public void enviarPublico(String text) {
        theout.println("sendall " + text);
        captarmio = text;

    }

    public void enviarPrivado(String text) {
        //System.out.println(listSeleccionarUsuario.getSelectedItem());
        theout.println("send " + listSeleccionarUsuario.getSelectedItem() + " " + text);
        ///
        
        captarmio = text;
    }
    
    public String cantUsuarios ( ){
        return this.listSeleccionarUsuario.getItemCount() + "";
    }
    

    @Override
    public void run() {
        String command = null;
        boolean quit = false;

        while (!quit) {
            command = readText();

            if (command != null) {
                command = command.trim();
                
                System.out.println("");
                String code = command.substring(0, 4);
                String message = command.substring(5, command.length());
                

                // txtOutputPublico.append(command + "\n");
                //ESTO ES PARA CUANDO SE REGISTRE UN USUARIO  -------------------REGISTER USERNAME
                if (code.equals("1000")) {
                    JOptionPane.showMessageDialog(null, "Su usuario ha sido registrado correctamente");
                } else if (code.equals("0000")) {
                    JOptionPane.showMessageDialog(null, "se ha conectado al servidor");
                } else if (code.equals("1001")) {
                    JOptionPane.showMessageDialog(null, "Su nombre de usuario es inválido." + "\n"
                            + "¡RECUERDE! Este debe tener al menos 8 caracteres y el primer caracter debe ser una letra");
                } else if (code.equals("1002")) {
                    JOptionPane.showMessageDialog(null, "Lo sentimos este nombre de usuario ya existe." + "\n" + ""
                            + "¡Por favor cree uno nuevo!");
                } //ESTO ES PARA CUANDO SE ENVÍA UN MENSAJE PRIVADO  --------------SEND USERNAME MESSAGE
                else if (code.equals("4000")) {
                    JOptionPane.showMessageDialog(null, "Mensaje Enviado privado");
                    txtOutputPrivado.append(listSeleccionarUsuario.getSelectedItem() + "  dice:  " + captarmio +  "\n");
                } else if (code.equals("4001")) {
                    JOptionPane.showMessageDialog(null, "Su mensaje no ha sido enviado");
                } //ESTO ES PARA CUANDO SE ENVÍA UN MENSAJE PARA TODOS  --------------SENDALL USERNAME MESSAGE
                else if (code.equals("2000")) {
                    JOptionPane.showMessageDialog(null, "Mensaje enviado a todos");
                    
                    txtOutputPublico.append(message + "\n");

                } else if (code.equals("2001")) {
                    JOptionPane.showMessageDialog(null, "Su mensaje para todos los usuarios. no ha sido enviado");
                } //MENSAJES PARA CAPTAR DEL SERVIDOR
                //USERNAME MESSAGE
                //IDENTIFICA UN MSJ PRIVADO                                     //(SOURCE) (PRIVATE)
                else if (code.equals("4010")) {

                    txtOutputPrivado.append("\n");
                    //listSeleccionarUsuario.getSelectedItem();

                } //USERNAME MESSAGE
                //IDENTIFICA UN MSJ PUBLICO                                     //(SOURCE) (PUBLIC)
                else if (code.equals("2010")) {

                    txtOutputPublico.append( "\n");

                } //LISTA USUARIOS
                else if (code.equals("5000")) {

                    String[] usu = message.split(";");
                    this.listSeleccionarUsuario.removeAllItems();
                    this.txtOutputUsuarios.setText("");

                    for (String usuario : usu) {

                        //this.txtOutputUsuarios.append(usuario+"\n");
                        if ((!usuarionorepet.equals(usuario)) && (!usuario.equals(usu))) {
                            listSeleccionarUsuario.addItem(usuario);

                            this.txtOutputUsuarios.append(usuario + "\n");

                        }

                    }

                }
                else if(code.equals("10000")){
                    quit = true;
                }
            }    
        }
    }
}
