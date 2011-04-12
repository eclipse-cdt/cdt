/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;

/**
 * Determines whether a valid function was selected by the user to be able to
 * run the appropriate strategy for moving the function body to another
 * position.
 */
public class ToggleRefactoring extends CRefactoring {

	private ITextSelection selection;
	private IToggleRefactoringStrategy strategy;
	protected ToggleRefactoringContext context;
	private IIndex fIndex;
	
	public ToggleRefactoring(IFile file, ITextSelection selection, ICProject proj) {
		super(file, selection, null, proj);
		if (selection == null || file == null || project == null)
			initStatus.addFatalError(Messages.ToggleRefactoring_InvalidSelection);
		if (!IDE.saveAllEditors(new IResource[] {ResourcesPlugin.getWorkspace().getRoot()}, false))
			initStatus.addFatalError(Messages.ToggleRefactoring_CanNotSaveFiles);
		this.selection = selection;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		try {
			pm.subTask(Messages.ToggleRefactoring_WaitingForIndexer);
			prepareIndexer(pm);
			pm.subTask(Messages.ToggleRefactoring_AnalyseSelection);
			context = new ToggleRefactoringContext(fIndex, file, selection);
			strategy = new ToggleStrategyFactory(context).getAppropriateStategy();
		} catch (InterruptedException e) {
		} catch (NotSupportedException e) {
			initStatus.addFatalError(e.getMessage());
		} finally {
			fIndex.releaseReadLock();
		}

		return initStatus;
	}

	private void prepareIndexer(IProgressMonitor pm) throws CoreException, InterruptedException  {
		IIndexManager im = CCorePlugin.getIndexManager();
		while (!im.isProjectIndexed(project)) {
			im.joinIndexer(500, pm);
			if (pm.isCanceled())
				throw new NotSupportedException(Messages.ToggleRefactoring_NoIndex);
		}
		if (!im.isProjectIndexed(project))
			throw new NotSupportedException(Messages.ToggleRefactoring_NoIndex);
		IndexerPreferences.set(project.getProject(), IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, Boolean.TRUE.toString());
		fIndex = CCorePlugin.getIndexManager().getIndex(project);
		fIndex.acquireReadLock();
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector modifications) throws CoreException {
		pm.subTask(Messages.ToggleRefactoring_CalculateModifications);
		strategy.run(modifications);
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		return new EmptyRefactoringDescription();
	}
}
