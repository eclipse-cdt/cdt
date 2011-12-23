/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.icu.text.MessageFormat;

/**
 * @since 3.0
 */
public class CollectAction extends AbstractTracepointAction {

	private static final String COLLECT_ACTION_ID = "org.eclipse.cdt.dsf.gdb.tracepointactions.CollectAction"; //$NON-NLS-1$
	
	private String fCollectString = ""; //$NON-NLS-1$
	
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

	@Override
	public String getIdentifier() {
		return COLLECT_ACTION_ID;
	}

	@Override
	public String getMemento() {
		String collectData = new String(""); //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("collectData"); //$NON-NLS-1$
			rootElement.setAttribute("collectString", fCollectString); //$NON-NLS-1$

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
			e.printStackTrace();
		}
		return collectData;
	}

	@Override
	public String getSummary() {
		return MessageFormat.format(MessagesForTracepointActions.TracepointActions_Collect_text, new Object[] { fCollectString });
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
			fCollectString = root.getAttribute("collectString"); //$NON-NLS-1$
			if (fCollectString == null)
				throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return MessageFormat.format(MessagesForTracepointActions.TracepointActions_Collect_text, new Object[] { fCollectString });
	}
}
