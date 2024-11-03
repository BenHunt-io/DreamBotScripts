import org.dreambot.api.input.Mouse;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Ben on 8/8/2017.
 */
@ScriptManifest(category = Category.WOODCUTTING, name = "MulitboxServer", author = "Computor", version = 6.0)
public class main extends AbstractScript implements MouseListener, MouseWheelListener{


    private ServerSocket serverSocket;
    private Socket servSock;
    DataOutputStream serverOS;
    private Mouse mouse;
    private ScriptManager scriptManager;
    Robot robot;
    @Override
    public void onStart() {

        setCameraAngle();

        scriptManager = new ScriptManager(getClient().getInstance());
        try {
            serverSocket = new ServerSocket(7025);
            servSock = serverSocket.accept();
            serverOS = new DataOutputStream(servSock.getOutputStream());
        } catch (IOException e) {
            log("exception" + e);
        }
        log("Connection made");


        setCameraAngle();
        log("After");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for(int i =0; i < gs.length; i++){
            log(gs[i].getIDstring());
        }

        try {
            robot = new Robot(gs[0]);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        super.onStart();
    }

    @Override
    public int onLoop() {


//        log("" + mouse.click());
//        log(mouse.getLastClicked() + " ");
//        log("" + getClient().getMousePosition());
        sleep(1000);




        return 0;


    }

    @Override
    public void mouseClicked(MouseEvent e) {
            log(getMouse().getPosition().x + " " + getMouse().getPosition().y );


        try {
            if(servSock != null) {
                serverOS.writeInt(getMouse().getPosition().x);
                serverOS.writeInt(getMouse().getPosition().y);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        log( e.paramString() + " ");
    }


    public void setCameraAngle(){
        log("wahat");
        int i = 0;
        while(getCamera().getYaw() != 1480 && i< 50) {
            getCamera().rotateToYaw(1480);
            sleep(25);
            i++;
            log("Test");
        }

        i = 0;
        while(getCamera().getPitch() != 300 && i<100){
            getCamera().rotateToPitch(300);
            sleep(25);
            i++;
        }
        log("lolll");

    }
}
