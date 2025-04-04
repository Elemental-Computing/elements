package dev.getelements.elements.rt;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.exception.RoutingException;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.*;
import dev.getelements.elements.rt.routing.SameNodeIdRoutingStrategy;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static dev.getelements.elements.sdk.cluster.id.ApplicationId.randomApplicationId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.sdk.cluster.id.NodeId.forInstanceAndApplication;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Guice(modules={RoutingTestModule.class, TestSameNodeIdRoutingStrategy.Module.class})
public class TestSameNodeIdRoutingStrategy extends BaseRoutingStrategyTest {

    private final InstanceId instanceId = randomInstanceId();

    @Test
    public void testInvokeSync() throws Exception {

        final NodeId nodeId = generateNodeId();
        final List<Object> address = generateSaneAddress();
        final Object mockResult = mock(Object.class);
        final Invocation invocation = spy(Invocation.class);
        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        when(getRemoteInvokerRegistry().getRemoteInvoker(eq(nodeId)))
            .thenReturn(mockRemoteInvoker);

        when(mockRemoteInvoker.invokeSync(invocation, asyncConsumers, invocationErrorConsumer))
                .thenReturn(mockResult);

        final Object result =
                getRoutingStrategy().invokeSync(address, invocation, asyncConsumers, invocationErrorConsumer);

        verify(mockRemoteInvoker, times(1))
                .invokeSync(eq(invocation), eq(asyncConsumers), eq(invocationErrorConsumer));

        assertEquals(result, mockResult);

    }

    @Test(expectedExceptions = {BullshitException.class})
    public void testInvokeSyncException() throws Exception {

        final NodeId nodeId = generateNodeId();
        final List<Object> address = generateSaneAddress();
        final List<Consumer<InvocationResult>> asyncConsumers = new ArrayList<>();
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);
        final Invocation invocation = spy(Invocation.class);

        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);

        when(getRemoteInvokerRegistry().getRemoteInvoker(nodeId))
                .thenReturn(mockRemoteInvoker);

        when(mockRemoteInvoker.invokeSync(any(), any(), any())).thenThrow(new BullshitException());
        getRoutingStrategy().invokeSync(address, invocation, asyncConsumers, invocationErrorConsumer);

    }

    @Test
    public void testInvokeAsync() {

        final NodeId nodeId = generateNodeId();
        final List<Object> address = generateSaneAddress();
        final Invocation invocation = spy(Invocation.class);
        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        when(getRemoteInvokerRegistry().getRemoteInvoker(eq(nodeId)))
                .thenReturn(mockRemoteInvoker);

        doNothing()
                .when(mockRemoteInvoker)
                .invokeAsyncV(invocation, asyncConsumers, invocationErrorConsumer);

        final Void result = getRoutingStrategy()
                .invokeAsyncV(address, invocation, asyncConsumers, invocationErrorConsumer);

        assertNull(result);

        verify(mockRemoteInvoker, times(1))
            .invokeAsync(eq(invocation), eq(asyncConsumers), eq(invocationErrorConsumer));

    }

    @Test
    public void testInvokeFuture() throws ExecutionException, InterruptedException {

        final NodeId nodeId = generateNodeId();
        final List<Object> address = generateSaneAddress();
        final Invocation invocation = spy(Invocation.class);
        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);

        final Object mockResult = mock(Object.class);
        final Future<Object> mockResultFuture = mock(Future.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        when(getRemoteInvokerRegistry().getRemoteInvoker(eq(nodeId)))
                .thenReturn(mockRemoteInvoker);

        when(mockRemoteInvoker.invokeFuture(invocation, asyncConsumers, invocationErrorConsumer))
                .thenReturn(mockResultFuture);

        when(mockResultFuture.get()).thenReturn(mockResult);

        final Future<Object> resultFuture =
                getRoutingStrategy().invokeFuture(address, invocation, asyncConsumers, invocationErrorConsumer);

        verify(mockRemoteInvoker, times(1))
                .invokeFuture(eq(invocation), eq(asyncConsumers), eq(invocationErrorConsumer));

        assertEquals(resultFuture, mockResultFuture);
        assertEquals(resultFuture.get(), mockResult);

    }

    @Test(expectedExceptions = RoutingException.class)
    public void testEmptyRoute() throws Exception {

        final List<Object> address = emptyList();
        final Invocation invocation = spy(Invocation.class);
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        getRoutingStrategy().invokeSync(address, invocation, asyncConsumers, invocationErrorConsumer);

    }

    @Test(expectedExceptions = RoutingException.class)
    public void testConflictingRoute() throws Exception {

        final List<Object> address = asList(
            forInstanceAndApplication(randomInstanceId(), randomApplicationId()),
            forInstanceAndApplication(randomInstanceId(), randomApplicationId()),
            forInstanceAndApplication(randomInstanceId(), randomApplicationId())
        );

        final Invocation invocation = spy(Invocation.class);
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        getRoutingStrategy().invokeSync(address, invocation, asyncConsumers, invocationErrorConsumer);

    }


    private NodeId generateNodeId() {
        return forInstanceAndApplication(instanceId, getApplicationId());
    }

    private List<Object> generateSaneAddress() {
        return asList(
            forInstanceAndApplication(instanceId, getApplicationId()),
            forInstanceAndApplication(instanceId, getApplicationId()),
            forInstanceAndApplication(instanceId, getApplicationId())
        );
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(RoutingStrategy.class).to(SameNodeIdRoutingStrategy.class);
        }

    }


}
