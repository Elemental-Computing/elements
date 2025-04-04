package dev.getelements.elements.rt;

import com.google.common.base.Stopwatch;
import dev.getelements.elements.rt.ResourceService.ResourceTransaction;
import dev.getelements.elements.rt.exception.NoSuchTaskException;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * The Scheduler is the main entry point dispatching requests and operations to the various {@link Resource} instances
 * contained in the underlying services.  This allows for both immediate and timed dispatches of various operations
 * to {@link Resource} instances and is responsible for coordinating and serializing access to each {@link Resource}.
 *
 * Created by patricktwohig on 8/22/15.
 */
public interface Scheduler {

    /**
     * Submits an arbitrary {@link Runnable} to run within the {@link Scheduler}.
     *
     * @param runnable the {@link Runnable} to run
     * @return a {@link Future<Void>} to control the execution state of the task
     */
    default Future<Void> submitV(final Runnable runnable) {
        return submit(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Submits an arbitrary {@link Callable<T>} to run within the {@link Scheduler}.
     *
     * @param tCallable the {@link Callable<T>} to run
     * @return a {@link Future<T>} to control the execution state of the task
     */
    <T> Future<T> submit(Callable<T> tCallable);

    /**
     * Provided the {@link Path}, this will schedule an unlink operation at some point in the near future, but not
     * before the delay time specified in the parameters of this method.  Any pending operations to relase their locks
     * gracefully unlink an potentially destroy any {@link Resource}s associated with the supplied {@link Path}.  If
     * the {@link Resource} is removed this will ensure that the {@link Resource#close()} method is called
     * appropriately.
     *
     * This returns a {@link RunnableFuture<Void>} which may be run sooner to short-circuit the unlink operation
     *
     * @param path the {@link Path} to unlink
     * @return a {@link RunnableFuture<Void>} used to signal destruction, may be run sooner by the calling code
     */
    RunnableFuture<Void> scheduleUnlink(Path path, long delay, TimeUnit timeUnit);

    /**
     * Provided the {@link ResourceId}, this will schedule destruction at some point in the near future, but not before.
     * the delay specified in the arguments to this method.  This allows any pending operations to scheduleRelease their
     * locks gracefully and destroy the {@link Resource} associated with the supplied. {@link ResourceId}.
     *
     * This ensures that the underlying {@link Resource} is removed from the {@link ResourceService} and its
     * {@link Resource#close()} method invoked.
     *
     * This returns a {@link RunnableFuture<Void>} which may be run sooner to short-circuit the unlink operation
     *
     * @param resourceId the {@link ResourceId}
     * @return a {@link RunnableFuture<Void>} used to signal destruction, may be run sooner by the calling code
     */
    RunnableFuture<Void> scheduleDestruction(ResourceId resourceId, long delay, TimeUnit timeUnit);

    /**
     * Performs an action against the resource with the provided {@link ResourceId}.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     * @return {@link Future<T>} which can be used to monitor the status of the request
     */
    <T> Future<T> perform(
            ResourceId resourceId,
            Function<ResourceTransaction, T> operation,
            Consumer<Throwable> failure);

    /**
     * Performs an action against the resource with the provided ID.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     *
     * */
    default Future<Void> performV(final ResourceId resourceId,
                                  final Consumer<ResourceTransaction> operation,
                                  final Consumer<Throwable> failure) {
        return perform(resourceId, resource -> {
            operation.accept(resource);
            return null;
        }, failure);
    }

    /**
     * Performs an action against the resource with the provided {@link ResourceId}
     *
     * @param path the path of the resource
     * @param operation the operation to perform
     *
     * @return {@link Future<T>} which can be used to monitor the status of the request
     *
     * @param <T>
     */
    <T> Future<T> perform(
            Path path,
            Function<ResourceTransaction, T> operation,
            Consumer<Throwable> failure);

    /**
     * Performs an action against the resource with the provided {@link Path}.
     *
     * @param path the path of the resource
     * @param operation the operation to perform
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> performV(final Path path,
                                  final Consumer<ResourceTransaction> operation,
                                  final Consumer<Throwable> failure) {
        return perform(path, resource -> {
            operation.accept(resource);
            return null;
        }, failure);
    }

    /**
     * Performs the supplied {@link Function<Resource, T>} after a specified delay.  The delay is calculated using the
     * supplied time and {@link TimeUnit}.
     *
     * @param resourceId the id of the resource
     * @param time the time value to delay
     * @param timeUnit the units of the time value
     * @param operation the operation to perform
     *
     * @param <T>
     *
     * @return {@link Future<T>} which can be used to monitor the status of the request
     */
    <T> Future<T> performAfterDelay(ResourceId resourceId,
                                    long time, TimeUnit timeUnit,
                                    Function<ResourceTransaction, T> operation, Consumer<Throwable> failure);

    /**
     * Invoke {@link #performAfterDelay(ResourceId, long, TimeUnit, Function, Consumer)} with a {@link Consumer<Resource>}
     * instead of a {@link Function<Resource, ?>}.
     *
     * @param resourceId the id of the resource
     * @param time the time value to delay
     * @param timeUnit the units of the time value
     * @param operation the operation to perform
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> performAfterDelayV(final ResourceId resourceId,
                                            final long time, final TimeUnit timeUnit,
                                            final Consumer<ResourceTransaction> operation,
                                            final Consumer<Throwable> failure) {
        return performAfterDelay(resourceId, time, timeUnit, resource -> {
            operation.accept(resource);
            return null;
        }, failure);
    }

    /**
     * Resumes the task associated with the supplied {@link TaskId}.  This allows for the specification of a delay
     * after a specified period of time.
     *
     * @param taskId the {@link TaskId} of the task
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> resumeTask(final TaskId taskId,
                                    final Consumer<Throwable> failure) {

        final Stopwatch stopwatch = Stopwatch.createStarted();

        return performV(taskId.getResourceId(), txn -> {

            final double mills = stopwatch.elapsed(MILLISECONDS);
            final double secondsPerMills = MILLISECONDS.convert(1, SECONDS);

            try {
                txn.getResource().resumeFromScheduler(taskId, mills == 0 ? 0 : (secondsPerMills / mills));
            } catch (NoSuchTaskException ex) {
                final Logger logger = LoggerFactory.getLogger(getClass());
                logger.debug("Ignoring dead task: {}", ex.getTaskId());
            }

        }, failure);

    }

    /**
     * Resumes the task associated with the supplied {@link TaskId}.  This allows for the specification of a delay
     * after a specified period of time.
     *
     * @param taskId the {@link TaskId} of the task
     *
     * @param time the time delay
     * @param timeUnit the {@link TimeUnit} instance designating the time units of measure
*      @param resumed a {@link Runnable} that will execute when the task has been resumed successfully
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> resumeTaskAfterDelay(final TaskId taskId,
                                              final long time, final TimeUnit timeUnit,
                                              final Runnable resumed,
                                              final Consumer<Throwable> failure) {

        final Stopwatch stopwatch = Stopwatch.createStarted();

        return performAfterDelayV(taskId.getResourceId(), time, timeUnit, txn -> {

            final double mills = stopwatch.elapsed(MILLISECONDS);
            final double secondsPerMills = MILLISECONDS.convert(1, SECONDS);

            try {
                txn.getResource().resumeFromScheduler(taskId, mills == 0 ? 0 : (secondsPerMills / mills));
            } catch (NoSuchTaskException ex) {
                final Logger logger = LoggerFactory.getLogger(getClass());
                logger.debug("Ignoring dead task: {}", ex.getTaskId());
            }

            resumed.run();

        }, failure);

    }

    /**
     * Shuts down the Scheduler.  Attempting to invoke any the other methods after invoking this will result in an
     * {@link IllegalStateException} until a subsequent call to {@link #start()} is made.
     *
     * @throws  IllegalStateException
     */
    default void stop() throws IllegalStateException {}

    /**
     * Starts the Scheduler.  Attempting to invoke any the other methods before invoking this will result in an
     * {@link IllegalStateException}.
     *
     * @throws IllegalStateException
     */
    default void start() throws IllegalStateException {}

}
