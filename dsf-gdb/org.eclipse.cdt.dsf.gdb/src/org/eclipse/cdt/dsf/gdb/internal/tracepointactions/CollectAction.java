/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Added support for collecting char pointers as strings (bug 373707)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.tracepointactions;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Action used to tell GDB to collect different values from a tracepoint.
 * It corresponds to GDB's 'collect' action.
 *
 * As for GDB 7.4:
 *	collect[/s] EXPRESSIONS
 *	 The tracepoint collect command now takes an optional modifier "/s"
 *	 that directs it to dereference pointer-to-character types and
 *	 collect the bytes of memory up to a zero byte.  The behavior is
 *	 similar to what you see when you use the regular print command on a
 *	 string.  An optional integer following the "/s" sets a bound on the
 *	 number of bytes that will be collected.
 *
 * @since 3.0
 */
public class CollectAction extends AbstractTracepointAction {

	private static final String COLLECT_ACTION_ID = "org.eclipse.cdt.dsf.gdb.tracepointactions.CollectAction"; //$NON-NLS-1$
	private static final String COLLECT_ACTION_ELEMENT_NAME = "collectData"; //$NON-NLS-1$
	private static final String COLLECT_STRING_ATTR = "collectString"; //$NON-NLS-1$
	private static final String COLLECT_AS_STRING_ATTR = "collectAsString"; //$NON-NLS-1$
	private static final String COLLECT_AS_STRING_LIMIT_ATTR = "collectAsStringLimit"; //$NON-NLS-1$

	private String fCollectString = ""; //$NON-NLS-1$
	/** Indicates if we should ask GDB to collect character pointers as strings */
	private boolean fCharPtrAsStrings;
	/**
	 * Optional limit of the size of the string to collect for character pointers.
	 * Null will indicate that no limit is to be used.
	 * This value should be non-negative. */
	private Integer fCharPtrAsStringsLimit;

	@Override
	public String getDefaultName() {
		return MessagesForTracepointActions.TracepointActions_Untitled_Collect;
	}

	public String getCollectString() {
		return fCollectString;
	}

	public void setCollectString(String str) {
		fCollectString = str;
	}

	/**
	 * Indicates if this collect action will treat character pointers as strings.
	 * @since 4.1
	 */
	public boolean getCharPtrAsStrings() {
		return fCharPtrAsStrings;
	}

	/**
	 * Specify if this collect action should treat character pointers as strings.
	 * @since 4.1
	 */
	public void setCharPtrAsStrings(boolean enable) {
		fCharPtrAsStrings = enable;
	}

	/**
	 * Indicates the maximum number of bytes that should be collected
	 * when treating character pointers as strings
	 * @return null if no limit is to be used
	 * @return a non-negative integer indicating the limit
	 *
	 * @since 4.1
	 */
	public Integer getCharPtrAsStringsLimit() {
		return fCharPtrAsStringsLimit;
	}

	/**
	 * Specify the maximum number of bytes that should be collected when
	 * when treating character pointers as strings.
	 * @param limit A non-negative integer, or null of no limit should be used.
	 *
	 *  @since 4.1
	 */
	public void setCharPtrAsStringsLimit(Integer limit) {
		fCharPtrAsStringsLimit = limit;
	}

	@Override
	public String getIdentifier() {
		return COLLECT_ACTION_ID;
	}

	@Override
	public String getMemento() {
		String collectData = ""; //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement(COLLECT_ACTION_ELEMENT_NAME);

			// Store the different attributes of this collect action
			rootElement.setAttribute(COLLECT_STRING_ATTR, fCollectString);
			rootElement.setAttribute(COLLECT_AS_STRING_ATTR, Boolean.toString(fCharPtrAsStrings));
			rootElement.setAttribute(COLLECT_AS_STRING_LIMIT_ATTR,
					fCharPtrAsStringsLimit == null ? "" : fCharPtrAsStringsLimit.toString()); //$NON-NLS-1$

			doc.appendChild(rootElement);

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			collectData = s.toString("UTF8"); //$NON-NLS-1$

		} catch (Exception e) {
			GdbPlugin.log(e);
		}
		return collectData;
	}

	@Override
	public String getSummary() {
		// Return the exact format that will be sent to GDB.

		StringBuilder collectCmd = new StringBuilder("collect "); //$NON-NLS-1$
		if (fCharPtrAsStrings) {
			collectCmd.append("/s"); //$NON-NLS-1$
			if (fCharPtrAsStringsLimit != null) {
				// No space between /s and the limit
				collectCmd.append(fCharPtrAsStringsLimit.toString());
			}
			// Now add the space before we append what to collect.
			collectCmd.append(" "); //$NON-NLS-1$
		}
		// Finally, actually add what to collect
		collectCmd.append(fCollectString);

		return collectCmd.toString();
	}

	@Override
	public String getTypeName() {
		return MessagesForTracepointActions.TracepointActions_Collect_Name;
	}

	@Override
	public void initializeFromMemento(String data) {
		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(data))).getDocumentElement();

			fCollectString = root.getAttribute(COLLECT_STRING_ATTR);
			if (fCollectString == null)
				fCollectString = ""; //$NON-NLS-1$

			String asStrings = root.getAttribute(COLLECT_AS_STRING_ATTR);
			if (asStrings != null) {
				fCharPtrAsStrings = Boolean.valueOf(asStrings);
			} else {
				fCharPtrAsStrings = false;
			}

			fCharPtrAsStringsLimit = null;
			String asStringsLimit = root.getAttribute(COLLECT_AS_STRING_LIMIT_ATTR);
			if (asStringsLimit != null) {
				try {
					fCharPtrAsStringsLimit = Integer.valueOf(asStringsLimit);
				} catch (NumberFormatException e) {
					// leave as null to disable
				}
			}
		} catch (Exception e) {
			GdbPlugin.log(e);
		}
	}

	@Override
	public String toString() {
		return getSummary();
	}
}
