/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.ICPathEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CDescriptor implements ICDescriptor {
	private ICPathEntry[] fPathEntries = new ICPathEntry[0];
	private COwner fOwner;
	private IProject fProject;
	private HashMap extMap = new HashMap(4);
	private HashMap extInfoMap = new HashMap(4);
	private Document dataDoc;

	static final String DESCRIPTION_FILE_NAME = ".cdtproject";
	private static final char[][] NO_CHAR_CHAR = new char[0][];
	private static final String PROJECT_DESCRIPTION = "cdtproject";
	private static final String PROJECT_EXTENSION = "extension";
	private static final String PROJECT_EXTENSION_ATTRIBUTE = "attribute";
	private static final String PATH_ENTRY = "cpathentry";
	private static final String PROJECT_DATA = "data";
	private static final String PROJECT_DATA_ITEM = "item";
	private static final String PROJECT_DATA_ID = "id";

	private boolean fDirty;
	private boolean autoSave;

	protected CDescriptor(IProject project, String id) throws CoreException {
		fProject = project;
		IPath projectLocation = project.getDescription().getLocation();

		final boolean isDefaultLocation = projectLocation == null;
		if (isDefaultLocation) {
			projectLocation = getProjectDefaultLocation(project);
		}
		IPath descriptionPath = projectLocation.append(DESCRIPTION_FILE_NAME);

		if (descriptionPath.toFile().exists()) {
			IStatus status;
			String ownerID = readCDTProject(descriptionPath);
			if (ownerID.equals(id)) {
				fOwner = new COwner(ownerID);
				status =
					new Status(
						IStatus.WARNING,
						CCorePlugin.PLUGIN_ID,
						CCorePlugin.STATUS_CDTPROJECT_EXISTS,
						"CDTProject already exisits",
						(Throwable) null);
			} else {
				status =
					new Status(
						IStatus.ERROR,
						CCorePlugin.PLUGIN_ID,
						CCorePlugin.STATUS_CDTPROJECT_MISMATCH,
						"CDTProject already exisits but does not match owner ID of creator",
						(Throwable) null);
			}
			throw new CoreException(status);
		}
		fOwner = new COwner(id);
	}

	protected CDescriptor(IProject project) throws CoreException {
		fProject = project;
		IPath projectLocation = project.getDescription().getLocation();

		final boolean isDefaultLocation = projectLocation == null;
		if (isDefaultLocation) {
			projectLocation = getProjectDefaultLocation(project);
		}
		IPath descriptionPath = projectLocation.append(DESCRIPTION_FILE_NAME);

		if (!descriptionPath.toFile().exists()) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, "CDTProject file not found", (Throwable) null);
			throw new CoreException(status);
		}
		fOwner = new COwner(readCDTProject(descriptionPath));
	}

	protected CDescriptor(IProject project, COwner owner) throws CoreException {
		fProject = project;
		IPath projectLocation = project.getDescription().getLocation();

		final boolean isDefaultLocation = projectLocation == null;
		if (isDefaultLocation) {
			projectLocation = getProjectDefaultLocation(project);
		}
		IPath descriptionPath = projectLocation.append(DESCRIPTION_FILE_NAME);

		if (!descriptionPath.toFile().exists()) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, "CDTProject file not found", (Throwable) null);
			throw new CoreException(status);
		}
		readCDTProject(descriptionPath);
		fOwner = owner;
		setDirty();
	}

	protected COwner getOwner() {
		return fOwner;
	}

	private String readCDTProject(IPath descriptionPath) throws CoreException {
		FileInputStream file = null;
		try {
			file = new FileInputStream(descriptionPath.toFile());
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(file);
			Node node = document.getFirstChild();
			if (node.getNodeName().equals(PROJECT_DESCRIPTION)) {
				return readProjectDescription(node);
			}
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, "Missing cdtproject element", null);
			throw new CoreException(status);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getLocalizedMessage(), e);
			throw new CoreException(status);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private IPath getProjectDefaultLocation(IProject project) {
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

	public ICExtensionReference[] get(String extensionID) {
		CExtensionReference[] refs = (CExtensionReference[]) extMap.get(extensionID);
		if (refs == null)
			return new ICExtensionReference[0];
		return refs;
	}

	public ICExtensionReference[] get(String extensionID, boolean update) {
		ICExtensionReference[] ext = get(extensionID);
		if (ext.length == 0 && update) {
			try {
				fOwner.update(fProject, this, extensionID);
				saveInfo();
				ext = get(extensionID);
			} catch (CoreException e) {
			}
		}
		return ext;
	}

	public ICExtensionReference create(String extensionPoint, String extensionID) throws CoreException {
		CExtensionReference extensions[] = (CExtensionReference[]) extMap.get(extensionPoint);
		if (extensions == null) {
			extensions = new CExtensionReference[1];
			extMap.put(extensionPoint, extensions);
		} else {
			CExtensionReference[] newExtensions = new CExtensionReference[extensions.length + 1];
			System.arraycopy(extensions, 0, newExtensions, 0, extensions.length);
			extensions = newExtensions;
			extMap.put(extensionPoint, extensions);
		}
		extensions[extensions.length - 1] = new CExtensionReference(this, extensionPoint, extensionID);
		setDirty();
		return extensions[extensions.length - 1];
	}

	public void remove(ICExtensionReference ext) throws CoreException {
		CExtensionReference extensions[] = (CExtensionReference[]) extMap.get(ext.getExtension());
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i] == ext) {
				System.arraycopy(extensions, i, extensions, i + 1, extensions.length - 1 - i);
				CExtensionReference[] newExtensions = new CExtensionReference[extensions.length - 1];
				System.arraycopy(extensions, 0, newExtensions, 0, extensions.length);
				extensions = newExtensions;
				if (extensions.length == 0) {
					extMap.put(ext.getExtension(), null);
				} else {
					extMap.put(ext.getExtension(), extensions);
				}
				setDirty();
			}
		}
	}

	public void remove(String extensionPoint) throws CoreException {
		CExtensionReference extensions[] = (CExtensionReference[]) extMap.get(extensionPoint);
		if (extensions != null) {
			extMap.remove(extensionPoint);
			setDirty();
		}
	}

	public CExtensionInfo getInfo(CExtensionReference cProjectExtension) {
		CExtensionInfo info = (CExtensionInfo) extInfoMap.get(cProjectExtension);
		if (info == null) {
			info = new CExtensionInfo();
			extInfoMap.put(cProjectExtension, info);
		}
		return info;
	}

	protected void saveInfo() throws CoreException {
		String xml;
		if (!isDirty()) {
			return;
		}
		try {
			xml = getAsXML();
		} catch (IOException e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e);
			throw new CoreException(s);
		}

		IFile rscFile = getProject().getFile(DESCRIPTION_FILE_NAME);
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		// update the resource content
		if (rscFile.exists()) {
			rscFile.setContents(inputStream, IResource.FORCE, null);
		} else {
			rscFile.create(inputStream, IResource.FORCE, null);
		}
		fDirty = false;
	}

	public boolean isAutoSave() {
		return autoSave;
	}

	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}

	protected void setDirty() throws CoreException {
		fDirty = true;
		if (isAutoSave())
			saveInfo();
	}

	protected boolean isDirty() {
		return fDirty;
	}

	protected String serializeDocument(Document doc) throws IOException {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setLineSeparator(System.getProperty("line.separator")); //$NON-NLS-1$
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(new OutputStreamWriter(s, "UTF8"), format);
		serializer.asDOMSerializer().serialize(doc);
		return s.toString("UTF8"); //$NON-NLS-1$		
	}

	private String readProjectDescription(Node node) throws CoreException {
		Node childNode;
		ArrayList pathEntries = new ArrayList();
		String ownerID = node.getAttributes().getNamedItem("id").getNodeValue();
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			childNode = list.item(i);
			if ( childNode.getNodeType() == Node.ELEMENT_NODE ) {
				if (childNode.getNodeName().equals(PROJECT_EXTENSION)) {
					//decodeProjectExtension((Element)node);
					decodeProjectExtension((Element)childNode);
				} else if (childNode.getNodeName().equals(PATH_ENTRY)) {
					//ICPathEntry entry = decodePathEntry((Element)node);
					ICPathEntry entry = decodePathEntry((Element)childNode);
					if (entry != null) {
						pathEntries.add(entry);
					}
				} else if (childNode.getNodeName().equals(PROJECT_DATA)) {
					decodeProjectData((Element)childNode);
				}
			}
		}
		fPathEntries = (ICPathEntry[]) pathEntries.toArray(new ICPathEntry[0]);
		return ownerID;
	}

	private void decodeProjectExtension(Element element) throws CoreException {
		ICExtensionReference ext = create(element.getAttribute("point"), element.getAttribute("id"));
		NodeList extAttrib = element.getChildNodes();
		for (int j = 0; j < extAttrib.getLength(); j++) {
			if (extAttrib.item(j).getNodeName().equals(PROJECT_EXTENSION_ATTRIBUTE)) {
				NamedNodeMap attrib = extAttrib.item(j).getAttributes();
				ext.setExtensionData(attrib.getNamedItem("key").getNodeValue(), attrib.getNamedItem("value").getNodeValue());
			}
		}
	}

	private ICPathEntry decodePathEntry(Element element) throws CoreException {
		IPath projectPath = fProject.getProject().getFullPath();
		String kindAttr = element.getAttribute("kind"); //$NON-NLS-1$
		String pathAttr = element.getAttribute("path"); //$NON-NLS-1$

		// ensure path is absolute
		IPath path = new Path(pathAttr);
		int kind = CPathEntry.kindFromString(kindAttr);
		if (kind != ICPathEntry.CDT_VARIABLE && !path.isAbsolute()) {
			path = projectPath.append(path);
		}
		// source attachment info (optional)
		IPath sourceAttachmentPath = element.hasAttribute("sourcepath") ? new Path(element.getAttribute("sourcepath")) : null;
		IPath sourceAttachmentRootPath = element.hasAttribute("rootpath") ? new Path(element.getAttribute("rootpath")) : null;
		IPath sourceAttachmentPrefixMapping =
			element.hasAttribute("prefixmapping") ? new Path(element.getAttribute("prefixmapping")) : null;

		// exclusion patterns (optional)
		String exclusion = element.getAttribute("excluding"); //$NON-NLS-1$ 
		IPath[] exclusionPatterns = CPathEntry.NO_EXCLUSION_PATTERNS;
		if (!exclusion.equals("")) { //$NON-NLS-1$ 
			char[][] patterns = splitOn('|', exclusion.toCharArray());
			int patternCount;
			if ((patternCount = patterns.length) > 0) {
				exclusionPatterns = new IPath[patternCount];
				for (int j = 0; j < patterns.length; j++) {
					exclusionPatterns[j] = new Path(new String(patterns[j]));
				}
			}
		}

		// recreate the CP entry
		switch (kind) {

			case ICPathEntry.CDT_PROJECT :
				return CCorePlugin.newProjectEntry(path);

			case ICPathEntry.CDT_LIBRARY :
				return CCorePlugin.newLibraryEntry(
					path,
					sourceAttachmentPath,
					sourceAttachmentRootPath,
					sourceAttachmentPrefixMapping);

			case ICPathEntry.CDT_SOURCE :
				// must be an entry in this project or specify another project
				String projSegment = path.segment(0);
				if (projSegment != null && projSegment.equals(fProject.getName())) { // this project
					return CCorePlugin.newSourceEntry(path, exclusionPatterns);
				} else { // another project
					return CCorePlugin.newProjectEntry(path);
				}

			case ICPathEntry.CDT_VARIABLE :
				return CCorePlugin.newVariableEntry(path, sourceAttachmentPath, sourceAttachmentRootPath);

			case ICPathEntry.CDT_INCLUDE :
				return CCorePlugin.newIncludeEntry(path, exclusionPatterns);

			default :
				{
					IStatus status =
						new Status(
							IStatus.ERROR,
							CCorePlugin.PLUGIN_ID,
							-1,
							"CPathEntry: unknown kind (" + kindAttr + ")",
							(Throwable) null);
					throw new CoreException(status);
				}
		}
	}

	private char[][] splitOn(char divider, char[] array) {
		int length = array == null ? 0 : array.length;

		if (length == 0)
			return NO_CHAR_CHAR;

		int wordCount = 1;
		for (int i = 0; i < length; i++)
			if (array[i] == divider)
				wordCount++;
		char[][] split = new char[wordCount][];
		int last = 0, currentWord = 0;
		for (int i = 0; i < length; i++) {
			if (array[i] == divider) {
				split[currentWord] = new char[i - last];
				System.arraycopy(array, last, split[currentWord++], 0, i - last);
				last = i + 1;
			}
		}
		split[currentWord] = new char[length - last];
		System.arraycopy(array, last, split[currentWord], 0, length - last);
		return split;
	}

	protected String getAsXML() throws IOException {
		Document doc = new DocumentImpl();
		Element configRootElement = doc.createElement(PROJECT_DESCRIPTION);
		doc.appendChild(configRootElement);
		configRootElement.setAttribute("id", fOwner.getID()); //$NON-NLS-1$
		encodeProjectExtensions(doc, configRootElement);
		encodePathEntries(doc, configRootElement);
		encodeProjectData(doc, configRootElement);
		return serializeDocument(doc);
	}

	private void encodeProjectExtensions(Document doc, Element configRootElement) {
		Element element;
		Iterator extIterator = extMap.values().iterator();
		while (extIterator.hasNext()) {
			CExtensionReference extension[] = (CExtensionReference[]) extIterator.next();
			for (int i = 0; i < extension.length; i++) {
				configRootElement.appendChild(element = doc.createElement(PROJECT_EXTENSION));
				element.setAttribute("point", extension[i].getExtension());
				element.setAttribute("id", extension[i].getID());
				CExtensionInfo info = (CExtensionInfo) extInfoMap.get(extension[i]);
				if (info != null) {
					Iterator attribIterator = info.getAttributes().entrySet().iterator();
					while (attribIterator.hasNext()) {
						Entry entry = (Entry) attribIterator.next();
						Element extAttributes = doc.createElement(PROJECT_EXTENSION_ATTRIBUTE);
						extAttributes.setAttribute("key", (String) entry.getKey());
						extAttributes.setAttribute("value", (String) entry.getValue());
						element.appendChild(extAttributes);
					}
				}
			}
		}
	}

	private void encodePathEntries(Document doc, Element configRootElement) {
		Element element;
		IPath projectPath = fProject.getProject().getFullPath();
		for (int i = 0; i < fPathEntries.length; i++) {
			configRootElement.appendChild(element = doc.createElement(PATH_ENTRY));
			element.setAttribute("kind", CPathEntry.kindToString(fPathEntries[i].getEntryKind())); //$NON-NLS-1$
			IPath xmlPath = fPathEntries[i].getPath();
			if (fPathEntries[i].getEntryKind() != ICPathEntry.CDT_VARIABLE) {
				// translate to project relative from absolute (unless a device path)
				if (xmlPath.isAbsolute()) {
					if (projectPath != null && projectPath.isPrefixOf(xmlPath)) {
						if (xmlPath.segment(0).equals(projectPath.segment(0))) {
							xmlPath = xmlPath.removeFirstSegments(1);
							xmlPath = xmlPath.makeRelative();
						} else {
							xmlPath = xmlPath.makeAbsolute();
						}
					}
				}
			}
			element.setAttribute("path", xmlPath.toString()); //$NON-NLS-1$
			if (fPathEntries[i].getSourceAttachmentPath() != null) {
				element.setAttribute("sourcepath", fPathEntries[i].getSourceAttachmentPath().toString()); //$NON-NLS-1$
			}
			if (fPathEntries[i].getSourceAttachmentRootPath() != null) {
				element.setAttribute("rootpath", fPathEntries[i].getSourceAttachmentRootPath().toString()); //$NON-NLS-1$
			}
			if (fPathEntries[i].getSourceAttachmentPrefixMapping() != null) {
				element.setAttribute("prefixmapping", fPathEntries[i].getSourceAttachmentPrefixMapping().toString()); //$NON-NLS-1$
			}
			IPath[] exclusionPatterns = fPathEntries[i].getExclusionPatterns();
			if (exclusionPatterns.length > 0) {
				StringBuffer excludeRule = new StringBuffer(10);
				for (int j = 0, max = exclusionPatterns.length; j < max; j++) {
					if (j > 0)
						excludeRule.append('|');
					excludeRule.append(exclusionPatterns[j]);
				}
				element.setAttribute("excluding", excludeRule.toString()); //$NON-NLS-1$
			}
		}
	}

	public void setPathEntries(ICPathEntry[] entries) throws CoreException {
		fPathEntries = entries;
		setDirty();
	}

	public ICPathEntry[] getPathEntries() {
		return fPathEntries;
	}

	protected ICExtension createExtensions(ICExtensionReference ext) throws CoreException {
		InternalCExtension cExtension = null;
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IExtensionPoint extensionPoint = pluginRegistry.getExtensionPoint(ext.getExtension());
		IExtension extension = extensionPoint.getExtension(ext.getID());
		IConfigurationElement element[] = extension.getConfigurationElements();
		for (int i = 0; i < element.length; i++) {
			if (element[i].getName().equalsIgnoreCase("cextension")) {
				cExtension = (InternalCExtension) element[i].createExecutableExtension("run");
				cExtension.setExtenionReference(ext);
				cExtension.setProject(fProject);
				break;
			}
		}
		return (ICExtension) cExtension;
	}
	
	// The project data allows for the storage of any structured information
	// into the cdtproject file.
	private Document getProjectDataDoc() throws CoreException {
		if (dataDoc == null) {
			try {
				dataDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e) {
				throw new CoreException(
					new Status(
						IStatus.ERROR,
						CCorePlugin.PLUGIN_ID,
						IStatus.ERROR,
						"getProjectDataDoc",
						e));
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

	public Element getProjectData(String id) throws CoreException {
		NodeList nodes = getProjectDataDoc().getDocumentElement().getElementsByTagName(PROJECT_DATA_ITEM);
		for (int i = 0; i < nodes.getLength(); ++i) {
			Element element = (Element)nodes.item(i);
			if (element.getAttribute(PROJECT_DATA_ID).equals(id))
				return element; 
		}

		// Not found, make a new one
		Element element = dataDoc.createElement(PROJECT_DATA_ITEM);
		element.setAttribute(PROJECT_DATA_ID, id);
		dataDoc.getDocumentElement().appendChild(element);
		return element;
	}
	
	public void saveProjectData() throws CoreException {
		setDirty();
	}
	
	private void encodeProjectData(Document doc, Element root) {
		// Don't create or encode the doc if it isn't there already
		if (dataDoc != null)
			root.appendChild(doc.importNode(dataDoc.getDocumentElement(), true));
	}
}
