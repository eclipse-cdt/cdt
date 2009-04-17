/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Remote file export description reader.
 */
public class RemoteFileExportDescriptionReader implements IRemoteFileExportDescriptionReader {
	protected InputStream fInputStream;

	/**
	 * Constructor.
	 */
	public RemoteFileExportDescriptionReader(InputStream inputStream) {
		Assert.isNotNull(inputStream);
		fInputStream = new BufferedInputStream(inputStream);
	}

	/**
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileExportDescriptionReader#read(org.eclipse.rse.internal.importexport.files.RemoteFileExportData)
	 */
	public void read(RemoteFileExportData exportData) throws CoreException {
		try {
			readXML(exportData);
		} catch (IOException ex) {
			String message = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, ex));
		} catch (SAXException ex) {
			String message = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, ex));
		}
	}

	public RemoteFileExportData readXML(RemoteFileExportData exportData) throws IOException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder parser = null;
		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new IOException(ex.getLocalizedMessage());
		}
		Element xmlFileDesc = parser.parse(new InputSource(fInputStream)).getDocumentElement();
		if (!xmlFileDesc.getNodeName().equals(Utilities.EXPORT_DESCRIPTION_EXTENSION)) {
			throw new IOException();
		}
		NodeList topLevelElements = xmlFileDesc.getChildNodes();
		for (int i = 0; i < topLevelElements.getLength(); i++) {
			Node node = topLevelElements.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) node;
			xmlReadDestinationLocation(exportData, element);
			xmlReadOptions(exportData, element);
			xmlReadSelectedElements(exportData, element);
		}
		return exportData;
	}

	private void xmlReadDestinationLocation(RemoteFileExportData exportData, Element element) {
		if (element.getNodeName().equals("destination")) { //$NON-NLS-1$
			exportData.setDestination(element.getAttribute("path")); //$NON-NLS-1$
		}
	}

	private void xmlReadOptions(RemoteFileExportData exportData, Element element) throws IOException {
		if (element.getNodeName().equals("options")) { //$NON-NLS-1$
			exportData.setReviewSynchronize(getBooleanAttribute(element, "reviewSynchronize")); //$NON-NLS-1$
			exportData.setOverWriteExistingFiles(getBooleanAttribute(element, "overWriteExistingFiles")); //$NON-NLS-1$
			exportData.setCreateDirectoryStructure(getBooleanAttribute(element, "createDirectoryStructure")); //$NON-NLS-1$
			exportData.setCreateSelectionOnly(getBooleanAttribute(element, "createSelectedOnly")); //$NON-NLS-1$
			exportData.setSaveSettings(getBooleanAttribute(element, "saveSettings")); //$NON-NLS-1$
			exportData.setDescriptionFilePath(element.getAttribute("descriptionFilePath")); //$NON-NLS-1$
		}
	}

	private void xmlReadSelectedElements(RemoteFileExportData exportData, Element element) throws IOException {
		if (element.getNodeName().equals("selectedElements")) { //$NON-NLS-1$
			NodeList selectedElements = element.getChildNodes();
			List elementsToExport = new ArrayList(selectedElements.getLength());
			for (int j = 0; j < selectedElements.getLength(); j++) {
				Node selectedNode = selectedElements.item(j);
				if (selectedNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				Element selectedElement = (Element) selectedNode;
				if (selectedElement.getNodeName().equals("file")) { //$NON-NLS-1$
					addFile(elementsToExport, selectedElement);
				} else if (selectedElement.getNodeName().equals("folder")) { //$NON-NLS-1$
					addFolder(elementsToExport, selectedElement);
				} else if (selectedElement.getNodeName().equals("project")) { //$NON-NLS-1$
					addProject(elementsToExport, selectedElement);
				}
			}
			exportData.setElements(elementsToExport);
		}
	}

	private void addFile(List selectedElements, Element element) throws IOException {
		IPath path = getPath(element);
		if (path != null) {
			IFile file = SystemBasePlugin.getWorkspace().getRoot().getFile(path);
			if (file != null) {
				selectedElements.add(file);
			}
		}
	}

	private void addFolder(List selectedElements, Element element) throws IOException {
		IPath path = getPath(element);
		if (path != null) {
			IFolder folder = SystemBasePlugin.getWorkspace().getRoot().getFolder(path);
			if (folder != null) {
				selectedElements.add(folder);
			}
		}
	}

	private void addProject(List selectedElements, Element element) throws IOException {
		String name = element.getAttribute("name"); //$NON-NLS-1$
		if (name.equals("")) { //$NON-NLS-1$
			throw new IOException();
		}
		IProject project = SystemBasePlugin.getWorkspace().getRoot().getProject(name);
		if (project != null) {
			selectedElements.add(project);
		}
	}

	private IPath getPath(Element element) throws IOException {
		String pathString = element.getAttribute("path"); //$NON-NLS-1$
		if (pathString.equals("")) { //$NON-NLS-1$
			throw new IOException();
		}
		return new Path(element.getAttribute("path")); //$NON-NLS-1$
	}

	protected boolean getBooleanAttribute(Element element, String name) throws IOException {
		String value = element.getAttribute(name);
		if (value != null && value.equalsIgnoreCase("true")) { //$NON-NLS-1$
			return true;
		}
		if (value != null && value.equalsIgnoreCase("false")) { //$NON-NLS-1$
			return false;
		}
		throw new IOException();
	}

	/**
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileExportDescriptionReader#close()
	 */
	public void close() throws CoreException {
		if (fInputStream != null) {
			try {
				fInputStream.close();
			} catch (IOException ex) {
				String message = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ""); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, ex));
			}
		}
	}

	/**
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileExportDescriptionReader#getStatus()
	 */
	public IStatus getStatus() {
		return new Status(IStatus.OK, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, "", null); //$NON-NLS-1$
	}
}
