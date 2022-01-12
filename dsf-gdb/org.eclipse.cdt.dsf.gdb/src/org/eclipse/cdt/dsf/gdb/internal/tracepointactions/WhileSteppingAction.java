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
 *     Marc Khouzam (Ericsson) - Don't use translatable strings for the command summary
 *                               since it will be send directly to GDB
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
 * @since 3.0
 */
public class WhileSteppingAction extends AbstractTracepointAction {
	private static final String WHILE_STEPPING_ACTION_ID = "org.eclipse.cdt.dsf.gdb.tracepointactions.WhileSteppingAction"; //$NON-NLS-1$

	// The name of the sub actions
	private String fSubActionNames = ""; //$NON-NLS-1$
	// A comma-separated string of the actual content of each sub command
	// This is the string than can be sent to GDB
	private String fSubActionContent = ""; //$NON-NLS-1$
	// The number of steps this while-stepping command will occur
	private int fStepCount = 1;

	@Override
	public String getDefaultName() {
		return MessagesForTracepointActions.TracepointActions_Untitled_WhileStepping;
	}

	public String getSubActionsNames() {
		return fSubActionNames;
	}

	public void setSubActionsNames(String str) {
		fSubActionNames = str;
	}

	public String getSubActionsContent() {
		return fSubActionContent;
	}

	// Take all the sub action names, and find their corresponding action,
	// then build the content string
	public void setSubActionsContent(String subActionNames) {
		String[] names = subActionNames.split(","); //$NON-NLS-1$
		fSubActionContent = ""; //$NON-NLS-1$

		for (String name : names) {
			ITracepointAction action = TracepointActionManager.getInstance().findAction(name.trim());
			if (action != null) {
				fSubActionContent += action.getSummary() + ","; //$NON-NLS-1$
			}
		}
		// Remove last comma
		if (fSubActionContent.length() > 0) {
			fSubActionContent = fSubActionContent.substring(0, fSubActionContent.length() - 1);
		}
	}

	public int getStepCount() {
		return fStepCount;
	}

	public void setStepCount(int count) {
		fStepCount = count;
	}

	@Override
	public String getIdentifier() {
		return WHILE_STEPPING_ACTION_ID;
	}

	@Override
	public String getMemento() {
		String collectData = ""; //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("whileSteppingData"); //$NON-NLS-1$
			rootElement.setAttribute("whileSteppingCount", Integer.toString(fStepCount)); //$NON-NLS-1$
			rootElement.setAttribute("subActionNames", fSubActionNames); //$NON-NLS-1$

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
		// Create command to be sent to GDB
		return String.format("while-stepping %s %s", fStepCount, fSubActionContent); //$NON-NLS-1$
	}

	@Override
	public String getTypeName() {
		return MessagesForTracepointActions.TracepointActions_WhileStepping_Name;
	}

	@Override
	public void initializeFromMemento(String data) {
		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(data))).getDocumentElement();
			setStepCount(Integer.parseInt(root.getAttribute("whileSteppingCount"))); //$NON-NLS-1$
			setSubActionsNames(root.getAttribute("subActionNames")); //$NON-NLS-1$
			if (fSubActionNames == null)
				throw new Exception();
			setSubActionsContent(fSubActionNames);
		} catch (Exception e) {
			GdbPlugin.log(e);
		}
	}

	@Override
	public String toString() {
		return getSummary();
	}
}
