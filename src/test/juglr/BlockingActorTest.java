package juglr;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * FIXME: Missing class docs for juglr.BlockingActorTest
 *
 * @author mke
 * @since Jan 5, 2010
 */
public class BlockingActorTest {

    public static class TwoNumbers extends Message {
        public int first;
        public int last;

        public TwoNumbers (int first, int last) {
            this.first = first;
            this.last = last;
        }
    }

    public static class SlowCalculator extends Actor {

        @Override
        public void react(Message msg) {
            final TwoNumbers numbers = (TwoNumbers)msg;
            int result = 0;
            try {
                result = await(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        Thread.sleep(2000);
                        return numbers.first + numbers.last;
                    }
                });
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(2);
            }

            assert(result == numbers.first + numbers.last);
            System.out.println(
                    "[" + this + "] " + numbers.first + " + "
                    + numbers.last + " = " + result);
        }
    }

    public static void main(String[] args) throws Exception {
        MessageBus bus = MessageBus.getDefault();
        Actor[] calcs = new Actor[50000];

        for (int i = 0; i < calcs.length; i++) {
            calcs[i] = new SlowCalculator();
        }

        for (Actor calc : calcs) {
            calc.start();
        }

        int i = 0;
        for (Actor calc : calcs) {
            i++;
            bus.send(new TwoNumbers(100, i), calc.getAddress());
        }

        Thread.sleep(50000);
        System.out.println("All good. Bye");
    }

}
