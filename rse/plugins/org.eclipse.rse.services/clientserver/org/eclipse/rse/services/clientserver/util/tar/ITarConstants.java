/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver.util.tar;

/**
 * This interface contains all constants needed for the tar package.s
 */
public interface ITarConstants {

	/**
	 * The block size, 512.
	 */
	public static final int BLOCK_SIZE = 512;
	
	/**
	 * The separator character in tar entry paths, '/'.
	 */
	public static final char SEPARATOR_CHAR = '/';
	
	/**
	 * The length of the name field, 100.
	 */
	public static final int NAME_LENGTH = 100;
	
	/**
	 * The length of the mode field, 8.
	 */
	public static final int MODE_LENGTH = 8;
	
	/**
	 * The length of the uid field, 8.
	 */
	public static final int UID_LENGTH = 8;
	
	/**
	 * The length of the gid field, 8.
	 */
	public static final int GID_LENGTH = 8;
	
	/**
	 * The length of the size field, 12.
	 */
	public static final int SIZE_LENGTH = 12;
	
	/**
	 * The length of the mtime field, 12.
	 */
	public static final int MTIME_LENGTH = 12;
	
	/**
	 * The length of the chksum field, 8.
	 */
	public static final int CHKSUM_LENGTH = 8;
	
	/**
	 * The length of the typeflag field, 1.
	 */
	public static final int TYPEFLAG_LENGTH = 1;
	
	/**
	 * The length of the linkname field, 100.
	 */
	public static final int LINKNAME_LENGTH = 100;
	
	/**
	 * The length of the magic field, 6.
	 */
	public static final int MAGIC_LENGTH = 6;
	
	/**
	 * The length of the version field, 2.
	 */
	public static final int VERSION_LENGTH = 2;
	
	/**
	 * The length of the uname field, 32.
	 */
	public static final int UNAME_LENGTH = 32;
	
	/**
	 * The length of the gname field, 32.
	 */
	public static final int GNAME_LENGTH = 32;
	
	/**
	 * The length of the devmajor field, 8.
	 */
	public static final int DEVMAJOR_LENGTH = 8;
	
	/**
	 * The length of the devminor field, 8.
	 */
	public static final int DEVMINOR_LENGTH = 8;
	
	/**
	 * The length of the prefix field, 155.
	 */
	public static final int PREFIX_LENGTH = 155;
	
	/**
	 * The total length of the header.
	 */
	public static final int HEADER_LENGTH = NAME_LENGTH + MODE_LENGTH + UID_LENGTH + GID_LENGTH + SIZE_LENGTH +
											MTIME_LENGTH + CHKSUM_LENGTH + TYPEFLAG_LENGTH + LINKNAME_LENGTH +
											MAGIC_LENGTH + VERSION_LENGTH + UNAME_LENGTH + GNAME_LENGTH +
											DEVMAJOR_LENGTH + DEVMINOR_LENGTH + PREFIX_LENGTH;
	
	// type flag constants
	public static final char TF_OLDNORMAL = '\0';
	public static final char TF_NORMAL = '0';
	public static final char TF_LINK = '1';
	public static final char TF_SYMLINK = '2';
	public static final char TF_CHAR = '3';
	public static final char TF_BLOCK = '4';
	public static final char TF_DIR = '5';
	public static final char TF_FIFO = '6';
	public static final char TF_CONTIGUOUS = '7';
}