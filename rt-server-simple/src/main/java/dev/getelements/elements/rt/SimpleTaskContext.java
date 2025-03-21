package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.id.TaskId;

import jakarta.inject.Inject;
import java.util.function.Consumer;

public class SimpleTaskContext implements TaskContext {

    @Inject
    private TaskService taskService;

    @Override
    public void start() {
        getTaskService().start();
    }

    @Override
    public void stop() {
        getTaskService().stop();
    }

    @Override
    public void register(final TaskId taskId,
                             final Consumer<Object> consumer,
                             final Consumer<Throwable> throwableTConsumer) {
        getTaskService().register(taskId, consumer, throwableTConsumer);
    }

    @Override
    public boolean finishWithResult(final TaskId taskId, final Object result) {
        return getTaskService().finishWithResult(taskId, result);
    }

    @Override
    public boolean finishWithError(final TaskId taskId, final Throwable error) {
        return getTaskService().finishWithError(taskId, error);
    }

    public TaskService getTaskService() {
        return taskService;
    }

    @Inject
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

}
