/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Interface of an object listening to (AST-) reconciling.
 * Inspired by JDT.
 * 
 * @since 4.0
 */
public interface ICReconcilingListener {
	/**
	 * Called before reconciling is started.
	 */
	void aboutToBeReconciled();

	/**
	 * Called after reconciling has been finished.
	 * 
	 * @param ast
	 *            the translation unit AST or <code>null</code> if the working
	 *            copy was consistent or reconciliation has been cancelled
	 * @param force
	 *            flag indicating whether the reconciler was invoked forcefully
	 * @param progressMonitor
	 *            the progress monitor
	 */
	void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor);
}
