/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.cxx;

import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.core.cxx.internal.model.CxxCodanReconciler;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Alena
 */
public class CodanCReconciler implements ICReconcilingListener {
	private CxxCodanReconciler reconsiler = new CxxCodanReconciler();

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
		// nothing?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#aboutToBeReconciled()
	 */
	public void aboutToBeReconciled() {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(
	 * 		org.eclipse.cdt.core.dom.ast.IASTTranslationUnit, boolean,
	 * 		org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		if (ast == null)
			return;
		ITranslationUnit tu = ast.getOriginatingTranslationUnit();
		if (tu == null)
			return;
		IResource resource = tu.getResource();
		if (resource == null)
			return;
		IProject project = resource.getProject();
		IPreferenceStore store = CodanUIActivator.getDefault().getPreferenceStore(project);
		if (store.getBoolean(PreferenceConstants.P_RUN_IN_EDITOR)) {
			reconsiler.reconciledAst(ast, resource, progressMonitor);
		}
	}
}
