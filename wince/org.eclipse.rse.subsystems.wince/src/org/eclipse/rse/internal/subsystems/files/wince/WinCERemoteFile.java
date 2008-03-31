/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from SftpRemoteFile
 *******************************************************************************/
package org.eclipse.rse.internal.subsystems.files.wince;

import org.eclipse.rse.internal.services.wince.files.WinCEHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;


public class WinCERemoteFile extends AbstractRemoteFile {

  public WinCERemoteFile(FileServiceSubSystem subSystem,
      IRemoteFileContext context, IRemoteFile parent, WinCEHostFile hostFile) {
    super(subSystem, context, parent, hostFile);
  }

  public String getCanonicalPath() {
    return getAbsolutePath();
  }

  public String getClassification() {
    // TODO
    return "unknown"; //$NON-NLS-1$
  }

  public String getEncoding() {
    // override the default implementation because it causes
    // infinite loop on WinCE, see bug #218947 
    return getParentRemoteFileSubSystem().getRemoteEncoding();
  }

}
