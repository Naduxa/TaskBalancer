import org.testng.Assert;
import org.testng.annotations.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import static org.testng.Assert.*;

@Test
public final class TaskBalancerTest {

    @Test
    void checkBalancerResultsTest(){
        final Queue<Callable<Integer>> tasks = new ArrayDeque<>();
        final int taskSize = 100;
        for (int i = 0; i < taskSize; i++){
            int a = i;
            int b = i + 1;
            tasks.add(()->{return a + b;});
        }

        TasksBalancer<Integer> balancer = new TasksBalancer<>(4, 4, tasks);

        List<Integer> results = balancer.poll();
        Assert.assertEquals(taskSize, results.size(), "Wrong size of results");

        for (int i  = 0 ; i < taskSize; i++){
            Assert.assertEquals(i + i + 1, (int)results.get(i), "Wrong value of task " + ((Integer)(i)).toString());;
        }
    }

    @Test
    void checkExecutorsBalanceTest(){
        Queue<Callable<Integer>> tasks = new ArrayDeque<>();
        int taskSize = 10;
        for (int i = 0; i < taskSize; i++){
            int a = i;
            int b = i + 1;
            tasks.add(()->{
                Thread.sleep(500);
                return a + b;
            });
        }

        TasksBalancer<Integer> balancer = new TasksBalancer<>(4, 4, tasks);
        balancer.poll();
        List<Integer> executorsTaskCount = balancer.getExecutedCount();

        for (Integer anExecutorsTaskCount : executorsTaskCount) {
            Assert.assertNotEquals(0, anExecutorsTaskCount);
        }
    }
}
