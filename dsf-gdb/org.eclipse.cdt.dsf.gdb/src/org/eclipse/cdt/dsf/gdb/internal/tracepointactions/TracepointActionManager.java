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
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.tracepointactions;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @since 3.0
 */
public class TracepointActionManager {

	private static final String TRACEPOINT_ACTION_DATA = "TracepointActionManager.actionData"; //$NON-NLS-1$
	private static final TracepointActionManager fTracepointActionManager = new TracepointActionManager();

	// We need a delimiter that the user won't type directly.
	// Bug 346215
	public static final String TRACEPOINT_ACTION_DELIMITER = "%_#"; //$NON-NLS-1$

	private ArrayList<ITracepointAction> tracepointActions = null;

	private TracepointActionManager() {
	}

	public static TracepointActionManager getInstance() {
		return fTracepointActionManager;
	}

	public void addAction(ITracepointAction action) {
		getActions().add(action);
	}

	public void deleteAction(ITracepointAction action) {
		getActions().remove(action);
	}

	public ITracepointAction findAction(String name) {
		for (ITracepointAction action : getActions()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}
		return null;
	}

	public ArrayList<ITracepointAction> getActions() {
		if (tracepointActions == null) {
			tracepointActions = new ArrayList<>();
			loadActionData();
		}
		return tracepointActions;
	}

	private void loadActionData() {

		String actionData = GdbPlugin.getDefault().getPluginPreferences().getString(TRACEPOINT_ACTION_DATA);

		if (actionData == null || actionData.length() == 0)
			return;

		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(actionData))).getDocumentElement();

			NodeList nodeList = root.getChildNodes();
			int entryCount = nodeList.getLength();

			for (int i = 0; i < entryCount; i++) {
				Node node = nodeList.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element subElement = (Element) node;
					String nodeName = subElement.getNodeName();
					if (nodeName.equalsIgnoreCase("actionEntry")) { //$NON-NLS-1$
						String name = subElement.getAttribute("name"); //$NON-NLS-1$
						if (name == null)
							throw new Exception();
						String value = subElement.getAttribute("value"); //$NON-NLS-1$
						if (value == null)
							throw new Exception();
						String className = subElement.getAttribute("class"); //$NON-NLS-1$
						if (className == null)
							throw new Exception();

						ITracepointAction action = (ITracepointAction) Class.forName(className).newInstance();
						action.setName(name);
						action.initializeFromMemento(value);
						addAction(action);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String makeUniqueActionName(String defaultName) {
		String result = defaultName;
		ITracepointAction action = findAction(defaultName);
		int actionCount = 1;
		while (action != null) {
			result = defaultName + "(" + actionCount + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			action = findAction(result);
			actionCount++;
		}
		return result;
	}

	public void revertActionData() {
		tracepointActions = null;
	}

	public void saveActionData() {
		String actionData = ""; //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("tracepointActionData"); //$NON-NLS-1$
			doc.appendChild(rootElement);

			for (Iterator<ITracepointAction> iter = getActions().iterator(); iter.hasNext();) {
				ITracepointAction action = iter.next();

				Element element = doc.createElement("actionEntry"); //$NON-NLS-1$
				element.setAttribute("name", action.getName()); //$NON-NLS-1$
				element.setAttribute("class", action.getClass().getName()); //$NON-NLS-1$
				element.setAttribute("value", action.getMemento()); //$NON-NLS-1$
				rootElement.appendChild(element);

			}

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			actionData = s.toString("UTF8"); //$NON-NLS-1$

		} catch (Exception e) {
			e.printStackTrace();
		}
		GdbPlugin.getDefault().getPluginPreferences().setValue(TRACEPOINT_ACTION_DATA, actionData);
		GdbPlugin.getDefault().savePluginPreferences();
	}

}
