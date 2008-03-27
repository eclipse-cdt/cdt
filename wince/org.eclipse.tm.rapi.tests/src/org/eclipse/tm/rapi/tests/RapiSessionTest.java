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
package org.eclipse.tm.rapi.tests;

import org.eclipse.tm.rapi.IRapiDesktop;
import org.eclipse.tm.rapi.OS;
import org.eclipse.tm.rapi.RapiException;
import org.eclipse.tm.rapi.RapiFindData;

public class RapiSessionTest extends RapiTestCase {

  private static final int TEMP_FILES_COUNT = 10;
  private static final int TEST_FILE_SIZE = 8 * 1024;
  private static final String TEST_FILE_NAME = "\\foo.bin";
  private static final int CHUNK_SIZE = 1024;
  
  private static final String TEST_DIR_NAME = "bar";
  
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Creates and initialize RAPI2 session
   */
  void createInitSession() throws RapiException {
    desktop = IRapiDesktop.getInstance();
    enumDevices = desktop.enumDevices();
    device = enumDevices.next();
    session = device.createSession();
    session.init();
  }
  
  /**
   * Returns true if the first <code>len</code> bytes of the specified arrays
   * are equal.
   */
  boolean arraysEqual(byte[] arr1, byte[] arr2, int len) {
    for (int i = 0 ; i < len ; i++) {
      if (arr1[i] != arr2[i]) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Returns true if the specified <code>path</code> exists and it is a directory.
   */
  boolean isDirectory(String path) {
      int attr = session.getFileAttributes(path);
      if (attr == -1) {
        return false;
      }
      return (attr & OS.FILE_ATTRIBUTE_DIRECTORY) != 0;
  }

  /**
   * Returns true if the specified <code>path</code> exists and it is a file.
   */
  boolean isFile(String path) {
    int attr = session.getFileAttributes(path);
    if (attr == -1) {
      return false;
    }
    return (attr & OS.FILE_ATTRIBUTE_DIRECTORY) == 0;  
  }

  /**
   * Tests reading and writing files on the device.
   */
  public void testReadWriteFiles() throws RapiException {
    createInitSession();
    
    // generate random file content
    byte[] content = new byte[TEST_FILE_SIZE];
    for (int i = 0 ; i < content.length ; i++) {
      content[i] = (byte)(Math.random() * 131);
    }
    
    // write the test file at once
    int handle = session.createFile(TEST_FILE_NAME, OS.GENERIC_WRITE, 
        OS.FILE_SHARE_READ, OS.CREATE_ALWAYS, OS.FILE_ATTRIBUTE_NORMAL);
    session.writeFile(handle, content);
    session.closeHandle(handle);

    // try to read the whole file
    handle = session.createFile(TEST_FILE_NAME, OS.GENERIC_READ, 
        OS.FILE_SHARE_READ, OS.OPEN_EXISTING, OS.FILE_ATTRIBUTE_NORMAL);
    byte[] contentRead = new byte[TEST_FILE_SIZE];
    int br = session.readFile(handle, contentRead);
    session.closeHandle(handle);
    assertTrue("Different file content", arraysEqual(content, contentRead, br));

    // write the test file by chunks
    handle = session.createFile(TEST_FILE_NAME, OS.GENERIC_WRITE, 
        OS.FILE_SHARE_READ, OS.CREATE_ALWAYS, OS.FILE_ATTRIBUTE_NORMAL);
    int off = 0;
    for (int i = 0 ; i < TEST_FILE_SIZE / CHUNK_SIZE ; i++) {
      session.writeFile(handle, content, off, CHUNK_SIZE);
      off += CHUNK_SIZE;
    }
    session.closeHandle(handle);
    
    // read the test file by chunks
    handle = session.createFile(TEST_FILE_NAME, OS.GENERIC_READ, 
        OS.FILE_SHARE_READ, OS.OPEN_EXISTING, OS.FILE_ATTRIBUTE_NORMAL);
    byte[] contentRead2 = new byte[TEST_FILE_SIZE];
    off = 0;
    int bytesToRead = TEST_FILE_SIZE;
    while (bytesToRead > 0) {
      int len = CHUNK_SIZE < bytesToRead ? CHUNK_SIZE : bytesToRead;
      br = session.readFile(handle, contentRead2, off, len);
      off += br;
      bytesToRead -= br;
    }
    session.closeHandle(handle);
    assertTrue("Different file content", arraysEqual(content, contentRead2, TEST_FILE_SIZE));
    
    // delete the test file
    session.deleteFile(TEST_FILE_NAME);
  }
  
  /**
   * Tests creating and removing directories on the device.
   */
  public void testCreateRemoveDirectories() throws RapiException {
    createInitSession();
    try {
      session.removeDirectory(TEST_DIR_NAME);
    } catch (RapiException re) {
      // ignore
    }
    session.createDirectory(TEST_DIR_NAME);
    assertTrue("Failed to create directory", isDirectory(TEST_DIR_NAME));
    session.removeDirectory(TEST_DIR_NAME);
    assertFalse("Failed to remove directory", isDirectory(TEST_DIR_NAME));
  }
  
  /**
   * Tests copying, moving and deleting files on the device.
   */
  public void testCopyMoveDeleteFiles() throws RapiException {
    createInitSession();
    
    // create test file
    int handle = session.createFile(TEST_FILE_NAME, OS.GENERIC_WRITE, 
        OS.FILE_SHARE_READ, OS.CREATE_ALWAYS, OS.FILE_ATTRIBUTE_NORMAL);
    session.writeFile(handle, "spam".getBytes());
    session.closeHandle(handle);
    
    // make a copy
    String copy = TEST_FILE_NAME + "1";
    session.copyFile(TEST_FILE_NAME, copy);
    assertTrue("Failed to copy file", isFile(copy));
    
    // delete the test file
    session.deleteFile(TEST_FILE_NAME);
    assertFalse("Failed to delete file", isFile(TEST_FILE_NAME));
    
    // rename the copy
    session.moveFile(copy, TEST_FILE_NAME);
    assertTrue("Failed to move file", isFile(TEST_FILE_NAME));
    assertFalse("Failed to move file", isFile(copy));
    
    // delete test file
    session.deleteFile(TEST_FILE_NAME);
  }
  
  /**
   * Utility method for creating a bunch of files.
   */
  void createTempFiles() throws RapiException {
    for (int i = 0 ; i < TEMP_FILES_COUNT ; i++) {
      int handle = session.createFile(TEST_FILE_NAME + i, OS.GENERIC_WRITE, 
          OS.FILE_SHARE_READ, OS.CREATE_ALWAYS, OS.FILE_ATTRIBUTE_NORMAL);
      session.writeFile(handle, "spam".getBytes());
      session.closeHandle(handle);
    }
  }
  
  /**
   * Tests finding files using {@link IRapiSession#findFirstFile(String, RapiFindData)}
   */
  public void testFindFiles() throws RapiException {
    createInitSession();
    createTempFiles();
    int filesFound = 0;
    RapiFindData findData = new RapiFindData();
    int fh = session.findFirstFile(TEST_FILE_NAME + "?", findData);
    while (findData != null) {
      filesFound++;
      findData = session.findNextFile(fh);
    }
    session.findClose(fh);
    assertTrue("Found " + filesFound + " , expected " + TEMP_FILES_COUNT, 
        filesFound == TEMP_FILES_COUNT);
    // clean up
    for (int i = 0 ; i < TEMP_FILES_COUNT ; i++) {
      session.deleteFile(TEST_FILE_NAME + i);
    }
  }
  
  /**
   * Tests finding files using {@link IRapiSession#findAllFiles(String, int)}
   */
  public void testFindAllFiles() throws RapiException {
    createInitSession();
    createTempFiles();
    RapiFindData[] faf = session.findAllFiles(TEST_FILE_NAME + "?", OS.FAF_NAME);
    int filesFound = faf.length;
    assertTrue("Found " + filesFound + " , expected " + TEMP_FILES_COUNT, 
        filesFound == TEMP_FILES_COUNT);
    // clean up
    for (int i = 0 ; i < TEMP_FILES_COUNT ; i++) {
      session.deleteFile(TEST_FILE_NAME + i);
    }
  }

  /**
   * Tests getting file attributes, size, etc.
   */
  public void testStatFiles() throws RapiException {
    createInitSession();
    // create test file
    int handle = session.createFile(TEST_FILE_NAME, OS.GENERIC_WRITE, 
        OS.FILE_SHARE_READ, OS.CREATE_ALWAYS, OS.FILE_ATTRIBUTE_NORMAL);
    session.writeFile(handle, "spam".getBytes());
    assertTrue("Wrong file size", 4 == session.getFileSize(handle));
    //TODO: add some checks for file times (creation, last modified, etc)
    session.closeHandle(handle);
    int attr = session.getFileAttributes(TEST_FILE_NAME);
    assertTrue("Wrong file attributes", (attr & OS.FILE_ATTRIBUTE_ARCHIVE) != 0);
    //clean up
    session.deleteFile(TEST_FILE_NAME);
  }
  
  protected void tearDown() throws Exception {
    if (session != null) {
      session.uninit();
    }
    super.tearDown();
  }

}
