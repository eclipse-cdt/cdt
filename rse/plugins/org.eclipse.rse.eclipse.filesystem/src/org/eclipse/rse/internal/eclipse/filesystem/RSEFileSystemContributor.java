/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Kushal Munir (IBM) - moved to internal package.
 * Martin Oberhuber (Wind River) - [181917] EFS Improvements: Avoid unclosed Streams,
 *    - Fix early startup issues by deferring FileStore evaluation and classloading,
 *    - Improve performance by RSEFileStore instance factory and caching IRemoteFile.
 *    - Also remove unnecessary class RSEFileCache and obsolete branding files.
 ********************************************************************************/


package org.eclipse.rse.internal.eclipse.filesystem;

import java.net.URI;

import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;

public class RSEFileSystemContributor extends FileSystemContributor {


	public URI browseFileSystem(String initialPath, Shell shell) {
		
		SystemRemoteFolderDialog dlg = new SystemRemoteFolderDialog(shell, "Select Folder"); //$NON-NLS-1$
		
		if (!initialPath.equals("")) { //$NON-NLS-1$
			
			try {
				URI uri = new URI(initialPath);
				IHost host = RSEFileStoreImpl.getConnectionFor(uri.getHost(), null);
				IRemoteFileSubSystem fs = RSEFileStoreImpl.getRemoteFileSubSystem(host);
				dlg.setInputObject(fs.getRemoteFileObject(uri.getPath()));			
			}
			catch (Exception e) {
			}
		}

		dlg.setNeedsProgressMonitor(true);

		if (dlg.open() == Window.OK) {
			
			Object selected = dlg.getSelectedObject();
			
			if (selected instanceof ISystemFilterReference) {
				
				ISubSystem targetSubSystem = ((ISystemFilterReference)selected).getSubSystem();
				ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
				
				if (factory.supportsDropInFilters()) {											        
					selected = targetSubSystem.getTargetForFilter((ISystemFilterReference)selected);										            
				}
			}
			
			IRemoteFile file = (IRemoteFile)selected;
			String path = file.getAbsolutePath();
			IHost host = dlg.getSelectedConnection();
			String hostName = host.getHostName();
			
			try {
				return new URI("rse", hostName, path, null); //$NON-NLS-1$
			}
			catch (Exception e) {
			}
		}
		return null;

	}

	public URI getURI(String string){
		
		try {
			return new URI(string);
		}
		catch (Exception e) {			
		}
		return null;
	}
}