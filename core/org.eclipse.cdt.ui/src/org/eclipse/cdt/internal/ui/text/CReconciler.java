/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.reconciler.MonoReconciler;

/**
 * A single strategy C reconciler.
 * 
 * @since 4.0
 */
public class CReconciler extends MonoReconciler {

	/**
	 * Create a reconciler for the given strategy.
	 * 
	 * @param strategy  the C reconciling strategy
	 */
	public CReconciler(CReconcilingStrategy strategy) {
		super(strategy, false);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.AbstractReconciler#aboutToBeReconciled()
	 */
	protected void aboutToBeReconciled() {
		CReconcilingStrategy strategy= (CReconcilingStrategy)getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
		strategy.aboutToBeReconciled();
	}

}
