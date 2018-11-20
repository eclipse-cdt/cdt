/*******************************************************************************
 * Copyright (c) 2008, 2011 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.settings.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.CConfigBasedDescriptorManager;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionStorageType.CProjectDescriptionStorageTypeProxy;
import org.eclipse.cdt.internal.core.settings.model.xml.XmlProjectDescriptionStorage;
import org.eclipse.cdt.internal.core.settings.model.xml2.XmlProjectDescriptionStorage2;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * Class that marshals creation of AbstractCProjectDescriptionStorages
 * for a given project.
 *
 * Persist Storage type ID in the .cproject file, and provides backwards compatibility
 * for existing project descriptions which don't encode the storage type in the project
 * description.
 */
public class CProjectDescriptionStorageManager {

	/* Extension point data */
	/** CProjectDescriptionStorage extension point ID */
	private static final String CPROJ_DESC_STORAGE_EXT_ID = "CProjectDescriptionStorage"; //$NON-NLS-1$
	/** storage type element ID */
	private static final String CPROJ_STORAGE_TYPE = "CProjectStorageType"; //$NON-NLS-1$

	// TODO provide some UI to select this
	/** Default project description Storage type */
	private static final String DEFAULT_STORAGE_TYPE = XmlProjectDescriptionStorage.STORAGE_TYPE_ID; //  XmlProjectDescriptionStorage2.STORAGE_TYPE_ID;
	/** Default project description Storage version */
	private static final Version DEFAULT_STORAGE_VERSION = XmlProjectDescriptionStorage.STORAGE_DESCRIPTION_VERSION; // XmlProjectDescriptionStorage2.STORAGE_DESCRIPTION_VERSION;

	/** Map of StorageType ID -> List of StorageTypes */
	private volatile Map<String, List<CProjectDescriptionStorageTypeProxy>> storageTypeMap;
	/** Map from IProject -> AbstractCProjectDescriptionStorage which is responsible for (de)serializing the project */
	private ConcurrentHashMap<IProject, AbstractCProjectDescriptionStorage> fDescriptionStorageMap = new ConcurrentHashMap<>();

	private volatile static CProjectDescriptionStorageManager instance;

	private CProjectDescriptionStorageManager() {
	}

	public static CProjectDescriptionStorageManager getInstance() {
		if (instance == null) {
			synchronized (CProjectDescriptionStorageManager.class) {
				if (instance == null)
					instance = new CProjectDescriptionStorageManager();
			}
		}
		return instance;
	}

	/**
	 * Return a AbstractCProjectDescriptionStorage for a particular project or
	 * null if none were found and the default storage type wasn't found
	 * @param project
	 * @return project description storage or null
	 */
	public AbstractCProjectDescriptionStorage getProjectDescriptionStorage(IProject project) {
		if (!project.isAccessible()) {
			assert (!fDescriptionStorageMap.contains(project));
			return null;
		}
		AbstractCProjectDescriptionStorage projStorage = fDescriptionStorageMap.get(project);
		if (projStorage == null) {
			projStorage = loadProjectStorage(project);
			fDescriptionStorageMap.putIfAbsent(project, projStorage);
		}
		return fDescriptionStorageMap.get(project);
	}

	/**
	 * Sets the Project description by delegating to the appropriate project storage type.
	 *
	 * If the storage type returns false on {@link ICProjectDescriptionStorageType#createsCProjectXMLFile()} then
	 * we create a .cproject file with type id and version.
	 *
	 * If no existing project storage is found then we throw a core exception (users should have called
	 * #getProjectDescriptionStorage(...) before calling this.
	 *
	 * @param project
	 * @param description
	 * @throws CoreException on failure
	 */
	public void setProjectDescription(IProject project, ICProjectDescription description, int flags,
			IProgressMonitor monitor) throws CoreException {
		AbstractCProjectDescriptionStorage storage = fDescriptionStorageMap.get(project);
		if (storage == null)
			throw ExceptionFactory
					.createCoreException("Can't set ProjectDescription before getProjectDescriptionStorage!"); //$NON-NLS-1$
		if (!storage.type.createsCProjectXMLFile())
			writeProjectStorageType(project, storage.type);
		storage.setProjectDescription(description, flags, monitor);
	}

	/**
	 * Persist the type and version of this particular project description storage type
	 * (for description storages that don't perform this job themselves).
	 * @param project
	 * @param type
	 */
	private void writeProjectStorageType(IProject project, CProjectDescriptionStorageTypeProxy type)
			throws CoreException {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			// Set the version
			ProcessingInstruction instruction = doc.createProcessingInstruction(
					ICProjectDescriptionStorageType.STORAGE_VERSION_NAME, type.version.toString());
			doc.appendChild(instruction);
			// Set the type id
			Element el = doc.createElement(ICProjectDescriptionStorageType.STORAGE_ROOT_ELEMENT_NAME);
			el.setAttribute(ICProjectDescriptionStorageType.STORAGE_TYPE_ATTRIBUTE, type.id);
			doc.appendChild(el);
			XmlUtil.prettyFormat(doc);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);

			InputStream input = new ByteArrayInputStream(stream.toByteArray());

			// Set the project description storage type
			IFile f = project.getFile(ICProjectDescriptionStorageType.STORAGE_FILE_NAME);
			if (!f.exists())
				f.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			ensureWritable(f);
			if (!f.exists())
				f.create(input, true, new NullProgressMonitor());
			else
				f.setContents(input, IResource.FORCE, new NullProgressMonitor());
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (TransformerConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (TransformerException e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}

	/**
	 * Given a project, this method attempts to discover the type of the storage
	 * and return the AbstractCProjectDescriptionStorage responsible for loading it.
	 *
	 * @return AbstractCProjectDescription or null if not found
	 */
	private AbstractCProjectDescriptionStorage loadProjectStorage(IProject project) {
		if (storageTypeMap == null)
			initExtensionPoints();

		// If no project description found, then use the default
		Version version = DEFAULT_STORAGE_VERSION;
		String storageTypeID = DEFAULT_STORAGE_TYPE;

		InputStream stream = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			stream = getInputStreamForIFile(project, ICProjectDescriptionStorageType.STORAGE_FILE_NAME);
			if (stream != null) {
				Document doc = builder.parse(stream);
				// Get the first element in the project file
				Node rootElement = doc.getFirstChild();
				if (rootElement.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE)
					throw ExceptionFactory
							.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.7")); //$NON-NLS-1$
				else
					version = new Version(rootElement.getNodeValue());

				// Now get the project root element (there should be only one)
				NodeList nodes = doc.getElementsByTagName(ICProjectDescriptionStorageType.STORAGE_ROOT_ELEMENT_NAME);
				if (nodes.getLength() == 0)
					throw ExceptionFactory
							.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.9")); //$NON-NLS-1$
				Node node = nodes.item(0);
				if (node.getNodeType() != Node.ELEMENT_NODE)
					throw ExceptionFactory
							.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.10")); //$NON-NLS-1$
				// If we've got this far, then we're at least dealing with an old style project:
				//  as this didn't use to provide a type, specify one explicitly
				// Choose new style -- separated out storage modules by default as this is backwards compatible...
				storageTypeID = XmlProjectDescriptionStorage2.STORAGE_TYPE_ID;
				if (((Element) node).hasAttribute(ICProjectDescriptionStorageType.STORAGE_TYPE_ATTRIBUTE))
					storageTypeID = ((Element) node)
							.getAttribute(ICProjectDescriptionStorageType.STORAGE_TYPE_ATTRIBUTE);
			}
		} catch (Exception e) {
			// Catch all, if not found, we use the old-style defaults...
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}

		List<CProjectDescriptionStorageTypeProxy> types = storageTypeMap.get(storageTypeID);
		if (types != null) {
			for (CProjectDescriptionStorageTypeProxy type : types) {
				if (type.isCompatible(version)) {
					return type.getProjectDescriptionStorage(type, project, version);
				}
			}
		}

		// No type found!
		CCorePlugin
				.log("CProjectDescriptionStorageType: " + storageTypeID + "  for version: " + version + " not found!"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		return null;
	}

	private InputStream getInputStreamForIFile(IProject project, String name) throws CoreException {
		IFile f = project.getFile(name);
		if (f.exists())
			return f.getContents(true);
		else {
			URI location = f.getLocationURI();
			if (location != null) {
				IFileStore file = EFS.getStore(location);
				IFileInfo info = null;

				if (file != null) {
					info = file.fetchInfo();
					if (info != null && info.exists())
						return file.openInputStream(EFS.NONE, null);
				}
			}
		}
		throw ExceptionFactory.createCoreException("No project des file found..."); //$NON-NLS-1$
	}

	/*
	 * Resource Change Callbacks
	 */

	/**
	 * Callback indicating that a project has moved
	 * @param fromProject
	 * @param toProject
	 */
	void projectMove(IProject fromProject, IProject toProject) {
		AbstractCProjectDescriptionStorage projStorage = fDescriptionStorageMap.get(fromProject);
		if (projStorage != null) {
			fDescriptionStorageMap.put(toProject, projStorage);
			projStorage.projectMove(toProject);
			fDescriptionStorageMap.remove(fromProject);
		}
		// Notify the CConfigBasedDescriptorManager as well
		CConfigBasedDescriptorManager.getInstance().projectMove(fromProject, toProject);
	}

	/**
	 * Callback indicating project has been closed or deleted
	 * @param project
	 */
	void projectClosedRemove(IProject project) {
		AbstractCProjectDescriptionStorage projStorage = fDescriptionStorageMap.get(project);
		if (projStorage != null)
			projStorage.projectCloseRemove();
		fDescriptionStorageMap.remove(project);
		// Remove from ICDescriptorManager as well
		CConfigBasedDescriptorManager.getInstance().projectClosedRemove(project);
	}

	/**
	 * Cleanup state
	 */
	public void shutdown() {
		instance = null;
	}

	/**
	 * Initialize the project description storage types
	 */
	private synchronized void initExtensionPoints() {
		if (storageTypeMap != null)
			return;
		Map<String, List<CProjectDescriptionStorageTypeProxy>> m = new HashMap<>();
		IExtensionPoint extpoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				CPROJ_DESC_STORAGE_EXT_ID);
		for (IExtension extension : extpoint.getExtensions()) {
			for (IConfigurationElement configEl : extension.getConfigurationElements()) {
				if (configEl.getName().equalsIgnoreCase(CPROJ_STORAGE_TYPE)) {
					CProjectDescriptionStorageTypeProxy type = initStorageType(configEl);
					if (type != null) {
						if (!m.containsKey(type.id))
							m.put(type.id, new LinkedList<CProjectDescriptionStorageTypeProxy>());
						m.get(type.id).add(type);
					}
				}
			}
		}
		storageTypeMap = m;
	}

	/**
	 * Initialize a storage type from a configuration element
	 * @param el
	 * @return
	 */
	private static CProjectDescriptionStorageTypeProxy initStorageType(IConfigurationElement el) {
		CProjectDescriptionStorageTypeProxy type = null;
		try {
			type = new CProjectDescriptionStorageTypeProxy(el);
		} catch (CoreException e) {
			CCorePlugin.log("Couldn't instantiate CProjectDescriptionStorageType " + //$NON-NLS-1$
					el.getDeclaringExtension().getNamespaceIdentifier() + " " + e.getMessage()); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			CCorePlugin.log("Failed to load CProjectDescriptionStorageType " + //$NON-NLS-1$
					el.getDeclaringExtension().getNamespaceIdentifier() + " " + e.getMessage()); //$NON-NLS-1$
		}
		return type;
	}

	/**
	 * Helper method to ensure that a resource is writable. This means: <br/>
	 *  - If the resource doesn't exist, it can be created (its parent is made writable) <br/>
	 *  - If the resource exists and its a file, it's made writable <br/>
	 *  - If the resource exists and its a directory, it and its (direct) children
	 * 		are made writable
	 * @param resource
	 * @throws CoreException on failure
	 */
	public static void ensureWritable(IResource resource) throws CoreException {
		if (!resource.exists())
			resource.refreshLocal(IResource.DEPTH_INFINITE, null);
		if (!resource.exists()) {
			// If resource doesn't exist, ensure it can be created.
			ResourceAttributes parentAttr = resource.getParent().getResourceAttributes();
			if (parentAttr.isReadOnly()) {
				parentAttr.setReadOnly(false);
				resource.getParent().setResourceAttributes(parentAttr);
			}
		} else {
			// If resource exists, ensure it and children are writable
			if (resource instanceof IFile) {
				if (resource.getResourceAttributes().isReadOnly()) {
					IStatus result = resource.getWorkspace().validateEdit(new IFile[] { (IFile) resource }, null);
					if (!result.isOK())
						throw new CoreException(result);
				}
			} else if (resource instanceof IContainer) {
				ResourceAttributes resAttr = resource.getResourceAttributes();
				if (resAttr.isReadOnly()) {
					resAttr.setReadOnly(false);
					resource.setResourceAttributes(resAttr);
				}
				IResource[] members = ((IContainer) resource).members();
				List<IFile> files = new ArrayList<>(members.length);
				for (IResource member : members) {
					if (member instanceof IFile && member.getResourceAttributes().isReadOnly()) {
						files.add((IFile) member);
					}
				}
				if (files.size() > 0) {
					IFile[] filesToValidate = files.toArray(new IFile[files.size()]);
					IStatus result = resource.getWorkspace().validateEdit(filesToValidate, null);
					if (!result.isOK())
						throw new CoreException(result);
				}
			}
		}
	}

}
