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

/**
 * @author Alena
 */
public class CodanCReconciler implements ICReconcilingListener {
	private CxxCodanReconciler reconsiler = new CxxCodanReconciler();

	void install(CEditor editor) {
		editor.addReconcileListener(this);
	}

	void uninstall(CEditor editor) {
		editor.removeReconcileListener(this);
	}

	@Override
	public void aboutToBeReconciled() {
		// nothing
	}

	@Override
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
