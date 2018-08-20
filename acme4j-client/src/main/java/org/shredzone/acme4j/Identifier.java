/*
 * acme4j - Java ACME client
 *
 * Copyright (C) 2018 Richard "Shred" Körber
 *   http://acme4j.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.acme4j;

import static java.util.Objects.requireNonNull;
import static org.shredzone.acme4j.toolbox.AcmeUtils.toAce;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.shredzone.acme4j.exception.AcmeProtocolException;
import org.shredzone.acme4j.toolbox.JSON;
import org.shredzone.acme4j.toolbox.JSONBuilder;

/**
 * Represents an identifier.
 * <p>
 * The ACME protocol only defines the DNS identifier, which identifies a domain name.
 * <p>
 * CAs may define further, proprietary identifier types.
 *
 * @since 2.3
 */
@ParametersAreNonnullByDefault
@Immutable
public class Identifier implements Serializable {
    private static final long serialVersionUID = -7777851842076362412L;

    /**
     * Type constant for DNS identifiers.
     */
    public static final String DNS = "dns";

    private static final String KEY_TYPE = "type";
    private static final String KEY_VALUE = "value";

    private final String type;
    private final String value;

    /**
     * Creates a new DNS identifier for the given domain name.
     *
     * @param domain
     *            Domain name. Unicode domains are automatically ASCII encoded.
     * @return New {@link Identifier}
     */
    public static Identifier dns(String domain) {
        return new Identifier(DNS, toAce(domain));
    }

    /**
     * Creates a new {@link Identifier}.
     * <p>
     * This is a generic constructor for identifiers. Refer to the documentation of your
     * CA to find out about the accepted identifier types and values.
     * <p>
     * Note that for DNS identifiers, no ASCII encoding of unicode domain takes place
     * here. Use {@link #dns(String)} instead.
     *
     * @param type
     *            Identifier type
     * @param value
     *            Identifier value
     */
    public Identifier(String type, String value) {
        this.type = requireNonNull(type, KEY_TYPE);
        this.value = requireNonNull(value, KEY_VALUE);
    }

    /**
     * Creates a new {@link Identifier} from the given {@link JSON} structure.
     *
     * @param json
     *            {@link JSON} containing the identifier data
     */
    public Identifier(JSON json) {
        this(json.get(KEY_TYPE).asString(), json.get(KEY_VALUE).asString());
    }

    /**
     * Returns the identifier type.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the identifier value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the domain name if this is a DNS identifier.
     *
     * @return Domain name. Unicode domains are ASCII encoded.
     * @throws AcmeProtocolException
     *             if this is not a DNS identifier.
     */
    public String getDomain() {
        if (!DNS.equals(type)) {
            throw new AcmeProtocolException("expected 'dns' identifier, but found '" + type + "'");
        }
        return value;
    }

    /**
     * Returns the identifier as JSON map.
     */
    public Map<String, Object> toMap() {
        return new JSONBuilder().put(KEY_TYPE, type).put(KEY_VALUE, value).toMap();
    }

    @Override
    public String toString() {
        return type + "=" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Identifier)) {
            return false;
        }

        Identifier i = (Identifier) obj;
        return type.equals(i.type) && value.equals(i.value);
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ value.hashCode();
    };

}
