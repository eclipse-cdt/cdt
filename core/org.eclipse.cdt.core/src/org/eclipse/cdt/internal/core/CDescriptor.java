/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CDescriptor implements ICDescriptor {
	/* constants */
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private COwner fOwner;
	private IProject fProject;
	private HashMap extMap = new HashMap(4);
	private HashMap extInfoMap = new HashMap(4);

	final static String DESCRIPTION_FILE_NAME = ".cdtproject";
	private final static String PROJECT_DESCRIPTION = "cdtproject";
	private final static String PROJECT_EXTENSION = "extension";
	private final static String PROJECT_EXTENSION_ATTRIBUTE = "attribute";

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
			readCDTProject(descriptionPath);
			if (fOwner.getID().equals(id)) {
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
		readCDTProject(descriptionPath);
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
		fOwner = owner;
		readProjectExtensions(getCDTProjectNode(descriptionPath));
	}

	protected Node getCDTProjectNode(IPath descriptionPath) throws CoreException {
		FileInputStream file = null;
		try {
			file = new FileInputStream(descriptionPath.toFile());
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(file);
			Node node = document.getFirstChild();
			if (node.getNodeName().equals(PROJECT_DESCRIPTION))
				return node;
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

	protected void readCDTProject(IPath projectLocation) throws CoreException {
		Node node = getCDTProjectNode(projectLocation);
		fOwner = readProjectDescription(node);
	}

	protected IPath getProjectDefaultLocation(IProject project) {
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
		return (CExtensionReference[]) extMap.get(extensionID);
	}

	public ICExtensionReference[] get(String extensionID, boolean update) {
		ICExtensionReference[] ext = get(extensionID);
		if ((ext == null || ext.length == 0) && update) {
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
		setDirty();
		extensions[extensions.length - 1] = new CExtensionReference(this, extensionPoint, extensionID);
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
			extMap.put(extensionPoint, null);
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

	protected Node searchNode(Node target, String tagName) {
		NodeList list = target.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(tagName))
				return list.item(i);
		}
		return null;
	}

	protected String getString(Node target, String tagName) {
		Node node = searchNode(target, tagName);
		return node != null ? (node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue()) : null;
	}

	private COwner readProjectDescription(Node node) throws CoreException {
		COwner owner = null;
		NamedNodeMap attrib = node.getAttributes();
		owner = new COwner(attrib.getNamedItem("id").getNodeValue());
		readProjectExtensions(node);
		return owner;
	}

	private void readProjectExtensions(Node node) throws CoreException {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(PROJECT_EXTENSION)) {
				NamedNodeMap attrib = list.item(i).getAttributes();
				ICExtensionReference ext =
					create(attrib.getNamedItem("point").getNodeValue(), attrib.getNamedItem("id").getNodeValue());
				NodeList extAttrib = list.item(i).getChildNodes();
				for (int j = 0; j < extAttrib.getLength(); j++) {
					if (extAttrib.item(j).getNodeName().equals(PROJECT_EXTENSION_ATTRIBUTE)) {
						attrib = extAttrib.item(j).getAttributes();
						ext.setExtensionData(
							attrib.getNamedItem("key").getNodeValue(),
							attrib.getNamedItem("value").getNodeValue());
					}
				}
			}
		}
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

			Serializer serializer = SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(new OutputStreamWriter(s, "UTF8"), //$NON-NLS-1$
	format);
		serializer.asDOMSerializer().serialize(doc);
		return s.toString("UTF8"); //$NON-NLS-1$		
	}

	protected String getAsXML() throws IOException {
		Element element;
		Document doc = new DocumentImpl();
		Element configRootElement = doc.createElement(PROJECT_DESCRIPTION);
		doc.appendChild(configRootElement);
		configRootElement.setAttribute("id", fOwner.getID()); //$NON-NLS-1$
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
		return serializeDocument(doc);
	}

	public boolean isAutoSave() {
		return autoSave;
	}

	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
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
}
