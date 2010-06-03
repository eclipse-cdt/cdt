/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;


/**
 * Low-level job to read source files in the background.
 */
public class SourceReadingJob extends Job {

	private final static String NAME = DisassemblyMessages.SourceReadingJob_name;

	private SourceFileInfo fFileInfo;
	private Runnable fDone;

	public SourceReadingJob(SourceFileInfo fi, Runnable done) {
		super(NAME);
		fFileInfo = fi;
		fFileInfo.fReadingJob = this;
		fDone = done;
		if (fi.fFile instanceof ISchedulingRule) {
			setRule((ISchedulingRule)fi.fFile);
		}
		setSystem(true);
		// usually short lived job
		setPriority(SHORT);
		if (fi.fFile.getFullPath() != null) {
			String fileName = fi.fFile.getFullPath().lastSegment();
			setName(NAME + " (" + fileName + ')'); //$NON-NLS-1$
		}
	}

	public synchronized void dispose() {
		fDone = null;
		Thread thread = getThread();
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		if (fFileInfo.fEditionJob != null) {
			try {
				fFileInfo.fEditionJob.join();
			} catch (InterruptedException e) {
				// ignore
			}
		}
		try {
			fFileInfo.initSource();
		} catch (Throwable e) {
			fFileInfo.fError = e;
		} finally {
			fFileInfo.fReadingJob = null;
			synchronized (this) {
				if (fDone != null && !getThread().isInterrupted()) {
					fDone.run();
				}
			}
		}
		// errors are handled elsewhere
		return Status.OK_STATUS;
	}
}
