package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.test.LargeObjectRequestFactory.DEFAULT_MIME_TYPE;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static org.testng.Assert.*;

public class AnonLargeObjectResourceTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(AnonLargeObjectResourceTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superuserClientContext;

    @Inject
    private LargeObjectRequestFactory requestFactory;

    @BeforeClass
    private void setUp() {
        superuserClientContext.createSuperuser("uploadingSuperuser").createSession();
    }

    @Test()
    public void shouldNotAllowToCreate() {

        CreateLargeObjectRequest request = requestFactory.createRequestWithFullAccess();

        final int status = client
                .target(apiRoot + "/large_object")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .getStatus();

        assertEquals(status, 403);
    }

    @Test()
    public void shouldGetLargeObject() {
        CreateLargeObjectRequest request = requestFactory.createRequestWithFullAccess();

        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, superuserClientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject foundlargeObject = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .get()
                .readEntity(LargeObject.class);

        assertNotNull(foundlargeObject);
        assertEquals(foundlargeObject.getMimeType(), DEFAULT_MIME_TYPE);
        assertTrue(foundlargeObject.getAccessPermissions().getRead().isWildcard());
        assertTrue(foundlargeObject.getAccessPermissions().getWrite().isWildcard());
        assertTrue(foundlargeObject.getAccessPermissions().getDelete().isWildcard());
    }
}
