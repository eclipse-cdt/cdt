/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.compile;

import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;

public class SystemCompileRemoteObjectMatcher {
	private String ssfId, nameFilter, typeFilter;
	private boolean allSsfs, allNames, allTypes;
	private boolean genericSsfStart, genericNamesStart, genericTypesStart;
	private boolean genericSsfEnd, genericNamesEnd, genericTypesEnd;
	private String ssfIdPart, nameFilterPart, typeFilterPart;

	public SystemCompileRemoteObjectMatcher(String ssfId, String nameFilter, String typeFilter) {
		this.ssfId = ssfId;
		this.nameFilter = nameFilter;
		this.typeFilter = typeFilter;
		if (ssfId == null) {
			ssfId = "*"; //$NON-NLS-1$
		}
		if (nameFilter == null) {
			nameFilter = "*"; //$NON-NLS-1$
		}
		if (typeFilter == null) {
			typeFilter = "*"; //$NON-NLS-1$
		}
		// determine if any attribute is totally generic
		this.allSsfs = ssfId.equals("*"); //$NON-NLS-1$
		this.allNames = nameFilter.equals("*"); //$NON-NLS-1$
		this.allTypes = typeFilter.equals("*"); //$NON-NLS-1$
		// determine if any attribute value starts with asterisk
		this.genericSsfStart = !allSsfs && startsWithAsterisk(ssfId);
		this.genericNamesStart = !allNames && startsWithAsterisk(nameFilter);
		this.genericTypesStart = !allTypes && startsWithAsterisk(typeFilter);
		// determine if any attribute value ends with asterisk
		this.genericSsfEnd = !allSsfs && endsWithAsterisk(ssfId);
		this.genericNamesEnd = !allNames && endsWithAsterisk(nameFilter);
		this.genericTypesEnd = !allTypes && endsWithAsterisk(typeFilter);
		// strip of leading asterisk if there is one
		if (genericSsfStart) {
			ssfIdPart = stripLeadingAsterisk(ssfId);
		}
		if (genericNamesStart) {
			nameFilterPart = stripLeadingAsterisk(nameFilter);
		}
		if (genericTypesStart) {
			typeFilterPart = stripLeadingAsterisk(typeFilter);
		}
		// strip of trailing asterisk if there is one
		if (genericSsfEnd) {
			ssfIdPart = stripTrailingAsterisk(ssfId);
		}
		if (genericNamesEnd) {
			nameFilterPart = stripTrailingAsterisk(nameFilter);
		}
		if (genericTypesEnd) {
			typeFilterPart = stripTrailingAsterisk(typeFilter);
		}
	}

	/**
	 * Helper method.
	 * Returns true if given name starts with an asterisk.
	 */
	protected boolean startsWithAsterisk(String name) {
		return name.startsWith("*"); //$NON-NLS-1$
	}

	/**
	 * Helper method.
	 * Returns true if given name ends with an asterisk.
	 */
	protected boolean endsWithAsterisk(String name) {
		return name.endsWith("*"); //$NON-NLS-1$
	}

	/**
	 * Helper method.
	 * Strips off the leading asterisk.
	 */
	protected String stripLeadingAsterisk(String name) {
		return name.substring(1);
	}

	/**
	 * Helper method.
	 * Strips off the trailing asterisk.
	 */
	protected String stripTrailingAsterisk(String name) {
		return name.substring(0, name.length() - 1);
	}

	/**
	 * Getter method.
	 * Return what was specified for the <samp>subsystemconfigurationid</samp> xml attribute.
	 */
	public String getSubSystemFactoryId() {
		return ssfId;
	}

	/**
	 * Getter method.
	 * Return what was specified for the <samp>namefilter</samp> xml attribute.
	 */
	public String getNameFilter() {
		return nameFilter;
	}

	/**
	 * Getter method.
	 * Return what was specified for the <samp>typefilter</samp> xml attribute.
	 */
	public String getTypeFilter() {
		return typeFilter;
	}

	/**
	 * Returns true if the current selection matches all the given filtering criteria, false otherwise.
	 */
	public boolean appliesTo(Object element) {
		ISystemRemoteElementAdapter adapter = SystemAdapterHelpers.getRemoteAdapter(element);
		if (adapter == null) {
			return false;
		}
		boolean applies = true;
		// must match on all attributes
		// check for match on subsystem factory id
		boolean ssfIdMatch = true;
		if (!allSsfs) {
			String subsystem = adapter.getSubSystemConfigurationId(element);
			if (ssfId == null) {
				ssfIdMatch = false;
			} else if (!genericSsfStart && !genericSsfEnd) {
				ssfIdMatch = subsystem.equals(ssfId);
			} else if (genericSsfStart) {
				ssfIdMatch = subsystem.endsWith(ssfIdPart);
			} else if (genericSsfEnd) {
				ssfIdMatch = subsystem.startsWith(ssfIdPart);
			}
		}
		if (!ssfIdMatch) {
			return false;
		}
		// check for match on name filter
		boolean nameMatch = true;
		if (!allNames) {
			String name = adapter.getName(element);
			if (name == null || !genericNamesStart) {
				nameMatch = false;
			} else if (!genericNamesStart && !genericNamesEnd) {
				nameMatch = name.equals(nameFilter);
			} else if (genericNamesStart) {
				nameMatch = name.endsWith(nameFilterPart);
			} else if (genericNamesEnd) {
				nameMatch = name.startsWith(nameFilterPart);
			}
		}
		if (!nameMatch) {
			return false;
		}
		// check for match on type filter
		boolean typeMatch = true;
		if (!allTypes) {
			String type = adapter.getRemoteSourceType(element);
			if (type == null) {
				typeMatch = false;
			} else if (!genericTypesStart && !genericTypesEnd) {
				typeMatch = type.equals(typeFilter);
			} else if (genericTypesStart) {
				typeMatch = type.endsWith(typeFilterPart);
			} else if (genericTypesEnd) {
				typeMatch = type.startsWith(typeFilterPart);
			}
		}
		if (!typeMatch) {
			return false;
		}
		return applies;
	}
}
