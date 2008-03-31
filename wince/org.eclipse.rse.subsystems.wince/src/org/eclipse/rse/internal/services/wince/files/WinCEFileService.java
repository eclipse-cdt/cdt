/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from SftpFileService and LocalFileService
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
import org.eclipse.rse.internal.services.wince.IRapiSessionProvider;
import org.eclipse.rse.internal.services.wince.IWinCEService;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.tm.rapi.IRapiSession;
import org.eclipse.tm.rapi.OS;
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
            OS.FAF_NAME | OS.FAF_ATTRIBUTES | OS.FAF_LASTWRITE_TIME | 
            OS.FAF_SIZE_HIGH | OS.FAF_SIZE_LOW);
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
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    }
    return (IHostFile[]) results.toArray(new IHostFile[results.size()]);
  }

  private WinCEHostFile makeHostFile(String parentPath, String fileName, RapiFindData findData) {
    boolean isDirectory = (findData.fileAttributes & OS.FILE_ATTRIBUTE_DIRECTORY) != 0;
    boolean isRoot = "\\".equals(parentPath) && "\\".equals(fileName); //$NON-NLS-1$ //$NON-NLS-2$
    long lastModified = (findData.lastWriteTime / 10000) - OS.TIME_DIFF;
    long size = findData.fileSize;
    return new WinCEHostFile(parentPath, fileName, isDirectory, isRoot, lastModified, size);
  }

  private boolean isDirectory(IRapiSession session, String fullPath) {
    int attr = session.getFileAttributes(fullPath);
    if (attr == -1) {
      return false;
    }
    return (attr & OS.FILE_ATTRIBUTE_DIRECTORY) != 0;
  }
  
  private boolean exist(IRapiSession session, String fileName) {
    return session.getFileAttributes(fileName) != -1;
  }
  
  public boolean copy(String srcParent, String srcName, String tgtParent,
      String tgtName, IProgressMonitor monitor) throws SystemMessageException {
    String srcFullPath = concat(srcParent, srcName);
    String tgtFullPath = concat(tgtParent, tgtName);
    if (srcFullPath.equals(tgtFullPath)) {
      // prevent copying file/folder to itself
      return false;
    }
    IRapiSession session = sessionProvider.getSession();
    try {
      if (isDirectory(session, srcFullPath)) {
        if (tgtFullPath.startsWith(srcFullPath + "\\")) { //$NON-NLS-1$
          // prevent copying \a to \a\b\c
          return false;
        }
        if (exist(session, tgtFullPath)) {
          // we are doing overwrite,
          // if the target file or folder already exist - delete it
          delete(tgtParent, tgtName, monitor);
        }
        session.createDirectory(tgtFullPath);
        RapiFindData[] allFiles = session.findAllFiles(concat(srcFullPath,"*"), OS.FAF_NAME); //$NON-NLS-1$
        for (int i = 0 ; i < allFiles.length ; i++) {
          String fileName = allFiles[i].fileName;
          copy(srcFullPath, fileName, tgtFullPath, fileName, monitor);
        }
      } else {
        session.copyFile(srcFullPath, tgtFullPath);
      }
    } catch (RapiException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    }
    return true;
  }

  public boolean copyBatch(String[] srcParents, String[] srcNames,
      String tgtParent, IProgressMonitor monitor) throws SystemMessageException {
    boolean ok = true;
    for (int i = 0 ; i < srcParents.length ; i++) {
      ok = ok && copy(srcParents[i], srcNames[i], tgtParent, srcNames[i], monitor);
    }
    return ok;
  }

  public IHostFile createFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, fileName);
    IRapiSession session = sessionProvider.getSession();
    try {
      int handle = session.createFile(fullPath, OS.GENERIC_WRITE, OS.FILE_SHARE_READ, 
          OS.CREATE_ALWAYS, OS.FILE_ATTRIBUTE_NORMAL);
      session.closeHandle(handle);
      RapiFindData findData = new RapiFindData();
      handle = session.findFirstFile(fullPath, findData);
      session.findClose(handle);
      return makeHostFile(remoteParent, fileName, findData);
    } catch (RapiException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
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
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    }
  }

  public boolean delete(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, fileName);
    IRapiSession session = sessionProvider.getSession();
    try {
      if (isDirectory(session, fullPath)) {
        // recursive delete if it is a directory
        RapiFindData[] allFiles = session.findAllFiles(concat(fullPath, "*"), OS.FAF_NAME); //$NON-NLS-1$
        for (int i = 0 ; i < allFiles.length ; i++) {
          delete(fullPath, allFiles[i].fileName, monitor);
        }
        session.removeDirectory(fullPath);
      } else {
        // it is a file
        session.deleteFile(fullPath);
      }
    } catch (RapiException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());      
    }
    return true;
  }

  public boolean download(String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding,
      IProgressMonitor monitor) throws SystemMessageException {
    
    if (!localFile.exists()) {
      File localParentFile = localFile.getParentFile();
      if (!localParentFile.exists()) {
        localParentFile.mkdirs();
      }
    }
    String fullPath = concat(remoteParent, remoteFile);
    IRapiSession session = sessionProvider.getSession();
    int handle = OS.INVALID_HANDLE_VALUE;
    BufferedOutputStream bos = null;
    try {
      handle = session.createFile(fullPath, OS.GENERIC_READ, 
          OS.FILE_SHARE_READ, OS.OPEN_EXISTING, OS.FILE_ATTRIBUTE_NORMAL);
      bos = new BufferedOutputStream(new FileOutputStream(localFile));
      // don't increase the buffer size! the native functions sometimes fail with large buffers, 4K always work
      byte[] buffer = new byte[4 * 1024];
      while (true) {
        int bytesRead = session.readFile(handle, buffer);
        if (bytesRead == -1) {
          break;
        }
        bos.write(buffer, 0, bytesRead);
      }
      bos.flush();
    } catch (RapiException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    } catch (IOException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    } finally {
      if (handle != OS.INVALID_HANDLE_VALUE) {
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
    return true;
  }

  public IHostFile getFile(String remoteParent, String name, IProgressMonitor monitor) throws SystemMessageException {
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
    WinCEHostFile dummy = new WinCEHostFile(remoteParent, name, false, false, 0, 0);
    dummy.setExists(false);
    return dummy;
  }

  public IHostFile[] getRoots(IProgressMonitor monitor) throws SystemMessageException {
    return new WinCEHostFile[] { new WinCEHostFile("\\", "\\", true, true, 0, 0) }; //$NON-NLS-1$ //$NON-NLS-2$
  }

  public IHostFile getUserHome() {
    return new WinCEHostFile("\\", "My Documents", true, false, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public boolean isCaseSensitive() {
    return false;
  }

  public boolean move(String srcParent, String srcName, String tgtParent, String tgtName, 
      IProgressMonitor monitor) throws SystemMessageException {
    boolean ok = copy(srcParent, srcName, tgtParent, tgtName, monitor);
    ok = ok && delete(srcParent, srcName, monitor);
    return ok;
  }

  public boolean rename(String remoteParent, String oldName, String newName, 
      IProgressMonitor monitor) throws SystemMessageException {
    String oldFullPath = concat(remoteParent, oldName);
    String newFullPath = concat(remoteParent, newName);
    IRapiSession session = sessionProvider.getSession();
    try {
      session.moveFile(oldFullPath, newFullPath);
    } catch (RapiException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    }
    return true;
  }

  public boolean rename(String remoteParent, String oldName, String newName, IHostFile oldFile, 
      IProgressMonitor monitor) throws SystemMessageException {
    boolean retVal = rename(remoteParent, oldName, newName, monitor);
    String newFullPath = concat(remoteParent, newName);
    oldFile.renameTo(newFullPath);
    return retVal;
  }

  public boolean setLastModified(String parent, String name, long timestamp, IProgressMonitor monitor) throws SystemMessageException {
    return false;
  }

  public boolean setReadOnly(String parent, String name, boolean readOnly, IProgressMonitor monitor) throws SystemMessageException {
    return false;
  }

  public boolean upload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary,
      String hostEncoding, IProgressMonitor monitor) throws SystemMessageException {
    BufferedInputStream bis = new BufferedInputStream(stream);
    IRapiSession session = sessionProvider.getSession();
    String fullPath = concat(remoteParent, remoteFile);
    int handle = OS.INVALID_HANDLE_VALUE;
    try {
      handle = session.createFile(fullPath, OS.GENERIC_WRITE, 
          OS.FILE_SHARE_READ, OS.CREATE_ALWAYS, OS.FILE_ATTRIBUTE_NORMAL);
      // don't increase the buffer size! the native functions sometimes fail with large buffers, 4K always work
      byte[] buffer = new byte[4 * 1024];
      while (true) {
        int bytesRead = bis.read(buffer);
        if (bytesRead == -1) {
          break;
        }
        session.writeFile(handle, buffer, 0, bytesRead);
      }
    } catch (RapiException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    } catch (IOException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    } finally {
      if (handle != OS.INVALID_HANDLE_VALUE) {
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
    return true;
  }

  public boolean upload(File localFile, String remoteParent, String remoteFile, boolean isBinary, 
      String srcEncoding, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(localFile);
    } catch (FileNotFoundException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    }
    //FIXME what to do with srcEncoding ?
    return upload(fis, remoteParent, remoteFile, isBinary, hostEncoding, monitor);
  }

  public InputStream getInputStream(String remoteParent, String remoteFile,
      boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, remoteFile);
    IRapiSession session = sessionProvider.getSession();
    try {
      int handle = session.createFile(fullPath, OS.GENERIC_READ, 
          OS.FILE_SHARE_READ, OS.OPEN_EXISTING, OS.FILE_ATTRIBUTE_NORMAL);
      return new BufferedInputStream(new WinCEInputStream(session, handle));
    } catch (RapiException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    }
  }

  public OutputStream getOutputStream(String remoteParent, String remoteFile,
      int options, IProgressMonitor monitor) throws SystemMessageException {
    String fullPath = concat(remoteParent, remoteFile);
    IRapiSession session = sessionProvider.getSession();
    try {
      int cd = OS.CREATE_ALWAYS;
      if ((options & IFileService.APPEND) == 0) {
        cd = OS.CREATE_ALWAYS;
      } else {
        cd = OS.OPEN_EXISTING;
      }
      int handle = session.createFile(fullPath, OS.GENERIC_WRITE, 
          OS.FILE_SHARE_READ, cd, OS.FILE_ATTRIBUTE_NORMAL);
      return new BufferedOutputStream(new WinCEOutputStream(session, handle));
    } catch (RapiException e) {
      //FIXME error handling
      throw new RemoteFileException(e.getMessage());
    }
  }

  public String getDescription() {
    return Messages.WinCEFileService_0;
  }

  public String getName() {
    return Messages.WinCEFileService_1;
  }

  public void initService(IProgressMonitor monitor) {
  }

  public void uninitService(IProgressMonitor monitor) {
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
