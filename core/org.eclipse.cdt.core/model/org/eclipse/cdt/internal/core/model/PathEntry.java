/**********************************************************************
 * Created on 25-Mar-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IPathEntry;

public class PathEntry implements IPathEntry {

	public int entryKind;
	public boolean isExported;

	public PathEntry(int entryKind, boolean isExported) {

		this.entryKind = entryKind;
		this.isExported = isExported;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IPathEntry#getEntryKind()
	 */
	public int getEntryKind() {
		return entryKind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IPathEntry#isExported()
	 */
	public boolean isExported() {
		return isExported;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IPathEntry) {
			IPathEntry otherEntry = (IPathEntry)obj;
			if (entryKind != otherEntry.getEntryKind()) {
				return false;
			}
			if (isExported != otherEntry.isExported()) {
				return false;
			}
			return true;
		}
		return super.equals(obj);
	}

	/**
	 * Returns the kind from its <code>String</code> form.
	 */
	static int kindFromString(String kindStr) {

		if (kindStr.equalsIgnoreCase("prj")) //$NON-NLS-1$
			return IPathEntry.CDT_PROJECT;
		//if (kindStr.equalsIgnoreCase("var")) //$NON-NLS-1$
		//	return IPathEntry.CDT_VARIABLE;
		if (kindStr.equalsIgnoreCase("src")) //$NON-NLS-1$
			return IPathEntry.CDT_SOURCE;
		if (kindStr.equalsIgnoreCase("lib")) //$NON-NLS-1$
			return IPathEntry.CDT_LIBRARY;
		if (kindStr.equalsIgnoreCase("inc")) //$NON-NLS-1$
			return IPathEntry.CDT_INCLUDE;
		if (kindStr.equalsIgnoreCase("mac")) //$NON-NLS-1$
			return IPathEntry.CDT_MACRO;
		if (kindStr.equalsIgnoreCase("con")) //$NON-NLS-1$
			return IPathEntry.CDT_CONTAINER;
		return -1;
	}

	/**
	 * Returns a <code>String</code> for the kind of a path entry.
	 */
	static String kindToString(int kind) {

		switch (kind) {
			case IPathEntry.CDT_PROJECT :
				return "prj"; //$NON-NLS-1$
			case IPathEntry.CDT_SOURCE :
				return "src"; //$NON-NLS-1$
			case IPathEntry.CDT_LIBRARY :
				return "lib"; //$NON-NLS-1$
			//case IPathEntry.CDT_VARIABLE :
			//	return "var"; //$NON-NLS-1$
			case IPathEntry.CDT_INCLUDE :
				return "inc"; //$NON-NLS-1$
			case IPathEntry.CDT_MACRO :
				return "mac"; //$NON-NLS-1$
			case IPathEntry.CDT_CONTAINER :
				return "con"; //$NON-NLS-1$
			default :
				return "unknown"; //$NON-NLS-1$
		}
	}

	/**
	 * Returns a printable representation of this classpath entry.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('[');
		switch (getEntryKind()) {
			case IPathEntry.CDT_LIBRARY :
				buffer.append("CDT_LIBRARY"); //$NON-NLS-1$
				break;
			case IPathEntry.CDT_PROJECT :
				buffer.append("CDT_PROJECT"); //$NON-NLS-1$
				break;
			case IPathEntry.CDT_SOURCE :
				buffer.append("CDT_SOURCE"); //$NON-NLS-1$
				break;
			//case IPathEntry.CDT_VARIABLE :
			//	buffer.append("CDT_VARIABLE"); //$NON-NLS-1$
			//	break;
			case IPathEntry.CDT_INCLUDE :
				buffer.append("CDT_INCLUDE"); //$NON-NLS-1$
				break;
			case IPathEntry.CDT_MACRO :
				buffer.append("CDT_MACRO"); //$NON-NLS-1$
				break;
			case IPathEntry.CDT_CONTAINER :
				buffer.append("CDT_CONTAINER"); //$NON-NLS-1$
				break;
		}
		buffer.append(']');
		return buffer.toString();
	}

}
