package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * An IArchive represents a group of files combined  into  a
 * single file(the Archive), for example libxx.a.
 */
public interface IArchive extends ICFile {
	/**
	 * Return the binaries contain in the archive.
 	 * It does not actually extract the files.
	 */
	public IBinary[] getBinaries();
}
