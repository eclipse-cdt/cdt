/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.ICProjectConverter;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.envvar.ContributedEnvironment;
import org.eclipse.cdt.internal.core.settings.model.AbstractCProjectDescriptionStorage;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescription;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionStorageManager;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionStorageType;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionStorageType.CProjectDescriptionStorageTypeProxy;
import org.eclipse.cdt.internal.core.settings.model.SettingsContext;
import org.eclipse.cdt.internal.core.settings.model.SettingsModelMessages;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

/**
 * This class acts as the (de)serialization of the Xml Project model
 *
 * The read-only project description is referenced through a volatile reference
 * which is updated atomically.
 * The serializationLock is used to prevent concurrent read & write of the project
 * description by Eclipse.  All Scheduling rules _must_ be acquired before attempting
 * to lock serializationLock (as it happens the setCProjectDescriptionOperation uses
 * the Workspace scheduling rule).
 *
 *   FIXME JBB we should use a more advanced overlay tree in the project
 *   description manager to allow safe concurrent access to the tree -- this will
 *   save both space (memory only needed for deltas) and provide easier access
 *   to the deltas.  Having done this we should return a different ICDescriptor for
 *   each thread that requests it.
 * @see AbstractCProjectDescriptionStorage
 */
public class XmlProjectDescriptionStorage extends AbstractCProjectDescriptionStorage {
	public static final int INTERNAL_GET_IGNORE_CLOSE = 1 << 31;

	private static final String OLD_PROJECT_DESCRIPTION = "cdtproject"; //$NON-NLS-1$
	private static final String OLD_CDTPROJECT_FILE_NAME = ".cdtproject"; //$NON-NLS-1$
	private static final String OLD_PROJECT_OWNER_ID = "id"; //$NON-NLS-1$
	private static final String CONVERTED_CFG_NAME = "convertedConfig"; //$NON-NLS-1$
	private static final String CONVERTED_CFG_ID_PREFIX = "converted.config"; //$NON-NLS-1$

	/** The version of this project description storage */
	public static final Version STORAGE_DESCRIPTION_VERSION = new Version("4.0"); //$NON-NLS-1$
	/** The extension point ID of this description storage type */
	public static final String STORAGE_TYPE_ID = CCorePlugin.PLUGIN_ID + ".XmlProjectDescriptionStorage"; //$NON-NLS-1$

	final static String MODULE_ID = "org.eclipse.cdt.core.settings"; //$NON-NLS-1$
	static final String CONFIGURATION = "cconfiguration"; //$NON-NLS-1$

	private static final QualifiedName LOAD_FLAG = new QualifiedName(CCorePlugin.PLUGIN_ID, "descriptionLoadded"); //$NON-NLS-1$

	public XmlProjectDescriptionStorage(CProjectDescriptionStorageTypeProxy type, IProject project, Version version) {
		super(type, project, version);
	}


	/**
	 * The workspace runnable that actually goes about serializing the project description
	 */
	private class DesSerializationRunnable implements IWorkspaceRunnable {
		private final ICProjectDescription fDes;
		private final ICStorageElement fElement;

		/*
		 * See Bug 249951 & Bug 310007
		 * Notification run with the workspace lock (which clients can't acquire explicitly)
		 * The result is deadlock if:
		 *   1) Notification listener does getProjectDescription  (workspaceLock -> serializingLock)
		 *   2) setProjectDescription does IFile write  (serializingLock -> workspaceLock)
		 * This workaround stops the periodic notification job while we're persisting the project description
		 * which prevents notification (1) from occurring while we do (2)
		 */
		private class NotifyJobCanceller extends JobChangeAdapter {
			@Override
			public void aboutToRun(IJobChangeEvent event) {
				final Job job = event.getJob();
				if ("org.eclipse.core.internal.events.NotificationManager$NotifyJob".equals(job.getClass().getName())) { //$NON-NLS-1$
					job.cancel();
				}
			}
		}

		public DesSerializationRunnable(ICProjectDescription des, ICStorageElement el) {
			fDes = des;
			fElement = el;
		}

		public void run(IProgressMonitor monitor) throws CoreException {
			JobChangeAdapter notifyJobCanceller = new NotifyJobCanceller();
			try {
				// See Bug 249951 & Bug 310007
				Job.getJobManager().addJobChangeListener(notifyJobCanceller);
				// Ensure we can check a null-job into the workspace 
				// i.e. if notification is currently in progress wait for it to finish...
				ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
					}
				}, null, IWorkspace.AVOID_UPDATE, null);
				// end Bug 249951 & Bug 310007
				serializingLock.acquire();
				projectModificaitonStamp = serialize(fDes.getProject(), ICProjectDescriptionStorageType.STORAGE_FILE_NAME, fElement);
				((ContributedEnvironment) CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment()).serialize(fDes);
			} finally {
				serializingLock.release();
				Job.getJobManager().removeJobChangeListener(notifyJobCanceller);				
			}
		}

	}

	/** A soft reference to the read-only project description
	 *  Volatile provides a memory barrier in Java 5+ */
	private volatile Reference<ICProjectDescription> fProjectDescription = new SoftReference<ICProjectDescription>(null);
	/** The last modification stamp of the .cproject project description file */
	private volatile long projectModificaitonStamp = IResource.NULL_STAMP;

	/** A lock that is held during project description serialization
	 *  This lock is also head during load to prevent a load overlapping
	 *  with a concurrent reply (as resource locks aren't used for load...)*/
	private final ILock serializingLock = Job.getJobManager().newLock();


	@Override
	public ICSettingsStorage getStorageForElement(ICStorageElement element) throws CoreException {
		return new XmlStorage((InternalXmlStorageElement)element);
	}

	@Override
	public final ICProjectDescription getProjectDescription(int flags, IProgressMonitor monitor) throws CoreException {
		ICProjectDescription des = null;
		boolean write = checkFlags(flags, ICProjectDescriptionManager.GET_WRITABLE);
		// Only 'load' if the caller hasn't explicitly requested currently loaded config
		boolean load = !checkFlags(flags, ICProjectDescriptionManager.GET_IF_LOADDED);
		// Create an empty configuration if the user has requested it
		boolean empty = checkFlags(flags, ICProjectDescriptionManager.GET_EMPTY_PROJECT_DESCRIPTION);
		boolean ignoreClose = checkFlags(flags, INTERNAL_GET_IGNORE_CLOSE);
		boolean create = checkFlags(flags, ICProjectDescriptionManager.GET_CREATE_DESCRIPTION);
		// set the PROJECT_CREATING flag on the project description
		boolean creatingState = checkFlags(flags, ICProjectDescriptionManager.PROJECT_CREATING);

		SettingsContext context = null;

		des = super.getProjectDescription(flags, monitor);

		// If no thread local project description then check for the previous loaded one
		// or load from the .cproject file
		if (des == null) {
			boolean released = false;
			try {
				// If the description is already loaded and has been modified externally, reload it
				load |= checkExternalModification();

				// Acquire the (de)serializing lock
				serializingLock.acquire();

				if (ignoreClose || project.isOpen())
					des = getLoadedDescription();

				if (!empty && des == null && load && project.isOpen()) {
					try {
						des = loadProjectDescription(project);
					} catch (CoreException e) {
						// This isn't an issue as there may not be a project description
						// file yet
					}
					if (des == null) {
						// TODO: check if conversion needed
						try {
							context = new SettingsContext(project);
							des = getConvertedDescription(context);
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
					}

					if (des != null) {
						if (setLoaddedDescriptionOnLoad(project, des)) {
							// Current read-only description loaded, unlock the serializing lock
							//  (as saving conversion below will require acquiring resource scheduling rules...)
							serializingLock.release();
							released = true;

							if (context != null)
								saveConversion(project, context, (CProjectDescription) des, new NullProgressMonitor());
							fireLoadedEvent(des);
							des = getLoadedDescription();
						}
					}
				}
			} finally {
				if (!released)
					serializingLock.release();
			}
		}

		// Only create a new empty configuration if the caller has requested an empty configuration
		// or they're creating a configuration and there is no existing configuration found
		if (empty || (des == null && create)) {
			if (creatingState && des != null)
				creatingState = des.isCdtProjectCreating();
			try {
				InternalXmlStorageElement element = createStorage(project, ICProjectDescriptionStorageType.STORAGE_FILE_NAME, false, true, false);
				return new CProjectDescription(project, new XmlStorage(element), element, false, creatingState);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}

		if (des != null && write) {
			des = createWritableDescription((CProjectDescription) des);
		}
		return des;
	}


	/**
	 * Method to check whether the description has been modified externally.
	 * If so the current read-only descriptor is nullified.
	 * It updates the cached modification stamp
	 * @return boolean indicating whether reload is needed
	 */
	protected synchronized boolean checkExternalModification() {
		// If loaded, and we have cached the modification stamp, reload
		long currentModificationStamp = getModificationStamp(project.getFile(ICProjectDescriptionStorageType.STORAGE_FILE_NAME));
		if (projectModificaitonStamp != currentModificationStamp) {
			setCurrentDescription(null, true);
			projectModificaitonStamp = currentModificationStamp;
			return true;
		}
		return false;
	}

	/**
	 * Gets the modification stamp for the resource.
	 * If the returned value has changed since last call to
	 * {@link #getModificationStamp(IResource)}, then the resource has changed.
	 * @param resource IResource to fetch modification stamp for
	 * @return long modification stamp
	 */
	protected long getModificationStamp(IResource resource) {
		// The modification stamp is based on the ResourceInfo modStamp and file store modification time. Note that
		// because of bug 160728 subsequent generations of resources may have the same modStamp. Until this is fixed
		// the suggested solution is to use modStamp + modTime
		//
		// Both values are cached in resourceInfo, so this is fast.
		return resource.getModificationStamp() + resource.getLocalTimeStamp();		
	}

	/**
	 * Create a writable version of the description
	 *
	 * @param cache The base CProjectDescription on which the writable copy is to be created
	 * @return CProjectDescription of null on failure
	 */
	private CProjectDescription createWritableDescription(CProjectDescription cache) {
		CProjectDescription des = null;
		try {
			InternalXmlStorageElement el = (InternalXmlStorageElement)cache.getRootStorageElement();
			el = copyElement(el, false);

			des = new CProjectDescription(cache, false, new XmlStorage(el), el, cache.isCdtProjectCreating());
			fireCopyCreatedEvent(des, cache);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return des;
	}

	/**
	 * @param project
	 * @param des
	 * @return
	 */
	private boolean setLoaddedDescriptionOnLoad(IProject project, ICProjectDescription des) {
		des.setSessionProperty(LOAD_FLAG, Boolean.TRUE);
		ICProjectDescription oldDes = getLoadedDescription();

		setCurrentDescription(des, true);

		if (oldDes == null)
			return true;

		return oldDes.getSessionProperty(LOAD_FLAG) == null;
	}

	/* Sets the current read-only descriptions -- uses the write lock
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.AbstractCProjectDescriptionStorage#setCurrentDescription(org.eclipse.cdt.core.settings.model.ICProjectDescription, boolean)
	 */
	@Override
	public boolean setCurrentDescription(ICProjectDescription des, boolean overwriteIfExists) {
		if (!overwriteIfExists && fProjectDescription.get() != null)
			return false;

		if (des != null) {
			if (project.exists() && project.isOpen()) {
				fProjectDescription = new SoftReference<ICProjectDescription>(des);
			} else {
				IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, SettingsModelMessages.getString("CProjectDescriptionManager.16"), null); //$NON-NLS-1$
				CCorePlugin.log(new CoreException(status));
			}
		} else {
			fProjectDescription = new SoftReference<ICProjectDescription>(null);
		}
		return true;
	}

	private Object[] loadProjectDescriptionFromOldstyleStorage() throws CoreException {
		ICStorageElement rootEl = readOldCDTProjectFile(project);
		if (rootEl != null) {
			String ownerId = rootEl.getAttribute(OLD_PROJECT_OWNER_ID);
			CProjectDescription des = (CProjectDescription) CProjectDescriptionManager.getInstance().createProjectDescription(project, false);
			String id = CDataUtil.genId(CONVERTED_CFG_ID_PREFIX);
			des.createConvertedConfiguration(id, CONVERTED_CFG_NAME, rootEl);
			return new Object[] { ownerId, des };
		}
		return null;
	}

	/**
	 * Convert and Load a previous version of the Project Description
	 *
	 * @param project
	 * @param context
	 * @return
	 * @throws CoreException
	 */
	private ICProjectDescription getConvertedDescription(SettingsContext context) throws CoreException {
		Object info[] = loadProjectDescriptionFromOldstyleStorage();
		CProjectDescription des = null;
		String ownerId = null;
		try {
			if (info != null) {
				ownerId = (String) info[0];
				des = (CProjectDescription) info[1];
				setThreadLocalProjectDesc(des);
				des.setLoading(true);
			}

			IProjectDescription eDes = context.getEclipseProjectDescription();

			ICProjectConverter converter = CProjectDescriptionManager.getInstance().getConverter(project, ownerId, des);
			if (converter != null) {
				CProjectDescription convertedDes = (CProjectDescription) converter.convertProject(project, eDes, ownerId, des);
				if (convertedDes != null) {
					CProjectDescriptionManager.getInstance().checkHandleActiveCfgChange(convertedDes, null, eDes, new NullProgressMonitor());
					des = convertedDes;
				}
			}

			if (des != null && des.isValid()) {
				// TODO: should be set via the CModel operation?
				InternalXmlStorageElement el = null;
				context.setEclipseProjectDescription(eDes);

				try {
					el = copyElement(des.getRootStorageElement(), false);
				} catch (CoreException e2) {
				}

				des = new CProjectDescription(des, true, new XmlStorage(el), el, des.isCdtProjectCreating());
				setThreadLocalProjectDesc(des);
				des.applyDatas(context);
				des.doneApplying();
			}
		} finally {
			setThreadLocalProjectDesc(null);
			if (des != null)
				des.setLoading(false);
		}
		return des;
	}

	private void saveConversion(final IProject proj, final SettingsContext context, CProjectDescription des, IProgressMonitor monitor) {
		try {
			context.addWorkspaceRunnable(createDesSerializationRunnable());
		} catch (CoreException e1) {
			CCorePlugin.log(e1);
		}
		IWorkspaceRunnable toRun = context.createOperationRunnable();
		if (toRun != null)
			CProjectDescriptionManager.runWspModification(toRun, monitor);
	}

	/**
	 * Return the read-only ICProjectDescription
	 * in a thread-safe manner
	 */
	public ICProjectDescription getLoadedDescription() {
		return fProjectDescription.get();
	}

	/**
	 * The internal method that actually causes the CProjectDescription to be created from an external storage.
	 * @param project
	 * @return the loaded ICProjectDescription
	 * @throws CoreException
	 */
	protected ICProjectDescription loadProjectDescription(IProject project) throws CoreException {
		try {
			// Ensure that there isn't a write to the project description occurring concurrently
			serializingLock.acquire();

			// Check if the description has already been loaded
			if (!checkExternalModification() && getLoadedDescription() != null)
				return getLoadedDescription();

			// Don't log core exceptions caused by .cproject file not exists. Leave that to caller
			InternalXmlStorageElement storage = createStorage(project, ICProjectDescriptionStorageType.STORAGE_FILE_NAME, true, false, false);
			try {
				// Update the modification stamp
				projectModificaitonStamp = getModificationStamp(project.getFile(ICProjectDescriptionStorageType.STORAGE_FILE_NAME));
				CProjectDescription des = new CProjectDescription(project, new XmlStorage(storage), storage, true, false);
				try {
					setThreadLocalProjectDesc(des);
					des.loadDatas();
					des.doneLoading();
				} finally {
					setThreadLocalProjectDesc(null);
				}
				return des;
			} catch (CoreException e) {
				// XmlStorage constructor does sanity checking of the project xml storage element, ensure that errors here are logged
				CCorePlugin.log(e);
				throw e;
			}
		} finally {
			serializingLock.release();
		}
	}

	@Override
	public IWorkspaceRunnable createDesSerializationRunnable() throws CoreException {
		CProjectDescription des = (CProjectDescription)getLoadedDescription();
		if (des == null) // This won't happen because CModelOperation has a handle on the current read-only description
			throw ExceptionFactory.createCoreException("No read-only Project Description found! Project: " + project.getName()); //$NON-NLS-1$
		final ICStorageElement element = des.getRootStorageElement();
		IWorkspaceRunnable r = new DesSerializationRunnable(des, element);
		return r;
	}

	/**
	 * Convert an Xml based ICStorageElement to an ByteArrayOutputStream
	 */
	private ByteArrayOutputStream write(ICStorageElement element) throws CoreException {
		Document doc = ((InternalXmlStorageElement) element).fElement.getOwnerDocument();
		XmlUtil.prettyFormat(doc);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);

			return stream;
		} catch (TransformerConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (TransformerException e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}

	/**
	 * The method that *actually* performs the serialization of the settings...
	 *
	 * @param container - the folder in which file is to be serialized
	 * @param file - the name of the file to serialize the storage element tree into
	 * @param element - read-only ICStorageElement tree to be serialized (no need to lock)
	 * @throws CoreException
	 * @return long modification stamp of the file in the container
	 */
	protected long serialize(IContainer container, String file, ICStorageElement element) throws CoreException {
		try {
			final IFile projectFile = container.getFile(new Path(file));
			final ISchedulingRule rule = MultiRule.combine(new ISchedulingRule[] {
					ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(projectFile),
					ResourcesPlugin.getWorkspace().getRuleFactory().createRule(projectFile),
					ResourcesPlugin.getWorkspace().getRuleFactory().deleteRule(projectFile)
					});

			String utfString;
			ByteArrayOutputStream stream = null;
			try {
				// Get the ProjectDescription as a utf-8 string
				stream = write(element);
				utfString = stream.toString("UTF-8"); //$NON-NLS-1$
			} finally {
				if (stream != null)
					stream.close(); // Cleanup the stream
			}

			try {
				// Lock the projectFile
				Job.getJobManager().beginRule(rule, null);

				// Ensure the file is writable
				CProjectDescriptionStorageManager.ensureWritable(projectFile);
				if (projectFile.exists()) {
					try {
						projectFile.setContents(new ByteArrayInputStream(utfString.getBytes("UTF-8")), IResource.FORCE, new NullProgressMonitor()); //$NON-NLS-1$
					} catch (CoreException e) {
						if (projectFile.getLocation().toFile().isHidden()) {
							String os = System.getProperty("os.name"); //$NON-NLS-1$
							if (os != null && os.startsWith("Win")) { //$NON-NLS-1$
								projectFile.delete(true, null);
								projectFile.create(new ByteArrayInputStream(utfString.getBytes("UTF-8")), IResource.FORCE, new NullProgressMonitor()); //$NON-NLS-1$
								CCorePlugin.log(e.getLocalizedMessage() + "\n** Error occured because of file status <hidden>." + //$NON-NLS-1$
										"\n** This status is disabled now, to allow writing."); //$NON-NLS-1$
							} else
								throw (e);
						} else
							throw (e);
					}
				} else {
					projectFile.create(new ByteArrayInputStream(utfString.getBytes("UTF-8")), IResource.FORCE, new NullProgressMonitor()); //$NON-NLS-1$
				}
				return getModificationStamp(projectFile);
			} finally {
				Job.getJobManager().endRule(rule);
			}
		} catch (IOException e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}

	private ICStorageElement readOldCDTProjectFile(IProject project) throws CoreException {
		ICStorageElement storage = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = null;
			InputStream stream = getSharedProperty(project, OLD_CDTPROJECT_FILE_NAME);
			if (stream != null) {
				doc = builder.parse(stream);
				NodeList nodeList = doc.getElementsByTagName(OLD_PROJECT_DESCRIPTION);

				if (nodeList != null && nodeList.getLength() > 0) {
					Node node = nodeList.item(0);
					storage = new InternalXmlStorageElement((Element) node, false);
				}
			}
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (SAXException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (IOException e) {
			throw ExceptionFactory.createCoreException(e);
		}
		return storage;
	}

	/**
	 * This method returns an ICStorageElement from a given Xml file filename in the container
	 * container
	 *
	 * @param container
	 * @param fileName
	 * @param reCreate
	 * @param createEmptyIfNotFound
	 * @param readOnly
	 * @return InternalXmlStorageElement representing the particular storage
	 * @throws CoreException
	 */
	protected InternalXmlStorageElement createStorage(IContainer container, String fileName, boolean reCreate, boolean createEmptyIfNotFound, boolean readOnly) throws CoreException{
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = null;
			Element element = null;
			InputStream stream = null;
			if(reCreate){
				try{
					stream = getSharedProperty(container, fileName);
					if(stream != null){
						doc = builder.parse(stream);

						// Get the first element in the project file
						Node rootElement = doc.getFirstChild();

						if (rootElement.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
							throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.7")); //$NON-NLS-1$
						} else {
							// Make sure that the version is compatible with the manager
							String fileVersion = rootElement.getNodeValue();
							Version version = new Version(fileVersion);
							if (getVersion().compareTo(version) < 0) {
								throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.8")); //$NON-NLS-1$
							}
						}

						// Now get the project root element (there should be only one)
						NodeList nodes = doc.getElementsByTagName(ICProjectDescriptionStorageType.STORAGE_ROOT_ELEMENT_NAME);
						if (nodes.getLength() == 0)
							throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.9")); //$NON-NLS-1$
						Node node = nodes.item(0);
						if(node.getNodeType() != Node.ELEMENT_NODE)
							throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.10")); //$NON-NLS-1$
						element = (Element)node;
					} else if(!createEmptyIfNotFound){
						throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.11") + fileName); //$NON-NLS-1$
					}
				} catch (FactoryConfigurationError e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e.getLocalizedMessage());
				} catch (SAXException e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e);
				} catch (IOException e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e);
				} finally {
					if(stream != null){
						try {
							stream.close();
						} catch (IOException e) {
						}
					}
				}
			}

			if(element == null) {
				doc = builder.newDocument();
				ProcessingInstruction instruction = doc.createProcessingInstruction(ICProjectDescriptionStorageType.STORAGE_VERSION_NAME, getVersion().toString());
				doc.appendChild(instruction);
				element = doc.createElement(ICProjectDescriptionStorageType.STORAGE_ROOT_ELEMENT_NAME);
				element.setAttribute(ICProjectDescriptionStorageType.STORAGE_TYPE_ATTRIBUTE, getStorageTypeId());
				doc.appendChild(element);
			}

			return new InternalXmlStorageElement(element, null, false, readOnly);
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}

	/**
	 * @return the maximum version supported by this description storage
	 */
	protected Version getVersion() {
		return STORAGE_DESCRIPTION_VERSION;
	}

	/**
	 * @return Return the storage type id for this storage
	 */
	protected String getStorageTypeId() {
		return STORAGE_TYPE_ID;
	}

	/**
	 * Return an input stream for the given file in the provided container
	 * @param container
	 * @param key
	 * @return InputStream
	 * @throws CoreException on failure
	 */
	public InputStream getSharedProperty(IContainer container, String key) throws CoreException {
		InputStream stream = null;
		final IFile rscFile = container.getFile(new Path(key));
		if (rscFile.exists()) {
			try {
				stream = rscFile.getContents(true);
			} catch (CoreException e) {
				// try refreshing
				final Throwable[] t = new Throwable[1];
				Job job = CProjectDescriptionManager.runWspModification(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						try {
							rscFile.refreshLocal(IResource.DEPTH_ZERO, null);
						} catch (Exception e) {
							t[0] = e;
						}
					}
				}, rscFile, new NullProgressMonitor());

				// if refresh was performed "inline" without job scheduled
				if (job == null) {
					// if no exceptions occured
					if (t[0] == null) {
						// try get contents
						stream = rscFile.getContents();
					} else {
						// refresh failed
						if (t[0] instanceof CoreException)
							throw (CoreException) t[0];
						throw e;
					}
				} else {
					throw e;
				}
			}
		} else {
			// FIXME JBB remove?
			// when a project is imported, we get a first delta for the addition
			// of the .project, but the .classpath is not accessible
			// so default to using java.io.File
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96258
			URI location = rscFile.getLocationURI();
			if (location != null) {
				IFileStore file = EFS.getStore(location);
				IFileInfo info = null;

				if (file != null) {
					info = file.fetchInfo();
					if (info != null && info.exists())
						stream = file.openInputStream(EFS.NONE, null);
				}
			}
		}
		return stream;
	}

	@Override
	public void projectCloseRemove() {
		super.projectCloseRemove();
		setCurrentDescription(null, true);
	}

	@Override
	public void projectMove(IProject newProject) {
		super.projectMove(newProject);

		// FIXME JBB From ResourceChangeHandler.
		//       Why do the project description and configurations need to know
		//       their project?  They should ask their ProjectDescriptionStorage
		CProjectDescription desc = (CProjectDescription)fProjectDescription.get();
		if (desc != null) {
			desc.updateProject(newProject);
		}
	}

	/**
	 * Return an Xml element copy based on a passed in InternalXmlStorageElement
	 *
	 * @param el
	 * @return Xml element based on a passed in xml ICStorageElement (InternalXmlStorageElement)
	 * @throws CoreException
	 */
	public Element createXmlElementCopy(InternalXmlStorageElement el) throws CoreException {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element newXmlEl = null;
			synchronized (doc) {
			synchronized (el.fLock) {
			if (el.fElement.getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
				Document baseDoc = el.fElement.getOwnerDocument();
				NodeList list = baseDoc.getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					node = importAddNode(doc, node);
					if (node.getNodeType() == Node.ELEMENT_NODE && newXmlEl == null) {
						newXmlEl = (Element) node;
					}
				}

			} else {
				newXmlEl = (Element) importAddNode(doc, el.fElement);
			}
			return newXmlEl;
			}}
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (FactoryConfigurationError e) {
			throw ExceptionFactory.createCoreException(e);
		}

	}

	@Override
	public InternalXmlStorageElement copyElement(ICStorageElement el, boolean readOnly) throws CoreException {
		InternalXmlStorageElement internalEl = (InternalXmlStorageElement)el;
		Element newXmlEl = createXmlElementCopy(internalEl);
		return new InternalXmlStorageElement(newXmlEl, internalEl.getParent(), internalEl.getAttributeFilters(), internalEl.getChildFilters(), readOnly);
	}

	private Node importAddNode(Document doc, Node node) {
		if (node.getOwnerDocument().equals(doc)) {
			node = node.cloneNode(true);
		} else {
			node = doc.importNode(node, true);
		}

		return doc.appendChild(node);
	}

}
