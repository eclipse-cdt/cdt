/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourceFileInfo;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;

/**
 * UI job to color source code.
 */
class SourceColorerJob extends UIJob implements Runnable {

	private final DisassemblyPart fDisassemblyPart;
	private final ISourceViewer fViewer;
	private final DisassemblyDocument fDocument;
	private final IStorage fStorage;

	public SourceColorerJob(Display jobDisplay, IStorage storage, DisassemblyPart disassemblyPart) {
		super(DisassemblyMessages.SourceColorerJob_name);
		fDisassemblyPart= disassemblyPart;
		fViewer= disassemblyPart.getTextViewer();
		fDocument= (DisassemblyDocument) fViewer.getDocument();
		fStorage = storage;
		setDisplay(fDisassemblyPart.getSite().getShell().getDisplay());
		setSystem(true);
		setPriority(INTERACTIVE);
	}

	/*
	 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (fViewer != null && !monitor.isCanceled()) {
			monitor.beginTask(DisassemblyMessages.SourceColorerJob_name, IProgressMonitor.UNKNOWN);
			SourceFileInfo fi = fDocument.getSourceInfo(fStorage);
			if (fi != null) {
				fi.initPresentationCreator(fViewer);
				if (fi.fError != null) {
					String message= DisassemblyMessages.Disassembly_log_error_readFile + fi.fFileKey;
					fDisassemblyPart.logWarning(message, fi.fError);
				}
			}
			fDisassemblyPart.updateInvalidSource();
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		IWorkbenchSiteProgressService progressService = (IWorkbenchSiteProgressService)fDisassemblyPart.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if(progressService != null) {
			progressService.schedule(this, 0, true);
		} else {
			schedule();
		}
	}

}
