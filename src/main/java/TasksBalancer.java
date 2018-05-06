import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TasksBalancer <T> {

    private final List<ThreadPoolExecutor> executors;
    private final Queue<Callable<T>> waiters;
    private final List<Integer> executorTasksCount;
    private final int poolSize;
    private final List<Future<T>> futures = new ArrayList<>();

    TasksBalancer(int executorsCount, int poolSize, Queue<Callable<T>> queue){
        this.poolSize = poolSize;
        this.executors = new ArrayList<>();
        this.executorTasksCount = new ArrayList<>();
        this.waiters = queue;

        for (int i = 0; i < executorsCount; i++) {
            executors.add((ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize));
            executorTasksCount.add(0);
        }
    }


     List<T> poll(){
        while (!waiters.isEmpty()) {
            int executorIndex = 0;
            long minTaskCount = poolSize;

            for (int i = 0; i < executors.size(); i++) {
                long taskCount = executors.get(i).getActiveCount();
                if (minTaskCount > taskCount) {
                    minTaskCount = taskCount;
                    executorIndex = i;
                }
            }

            if (minTaskCount != poolSize){
                futures.add(executors.get(executorIndex).submit(Objects.requireNonNull(waiters.poll())));
                executorTasksCount.set(executorIndex, executorTasksCount.get(executorIndex) + 1);
            }
        }

        shutdown();

        return futures.stream().map(f -> {
            try{
                return f.get();
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }


    List<Integer> getExecutedCount() {
        return new ArrayList<>((executorTasksCount));
    }

    private void shutdown(){
        for (ThreadPoolExecutor ex : executors){
            ex.shutdown();
        }
    }
}

class Main
{
    public static void main(String[] args){
    }
}