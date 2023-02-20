package CarvanaTracker.config;

import CarvanaTracker.Component.ExecutorHandlerComponent;
import CarvanaTracker.Component.VehicleDetailHandler;
import CarvanaTracker.Service.VinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
    @Value("${CarvanaTracker.Configuration.NumberOfAPIExecutors}")
    private int numberOfAPIExecutors;
    @Value("${CarvanaTracker.Configuration.RandomTaskExecutionDelay}")
    private int randomTaskExecutionDelay;
    @Autowired
    private VinService vinService;

    @Bean()
    public ExecutorHandlerComponent handler() {
            ExecutorHandlerComponent executorHandlerComponent = new ExecutorHandlerComponent(vinService);
        return executorHandlerComponent;
    }

    @Bean()
    public VehicleDetailHandler vehicleDetailHandler() {
        VehicleDetailHandler vehicleDetailHandler = new VehicleDetailHandler(vinService);
        return vehicleDetailHandler;
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(100);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
       ExecutorHandlerComponent executorHandlerComponent = handler();
       handler().init();
        VehicleDetailHandler vehicleDetailHandler = vehicleDetailHandler();
        vehicleDetailHandler.init();
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
                executorHandlerComponent::run,
                triggerContext -> {
                    Calendar nextExecutionTime =  new GregorianCalendar();
                    Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
                    nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
                    // calculate random time to wait until next execution
                    double ms = ((double)randomTaskExecutionDelay) + (Math.random()*(randomTaskExecutionDelay));
                    nextExecutionTime.add(Calendar.MILLISECOND, (int) ms); //you can get the value from wherever you want
                    return nextExecutionTime.getTime().toInstant();
                }
        );
        taskRegistrar.addTriggerTask(
                vehicleDetailHandler::run,
                triggerContext -> {
                    Calendar nextExecutionTime =  new GregorianCalendar();
                    Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
                    nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
                    // calculate random time to wait until next execution
                    double ms = ((double)randomTaskExecutionDelay/2) + (Math.random()*(randomTaskExecutionDelay));
                    nextExecutionTime.add(Calendar.MILLISECOND, (int) ms); //you can get the value from wherever you want
                    return nextExecutionTime.getTime().toInstant();
                }
        );
    }


}