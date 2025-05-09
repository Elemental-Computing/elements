package dev.getelements.elements.sdk.model.blockchain.contract;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;
import java.util.Objects;

@Schema
public class EVMInvokeContractResponse {

    @Schema(description = "The transaction hash.")
    private String transactionHash;

    @Schema(description = "The transaction index.")
    private long transactionIndex;

    @Schema(description = "The block hash that contains the transaction.")
    private String blockHash;

    @Schema(description = "The block number that contains the transaction.")
    private long blockNumber;

    @Schema(description = "The cumulative gas used.")
    private long cumulativeGasUsed;

    @Schema(description = "The gas used.")
    private long gasUsed;

    @Schema(description = "The contract address.")
    private String contractAddress;

    @Schema(description = "The root.")
    private String root;

    // status is only present on Byzantium transactions onwards
    // see EIP 658 https://github.com/ethereum/EIPs/pull/658
    @Schema(description = "The status. Only present on Byzantium transactions onwards.")
    private long status;

    @Schema(description = "The address of the sender.")
    private String from;

    @Schema(description = "The address of the recipient.")
    private String to;

    @Schema(description = "The logs of any emitted events.")
    private List<EVMTransactionLog> logs;

    @Schema(description = "The logs bloom.")
    private String logsBloom;

    @Schema(description = "The revert reason, if any.")
    private String revertReason;

    @Schema(description = "The type.")
    private String type;

    @Schema(description = "The effective gas price.")
    private String effectiveGasPrice;

    @Schema(description = 
            "The decoded log. Elements will attempt to decode the first log using the passed in " +
            "output types. This is made available to get any sort of return description from an emitted event, " +
            "as transactions that affect state or storage on a contract cannot directly return values.")
    private List<Object> decodedLog;

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public long getTransactionIndex() {
        return transactionIndex;
    }

    public long getTransactionIndexRaw() {
        return transactionIndex;
    }

    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public long getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(long cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public boolean isStatusOK() {
        return getStatus() == 1;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<EVMTransactionLog> getLogs() {
        return logs;
    }

    public void setLogs(List<EVMTransactionLog> logs) {
        this.logs = logs;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public String getRevertReason() {
        return revertReason;
    }

    public void setRevertReason(String revertReason) {
        this.revertReason = revertReason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEffectiveGasPrice() {
        return effectiveGasPrice;
    }

    public void setEffectiveGasPrice(String effectiveGasPrice) {
        this.effectiveGasPrice = effectiveGasPrice;
    }

    public List<Object> getDecodedLog() {
        return decodedLog;
    }

    public void setDecodedLog(List<Object> decodedLog) {
        this.decodedLog = decodedLog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EVMInvokeContractResponse that = (EVMInvokeContractResponse) o;
        return getTransactionIndex() == that.getTransactionIndex() && getBlockNumber() == that.getBlockNumber() && getCumulativeGasUsed() == that.getCumulativeGasUsed() && getGasUsed() == that.getGasUsed() && getStatus() == that.getStatus() && Objects.equals(getTransactionHash(), that.getTransactionHash()) && Objects.equals(getBlockHash(), that.getBlockHash()) && Objects.equals(getContractAddress(), that.getContractAddress()) && Objects.equals(getRoot(), that.getRoot()) && Objects.equals(getFrom(), that.getFrom()) && Objects.equals(getTo(), that.getTo()) && Objects.equals(getLogs(), that.getLogs()) && Objects.equals(getLogsBloom(), that.getLogsBloom()) && Objects.equals(getRevertReason(), that.getRevertReason()) && Objects.equals(getType(), that.getType()) && Objects.equals(getEffectiveGasPrice(), that.getEffectiveGasPrice()) && Objects.equals(getDecodedLog(), that.getDecodedLog());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTransactionHash(), getTransactionIndex(), getBlockHash(), getBlockNumber(), getCumulativeGasUsed(), getGasUsed(), getContractAddress(), getRoot(), getStatus(), getFrom(), getTo(), getLogs(), getLogsBloom(), getRevertReason(), getType(), getEffectiveGasPrice(), getDecodedLog());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EVMInvokeContractResponse{");
        sb.append("transactionHash='").append(transactionHash).append('\'');
        sb.append(", transactionIndex=").append(transactionIndex);
        sb.append(", blockHash='").append(blockHash).append('\'');
        sb.append(", blockNumber=").append(blockNumber);
        sb.append(", cumulativeGasUsed=").append(cumulativeGasUsed);
        sb.append(", gasUsed=").append(gasUsed);
        sb.append(", contractAddress='").append(contractAddress).append('\'');
        sb.append(", root='").append(root).append('\'');
        sb.append(", status=").append(status);
        sb.append(", from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", logs=").append(logs);
        sb.append(", logsBloom='").append(logsBloom).append('\'');
        sb.append(", revertReason='").append(revertReason).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", effectiveGasPrice='").append(effectiveGasPrice).append('\'');
        sb.append(", decodedLog=").append(decodedLog);
        sb.append('}');
        return sb.toString();
    }

}
