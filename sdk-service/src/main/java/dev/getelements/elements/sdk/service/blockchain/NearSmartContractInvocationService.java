package dev.getelements.elements.sdk.service.blockchain;

import dev.getelements.elements.sdk.model.blockchain.contract.near.NearContractFunctionCallResult;
import dev.getelements.elements.sdk.model.blockchain.contract.near.NearInvokeContractResponse;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.sdk.service.blockchain.invoke.near.NearInvocationScope;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * A service which allows for invocation of Near based smart contracts.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface NearSmartContractInvocationService extends SmartContractInvocationService<NearSmartContractInvocationService.Invoker> {

    /**
     * Used to make invocations on the Near blockchain.
     */
    interface Invoker extends ScopedInvoker<NearInvocationScope> {


        /**
         * Gas Limits
         *
         * @deprecated migrate this to a configurable parameter
         */
        BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(6721975);

        /**
         * Sends a transaction to the blockchain with a specified receiver id.
         *
         * @param receiverId the id of the address to send the transactions to
         * @return the return value
         */
        default NearInvokeContractResponse sendDirect(final String receiverId) {
            return sendDirect(receiverId, List.of());
        }

        /**
         * Sends a transaction to the blockchain with a specified receiver id.
         *
         * @param receiverId the id of the address to send the transactions to
         * @param actions the actions to be performed by the transaction (see https://nomicon.io/RuntimeSpec/Actions)
         *                with their associated arguments. List(ActionName, Map(PropertyName, PropertyValue)).
         * @return the return value
         */
        NearInvokeContractResponse sendDirect(String receiverId, List<Map.Entry<String, Map<String, Object>>> actions);

        /**
         * Sends a transaction to the blockchain. Receiver id is determined by the invocation scope.
         * @return the return value
         */
        default NearInvokeContractResponse send() {
            return send(List.of());
        }

        /**
         * Sends a transaction to the blockchain. Receiver id is determined by the invocation scope.
         *
         * @param actions the actions to be performed by the transaction (see https://nomicon.io/RuntimeSpec/Actions)
         *                with their associated arguments. List(ActionName, Map(PropertyName, PropertyValue)).
         * @return the return value
         */
        NearInvokeContractResponse send(List<Map.Entry<String, Map<String, Object>>> actions);

        /**
         * Calls a script on the blockchain.
         *
         * @param methodName the Cadence script to execute
         * @return the return value
         */
        default NearContractFunctionCallResult call(final String accountId, final String methodName) {
            return call(accountId, methodName, Map.of());
        }

        /**
         * Executes a script, but does not write, to the blockchain.
         *
         * @param methodName the Cadence script to execute
         * @param arguments the arguments passed to the script itself
         * @return the return value
         */
        NearContractFunctionCallResult call(final String accountId, final String methodName, final Map<String, ?> arguments);

    }
}
