/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class CDescriptorManager implements IResourceChangeListener {
	private static HashMap fDescriptorMap;

	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public void shutdown() {
		getWorkspace().removeResourceChangeListener(this);
	}

	public void startup() {
		getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Traverses the delta looking for added/removed/changed launch
	 * configuration files.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if ( fDescriptorMap == null ) {
			return;
		}
		IResource resource = event.getResource();
		if ( event.getType() == IResourceChangeEvent.PRE_DELETE ) {
			if(resource.getType() == IResource.PROJECT ) {
				fDescriptorMap.remove(resource);
			}
		}
	}
	

	public synchronized ICDescriptor getDescriptor(IProject project) throws CoreException {
		if ( fDescriptorMap == null ) {
			fDescriptorMap = new HashMap();
		}
		CDescriptor cproject;
		cproject = (CDescriptor)fDescriptorMap.get(project) ;
		if ( cproject == null ) {
			cproject = new CDescriptor(project);
			fDescriptorMap.put(project, cproject);
		}
		return cproject;
	}

	public synchronized void configure(IProject project, String id) throws CoreException {
		CDescriptor cproject;
		if ( fDescriptorMap == null ) {
			fDescriptorMap = new HashMap();
		}
		if ( fDescriptorMap.get(project) != null ) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.getDefault().PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_EXISTS, "CDTProject already configured", (Throwable)null);
			throw new CoreException(status);
		}
		try {
			cproject = new CDescriptor(project, id);
		}
		catch (CoreException e) { // if .cdtproject already exists will use that
			IStatus status = e.getStatus();
			if ( status.getCode() == CCorePlugin.STATUS_CDTPROJECT_EXISTS ) {
				cproject = new CDescriptor(project);
				fDescriptorMap.put(project, cproject);
				return;
			}
			else
				throw e;
		}
		COwner cowner = new COwner(id);
		cowner.configure(project, cproject);
		cproject.saveInfo();
		fDescriptorMap.put(project, cproject);
	}
	
	
	public ICExtension[] createExtensions(String extensionID, IProject project) throws CoreException {
		ArrayList extensionList = new ArrayList(1);
		ICDescriptor cDescriptor = getDescriptor(project);
		ICExtensionReference ext[] = cDescriptor.get(extensionID, true);
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		for( int i = 0; i < ext.length; i++ ) {
			IExtensionPoint extensionPoint = pluginRegistry.getExtensionPoint(ext[i].getExtension());
			IExtension extension = extensionPoint.getExtension(ext[i].getID());
			IConfigurationElement element[] = extension.getConfigurationElements();
			for( int j = 0; j < element.length; j++ ) {
				if ( element[j].getName().equalsIgnoreCase("run") ) {
					InternalCExtension cExtension = (InternalCExtension) element[i].createExecutableExtension("class");
					cExtension.setExtenionReference(ext[i]);
					cExtension.setProject(project);
					extensionList.add(cExtension);
					break;
				}
			}
		}		
		return (ICExtension[]) extensionList.toArray(new ICExtension[extensionList.size()]);
	}	
}
