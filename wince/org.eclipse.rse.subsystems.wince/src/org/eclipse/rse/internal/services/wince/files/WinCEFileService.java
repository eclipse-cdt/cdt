/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from SftpFileService and LocalFileService
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 * Radoslav Gerganov (ProSyst) - [221211] [api][breaking][files] need batch operations to indicate which operations were successful
 * Martin Oberhuber (Wind River) - [221211] Throw SystemUnsupportedOperationException for WinCE setLastModified() and setReadOnly()
 * Radoslav Gerganov (ProSyst) - [230850] [WinCE] Implement setLastModified and setReadOnly in WinCEFileService
 * Radoslav Gerganov (ProSyst) - [231425] [WinCE] Use the progress monitors in WinCEFileService
 * Radoslav Gerganov (ProSyst) - [230856] [WinCE] Improve the error handling in WinCEFileService
 * Radoslav Gerganov (ProSyst) - [230919] IFileService.delete() should not return a boolean
 * Radoslav Gerganov (ProSyst) - [235360] Return proper "Root" IHostFile
 *******************************************************************************/
package org.eclipse.rse.internal.services.wince.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.internal.services.wince.IRapiSessionProvider;
import org.eclipse.rse.internal.services.wince.IWinCEService;
import org.eclipse.rse.internal.subsystems.files.wince.Activator;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemElementNotFoundException;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.services.clientserver.messages.SystemUnexpectedErrorException;
import org.eclipse.rse.services.clientserver.messages.SystemUnsupportedOperationException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.tm.rapi.IRapiSession;
import org.eclipse.tm.rapi.Rapi;
import org.eclipse.tm.rapi.RapiException;
import org.eclipse.tm.rapi.RapiFindData;

public class WinCEFileService extends AbstractFileService implements IWinCEService {

  IRapiSessionProvider sessionProvider;

  public WinCEFileService(IRapiSessionProvider sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  String concat(String parentDir, String fileName) {
    String result = parentDir;
    if (!result.endsWith("\\")) { //$NON-NLS-1$
      result += "\\"; //$NON-NLS-1$
    }
    result += fileName;
    return result;
  }

  protected IHostFile[] internalFetch(String parentPath, String fileFilter,
      int fileType, IProgressMonitor monitor) throws SystemMessageException {
    if (fileFilter == null) {
      fileFilter = "*"; //$NON-NLS-1$
    }
    IMatcher fileMatcher = null;
    if (fileFilter.endsWith(",")) { //$NON-NLS-1$
      String[] types = fileFilter.split(","); //$NON-NLS-1$
      fileMatcher = new FileTypeMatcher(types, true);
    } else {
      fileMatcher = new NamePatternMatcher(fileFilter, true, true);
    }
    List results = new ArrayList();
    try {
      IRapiSession session = sessionProvider.getSession();
      RapiFindData[] foundFiles = session.findAllFiles(concat(parentPath,"*"),  //$NON-NLS-1$
            Rapi.FAF_NAME | Rapi.FAF_ATTRIBUTES | Rapi.FAF_LASTWRITE_TIME |
            Rapi.FAF_SIZE_HIGH | Rapi.FAF_SIZE_LOW);
      for (int i = 0 ; i < foundFiles.length ; i++) {
        String fileName = foundFiles[i].fileName;
        if (fileMatcher.matches(fileName)) {
          WinCEHostFile hostFile = makeHostFile(parentPath, fileName, foundFiles[i]);
          if (isRightType(fileType, hostFile)) {
            results.add(hostFile);
          }
        }
      }
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
    return (IHostFile[]) results.toArray(new IHostFile[results.size()]);
  }

  private WinCEHostFile makeHostFile(String parentPath, String fileName, RapiFindData findData) {
    boolean isDirectory = (findData.fileAttributes & Rapi.FILE_ATTRIBUTE_DIRECTORY) != 0;
    boolean isWritable = (findData.fileAttributes & Rapi.FILE_ATTRIBUTE_READONLY) == 0;
    long lastModified = (findData.lastWriteTime / 10000) - Rapi.TIME_DIFF;
    long size = findData.fileSize;
    return new WinCEHostFile(parentPath, fileName, isDirectory, false, isWritable, lastModified, size);
  }

  private boolean isDirectory(IRapiSession session, String fullPath) {
    int attr = session.getFileAttributes(fullPath);
    if (attr == -1) {
      return false;
    }
    return (attr & Rapi.FILE_ATTRIBUTE_DIRECTORY) != 0;
  }

  private boolean exist(IRapiSession session, String fileName) {
    return session.getFileAttributes(fileName) != -1;
  }

  public void copy(String srcParent, String srcName, String tgtParent,
      String tgtName, IProgressMonitor monitor) throws SystemMessageException {
    String srcFullPath = concat(srcParent, srcName);
    String tgtFullPath = concat(tgtParent, tgtName);
    if (srcFullPath.equals(tgtFullPath)) {
      // prevent copying file/folder to itself
      throw new SystemUnsupportedOperationException(Activator.PLUGIN_ID, "Cannot copy file or folder to itself"); //$NON-NLS-1$
    }
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    IRapiSession session = sessionProvider.getSession();
    try {
      if (isDirectory(session, srcFullPath)) {
        if (tgtFullPath.startsWith(srcFullPath + "\\")) { //$NON-NLS-1$
          // prevent copying \a to \a\b\c
          throw new SystemUnsupportedOperationException(Activator.PLUGIN_ID, "Cannot copy folder to its subfolder"); //$NON-NLS-1$
        }
        if (!exist(session, tgtFullPath)) {
          // the target path is a directory and it doesn't exist -> create it
          session.createDirectory(tgtFullPath);
        }
        RapiFindData[] allFiles = session.findAllFiles(concat(srcFullPath,"*"), Rapi.FAF_NAME); //$NON-NLS-1$
        for (int i = 0 ; i < allFiles.length ; i++) {
          String fileName = allFiles[i].fileName;
          if (monitor.isCanceled()) {
            throw new SystemOperationCancelledException();
          }
          copy(srcFullPath, fileName, tgtFullPath, fileName, monitor);
        }
      } else {
        session.copyFile(srcFullPath, tgtFullPath);
        monitor.worked(1);
      }
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
  }

  public void copyBatch(String[] srcParents, String[] srcNames,
      String tgtParent, IProgressMonitor monitor) throws SystemMessageException {
    for (int i = 0 ; i < srcParents.length ; i++) {
      copy(srcParents[i], srcNames[i], tgtParent, srcNames[i], monitor);
    }
  }

  public IHostFile createFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, fileName);
    IRapiSession session = sessionProvider.getSession();
    try {
      int handle = session.createFile(fullPath, Rapi.GENERIC_WRITE, Rapi.FILE_SHARE_READ,
          Rapi.CREATE_ALWAYS, Rapi.FILE_ATTRIBUTE_NORMAL);
      session.closeHandle(handle);
      RapiFindData findData = new RapiFindData();
      handle = session.findFirstFile(fullPath, findData);
      session.findClose(handle);
      return makeHostFile(remoteParent, fileName, findData);
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
  }

  public IHostFile createFolder(String remoteParent, String folderName, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, folderName);
    IRapiSession session = sessionProvider.getSession();
    try {
      session.createDirectory(fullPath);
      RapiFindData findData = new RapiFindData();
      int handle = session.findFirstFile(fullPath, findData);
      session.findClose(handle);
      return makeHostFile(remoteParent, folderName, findData);
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
  }

  public void delete(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, fileName);
    IRapiSession session = sessionProvider.getSession();
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    try {
      if (!exist(session, fullPath)) {
        throw new SystemElementNotFoundException(fullPath, "delete"); //$NON-NLS-1$
      }
      if (isDirectory(session, fullPath)) {
        // recursive delete if it is a directory
        RapiFindData[] allFiles = session.findAllFiles(concat(fullPath, "*"), Rapi.FAF_NAME); //$NON-NLS-1$
        for (int i = 0; i < allFiles.length; i++) {
          if (monitor.isCanceled()) {
            throw new SystemOperationCancelledException();
          }          
          delete(fullPath, allFiles[i].fileName, monitor);
        }
        session.removeDirectory(fullPath);
      } else {
        // it is a file
        session.deleteFile(fullPath);
        monitor.worked(1);
      }
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
  }

  public void download(String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding,
      IProgressMonitor monitor) throws SystemMessageException {

    if (!localFile.exists()) {
      File localParentFile = localFile.getParentFile();
      if (!localParentFile.exists()) {
        if (!localParentFile.mkdirs()) {
          throw new SystemUnexpectedErrorException(Activator.PLUGIN_ID);
        }
      }
    }
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    String fullPath = concat(remoteParent, remoteFile);
    IRapiSession session = sessionProvider.getSession();
    int handle = Rapi.INVALID_HANDLE_VALUE;
    BufferedOutputStream bos = null;
    try {
      handle = session.createFile(fullPath, Rapi.GENERIC_READ,
          Rapi.FILE_SHARE_READ, Rapi.OPEN_EXISTING, Rapi.FILE_ATTRIBUTE_NORMAL);      
      long fileSize = session.getFileSize(handle);
      monitor.beginTask(fullPath, (int) fileSize);
      bos = new BufferedOutputStream(new FileOutputStream(localFile));
      // don't increase the buffer size! the native functions sometimes fail with large buffers, 4K always work
      byte[] buffer = new byte[4 * 1024];
      while (true) {
        int bytesRead = session.readFile(handle, buffer);
        if (bytesRead == -1) {
          break;
        }
        bos.write(buffer, 0, bytesRead);
        if (monitor.isCanceled()) {
          throw new SystemOperationCancelledException();
        }
        monitor.worked(bytesRead);
      }
      bos.flush();
      monitor.done();
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    } catch (IOException e) {
      throw new RemoteFileIOException(e);
    } finally {
      if (handle != Rapi.INVALID_HANDLE_VALUE) {
        try {
          session.closeHandle(handle);
        } catch (RapiException e) {
          // ignore
        }
      }
      if (bos != null) {
        try {
          bos.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  public IHostFile getFile(String remoteParent, String name, IProgressMonitor monitor) throws SystemMessageException {
    if (remoteParent == null || remoteParent.length() == 0) {
    	// special case for root
    	return getRoots(null)[0];
    }
    IRapiSession session = sessionProvider.getSession();
    try {
      RapiFindData findData = new RapiFindData();
      int h = session.findFirstFile(concat(remoteParent, name), findData);
      session.findClose(h);
      return makeHostFile(remoteParent, name, findData);
    } catch (RapiException e) {
      // ignore the exception and return dummy
    }
    // return dummy if the file doesn't exist
    WinCEHostFile dummy = new WinCEHostFile(remoteParent, name, false, false, false, 0, 0);
    dummy.setExists(false);
    return dummy;
  }

  public IHostFile[] getRoots(IProgressMonitor monitor) throws SystemMessageException {
    return new WinCEHostFile[] { new WinCEHostFile(null, "\\", true, true, true, 0, 0) }; //$NON-NLS-1$
  }

  public IHostFile getUserHome() {
    return new WinCEHostFile("\\", "My Documents", true, false, true, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public boolean isCaseSensitive() {
    return false;
  }

  public void move(String srcParent, String srcName, String tgtParent, String tgtName,
      IProgressMonitor monitor) throws SystemMessageException {
    copy(srcParent, srcName, tgtParent, tgtName, monitor);
    delete(srcParent, srcName, monitor);
  }

  public void rename(String remoteParent, String oldName, String newName,
      IProgressMonitor monitor) throws SystemMessageException {
    String oldFullPath = concat(remoteParent, oldName);
    String newFullPath = concat(remoteParent, newName);
    IRapiSession session = sessionProvider.getSession();
    try {
      session.moveFile(oldFullPath, newFullPath);
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
  }

  public void rename(String remoteParent, String oldName, String newName, IHostFile oldFile,
      IProgressMonitor monitor) throws SystemMessageException {
    rename(remoteParent, oldName, newName, monitor);
    String newFullPath = concat(remoteParent, newName);
    oldFile.renameTo(newFullPath);
  }

  public void setLastModified(String parent, String name, long timestamp, IProgressMonitor monitor) throws SystemMessageException {
    IRapiSession session = sessionProvider.getSession();
    String fullPath = concat(parent, name);
    int handle = Rapi.INVALID_HANDLE_VALUE;
    try {
      handle = session.createFile(fullPath, Rapi.GENERIC_WRITE, 
    		  Rapi.FILE_SHARE_READ, Rapi.OPEN_EXISTING, Rapi.FILE_ATTRIBUTE_NORMAL);
      session.setFileLastWriteTime(handle, timestamp);
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    } finally {
      if (handle != Rapi.INVALID_HANDLE_VALUE) {
        try {
          session.closeHandle(handle);
        } catch (RapiException e) {
          // ignore
        }
      }
    }
  }

  public void setReadOnly(String parent, String name, boolean readOnly, IProgressMonitor monitor) throws SystemMessageException {
    IRapiSession session = sessionProvider.getSession();
    String fullPath = concat(parent, name);
    int attr = session.getFileAttributes(fullPath);
    if (readOnly) {
      attr = attr | Rapi.FILE_ATTRIBUTE_READONLY;
    } else {
      attr = attr & (~Rapi.FILE_ATTRIBUTE_READONLY);
    }
    try {
      session.setFileAttributes(fullPath, attr);
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
  }
  
  private void internalUpload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary,
      String hostEncoding, long fileSize, IProgressMonitor monitor) throws SystemMessageException {
    BufferedInputStream bis = new BufferedInputStream(stream);
    IRapiSession session = sessionProvider.getSession();
    String fullPath = concat(remoteParent, remoteFile);
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    if (fileSize != -1) {
      monitor.beginTask(fullPath, (int) fileSize);
    }
    int handle = Rapi.INVALID_HANDLE_VALUE;
    try {
      handle = session.createFile(fullPath, Rapi.GENERIC_WRITE,
          Rapi.FILE_SHARE_READ, Rapi.CREATE_ALWAYS, Rapi.FILE_ATTRIBUTE_NORMAL);
      // don't increase the buffer size! the native functions sometimes fail with large buffers, 4K always work
      byte[] buffer = new byte[4 * 1024];
      while (true) {
        int bytesRead = bis.read(buffer);
        if (bytesRead == -1) {
          break;
        }
        session.writeFile(handle, buffer, 0, bytesRead);
        if (monitor.isCanceled()) {
          throw new SystemOperationCancelledException();
        }
        monitor.worked(bytesRead);
      }
      monitor.done();
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    } catch (IOException e) {
      throw new RemoteFileIOException(e);
    } finally {
      if (handle != Rapi.INVALID_HANDLE_VALUE) {
        try {
          session.closeHandle(handle);
        } catch (RapiException e) {
          // ignore
        }
      }
      try {
        bis.close();
      } catch (IOException e) {
        // ignore
      }
    }    
  }

  public void upload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary,
      String hostEncoding, IProgressMonitor monitor) throws SystemMessageException {
    internalUpload(stream, remoteParent, remoteFile, isBinary, hostEncoding, -1, monitor);
  }

  public void upload(File localFile, String remoteParent, String remoteFile, boolean isBinary,
      String srcEncoding, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException {
    long fileSize = localFile.length();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(localFile);
    } catch (FileNotFoundException e) {
      throw new SystemUnexpectedErrorException(Activator.PLUGIN_ID);
    }
    //FIXME what to do with srcEncoding ?
    internalUpload(fis, remoteParent, remoteFile, isBinary, hostEncoding, fileSize, monitor);
  }

  public InputStream getInputStream(String remoteParent, String remoteFile,
      boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, remoteFile);
    IRapiSession session = sessionProvider.getSession();
    try {
      int handle = session.createFile(fullPath, Rapi.GENERIC_READ,
          Rapi.FILE_SHARE_READ, Rapi.OPEN_EXISTING, Rapi.FILE_ATTRIBUTE_NORMAL);
      return new BufferedInputStream(new WinCEInputStream(session, handle));
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
  }

  public OutputStream getOutputStream(String remoteParent, String remoteFile,
      int options, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, remoteFile);
    IRapiSession session = sessionProvider.getSession();
    try {
      int cd = Rapi.CREATE_ALWAYS;
      if ((options & IFileService.APPEND) == 0) {
        cd = Rapi.CREATE_ALWAYS;
      } else {
        cd = Rapi.OPEN_EXISTING;
      }
      int handle = session.createFile(fullPath, Rapi.GENERIC_WRITE,
          Rapi.FILE_SHARE_READ, cd, Rapi.FILE_ATTRIBUTE_NORMAL);
      return new BufferedOutputStream(new WinCEOutputStream(session, handle));
    } catch (RapiException e) {
      throw new RemoteFileIOException(e);
    }
  }

  public String getDescription() {
    return Messages.WinCEFileService_0;
  }

  public String getName() {
    return Messages.WinCEFileService_1;
  }

  private static class WinCEInputStream extends InputStream {

    private int handle;
    private IRapiSession session;

    public WinCEInputStream(IRapiSession session, int handle) {
      this.handle = handle;
      this.session = session;
    }

    public int read() throws IOException {
      byte[] b = new byte[1];
      try {
        int br = session.readFile(handle, b);
        return (br == -1) ? -1 : b[0];
      } catch (RapiException e) {
        throw new IOException(e.getMessage());
      }
    }

    public int read(byte[] b, int off, int len) throws IOException {
      try {
        return session.readFile(handle, b, off, len);
      } catch (RapiException e) {
        throw new IOException(e.getMessage());
      }
    }

    public void close() throws IOException {
      try {
        session.closeHandle(handle);
      } catch (RapiException e) {
        throw new IOException(e.getMessage());
      }
    }

  }

  private static class WinCEOutputStream extends OutputStream {

    private int handle;
    private IRapiSession session;

    public WinCEOutputStream(IRapiSession session, int handle) {
      this.session = session;
      this.handle = handle;
    }

    public void write(int b) throws IOException {
      try {
        session.writeFile(handle, new byte[] {(byte)b});
      } catch (RapiException e) {
        throw new IOException(e.getMessage());
      }
    }

    public void write(byte[] b, int off, int len) throws IOException {
      try {
        session.writeFile(handle, b, off, len);
      } catch (RapiException e) {
        throw new IOException(e.getMessage());
      }
    }

    public void close() throws IOException {
      try {
        session.closeHandle(handle);
      } catch (RapiException e) {
        throw new IOException(e.getMessage());
      }
    }
  }
}
