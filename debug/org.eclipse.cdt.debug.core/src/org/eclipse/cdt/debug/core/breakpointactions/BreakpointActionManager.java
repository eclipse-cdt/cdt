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
package org.eclipse.cdt.debug.core.breakpointactions;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IBreakpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class BreakpointActionManager {

	public static final String BREAKPOINT_ACTION_ATTRIBUTE = "BREAKPOINT_ACTIONS"; //$NON-NLS-1$
	private static final String BREAKPOINT_ACTION_DATA = "BreakpointActionManager.actionData"; //$NON-NLS-1$

	private IExtension[] breakpointActionExtensions = null;
	private ArrayList breakpointActions = null;

	public BreakpointActionManager() {
	}

	public void addAction(IBreakpointAction action) {
		getBreakpointActions().add(action);
	}

	private IBreakpointAction createActionFromClassName(String name, String className) {

		IBreakpointAction action = null;
		IExtension[] actionExtensions = CDebugCorePlugin.getDefault().getBreakpointActionManager().getBreakpointActionExtensions();

		try {

			for (int i = 0; i < actionExtensions.length && action == null; i++) {
				IConfigurationElement[] elements = actionExtensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length && action == null; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(CDebugCorePlugin.ACTION_TYPE_ELEMENT)) {
						if (element.getAttribute("class").equals(className)) { //$NON-NLS-1$
							action = (IBreakpointAction) element.createExecutableExtension("class"); //$NON-NLS-1$
							action.setName(name);
							CDebugCorePlugin.getDefault().getBreakpointActionManager().addAction(action);
						}
					}
				}
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}

		return action;
	}

	public void deleteAction(IBreakpointAction action) {
		getBreakpointActions().remove(action);
	}

	public boolean breakpointHasActions(IBreakpoint breakpoint) {
		if (breakpoint != null) {
			IMarker marker = breakpoint.getMarker();
			String actionNames = marker.getAttribute(BREAKPOINT_ACTION_ATTRIBUTE, ""); //$NON-NLS-1$
			return actionNames.length() > 0;
		}
		return false;
	}
	
	public void executeActions(IBreakpoint breakpoint, IAdaptable context) {

		if (breakpoint != null) {
			IMarker marker = breakpoint.getMarker();
			String actionNames = marker.getAttribute(BREAKPOINT_ACTION_ATTRIBUTE, ""); //$NON-NLS-1$
			StringTokenizer tok = new StringTokenizer(actionNames, ","); //$NON-NLS-1$
			while (tok.hasMoreTokens()) {
				String actionName = tok.nextToken();
				IBreakpointAction action = findBreakpointAction(actionName);
				if (action != null) {
					action.execute(breakpoint, context);
				}
			}
		}
	}

	public IBreakpointAction findBreakpointAction(String name) {
		for (Iterator iter = getBreakpointActions().iterator(); iter.hasNext();) {
			IBreakpointAction action = (IBreakpointAction) iter.next();
			if (action.getName().equals(name))
				return action;
		}
		return null;
	}

	public IExtension[] getBreakpointActionExtensions() {
		if (breakpointActionExtensions == null) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CDebugCorePlugin.PLUGIN_ID, CDebugCorePlugin.BREAKPOINT_ACTION_EXTENSION_POINT_ID);
			if (point == null)
				breakpointActionExtensions = new IExtension[0];
			else {
				breakpointActionExtensions = point.getExtensions();
			}
		}

		return breakpointActionExtensions;
	}

	public ArrayList getBreakpointActions() {
		if (breakpointActions == null) {
			breakpointActions = new ArrayList();
			CDebugCorePlugin.getDefault().getBreakpointActionManager().loadActionData();
		}
		return breakpointActions;
	}

	private void loadActionData() {

		String actionData = CDebugCorePlugin.getDefault().getPluginPreferences().getString(BREAKPOINT_ACTION_DATA);

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

						IBreakpointAction action = createActionFromClassName(name, className);
						action.initializeFromMemento(value);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String makeUniqueActionName(String defaultName) {
		String result = defaultName;
		IBreakpointAction action = findBreakpointAction(defaultName);
		int actionCount = 1;
		while (action != null) {
			result = defaultName + "(" + actionCount + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			action = findBreakpointAction(result);
			actionCount++;
		}
		return result;
	}

	public void revertActionData() {
		breakpointActions = null;
	}

	public void saveActionData() {
		String actionData = new String(""); //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("breakpointActionData"); //$NON-NLS-1$
			doc.appendChild(rootElement);

			for (Iterator iter = getBreakpointActions().iterator(); iter.hasNext();) {
				IBreakpointAction action = (IBreakpointAction) iter.next();

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
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue(BREAKPOINT_ACTION_DATA, actionData);
		CDebugCorePlugin.getDefault().savePluginPreferences();
	}

}
