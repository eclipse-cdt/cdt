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
package org.eclipse.cdt.ui.text;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

/**
 * A Job specialized to give access to the shared AST of the currently active editor.
 * Clients must implement {@link #runOnAST(ILanguage, IASTTranslationUnit)}.
 * 
 * @since 5.1
 */
public abstract class SharedASTJob extends Job {

	/**
	 * The translation unit for which to access the AST.
	 */
	protected final ITranslationUnit fUnit;

	/**
	 * Create a shared AST job for the given translation unit.
	 * 
	 * @param name  the display name of this job
	 * @param tUnit  the translation unit to get the AST for
	 */
	public SharedASTJob(String name, ITranslationUnit tUnit) {
		super(name);
		fUnit = tUnit;
	}

	/**
	 * Run an operation on the shared AST of the requested translation unit.
	 * This method will only be called if the requested translation unit is open 
	 * in the currently active editor. 
	 * 
	 * @param lang  the associated <code>ILanguage</code> of the translation unit
	 * @param ast  the AST object of the translation unit or <code>null</code>
	 * @return A <code>Status</code> object reflecting the result of the operation
	 * @throws CoreException
	 */
	public abstract IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException;

	@Override
	protected final IStatus run(IProgressMonitor monitor) {
		ASTProvider provider = CUIPlugin.getDefault().getASTProvider();
		if (provider == null) {
			return Status.CANCEL_STATUS;
		}
		return provider.runOnAST(fUnit, ASTProvider.WAIT_ACTIVE_ONLY, monitor, new ASTRunnable() {
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
				return SharedASTJob.this.runOnAST(lang, ast);
			}});
	}

    @Override
	public boolean shouldSchedule() {
        return super.shouldSchedule() && PlatformUI.isWorkbenchRunning() && CUIPlugin.getDefault() != null;
    }

    @Override
	public boolean shouldRun() {
        return super.shouldRun() && PlatformUI.isWorkbenchRunning() && CUIPlugin.getDefault() != null;
    }

}
