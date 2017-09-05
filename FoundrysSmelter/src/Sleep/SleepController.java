package Sleep;

import org.dreambot.api.script.ScriptManager;



import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Holds all the methods needed for sleeping and polling conditions
 * Created by Ben on 9/4/2017.
 */
public class SleepController  {

    private static long startTime;


    /**
     * Polls condition at the specified time until either the condition becomes true or the
     * specified timeOut time has elapsed.
     * @param pollTime - in ms
     * @param timeOut - in ms
     * @return true if condition ends up being met, or false if it is never met.
     */
    public static boolean sleepUntil(Condition condition, int pollTime, int timeOut){

        startTime = System.currentTimeMillis();

        while(System.currentTimeMillis() - startTime != timeOut) {
            if (condition.verify()) {
                return true;
            } else {
                try {
                    Thread.sleep(pollTime);
                } catch (InterruptedException e) {
                    log("SleepUntil interrupted");
                }
            }
        }
        return false; // condition is never met
    }


    public static void sleep(int time){

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log("Sleep interrupted " + e);
        }
    }

}
