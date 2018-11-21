/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.scannerconfig;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;

/**
 * Similar to CPElement. Describes discovered paths and symbols available
 * through DiscoveredPathInfo instead of CPathEntry.
 *
 * @deprecated as of CDT 4.0. This class was used to set preferences/properties
 * for 3.X style projects.
 *
 * @author vhirsl
 */
@Deprecated
public class DiscoveredElement {
	public static final int CONTAINER = 1;
	public static final int INCLUDE_PATH = 2;
	public static final int SYMBOL_DEFINITION = 3;
	public static final int INCLUDE_FILE = 4;
	public static final int MACROS_FILE = 5;
	public static final int PATHS_GROUP = 10;
	public static final int SYMBOLS_GROUP = 11;
	public static final int INCLUDE_FILE_GROUP = 12;
	public static final int MACROS_FILE_GROUP = 13;

	private IProject fProject;
	private String fEntry;
	private int fEntryKind;
	private boolean fRemoved;
	private ArrayList<DiscoveredElement> fChildren = new ArrayList<>();
	private DiscoveredElement fParent;

	public DiscoveredElement(IProject project, String entry, int kind, boolean removed, boolean system) {
		fProject = project;
		fEntry = entry;
		fEntryKind = kind;
		fRemoved = removed;
	}

	public static DiscoveredElement createNew(DiscoveredElement parent, IProject project, String entry, int kind,
			boolean removed, boolean system) {
		DiscoveredElement rv = null;
		int parentKind = 0;
		switch (kind) {
		case CONTAINER: {
			rv = new DiscoveredElement(project, entry, kind, removed, system);
			DiscoveredElement group = new DiscoveredElement(project, null, PATHS_GROUP, false, false);
			rv.fChildren.add(group);
			group.setParent(rv);
			group = new DiscoveredElement(project, null, SYMBOLS_GROUP, false, false);
			rv.fChildren.add(group);
			group.setParent(rv);
			group = new DiscoveredElement(project, null, INCLUDE_FILE_GROUP, false, false);
			rv.fChildren.add(group);
			group.setParent(rv);
			group = new DiscoveredElement(project, null, MACROS_FILE_GROUP, false, false);
			rv.fChildren.add(group);
			group.setParent(rv);
		}
			return rv;
		case INCLUDE_PATH:
			parentKind = PATHS_GROUP;
			break;
		case SYMBOL_DEFINITION:
			parentKind = SYMBOLS_GROUP;
			break;
		case INCLUDE_FILE:
			parentKind = INCLUDE_FILE_GROUP;
			break;
		case MACROS_FILE:
			parentKind = MACROS_FILE_GROUP;
			break;
		}
		if (parentKind != 0) {
			if (parent != null) {
				DiscoveredElement group = null;
				if (parent.getEntryKind() == parentKind) {
					group = parent;
				} else if (parent.getEntryKind() == CONTAINER) {
					for (DiscoveredElement child : parent.fChildren) {
						if (child.getEntryKind() == parentKind) {
							group = child;
							break;
						}
					}
				}
				if (group != null) {
					rv = new DiscoveredElement(project, entry, kind, removed, system);
					group.fChildren.add(rv);
					rv.setParent(group);
				}
			}
		}
		return rv;
	}

	/**
	 * @return Returns the fProject.
	 */
	public IProject getProject() {
		return fProject;
	}

	/**
	 * @return the fEntry.
	 */
	public String getEntry() {
		return fEntry;
	}

	public void setEntry(String entry) {
		fEntry = entry;
	}

	/**
	 * @return Returns the fEntryKind.
	 */
	public int getEntryKind() {
		return fEntryKind;
	}

	/**
	 * @param entryKind The fEntryKind to set.
	 */
	public void setEntryKind(int entryKind) {
		fEntryKind = entryKind;
	}

	/**
	 * @return Returns the fRemoved.
	 */
	public boolean isRemoved() {
		return fRemoved;
	}

	/**
	 * @param removed The fRemoved to set.
	 */
	public void setRemoved(boolean removed) {
		fRemoved = removed;
	}

	/**
	 * @return Returns the fParent.
	 */
	public DiscoveredElement getParent() {
		return fParent;
	}

	/**
	 * @param parent The fParent to set.
	 */
	private void setParent(DiscoveredElement parent) {
		fParent = parent;
	}

	/**
	 * @return children of the discovered element
	 */
	public DiscoveredElement[] getChildren() {
		switch (fEntryKind) {
		case INCLUDE_PATH:
		case SYMBOL_DEFINITION:
		case INCLUDE_FILE:
		case MACROS_FILE:
			return new DiscoveredElement[0];
		}
		return fChildren.toArray(new DiscoveredElement[fChildren.size()]);
	}

	public boolean hasChildren() {
		switch (fEntryKind) {
		case INCLUDE_PATH:
		case SYMBOL_DEFINITION:
		case INCLUDE_FILE:
		case MACROS_FILE:
			return false;
		}
		return (fChildren.size() > 0);
	}

	public void setChildren(DiscoveredElement[] children) {
		fChildren = new ArrayList<>(Arrays.asList(children));
	}

	public boolean delete() {
		boolean rc = false;
		DiscoveredElement parent = getParent();
		if (parent != null) {
			rc = parent.fChildren.remove(this);
			for (DiscoveredElement child : fChildren) {
				child.setParent(null);
				rc |= true;
			}
		}
		return rc;
	}
}
