/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorListener;
import org.eclipse.cdt.core.ICDescriptorManager;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescription;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionListener;
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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

public class CConfigBasedDescriptorManager implements ICDescriptorManager {
	private static CConfigBasedDescriptorManager fInstance;
	public static final String NULL_OWNER_ID = ""; //$NON-NLS-1$
	private Map fOwnerConfigMap = null;
	private ICProjectDescriptionListener fDescriptionListener;
	
	private static final QualifiedName DESCRIPTOR_PROPERTY = new QualifiedName(CCorePlugin.PLUGIN_ID, "CDescriptor"); //$NON-NLS-1$

	private List fListeners = Collections.synchronizedList(new Vector());


	private CConfigBasedDescriptorManager(){
	}
	
	public static CConfigBasedDescriptorManager getInstance(){
		if(fInstance == null){
			fInstance = new CConfigBasedDescriptorManager();
		}
		return fInstance;
	}

	private static final COwnerConfiguration NULLCOwner = new COwnerConfiguration(NULL_OWNER_ID, 
			CCorePlugin.getResourceString("CDescriptorManager.internal_owner")); //$NON-NLS-1$

	public void configure(IProject project, String id) throws CoreException {
		CConfigBasedDescriptor dr;
		if (id.equals(NULLCOwner.getOwnerID())) { //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
					CCorePlugin.getResourceString("CDescriptorManager.exception.invalid_ownerID"), //$NON-NLS-1$
					(Throwable)null);
			throw new CoreException(status);
		}
		synchronized (this) {
			dr = findDescriptor(project, false);
			if (dr != null) {
				if (dr.getProjectOwner().getID().equals(NULLCOwner.getOwnerID())) { //$NON-NLS-1$
					// non owned descriptors are simply configure to the new owner no questions ask!
					dr = updateDescriptor(project, dr, id);
					dr.apply(true);
				} else if (!dr.getProjectOwner().getID().equals(id)) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_EXISTS,
							CCorePlugin.getResourceString("CDescriptorManager.exception.alreadyConfigured"), //$NON-NLS-1$
							(Throwable)null);
					throw new CoreException(status);
				} else {
					return; // already configured with same owner.
				}
			} else {
//				try {
					dr = findDescriptor(project, true);
					dr = updateDescriptor(project, dr, id);
					dr.apply(true);
//				} catch (CoreException e) { // if .cdtproject already exists we'll use that
//					IStatus status = e.getStatus();
//					if (status.getCode() == CCorePlugin.STATUS_CDTPROJECT_EXISTS) {
//						descriptor = new CDescriptor(this, project);
//					} else
//						throw e;
//				}
			}
		}
	}
	
	private CConfigBasedDescriptor updateDescriptor(IProject project, CConfigBasedDescriptor dr, String ownerId) throws CoreException{
		ICConfigurationDescription cfgDes = dr.getConfigurationDescription();
		ICProjectDescription projDes = cfgDes.getProjectDescription();
		CConfigurationSpecSettings settings = ((IInternalCCfgInfo)cfgDes).getSpecSettings();
		settings.setCOwner(ownerId);
		COwner owner = settings.getCOwner();
		setLoaddedDescriptor(projDes, null);
		dr = findDescriptor(projDes);
		dr.setApplyOnChange(false);
		owner.configure(project, dr);
		dr.setApplyOnChange(true);

		return dr;
	}

	public void convert(IProject project, String id) throws CoreException {
		CConfigBasedDescriptor dr = findDescriptor(project, false);
		if(dr == null){
			throw ExceptionFactory.createCoreException("the project is not a CDT project");
		}
		
		

//		boolean applyOnChange = dr.isApplyOnChange();
//		CProjectDescription des = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(project);
//		if(des == null){
//			throw ExceptionFactory.createCoreException("the project is not a CDT project");
//		}
//		ICConfigurationDescription cfgDes = des.getIndexConfiguration();
//		if(cfgDes == null){
//			throw ExceptionFactory.createCoreException("the projecty does not contain valid configurations");
//		}

		dr = updateDescriptor(project, dr, id);
		dr.apply(true);
	}

	public ICDescriptor getDescriptor(IProject project) throws CoreException {
		return getDescriptor(project, true);
	}

	public ICDescriptor getDescriptor(IProject project, boolean create)
			throws CoreException {
		return findDescriptor(project, create);
	}

	public void addDescriptorListener(ICDescriptorListener listener) {
		fListeners.add(listener);
	}

	public void removeDescriptorListener(ICDescriptorListener listener) {
		fListeners.remove(listener);
	}

	public void runDescriptorOperation(IProject project,
			ICDescriptorOperation op, IProgressMonitor monitor)
			throws CoreException {
		CConfigBasedDescriptor dr = findDescriptor(project, true);
		if (dr == null) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, "Failed to create descriptor", null)); //$NON-NLS-1$
		}

		synchronized (dr) {
			dr.setApplyOnChange(false);
			try {
				op.execute(dr, monitor);
			} finally {
				dr.setApplyOnChange(true);
			}
		}
		
		dr.apply(false);
	}
	
	private CConfigBasedDescriptor getLoaddedDescriptor(ICProjectDescription des){
		return (CConfigBasedDescriptor)des.getSessionProperty(DESCRIPTOR_PROPERTY);
	}

	private void setLoaddedDescriptor(ICProjectDescription des, CConfigBasedDescriptor dr){
		des.setSessionProperty(DESCRIPTOR_PROPERTY, dr);
	}
	
	private CConfigBasedDescriptor findDescriptor(IProject project, boolean create) throws CoreException{
		ICProjectDescription des = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		CConfigBasedDescriptor dr = null;
		if(des == null && create){
			des = createProjDescriptionForDescriptor(project);
		}
		if(des != null){
			dr = findDescriptor(des);
		} 
		return dr;
	}
	
	private CConfigBasedDescriptor findDescriptor(ICProjectDescription des) throws CoreException{
		CConfigBasedDescriptor dr = getLoaddedDescriptor(des);
		if(dr == null){
			dr = loadDescriptor((CProjectDescription)des);
			if(dr != null){
				setLoaddedDescriptor(des, dr);
			}
		}
		return dr;
	}

	private CProjectDescription createProjDescriptionForDescriptor(IProject project) throws CoreException{
		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		CProjectDescription des = (CProjectDescription)mngr.createProjectDescription(project, false);
			
		CConfigurationData data = mngr.createDefaultConfigData(project, PathEntryConfigurationDataProvider.getDataFactory());
		des.createConfiguration(CProjectDescriptionManager.DEFAULT_PROVIDER_ID, data);
		
		return des;
	}

	private CConfigBasedDescriptor loadDescriptor(CProjectDescription des) throws CoreException{
		CConfigBasedDescriptor dr = null;

		if(des != null){
			if(des.isReadOnly())
				des = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(des.getProject(), true);
			
			ICConfigurationDescription cfgDes = des.getIndexConfiguration();
	
			
			if(cfgDes != null){
				dr = new CConfigBasedDescriptor((CConfigurationDescription)cfgDes);
			} else {
				throw ExceptionFactory.createCoreException("the project does not contain valid configurations");
			}
		}
		
		return dr;
	}

	public COwnerConfiguration getOwnerConfiguration(String id) {
		if (id.equals(NULLCOwner.getOwnerID())) { //$NON-NLS-1$
			return NULLCOwner;
		}
		if (fOwnerConfigMap == null) {
			initializeOwnerConfiguration();
		}
		COwnerConfiguration config = (COwnerConfiguration)fOwnerConfigMap.get(id);
		if (config == null) { // no install owner, lets create place holder config for it.
			config = new COwnerConfiguration(id, CCorePlugin.getResourceString("CDescriptorManager.owner_not_Installed")); //$NON-NLS-1$
			fOwnerConfigMap.put(id, config);
		}
		return config;
	}

	private void initializeOwnerConfiguration() {
        IExtensionPoint extpoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "CProject"); //$NON-NLS-1$
		IExtension extension[] = extpoint.getExtensions();
		fOwnerConfigMap = new HashMap(extension.length);
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
		if(fDescriptionListener == null){
			fDescriptionListener = new ICProjectDescriptionListener(){

				public void handleEvent(CProjectDescriptionEvent event) {
					doHandleEvent(event);
				}
				
			};
			CProjectDescriptionManager.getInstance().addListener(fDescriptionListener, CProjectDescriptionEvent.APPLIED | CProjectDescriptionEvent.LOADDED);
		}
	}
	
	public void shutdown(){
		if(fDescriptionListener != null){
			CProjectDescriptionManager.getInstance().removeListener(fDescriptionListener);
		}
	}
	
	private void doHandleEvent(CProjectDescriptionEvent event){
		try {
			switch(event.getEventType()){
			case CProjectDescriptionEvent.LOADDED:{
				CProjectDescription des = (CProjectDescription)event.getNewCProjectDescription();
				CConfigBasedDescriptor dr = getLoaddedDescriptor(des);
				if(dr != null){
					//the descriptor was requested while load process
					des = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(des.getProject(), true);
					ICConfigurationDescription cfgDescription = des.getIndexConfiguration();
					if(cfgDescription != null)
						dr.updateConfiguration((CConfigurationDescription)cfgDescription);
					else
						setLoaddedDescriptor(des, null);
				}
			}
				break;
			case CProjectDescriptionEvent.APPLIED:
				CProjectDescription newDes = (CProjectDescription)event.getNewCProjectDescription();
				CProjectDescription oldDes = (CProjectDescription)event.getOldCProjectDescription();
				CDescriptorEvent desEvent = null;
				CConfigBasedDescriptor dr = null;
				ICConfigurationDescription updatedCfg = null;
				if(oldDes == null){
					dr = findDescriptor(newDes);
					updatedCfg = newDes.getIndexConfiguration();
					if(dr != null){
						desEvent = new CDescriptorEvent(dr, CDescriptorEvent.CDTPROJECT_ADDED, CDescriptorEvent.EXTENSION_CHANGED | CDescriptorEvent.OWNER_CHANGED);
					}
				} else if(newDes == null) {
					dr = findDescriptor(oldDes);
					if(dr != null){
						desEvent = new CDescriptorEvent(dr, CDescriptorEvent.CDTPROJECT_REMOVED, CDescriptorEvent.EXTENSION_CHANGED | CDescriptorEvent.OWNER_CHANGED);
					}
				} else {
					dr = findDescriptor(newDes);
					updatedCfg = newDes.getIndexConfiguration();
					if(dr != null){
						ICConfigurationDescription newCfg = newDes.getIndexConfiguration();
						ICConfigurationDescription oldCfg = oldDes.getIndexConfiguration();
						int flags = 0;
						if(newCfg.getId().equals(oldCfg.getId())){
							ICDescriptionDelta cfgDelta = findCfgDelta(event.getProjectDelta(), newCfg.getId());
							if(cfgDelta != null){
								flags = cfgDelta.getChangeFlags() & (ICDescriptionDelta.EXT_REF | ICDescriptionDelta.OWNER);
							}
						} else {
							flags = CProjectDescriptionManager.getInstance().calculateDescriptorFlags(newCfg, oldCfg);
						}
						
						int drEventFlags = descriptionFlagsToDescriptorFlags(flags);
						if(drEventFlags != 0){
							desEvent = new CDescriptorEvent(dr, CDescriptorEvent.CDTPROJECT_CHANGED, drEventFlags);
						}
					}
				}
				
				if(updatedCfg != null && dr != null){
					CProjectDescription writableDes = (CProjectDescription)CProjectDescriptionManager.getInstance().getProjectDescription(event.getProject(), true);
					ICConfigurationDescription indexCfg = writableDes.getIndexConfiguration();
					dr.updateConfiguration((CConfigurationDescription)indexCfg);
				}
				if(desEvent != null){
					notifyListeners(desEvent);
				}
				break;
		}
		} catch (CoreException e){
		}
	}
	
	private int descriptionFlagsToDescriptorFlags(int flags){
		int result = 0;
		if((flags & ICDescriptionDelta.EXT_REF) != 0){
			result |= CDescriptorEvent.EXTENSION_CHANGED;
		}
//		if((flags & ICDescriptionDelta.OWNER) != 0){
			result |= CDescriptorEvent.OWNER_CHANGED;
//		}
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
		final ICDescriptorListener[] listeners;
		synchronized (fListeners) {
			listeners = (ICDescriptorListener[])fListeners.toArray(new ICDescriptorListener[fListeners.size()]);
		}
		for (int i = 0; i < listeners.length; i++) {
			final int index = i;
			Platform.run(new ISafeRunnable() {

				public void handleException(Throwable exception) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
							CCorePlugin.getResourceString("CDescriptorManager.exception.listenerError"), exception); //$NON-NLS-1$
					CCorePlugin.log(status);
				}

				public void run() throws Exception {
					listeners[index].descriptorChanged(event);
				}
			});
		}
	}

}
