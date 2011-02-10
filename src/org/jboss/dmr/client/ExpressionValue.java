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

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class ExpressionValue extends ModelValue {

    /**
     * JSON Key used to identify ExpressionValue.
     */
    public static final String TYPE_KEY = "EXPRESSION_VALUE";

    private final String expressionString;

    ExpressionValue(final String expressionString) {
        super(ModelType.EXPRESSION);
        if (expressionString == null) {
            throw new IllegalArgumentException("expressionString is null");
        }
        this.expressionString = expressionString;
    }

    @Override
    void writeExternal(final DataOutput out) throws IOException {
        out.writeUTF(expressionString);
    }

    @Override
    String asString() {
        return expressionString;
    }

    @Override
    void format(final StringBuilder builder, final int indent, final boolean multiLine) {
        builder.append("expression ").append(quote(expressionString));
    }

    @Override
    void formatAsJSON(final StringBuilder builder, final int indent, final boolean multiLine) {
        builder.append('{');
        if(multiLine) {
            indent(builder.append('\n'), indent + 1);
        } else {
            builder.append(' ');
        }
        builder.append(jsonEscape(TYPE_KEY));
        builder.append(" : ");
        builder.append(jsonEscape(asString()));
        if (multiLine) {
            indent(builder.append('\n'), indent);
        } else {
            builder.append(' ');
        }
        builder.append('}');
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ExpressionValue && equals((ExpressionValue)other);
    }

    public boolean equals(final ExpressionValue other) {
        return this == other || other != null && expressionString.equals(other.expressionString);
    }

    @Override
    public int hashCode() {
        return expressionString.hashCode();
    }

    @Override
    ModelValue resolve() {
        return new StringModelValue(replaceProperties(expressionString));
    }

    private static final int INITIAL = 0;
    private static final int GOT_DOLLAR = 1;
    private static final int GOT_OPEN_BRACE = 2;
    private static final int RESOLVED = 3;
    private static final int DEFAULT = 4;

    /**
     * Replace properties of the form:
     * <code>${<i>&lt;name&gt;[</i>,<i>&lt;name2&gt;[</i>,<i>&lt;name3&gt;...]][</i>:<i>&lt;default&gt;]</i>}</code>
     * @param value
     * @return
     */
    private static String replaceProperties(final String value) {
        final StringBuilder builder = new StringBuilder();
        final int len = value.length();
        int state = 0;
        int start = -1;
        int nameStart = -1;
        for (int i = 0; i < len; i = value.offsetByCodePoints(i, 1)) {
            final char ch = value.charAt(i);
            switch (state) {
                case INITIAL: {
                    switch (ch) {
                        case '$': {
                            state = GOT_DOLLAR;
                            continue;
                        }
                        default: {
                            builder.append(ch);
                            continue;
                        }
                    }
                    // not reachable
                }
                case GOT_DOLLAR: {
                    switch (ch) {
                        case '$': {
                            builder.append(ch);
                            state = INITIAL;
                            continue;
                        }
                        case '{': {
                            start = i + 1;
                            nameStart = start;
                            state = GOT_OPEN_BRACE;
                            continue;
                        }
                        default: {
                            // invalid; emit and resume
                            builder.append('$').append(ch);
                            state = INITIAL;
                            continue;
                        }
                    }
                    // not reachable
                }
                case GOT_OPEN_BRACE: {
                    switch (ch) {
                        case ':':
                        case '}':
                        case ',': {
                            final String name = value.substring(nameStart, i).trim();
                            if ("/".equals(name)) {
                                builder.append('/');
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            } else if (":".equals(name)) {
                                builder.append('/');
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            }
                            final String val = null;//System.getProperty(name);
                            if (val != null) {
                                builder.append(val);
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            } else if (ch == ',') {
                                nameStart = i + 1;
                                continue;
                            } else if (ch == ':') {
                                start = i + 1;
                                state = DEFAULT;
                                continue;
                            } else {
                                builder.append(value.substring(start - 2, i + 1));
                                state = INITIAL;
                                continue;
                            }
                        }
                        default: {
                            continue;
                        }
                    }
                    // not reachable
                }
                case RESOLVED: {
                    if (ch == '}') {
                        state = INITIAL;
                    }
                    continue;
                }
                case DEFAULT: {
                    if (ch == '}') {
                        state = INITIAL;
                        builder.append(value.substring(start, i));
                    }
                    continue;
                }
                default: throw new IllegalStateException();
            }
        }
        switch (state) {
            case GOT_DOLLAR: {
                builder.append('$');
                break;
            }
            case DEFAULT:
            case GOT_OPEN_BRACE: {
                builder.append(value.substring(start - 2));
                break;
            }
        }
        return builder.toString();
    }
}
