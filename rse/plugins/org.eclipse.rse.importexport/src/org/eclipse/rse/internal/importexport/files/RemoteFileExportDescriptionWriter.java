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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Remote file export description writer.
 */
public class RemoteFileExportDescriptionWriter implements IRemoteFileExportDescriptionWriter {
	protected OutputStream fOutputStream;

	/**
	 * Constructor.
	 */
	public RemoteFileExportDescriptionWriter(OutputStream outputStream) {
		Assert.isNotNull(outputStream);
		fOutputStream = new BufferedOutputStream(outputStream);
	}

	/**
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileExportDescriptionWriter#write(org.eclipse.rse.internal.importexport.files.RemoteFileExportData)
	 */
	public void write(RemoteFileExportData exportData) throws CoreException {
		try {
			writeXML(exportData);
		} catch (IOException ex) {
			String message = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, ex));
		}
	}

	/**
	 * Writes a XML representation of file export data.
	 * @exception IOException if writing to the underlying stream fails.
	 */
	public void writeXML(RemoteFileExportData exportData) throws IOException {
		Assert.isNotNull(exportData);
		DocumentBuilder docBuilder = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new IOException(ex.getLocalizedMessage());
		}
		Document document = docBuilder.newDocument();
		// create the document
		Element xmlFileDesc = document.createElement(Utilities.EXPORT_DESCRIPTION_EXTENSION);
		document.appendChild(xmlFileDesc);
		xmlWriteDestinationLocation(exportData, document, xmlFileDesc);
		xmlWriteOptions(exportData, document, xmlFileDesc);
		xmlWriteSelectedElements(exportData, document, xmlFileDesc);
		try {
			// write the document to the stream
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(fOutputStream);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}

	private void xmlWriteDestinationLocation(RemoteFileExportData exportData, Document document, Element xmlFileDesc) throws DOMException {
		Element destination = document.createElement("destination"); //$NON-NLS-1$
		xmlFileDesc.appendChild(destination);
		destination.setAttribute("path", exportData.getDestination()); //$NON-NLS-1$
	}

	private void xmlWriteOptions(RemoteFileExportData exportData, Document document, Element xmlFileDesc) throws DOMException {
		Element options = document.createElement("options"); //$NON-NLS-1$
		xmlFileDesc.appendChild(options);
		options.setAttribute("reviewSynchronize", "" + exportData.isReviewSynchronize());  //$NON-NLS-1$//$NON-NLS-2$
		options.setAttribute("overWriteExistingFiles", "" + exportData.isOverWriteExistingFiles()); //$NON-NLS-1$ //$NON-NLS-2$
		options.setAttribute("createDirectoryStructure", "" + exportData.isCreateDirectoryStructure()); //$NON-NLS-1$ //$NON-NLS-2$
		options.setAttribute("createSelectedOnly", "" + exportData.isCreateSelectionOnly()); //$NON-NLS-1$ //$NON-NLS-2$
		options.setAttribute("saveSettings", "" + exportData.isSaveSettings()); //$NON-NLS-1$ //$NON-NLS-2$
		options.setAttribute("descriptionFilePath", "" + exportData.getDescriptionFilePath()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void xmlWriteSelectedElements(RemoteFileExportData exportData, Document document, Element xmlFileDesc) throws DOMException {
		Element selectedElements = document.createElement("selectedElements"); //$NON-NLS-1$
		xmlFileDesc.appendChild(selectedElements);
		List elements = exportData.getElements();
		Iterator iter = elements.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (element instanceof IResource) {
				add((IResource) element, selectedElements, document);
			}
		}
	}

	private void add(IResource resource, Element parent, Document document) {
		Element element = null;
		if (resource.getType() == IResource.PROJECT) {
			element = document.createElement("project"); //$NON-NLS-1$
			parent.appendChild(element);
			element.setAttribute("name", resource.getName()); //$NON-NLS-1$
			return;
		}
		if (resource.getType() == IResource.FILE) {
			element = document.createElement("file"); //$NON-NLS-1$
		} else if (resource.getType() == IResource.FOLDER) {
			element = document.createElement("folder"); //$NON-NLS-1$
		}
		if (element != null) {
			parent.appendChild(element);
			element.setAttribute("path", resource.getFullPath().toString()); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileExportDescriptionWriter#close()
	 */
	public void close() throws CoreException {
		if (fOutputStream != null) {
			try {
				fOutputStream.close();
			} catch (IOException ex) {
				String message = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ""); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, message, ex));
			}
		}
	}

	/**
	 * @see org.eclipse.rse.internal.importexport.files.IRemoteFileExportDescriptionWriter#getStatus()
	 */
	public IStatus getStatus() {
		return new Status(IStatus.OK, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, "", null); //$NON-NLS-1$
	}
}
