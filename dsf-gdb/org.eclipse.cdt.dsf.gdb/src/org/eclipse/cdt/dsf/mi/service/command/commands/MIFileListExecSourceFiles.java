/*******************************************************************************
 * Copyright (c) 2017, 2018 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MiSourceFilesInfo;

/**
 *
 * -file-list-exec-source-files
 *
 * Returns the list of source files for the current execution context. It
 * outputs both filename and full (absolute path) file name of a source file.
 *
 * @since 5.8
 */
public class MIFileListExecSourceFiles extends MICommand<MiSourceFilesInfo> {

	public MIFileListExecSourceFiles(IDMContext ctx) {
		super(ctx, "-file-list-exec-source-files"); //$NON-NLS-1$
	}

	@Override
	public MiSourceFilesInfo getResult(MIOutput out) {
		return new MiSourceFilesInfo(out);
	}
}