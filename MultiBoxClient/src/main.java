import org.dreambot.api.input.Mouse;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by Ben on 8/8/2017.
 */
@ScriptManifest(category = Category.WOODCUTTING, name = "MulitboxClient", author = "Computor", version = 6.0)
public class main extends AbstractScript {


    private Socket clientSocket;
    private DataInputStream inputStream;
    private String hostname = "localhost";
    private int port = 7025;

    private Point point;
    private Mouse mouse;

    private int x;
    private int y;

    @Override
    public void onStart() {
        mouse = getMouse();
        mouse.setAlwaysHop(true);
        point = new Point(0,0);
        setCameraAngle();
        try {
            clientSocket = new Socket(hostname,port);
            inputStream = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    public int onLoop() {

        try {

            x = inputStream.readInt();
            log(x + " ");
            y = inputStream.readInt();
            log( y + " ");

            point.x = x;
            point.y = y;
            mouse.click(point);
            log("after mouse click");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public void setCameraAngle(){
        int i = 0;
        while(getCamera().getYaw() != 1480 && i< 100) {
            getCamera().mouseRotateToYaw(1480);
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

    }
}