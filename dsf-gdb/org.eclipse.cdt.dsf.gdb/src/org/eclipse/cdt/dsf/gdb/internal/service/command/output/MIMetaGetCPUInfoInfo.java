/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.service.command.output;

import java.util.Arrays;

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.internal.core.ICoreInfo;

/**
 * Result obtained from MIMetaGetCPUInfo.
 * @since 4.1
 */
public class MIMetaGetCPUInfoInfo implements ICommandResult {

	private final ICoreInfo[] fCoresInfo;

	public MIMetaGetCPUInfoInfo(ICoreInfo[] info) {
		fCoresInfo = info;
	}

	public ICoreInfo[] getInfo() {
		return fCoresInfo;
	}

	@Override
	public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + Arrays.toString(getInfo()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}