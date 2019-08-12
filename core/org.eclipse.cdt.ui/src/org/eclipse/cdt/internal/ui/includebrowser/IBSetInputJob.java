/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

public class IBSetInputJob extends Job {

	private ITranslationUnit fInput;
	private Display fDisplay;
	private IBViewPart fViewPart;

	public IBSetInputJob(IBViewPart viewPart, Display disp) {
		super(IBMessages.IBViewPart_waitingOnIndexerMessage);
		setSystem(true);
		fViewPart = viewPart;
		fDisplay = disp;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (CCorePlugin.getIndexManager().joinIndexer(IIndexManager.FOREVER, monitor)) {
			try {
				fDisplay.asyncExec(() -> fViewPart.setInput(fInput));
			} catch (SWTException e) {
				// display may be disposed
			}
		}
		return Status.OK_STATUS;
	}

	public void setInput(ITranslationUnit input) {
		fInput = input;
	}
}
