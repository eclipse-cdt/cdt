/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class ResumeAction extends AbstractBreakpointAction {

	int pauseTime = 0;

	public void execute(IBreakpoint breakpoint, IAdaptable context) {
		IResumeActionEnabler enabler = (IResumeActionEnabler) context.getAdapter(IResumeActionEnabler.class);
		if (enabler != null)
			try {
				enabler.resume();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public String getDefaultName() {
		return Messages.getString("ResumeAction.UntitledName"); //$NON-NLS-1$
	}

	public int getPauseTime() {
		return pauseTime;
	}

	public void setPauseTime(int pauseTime) {
		this.pauseTime = pauseTime;
	}

	public String getIdentifier() {
		return "org.eclipse.cdt.debug.ui.breakpointactions.ResumeAction"; //$NON-NLS-1$
	}

	public String getMemento() {
		String resumeData = new String(""); //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("resumeData"); //$NON-NLS-1$
			rootElement.setAttribute("pauseTime", Integer.toString(pauseTime)); //$NON-NLS-1$

			doc.appendChild(rootElement);

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			resumeData = s.toString("UTF8"); //$NON-NLS-1$

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resumeData;
	}

	public String getSummary() {
		if (pauseTime == 0)
			return Messages.getString("ResumeAction.SummaryImmediately"); //$NON-NLS-1$
		return MessageFormat.format(Messages.getString("ResumeAction.SummaryResumeTime"), new Object[] { new Integer(pauseTime) }); //$NON-NLS-1$
	}

	public String getTypeName() {
		return Messages.getString("ResumeAction.TypeName"); //$NON-NLS-1$
	}

	public void initializeFromMemento(String data) {
		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(data))).getDocumentElement();
			String value = root.getAttribute("pauseTime"); //$NON-NLS-1$
			if (value == null)
				throw new Exception();
			pauseTime = Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
