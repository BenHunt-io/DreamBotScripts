import java.awt.*;

/**
 * Created by Ben on 7/22/2017.
 */
public class StrobeRectangle {
    public Color strobeRectColor;

    public int r;
    public int g;
    public int b;
    public int a;

    public Boolean blueFinished;
    public Boolean redFinsihed;
    public Boolean greenFinished;
    public Boolean reversingColor;

    public StrobeRectangle(){
        r = 79;
        g = 155;
        b = 80;
        a = 120;

        blueFinished = false;
        redFinsihed = false;
        greenFinished = false;
        reversingColor = false;

        strobeRectColor = new Color(r,g,b,a);
    }


    public void changeStrobeColor(){

        if(reversingColor){
            reverseColor();
            return;
        }

        // For blue
        switch(b){
            case 80: blueFinished = false;
                break;
            case 155: blueFinished = true;
                break;
        }
        if(!blueFinished) {
            b++;
            strobeRectColor = new Color(r,g,b,a); // set the new color
            return;
        }

        switch(g){
            case 155: greenFinished = false;
                break;
            case 79: greenFinished = true;
                break;
        }

        if(!greenFinished && blueFinished){
            g--;
            strobeRectColor = new Color(r,g,b,a); // set the new color
            return;
        }


        switch(r){
            case 79: redFinsihed = false;
                break;
            case 155: redFinsihed = true;
                break;
        }


        if(!redFinsihed && greenFinished && blueFinished){
            r++;
            strobeRectColor = new Color(r,g,b,a); // set the new color
            return;
        }
        else if(redFinsihed && greenFinished && blueFinished) reversingColor = true;

    }

    // I could do away with some of the conditional checks, but its for readability.
    public void reverseColor(){

        switch(r){
            case 79: redFinsihed = false;
                break;
            case 155: redFinsihed = true;
                break;
        }

        if(redFinsihed && greenFinished && blueFinished){
            r--;
            strobeRectColor = new Color(r,g,b,a); // set the new color
        }


        switch(g){
            case 155: greenFinished = false;
                break;
            case 79: greenFinished = true;
                break;
        }

        if(!redFinsihed && greenFinished){
            g++;
            strobeRectColor = new Color(r,g,b,a); // set the new color
            return;
        }

        switch(b){
            case 80: blueFinished = false;
                break;
            case 155: blueFinished = true;
                break;
        }

        if(!redFinsihed && !greenFinished && blueFinished) {
            b--;
            strobeRectColor = new Color(r,g,b,a); // set the new color
            return;
        }
        else if (!redFinsihed && !greenFinished && !blueFinished) reversingColor = false;



    }
}
