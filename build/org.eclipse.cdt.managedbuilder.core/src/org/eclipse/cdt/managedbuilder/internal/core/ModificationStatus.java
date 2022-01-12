/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ModificationStatus extends Status implements IModificationStatus {
	private HashMap<String, String> fUnsupportedProperties = new HashMap<>();
	private HashMap<String, String> fUnsupportedRequiredProperties = new HashMap<>();
	private HashSet<String> fUndefinedProperties = new HashSet<>();
	private ITool[][] fToolConflicts;
	private ITool[] fNonManagedBuildTools;

	public static final ModificationStatus OK = new ModificationStatus(IStatus.OK, "", null); //$NON-NLS-1$

	ModificationStatus(String msg) {
		this(msg, null);
	}

	ModificationStatus(String msg, Throwable t) {
		this(IStatus.ERROR, msg, t);
	}

	ModificationStatus(int severity, String msg, Throwable t) {
		super(severity, ManagedBuilderCorePlugin.getUniqueIdentifier(), msg, t);
		fToolConflicts = new ITool[0][];
		fNonManagedBuildTools = new ITool[0];
	}

	ModificationStatus(Map<String, String> unsupportedRequiredProps, Map<String, String> unsupportedProps,
			Set<String> undefinedProps, ITool[][] conflicts, ITool nonMbsTools[]) {
		super(IStatus.OK, ManagedBuilderCorePlugin.getUniqueIdentifier(), ""); //$NON-NLS-1$

		int severity = IStatus.OK;
		int flags = 0;
		if (unsupportedRequiredProps != null && unsupportedRequiredProps.size() != 0) {
			fUnsupportedRequiredProperties.putAll(unsupportedRequiredProps);
			fUnsupportedProperties.putAll(unsupportedRequiredProps);
			flags |= REQUIRED_PROPS_NOT_SUPPORTED | PROPS_NOT_DEFINED;
			severity = IStatus.ERROR;
		}

		if (unsupportedProps != null && unsupportedProps.size() != 0) {
			fUnsupportedProperties.putAll(unsupportedProps);
			flags |= PROPS_NOT_SUPPORTED;
			if (severity == IStatus.OK)
				severity = IStatus.WARNING;
		}

		if (undefinedProps != null && undefinedProps.size() != 0) {
			fUndefinedProperties.addAll(undefinedProps);
			flags |= PROPS_NOT_DEFINED;
			if (severity == IStatus.OK)
				severity = IStatus.WARNING;
		}

		if (conflicts != null && conflicts.length != 0) {
			fToolConflicts = new ITool[conflicts.length][];
			for (int i = 0; i < conflicts.length; i++) {
				fToolConflicts[i] = conflicts[i].clone();
			}
			flags |= TOOLS_CONFLICT;
			if (severity == IStatus.OK)
				severity = IStatus.WARNING;
		} else {
			fToolConflicts = new ITool[0][];
		}

		if (nonMbsTools != null && nonMbsTools.length != 0) {
			fNonManagedBuildTools = nonMbsTools.clone();
			flags |= TOOLS_DONT_SUPPORT_MANAGED_BUILD;
			severity = IStatus.ERROR;
		} else {
			fNonManagedBuildTools = new ITool[0];
		}

		if (flags != 0) {
			setCode(flags);
		}

		if (severity != IStatus.OK) {
			setSeverity(severity);
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, String> getUnsupportedProperties() {
		return (HashMap<String, String>) fUnsupportedProperties.clone();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, String> getUnsupportedRequiredProperties() {
		return (HashMap<String, String>) fUnsupportedRequiredProperties.clone();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getUndefinedProperties() {
		return (HashSet<String>) fUndefinedProperties.clone();
	}

	@Override
	public ITool[][] getToolsConflicts() {
		ITool[][] copy = new ITool[fToolConflicts.length][];
		for (int i = 0; i < fToolConflicts.length; i++) {
			copy[i] = fToolConflicts[i].clone();
		}
		return copy;
	}

	@Override
	public ITool[] getNonManagedBuildTools() {
		return fNonManagedBuildTools.clone();
	}
}
