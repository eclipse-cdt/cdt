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
package org.eclipse.tm.rapi;

/**
 * This class is used to perform Remote API 2 operations on a connected
 * WinCE-based remote device.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @author Radoslav Gerganov
 */
public abstract class IRapiSession extends IUnknown {

  public IRapiSession(int addr) {
    super(addr);
  }

  /**
   * Initializes (synchronously) the communication layers between the desktop 
   * and the target remote device. This method must be called before calling any
   * of the other <code>IRapiSession</code> methods. Use {@link IRapiSession#uninit()}
   * to uninitialize the session.
   * @throws RapiException if an error occurs.
   */
  public abstract void init() throws RapiException;

  /**
   * Uninitializes the session. This method should be called last.
   * @throws RapiException if an error occurs.
   */
  public abstract void uninit() throws RapiException;

  /**
   * Creates, opens, or truncates a file on the remote device.
   * @param fileName file name on the remote device
   * @param desiredAccess specifies the type of access to the file
   * @param shareMode specifies how the file can be shared
   * @param creationDisposition specifies which action to take on 
   * files that exist, and which action to take when files do not exist
   * @param flagsAndAttributes specifies the file attributes and flags 
   * for the file
   * @return integer representing a valid handle that can be used to access the file
   * @throws RapiException if an error occurs.
   */
  public abstract int createFile(String fileName, int desiredAccess,
      int shareMode, int creationDisposition, 
      int flagsAndAttributes) throws RapiException;

  /**
   * Reads up to <code>b.length</code> bytes of data from a remote file
   * into an array of bytes.
   * @param handle handle to the file to be read
   * @param b the buffer into which the data is read
   * @return the total number of bytes read into the buffer, or
   * <code>-1</code> if there is no more data because the end of
   * the file has been reached
   * @throws RapiException if an error occurs.
   */
  public abstract int readFile(int handle, byte[] b) throws RapiException;
  
  /**
   * Reads up to <code>len</code> bytes of data from a remote file
   * into an array of bytes.
   * @param handle handle to the file to be read
   * @param b the buffer into which the data is read
   * @param off the start offset of the data
   * @param len the maximum number of bytes read
   * @return the total number of bytes read into the buffer, or
   * <code>-1</code> if there is no more data because the end of
   * the file has been reached
   * @throws RapiException if an error occurs.
   */
  public abstract int readFile(int handle, byte[] b, int off, int len)
      throws RapiException;
  
  /**
   * Writes <code>b.length</code> bytes from the specified byte array 
   * to a remote file. 
   * @param handle handle to the file to which bytes will be written
   * @param b the data
   * @throws RapiException if an error occurs.
   */
  public abstract void writeFile(int handle, byte[] b) throws RapiException;
  
  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to a remote file.    
   * @param handle handle to the file to which bytes will be written
   * @param b the data
   * @param off the start offset in the data
   * @param len the number of bytes to write
   * @throws RapiException if an error occurs.
   */
  public abstract void writeFile(int handle, byte[] b, int off, int len) 
      throws RapiException;
  
  /**
   * Closes an open file handle.
   * @param handle handle to an open file
   * @throws RapiException if an error occurs.
   */
  public abstract void closeHandle(int handle) throws RapiException;

  /**
   * Copies an existing file on the remote device to a new file on the
   * remote device.
   * @param existingFile the file name of the existing file
   * @param newFile the file name of the new file
   * @throws RapiException if an error occurs.
   */
  public abstract void copyFile(String existingFile, String newFile) 
      throws RapiException;

  /**
   * Deletes the specified file on the remote device.
   * @param fileName the file to be deleted
   * @throws RapiException if an error occurs.
   */
  public abstract void deleteFile(String fileName) throws RapiException;

  /**
   * Renames existing file or directory on the remote device.
   * @param existingFileName the existing name
   * @param newFileName the new name
   * @throws RapiException if an error occurs.
   */
  public abstract void moveFile(String existingFileName, 
      String newFileName) throws RapiException;

  /**
   * Creates a new directory on the remote device.
   * @param pathName the path of the directory to be created
   * @throws RapiException if an error occurs.
   */
  public abstract void createDirectory(String pathName) throws RapiException;

  /**
   * Deletes an existing empty directory on the remote device.
   * @param pathName the path of the directory to be deleted
   * @throws RapiException if an error occurs.
   */
  public abstract void removeDirectory(String pathName) throws RapiException;

  /**
   * Searches for a file or sub-directory in a directory on the remote device.
   * Use {@link IRapiSession#findNextFile(int)} to get the next found file.  
   * Finally, call {@link IRapiSession#findClose(int)} to close the returned search handle.
   * @param fileName string that specifies a valid directory or path and file name. 
   * This string can contain wildcard characters (* and ?)
   * @param findData [out] this object receives information about the found file 
   * or sub-directory
   * @return integer representing valid search handle
   * @throws RapiException if an error occurs.
   */
  public abstract int findFirstFile(String fileName, 
      RapiFindData findData) throws RapiException;

  /**
   * Retrieves the next file in an enumeration context.
   * @param handle search handle obtained with a call to 
   * {@link IRapiSession#findFirstFile(String, RapiFindData)}
   * @return <code>RapiFindData</code> object containing information about the
   * next file/sub-directory or <code>null</code> if no matching files can be found
   */
  public abstract RapiFindData findNextFile(int handle);

  /**
   * Closes the specified search handle on the remote device.
   * @param handle the search handle to close
   * @throws RapiException if an error occurs.
   */
  public abstract void findClose(int handle) throws RapiException;

  /**
   * Retrieves information about all files and directories in the given directory on
   * the remote device.
   * @param path string containing the path in which to search for files
   * @param flags combination of filter and retrieval flags
   * @return an array of <code>RapiFindData</code> objects containing the information
   * about the found items
   * @throws RapiException if an error occurs.
   */
  public abstract RapiFindData[] findAllFiles(String path, 
      int flags) throws RapiException;

  /**
   * Returns attributes for the specified file or directory on the remote device.
   * @param fileName string that specifies the name of a file or directory
   * @return attributes for the specified file or <code>-1</code>
   * if an error has occurred 
   */
  public abstract int getFileAttributes(String fileName);

  /**
   * Returns the size, in bytes, of the specified file on the remote device.
   * @param handle open handle of the file whose size is being returned
   * @return the file size in bytes
   */
  public abstract long getFileSize(int handle);

  /**
   * Returns the time when the file was created.
   * <p>
   * The time is represented as the number of Universal Time (UT) 
   * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
   * </p> 
   * @param handle handle to the file for which to get creation time
   * @return the creation time for this file
   * @throws RapiException if an error occurs.
   */
  public abstract long getFileCreationTime(int handle) throws RapiException;

  /**
   * Returns the time when the file was last accessed.
   * <p>
   * The time is represented as the number of Universal Time (UT) 
   * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
   * </p> 
   * @param handle handle to the file for which to get last access time
   * @return the last access time for this file
   * @throws RapiException if an error occurs.
   */
  public abstract long getFileLastAccessTime(int handle) throws RapiException;

  /**
   * Returns the time when the file was last written to.
   * <p>
   * The time is represented as the number of Universal Time (UT) 
   * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
   * </p> 
   * @param handle handle to the file for which to get last write time
   * @return the last write time for this file
   * @throws RapiException if an error occurs.
   */
  public abstract long getFileLastWriteTime(int handle) throws RapiException;

  /**
   * Sets the attributes of the specified file on the remote device.
   * @param fileName the target file
   * @param fileAttributes the new attributes; this parameter is combination of the
   * following values: {@link Rapi#FILE_ATTRIBUTE_ARCHIVE}, {@link Rapi#FILE_ATTRIBUTE_HIDDEN},
   * {@link Rapi#FILE_ATTRIBUTE_NORMAL}, {@link Rapi#FILE_ATTRIBUTE_READONLY},
   * {@link Rapi#FILE_ATTRIBUTE_SYSTEM}, {@link Rapi#FILE_ATTRIBUTE_TEMPORARY}
   * @throws RapiException if an error occurs.
   */
  public abstract void setFileAttributes(String fileName, int fileAttributes) throws RapiException;
  
  /**
   * Sets the last write time of the specified file.
   * <p>
   * The time is represented as the number of Universal Time (UT) 
   * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
   * </p>
   * The specified time will be truncated to fit the supported precision.
   * @param handle handle to the target file
   * @param lastWriteTime the new last write time for this file
   * @throws IllegalArgumentException if the specified time is negative
   * @throws RapiException if an error occurs.
   */
  public abstract void setFileLastWriteTime(int handle, long lastWriteTime) throws RapiException;
  
  /**
   * Creates new process on the remote device.
   * @param appName module to execute
   * @param commandLine command line arguments
   * @param creationFlags additional flags controlling the creation
   * @return <code>ProcessInformaion</code> containing information about the new process
   * @throws RapiException if an error occurs.
   */
  public abstract ProcessInformation createProcess(String appName, String commandLine, 
      int creationFlags) throws RapiException;
}
