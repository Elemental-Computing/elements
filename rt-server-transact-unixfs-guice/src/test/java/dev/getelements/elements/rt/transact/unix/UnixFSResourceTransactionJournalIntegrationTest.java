package dev.getelements.elements.rt.transact.unix;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.transact.JournalTransactionalPersistenceDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.getelements.elements.sdk.cluster.id.NodeId.randomNodeId;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.*;

@Guice(modules = UnixFSResourceTransactionJournalIntegrationTest.Module.class)
public class UnixFSResourceTransactionJournalIntegrationTest {

    @Inject
    private NodeId nodeId;

    @Inject
    private UnixFSTransactionJournal journal;

    @Inject
    private JournalTransactionalPersistenceDriver journalTransactionalPersistenceDriver;

    @BeforeClass
    public void start() {
        journalTransactionalPersistenceDriver.start();
    }

    @AfterClass
    public void stop() {
        journalTransactionalPersistenceDriver.stop();
    }

    @Test
    public void testHeaderCounter() throws IOException {

        final Path temp = Files.createTempFile("test", "bin");
        final UnixFSJournalHeader header = new UnixFSJournalHeader();

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {
            fileChannel.write(ByteBuffer.allocate(1024 * 1024));
            final MappedByteBuffer mapped = fileChannel.map(READ_WRITE, 0, 1024*1024);
            header.setByteBuffer(mapped, 0);
        }

        final UnixFSAtomicLong atomicLong = header.counter.createAtomicLong();
        atomicLong.set(0);
        assertTrue(atomicLong.compareAndSet(0, 42));
        assertEquals(atomicLong.get(), 42);

    }

    @Test
    public void testGetMultipleEntries() {
        try (final var a = journal.newMutableEntry(nodeId);
             final var b = journal.newMutableEntry(nodeId);) {
            final UnixFSTransactionProgramBuilder builderA = a.getProgramBuilder();
            final UnixFSTransactionProgramBuilder builderB = b.getProgramBuilder();
            assertNotSame(builderA.getByteBuffer(), builderB.getByteBuffer());
        }
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {

            bind(NodeId.class).toInstance(randomNodeId());

            install(new UnixFSTransactionalPersistenceContextModule()
                .exposeDetailsForTesting()
                .withTestingDefaults()
            );

        }
    }

}
