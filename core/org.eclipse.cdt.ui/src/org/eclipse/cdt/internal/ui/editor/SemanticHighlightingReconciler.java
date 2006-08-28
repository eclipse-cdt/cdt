/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedPosition;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightingStyle;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;


/**
 * Semantic highlighting reconciler - Background thread implementation.
 * Cloned from JDT.
 * 
 * @since 4.0
 */
public class SemanticHighlightingReconciler implements ICReconcilingListener {

	/**
	 * Collects positions from the AST.
	 */
	private class PositionCollector extends ASTVisitor {
		{
			shouldVisitNames= true;
			shouldVisitDeclarations= true;
			shouldVisitExpressions= true;
		}
		
		/** The semantic token */
		private SemanticToken fToken= new SemanticToken();
		private String fFilePath;
		private IPositionConverter fPositionTracker;
		
		/**
		 * @param filePath
		 * @param positionTracker
		 */
		public PositionCollector(String filePath, IPositionConverter positionTracker) {
			fFilePath= filePath;
			fPositionTracker= positionTracker;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
		 */
		public int visit(IASTDeclaration declaration) {
			if (!fFilePath.equals(declaration.getContainingFilename())) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
		 */
		public int visit(IASTExpression expression) {
			if (!fFilePath.equals(expression.getContainingFilename())) {
				return PROCESS_SKIP;
			}
			IASTNodeLocation[] nodeLocations= expression.getNodeLocations();
			if (nodeLocations.length > 0 && nodeLocations[0] instanceof IASTMacroExpansion) {
				if (visitNode(expression)) {
					return PROCESS_SKIP;
				}
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTName)
		 */
		public int visit(IASTName node) {
			if (!fFilePath.equals(node.getContainingFilename())) {
				return PROCESS_SKIP;
			}
			visitNode(node);
			return PROCESS_CONTINUE;
		}
		
		private boolean visitNode(IASTNode node) {
			boolean consumed= false;
			fToken.update(node);
			for (int i= 0, n= fJobSemanticHighlightings.length; i < n; ++i) {
				SemanticHighlighting semanticHighlighting= fJobSemanticHighlightings[i];
				if (fJobHighlightings[i].isEnabled() && semanticHighlighting.consumes(fToken)) {
					IASTNodeLocation[] nodeLocations= node.getNodeLocations();
					if (nodeLocations.length > 0) {
						addNodeLocations(nodeLocations, fJobHighlightings[i]);
					}
					consumed= true;
					break;
				}
			}
			fToken.clear();
			return consumed;
		}

		/**
		 * Add all node locations for the given highlighting.
		 * 
		 * @param nodeLocations The node locations
		 * @param highlighting The highlighting
		 */
		private void addNodeLocations(IASTNodeLocation[] nodeLocations, HighlightingStyle highlighting) {
			for (int j = 0; j < nodeLocations.length; j++) {
				IASTNodeLocation nodeLocation = nodeLocations[j];
				if (nodeLocation instanceof IASTMacroExpansion) {
					IASTMacroExpansion macroExpansion= (IASTMacroExpansion)nodeLocation;
					IASTNodeLocation[] expansionLocations = macroExpansion.getExpansionLocations();
					addNodeLocations(expansionLocations, highlighting);
				} else {
					int offset= nodeLocation.getNodeOffset();
					int length= nodeLocation.getNodeLength();
					if (fPositionTracker != null) {
						IRegion actualPos= fPositionTracker.historicToActual(new Region(offset, length));
						offset= actualPos.getOffset();
						length= actualPos.getLength();
					}
					if (offset > -1 && length > 0) {
						addPosition(offset, length, highlighting);
					}
				}
			}
		}

	/**
		 * Add a position with the given range and highlighting iff it does not exist already.
		 * 
		 * @param offset The range offset
		 * @param length The range length
		 * @param highlighting The highlighting
		 */
		private void addPosition(int offset, int length, HighlightingStyle highlighting) {
			boolean isExisting= false;
			// TODO: use binary search
			for (int i= 0, n= fRemovedPositions.size(); i < n; i++) {
				HighlightedPosition position= (HighlightedPosition) fRemovedPositions.get(i);
				if (position == null)
					continue;
				if (position.isEqual(offset, length, highlighting)) {
					isExisting= true;
					fRemovedPositions.set(i, null);
					fNOfRemovedPositions--;
					break;
				}
			}

			if (!isExisting) {
				Position position= fJobPresenter.createHighlightedPosition(offset, length, highlighting);
				fAddedPositions.add(position);
			}
		}

	}

	/** The C editor this semantic highlighting reconciler is installed on */
	private CEditor fEditor;
	/** The semantic highlighting presenter */
	private SemanticHighlightingPresenter fPresenter;
	/** Semantic highlightings */
	private SemanticHighlighting[] fSemanticHighlightings;
	/** Highlightings */
	private HighlightingStyle[] fHighlightings;

	/** Background job's added highlighted positions */
	private List fAddedPositions= new ArrayList();
	/** Background job's removed highlighted positions */
	private List fRemovedPositions= new ArrayList();
	/** Number of removed positions */
	private int fNOfRemovedPositions;

	/** Background job */
	private Job fJob;
	/** Background job lock */
	private final Object fJobLock= new Object();
	/** Reconcile operation lock. */
	private final Object fReconcileLock= new Object();
	/**
	 * <code>true</code> if any thread is executing
	 * <code>reconcile</code>, <code>false</code> otherwise.
	 */
	private boolean fIsReconciling= false;

	/** The semantic highlighting presenter - cache for background thread, only valid during {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)} */
	private SemanticHighlightingPresenter fJobPresenter;
	/** Semantic highlightings - cache for background thread, only valid during {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)} */
	private SemanticHighlighting[] fJobSemanticHighlightings;
	/** Highlightings - cache for background thread, only valid during {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)} */
	private HighlightingStyle[] fJobHighlightings;

	/*
	 * @see org.eclipse.cdt.internal.ui.text.java.ICReconcilingListener#aboutToBeReconciled()
	 */
	public void aboutToBeReconciled() {
		// Do nothing
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(IASTTranslationUnit, IPositionConverter, IProgressMonitor)
	 */
	public void reconciled(IASTTranslationUnit ast, IPositionConverter positionTracker, IProgressMonitor progressMonitor) {
		// ensure at most one thread can be reconciling at any time
		synchronized (fReconcileLock) {
			if (fIsReconciling)
				return;
			else
				fIsReconciling= true;
		}
		fJobPresenter= fPresenter;
		fJobSemanticHighlightings= fSemanticHighlightings;
		fJobHighlightings= fHighlightings;
		
		try {
			if (fJobPresenter == null || fJobSemanticHighlightings == null || fJobHighlightings == null)
				return;
			
			fJobPresenter.setCanceled(progressMonitor != null && progressMonitor.isCanceled());
			
			if (ast == null || fJobPresenter.isCanceled())
				return;
			
			PositionCollector collector= new PositionCollector(ast.getFilePath(), positionTracker);
			IASTNode[] subtrees= getAffectedSubtrees(ast);
			if (subtrees.length == 0)
				return;
			
			startReconcilingPositions();
			
			if (!fJobPresenter.isCanceled())
				reconcilePositions(subtrees, collector);
			
			TextPresentation textPresentation= null;
			if (!fJobPresenter.isCanceled())
				textPresentation= fJobPresenter.createPresentation(fAddedPositions, fRemovedPositions);
			
			if (!fJobPresenter.isCanceled())
				updatePresentation(textPresentation, fAddedPositions, fRemovedPositions);
			
			stopReconcilingPositions();
		} finally {
			fJobPresenter= null;
			fJobSemanticHighlightings= null;
			fJobHighlightings= null;
			synchronized (fReconcileLock) {
				fIsReconciling= false;
			}
		}
	}

	/**
	 * @param node Root node
	 * @return Array of subtrees that may be affected by past document changes
	 */
	private IASTNode[] getAffectedSubtrees(IASTNode node) {
		return new IASTNode[] { node };
	}

	/**
	 * Start reconciling positions.
	 */
	private void startReconcilingPositions() {
		fJobPresenter.addAllPositions(fRemovedPositions);
		fNOfRemovedPositions= fRemovedPositions.size();
	}

	/**
	 * Reconcile positions based on the AST subtrees
	 *
	 * @param subtrees the AST subtrees
	 */
	private void reconcilePositions(IASTNode[] subtrees, ASTVisitor visitor) {
		// FIXME: remove positions not covered by subtrees
		for (int i= 0, n= subtrees.length; i < n; i++) {
			subtrees[i].accept(visitor);
		}
		List oldPositions= fRemovedPositions;
		List newPositions= new ArrayList(fNOfRemovedPositions);
		for (int i= 0, n= oldPositions.size(); i < n; i ++) {
			Object current= oldPositions.get(i);
			if (current != null)
				newPositions.add(current);
		}
		fRemovedPositions= newPositions;
	}

	/**
	 * Update the presentation.
	 *
	 * @param textPresentation the text presentation
	 * @param addedPositions the added positions
	 * @param removedPositions the removed positions
	 */
	private void updatePresentation(TextPresentation textPresentation, List addedPositions, List removedPositions) {
		Runnable runnable= fJobPresenter.createUpdateRunnable(textPresentation, addedPositions, removedPositions);
		if (runnable == null)
			return;

		CEditor editor= fEditor;
		if (editor == null)
			return;

		IWorkbenchPartSite site= editor.getSite();
		if (site == null)
			return;

		Shell shell= site.getShell();
		if (shell == null || shell.isDisposed())
			return;

		Display display= shell.getDisplay();
		if (display == null || display.isDisposed())
			return;

		display.asyncExec(runnable);
	}

	/**
	 * Stop reconciling positions.
	 */
	private void stopReconcilingPositions() {
		fRemovedPositions.clear();
		fNOfRemovedPositions= 0;
		fAddedPositions.clear();
	}

	/**
	 * Install this reconciler on the given editor, presenter and highlightings.
	 * @param editor the editor
	 * @param sourceViewer the source viewer
	 * @param presenter the semantic highlighting presenter
	 * @param semanticHighlightings the semantic highlightings
	 * @param highlightings the highlightings
	 */
	public void install(CEditor editor, ISourceViewer sourceViewer, SemanticHighlightingPresenter presenter, SemanticHighlighting[] semanticHighlightings, HighlightingStyle[] highlightings) {
		fPresenter= presenter;
		fSemanticHighlightings= semanticHighlightings;
		fHighlightings= highlightings;

		fEditor= editor;

		if (fEditor != null) {
			fEditor.addReconcileListener(this);
		}
	}

	/**
	 * Uninstall this reconciler from the editor
	 */
	public void uninstall() {
		if (fPresenter != null)
			fPresenter.setCanceled(true);

		if (fEditor != null) {
			fEditor.removeReconcileListener(this);
			fEditor= null;
		}

		fSemanticHighlightings= null;
		fHighlightings= null;
		fPresenter= null;
	}

	/**
	 * Schedule a background job for retrieving the AST and reconciling the Semantic Highlighting model.
	 */
	private void scheduleJob() {
		final ICElement element= fEditor.getInputCElement();

		synchronized (fJobLock) {
			final Job oldJob= fJob;
			if (fJob != null) {
				fJob.cancel();
				fJob= null;
			}
			
			if (element != null) {
				fJob= new Job(CEditorMessages.getString("SemanticHighlighting_job")) { //$NON-NLS-1$
					protected IStatus run(IProgressMonitor monitor) {
						if (oldJob != null) {
							try {
								oldJob.join();
							} catch (InterruptedException e) {
								CUIPlugin.getDefault().log(e);
								return Status.CANCEL_STATUS;
							}
						}
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						IASTTranslationUnit ast= CUIPlugin.getDefault().getASTProvider().getAST(element, ASTProvider.WAIT_YES, monitor);
						reconciled(ast, null, monitor);
						synchronized (fJobLock) {
							// allow the job to be gc'ed
							if (fJob == this)
								fJob= null;
						}
						return Status.OK_STATUS;
					}
				};
//				fJob.setSystem(true);
				fJob.setPriority(Job.DECORATE);
				fJob.schedule();
			}
		}
	}

	/**
	 * Refreshes the highlighting.
	 */
	public void refresh() {
		scheduleJob();
	}
}
