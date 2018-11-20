/*******************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.io.File;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;
import org.eclipse.core.runtime.IPath;

class PathInfo {
	private String fUnresolvedStr;
	private IPath fResolvedPath;
	private boolean fIsWorkspacePath;
	private String fAbsoluteInfoStr;
	private Boolean fIsAbsolute;
	private SupplierBasedCdtVariableSubstitutor fSubstitutor;

	private static final boolean IS_WINDOWS = File.separatorChar == '\\';

	public PathInfo(String str, boolean isWspPath, SupplierBasedCdtVariableSubstitutor subst) {
		fUnresolvedStr = str;
		fIsWorkspacePath = isWspPath;
		fSubstitutor = subst;
	}

	public String getUnresolvedPath() {
		return fUnresolvedStr;
	}

	public boolean isWorkspacePath() {
		return fIsWorkspacePath;
	}

	public boolean isAbsolute() {
		if (fIsAbsolute == null)
			fIsAbsolute = Boolean.valueOf(checkIsAbsolute());
		return fIsAbsolute.booleanValue();
	}

	private boolean checkIsAbsolute() {
		//		if(fIsWorkspacePath)
		//			return true;

		if (fResolvedPath != null)
			return fResolvedPath.isAbsolute();

		if (fAbsoluteInfoStr != null) {
			return isAbsolute(fAbsoluteInfoStr, fSubstitutor, new String[1]);
		}

		String str[] = new String[1];
		boolean isAbs = isAbsolute(fUnresolvedStr, fSubstitutor, str);
		fAbsoluteInfoStr = str[0];
		return isAbs;
	}

	private static boolean isAbsolute(String str, SupplierBasedCdtVariableSubstitutor subst, String[] out) {
		int length = str.length();
		out[0] = str;
		if (length == 0)
			return false;

		char c0 = str.charAt(0);
		if (c0 == '/' || c0 == '\\')
			return true;

		if (length == 1)
			return false;

		char c1 = str.charAt(1);
		if (IS_WINDOWS && c1 == ':')
			return true;

		if (length < 4)
			return false;

		if (c0 == '$' && c1 == '{') {
			int indx = str.indexOf('}');
			if (indx != -1) {
				String macroName = str.substring(2, indx);
				if (macroName.length() != 0) {
					String resolvedMacro;
					try {
						resolvedMacro = subst.resolveToString(macroName);
					} catch (CdtVariableException e) {
						ManagedBuilderCorePlugin.log(e);
						resolvedMacro = null;
						e.printStackTrace();
					}
					String substr = str.substring(indx + 1);
					String rStr = resolvedMacro == null || resolvedMacro.length() == 0 ? substr
							: new StringBuilder().append(resolvedMacro).append(subst).toString();
					return isAbsolute(rStr, subst, out);
				}
			}
		}

		return false;
	}

	public SupplierBasedCdtVariableSubstitutor getSubstitutor() {
		return fSubstitutor;
	}
}
