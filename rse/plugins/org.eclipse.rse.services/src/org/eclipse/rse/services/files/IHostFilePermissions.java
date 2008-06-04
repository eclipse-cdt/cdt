/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 ********************************************************************************/
package org.eclipse.rse.services.files;


/**
 * Interface for file permission and ownership support in RSE
 * IFileService. An implementation of an IHostFilePermissions
 * object is expected to be a data storage for permission and
 * ownership information.
 * @since 3.0
 */
public interface IHostFilePermissions {

	/**
	 * Permission constant indicating that
	 * the user can read this file
	 */
	public static final int PERM_USER_READ = 1 << 8;

	/**
	 * Permission constant indicating that
	 * the user can write to this file
	 */
	public static final int PERM_USER_WRITE = 1 << 7;

	/**
	 * Permission constant indicating that
	 * the user can execute this file
	 */
	public static final int PERM_USER_EXECUTE = 1 << 6;

	/**
	 * Permission constant indicating that
	 * the group can read this file
	 */
	public static final int PERM_GROUP_READ = 1 << 5;

	/**
	 * Permission constant indicating that
	 * the group can write to this file
	 *
	 */
	public static final int PERM_GROUP_WRITE = 1 << 4;

	/**
	 * Permission constant indicating that
	 * the group can execute this file
	 *
	 */
	public static final int PERM_GROUP_EXECUTE = 1 << 3;

	/**
	 * Permission constant indicating that
	 * other users can read this file
	 *
	 */
	public static final int PERM_OTHER_READ = 1 << 2;

	/**
	 * Permission constant indicating that
	 * other users can write to this file
	 *
	 */
	public static final int PERM_OTHER_WRITE = 1 << 1;

	/**
	 * Permission constant indicating that
	 * other users can execute to this file
	 *
	 */
	public static final int PERM_OTHER_EXECUTE = 1 << 0;


	// support masks
	public static final int PERM_ANY_READ = PERM_USER_READ | PERM_GROUP_READ | PERM_OTHER_READ;
	public static final int PERM_ANY_WRITE = PERM_USER_WRITE | PERM_GROUP_WRITE | PERM_OTHER_WRITE;
	public static final int PERM_ANY_EXECUTE = PERM_USER_EXECUTE | PERM_GROUP_EXECUTE | PERM_OTHER_EXECUTE;

	/**
	 * Set or reset all the permission bits from the given bitmask.
	 *
	 * @param bitmask the permission(s) bits to modify
	 * @param value whether to turn on off of the permission(s)
	 *
	 * Example: setPermission(PERM_USER_WRITE | PERM_GROUP_WRITE, true);
	 */
	public void setPermission(int bitmask, boolean value);

	/**
	 * Test if any of the permission bits from the bitmask are set.
	 *
	 * @param bitmask the permission(s) to check for
	 * @return true if one of the permission bits is set
	 *
	 * Example: getPermission(PERM_USER_WRITE | PERM_GROUP_WRITE)
	 */
	public boolean getPermission(int bitmask);

	/**
	 * Get the set of permission bits.
	 *
	 * @return set of permission bits
	 */
	public int getPermissionBits();

	/**
	 * Set the permission bits
	 * @param bits the set of permission bits
	 */
	public void setPermissionBits(int bits);


	/**
	 * return permissions in rwxrwxrwx form
	 */
	public String toAlphaString();

	/**
	 * returns the user owner of the file
	 * @return the user owner
	 */
	public String getUserOwner();

	/**
	 * returns the group owner of the file
	 * @return the group owner
	 */
	public String getGroupOwner();

	/**
	 * Sets the user owner attribute
	 * @param user the user owner attribute
	 */
	public void setUserOwner(String user);

	/**
	 * Sets the group owner attribute
	 * @param group the group owner attribute
	 */
	public void setGroupOwner(String group);

	/**
	 * Creates and returns a copy of this object, as specified
	 * by the {@link Object#clone()} contract.
	 *
	 * Each implementer of the IHostFilePermission API must properly implement
	 * this method and implement the {@link Cloneable} interface.
	 * @return the cloned object
	 * @throws CloneNotSupportedException if an instance cannot be cloned for
	 *    any reason, e.g. because its state contains complex objects that
	 *    cannot be cloned.
	 * @see Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException;
}
