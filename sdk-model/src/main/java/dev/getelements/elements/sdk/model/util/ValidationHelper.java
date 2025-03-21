package dev.getelements.elements.sdk.model.util;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.ValidationFailureException;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.Set;

import static java.lang.System.arraycopy;

/**
 * Created by patricktwohig on 5/7/15.
 */
public class ValidationHelper {

    @Inject
    private Validator validator;

    /**
     * Validates the given model object.  If the model fails validation, then an instance
     * of ValidationFailureException is thrown.
     *
     * @param model the model
     * @throws ValidationFailureException if validation fails.
     */
    public <T> T validateModel(final T model) throws ValidationFailureException {

        if (model == null) {
            throw new InvalidDataException("Received null instance.");
        }

        final Set<ConstraintViolation<Object>> violationSet = validator.validate(model);

        if (!violationSet.isEmpty()) {
            throw new ValidationFailureException(violationSet, model);
        }

        return model;

    }

    /**
     * Validates the given model object.  If the model fails validation, then an instance of ValidationFailureException
     * is thrown.  In addition to the groups specified, this will assume the user is requesting validation from the
     * {@link Default} group as well.
     *
     * @param model the model
     * @param first the first group
     * @param remaining the remaining n groups
     * @throws ValidationFailureException if validation fails.
     */
    public <T> T validateModel(final T model,
                               final Class<?> first,
                               final Class<?> ... remaining) throws ValidationFailureException {

        if (model == null) {
            throw new InvalidDataException("Received null instance.");
        }

        final Class<?> groups[] = new Class[remaining.length + 2];
        groups[0] = Default.class;
        groups[1] = first;
        arraycopy(remaining, 0, groups, 2, remaining.length);

        final Set<ConstraintViolation<Object>> violationSet = validator.validate(model, groups);

        if (!violationSet.isEmpty()) {
            throw new ValidationFailureException(violationSet, model);
        }

        return model;

    }

}
