/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.dmr.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class StringModelValue extends ModelValue {

    private final String value;

    StringModelValue(final String value) {
        super(ModelType.STRING);
        this.value = value;
    }

    @Override
    void writeExternal(final DataOutput out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    long asLong() {
        return Long.parseLong(value);
    }

    @Override
    long asLong(final long defVal) {
        return Long.parseLong(value);
    }

    @Override
    int asInt() {
        return Integer.parseInt(value);
    }

    @Override
    int asInt(final int defVal) {
        return Integer.parseInt(value);
    }

    @Override
    boolean asBoolean() {
        return Boolean.parseBoolean(value);
    }

    @Override
    boolean asBoolean(final boolean defVal) {
        return Boolean.parseBoolean(value);
    }

    @Override
    double asDouble() {
        return Double.parseDouble(value);
    }

    @Override
    double asDouble(final double defVal) {
        return Double.parseDouble(value);
    }

    @Override
    byte[] asBytes() {
        try {
            return value.getBytes("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return value.getBytes();
        }
    }

    @Override
    BigDecimal asBigDecimal() {
        return new BigDecimal(value);
    }

    @Override
    BigInteger asBigInteger() {
        return new BigInteger(value);
    }

    @Override
    String asString() {
        return value;
    }

    @Override
    ModelType asType() {
        return ModelType.valueOf(value);
    }

    @Override
    void format(final StringBuilder builder, final int indent, final boolean multiLine) {
        builder.append(quote(value));
    }

    @Override
    void formatAsJSON(final StringBuilder builder, final int indent, final boolean multiLine) {
        builder.append(jsonEscape(asString()));
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof StringModelValue && equals((StringModelValue)other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(final StringModelValue other) {
        return this == other || other != null && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
