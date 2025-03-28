package dev.getelements.elements.service.blockchain.crypto.flow;

import dev.getelements.elements.sdk.service.blockchain.FlowSmartContractInvocationService;
import dev.getelements.elements.sdk.service.blockchain.FlowSmartContractInvocationService.*;
import dev.getelements.elements.sdk.service.blockchain.SmartContractInvocationResolution;
import dev.getelements.elements.sdk.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.service.blockchain.invoke.StandardSmartContractInvocationResolution;
import dev.getelements.elements.service.blockchain.invoke.SuperUserSmartContractInvocationService;
import dev.getelements.elements.sdk.service.blockchain.invoke.flow.FlowInvocationScope;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class SuperUserFlowSmartContractInvocationService
        extends SuperUserSmartContractInvocationService<FlowInvocationScope, Invoker>
        implements FlowSmartContractInvocationService {

    private static final long DEFAULT_GAS_LIMIT = 100L;

    private ScopedInvoker.Factory<FlowInvocationScope, FlowSmartContractInvocationService.Invoker> scopedInvokerFactory;

    private Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> resolutionProvider;

    @Override
    protected FlowInvocationScope newInvocationScope() {
        final var scope = new FlowInvocationScope();
        scope.setGasLimit(DEFAULT_GAS_LIMIT);
        return scope;
    }

    @Override
    protected SmartContractInvocationResolution<Invoker> newResolution(final FlowInvocationScope flowInvocationScope) {
        final var resolution = getResolutionProvider().get();
        resolution.setScope(flowInvocationScope);
        resolution.setScopedInvokerFactory(getScopedInvokerFactory());
        return resolution;
    }

    public ScopedInvoker.Factory<FlowInvocationScope, Invoker> getScopedInvokerFactory() {
        return scopedInvokerFactory;
    }

    @Inject
    public void setScopedInvokerFactory(ScopedInvoker.Factory<FlowInvocationScope, Invoker> scopedInvokerFactory) {
        this.scopedInvokerFactory = scopedInvokerFactory;
    }

    public Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> getResolutionProvider() {
        return resolutionProvider;
    }

    @Inject
    public void setResolutionProvider(Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> resolutionProvider) {
        this.resolutionProvider = resolutionProvider;
    }

}
