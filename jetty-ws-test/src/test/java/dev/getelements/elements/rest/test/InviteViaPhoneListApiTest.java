package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.sdk.model.invite.PhoneMatchedInvitation;
import dev.getelements.elements.sdk.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.util.PhoneNormalizer.normalizePhoneNb;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class InviteViaPhoneListApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(InviteViaPhoneListApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private UserDao userDao;

    @Inject
    private ClientContext clientContext;

    @Inject
    private ClientContext notSUContext;

    @BeforeClass
    private void setUp() {
        clientContext.createSuperuser("invitingSuperUser").createSession();
    }

    @Test()
    public void shouldInviteUsersWithMatchedPhones() {
        final String matchedPhone1 = "+1234567890";
        final String matchedPhone2 = "7774567890";
        final String wrongPhone = "+987654321";
        final String unmatchedUserPhone = "+000000000";
        final String unmatchedListPhone = "+999999999";

        createUsersWithPhones(matchedPhone1, matchedPhone2, unmatchedUserPhone);
        final List<String> invitatePhonesList = asList(matchedPhone1, matchedPhone2, wrongPhone, unmatchedListPhone);

        final InviteViaPhonesRequest request = new InviteViaPhonesRequest();
        request.setPhoneNumbers(invitatePhonesList);

        final InviteViaPhonesResponse response = client
                .target(apiRoot + "/invite")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(InviteViaPhonesResponse.class);

        assertNotNull(response);
        List<PhoneMatchedInvitation> matched = response.getMatched();

        assertEquals(matched.size(), 2);
        assertEquals(matched.get(0).getProfileIds().size(), 1);
        assertEquals(matched.get(0).getPhoneNumber(), normalizePhoneNb(matchedPhone1).get());
        assertEquals(matched.get(1).getProfileIds().size(), 1);
        assertEquals(matched.get(1).getPhoneNumber(), normalizePhoneNb(matchedPhone2).get());
    }

    private void createUsersWithPhones(String... phones) {
        stream(phones).forEach(this::createUserWithPhoneAndProfile);
    }

    private void createUserWithPhoneAndProfile(String phone) {
        User user = notSUContext.createUser(UUID.randomUUID().toString()).getUser();
        notSUContext.createProfile(UUID.randomUUID().toString());
        user.setPrimaryPhoneNb(normalizePhoneNb(phone).orElse(null));
        userDao.updateUser(user);
    }
}
