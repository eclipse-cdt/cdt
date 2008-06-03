/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from SftpFileAdapter
 * Martin Oberhuber (Wind River) - [235363][api][breaking] IHostFileToRemoteFileAdapter methods should return AbstractRemoteFile
 *******************************************************************************/
package org.eclipse.rse.internal.subsystems.files.wince;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.internal.services.wince.files.WinCEHostFile;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;


public class WinCEFileAdapter implements IHostFileToRemoteFileAdapter {

  public AbstractRemoteFile convertToRemoteFile(FileServiceSubSystem ss,
      IRemoteFileContext context, IRemoteFile parent, IHostFile node) {

    WinCERemoteFile remoteFile = new WinCERemoteFile(ss, context, parent, (WinCEHostFile) node);
    ss.cacheRemoteFile(remoteFile);
    return remoteFile;
  }

  public AbstractRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss,
      IRemoteFileContext context, IRemoteFile parent, IHostFile[] nodes) {

    List results = new ArrayList();
    if (nodes != null) {
      for (int i = 0 ; i < nodes.length ; i++) {
        WinCEHostFile node = (WinCEHostFile) nodes[i];
        WinCERemoteFile remoteFile = new WinCERemoteFile(ss, context, parent, node);
        results.add(remoteFile);
        ss.cacheRemoteFile(remoteFile);
      }
    }
    return (WinCERemoteFile[]) results.toArray(new WinCERemoteFile[results.size()]);
  }

}
