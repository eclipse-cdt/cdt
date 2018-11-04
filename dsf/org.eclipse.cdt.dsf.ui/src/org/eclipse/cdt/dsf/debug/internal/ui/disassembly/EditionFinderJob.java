/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.internalError;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourceFileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job to find a suitable edition from the local history
 * based on a file and the timestamp of the code module.
 */
class EditionFinderJob extends Job {

	private final IFile fFile;
	private final BigInteger fAddress;
	private final DisassemblyPart fDisassemblyPart;
	private final SourceFileInfo fSourceInfo;

	/**
	 * Create a new edition finder for a file resource and address.
	 * 
	 * @param sourceInfo  the file info containing the file resource for which to find an edition
	 * @param address  address inside the module
	 * @param disassemblyPart  the disassembly part where this job originated from
	 */
	public EditionFinderJob(SourceFileInfo sourceInfo, BigInteger address, DisassemblyPart disassemblyPart) {
		super(DisassemblyMessages.EditionFinderJob_name);
		Assert.isNotNull(sourceInfo);
		Assert.isLegal(sourceInfo.fFile instanceof IFile);
		fSourceInfo= sourceInfo;
		fFile = (IFile)sourceInfo.fFile;
		fAddress = address;
		fDisassemblyPart= disassemblyPart;
		setRule(fFile);
		setSystem(true);
		sourceInfo.fEditionJob= this;
	}

	/*
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, DisassemblyMessages.EditionFinderJob_name, 2);
		progress.subTask(DisassemblyMessages.EditionFinderJob_task_get_timestamp);
		long moduleTime;
		Object token = fDisassemblyPart.retrieveModuleTimestamp(fAddress);
		if (token != null && !(token instanceof Long) && !progress.isCanceled()) {
			try {
				synchronized (token) {
					token.wait(1000);
				}
			} catch (InterruptedException e) {
				internalError(e);
			}
			token = fDisassemblyPart.retrieveModuleTimestamp(fAddress);
		}
		progress.worked(1);
		if (token instanceof Long && !progress.isCanceled()) {
			moduleTime = ((Long)token).longValue();
			long buildTime = moduleTime * 1000;
			if (fFile.getLocalTimeStamp() > buildTime) {
				progress.subTask(DisassemblyMessages.EditionFinderJob_task_search_history);
				// get history - recent states first
				IFileState[] states;
				try {
					states = fFile.getHistory(progress.split(1));
				} catch (CoreException e) {
					states = new IFileState[0];
				}
				for (int i = 0; i < states.length; i++) {
					IFileState state = states[i];
					long saveTime = state.getModificationTime();
					if (saveTime <= buildTime) {
						fSourceInfo.fEdition = state;
						break;
					}
				}
			}
		}
		fSourceInfo.fEditionJob = null;
		progress.worked(1);
		return Status.OK_STATUS;
	}

}
