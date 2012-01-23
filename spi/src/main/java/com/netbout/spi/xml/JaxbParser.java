/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.xml;

import com.netbout.spi.Urn;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses text and builds JAXB-annotated object.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class JaxbParser {

    /**
     * The XML content.
     */
    private final transient String xml;

    /**
     * Public ctor.
     * @param txt The XML text
     */
    public JaxbParser(final String txt) {
        this.xml = txt;
    }

    /**
     * Do this text has this object?
     * @param type The type I'm expecting
     * @return Do we have this object inside?
     */
    public <T> boolean has(final Class<? extends T> type) {
        boolean has = false;
        if (this.xml != null && !this.xml.isEmpty()
            && this.xml.charAt(0) == '<') {
            final String namespace = new DomParser(this.xml).parse()
                .getDocumentElement()
                .getNamespaceURI();
            has = namespace != null
                && this.matches(this.namespace(type), Urn.create(namespace));
        }
        return has;
    }

    /**
     * Parse the text and return an object.
     * @param type The type I'm expecting
     */
    public <T> T parse(final Class<? extends T> type) {
        JAXBContext ctx;
        try {
            ctx = JAXBContext.newInstance(type);
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalArgumentException(ex);
        }
        Unmarshaller umrsh;
        try {
            umrsh = ctx.createUnmarshaller();
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalStateException(ex);
        }
        final Document dom = JaxbParser.clear(
            new DomParser(this.xml).parse(),
            type
        );
        T token;
        try {
            token = (T) umrsh.unmarshal(new DOMSource(dom), type).getValue();
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalArgumentException(ex);
        }
        return token;
    }

    /**
     * Change the namespace of the root element of this document (and
     * all its children) according to the requirements of the provided JAXB
     * type.
     * @param dom The document
     * @param type The type we're expecting
     * @return The same document
     */
    private static Document clear(final Document dom, final Class type) {
        Urn required = JaxbParser.namespace(type);
        if (required != null) {
            Urn actual;
            try {
                actual = new Urn(dom.getDocumentElement().getNamespaceURI());
            } catch (java.net.URISyntaxException ex) {
                throw new IllegalArgumentException(
                    "Invalid format of namespace in incoming document",
                    ex
                );
            }
            if (!JaxbParser.matches(required, actual)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Actual namespace is '%s' while '%s' is required",
                        actual,
                        required
                    )
                );
            }
            DomParser.rename(
                dom,
                dom.getDocumentElement(),
                actual,
                required
            );
        }
        return dom;
    }

    /**
     * Get namespace of the class.
     * @param type The type to get annotation from
     * @return The namespace of it
     */
    private static Urn namespace(final Class type) {
        final XmlType annot = (XmlType) type.getAnnotation(XmlType.class);
        if (annot == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Object of type '%s' is not @XmlType annotated entity",
                    type.getName()
                )
            );
        }
        Urn namespace = null;
        if (!"##default".equals(annot.namespace())) {
            try {
                namespace = new Urn(annot.namespace());
            } catch (java.net.URISyntaxException ex) {
                throw new IllegalArgumentException(
                    String.format(
                        "Invalid format of namespace in %s",
                        type.getName()
                    ),
                    ex
                );
            }
        }
        return namespace;
    }

    /**
     * Actual namespace matches the canonical one.
     * @param canonical The namespace as it should be, without a suffix
     * @param actual The actual namespace
     * @return Actual is a variation of a canonical one
     */
    private static boolean matches(final Urn canonical, final Urn actual) {
        boolean matches = false;
        if (canonical != null && actual != null) {
            matches = actual.toString().matches(
                String.format("^\\Q%s\\E(\\?.*)?$", canonical.toString())
            );
        }
        return matches;
    }

}
