/**
 * Created by Ben on 8/2/2017.
 */
public class InterruptFlag {

    private boolean interruptFlag;

    public InterruptFlag(boolean interruptFlag){
        this.interruptFlag = interruptFlag;
    }

    public boolean getValue(){
        return interruptFlag;
    }
    public void setValue(boolean newValue){
        this.interruptFlag = newValue;

    }

    public void interrupt(){
        interruptFlag = true;
    }

    /**
     * This should function the same as a regular interrupt flag...
     * @return
     */
    public boolean interrupted(){

        boolean tempBool = getValue();
        interruptFlag = false;
        return tempBool; // return original value

    }
}
