/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProjectTargets {

	private static final String MAKE_TARGET_KEY = MakeCorePlugin.getUniqueIdentifier() + ".buildtargets"; //$NON-NLS-1$
	private static final String TARGETS_EXT = "targets"; //$NON-NLS-1$

	private static final String BUILD_TARGET_ELEMENT = "buildTargets"; //$NON-NLS-1$
	private static final String TARGET_ELEMENT = "target"; //$NON-NLS-1$
	private static final String TARGET_ATTR_ID = "targetID"; //$NON-NLS-1$
	private static final String TARGET_ATTR_PATH = "path"; //$NON-NLS-1$
	private static final String TARGET_ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String TARGET_STOP_ON_ERROR = "stopOnError"; //$NON-NLS-1$
	private static final String TARGET_USE_DEFAULT_CMD = "useDefaultCommand"; //$NON-NLS-1$
	private static final String TARGET_ARGUMENTS = "buildArguments"; //$NON-NLS-1$
	private static final String TARGET_COMMAND = "buildCommand"; //$NON-NLS-1$
	private static final String BAD_TARGET = "buidlTarget"; //$NON-NLS-1$
	private static final String TARGET = "buildTarget"; //$NON-NLS-1$

	private HashMap targetMap = new HashMap();

	private IProject project;

	public ProjectTargets(MakeTargetManager manager, IProject project) throws CoreException {
		boolean writeTargets = false;
		File targetFile = null;

		this.project = project;

		Document document = translateCDTProjectToDocument();

		//Historical ... fall back to the workspace and look in previous
		// location
		if (document == null || !document.hasChildNodes()) {
			IPath targetFilePath = MakeCorePlugin.getDefault().getStateLocation().append(project.getName()).addFileExtension(
					TARGETS_EXT);
			targetFile = targetFilePath.toFile();
			try {
				InputStream input = new FileInputStream(targetFile);
				document = translateInputStreamToDocument(input);
				writeTargets = true; // update cdtproject
			} catch (FileNotFoundException ex) {
				/* Ignore */
			}
		}

		if (document != null) {
			extractMakeTargetsFromDocument(document, manager);
			if (writeTargets) {
				try {
					Document doc = getAsXML();
					translateDocumentToCDTProject(doc);
				} catch (Exception e) {
					targetFile = null;
				}
				if (targetFile != null) {
					targetFile.delete(); // removed old
				}
			}
		}
	}

	protected String getString(Node target, String tagName) {
		Node node = searchNode(target, tagName);
		return node != null ? (node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue()) : null;
	}

	protected Node searchNode(Node target, String tagName) {
		NodeList list = target.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(tagName))
				return list.item(i);
		}
		return null;
	}

	public IMakeTarget[] get(IContainer container) {
		ArrayList list = (ArrayList) targetMap.get(container);
		if (list != null) {
			return (IMakeTarget[]) list.toArray(new IMakeTarget[list.size()]);
		}
		return new IMakeTarget[0];
	}

	public IMakeTarget findTarget(IContainer container, String name) {
		ArrayList list = (ArrayList) targetMap.get(container);
		if (list != null) {
			Iterator targets = list.iterator();
			while (targets.hasNext()) {
				IMakeTarget target = (IMakeTarget) targets.next();
				if (target.getName().equals(name)) {
					return target;
				}
			}
		}
		return null;
	}

	public void add(MakeTarget target) throws CoreException {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list != null && list.contains(target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("MakeTargetManager.target_exists"), null)); //$NON-NLS-1$
		}
		if (list == null) {
			list = new ArrayList();
			targetMap.put(target.getContainer(), list);
		}
		list.add(target);
	}

	public boolean contains(MakeTarget target) {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list != null && list.contains(target)) {
			return true;
		}
		return false;
	}

	public boolean remove(IMakeTarget target) {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list == null || !list.contains(target)) {
			return false;
		}
		boolean found = list.remove(target);
		if (list.size() == 0) {
			targetMap.remove(list);
		}
		return found;
	}

	public IProject getProject() {
		return project;
	}

	protected Document getAsXML() throws IOException {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			//This should never happen.
			throw new IOException("Error creating new XML storage document"); //$NON-NLS-1$
		}
		Element targetsRootElement = doc.createElement(BUILD_TARGET_ELEMENT);
		doc.appendChild(targetsRootElement);
		Iterator container = targetMap.entrySet().iterator();
		while (container.hasNext()) {
			List targets = (List) ((Map.Entry) container.next()).getValue();
			for (int i = 0; i < targets.size(); i++) {
				MakeTarget target = (MakeTarget) targets.get(i);
				targetsRootElement.appendChild(createTargetElement(doc, target));
			}
		}
		return doc;
	}

	private Node createTargetElement(Document doc, MakeTarget target) {
		Element targetElem = doc.createElement(TARGET_ELEMENT);
		targetElem.setAttribute(TARGET_ATTR_NAME, target.getName());
		targetElem.setAttribute(TARGET_ATTR_ID, target.getTargetBuilderID());
		targetElem.setAttribute(TARGET_ATTR_PATH, target.getContainer().getProjectRelativePath().toString());
		Element elem = doc.createElement(TARGET_COMMAND);
		targetElem.appendChild(elem);
		elem.appendChild(doc.createTextNode(target.getBuildCommand().toString()));

		if (target.getBuildArguments().length() > 0) {
			elem = doc.createElement(TARGET_ARGUMENTS);
			elem.appendChild(doc.createTextNode(target.getBuildArguments()));
			targetElem.appendChild(elem);
		}

		if (target.getBuildTarget().length() > 0) {
			elem = doc.createElement(TARGET);
			elem.appendChild(doc.createTextNode(target.getBuildTarget()));
			targetElem.appendChild(elem);
		}

		elem = doc.createElement(TARGET_STOP_ON_ERROR);
		elem.appendChild(doc.createTextNode(new Boolean(target.isStopOnError()).toString()));
		targetElem.appendChild(elem);

		elem = doc.createElement(TARGET_USE_DEFAULT_CMD);
		elem.appendChild(doc.createTextNode(new Boolean(target.isDefaultBuildCmd()).toString()));
		targetElem.appendChild(elem);
		return targetElem;
	}

	public void saveTargets() throws IOException {
		Document doc = getAsXML();
		//Historical method would save the output to the stream specified
		//translateDocumentToOutputStream(doc, output);
		try {
			translateDocumentToCDTProject(doc);
		} catch (Exception e) {
			IPath targetFilePath = MakeCorePlugin.getDefault().getStateLocation().append(project.getName()).addFileExtension(
					TARGETS_EXT);
			File targetFile = targetFilePath.toFile();
			try {
				saveTargets(doc, new FileOutputStream(targetFile));
			} catch (FileNotFoundException e1) {
			} catch (IOException e1) {
			} catch (TransformerException e1) {
			}
		}
	}

	protected void saveTargets(Document doc, OutputStream output) throws IOException, TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer;
		transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

		DOMSource source = new DOMSource(doc);
		StreamResult outputTarget = new StreamResult(output);
		transformer.transform(source, outputTarget);
	}
	/**
	 * This output method saves the information into the .cdtproject metadata file.
	 * 
	 * @param doc
	 * @throws IOException
	 */
	protected void translateDocumentToCDTProject(Document doc) throws CoreException, IOException {
		ICDescriptor descriptor;
		descriptor = CCorePlugin.getDefault().getCProjectDescription(getProject(), true);

		Element rootElement = descriptor.getProjectData(MAKE_TARGET_KEY);

		//Nuke the children since we are going to write out new ones
		NodeList kids = rootElement.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			rootElement.removeChild(kids.item(i));
			i--;
		}

		//Extract the root of our temporary document
		Node node = doc.getFirstChild();
		if (node.hasChildNodes()) {
			//Create a copy which is a part of the new document
			Node appendNode = rootElement.getOwnerDocument().importNode(node, true);
			//Put the copy into the document in the appropriate location
			rootElement.appendChild(appendNode);
		}
		//Save the results
		descriptor.saveProjectData();
	}

	/**
	 * This method parses the .cdtproject file for the XML document describing the build targets.
	 * 
	 * @param input
	 * @return
	 */
	protected Document translateCDTProjectToDocument() {
		Document document = null;
		Element rootElement = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			ICDescriptor descriptor;
			descriptor = CCorePlugin.getDefault().getCProjectDescription(getProject(), true);

			rootElement = descriptor.getProjectData(MAKE_TARGET_KEY);
		} catch (ParserConfigurationException e) {
			return document;
		} catch (CoreException e) {
			return document;
		}
		NodeList list = rootElement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Node appendNode = document.importNode(list.item(i), true);
				document.appendChild(appendNode);
				break; // should never have multiple <buildtargets>
			}
		}
		return document;
	}

	/**
	 * This method parses the input stream for the XML document describing the build targets.
	 * 
	 * @param input
	 * @return
	 */
	protected Document translateInputStreamToDocument(InputStream input) {
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
		} catch (Exception e) {
			MakeCorePlugin.log(e);
		}
		return document;
	}

	/**
	 * Extract the make target information which is contained in the XML Document
	 * 
	 * @param document
	 */
	protected void extractMakeTargetsFromDocument(Document document, MakeTargetManager manager) {
		Node node = document.getFirstChild();
		if (node != null && node.getNodeName().equals(BUILD_TARGET_ELEMENT)) {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				node = list.item(i);
				if (node.getNodeName().equals(TARGET_ELEMENT)) {
					IContainer container = null;
					NamedNodeMap attr = node.getAttributes();
					String path = attr.getNamedItem(TARGET_ATTR_PATH).getNodeValue();
					if (path != null && !path.equals("")) { //$NON-NLS-1$
						container = project.getFolder(path);
					} else {
						container = project;
					}
					try {
						MakeTarget target = new MakeTarget(manager, project, attr.getNamedItem(TARGET_ATTR_ID).getNodeValue(),
								attr.getNamedItem(TARGET_ATTR_NAME).getNodeValue());
						target.setContainer(container);
						String option = getString(node, TARGET_STOP_ON_ERROR);
						if (option != null) {
							target.setStopOnError(Boolean.valueOf(option).booleanValue());
						}
						option = getString(node, TARGET_USE_DEFAULT_CMD);
						if (option != null) {
							target.setUseDefaultBuildCmd(Boolean.valueOf(option).booleanValue());
						}
						option = getString(node, TARGET_COMMAND);
						if (option != null) {
							target.setBuildCommand(new Path(option));
						}
						option = getString(node, TARGET_ARGUMENTS);
						if (option != null) {
							target.setBuildArguments(option);
						}
						option = getString(node, BAD_TARGET);
						if (option != null) {
							target.setBuildTarget(option);
						}
						option = getString(node, TARGET);
						if (option != null) {
							target.setBuildTarget(option);
						}
						add(target);
					} catch (CoreException e) {
						MakeCorePlugin.log(e);
					}
				}
			}
		}
	}
}