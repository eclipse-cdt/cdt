/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

public class CDescriptor implements ICDescriptor {

	final CDescriptorManager fManager;
	final IProject fProject;
	private COwner fOwner;

	private HashMap extMap = new HashMap(4);
	private HashMap extInfoMap = new HashMap(4);
	private Document dataDoc;

	protected static final String DESCRIPTION_FILE_NAME = ".cdtproject"; //$NON-NLS-1$

	private static final String CEXTENSION_NAME = "cextension"; //$NON-NLS-1$

	private static final String PROJECT_DESCRIPTION = "cdtproject"; //$NON-NLS-1$
	private static final String PROJECT_OWNER_ID = "id"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION = "extension"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTR_POINT = "point"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTR_ID = "id"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTRIBUTE = "attribute"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTRIBUTE_KEY = "key"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	private static final String PROJECT_DATA = "data"; //$NON-NLS-1$
	private static final String PROJECT_DATA_ITEM = "item"; //$NON-NLS-1$
	private static final String PROJECT_DATA_ID = "id"; //$NON-NLS-1$

	boolean fUpdating;
	boolean isInitializing = true;
	boolean bDirty = false;

	protected CDescriptor(CDescriptorManager manager, IProject project, String id) throws CoreException {
		fProject = project;
		fManager = manager;
		IPath projectLocation = project.getDescription().getLocation();

		if (projectLocation == null) {
			projectLocation = getProjectDefaultLocation(project);
		}
		IPath descriptionPath = projectLocation.append(DESCRIPTION_FILE_NAME);

		if (descriptionPath.toFile().exists()) {
			IStatus status;
			String ownerID = readCDTProjectFile(descriptionPath);
			if (!ownerID.equals("")) { //$NON-NLS-1$
				if (ownerID.equals(id)) {
					status = new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_EXISTS,
							CCorePlugin.getResourceString("CDescriptor.exception.projectAlreadyExists"), (Throwable)null); //$NON-NLS-1$
				} else {
					status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_MISMATCH,
							CCorePlugin.getResourceString("CDescriptor.exception.unmatchedOwnerId") + "<requested:" +id +"/ In file:" +ownerID+">", (Throwable)null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
				}
				throw new CoreException(status);
			}
		}
		fOwner = new COwner(manager.getOwnerConfiguration(id));
		fOwner.configure(project, this);
		isInitializing = false;
		save();
	}

	protected CDescriptor(CDescriptorManager manager, IProject project) throws CoreException {
		fProject = project;
		fManager = manager;
		IPath projectLocation = project.getDescription().getLocation();

		if (projectLocation == null) {
			projectLocation = getProjectDefaultLocation(project);
		}
		IPath descriptionPath = projectLocation.append(DESCRIPTION_FILE_NAME);

		if (!descriptionPath.toFile().exists()) {
			fOwner = new COwner(manager.getOwnerConfiguration(project));
			fOwner.configure(project, this);
			fManager.updateDescriptor(this);
		} else {
			String ownerId = readCDTProjectFile(descriptionPath);
			fOwner = new COwner(manager.getOwnerConfiguration(ownerId));
		}
		isInitializing = false;
	}

	protected CDescriptor(CDescriptorManager manager, IProject project, COwner owner) throws CoreException {
		fProject = project;
		fManager = manager;
		IPath projectLocation = project.getDescription().getLocation();

		if (projectLocation == null) {
			projectLocation = getProjectDefaultLocation(project);
		}
		IPath descriptionPath = projectLocation.append(DESCRIPTION_FILE_NAME);

		if (descriptionPath.toFile().exists()) {
			readCDTProjectFile(descriptionPath);
		}
		fOwner = owner;
		fOwner.configure(project, this);
		isInitializing = false;
		save();
	}

	private String readCDTProjectFile(IPath descriptionPath) throws CoreException {
		String ownerID = ""; //$NON-NLS-1$
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(descriptionPath.toFile());
			NodeList nodeList = document.getElementsByTagName(PROJECT_DESCRIPTION);
			if (nodeList != null && nodeList.getLength() > 0) {
				Node node = nodeList.item(0);
				if (node.hasAttributes()) {
					ownerID = node.getAttributes().getNamedItem(PROJECT_OWNER_ID).getNodeValue();
				}
				readProjectDescription(node);
				return ownerID;
			}
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
					CCorePlugin.getResourceString("CDescriptor.exception.missingElement"), null); //$NON-NLS-1$
			throw new CoreException(status);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.toString(), e);
			throw new CoreException(status);
		}
	}

	private static IPath getProjectDefaultLocation(IProject project) {
		return Platform.getLocation().append(project.getFullPath());
	}

	public ICOwnerInfo getProjectOwner() {
		return fOwner;
	}

	public String getPlatform() {
		return fOwner.getPlatform();
	}

	public IProject getProject() {
		return fProject;
	}

	synchronized public ICExtensionReference[] get(String extensionID) {
		CExtensionReference[] refs = (CExtensionReference[])extMap.get(extensionID);
		if (refs == null)
			return new ICExtensionReference[0];
		return refs;
	}

	synchronized public ICExtensionReference[] get(String extensionID, boolean update) {
		ICExtensionReference[] refs = get(extensionID);
		if (refs.length == 0 && update) {
			try {
				boolean oldIsInitializing = isInitializing;
				isInitializing = true;
				fOwner.update(fProject, this, extensionID);
				isInitializing = oldIsInitializing;
				updateIfDirty();
				refs = get(extensionID);
			} catch (CoreException e) {
			}
		}
		return refs;
	}

	private CExtensionReference createRef(String extensionPoint, String extension) {
		CExtensionReference extensions[] = (CExtensionReference[])extMap.get(extensionPoint);
		if (extensions == null) {
			extensions = new CExtensionReference[1];
			extMap.put(extensionPoint, extensions);
		} else {
			CExtensionReference[] newExtensions = new CExtensionReference[extensions.length + 1];
			System.arraycopy(extensions, 0, newExtensions, 0, extensions.length);
			extensions = newExtensions;
			extMap.put(extensionPoint, extensions);
		}
		extensions[extensions.length - 1] = new CExtensionReference(this, extensionPoint, extension);
		return extensions[extensions.length - 1];
	}

	synchronized public ICExtensionReference create(String extensionPoint, String extension) throws CoreException {
		boolean fireEvent = false;
		CExtensionReference extRef;
		synchronized (this) {
			extRef = createRef(extensionPoint, extension);
			updateOnDisk();
			if (!isInitializing) {
				fireEvent = true;
			}
		}
		if (fireEvent) {
			fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
		}
		return extRef;
	}

	synchronized public void remove(ICExtensionReference ext) throws CoreException {
		boolean fireEvent = false;
		synchronized (this) {
			CExtensionReference extensions[] = (CExtensionReference[])extMap.get(ext.getExtension());
			for (int i = 0; i < extensions.length; i++) {
				if (extensions[i] == ext) {
					System.arraycopy(extensions, i, extensions, i + 1, extensions.length - 1 - i);
					if (extensions.length > 1) {
						CExtensionReference[] newExtensions = new CExtensionReference[extensions.length - 1];
						System.arraycopy(extensions, 0, newExtensions, 0, newExtensions.length);
						extMap.put(ext.getExtension(), newExtensions);
					} else {
						extMap.remove(ext.getExtension());
					}
					updateOnDisk();
					if (!isInitializing) {
						fireEvent = true;
					}
				}
			}
		}
		if (fireEvent) {
			fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
		}
	}

	public void remove(String extensionPoint) throws CoreException {
		boolean fireEvent = false;
		synchronized (this) {
			CExtensionReference extensions[] = (CExtensionReference[])extMap.get(extensionPoint);
			if (extensions != null) {
				extMap.remove(extensionPoint);
				updateOnDisk();
				if (!isInitializing) {
					fireEvent = true;
				}
			}
		}
		if (fireEvent) {
			fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
		}
	}

	synchronized CExtensionInfo getInfo(CExtensionReference cProjectExtension) {
		CExtensionInfo info = (CExtensionInfo)extInfoMap.get(cProjectExtension);
		if (info == null) {
			info = new CExtensionInfo();
			extInfoMap.put(cProjectExtension, info);
		}
		return info;
	}

	protected IFile getFile() {
		return getProject().getFile(DESCRIPTION_FILE_NAME);
	}

	void save() throws CoreException {
		fManager.getWorkspace().run(new IWorkspaceRunnable() {

			public void run(IProgressMonitor mon) throws CoreException {
				String xml;
				bDirty = false;
				if (!fProject.isAccessible()) {
					return;
				}
				fUpdating = true;

				try {
					xml = getAsXML();
				} catch (IOException e) {
					IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e);
					throw new CoreException(s);
				} catch (TransformerException e) {
					IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e);
					throw new CoreException(s);
				} catch (ParserConfigurationException e) {
					IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e);
					throw new CoreException(s);
				}

				IFile rscFile = getFile();
				InputStream inputStream;
				try {
					inputStream = new ByteArrayInputStream(xml.getBytes("UTF8")); //$NON-NLS-1$
					if (rscFile.exists()) {
						if (rscFile.isReadOnly()) {
							// provide opportunity to checkout read-only .cdtproject file
							fManager.getWorkspace().validateEdit(new IFile[]{rscFile}, null);
						}
						rscFile.setContents(inputStream, IResource.FORCE, null);
					} else {
						rscFile.create(inputStream, IResource.FORCE, null);
					}
				} catch (UnsupportedEncodingException e) {
					IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e);
					throw new CoreException(s);
				}
				fUpdating = false;
			}
		}, fProject, IWorkspace.AVOID_UPDATE, null);
	}

	boolean isUpdating() {
		return fUpdating;
	}
	
	void updateIfDirty() {
		if ( bDirty ) {
			updateOnDisk();
		}
	}

	synchronized void updateOnDisk() {
		if (isUpdating()) {
			return;
		}
		if (isInitializing) {
			bDirty = true;
			return;
		}
		fUpdating = true;
		fManager.updateDescriptor(this);
	}

	void updateFromDisk() throws CoreException {
		COwner origOwner;
		HashMap origExtMap;
		HashMap origExtInfoMap;
		Document origDataDoc;
		synchronized (this) {
			IPath projectLocation = fProject.getDescription().getLocation();

			if (projectLocation == null) {
				projectLocation = getProjectDefaultLocation(fProject);
			}
			IPath descriptionPath = projectLocation.append(DESCRIPTION_FILE_NAME);
			if (!descriptionPath.toFile().exists()) {
				updateOnDisk();
				return;
			}

			origOwner = fOwner;
			origExtMap = extMap;
			origExtInfoMap = extInfoMap;
			origDataDoc = dataDoc;

			extMap = new HashMap(4);
			extInfoMap = new HashMap(4);
			dataDoc = null;

			try {
				String ownerId = readCDTProjectFile(descriptionPath);
				fOwner = new COwner(fManager.getOwnerConfiguration(ownerId));
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fOwner = origOwner;
				extMap = origExtMap;
				extInfoMap = origExtInfoMap;
				dataDoc = origDataDoc;
			}
		}
		if (!fOwner.equals(origOwner)) {
			fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.OWNER_CHANGED));
		} else {
			boolean extChanges = true;
			if (extMap.size() == origExtMap.size() && extInfoMap.size() == origExtInfoMap.size()) {
				extChanges = false;
				Iterator entries = extMap.entrySet().iterator();
				while (entries.hasNext()) {
					Entry entry = (Entry)entries.next();
					if (!origExtMap.containsKey(entry.getKey())) {
						extChanges = true;
						break;
					}
					CExtensionReference origExt[] = (CExtensionReference[])origExtMap.get(entry.getKey());
					CExtensionReference newExt[] = (CExtensionReference[])entry.getValue();
					if (!Arrays.equals(origExt, newExt)) {
						extChanges = true;
						break;
					}
				}
			}
			if (extChanges) {
				fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED,
						CDescriptorEvent.EXTENSION_CHANGED));
			} else {
				fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, 0));
			}
		}
	}

	private void readProjectDescription(Node node) {
		Node childNode;
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			childNode = list.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				if (childNode.getNodeName().equals(PROJECT_EXTENSION)) {
					try {
						decodeProjectExtension((Element)childNode);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				} else if (childNode.getNodeName().equals(PROJECT_DATA)) {
					try {
						decodeProjectData((Element)childNode);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}
	}

	private void decodeProjectExtension(Element element) throws CoreException {
		String point = element.getAttribute(PROJECT_EXTENSION_ATTR_POINT);
		String id = element.getAttribute(PROJECT_EXTENSION_ATTR_ID);
		CExtensionReference ext = createRef(point, id);
		NodeList extAttrib = element.getChildNodes();
		for (int j = 0; j < extAttrib.getLength(); j++) {
			if (extAttrib.item(j).getNodeName().equals(PROJECT_EXTENSION_ATTRIBUTE)) {
				NamedNodeMap attrib = extAttrib.item(j).getAttributes();
				getInfo(ext).setAttribute(attrib.getNamedItem(PROJECT_EXTENSION_ATTRIBUTE_KEY).getNodeValue(),
						attrib.getNamedItem(PROJECT_EXTENSION_ATTRIBUTE_VALUE).getNodeValue());
			}
		}
	}

	private void encodeProjectExtensions(Document doc, Element configRootElement) {
		Element element;
		Iterator extIterator = extMap.values().iterator();
		while (extIterator.hasNext()) {
			CExtensionReference extension[] = (CExtensionReference[])extIterator.next();
			for (int i = 0; i < extension.length; i++) {
				configRootElement.appendChild(element = doc.createElement(PROJECT_EXTENSION));
				element.setAttribute(PROJECT_EXTENSION_ATTR_POINT, extension[i].getExtension());
				element.setAttribute(PROJECT_EXTENSION_ATTR_ID, extension[i].getID());
				CExtensionInfo info = (CExtensionInfo)extInfoMap.get(extension[i]);
				if (info != null) {
					Iterator attribIterator = info.getAttributes().entrySet().iterator();
					while (attribIterator.hasNext()) {
						Entry entry = (Entry)attribIterator.next();
						Element extAttributes = doc.createElement(PROJECT_EXTENSION_ATTRIBUTE);
						extAttributes.setAttribute(PROJECT_EXTENSION_ATTRIBUTE_KEY, (String)entry.getKey());
						extAttributes.setAttribute(PROJECT_EXTENSION_ATTRIBUTE_VALUE, (String)entry.getValue());
						element.appendChild(extAttributes);
					}
				}
			}
		}
	}

	String getAsXML() throws IOException, TransformerException, ParserConfigurationException {
		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		ProcessingInstruction version = doc.createProcessingInstruction("eclipse-cdt", "version=\"2.0\""); //$NON-NLS-1$ //$NON-NLS-2$
		doc.appendChild(version);
		Element configRootElement = doc.createElement(PROJECT_DESCRIPTION);
		doc.appendChild(configRootElement);
		if (fOwner.getID().length() > 0) {
			configRootElement.setAttribute(PROJECT_OWNER_ID, fOwner.getID());
		}
		encodeProjectExtensions(doc, configRootElement);
		encodeProjectData(doc, configRootElement);
		return serializeDocument(doc);
	}

	protected ICExtension createExtensions(ICExtensionReference ext) throws CoreException {
		InternalCExtension cExtension = null;
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(ext.getExtension());
		IExtension extension = extensionPoint.getExtension(ext.getID());
		if (extension == null) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
					CCorePlugin.getResourceString("CDescriptor.exception.providerNotFound") + ":" + ext.getID(), null)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		IConfigurationElement element[] = extension.getConfigurationElements();
		for (int i = 0; i < element.length; i++) {
			if (element[i].getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
				cExtension = (InternalCExtension)element[i].createExecutableExtension("run"); //$NON-NLS-1$
				cExtension.setExtenionReference(ext);
				cExtension.setProject(fProject);
				break;
			}
		}
		return (ICExtension)cExtension;
	}

	protected IConfigurationElement[] getConfigurationElement(ICExtensionReference ext) throws CoreException {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(ext.getExtension());
		IExtension extension = extensionPoint.getExtension(ext.getID());
		if (extension == null) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
					CCorePlugin.getResourceString("CDescriptor.exception.providerNotFound"), null)); //$NON-NLS-1$
		}
		IConfigurationElement element[] = extension.getConfigurationElements();
		for (int i = 0; i < element.length; i++) {
			if (element[i].getName().equalsIgnoreCase(CEXTENSION_NAME)) {
				return element[i].getChildren();
			}
		}
		return new IConfigurationElement[0];
	}

	public synchronized Element getProjectData(String id) throws CoreException {
		Document doc = getProjectDataDoc();
		NodeList nodes = doc.getDocumentElement().getElementsByTagName(PROJECT_DATA_ITEM);
		for (int i = 0; i < nodes.getLength(); ++i) {
			Element element = (Element)nodes.item(i);
			if (element.getAttribute(PROJECT_DATA_ID).equals(id))
				return element;
		}

		// Not found, make a new one
		Element element = doc.createElement(PROJECT_DATA_ITEM);
		element.setAttribute(PROJECT_DATA_ID, id);
		doc.getDocumentElement().appendChild(element);
		return element;
	}

	public void saveProjectData() throws CoreException {
		save();
		fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, 0));
	}

	// The project data allows for the storage of any structured information
	// into the cdtproject file.
	synchronized private Document getProjectDataDoc() throws CoreException {
		if (dataDoc == null) {
			try {
				dataDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e) {
				throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.ERROR,
						CCorePlugin.getResourceString("CDescriptor.extension.internalError"), e)); //$NON-NLS-1$
			}
			Element rootElem = dataDoc.createElement(PROJECT_DATA);
			dataDoc.appendChild(rootElem);
		}
		return dataDoc;
	}

	private void decodeProjectData(Element data) throws CoreException {
		Document doc = getProjectDataDoc();
		doc.replaceChild(doc.importNode(data, true), doc.getDocumentElement());
	}

	private void encodeProjectData(Document doc, Element root) {
		// Don't create or encode the doc if it isn't there already
		if (dataDoc != null) {
			Element dataElements = dataDoc.getDocumentElement();
			NodeList nodes = dataElements.getElementsByTagName(PROJECT_DATA_ITEM);
			for (int i = 0; i < nodes.getLength(); ++i) {
				Element item = (Element)nodes.item(i);
				if (!item.hasChildNodes()) { // remove any empty item tags
					dataElements.removeChild(item);
					i--; //nodeList is live.... removeal changes nodelist
				}
			}
			root.appendChild(doc.importNode(dataDoc.getDocumentElement(), true));
		}
	}

	private String serializeDocument(Document doc) throws IOException, TransformerException {
		ByteArrayOutputStream s = new ByteArrayOutputStream();

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

		DOMSource source = new DOMSource(doc);
		StreamResult outputTarget = new StreamResult(s);
		transformer.transform(source, outputTarget);

		return s.toString("UTF8"); //$NON-NLS-1$			
	}
}
