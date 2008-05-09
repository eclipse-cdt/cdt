/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from SftpHostFile
 * Radoslav Gerganov (ProSyst) - [230850] [WinCE] Implement setLastModified and setReadOnly in WinCEFileService
 *******************************************************************************/
package org.eclipse.rse.internal.services.wince.files;

import org.eclipse.rse.services.files.IHostFile;

public class WinCEHostFile implements IHostFile {

  String name;
  String parentPath;
  boolean isDirectory;
  boolean isRoot;
  boolean isArchive = false;
  boolean isReadable = true;
  boolean isWritable = true;
  boolean exists = true;
  long lastModified = 0;
  long size = 0;
  
  public WinCEHostFile(String parentPath, String name, boolean isDirectory, 
      boolean isRoot, boolean isWritable, long lastModified, long size) {
    this.parentPath = parentPath;
    this.name = name;
    this.isDirectory = isDirectory;
    this.isRoot = isRoot;
    this.isWritable = isWritable;
    this.lastModified = lastModified;
    this.size = size;
  }
  
  public boolean canRead() {
    return isReadable;
  }

  public boolean canWrite() {
    return isWritable;
  }

  public boolean exists() {
    return exists;
  }
  
  public void setExists(boolean exists) {
    this.exists = exists;
  }

  public String getAbsolutePath() {
    if (isRoot()) {
      return getName();
    }
    String path = parentPath;
    if (!parentPath.endsWith("\\")) { //$NON-NLS-1$
      path += "\\"; //$NON-NLS-1$
    }
    path += name;
    return path;
  }

  public long getModifiedDate() {
    return lastModified;
  }

  public String getName() {
    return name;
  }

  public String getParentPath() {
    return parentPath;
  }

  public long getSize() {
    return size;
  }

  public boolean isArchive() {
    return isArchive;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public boolean isFile() {
    return !(isDirectory || isRoot);
  }

  public boolean isHidden() {
    return false;
  }

  public boolean isRoot() {
    return isRoot;
  }

  public void renameTo(String newAbsolutePath) {
    int ind = newAbsolutePath.lastIndexOf("\\"); //$NON-NLS-1$
    if (ind == -1) {
      name = newAbsolutePath;
    } else {
      parentPath = newAbsolutePath.substring(0, ind);
      name = newAbsolutePath.substring(ind+1);
    }
  }

}
