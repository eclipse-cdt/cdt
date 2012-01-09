/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Marc-Andre Laperle
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @author Mirko Stocker
 */
public class ImplementMethodRefactoringWizard extends RefactoringWizard {
   private final ImplementMethodRefactoring refactoring;
   private Map<MethodToImplementConfig, ParameterNamesInputPage>pagesMap =
		   new HashMap<MethodToImplementConfig, ParameterNamesInputPage>();

	public ImplementMethodRefactoringWizard(ImplementMethodRefactoring refactoring) {
	   super(refactoring, WIZARD_BASED_USER_INTERFACE);
	   this.refactoring = refactoring;
	}

	@Override
	protected void addUserInputPages() {
		addPage(new ImplementMethodInputPage(refactoring.getRefactoringData(), this));
		ImplementMethodData data = refactoring.getRefactoringData();
		for (MethodToImplementConfig config : data.getMethodDeclarations()) {
			if (config.getParaHandler().needsAdditionalArgumentNames()) {
				ParameterNamesInputPage page = new ParameterNamesInputPage(config, this);
				pagesMap.put(config, page);
				addPage(page);
			}
		}
	}
	
	public ParameterNamesInputPage getPageForConfig(MethodToImplementConfig config) {
		return pagesMap.get(config);
	}

	/**
	 * When canceling the wizard, RefactoringASTCache gets disposed and releases the lock on
	 * the index but the preview jobs might still be running and access the index or an index-based
	 * AST so we need to make sure they are done before disposing the cache
	 * <p> 
	 * When proceeding to the last page and finishing the wizard, the refactoring will run
	 * and possibly use concurrently the same ASTs that the jobs use, so we need to make
	 * sure the jobs are joined.
	 */
	protected void cancelAndJoinPreviewJobs() {
		boolean isOnePreviewJobRunning = false;
		for (ParameterNamesInputPage parameterNamesInputPage : pagesMap.values()) {
			isOnePreviewJobRunning |= parameterNamesInputPage.cancelPreviewJob();
		}
		
		// There are good chances that one job is still running, show a progress bar to the user,
		// join everything.
		if (isOnePreviewJobRunning) {
			try {
				getContainer().run(false, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Messages.ImplementMethodRefactoringWizard_CancelingPreviewGeneration,
								pagesMap.size() + 1);
						monitor.worked(1);

						for (ParameterNamesInputPage parameterNamesInputPage : pagesMap.values()) {
							parameterNamesInputPage.joinPreviewJob();
							monitor.worked(1);
						}

						monitor.done();
					}
				});
			} catch (InvocationTargetException e) {
				CUIPlugin.log(e);
			} catch (InterruptedException e) {
				// ignore since not cancelable
			}
		} else {
			// We don't take any chances, we still join everything. But there are good chances that
			// the jobs are stopped so we don't show a progress bar.
			for (ParameterNamesInputPage parameterNamesInputPage : pagesMap.values()) {
				parameterNamesInputPage.joinPreviewJob();
			}
		}
	}

	@Override
	public boolean performCancel() {
		cancelAndJoinPreviewJobs();
		return super.performCancel();
	}

	@Override
	public boolean performFinish() {
		cancelAndJoinPreviewJobs();
		return super.performFinish();
	}
}
