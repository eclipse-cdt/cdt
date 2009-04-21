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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Remote file import description reader.
 */
public class RemoteFileImportDescriptionReader implements IRemoteFileImportDescriptionReader {
	protected InputStream fInputStream;
	protected IRemoteFileSubSystem subsystem;

	/**
	 * Constructor.
	 */
	public RemoteFileImportDescriptionReader(InputStream inputStream) {
		Assert.isNotNull(inputStream);
		fInputStream = new BufferedInputStream(inputStream);
	}

	/**
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileImportDescriptionReader#read(org.eclipse.rse.internal.importexport.files.RemoteFileImportData)
	 */
	public void read(RemoteFileImportData importData) throws CoreException {
		try {
			readXML(importData);
		} catch (IOException ex) {
			String message = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, ex));
		} catch (SAXException ex) {
			String message = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, ex));
		}
	}

	public RemoteFileImportData readXML(RemoteFileImportData importData) throws IOException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder parser = null;
		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new IOException(ex.getLocalizedMessage());
		}
		Element xmlFileDesc = parser.parse(new InputSource(fInputStream)).getDocumentElement();
		if (!xmlFileDesc.getNodeName().equals(Utilities.IMPORT_DESCRIPTION_EXTENSION)) {
			throw new IOException();
		}
		NodeList topLevelElements = xmlFileDesc.getChildNodes();
		for (int i = 0; i < topLevelElements.getLength(); i++) {
			Node node = topLevelElements.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) node;
			xmlReadDestinationLocation(importData, element);
			xmlReadOptions(importData, element);
			xmlReadSourceLocation(importData, element);
			xmlReadSelectedElements(importData, element);
		}
		return importData;
	}

	private void xmlReadDestinationLocation(RemoteFileImportData importData, Element element) {
		if (element.getNodeName().equals("destination")) { //$NON-NLS-1$
			String destinationPath = element.getAttribute("path"); //$NON-NLS-1$
			importData.setContainerPath(new Path(destinationPath));
		}
	}

	private void xmlReadOptions(RemoteFileImportData importData, Element element) throws IOException {
		if (element.getNodeName().equals("options")) { //$NON-NLS-1$
			try {	
				importData.setReviewSynchronize(getBooleanAttribute(element, "reviewSynchronize")); //$NON-NLS-1$
			}
			catch (IOException e){
				// this is a new option so if we're reading an older config file, this attribute doesn't exist
				importData.setReviewSynchronize(false);
			}
			importData.setOverWriteExistingFiles(getBooleanAttribute(element, "overWriteExistingFiles")); //$NON-NLS-1$
			importData.setCreateDirectoryStructure(getBooleanAttribute(element, "createDirectoryStructure")); //$NON-NLS-1$
			importData.setCreateSelectionOnly(getBooleanAttribute(element, "createSelectedOnly")); //$NON-NLS-1$
			importData.setSaveSettings(getBooleanAttribute(element, "saveSettings")); //$NON-NLS-1$
			importData.setDescriptionFilePath(element.getAttribute("descriptionFilePath")); //$NON-NLS-1$
		}
	}

	private void xmlReadSourceLocation(RemoteFileImportData importData, Element element) {
		if (element.getNodeName().equals("source")) { //$NON-NLS-1$
			String sourceCanonicalPath = element.getAttribute("path"); //$NON-NLS-1$
			IRemoteFile remoteFile = Utilities.parseForIRemoteFile(sourceCanonicalPath);
			UniFilePlus file = new UniFilePlus(remoteFile);
			importData.setSource(file);
			subsystem = remoteFile.getParentRemoteFileSubSystem();
		}
	}

	private void xmlReadSelectedElements(RemoteFileImportData importData, Element element) throws IOException {
		if (element.getNodeName().equals("selectedElements")) { //$NON-NLS-1$
			NodeList selectedElements = element.getChildNodes();
			List elementsToImport = new ArrayList(selectedElements.getLength());
			for (int j = 0; j < selectedElements.getLength(); j++) {
				Node selectedNode = selectedElements.item(j);
				if (selectedNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				Element selectedElement = (Element) selectedNode;
				if (selectedElement.getNodeName().equals("file")) { //$NON-NLS-1$
					addResource(importData, elementsToImport, selectedElement);
				} else if (selectedElement.getNodeName().equals("folder")) { //$NON-NLS-1$
					addResource(importData, elementsToImport, selectedElement);
				}
			}
			importData.setElements(elementsToImport);
		}
	}

	private void addResource(RemoteFileImportData importData, List selectedElements, Element element) throws IOException {
		String path = element.getAttribute("path"); //$NON-NLS-1$
		if (path != null && subsystem != null) {
			IRemoteFile remoteFile = null;
			try {
				remoteFile = subsystem.getRemoteFileObject(path, new NullProgressMonitor());
				if (remoteFile != null && remoteFile.exists()) {
					UniFilePlus file = new UniFilePlus(remoteFile);
					selectedElements.add(file);
					// add to list of import data
					importData.addToList(file);
				}
			} catch (SystemMessageException e) {
				SystemBasePlugin.logError("Error occured trying to retrieve file " + path, e); //$NON-NLS-1$
			}
		}
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
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileImportDescriptionReader#close()
	 */
	public void close() throws CoreException {
		if (fInputStream != null) {
			try {
				fInputStream.close();
				subsystem = null;
			} catch (IOException ex) {
				String message = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ""); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, ex));
			}
		}
	}

	/**
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileImportDescriptionReader#getStatus()
	 */
	public IStatus getStatus() {
		return new Status(IStatus.OK, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, "", null); //$NON-NLS-1$
	}
}
