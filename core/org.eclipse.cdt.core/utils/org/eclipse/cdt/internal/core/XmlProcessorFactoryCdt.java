/*******************************************************************************
 *  Copyright (c) 2023 Joerg Kubitz and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * XML processing which prohibits external entities.
 *
 * Copied from https://github.com/eclipse-jdt/eclipse.jdt.debug/blob/1d59af8a6a37f9cf8143d73d94e4c1c3555363d6/org.eclipse.jdt.launching/launching/org/eclipse/jdt/internal/launching/XmlProcessorFactoryJdtDebug.java
 *
 * @see <a href="https://rules.sonarsource.com/java/RSPEC-2755/">RSPEC-2755</a>
 */
public class XmlProcessorFactoryCdt {
	private XmlProcessorFactoryCdt() {
		// static Utility only
	}

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY_ERROR_ON_DOCTYPE = createDocumentBuilderFactoryWithErrorOnDOCTYPE();
	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY_IGNORING_DOCTYPE = createDocumentBuilderFactoryIgnoringDOCTYPE();
	private static final SAXParserFactory SAX_FACTORY_ERROR_ON_DOCTYPE = createSAXFactoryWithErrorOnDOCTYPE(false);
	private static final SAXParserFactory SAX_FACTORY_ERROR_ON_DOCTYPE_NS = createSAXFactoryWithErrorOnDOCTYPE(true);
	private static final SAXParserFactory SAX_FACTORY_IGNORING_DOCTYPE = createSAXFactoryIgnoringDOCTYPE();

	/**
	 * Creates TransformerFactory which throws TransformerException when detecting external entities.
	 *
	 * @return javax.xml.transform.TransformerFactory
	 */
	public static TransformerFactory createTransformerFactoryWithErrorOnDOCTYPE() {
		TransformerFactory factory = TransformerFactory.newInstance();
		// prohibit the use of all protocols by external entities:
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); //$NON-NLS-1$
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); //$NON-NLS-1$
		return factory;
	}

	/**
	 * Creates DocumentBuilderFactory which throws SAXParseException when detecting external entities. It's magnitudes faster to call
	 * {@link #createDocumentBuilderWithErrorOnDOCTYPE()}.
	 *
	 * @return javax.xml.parsers.DocumentBuilderFactory
	 */
	public static synchronized DocumentBuilderFactory createDocumentBuilderFactoryWithErrorOnDOCTYPE() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// completely disable DOCTYPE declaration:
		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return factory;
	}

	/**
	 * Creates DocumentBuilderFactory which ignores external entities. It's magnitudes faster to call {@link #createDocumentBuilderIgnoringDOCTYPE()}.
	 *
	 * @return javax.xml.parsers.DocumentBuilderFactory
	 */
	public static synchronized DocumentBuilderFactory createDocumentBuilderFactoryIgnoringDOCTYPE() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			// completely disable external entities declarations:
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false); //$NON-NLS-1$
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return factory;
	}

	/**
	 * Creates DocumentBuilder which throws SAXParseException when detecting external entities. The builder is not thread safe.
	 *
	 * @return javax.xml.parsers.DocumentBuilder
	 * @throws ParserConfigurationException
	 */
	public static DocumentBuilder createDocumentBuilderWithErrorOnDOCTYPE() throws ParserConfigurationException {
		return DOCUMENT_BUILDER_FACTORY_ERROR_ON_DOCTYPE.newDocumentBuilder();
	}

	/**
	 * Creates DocumentBuilder which ignores external entities. The builder is not thread safe.
	 *
	 * @return javax.xml.parsers.DocumentBuilder
	 * @throws ParserConfigurationException
	 */
	public static DocumentBuilder createDocumentBuilderIgnoringDOCTYPE() throws ParserConfigurationException {
		return DOCUMENT_BUILDER_FACTORY_IGNORING_DOCTYPE.newDocumentBuilder();
	}

	/**
	 * Creates DocumentBuilderFactory which throws SAXParseException when detecting external entities.
	 *
	 * @return javax.xml.parsers.DocumentBuilderFactory
	 */
	public static SAXParserFactory createSAXFactoryWithErrorOnDOCTYPE() {
		SAXParserFactory f = SAXParserFactory.newInstance();
		try {
			// force org.xml.sax.SAXParseException for any DOCTYPE:
			f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return f;
	}

	private static synchronized SAXParserFactory createSAXFactoryWithErrorOnDOCTYPE(boolean awareness) {
		SAXParserFactory f = SAXParserFactory.newInstance();
		f.setNamespaceAware(awareness);
		try {
			// force org.xml.sax.SAXParseException for any DOCTYPE:
			f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return f;
	}

	private static synchronized SAXParserFactory createSAXFactoryIgnoringDOCTYPE() {
		SAXParserFactory f = SAXParserFactory.newInstance();
		try {
			// ignore DOCTYPE:
			f.setFeature("http://xml.org/sax/features/external-general-entities", false); //$NON-NLS-1$
			f.setFeature("http://xml.org/sax/features/external-parameter-entities", false); //$NON-NLS-1$
			f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return f;
	}

	/**
	 * Creates SAXParser which throws SAXParseException when detecting external entities.
	 *
	 * @return javax.xml.parsers.SAXParser
	 */

	public static SAXParser createSAXParserWithErrorOnDOCTYPE() throws ParserConfigurationException, SAXException {
		return createSAXParserWithErrorOnDOCTYPE(false);
	}

	/**
	 * Creates SAXParser which throws SAXParseException when detecting external entities.
	 *
	 * @param namespaceAware
	 *            parameter for SAXParserFactory
	 *
	 * @return javax.xml.parsers.SAXParser
	 */
	public static SAXParser createSAXParserWithErrorOnDOCTYPE(boolean namespaceAware)
			throws ParserConfigurationException, SAXException {
		if (namespaceAware) {
			return SAX_FACTORY_ERROR_ON_DOCTYPE_NS.newSAXParser();
		}
		return SAX_FACTORY_ERROR_ON_DOCTYPE.newSAXParser();
	}

	/**
	 * Creates SAXParser which does not throw Exception when detecting external entities but ignores them.
	 *
	 * @return javax.xml.parsers.SAXParser
	 */
	public static SAXParser createSAXParserIgnoringDOCTYPE()
			throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException, SAXException {
		SAXParser parser = SAX_FACTORY_IGNORING_DOCTYPE.newSAXParser();
		parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); //$NON-NLS-1$
		parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); //$NON-NLS-1$
		return parser;
	}
}