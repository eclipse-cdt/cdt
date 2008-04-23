/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 *******************************************************************************/
package org.eclipse.rse.core.model;

/**
 * An object that keeps track of its persistable state: 
 * whether it is dirty or not and whether or not
 * any of its progeny are marked dirty.
 * @noextend This class is not intended to be subclassed by clients.
 * The standard extensions are included in the framework.
 */
public abstract class RSEPersistableObject implements IRSEPersistableContainer {

	private boolean _isDirty = false;
	private boolean _wasRestored = false;
	private boolean _isTainted = false;

	public RSEPersistableObject() {
		super();
	}

	public final boolean isDirty() {
		return _isDirty;
	}

	public final void setDirty(boolean flag) {
		_isDirty = flag;
		setTainted(flag);
	}

	public final boolean wasRestored() {
		return _wasRestored;
	}

	public final void setWasRestored(boolean flag) {
		_wasRestored = flag;
	}

	public final boolean isTainted() {
		return _isTainted;
	}

	public final void setTainted(boolean flag) {
		boolean taintParent = flag && !_isTainted;
		_isTainted = flag;
		if (taintParent) {
			IRSEPersistableContainer parent = getPersistableParent();
			if (parent != null) {
				parent.setTainted(true);
			}
		}
	}

	/**
	 * Does a null-aware string comparison. Two strings that are
	 * <code>null</code> will compare equal. Otherwise the result is 
	 * the same as s1.equals(s2), if s1 is not null.
	 * @param s1 The first string to compare
	 * @param s2 the second string
	 * @return true if the strings are equal or both null.
	 */
	protected boolean compareStrings(String s1, String s2) {
		if (s1 == s2) return true;
		if (s1 == null) return false;
		return s1.equals(s2);
	}

}