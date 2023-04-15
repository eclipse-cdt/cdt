/*******************************************************************************
 * Copyright (c) 2004, 2023 QNX Software Systems and others.
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
 *     John Dallaway - set environment and tool prefix (#361)
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class DefaultGnuToolFactory implements IGnuToolFactory {
	protected ICExtension fExtension;

	public DefaultGnuToolFactory(ICExtension ext) {
		fExtension = ext;
	}

	@Override
	public Addr2line getAddr2line(IPath path) {
		IPath addr2LinePath = getAddr2linePath();
		String[] environment = getEnvironment();
		Addr2line addr2line = null;
		if (addr2LinePath != null && !addr2LinePath.isEmpty()) {
			try {
				addr2line = new Addr2line(addr2LinePath.toOSString(), new String[0], path.toOSString(), environment);
			} catch (IOException e1) {
			}
		}
		return addr2line;
	}

	@Override
	public CPPFilt getCPPFilt() {
		IPath cppFiltPath = getCPPFiltPath();
		String[] environment = getEnvironment();
		CPPFilt cppfilt = null;
		if (cppFiltPath != null && !cppFiltPath.isEmpty()) {
			try {
				cppfilt = new CPPFilt(cppFiltPath.toOSString(), new String[0], environment);
			} catch (IOException e2) {
			}
		}
		return cppfilt;
	}

	@Override
	public Objdump getObjdump(IPath path) {
		IPath objdumpPath = getObjdumpPath();
		String objdumpArgs = getObjdumpArgs();
		String[] environment = getEnvironment();
		Objdump objdump = null;
		if (objdumpPath != null && !objdumpPath.isEmpty()) {
			try {
				objdump = new Objdump(objdumpPath.toOSString(), objdumpArgs, path.toOSString(), environment);
			} catch (IOException e1) {
			}
		}
		return objdump;
	}

	@Override
	public NM getNM(IPath path) {
		IPath nmPath = getNMPath();
		String nmArgs = getNMArgs();
		String[] environment = getEnvironment();
		NM nm = null;
		if (nmPath != null && !nmPath.isEmpty()) {
			try {
				nm = new NM(nmPath.toOSString(), nmArgs, path.toOSString(), environment);
			} catch (IOException e1) {
			}
		}
		return nm;
	}

	protected IPath getAddr2linePath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("addr2line"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = getToolPrefix() + "addr2line"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IPath getObjdumpPath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("objdump"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = getToolPrefix() + "objdump"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected String getObjdumpArgs() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("objdumpArgs"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = ""; //$NON-NLS-1$
		}
		return value;
	}

	protected IPath getCPPFiltPath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("c++filt"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = getToolPrefix() + "c++filt"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IPath getStripPath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("strip"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = getToolPrefix() + "strip"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IPath getNMPath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("nm"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = getToolPrefix() + "nm"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected String getNMArgs() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("nmArgs"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = ""; //$NON-NLS-1$
		}
		return value;
	}

	/** @since 8.2 */
	protected String[] getEnvironment() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		ICConfigurationDescription cfg = ref.getConfiguration();
		IEnvironmentVariable[] vars = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cfg, true);
		return Arrays.stream(vars).map(v -> String.format("%s=%s", v.getName(), v.getValue())) //$NON-NLS-1$
				.toArray(String[]::new);
	}

	/** @since 8.2 */
	protected String getToolPrefix() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		ICConfigurationDescription cfg = ref.getConfiguration();
		ICdtVariable[] userVars = CCorePlugin.getUserVarSupplier().getMacros(cfg);
		ICdtVariable var = Arrays.stream(userVars).filter(v -> v.getName().equals(GNU_TOOL_PREFIX_VARIABLE)).findFirst()
				.orElse(null);

		// if user-defined variable not found, look for system variable provided by toolchain integration
		if (var == null) {
			var = CCorePlugin.getDefault().getCdtVariableManager().getVariable(GNU_TOOL_PREFIX_VARIABLE, cfg);
		}

		if (var != null) {
			try {
				return var.getStringValue();
			} catch (CdtVariableException e) {
				Platform.getLog(getClass())
						.log(Status.error("Error getting CDT variable string value: " + GNU_TOOL_PREFIX_VARIABLE, e)); //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
	}
}
