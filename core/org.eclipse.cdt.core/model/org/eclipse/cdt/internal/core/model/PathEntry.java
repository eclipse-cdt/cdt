/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathEntry implements IPathEntry {

	protected int entryKind;
	protected boolean isExported;
	protected IPath path;

	public PathEntry(int entryKind, IPath path, boolean isExported) {
		this.path = (path == null) ? Path.EMPTY : path;
		this.entryKind = entryKind;
		this.isExported = isExported;
	}

	@Override
	public IPath getPath() {
		return path;
	}

	@Override
	public int getEntryKind() {
		return entryKind;
	}

	@Override
	public boolean isExported() {
		return isExported;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + entryKind;
		result = prime * result + (isExported ? 1231 : 1237);
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IPathEntry) {
			IPathEntry otherEntry = (IPathEntry) obj;
			if (!path.equals(otherEntry.getPath())) {
				return false;
			}
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
	public static int kindFromString(String kindStr) {

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
		if (kindStr.equalsIgnoreCase("incfile")) //$NON-NLS-1$
			return IPathEntry.CDT_INCLUDE_FILE;
		if (kindStr.equalsIgnoreCase("mac")) //$NON-NLS-1$
			return IPathEntry.CDT_MACRO;
		if (kindStr.equalsIgnoreCase("macfile")) //$NON-NLS-1$
			return IPathEntry.CDT_MACRO_FILE;
		if (kindStr.equalsIgnoreCase("con")) //$NON-NLS-1$
			return IPathEntry.CDT_CONTAINER;
		if (kindStr.equalsIgnoreCase("out")) //$NON-NLS-1$
			return IPathEntry.CDT_OUTPUT;
		return -1;
	}

	/**
	 * Returns a <code>String</code> for the kind of a path entry.
	 */
	static String kindToString(int kind) {

		switch (kind) {
		case IPathEntry.CDT_PROJECT:
			return "prj"; //$NON-NLS-1$
		case IPathEntry.CDT_SOURCE:
			return "src"; //$NON-NLS-1$
		case IPathEntry.CDT_LIBRARY:
			return "lib"; //$NON-NLS-1$
		case IPathEntry.CDT_INCLUDE:
			return "inc"; //$NON-NLS-1$
		case IPathEntry.CDT_INCLUDE_FILE:
			return "incfile"; //$NON-NLS-1$
		case IPathEntry.CDT_MACRO:
			return "mac"; //$NON-NLS-1$
		case IPathEntry.CDT_MACRO_FILE:
			return "macfile"; //$NON-NLS-1$
		case IPathEntry.CDT_CONTAINER:
			return "con"; //$NON-NLS-1$
		case IPathEntry.CDT_OUTPUT:
			return "out"; //$NON-NLS-1$
		default:
			return "unknown"; //$NON-NLS-1$
		}
	}

	/**
	 * Returns a printable representation of this classpath entry.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if (path != null && !path.isEmpty()) {
			buffer.append(path.toString()).append(' ');
		}
		buffer.append('[');
		buffer.append(getKindString());
		buffer.append(']');
		return buffer.toString();
	}

	String getKindString() {
		switch (getEntryKind()) {
		case IPathEntry.CDT_LIBRARY:
			return ("Library path"); //$NON-NLS-1$
		case IPathEntry.CDT_PROJECT:
			return ("Project path"); //$NON-NLS-1$
		case IPathEntry.CDT_SOURCE:
			return ("Source path"); //$NON-NLS-1$
		case IPathEntry.CDT_OUTPUT:
			return ("Output path"); //$NON-NLS-1$
		case IPathEntry.CDT_INCLUDE:
			return ("Include path"); //$NON-NLS-1$
		case IPathEntry.CDT_INCLUDE_FILE:
			return ("Include-file path"); //$NON-NLS-1$
		case IPathEntry.CDT_MACRO:
			return ("Symbol definition"); //$NON-NLS-1$
		case IPathEntry.CDT_MACRO_FILE:
			return ("Symbol-file definition"); //$NON-NLS-1$
		case IPathEntry.CDT_CONTAINER:
			return ("Contributed paths"); //$NON-NLS-1$
		}
		return ("Unknown"); //$NON-NLS-1$
	}
}
