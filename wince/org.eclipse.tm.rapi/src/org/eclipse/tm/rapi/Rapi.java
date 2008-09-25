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
 * This class provides access to some native Win32 APIs and constants.
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @author Radoslav Gerganov
 */
public final class Rapi {

  static {
    System.loadLibrary("jrapi"); //$NON-NLS-1$
  }

  public static final int NOERROR = 0;
  //TODO: add more error codes

  public static final int GENERIC_READ = 0x80000000;
  public static final int GENERIC_WRITE = 0x40000000;
  public static final int FILE_SHARE_READ = 0x00000001;
  public static final int FILE_SHARE_WRITE = 0x00000002;

  public static final int CREATE_NEW = 1;
  public static final int CREATE_ALWAYS = 2;
  public static final int OPEN_EXISTING = 3;
  public static final int OPEN_ALWAYS = 4;
  public static final int TRUNCATE_EXISTING = 5;

  public static final int FILE_ATTRIBUTE_ARCHIVE = 0x00000020;
  public static final int FILE_ATTRIBUTE_COMPRESSED = 0x00000800;
  public static final int FILE_ATTRIBUTE_DIRECTORY = 0x00000010;
  public static final int FILE_ATTRIBUTE_ENCRYPTED = 0x00004000;
  public static final int FILE_ATTRIBUTE_HIDDEN = 0x00000002;
  public static final int FILE_ATTRIBUTE_INROM = 0x00000040;
  public static final int FILE_ATTRIBUTE_NORMAL = 0x00000080;
  public static final int FILE_ATTRIBUTE_READONLY = 0x00000001;
  public static final int FILE_ATTRIBUTE_REPARSE_POINT = 0x00000400;
  public static final int FILE_ATTRIBUTE_ROMMODULE = 0x00002000;
  public static final int FILE_ATTRIBUTE_SPARSE_FILE = 0x00000200;
  public static final int FILE_ATTRIBUTE_SYSTEM = 0x00000004;
  public static final int FILE_ATTRIBUTE_TEMPORARY = 0x00000100;

  public static final int FILE_FLAG_WRITE_THROUGH = 0x80000000;
  public static final int FILE_FLAG_OVERLAPPED = 0x40000000;
  public static final int FILE_FLAG_RANDOM_ACCESS = 0x10000000;
  public static final int FILE_FLAG_SEQUENTIAL_SCAN = 0x08000000;

  public static final int FAF_ATTRIB_CHILDREN = 0x01000;
  public static final int FAF_ATTRIB_NO_HIDDEN = 0x02000;
  public static final int FAF_FOLDERS_ONLY = 0x04000;
  public static final int FAF_NO_HIDDEN_SYS_ROMMODULES = 0x08000;
  public static final int FAF_GETTARGET = 0x10000;

  public static final int FAF_ATTRIBUTES = 0x01;
  public static final int FAF_CREATION_TIME = 0x02;
  public static final int FAF_LASTACCESS_TIME = 0x04;
  public static final int FAF_LASTWRITE_TIME = 0x08;
  public static final int FAF_SIZE_HIGH = 0x10;
  public static final int FAF_SIZE_LOW = 0x20;
  public static final int FAF_OID = 0x40;
  public static final int FAF_NAME = 0x80;

  public static final int INVALID_HANDLE_VALUE = -1;

  public static final long TIME_DIFF = 11644473600000L;

  public static final int COINIT_MULTITHREADED     = 0x0;
  public static final int COINIT_APARTMENTTHREADED = 0x2;
  public static final int COINIT_DISABLE_OLE1DDE   = 0x4;
  public static final int COINIT_SPEED_OVER_MEMORY = 0x8;

  /**
   * Initializes the library, the clients must call this method before any
   * other. The meaning of the <code>init</code> parameter and the returned
   * value is platform dependent. On Win32 platforms this method is directly
   * mapped to <code>CoInitializeEx</code>.
   *
   * @param init on Win32 it is either {@link #COINIT_APARTMENTTHREADED} or
   *            {@link #COINIT_MULTITHREADED}
   * @return on Win32 this is the returned value from
   *         <code>CoInitializeEx</code>
   */
  public static final int initialize(int init) {
  	return CoInitializeEx(0, init);
  }

  /**
   * Uninitializes the library, the clients must call this method last to free any allocated resources.
   * This method is platform dependent, on Win32 it is directly mapped to <code>CoUninitialize</code>.
   */
  public static final void uninitialize() {
  	CoUninitialize();
  }

  /**
   * Initializes the COM library.
   */
  static final native int CoInitializeEx(int pvReserved, int dwCoInit);

  /**
   * Closes the COM library on the current thread.
   */
  static final native void CoUninitialize();

  /**
   * Sets the first element of the specified array with the address of a newly
   * instantiated <code>IRAPIDesktop</code> native interface.
   * 
   * @param pIRAPIDesktop an array with one element
   * @return {@link #NOERROR} if the function succeeds; otherwise an error
   *         code
   */
  final static native int CreateRapiDesktop(int[] pIRAPIDesktop);


  /**
   * Invokes the <code>Release</code> method of the <code>IUnkonwn</code> object
   * having the specified address.
   * @param addr the address of the <code>IUnknown</code> object
   */
  final static native void ReleaseIUnknown(int addr);
}
