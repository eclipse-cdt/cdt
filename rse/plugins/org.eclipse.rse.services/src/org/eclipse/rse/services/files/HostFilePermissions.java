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
 * Default implementation of "Universal" UNIX kind of IHostFilePermissions.
 * @since 3.0
 */
public class HostFilePermissions implements
		IHostFilePermissions, Cloneable  {

	private int _permissions = 0;
	private String _user;
	private String _group;


	/**
	 * Constructor without any initial values. Users of this need to set fields
	 * as appropriate
	 */
	public HostFilePermissions(){
	}

	/**
	 * Constructor that takes the initial permissions in rwxrwxrwx form
	 * @param alphaPermissions the initial permissions in alpha form
	 */
	public HostFilePermissions(String alphaPermissions, String user, String group){
		String accessString = alphaPermissionsToOctal(alphaPermissions);
		_permissions = Integer.parseInt(accessString, 8);
		_user = user;
		_group = group;
	}

	/**
	 * Constructor that takes the initial permissions as a bitmask
	 * @param initialPermissions the initial permissions bitmask
	 */
	public HostFilePermissions(int initialPermissions, String user, String group){
		_permissions = initialPermissions;
		_user = user;
		_group = group;
	}

	/**
	 * Convert permissions in rwxrwxrwx form to octal
	 * @param userPermissions
	 * @return
	 */
	private String alphaPermissionsToOctal(String alphaPermissions)
	{
		if (alphaPermissions.length() == 10){ // directory bit?
			alphaPermissions = alphaPermissions.substring(1);
		}
		StringBuffer buf = new StringBuffer();
		// permissions
		char[] chars = alphaPermissions.toCharArray();

		int offset = -1;
		for (int i = 0; i < 3; i++){
			int value = 0;

			if (chars[++offset] == 'r'){
				value = 4;
			}
			if (chars[++offset] == 'w'){
				value += 2;
			}
			if (chars[++offset] == 'x'){
				value += 1;
			}
			buf.append(value);
		}

		return buf.toString();
	}

	public void setPermission(int permission, boolean value) {
		if (value)
			set(permission);
		else
			clear(permission);
	}

	public boolean getPermission(int permission) {
		return isSet(permission);
	}

	public int getPermissionBits() {
		return _permissions;
	}

	public void setPermissionBits(int bits) {
		_permissions = bits;
	}

	public String toString(){
		return "" + _permissions; //$NON-NLS-1$
	}

	private boolean isSet(long mask) {
		return (_permissions & mask) != 0;
	}

	private void set(int mask) {
		_permissions |= mask;
	}

	private void clear(int mask) {
		_permissions &= ~mask;
	}

	/**
	 * return permissions in rwxrwxrwx form
	 */
	public String toAlphaString(){
		StringBuffer buf = new StringBuffer();

		buf.append(getPermission(IHostFilePermissions.PERM_USER_READ) ? 'r' : '-');
		buf.append(getPermission(IHostFilePermissions.PERM_USER_WRITE) ? 'w' : '-');
		buf.append(getPermission(IHostFilePermissions.PERM_USER_EXECUTE) ? 'x' : '-');
		buf.append(getPermission(IHostFilePermissions.PERM_GROUP_READ) ? 'r' : '-');
		buf.append(getPermission(IHostFilePermissions.PERM_GROUP_WRITE) ? 'w' : '-');
		buf.append(getPermission(IHostFilePermissions.PERM_GROUP_EXECUTE) ? 'x' : '-');
		buf.append(getPermission(IHostFilePermissions.PERM_OTHER_READ) ? 'r' : '-');
		buf.append(getPermission(IHostFilePermissions.PERM_OTHER_WRITE) ? 'w' : '-');
		buf.append(getPermission(IHostFilePermissions.PERM_OTHER_EXECUTE) ? 'x' : '-');
		return buf.toString();
	}

	public String getGroupOwner() {
		return _group;
	}

	public String getUserOwner() {
		return _user;
	}

	public void setGroupOwner(String group) {
		_group = group;
	}

	public void setUserOwner(String user) {
		_user = user;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}


}
