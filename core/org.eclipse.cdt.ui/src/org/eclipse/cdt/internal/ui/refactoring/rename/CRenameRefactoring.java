/*******************************************************************************
 * Copyright (c) 2004, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

/**
 * Refactoring implementation using a refactoring processor.
 */
public class CRenameRefactoring extends ProcessorBasedRefactoring {
	public static final String ID = "org.eclipse.cdt.internal.ui.refactoring.rename.CRenameRefactoring"; //$NON-NLS-1$
	private Change fChange;

	public CRenameRefactoring(CRenameProcessor processor) {
		super(processor);
	}

	@Override
	public CRenameProcessor getProcessor() {
		return (CRenameProcessor) super.getProcessor();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		fChange = super.createChange(pm);
		return fChange;
	}

	/**
	 * Returns the change if it has been created, or {@code null} otherwise.
	 */
	Change getChange() {
		return fChange;
	}
}
