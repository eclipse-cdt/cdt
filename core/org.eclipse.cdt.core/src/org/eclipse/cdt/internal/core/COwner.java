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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class COwner implements ICOwnerInfo {
	String ownerID;
	String fPlatform;
	IExtension extension;
	
	public COwner(String id) throws CoreException {
		ownerID = id;
		IExtensionPoint extpoint = CCorePlugin.getDefault().getDescriptor().getExtensionPoint("CProject"); //$NON-NLS-1$
		extension =  extpoint.getExtension(ownerID);
		
		if (extension == null) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, CCorePlugin.getResourceString("COwner.exception.invalidOwnerID"), (Throwable)null); //$NON-NLS-1$
			throw new CoreException(status);
		}
	}

	public String getID() {
		return ownerID;
	}
	
	public String getName() {
		return extension == null ? null : extension.getLabel();
	}

	public String getPlatform() {
		if ( fPlatform == null ) {
			IConfigurationElement element[] = extension.getConfigurationElements();
			for( int i = 0; i < element.length; i++ ) {
				if ( element[i].getName().equalsIgnoreCase("cproject") ) { //$NON-NLS-1$
					fPlatform = element[i].getAttribute("platform"); //$NON-NLS-1$
					break;
				}
			}
		}
		return fPlatform == null ? "*" : fPlatform; //$NON-NLS-1$
	}
	
	void configure(IProject project, ICDescriptor cproject) throws CoreException {
		IConfigurationElement element[] = extension.getConfigurationElements();
		for( int i = 0; i < element.length; i++ ) {
			if ( element[i].getName().equalsIgnoreCase("cproject") ) { //$NON-NLS-1$
				ICOwner owner = (ICOwner) element[i].createExecutableExtension("class"); //$NON-NLS-1$
				owner.configure(cproject);
				return;
			}
		}
		IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, CCorePlugin.getResourceString("COwner.exception.invalidOwnerExtension"), (Throwable)null); //$NON-NLS-1$
		throw new CoreException(status);
	}

	void update(IProject project, ICDescriptor cproject, String extensionID) throws CoreException {
		IConfigurationElement element[] = extension.getConfigurationElements();
		for( int i = 0; i < element.length; i++ ) {
			if ( element[i].getName().equalsIgnoreCase("cproject") ) { //$NON-NLS-1$
				ICOwner owner = (ICOwner) element[i].createExecutableExtension("class"); //$NON-NLS-1$
				owner.update(cproject, extensionID);
				return;
			}
		}
		IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, CCorePlugin.getResourceString("COwner.exception.invalidOwnerExtension"), (Throwable)null); //$NON-NLS-1$
		throw new CoreException(status);
	}

}
