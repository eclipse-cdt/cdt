/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Alena
 * 
 */
public class CodanCReconciler implements ICReconcilingListener {
	void install(ITextEditor editor) {
		if (editor instanceof CEditor) {
			initialize();
			((CEditor) editor).addReconcileListener(this);
		}
	}

	void uninstall(ITextEditor editor) {
		if (editor instanceof CEditor) {
			initialize();
			((CEditor) editor).removeReconcileListener(this);
		}
	}

	/**
	 * 
	 */
	private void initialize() {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.internal.ui.text.ICReconcilingListener#aboutToBeReconciled
	 * ()
	 */
	public void aboutToBeReconciled() {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(org
	 * .eclipse.cdt.core.dom.ast.IASTTranslationUnit, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void reconciled(IASTTranslationUnit ast, boolean force,
			IProgressMonitor progressMonitor) {
		CodanRuntime.getInstance().getAstQuickBuilder().reconcileAst(ast,
				progressMonitor);
		// System.err.println("ast reconsiled");
	}
}
