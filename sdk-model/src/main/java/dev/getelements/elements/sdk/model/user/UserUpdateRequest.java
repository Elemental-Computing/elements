package dev.getelements.elements.sdk.model.user;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

@Schema
public class UserUpdateRequest {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String name;

    @NotNull
    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    private String email;

    @Pattern(regexp = Constants.Regexp.PHONE_NB)
    private String primaryPhoneNb;

    @Pattern(regexp = Constants.Regexp.FIRST_NAME)
    private String firstName;

    @Pattern(regexp = Constants.Regexp.LAST_NAME)
    private String lastName;

    @Schema(description = "The user's plaintext password, only to be provided in POST/PUT requests in the User Resource " +
            "REST API interface. In the future, a dedicated REST API model may be constructed instead of using a " +
            "direct User model.")
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String password;

    @NotNull
    private User.Level level;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User.Level getLevel() {
        return level;
    }

    public void setLevel(User.Level level) {
        this.level = level;
    }

    public String getPrimaryPhoneNb() {
        return primaryPhoneNb;
    }

    public void setPrimaryPhoneNb(String primaryPhoneNb) {
        this.primaryPhoneNb = primaryPhoneNb;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdateRequest that = (UserUpdateRequest) o;
        return Objects.equals(name, that.name) && Objects.equals(email, that.email) && Objects.equals(primaryPhoneNb, that.primaryPhoneNb) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(password, that.password) && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, primaryPhoneNb, firstName, lastName, password, level);
    }

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", primaryPhoneNb='" + primaryPhoneNb + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", level=" + level +
                '}';
    }
}
