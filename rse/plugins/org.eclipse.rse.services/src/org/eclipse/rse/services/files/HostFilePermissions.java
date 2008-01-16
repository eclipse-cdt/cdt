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
 * Implementation of IHostFilePermissions.
 *
 */
public class HostFilePermissions implements
		IHostFilePermissions {
	
	private int _permissions = 0;
		
	/**
	 * Constructor that take the initial permissions as a bitmask
	 * @param initialPermissions the intial permissions bitmask
	 */
	public HostFilePermissions(int initialPermissions){
		_permissions = initialPermissions;
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
		return "" + _permissions;
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
	public String toUserString(){
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
}
