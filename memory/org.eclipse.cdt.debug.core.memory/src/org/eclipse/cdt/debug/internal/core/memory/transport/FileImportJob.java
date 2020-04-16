/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.memory.transport;

import org.eclipse.cdt.debug.core.memory.transport.FileImport;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Runs {@link FileImport} operation as a {@link Job}
 *
 */
public final class FileImportJob extends Job {

	private final FileImport fileImport;

	public FileImportJob(String name, FileImport fileImport) {
		super(name);
		this.fileImport = fileImport;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			fileImport.run(monitor);
		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

}
