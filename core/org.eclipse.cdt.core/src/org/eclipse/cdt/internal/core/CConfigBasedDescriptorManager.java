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
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorListener;
import org.eclipse.cdt.core.ICDescriptorManager;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescription;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.internal.core.settings.model.PathEntryConfigurationDataProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

/**
 * CConfigBasedDescriptorManager
 *
 * ICDescriptor settings are set directly within the project description.
 *
 * The ICDescriptorManager can be used to fetch the current ICDescriptor
 * for the project get and set settings in this module in a safe manner.
 *
 * This thread delegates operations to the particular CConfigBasedDescriptor
 * being operated upon. Locking is performed per descriptor.  This manager
 * provides additional concurrency above any beyond that provided by
 * {@link CoreModel#getProjectDescription(IProject)} as each project only
 * has one ICDescriptor live at any time. This prevents concurrent modifications
 * from overwriting changes made in other threads.
 *
 * Usage:
 * 	  Users should consider making changes to project ICDescriptors using an {@link ICDescriptorOperation}
 * with the {@link #runDescriptorOperation} method.
 *    The ICDescriptor's returned for {@link #getDescriptor} are shared between multiple threads,
 * but they are synchronized.  This is safe as long as structural changes aren't made to the same
 * project storage element from multiple threads.
 *
 * @see ICDescriptor for more
 */
final public class CConfigBasedDescriptorManager implements ICDescriptorManager {
	private volatile static CConfigBasedDescriptorManager fInstance;
	public static final String NULL_OWNER_ID = ""; //$NON-NLS-1$
	private static volatile Map<String, COwnerConfiguration> fOwnerConfigMap;
	private volatile static ICProjectDescriptionListener fDescriptionListener;

	private Collection<ICDescriptorListener> fListeners = new CopyOnWriteArraySet<ICDescriptorListener>();

	/** Map: IProjet -> CConfigBasedDescriptor weak reference <br />
	 *  Multiple threads operating concurrently will get the same shared
	 *  ICDescriptor, however we don't keep a reference to this for longer
	 *  than is necessary.*/
	final ConcurrentHashMap<IProject, Reference<CConfigBasedDescriptor>> fProjectDescriptorMap = new ConcurrentHashMap<IProject, Reference<CConfigBasedDescriptor>>();

	private CConfigBasedDescriptorManager(){}

	public static CConfigBasedDescriptorManager getInstance(){
		if(fInstance == null){
			synchronized (CConfigBasedDescriptor.class) {
				if(fInstance == null){
					fInstance = new CConfigBasedDescriptorManager();
				}
			}
		}
		return fInstance;
	}

	private static final COwnerConfiguration NULLCOwner = new COwnerConfiguration(NULL_OWNER_ID,
			CCorePlugin.getResourceString("CDescriptorManager.internal_owner")); //$NON-NLS-1$


	/**
	 * Callback indicating that a project has moved
	 * @param fromProject
	 * @param toProject
	 */
	public void projectMove(IProject fromProject, IProject toProject) {
		Reference<CConfigBasedDescriptor> ref = fProjectDescriptorMap.get(fromProject);
		if (ref != null)
			fProjectDescriptorMap.putIfAbsent(toProject, ref);
		fProjectDescriptorMap.remove(fromProject);
	}

	/**
	 * Callback to remove references the ICDescriptor when the project
	 * is closed or removed
	 * @param project
	 */
	public void projectClosedRemove(IProject project) {
		fProjectDescriptorMap.remove(project);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptorManager#configure(org.eclipse.core.resources.IProject, java.lang.String)
	 */
	@Override
	public void configure(IProject project, String id) throws CoreException {
		if (id.equals(NULLCOwner.getOwnerID())) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
					CCorePlugin.getResourceString("CDescriptorManager.exception.invalid_ownerID"), //$NON-NLS-1$
					(Throwable)null);
			throw new CoreException(status);
		}

		CConfigBasedDescriptor dr = null;

		// Only allow one IProject configure
		synchronized (this) {
			try {
				dr = findDescriptor(project, false);
				if (dr != null) {
					dr.fLock.acquire();
					if (dr.getProjectOwner().getID().equals(NULLCOwner.getOwnerID())) {
						// non owned descriptors are simply configure to the new owner no questions ask!
						dr = updateDescriptor(project, dr, id);
					} else if (!dr.getProjectOwner().getID().equals(id)) {
						IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_EXISTS,
								CCorePlugin.getResourceString("CDescriptorManager.exception.alreadyConfigured"), //$NON-NLS-1$
								(Throwable)null);
						throw new CoreException(status);
					} else {
						return; // already configured with same owner.
					}
				} else {
					dr = findDescriptor(project, true);
					dr.fLock.acquire();
					dr = updateDescriptor(project, dr, id);
				}

				dr.apply(true);
			if(dr.isOperationStarted())
				dr.setOpEvent(new CDescriptorEvent(dr, CDescriptorEvent.CDTPROJECT_ADDED, 0));
			} finally {
				if (dr != null)
					dr.fLock.release();
			}
		}
	}

	/**
	 * Update the descriptor to the particular ownerId
	 *
	 * FIXME JBB don't twiddle CConfigBasedDescription state here.
	 * Creates the project description if non found. Locks the CConfigBasedDescriptor while this takes place
	 * @param project
	 * @param dr
	 * @param ownerId
	 * @return
	 * @throws CoreException
	 */
	private CConfigBasedDescriptor updateDescriptor(IProject project, CConfigBasedDescriptor dr, String ownerId) throws CoreException {
		try {
			dr.fLock.acquire();
			ICConfigurationDescription cfgDes = dr.getConfigurationDescription();
			CConfigurationSpecSettings settings = ((IInternalCCfgInfo)cfgDes).getSpecSettings();
			settings.setCOwner(ownerId);
			COwner owner = settings.getCOwner();
			dr = findDescriptor(project, true);
			owner.configure(project, dr);
			return dr;
		} finally {
			dr.fLock.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptorManager#convert(org.eclipse.core.resources.IProject, java.lang.String)
	 */
	@Override
	public void convert(IProject project, String id) throws CoreException {
		CConfigBasedDescriptor dr = findDescriptor(project, false);
		if(dr == null)
			throw ExceptionFactory.createCoreException(CCorePlugin.getResourceString("CConfigBasedDescriptorManager.0")); //$NON-NLS-1$
		try {
			dr.fLock.acquire();
			dr = updateDescriptor(project, dr, id);
			dr.apply(true);
			if(dr.isOperationStarted())
				dr.setOpEvent(new CDescriptorEvent(dr, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.OWNER_CHANGED));
		} finally {
			dr.fLock.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptorManager#getDescriptor(org.eclipse.core.resources.IProject)
	 */
	@Override
	public ICDescriptor getDescriptor(IProject project) throws CoreException {
		return getDescriptor(project, true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptorManager#getDescriptor(org.eclipse.core.resources.IProject, boolean)
	 */
	@Override
	public ICDescriptor getDescriptor(IProject project, boolean create) throws CoreException {
		return findDescriptor(project, create);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptorManager#addDescriptorListener(org.eclipse.cdt.core.ICDescriptorListener)
	 */
	@Override
	public void addDescriptorListener(ICDescriptorListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptorManager#removeDescriptorListener(org.eclipse.cdt.core.ICDescriptorListener)
	 */
	@Override
	public void removeDescriptorListener(ICDescriptorListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * Run the descriptor operation. Lock the descriptor while this takes place...
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptorManager#runDescriptorOperation(org.eclipse.core.resources.IProject, org.eclipse.cdt.core.ICDescriptorOperation, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void runDescriptorOperation(IProject project, ICDescriptorOperation op, IProgressMonitor monitor)
			throws CoreException {
		CConfigBasedDescriptor dr = findDescriptor(project, true);
		if (dr == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, "Failed to create descriptor", null)); //$NON-NLS-1$

		try {
			dr.fLock.acquire();
			CDescriptorEvent event = null;
			try {
				dr.operationStart();
			op.execute(dr, monitor);
			} finally {
				event = dr.operationStop();
			}
			dr.apply(false);
			if(event != null)
				CConfigBasedDescriptorManager.getInstance().notifyListeners(event);
		} finally {
			dr.fLock.release();
		}
	}

	/*
	 * Runs a descriptor operation directly on an ICProjectDescription.
	 *
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICDescriptorManager#runDescriptorOperation(org.eclipse.core.resources.IProject, org.eclipse.cdt.core.settings.model.ICProjectDescription, org.eclipse.cdt.core.ICDescriptorOperation, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void runDescriptorOperation(IProject project, ICProjectDescription des, ICDescriptorOperation op, IProgressMonitor monitor)
				throws CoreException {
		// Ensure that only one of these is running on the project at any one time...
		if(des.isReadOnly())
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, CCorePlugin.getResourceString("CConfigBasedDescriptorManager.2"), null)); //$NON-NLS-1$
		//create a new descriptor
		CConfigBasedDescriptor dr = loadDescriptor((CProjectDescription)des);
		if (dr == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, CCorePlugin.getResourceString("CConfigBasedDescriptorManager.3"), null)); //$NON-NLS-1$
		op.execute(dr, monitor);
		// reconcile the changes into the passed in ICProjectDescription
		CConfigBasedDescriptor.reconcile(dr, des);
	}

	/**
	 * Fetch the ICDescriptor for the project.
	 *
	 * If there is no project description and create is specified, then a project
	 * description is created and a descriptor is returned.
	 *
	 * Otherwise a share ICDescriptor is returned for the given project
	 *
	 * @param project
	 * @param create create project description if existing description not found
	 * @throws CoreException
	 */
	private CConfigBasedDescriptor findDescriptor(IProject project, boolean create) throws CoreException {
		if (!project.isAccessible() && !create)
			return null;
		CConfigBasedDescriptor dr = null;
		Reference<CConfigBasedDescriptor> ref = fProjectDescriptorMap.get(project);
		if (ref != null)
			dr = ref.get();
		if (dr != null)
			return dr;

		// Only create one descriptor at a time...
		try {
			// Use workspace root lock rule here as:
			// CoreException in getProjectDescription can lead to a refresh holding a resource lock.
			// Meanwhile the lock might be held by a resource notification thread.

			// FIXME JBB we really want to hold a resource lock here...
			//       However doing so changes the way ProjectDescriptions are created and makes CDescriptor tests fail
//			Job.getJobManager().beginRule(ResourcesPlugin.getWorkspace().getRoot(), new NullProgressMonitor());

//			if (fProjectDescriptorMap.get(project) != null && (dr = fProjectDescriptorMap.get(project).get()) != null)
//				return dr;
			// None found, create a new one based off of read-only project description

			CProjectDescription des = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			if(des == null && create)
				des = createProjDescriptionForDescriptor(project);
			if(des != null)
				dr = loadDescriptor(des);

			// Use the ConcurrentHashMap to ensure that only one descriptor is live at a time (for a given project...)
			ref = fProjectDescriptorMap.putIfAbsent(project, new SoftReference<CConfigBasedDescriptor>(dr));
			if (ref != null) {
				// Someone was here before us...
				CConfigBasedDescriptor dr1 = ref.get();
				if (dr1 != null)
					return dr1;
				synchronized (this) {
					ref = fProjectDescriptorMap.putIfAbsent(project, new SoftReference<CConfigBasedDescriptor>(dr));
					if (ref != null) {
						// Someone was here before us...
						dr1 = ref.get();
						if (dr1 != null)
							return dr1;
					}
					fProjectDescriptorMap.put(project, new SoftReference<CConfigBasedDescriptor>(dr));
				}
			}
		} finally {
//			Job.getJobManager().endRule(ResourcesPlugin.getWorkspace().getRoot());
		}
		return dr;
	}

	private static CProjectDescription createProjDescriptionForDescriptor(final IProject project) throws CoreException{
		final CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		final CProjectDescription des = (CProjectDescription)mngr.createProjectDescription(project, false, true);

		CConfigurationData data = mngr.createDefaultConfigData(project, PathEntryConfigurationDataProvider.getDataFactory());
		des.createConfiguration(CCorePlugin.DEFAULT_PROVIDER_ID, data);
		return des;
	}

	/**
	 * Creates a new CConfigBasedDescriptor from the passed CProjectDescription
	 *
	 * static method does not alter any instance state
	 *
	 * @param des
	 * @return CConfigBasedDescriptor or null
	 * @throws CoreException
	 */
	private static CConfigBasedDescriptor loadDescriptor(CProjectDescription des) throws CoreException {
		if (des == null)
			throw ExceptionFactory.createCoreException("CProjectDescription des is null"); //$NON-NLS-1$

		if(des.isReadOnly())
			des = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(des.getProject(), true);

		ICConfigurationDescription cfgDes = des.getDefaultSettingConfiguration();
		if (cfgDes instanceof CConfigurationDescriptionCache) {
			des = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(des.getProject(), true);
			cfgDes = des.getDefaultSettingConfiguration();
		}

		if (cfgDes != null){
			if(cfgDes.isReadOnly())
				throw ExceptionFactory.createCoreException(CCorePlugin.getResourceString("CConfigBasedDescriptorManager.4")); //$NON-NLS-1$
			return new CConfigBasedDescriptor(cfgDes);
		} else if (!des.isCdtProjectCreating()){
			throw ExceptionFactory.createCoreException(CCorePlugin.getResourceString("CConfigBasedDescriptorManager.5")); //$NON-NLS-1$
		}
		return null;
	}

	public static synchronized COwnerConfiguration getOwnerConfiguration(String id) {
		if (id.equals(NULLCOwner.getOwnerID()))
			return NULLCOwner;
		if (fOwnerConfigMap == null)
			initializeOwnerConfiguration();
		COwnerConfiguration config = fOwnerConfigMap.get(id);
		if (config == null) { // no install owner, lets create place holder config for it.
			config = new COwnerConfiguration(id, CCorePlugin.getResourceString("CDescriptorManager.owner_not_Installed")); //$NON-NLS-1$
			fOwnerConfigMap.put(id, config);
		}
		return config;
	}

	private static void initializeOwnerConfiguration() {
        IExtensionPoint extpoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "CProject"); //$NON-NLS-1$
		IExtension extension[] = extpoint.getExtensions();
		fOwnerConfigMap = new HashMap<String, COwnerConfiguration>(extension.length);
		for (int i = 0; i < extension.length; i++) {
			IConfigurationElement element[] = extension[i].getConfigurationElements();
			for (int j = 0; j < element.length; j++) {
				if (element[j].getName().equalsIgnoreCase("cproject")) { //$NON-NLS-1$
					fOwnerConfigMap.put(extension[i].getUniqueIdentifier(), new COwnerConfiguration(element[j]));
					break;
				}
			}
		}
	}

	public void startup(){
		if (fDescriptionListener != null)
			return;
		fDescriptionListener = new ICProjectDescriptionListener(){
			@Override
			public void handleEvent(CProjectDescriptionEvent event) {
				doHandleEvent(event);
			}
		};
		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(fDescriptionListener,
				CProjectDescriptionEvent.APPLIED |
				CProjectDescriptionEvent.LOADED |
				CProjectDescriptionEvent.DATA_APPLIED |
				CProjectDescriptionEvent.ABOUT_TO_APPLY);
	}

	public void shutdown(){
		if(fDescriptionListener != null)
			CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(fDescriptionListener);
		fDescriptionListener = null;
	}

	/**
	 * Hand CProjectDescription events
	 * @param event
	 */
	private void doHandleEvent(CProjectDescriptionEvent event){
		CConfigBasedDescriptor dr = null;

		// Check for an in memory descriptor matching the current event's project
		Reference<CConfigBasedDescriptor> ref = fProjectDescriptorMap.get(event.getProject());
		if (ref != null)
			dr = ref.get();
		// If no delta, return
		if (dr == null)
			return;

		try {
			dr.fLock.acquire();
			try {
				switch(event.getEventType()){
				case CProjectDescriptionEvent.LOADED:{
					// the descriptor was requested while load process
					CProjectDescription des = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(event.getProject(), true);
					if(des != null){
						ICConfigurationDescription cfgDescription = des.getDefaultSettingConfiguration();
						if(cfgDescription != null){
							dr.updateConfiguration(cfgDescription);
							dr.setDirty(false);
						}
					}
				}
				break;
				case CProjectDescriptionEvent.ABOUT_TO_APPLY:{
					CProjectDescription des = (CProjectDescription)event.getNewCProjectDescription();
					if(des != null
							&& dr.getConfigurationDescription().getProjectDescription() != des){
						CConfigBasedDescriptor.reconcile(dr, des);
					}
				}
				break;
				case CProjectDescriptionEvent.DATA_APPLIED:{
					CProjectDescription des = (CProjectDescription)event.getNewCProjectDescription();
					if(des != null){
						CConfigBasedDescriptor.reconcile(dr, des);
					}
				}
				break;
				case CProjectDescriptionEvent.APPLIED:
					CProjectDescription newDes = (CProjectDescription)event.getNewCProjectDescription();
					CProjectDescription oldDes = (CProjectDescription)event.getOldCProjectDescription();
					CDescriptorEvent desEvent = null;
					ICConfigurationDescription updatedCfg = null;
					if(oldDes == null){
						updatedCfg = newDes.getDefaultSettingConfiguration();
						desEvent = new CDescriptorEvent(dr, CDescriptorEvent.CDTPROJECT_ADDED, 0);
					} else if(newDes == null) {
						desEvent = new CDescriptorEvent(dr, CDescriptorEvent.CDTPROJECT_REMOVED, 0);
					} else {
						updatedCfg = newDes.getDefaultSettingConfiguration();
						ICConfigurationDescription newCfg = newDes.getDefaultSettingConfiguration();
						ICConfigurationDescription oldCfg = oldDes.getDefaultSettingConfiguration();
						int flags = 0;
						if(oldCfg != null && newCfg != null){
							if(newCfg.getId().equals(oldCfg.getId())){
								ICDescriptionDelta cfgDelta = findCfgDelta(event.getProjectDelta(), newCfg.getId());
								if(cfgDelta != null){
									flags = cfgDelta.getChangeFlags() & (ICDescriptionDelta.EXT_REF | ICDescriptionDelta.OWNER);
								}
							} else {
								flags = CProjectDescriptionManager.getInstance().calculateDescriptorFlags(newCfg, oldCfg);
							}
						}

						int drEventFlags = descriptionFlagsToDescriptorFlags(flags);
	//						if(drEventFlags != 0){
							desEvent = new CDescriptorEvent(dr, CDescriptorEvent.CDTPROJECT_CHANGED, drEventFlags);
	//						}
					}
					if(updatedCfg != null){
						CProjectDescription writableDes = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(event.getProject(), true);
						ICConfigurationDescription indexCfg = writableDes.getDefaultSettingConfiguration();
						dr.updateConfiguration(indexCfg);
						dr.setDirty(false);
					}
					notifyListeners(desEvent);
					break;
				}

			} catch (CoreException e){
				CCorePlugin.log(e);
			}
		} finally {
			dr.fLock.release();
		}
	}

	private int descriptionFlagsToDescriptorFlags(int flags){
		int result = 0;
		if((flags & ICDescriptionDelta.EXT_REF) != 0){
			result |= CDescriptorEvent.EXTENSION_CHANGED;
		}
		if((flags & ICDescriptionDelta.OWNER) != 0){
			result |= CDescriptorEvent.OWNER_CHANGED;
		}
		return result;
	}

	private ICDescriptionDelta findCfgDelta(ICDescriptionDelta delta, String id){
		if(delta == null)
			return null;
		ICDescriptionDelta children[] = delta.getChildren();
		for(int i = 0; i < children.length; i++){
			ICSettingObject s = children[i].getNewSetting();
			if(s != null && id.equals(s.getId()))
				return children[i];
		}
		return null;
	}

	protected void notifyListeners(final CDescriptorEvent event) {
		for (final ICDescriptorListener listener : fListeners) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void handleException(Throwable exception) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
							CCorePlugin.getResourceString("CDescriptorManager.exception.listenerError"), exception); //$NON-NLS-1$
					CCorePlugin.log(status);
				}

				@Override
				public void run() throws Exception {
					listener.descriptorChanged(event);
				}
			});
		}
	}

}
