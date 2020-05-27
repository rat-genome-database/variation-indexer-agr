package edu.mcw.rgd.variantIndexer.vcfUtils;

import edu.mcw.rgd.variantIndexer.Manager;
import edu.mcw.rgd.variantIndexer.service.ESClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.*;

public class MyThreadPoolExecutor extends ThreadPoolExecutor {
    Logger log=Logger.getLogger(Manager.class);
    public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    public void afterExecute(Runnable r, Throwable t){
        super.afterExecute(r,t);
        if(t==null && r instanceof Future){
            try{
                Object result=((Future) r).get();

            }catch (CancellationException e){
                t=e;
            }catch (ExecutionException e){
                t=e.getCause();
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        if(t!=null){
            System.err.println("Uncaught exception! "+t +" STACKTRACE:"+ Arrays.toString(t.getStackTrace()));
            log.info("Uncaught exception! "+t +" STACKTRACE:"+ Arrays.toString(t.getStackTrace()));
            if(ESClient.getClient()!=null)
                try {
                    ESClient.getClient().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            System.exit(1);
        }
    }
}
