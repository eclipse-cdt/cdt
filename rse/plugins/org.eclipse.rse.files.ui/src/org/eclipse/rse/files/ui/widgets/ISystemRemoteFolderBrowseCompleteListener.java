/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.widgets;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;


/**
 * Interface for listeners interested in being notified whenever
 * the browse button in <code>SystemRemoteFolderCombo</code> is run.
 */
public interface ISystemRemoteFolderBrowseCompleteListener {
	
	/**
	 * Notifies that the given file was selected from the browse dialog. Note that the file will
	 * be null if the user cancelled from the browse dialog.
	 * @param remoteFile the remote file that was selected, or <code>null</code> if the user cancelled
	 * from the browse dialog.
	 */
	public void fileSelected(IRemoteFile remoteFile);
}