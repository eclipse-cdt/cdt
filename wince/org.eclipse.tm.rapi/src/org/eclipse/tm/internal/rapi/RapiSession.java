/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.rapi;

import org.eclipse.tm.rapi.IRapiSession;
import org.eclipse.tm.rapi.Rapi;
import org.eclipse.tm.rapi.ProcessInformation;
import org.eclipse.tm.rapi.RapiException;
import org.eclipse.tm.rapi.RapiFindData;


/**
 * Implementation of <code>IRapiSession</code>.
 * 
 * @author Radoslav Gerganov
 */
public class RapiSession extends IRapiSession {
  
  private RapiFindData[] EMPTY_FIND_DATA_ARR = new RapiFindData[0]; 
  
  public RapiSession(int addr) {
    super(addr);
  }
  
  private int getError() {
    int err = CeRapiGetError(addr);
    if (err >= 0) {
      err = CeGetLastError(addr);
    }
    return err;
  }
  
  public void init() throws RapiException {
    int rc = CeRapiInit(addr);
    if (rc != Rapi.NOERROR) {
      throw new RapiException("CeRapiInit failed", rc); //$NON-NLS-1$
    }
  }
  
  public void uninit() throws RapiException {
    int rc = CeRapiUninit(addr);
    if (rc != Rapi.NOERROR) {
      throw new RapiException("CeRapiUninit failed", rc); //$NON-NLS-1$
    }    
  }
  
  public int createFile(String fileName, int desiredAccess, int shareMode,
      int creationDisposition, int flagsAndAttributes) throws RapiException {
    
    int handle = CeCreateFile(addr, fileName, desiredAccess, shareMode, 
        creationDisposition, flagsAndAttributes);
    if (handle == Rapi.INVALID_HANDLE_VALUE) {
      throw new RapiException("CeCreateFile failed", getError()); //$NON-NLS-1$
    } 
    return handle; 
  }
  
  public int readFile(int handle, byte[] b) throws RapiException {
    if (b.length == 0) {
      return 0;
    }
    int[] bytesRead = new int[1];
    boolean res = CeReadFile(addr, handle, b, b.length, bytesRead);
    if (!res) {
      throw new RapiException("CeReadFile failed", getError()); //$NON-NLS-1$
    }
    return bytesRead[0] > 0 ? bytesRead[0] : -1;
  }
  
  public int readFile(int handle, byte[] b, int off, int len)
      throws RapiException {
    if (off < 0 || len < 0 || off + len > b.length) {
      throw new IndexOutOfBoundsException("Incorrect offset/length"); //$NON-NLS-1$
    }
    if (len == 0) {
      return 0;
    }
    //TODO: add support for setting offset in the native code and remove this tmp array
    byte[] tmp = new byte[len];
    int[] bytesRead = new int[1];
    boolean res = CeReadFile(addr, handle, tmp, tmp.length, bytesRead);
    if (!res) {
      throw new RapiException("CeReadFile failed", getError()); //$NON-NLS-1$
    }
    System.arraycopy(tmp, 0, b, off, len);
    return bytesRead[0] > 0 ? bytesRead[0] : -1;
  }
  
  public void writeFile(int handle, byte[] b) throws RapiException {
    int[] bytesWritten = new int[1];
    boolean res = CeWriteFile(addr, handle, b, b.length, bytesWritten);
    if (!res) {
      throw new RapiException("CeWriteFile failed", getError()); //$NON-NLS-1$
    }
  }
  
  public void writeFile(int handle, byte[] b, int off, int len) 
      throws RapiException {

    if (off < 0 || len < 0 || off + len > b.length) {
      throw new IndexOutOfBoundsException("Incorrect offset/length"); //$NON-NLS-1$
    }
    //TODO: add support for setting offset in the native code and remove this tmp array
    byte[] tmp = new byte[len];
    System.arraycopy(b, off, tmp, 0, len);
    int[] bytesWritten = new int[1];
    boolean res = CeWriteFile(addr, handle, tmp, tmp.length, bytesWritten);
    if (!res) {
      throw new RapiException("CeWriteFile failed", getError()); //$NON-NLS-1$
    }
  }
  
  public void closeHandle(int handle) throws RapiException {
    boolean res = CeCloseHandle(addr, handle);
    if (!res) {
      throw new RapiException("CeCloseHandle failed", getError()); //$NON-NLS-1$
    }
  }
  
  public void copyFile(String existingFile, String newFile) throws RapiException {
    //overwrite by default
    boolean res = CeCopyFile(addr, existingFile, newFile, false);
    if (!res) {
      throw new RapiException("CeCopyFile failed", getError()); //$NON-NLS-1$
    }
  }

  public void deleteFile(String fileName) throws RapiException {
    boolean res = CeDeleteFile(addr, fileName);
    if (!res) {
      throw new RapiException("CeDeleteFile failed", getError()); //$NON-NLS-1$
    }
  }
  
  public void moveFile(String existingFileName, String newFileName) throws RapiException {
    boolean res = CeMoveFile(addr, existingFileName, newFileName);
    if (!res) {
      throw new RapiException("CeMoveFile failed", getError()); //$NON-NLS-1$
    }
  }
  
  public void createDirectory(String pathName) throws RapiException {
    boolean res = CeCreateDirectory(addr, pathName);
    if (!res) {
      throw new RapiException("CeCreateDirectory failed", getError()); //$NON-NLS-1$
    }
  }
  
  public void removeDirectory(String pathName) throws RapiException {
    boolean res = CeRemoveDirectory(addr, pathName);
    if (!res) {
      throw new RapiException("CeRemoveDirectory failed", getError()); //$NON-NLS-1$
    }
  }

  public int findFirstFile(String fileName, RapiFindData findData) throws RapiException {
    int handle = CeFindFirstFile(addr, fileName, findData);
    if (handle == Rapi.INVALID_HANDLE_VALUE) {
      throw new RapiException("CeFindFirstFile failed", getError()); //$NON-NLS-1$
    }
    return handle;
  }
  
  public RapiFindData findNextFile(int handle) {
    RapiFindData findData = new RapiFindData();
    boolean res = CeFindNextFile(addr, handle, findData);
    // just return null if findNext fail
    return res ? findData : null;
  }
  
  public void findClose(int handle) throws RapiException {
    boolean res = CeFindClose(addr, handle);
    if (!res) {
      throw new RapiException("CeFindClose failed", getError()); //$NON-NLS-1$
    }
  }
  
  public RapiFindData[] findAllFiles(String path, int flags) throws RapiException {
    int[] foundCount = new int[1];
    int[] dataArr = new int[1];
    boolean res = CeFindAllFiles(addr, path, flags, foundCount, dataArr);
    int count = foundCount[0];
    if (!res || count == 0) {
      // nothing found
      return EMPTY_FIND_DATA_ARR;
    }
    RapiFindData[] findDataArr = new RapiFindData[count];
    for (int i = 0 ; i < count ; i++) {
      findDataArr[i] = new RapiFindData();
    }
    int hRes = CeFindAllFilesEx(addr, count, dataArr[0], findDataArr);
    if (hRes != Rapi.NOERROR) {
      throw new RapiException("CeFindAllFilesEx failed", hRes); //$NON-NLS-1$
    }
    return findDataArr;
  }
  
  public int getFileAttributes(String fileName) {
    int attributes = CeGetFileAttributes(addr, fileName);
//    if (attributes == 0xFFFFFFFF) {
//      throw new RapiException("CeGetFileAttributes failed", getError());
//    }
    return attributes;
  }
  
  public long getFileSize(int handle) {
    int[] sizeHigh = new int[] {1};
    int sizeLow = CeGetFileSize(addr, handle, sizeHigh);
    return ( ((long)sizeHigh[0] << 32) | (sizeLow & 0xFFFFFFFF));
  }
  
  public long getFileCreationTime(int handle) throws RapiException {
    long[] crTime = new long[1];
    long[] laTime = new long[1];
    long[] lwTime = new long[1];
    boolean res = CeGetFileTime(addr, handle, crTime, laTime, lwTime);
    if (!res) {
      throw new RapiException("CeGetFileTime failed", getError()); //$NON-NLS-1$
    }
    return (crTime[0] / 10000) - Rapi.TIME_DIFF;
  }

  public long getFileLastAccessTime(int handle) throws RapiException {
    long[] crTime = new long[1];
    long[] laTime = new long[1];
    long[] lwTime = new long[1];
    boolean res = CeGetFileTime(addr, handle, crTime, laTime, lwTime);
    if (!res) {
      throw new RapiException("CeGetFileTime failed", getError()); //$NON-NLS-1$
    }
    return (laTime[0] / 10000) - Rapi.TIME_DIFF;
  }

  public long getFileLastWriteTime(int handle) throws RapiException {
    long[] crTime = new long[1];
    long[] laTime = new long[1];
    long[] lwTime = new long[1];
    boolean res = CeGetFileTime(addr, handle, crTime, laTime, lwTime);
    if (!res) {
      throw new RapiException("CeGetFileTime failed", getError()); //$NON-NLS-1$
    }
    return (lwTime[0] / 10000) - Rapi.TIME_DIFF;
  }
  
  public void setFileAttributes(String fileName, int fileAttributes) throws RapiException {
    boolean res = CeSetFileAttributes(addr, fileName, fileAttributes);
    if (!res) {
      throw new RapiException("CeSetFileAttributes failed", getError()); //$NON-NLS-1$
    }
  }

  public void setFileLastWriteTime(int handle, long lastWriteTime) throws RapiException {
    if (lastWriteTime < 0) {
      throw new IllegalArgumentException("Time cannot be negative"); //$NON-NLS-1$
    }
    long[] lwTime = new long[] {(lastWriteTime + Rapi.TIME_DIFF) * 10000};
    boolean res = CeSetFileTime(addr, handle, null, null, lwTime);
    if (!res) {
      throw new RapiException("CeSetFileTime failed", getError()); //$NON-NLS-1$
    }
  }
  
  public ProcessInformation createProcess(String appName, String commandLine, int creationFlags) throws RapiException {
    ProcessInformation pi = new ProcessInformation();
    if (!CeCreateProcess(addr, appName, commandLine, creationFlags, pi)) {
      throw new RapiException("CeCreateProcess failed", getError()); //$NON-NLS-1$
    }
    return pi;
  }

  public String toString() {
    return "[RapiSession] addr: " + Integer.toHexString(addr); //$NON-NLS-1$
  }  
  
  private final native int CeRapiInit(int addr);

  private final native int CeRapiUninit(int addr);

  private final native int CeRapiGetError(int addr);
  
  private final native int CeGetLastError(int addr);

  private final native int CeCreateFile(int addr, String lpFileName, 
      int dwDesiredAccess, int dwShareMode, int dwCreationDisposition,
      int dwFlagsAndAttributes);

  private final native boolean CeReadFile(int addr, int hFile, byte[] lpBuffer,
      int nNumberOfBytesToRead, int[] lpNumberOfBytesRead);

  private final native boolean CeWriteFile(int addr, int hFile, byte[] lpBuffer,
      int nNumberOfBytesToWrite, int[] lpNumberOfBytesWritten);

  private final native boolean CeCloseHandle(int addr, int hObject);

  private final native boolean CeCopyFile(int addr, String lpExistingFileName,
      String lpNewFileName, boolean bFailIfExists);
  
  private final native boolean CeDeleteFile(int addr, String lpFileName);
  
  private final native boolean CeMoveFile(int addr, String lpExistingFileName, 
      String lpNewFileName);

  private final native boolean CeCreateDirectory(int addr, String lpPathName);

  private final native boolean CeRemoveDirectory(int addr, String lpPathName);

  private final native int CeFindFirstFile(int addr, String lpFileName, 
      RapiFindData lpFindFileData);

  private final native boolean CeFindNextFile(int addr, int hFindFile, 
      RapiFindData lpFindFileData);

  private final native boolean CeFindClose(int addr, int hFindFile);
  
  private final native boolean CeFindAllFiles(int addr, String szPath, 
      int dwFlags, int[] lpdwFoundCount, int[] ppFindDataArray);

  private final native int CeFindAllFilesEx(int addr, int foundCount, 
      int dataArr, RapiFindData[] findDataArr);
  
  private final native int CeGetFileAttributes(int addr, String lpFileName);
  
  private final native int CeGetFileSize(int addr, int hFile, int[] lpFileSizeHigh);
  
  private final native boolean CeGetFileTime(int addr, int hFile, 
      long[] lpCreationTime, long[] lpLastAccessTime, long[] lpLastWriteTime);
  
  private final native boolean CeSetFileAttributes(int addr, String lpFileName, int dwFileAttributes);
  
  private final native boolean CeSetFileTime(int addr, int hFile, long[] lpCreationTime, 
      long[] lpLastAccessTime, long[] lpLastWriteTime);
  
  private final native boolean CeCreateProcess(int addr, String lpApplicationName,
      String lpCommandLine, int dwCreationFlags, ProcessInformation lpProcessInformation);
}
