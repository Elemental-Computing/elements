package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static dev.getelements.elements.sdk.Attributes.emptyAttributes;

public record InheritedAttributes(Attributes base, MutableAttributes current) implements MutableAttributes {

    public InheritedAttributes {

        if (base == null) {
            base = emptyAttributes();
        }

        if (current == null) {
            throw new IllegalArgumentException("current attributes cannot be null");
        }

    }

    @Override
    public void setAttribute(final String name, final Object obj) {
        current().setAttribute(name, obj);
    }

    @Override
    public Set<String> getAttributeNames() {
        final var result = new TreeSet<String>();
        result.addAll(base().getAttributeNames());
        result.addAll(current().getAttributeNames());
        return result;
    }

    @Override
    public Optional<Object> getAttributeOptional(final String name) {
        return current
                .getAttributeOptional(name)
                .or(() -> base().getAttributeOptional(name));
    }

    @Override
    public Attributes immutableCopy() {
        return ImmutableAttributes.copyOf(current);
    }

}
