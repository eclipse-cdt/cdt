/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from FileServiceSubSystem
 *******************************************************************************/
package org.eclipse.rse.subsystems.files.wince;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;

public class WinCEFileServiceSubSystem extends FileServiceSubSystem {

  public WinCEFileServiceSubSystem(IHost host, IConnectorService connectorService, IFileService hostFileService,
      IHostFileToRemoteFileAdapter fileAdapter, ISearchService searchService) {
    super(host, connectorService, hostFileService, fileAdapter, searchService);
  }

  public IRemoteFile getRemoteFileObject(String folderOrFileName, IProgressMonitor monitor) throws SystemMessageException {
    String fofName = folderOrFileName.replace('/', '\\');
    IRemoteFile file = getCachedRemoteFile(fofName);
    if (file != null && !file.isStale()) {
      return file;
    }
    
    // for bug 207095, implicit connect if the connection is not connected
    checkIsConnected(monitor);
    
    if (fofName.equals("\\")) { //$NON-NLS-1$
      try {
        return listRoots(null)[0];
      } catch (Exception e) {
      }
    } 
      
    if (fofName.equals(".")) { //$NON-NLS-1$
      IRemoteFile userHome = getUserHome();
      if (userHome == null){
        // with 207095, it's possible that we could be trying to get user home when not connected 
        SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
        throw new SystemMessageException(msg);
      }
      return userHome;
    }

    if (fofName.endsWith("\\")) { //$NON-NLS-1$
      fofName = fofName.substring(0, fofName.length() - 1);
    }
    
    int lastSep = fofName.lastIndexOf("\\"); //$NON-NLS-1$
    if (lastSep > -1) {     
      String parentPath = fofName.substring(0, lastSep);
      if (parentPath.length() == 0) {
        parentPath = "\\"; //$NON-NLS-1$
      }
      String name = fofName.substring(lastSep + 1, fofName.length());

      IHostFile node = getFile(parentPath, name, monitor);
      if (node != null) {
        IRemoteFile parent = null;
        if (!node.isRoot()) {
          //parent = getRemoteFileObject(parentPath);
        }
        return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), parent, node);
      }
    }
    return null;
  }
  
}
