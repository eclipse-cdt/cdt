/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 * James Blackburn (Broadcom Corp.)
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorManager;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.CExtensionUtil;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.internal.core.settings.model.SynchronizedStorageElement;
import org.eclipse.cdt.internal.core.settings.model.xml.XmlStorage;
import org.eclipse.cdt.internal.core.settings.model.xml.XmlStorageElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.icu.text.MessageFormat;

/**
 * Concrete ICDescriptor for a Project.
 *
 * There is only one of these per project.  Settings are serialized as storage elements
 * as children of the root of the project description. Methods which change or access data
 * on the descriptor use the Eclipse ILock 'fLock' on the given descriptor instance.
 *
 * Structural changes made to extension elements are persisted immediately to
 * the project description.
 *
 * Changes made to child storage elements are serialized to the project description
 * with saveProjectData(...) and the serializingJob.
 *
 * Users should consider using {@link ICDescriptorManager#runDescriptorOperation} for threadsafe
 * access to the project's configuration.  However failing this does provide some basic
 * concurrency on {@link #getProjectStorageElement(String)} by wrapping the returned
 * ICStorageElement in an {@link SynchronizedStorageElement}.  Note that this is best
 * effort, so concurrent structural changes to the tree (such as one thread removing
 * an element from a tree while another is writing to it) may result in inconsistent data
 * stored.
 *
 */
final public class CConfigBasedDescriptor implements ICDescriptor {
	private static final String CEXTENSION_NAME = "cextension"; //$NON-NLS-1$
	/** The current default setting configuration description
	 *  Equivalent to {@link ICProjectDescription#getDefaultSettingConfiguration()}*/
	private ICConfigurationDescription fCfgDes;
	private COwner fOwner;

	/** Map: storageModule ID -> ICStorageElement <br/>
	 *  CDescriptor's map of so far uncommited storage elements. */
	private final Map<String, SynchronizedStorageElement> fStorageDataElMap = new HashMap<String, SynchronizedStorageElement>();
	private volatile boolean fIsDirty;
	/** Current CDescriptor Event which tracks changes between operationStart & operationStop */
	private CDescriptorEvent fOpEvent;
	/** Flag indicating whether an operation has started */
	private volatile boolean fIsOpStarted;

	/** This descriptor's lock */
	final ILock fLock = Job.getJobManager().newLock();
	/**
	 * The Job the actually does the data applying (by getting and setting the current project description)
	 * saveProjectData never does the saving itself, rather it schedules this job to run.
	 * During the setCProjectDescriptionOperation the changes in this ICDescriptor are synchronized into the
	 * project description being persisted.
	 */
	class SerializingJob extends Job {
		public SerializingJob(String name) {
			super (name);
			setSystem(true);
			// This rule must contain that in SetCProjectDescriptionOperation
			// (Resource scheduling rules are always obtained before data structure locks to prevent deadlocks.)
			setRule(ResourcesPlugin.getWorkspace().getRoot());
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				fLock.acquire();
				// No point scheduling the run if the project is closed...
				if (!getProject().isAccessible())
					return Status.CANCEL_STATUS;
				serialize();
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} finally {
				fLock.release();
			}
			return Status.OK_STATUS;
		}

		public void serialize() throws CoreException {
			if (!getProject().isAccessible())
				throw ExceptionFactory.createCoreException(MessageFormat.format(CCorePlugin.getResourceString("ProjectDescription.ProjectNotAccessible"), new Object[] {getProject().getName()})); //$NON-NLS-1$
			if(fIsDirty) {
				ICProjectDescription des = fCfgDes.getProjectDescription();
				if(des.isCdtProjectCreating())
					des.setCdtProjectCreated();
				CProjectDescriptionManager.getInstance().setProjectDescription(getProject(), des);
				fIsDirty = false;
			}
		}
	}
	SerializingJob serializingJob = new SerializingJob("CConfigBasedDescriptor Serializing Job"); //$NON-NLS-1$ (system)

	/**
	 * Concrete implementation of ICExtensionReference based on ICConfigExtensionReference elements.
 	 * In the old world ICExtensions had no notion of which configuration they belong to.
 	 * As a result all state that would have be persisted at the ICExtension level is saved to all
 	 * the configurations in the project
	 *
	 * This is a lightweight proxy onto ICConfigExtensionReference and doesn't hold any state
	 * itself (though alters the isDirty state and descriptor event of the containing Descriptor).
	 */
	final class CConfigBaseDescriptorExtensionReference implements ICExtensionReference {
		/** The ICConfigExtensionReference this is based on -- the identifying feature of this ICExtensionReference */
		private final ICConfigExtensionReference fCfgExtRef;
		CConfigBaseDescriptorExtensionReference(ICConfigExtensionReference cfgRef){
			fCfgExtRef = cfgRef;
		}

		@Override
		public ICExtension createExtension() throws CoreException {
			AbstractCExtension cExtension = null;
			IConfigurationElement el = CExtensionUtil.getFirstConfigurationElement(fCfgExtRef, CEXTENSION_NAME, false);
			cExtension = (AbstractCExtension)el.createExecutableExtension("run"); //$NON-NLS-1$
			cExtension.setExtensionReference(fCfgExtRef);
			cExtension.setProject(getProject());
			return cExtension;
		}

		@Override
		public ICDescriptor getCDescriptor() {
			return CConfigBasedDescriptor.this;
		}

		@Override
		public String getExtension() {
			return fCfgExtRef.getExtensionPoint();
		}

		@Override
		public String getExtensionData(String key) {
			return fCfgExtRef.getExtensionData(key);
		}

		@Override
		public IConfigurationElement[] getExtensionElements()
				throws CoreException {
			IConfigurationElement el = CExtensionUtil.getFirstConfigurationElement(fCfgExtRef, CEXTENSION_NAME, false);
			if(el != null)
				return el.getChildren();
			return new IConfigurationElement[0];
		}

		@Override
		public String getID() {
			return fCfgExtRef.getID();
		}

		@Override
		public void setExtensionData(String key, String value)
				throws CoreException {
			if(!CDataUtil.objectsEqual(fCfgExtRef.getExtensionData(key), value)){
				fIsDirty = true;
				fCfgExtRef.setExtensionData(key, value);
				checkApply();
				if(isOperationStarted())
					setOpEvent(new CDescriptorEvent(CConfigBasedDescriptor.this, CDescriptorEvent.CDTPROJECT_CHANGED, 0));
			}
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof CConfigBaseDescriptorExtensionReference)
				return fCfgExtRef.equals(((CConfigBaseDescriptorExtensionReference)obj).fCfgExtRef);
			return fCfgExtRef.equals(obj);
		}
		@Override
		public int hashCode() {
			return fCfgExtRef.hashCode();
		}
	}

	public CConfigBasedDescriptor(ICConfigurationDescription des) throws CoreException{
		this(des, true);
	}

	public CConfigBasedDescriptor(ICConfigurationDescription des, boolean write) throws CoreException{
		updateConfiguration(des, write);
	}

	/**
	 * Persist the current project description (to persist changes to the ICExtensions)
	 * @param force
	 * @throws CoreException
	 */
	void apply(boolean force) throws CoreException {
		fIsDirty |= force;
		if (!fIsDirty)
			return;

		// If we're already serializing the project description, schedule a job
		// to perform the serialization...
		if (CProjectDescriptionManager.getInstance().isCurrentThreadSetProjectDescription()) {
			serializingJob.schedule();
			return;
		}

		// Deadlock warning: path entry, for example, can do getStorageElement
		// in resource delta (while holding the workspace lock). As CModelOperation
		// runs the job as a workspace runnable, this leads to potential deadlock.
		//
		// So before applying, we ensure that we hold the project resource rule
		// before getting the 'lock' on the datastructures

//		final IProject project = getProject();
		// Release the lock
		final int lockDepth = fLock.getDepth();
		for (int i = 0; i < lockDepth ; ++i)
			fLock.release();

		try {
			// This rule must contain that in SetCProjectDescriptionOperation
			Job.getJobManager().beginRule(ResourcesPlugin.getWorkspace().getRoot(), new NullProgressMonitor());
			try {
				fLock.acquire();
				serializingJob.serialize();
			} finally {
				if (lockDepth == 0) // Only release the lock if it wasn't previously held on entrance to this method
					fLock.release();
				else // Reacquire the lock to the appropriate depth
					for (int i = 0; i < lockDepth - 1; i++)
						fLock.acquire();
			}
		} finally {
			Job.getJobManager().endRule(ResourcesPlugin.getWorkspace().getRoot());
		}
	}

	private void checkApply() throws CoreException {
		apply(false);
	}

	/**
	 * Set the dirty flag
	 * @param dirty
	 */
	void setDirty(boolean dirty){
		fIsDirty = dirty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptor#create(java.lang.String, java.lang.String)
	 */
	@Override
	public ICExtensionReference create(String extensionPoint, String id) throws CoreException {
		try {
			fLock.acquire();
			ICConfigExtensionReference ref = fCfgDes.create(extensionPoint, id);

			//write is done for all configurations to avoid "data loss" on configuration change
			ICProjectDescription des = fCfgDes.getProjectDescription();
			ICConfigurationDescription cfgs[] = des.getConfigurations();
			for (ICConfigurationDescription cfg : cfgs) {
				if(cfg != fCfgDes){
					try {
						cfg.create(extensionPoint, id);
					} catch (CoreException e){
						CCorePlugin.log(e);
					}
				}
			}

			ICExtensionReference r = new CConfigBaseDescriptorExtensionReference(ref);
			fIsDirty = true;
			checkApply();
			if(isOperationStarted())
				setOpEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
			return r;
		} finally {
			fLock.release();
		}
	}

	/**
	 * Equivalent to {@code updateConfiguration(des, true)}
	 * @param des the new ICConfigurationDescription
	 * @throws CoreException
	 */
	public void updateConfiguration(ICConfigurationDescription des) throws CoreException{
		updateConfiguration(des, true);
	}

	/**
	 * Update the currently default (settings) configuration
	 * @param des
	 * @param write
	 * @throws CoreException
	 */
	public void updateConfiguration(ICConfigurationDescription des, boolean write) throws CoreException{
		try {
			fLock.acquire();
			if(write && des instanceof CConfigurationDescriptionCache)
				throw new IllegalArgumentException();

			fCfgDes = des;
			CConfigurationSpecSettings settings = ((IInternalCCfgInfo)fCfgDes).getSpecSettings();
			fOwner = settings.getCOwner();
		} finally {
			fLock.release();
		}
	}

	/**
	 * Attempt to return an ICExtensionReference array based on the ICConfigExtensionReferences
	 * contained in this project description (which match the extensionPointId).
	 *
	 * Fetches all the ICConfigExtensionReferences from the project's configurations.
	 *
	 * Previously this cached the current set of ICExtensionReferences,
	 * but this cache was never used (it was always overwritten by this method).
	 *
	 * FIXME re-add caching (the current behaviour mirrors the previous behaviour -- just tidier)
	 *  @return an array of ICExtenionReference
	 */
	@Override
	public ICExtensionReference[] get(String extensionPoint) {
		try {
			fLock.acquire();
			LinkedHashSet<ICExtensionReference> extRefs = new LinkedHashSet<ICExtensionReference>();

			// Add the ICConfigExtensionReferences for the current configuration description
			for (ICConfigExtensionReference cfgRes : fCfgDes.get(extensionPoint))
				extRefs.add(new CConfigBaseDescriptorExtensionReference(cfgRes));

			for (ICConfigurationDescription cfg : fCfgDes.getProjectDescription().getConfigurations())
				if (!cfg.equals(fCfgDes))
					for (ICConfigExtensionReference cfgRes : fCfgDes.get(extensionPoint))
						extRefs.add(new CConfigBaseDescriptorExtensionReference(cfgRes));

			return extRefs.toArray(new ICExtensionReference[extRefs.size()]);
		} finally {
			fLock.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptor#get(java.lang.String, boolean)
	 */
	@Override
	public ICExtensionReference[] get(String extensionPoint, boolean update) throws CoreException {
		try {
			fLock.acquire();
			ICExtensionReference[] refs = get(extensionPoint);
			if(refs.length == 0 && update){
				fOwner.update(getProject(), this, extensionPoint);
				checkApply();
				refs = get(extensionPoint);
			}
			return refs;
		} finally {
			fLock.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptor#getPlatform()
	 */
	@Override
	public String getPlatform() {
		try {
			fLock.acquire();
			return fOwner.getPlatform();
		} finally {
			fLock.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptor#getProject()
	 */
	@Override
	public IProject getProject() {
		try {
			fLock.acquire();
			return fCfgDes.getProjectDescription().getProject();
		} finally {
			fLock.release();
		}
	}

	/**
	 * Note that in the current implementation of the xml based project description
	 * it is not safe to work on the same storage element in more than one thread.
	 *
	 * It is likely that doing so will return a concurrent modification exception on the
	 * returned ICStorageElement. We must allow this as this is how the existing implementation
	 * behaves.
	 */
	@Override
	public ICStorageElement getProjectStorageElement(String id) throws CoreException {
		try {
			fLock.acquire();
			// Check if the storage element already exists in our local map
			SynchronizedStorageElement storageEl = fStorageDataElMap.get(id);
			if(storageEl == null){
				// Check in the Proejct Description
				ICStorageElement el = fCfgDes.getProjectDescription().getStorage(id, false);

				// Fall-back to checking in the configuration (which is how it used ot be)
				if (el == null)
					el = fCfgDes.getStorage(id, true);
				try {
					el = el.createCopy();
				} catch (UnsupportedOperationException e) {
					throw ExceptionFactory.createCoreException(e);
				}
				storageEl = SynchronizedStorageElement.synchronizedElement(el);
				fStorageDataElMap.put(id, storageEl);
			}
			return storageEl;
		} finally {
			fLock.release();
		}
	}

	/**
	 * Backwards compatibility method which provides an XML Element.
	 * Currently relies on the fact that the only implementation if ICStorageElement
	 * in the core is XmlStorageElement.
	 */
	@Override
	public Element getProjectData(String id) throws CoreException {
		try {
			fLock.acquire();
			// Check if the storage element already exists in our local map
			SynchronizedStorageElement storageEl = fStorageDataElMap.get(id);
			ICStorageElement el;
			if(storageEl == null) {
				el = fCfgDes.getProjectDescription().getStorage(id, false);
				if (el == null)
					el = fCfgDes.getStorage(id, true);
				try {
					el = el.createCopy();
				} catch (UnsupportedOperationException e) {
					throw ExceptionFactory.createCoreException(e);
				}

				if (!(el instanceof XmlStorageElement))
					throw ExceptionFactory.createCoreException(
							"Internal Error: getProjectData(...) currently only supports XmlStorageElement types.", new Exception()); //$NON-NLS-1$

				// Get the underlying Xml Element
				final Element xmlEl = ((XmlStorageElement)el).fElement;
				// This proxy synchronizes the storage element's root XML Element
				el = new XmlStorageElement((Element)Proxy.newProxyInstance(Element.class.getClassLoader(), new Class[]{Element.class}, new InvocationHandler(){
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						Method realMethod = xmlEl.getClass().getMethod(method.getName(), method.getParameterTypes());
						// Now just execute the method
						synchronized (xmlEl) {
							// If requesting the parent node, then we need another proxy
							// so that parent.removeChildNode(...) 'does the right thing'
							if (method.getName().equals("getParentNode")) { //$NON-NLS-1$
								final Node parent = (Node)realMethod.invoke(xmlEl, args);
								Node parentProxy = (Node)Proxy.newProxyInstance(Node.class.getClassLoader(), new Class[]{Node.class}, new InvocationHandler(){
									@Override
									public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
										Method realMethod = parent.getClass().getMethod(method.getName(), method.getParameterTypes());
										synchronized (xmlEl) {
											// Handle the remove child case
											if (method.getName().equals("removeChild")) { //$NON-NLS-1$
												if (args[0] instanceof Element && ((Element)args[0]).getAttribute(
														XmlStorage.MODULE_ID_ATTRIBUTE).length() > 0) {
													ICStorageElement removed = removeProjectStorageElement(((Element)args[0]).getAttribute(
															XmlStorage.MODULE_ID_ATTRIBUTE));
													if (removed != null)
														return ((XmlStorageElement)((SynchronizedStorageElement)removed).getOriginalElement()).fElement;
													return null;
												}
											}
											// else return the realMethod
											return realMethod.invoke(parent, args);
										}
									}
								});
								return parentProxy;
							}
							// Otherwise just execute the method
							return realMethod.invoke(xmlEl, args);
						}
					}
				}));

				storageEl = SynchronizedStorageElement.synchronizedElement(el, xmlEl);
				fStorageDataElMap.put(id, storageEl);
			} else {
				el = storageEl.getOriginalElement();
				if (!(el instanceof XmlStorageElement))
					throw ExceptionFactory.createCoreException(
							"Internal Error: getProjectData(...) currently only supports XmlStorageElement types.", new Exception()); //$NON-NLS-1$
			}

			return ((XmlStorageElement)el).fElement;
		} finally {
			fLock.release();
		}
	}

	@Override
	public ICStorageElement removeProjectStorageElement(String id) throws CoreException {
		try {
			fLock.acquire();
			return fStorageDataElMap.put(id, null);
		} finally {
			fLock.release();
		}
	}

	@Override
	public ICOwnerInfo getProjectOwner() {
		try {
			fLock.acquire();
			return fOwner;
		} finally {
			fLock.release();
		}
	}

	@Override
	public void remove(ICExtensionReference extension) throws CoreException {
		try {
			fLock.acquire();
			ICConfigExtensionReference ref = ((CConfigBaseDescriptorExtensionReference)extension).fCfgExtRef;
			fCfgDes.remove(ref);

			// write is done for all configurations to avoid "data loss" on configuration change
			for (ICConfigurationDescription cfg : fCfgDes.getProjectDescription().getConfigurations()) {
				if(cfg != fCfgDes){
					try {
						ICConfigExtensionReference rs[] = cfg.get(ref.getExtensionPoint());
						for (ICConfigExtensionReference element : rs) {
							if(ref.getID().equals(element.getID())){
								cfg.remove(element);
								break;
							}
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
			fIsDirty = true;
			checkApply();
			if(isOperationStarted())
				setOpEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
		} finally {
			fLock.release();
		}
	}

	@Override
	public void remove(String extensionPoint) throws CoreException {
		try {
			fLock.acquire();
			fCfgDes.remove(extensionPoint);
			//write is done for all configurations to avoid "data loss" on configuration change
			for (ICConfigurationDescription cfg : fCfgDes.getProjectDescription().getConfigurations()) {
				if(cfg != fCfgDes){
					try {
						cfg.remove(extensionPoint);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
			fIsDirty = true;
			checkApply();
			if(isOperationStarted())
				setOpEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
		} finally {
			fLock.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptor#saveProjectData()
	 */
	@Override
	public void saveProjectData() throws CoreException {
		try {
			fLock.acquire();
			// Reconcile changes into the current project description
			if(reconcile(this, fCfgDes.getProjectDescription())) {
				// Dirty => Apply
				fIsDirty = true;
				apply(true);
				if(isOperationStarted())
					setOpEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, 0));
			}
		} finally {
			fLock.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptor#getConfigurationDescription()
	 */
	@Override
	public ICConfigurationDescription getConfigurationDescription() {
		try {
			fLock.acquire();
			return fCfgDes;
		} finally {
			fLock.release();
		}
	}

	/*
	 * Event handling routines
	 */

	void setOpEvent(CDescriptorEvent event) {
		try {
			fLock.acquire();
			if(!isOperationStarted())
				return;

			if (event.getType() == CDescriptorEvent.CDTPROJECT_ADDED) {
				fOpEvent = event;
			} else if (event.getType() == CDescriptorEvent.CDTPROJECT_REMOVED) {
				fOpEvent = event;
			} else {
				if (fOpEvent == null) {
					fOpEvent = event;
				} else if ( (fOpEvent.getFlags() & event.getFlags()) != event.getFlags()) {
					fOpEvent = new CDescriptorEvent(event.getDescriptor(), event.getType(),
							fOpEvent.getFlags() | event.getFlags());
				}
			}
		} finally {
			fLock.release();
		}
	}

	boolean isOperationStarted(){
		return fIsOpStarted;
	}

	void operationStart(){
		fIsOpStarted = true;
	}

	/**
	 * Mark the operation as over -- return the CDescriptorEvent
	 * @return
	 */
	CDescriptorEvent operationStop(){
		try {
			fLock.acquire();
			fIsOpStarted = false;
			CDescriptorEvent e = fOpEvent;
			fOpEvent = null;
			return e;
		} finally {
			fLock.release();
		}
	}

	/*
	 *
	 * The reconcile methods below are for copying storage element changes from the current
	 * CConfigBasedDescriptor to the passed in writable project description
	 *
	 */
	/**
	 * Copies the changes made to the CConfigBasedDescriptor to the ICProjectDescription
	 *
	 * The changes are reconciled into all the project's configurations!
	 * @param descriptor
	 * @param des
	 * @return boolean indicating whether changes were made
	 */
	public static boolean reconcile(CConfigBasedDescriptor descriptor, ICProjectDescription des) throws CoreException {
		try {
			descriptor.fLock.acquire();

			Map<String, SynchronizedStorageElement> map = descriptor.fStorageDataElMap;
			boolean reconciled = false;
			if(!map.isEmpty()){
				for (Map.Entry<String, SynchronizedStorageElement> entry : map.entrySet()) {
					String id = entry.getKey();
					SynchronizedStorageElement synchStor = entry.getValue();

					if (synchStor != null ) {
						// Lock the synchronized storage element to prevent further changes
						synchronized (synchStor.lock()) {
							if(reconcile(id, synchStor.getOriginalElement(), des))
								reconciled = true;
						}
					} else {
						if (reconcile(id, null, des))
							reconciled = true;
					}
				}
			}
			return reconciled;
		} finally {
			descriptor.fLock.release();
		}
	}

	private static boolean reconcile(String id, ICStorageElement newStorEl, ICProjectDescription des) throws CoreException {
		ICStorageElement storEl = des.getStorage(id, false);

		boolean modified = false;

		if(storEl != null){
			if(newStorEl == null){
				des.removeStorage(id);
				modified = true;
			} else {
				if(!newStorEl.equals(storEl)){
					des.importStorage(id, newStorEl);
					modified = true;
				}
			}
		} else {
			if(newStorEl != null){
				des.importStorage(id, newStorEl);
				modified = true;
			}
		}

		// Now storing the descriptor info directly in the Project Description.
		// Ensure that the setting is no longer stored in all the configurations
		for (ICConfigurationDescription cfgDes : des.getConfigurations()) {
			ICStorageElement el = cfgDes.getStorage(id, false);
			if (el != null)
				cfgDes.removeStorage(id);
		}

		return modified;
	}

}
