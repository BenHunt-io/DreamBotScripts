import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ben on 8/3/2017.
 */
public class Sleep extends Timer{

    private Object taskLock;

    public Sleep(){
        taskLock = new Object();
    }



    public interface MyCondition{
        boolean verify();
    }

    public boolean sleepUntil(MyCondition myCondition, int timeout) throws InterruptedException {

        synchronized (taskLock) {

            schedule(new MyTask(myCondition), 0, 50);
            taskLock.wait(timeout);

            return true;

        }
    };


    private class MyTask extends TimerTask{

        private MyCondition condition;

        public MyTask(MyCondition condition){
            this.condition = condition;
        }

        @Override
        public void run() {

            if(condition.verify() == true){
                synchronized (taskLock) {
                    cancel();
                    taskLock.notify();
                }
            }
        }
    }

}