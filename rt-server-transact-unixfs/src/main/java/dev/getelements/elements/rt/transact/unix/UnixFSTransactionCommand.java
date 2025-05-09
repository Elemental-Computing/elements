package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionParameter.*;

/**
 * Enumerates all commands that can take place during the scope of a transaction.
 */
public class UnixFSTransactionCommand {

    final UnixFSTransactionCommandHeader header;

    private UnixFSTransactionCommand(final UnixFSTransactionCommandHeader header) {
        this.header = header;
    }

    /**
     * Gets the phase.
     *
     * @return the {@link UnixFSTransactionProgramExecutionPhase}
     */
    public UnixFSTransactionProgramExecutionPhase getPhase() {
        return header.phase.get();
    }

    /**
     * Gets the instruction.
     *
     * @return the {@link UnixFSTransactionCommandInstruction}
     */
    public UnixFSTransactionCommandInstruction getInstruction() {
        return header.instruction.get();
    }

    /**
     * Gets the parameter at the supplied position. Throwing an instance of {@link UnixFSProgramCorruptionException} if there
     * is a problem extracting the parameter from the command.
     *
     * @param index
     * @return an instance of {@link UnixFSTransactionParameter}
     */
    public UnixFSTransactionParameter getParameterAt(final int index) {
        return UnixFSTransactionParameter.fromCommand(this, index);
    }

    /**
     * Gets a {@link Builder} used to construct an instance of {@link UnixFSTransactionCommand} command.
     *
     * @return a new {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Reads a new {@link UnixFSTransactionCommand} from the supplied {@link ByteBuffer}. This will read until the end
     * of the {@link ByteBuffer}
     *
     * @param byteBuffer the {@link ByteBuffer} to read from.
     */
    public static UnixFSTransactionCommand from(final ByteBuffer byteBuffer) {

        final ByteBuffer duplicate = byteBuffer.duplicate();

        final UnixFSTransactionCommandHeader header = new UnixFSTransactionCommandHeader();
        final int position = duplicate.position();
        header.setByteBuffer(duplicate, position);

        final long lLength = header.length.get();

        if (lLength > Integer.MAX_VALUE || lLength < 0) {
            throw new UnixFSProgramCorruptionException("Invalid command length: " + lLength);
        }

        duplicate.limit(position + (int) lLength);
        byteBuffer.position(position + (int) lLength);

        final ByteBuffer slice = duplicate.slice().asReadOnlyBuffer();
        header.setByteBuffer(slice, 0);

        return new UnixFSTransactionCommand(header);

    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        UnixFSTransactionCommandInstruction instruction;

        try {
            instruction = header.instruction.get();
        } catch (ArrayIndexOutOfBoundsException ex) {
            instruction = null;
        }

        UnixFSTransactionProgramExecutionPhase phase;

        try {
            phase = header.phase.get();
        } catch (ArrayIndexOutOfBoundsException ex) {
            phase = null;
        }

        sb.append(phase == null ? "<undefined>" : phase).append('.')
          .append(instruction == null ? "<undefined>" : instruction).append("(");

        final int count = header.parameterCount.get();

        for (int i = 0; i < count; ++i) {
            final UnixFSTransactionParameter parameter = getParameterAt(i);
            sb.append(parameter.toString());
            if (i < (count - 1)) sb.append(", ");
        }

        return sb.append(")").toString();

    }

    /**
     * USed to build an instance of {@link UnixFSTransactionCommand}. This configures the command header as well as each
     * additional parameter associated with the command.
     */
    public static class Builder {

        private Builder() {}

        private UnixFSTransactionProgramExecutionPhase executionPhase;

        private UnixFSTransactionCommandInstruction instruction;

        private final List<ParameterWriter> parameterOperations = new LinkedList<>();

        /**
         * Specifies the {@link UnixFSTransactionProgramExecutionPhase} of the command.
         *
         * @param executionPhase the {@link UnixFSTransactionProgramExecutionPhase}
         *
         * @return this instance
         */
        public Builder withPhase(final UnixFSTransactionProgramExecutionPhase executionPhase) {
            this.executionPhase = executionPhase;
            return this;
        }

        /**
         * Specifies the {@link UnixFSTransactionCommandInstruction} to execute.
         *
         * @param instruction the {@link UnixFSTransactionCommandInstruction}
         * @return
         */
        public Builder withInstruction(final UnixFSTransactionCommandInstruction instruction) {
            this.instruction = instruction;
            return this;
        }

        /**
         * Adds a string parameter to the transaction command.
         * @param value the string value
         * @return this instance
         */
        public Builder addStringParameter(final String value) {
            if (parameterOperations.size() >= Short.MAX_VALUE) throw new InternalException("Exceeded parameter count");
            parameterOperations.add((commandHeader, param) -> appendString(commandHeader, param, value));
            return this;
        }

        /**
         * Appends a {@link java.nio.file.Path} parameter.
         *
         * @param path the {@link java.nio.file.Path} parameter
         *
         * @return this instance
         */
        public Builder addFSPathParameter(final java.nio.file.Path path) {
            if (parameterOperations.size() >= Short.MAX_VALUE) throw new InternalException("Exceeded parameter count");
            parameterOperations.add((commandHeader, param) -> appendFSPath(commandHeader, param, path));
            return this;
        }

        /**
         * Appends a {@link Path} parameter.
         *
         * @param path the {@link Path} parameter
         *
         * @return this instance
         */
        public Builder addRTPathParameter(final Path path) {
            if (parameterOperations.size() >= Short.MAX_VALUE) throw new InternalException("Exceeded parameter count");
            parameterOperations.add((commandHeader, param) -> appendRTPath(commandHeader, param, path));
            return this;
        }

        /**
         * Appends a {@link ResourceId} parameter.
         *
         * @param resourceId the {@link ResourceId} parameter
         *
         * @return this instance
         */
        public Builder addResourceIdParameter(final ResourceId resourceId) {
            if (parameterOperations.size() >= Short.MAX_VALUE) throw new InternalException("Exceeded parameter count");
            parameterOperations.add((commandHeader, param) -> appendResourceId(commandHeader, param, resourceId));
            return this;
        }

        /**
         * Builds the {@link UnixFSTransactionCommand} by appending it and its commands to the supplied
         * {@link ByteBuffer}.
         *
         * @param byteBuffer the {@link ByteBuffer} which will receive the {@link UnixFSTransactionCommand}
         */
        public UnixFSTransactionCommand build(final ByteBuffer byteBuffer) {

            final ByteBuffer duplicate = byteBuffer.duplicate();

            // Counts the parameters and locks the position of the command to the current byte buffer position

            final int paramCount = parameterOperations.size();
            final int commandPosition = byteBuffer.position();

            // Creates the header and sets its byte buffer to the command position.
            final UnixFSTransactionCommandHeader header = new UnixFSTransactionCommandHeader();
            header.setByteBuffer(byteBuffer, commandPosition);

            // Fills the header bytes full of place holder data.
            for (int i = 0; i < UnixFSTransactionCommandHeader.SIZE; ++i) byteBuffer.put((byte)0xFF);

            // Set all headers to the desired values, overwriting previous clearing of the buffer.
            header.phase.set(executionPhase);
            header.instruction.set(instruction);
            header.parameterCount.set((short)paramCount);

            // Allocate space for the parameter headers, clearing each byte as we go.
            for (int i = 0; i < paramCount * UnixFSTransactionParameter.Header.SIZE; ++i) byteBuffer.put((byte)0xFF);

            // Writes all parameters to the byte buffer

            final ListIterator<ParameterWriter> listIterator = parameterOperations.listIterator();

            while (listIterator.hasNext()) {
                final int parameterIndex = listIterator.nextIndex();
                final ParameterWriter parameterWriter = listIterator.next();
                parameterWriter.write(header, parameterIndex);
            }

            final int commandLength = byteBuffer.position() - commandPosition;
            header.length.set(commandLength);
            duplicate.limit(commandPosition + commandLength);

            final ByteBuffer slice = duplicate.slice().asReadOnlyBuffer();
            header.setByteBuffer(slice, 0);

            return new UnixFSTransactionCommand(header);

        }

    }

    @FunctionalInterface
    private interface ParameterWriter {

        /**
         * Writes the command to the
         * @param header
         * @param parameter
         * @return the number of bytes written to the buffer
         */
        void write(UnixFSTransactionCommandHeader header, int parameter);

    }

}
