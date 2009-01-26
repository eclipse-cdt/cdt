/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight     (IBM)  - [218227][usability] Contribute a "Show in RSE" action to Resource Navigator and Project Explorer
 ********************************************************************************/
package org.eclipse.rse.internal.files.ui.actions;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.ui.actions.ShowInSystemsViewDelegate;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

public class ShowResourceInSystemsViewDelegate extends
		ShowInSystemsViewDelegate {
	
	public void run(IAction action) {

		if (_selectedObject instanceof IResource){			
			Object remoteObject = null;
			IResource resource = (IResource)_selectedObject;
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			// need to find the remote equivalent of this
			IPath location = resource.getLocation();
	
			if (location != null){
				String fullPath = location.toOSString();
							
				IHost localHost = sr.getLocalHost();
				if (localHost != null){
					IRemoteFileSubSystem ss = RemoteFileUtility.getFileSubSystem(localHost);
					try {
						remoteObject = ss.getRemoteFileObject(fullPath, new NullProgressMonitor());
					} 
					catch (Exception e) {
					}
				}
			}
			else {
				URI uri = resource.getLocationURI();
				
				String hostName = uri.getHost();
				String fullPath = uri.getPath();

				IHost host = null;
				
				// find the host
				ISystemProfile[] profiles = sr.getSystemProfileManager().getActiveSystemProfiles();
				for (int i = 0; i < profiles.length && host == null; i++){
					ISystemProfile profile = profiles[i];
					host = sr.getHost(profile, hostName);
				}
				
				if (host != null){
					IRemoteFileSubSystem ss = RemoteFileUtility.getFileSubSystem(host);
					try {
						remoteObject = ss.getRemoteFileObject(fullPath, new NullProgressMonitor());
					} 
					catch (Exception e) {
					}
				}
				
			}
			
			if (remoteObject != null){
				_selectedObject = remoteObject;
			}
			else {
				//unable to find remote object equivalent so returning
				return;
			}
		}
		super.run(action);
	}
}
