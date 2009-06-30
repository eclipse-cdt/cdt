/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [188360] renamed from plugin org.eclipse.rse.eclipse.filesystem
 * Martin Oberhuber (Wind River) - [189441] fix EFS operations on Windows (Local) systems
 * David Dykstal (IBM) - [235840] externalizing dialog title
 * David McKnight  (IBM)         - [280763] [efs] Cannot pick a file when linking a resource (only folders)
 ********************************************************************************/


package org.eclipse.rse.internal.efs.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.internal.efs.RSEFileStoreImpl;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;

public class RSEFileSystemContributor extends FileSystemContributor {


	public URI browseFileSystem(String initialPath, Shell shell) {

		SystemRemoteFileDialog dlg = new SystemRemoteFileDialog(shell);

		
		if (!initialPath.equals("")) { //$NON-NLS-1$
			
			try {
				URI uri = new URI(initialPath);
				IHost host = RSEFileStoreImpl.getConnectionFor(uri.getHost(), null);
				IRemoteFileSubSystem fs = RSEFileStoreImpl.getRemoteFileSubSystem(host);
				dlg.setInputObject(fs.getRemoteFileObject(uri.getPath(), new NullProgressMonitor()));			
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
			
			IHost host = dlg.getSelectedConnection();
			String hostName = host.getHostName();

			IRemoteFile file = (IRemoteFile)selected;
			String path = file.getAbsolutePath();
			if (host.getSystemType().isWindows()) {
				path = path.replace('\\', '/');
			}
			path = fixPathForURI(path);
			try {
				return new URI("rse", hostName, path, null); //$NON-NLS-1$
			}
			catch (URISyntaxException e) {
				SystemMessageDialog.displayErrorMessage(SystemMessageDialog.getDefaultShell(), e.getLocalizedMessage());
			}
		}
		return null;
	}
	
	/**
	 * Adapt a local file system path such that it can be used as
	 * path in an URI. Converts path delimiters do '/' default 
	 * delimiter, and adds a slash in front if necessary.  
	 * @param path the path to adapt
	 * @return adapted path
	 */
	private String fixPathForURI(String path) {
		String sep = PathUtility.getSeparator(path);
		if (!sep.equals("/")) { //$NON-NLS-1$
			path = path.replace(sep.charAt(0), '/');
		}
		//<adapted from org.eclipse.core.filesystem.URIUtil.toURI() Copyright(c) 2005, 2006 IBM>
		final int length = path.length();
		StringBuffer pathBuf = new StringBuffer(length + 3);
		//There must be a leading slash in a hierarchical URI
		if (length > 0 && (path.charAt(0) != '/'))
			pathBuf.append('/');
		//additional double-slash for UNC paths to distinguish from host separator
		if (path.startsWith("//")) //$NON-NLS-1$
			pathBuf.append('/').append('/');
		pathBuf.append(path);
		//</adapted from org.eclipse.core.filesystem.URIUtil.toURI() Copyright(c) 2005, 2006 IBM>
		return pathBuf.toString();
	}

	public URI getURI(String string){
		try {
			return new URI(string);
		}
		catch (URISyntaxException e) {
			//Do not show an error or log here, since this method is called repeatedly while typing
		}
		return null;
	}
}