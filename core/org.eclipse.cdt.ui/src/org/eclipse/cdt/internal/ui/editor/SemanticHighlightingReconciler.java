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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTClassVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache;

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
		/** The semantic token */
		private SemanticToken fToken= new SemanticToken();
		private int fMinLocation;
		
		public PositionCollector(boolean visitImplicitNames) {
			fMinLocation= -1;
			shouldVisitTranslationUnit= true;
			shouldVisitNames= true;
			shouldVisitDeclarations= true;
			shouldVisitExpressions= true;
			shouldVisitStatements= true;
			shouldVisitDeclarators= true;
			shouldVisitNamespaces= true;
			shouldVisitVirtSpecifiers= true;
			shouldVisitImplicitNames = visitImplicitNames;
			shouldVisitImplicitNameAlternates = visitImplicitNames;
		}

		@Override
		public int visit(IASTTranslationUnit tu) {
			// Visit macro definitions.
			IASTPreprocessorMacroDefinition[] macroDefs= tu.getMacroDefinitions();
			for (IASTPreprocessorMacroDefinition macroDef : macroDefs) {
				if (macroDef.isPartOfTranslationUnitFile()) {
					visitNode(macroDef.getName());
				}
			}
			fMinLocation= -1;

			// Visit macro expansions.
			IASTPreprocessorMacroExpansion[] macroExps= tu.getMacroExpansions();
			for (IASTPreprocessorMacroExpansion macroExp : macroExps) {
				if (macroExp.isPartOfTranslationUnitFile()) {
					IASTName macroRef= macroExp.getMacroReference();
					visitNode(macroRef);
					IASTName[] nestedMacroRefs= macroExp.getNestedMacroReferences();
					for (IASTName nestedMacroRef : nestedMacroRefs) {
						visitNode(nestedMacroRef);
					}
				}
			}
			fMinLocation= -1;

			// Visit ordinary code.
			return super.visit(tu);
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (!declaration.isPartOfTranslationUnitFile()) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
//			if (!shouldVisitCatchHandlers && declaration instanceof IASTFunctionDefinition) {
//				shouldVisitCatchHandlers= true;
//				IASTFunctionDefinition functionDef= (IASTFunctionDefinition) declaration;
//				ICPPASTFunctionTryBlockDeclarator declarator= (ICPPASTFunctionTryBlockDeclarator) functionDef.getDeclarator();
//				ICPPASTCatchHandler[] catchHandlers= declarator.getCatchHandlers();
//				for (ICPPASTCatchHandler catchHandler : catchHandlers) {
//					catchHandler.accept(this);
//				}
//			}
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(ICPPASTNamespaceDefinition namespace) {
			if (!namespace.isPartOfTranslationUnitFile()) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTDeclarator declarator) {
//			if (declarator instanceof ICPPASTFunctionTryBlockDeclarator) {
//				shouldVisitCatchHandlers= false;
//			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTStatement statement) {
//			if (!shouldVisitCatchHandlers && statement instanceof ICPPASTCatchHandler) {
//				return PROCESS_SKIP;
//			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTName name) {
			if (visitNode(name)) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(ICPPASTVirtSpecifier virtSpecifier) {
			visitNode(virtSpecifier);
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(ICPPASTClassVirtSpecifier classVirtSpecifier) {
			visitNode(classVirtSpecifier);
			return PROCESS_CONTINUE;
		}
		
		private boolean visitNode(IASTNode node) {
			boolean consumed= false;
			fToken.update(node);
			for (int i= 0, n= fJobSemanticHighlightings.length; i < n; ++i) {
				SemanticHighlighting semanticHighlighting= fJobSemanticHighlightings[i];
				if (fJobHighlightings[i].isEnabled() && semanticHighlighting.consumes(fToken)) {
					if (node instanceof IASTName) {
						addNameLocation((IASTName) node, fJobHighlightings[i]);
					} else {
						addNodeLocation(node.getFileLocation(), fJobHighlightings[i]);
					}
					consumed= true;
					break;
				}
			}
			fToken.clear();
			return consumed;
		}

		/**
		 * Add the a location range for the given name.
		 * 
		 * @param name  The name
		 * @param highlighting The highlighting
		 */
		private void addNameLocation(IASTName name, HighlightingStyle highlightingStyle) {
			IASTImageLocation imageLocation= name.getImageLocation();
			if (imageLocation != null) {
				if (imageLocation.getLocationKind() != IASTImageLocation.MACRO_DEFINITION) {
					int offset= imageLocation.getNodeOffset();
					if (offset >= fMinLocation) {
						int length= imageLocation.getNodeLength();
						if (offset >= 0 && length > 0) {
							fMinLocation= offset + length;
							addPosition(offset, length, highlightingStyle);
						}
					}
				}
			} else {
				// Fallback in case no image location available.
				IASTNodeLocation[] nodeLocations= name.getNodeLocations();
				if (nodeLocations.length == 1 && !(nodeLocations[0] instanceof IASTMacroExpansionLocation)) {
					addNodeLocation(nodeLocations[0], highlightingStyle);
				}
			}
		}

		/**
		 * Adds the a location range for the given highlighting.
		 * 
		 * @param nodeLocation  The node location
		 * @param highlighting The highlighting
		 */
		private void addNodeLocation(IASTNodeLocation nodeLocation, HighlightingStyle highlightingStyle) {
			if (nodeLocation == null) {
				return;
			}
			int offset= nodeLocation.getNodeOffset();
			if (offset >= fMinLocation) {
				int length= nodeLocation.getNodeLength();
				if (offset > -1 && length > 0) {
					fMinLocation= offset + length;
					addPosition(offset, length, highlightingStyle);
				}
			}
		}

		/**
		 * Adds a position with the given range and highlighting iff it does not exist already.
		 * 
		 * @param offset The range offset
		 * @param length The range length
		 * @param highlighting The highlighting
		 */
		private void addPosition(int offset, int length, HighlightingStyle highlightingStyle) {
			boolean isExisting= false;
			// TODO: use binary search
			for (int i= 0, n= fRemovedPositions.size(); i < n; i++) {
				HighlightedPosition position= fRemovedPositions.get(i);
				if (position == null)
					continue;
				if (position.isEqual(offset, length, highlightingStyle)) {
					isExisting= true;
					fRemovedPositions.set(i, null);
					fNOfRemovedPositions--;
					break;
				}
			}

			if (!isExisting) {
				HighlightedPosition position= fJobPresenter.createHighlightedPosition(offset, length, highlightingStyle);
				fAddedPositions.add(position);
			}
		}

	}

	/** The C editor this semantic highlighting reconciler is installed on */
	private CEditor fEditor;
	/** The semantic highlighting presenter */
	protected SemanticHighlightingPresenter fPresenter;
	/** Semantic highlightings */
	protected SemanticHighlighting[] fSemanticHighlightings;
	/** Highlightings */
	private HighlightingStyle[] fHighlightings;

	/** Background job's added highlighted positions */
	protected List<HighlightedPosition> fAddedPositions= new ArrayList<HighlightedPosition>();
	/** Background job's removed highlighted positions */
	protected List<HighlightedPosition> fRemovedPositions= new ArrayList<HighlightedPosition>();
	/** Number of removed positions */
	protected int fNOfRemovedPositions;

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

	/**
	 * The semantic highlighting presenter - cache for background thread, only valid during
	 * {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)}
	 */
	protected SemanticHighlightingPresenter fJobPresenter;
	/**
	 * Semantic highlightings - cache for background thread, only valid during
	 * {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)}
	 */
	protected SemanticHighlighting[] fJobSemanticHighlightings;
	/**
	 * Highlightings - cache for background thread, only valid during
	 * {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)}
	 */
	private HighlightingStyle[] fJobHighlightings;

	@Override
	public void aboutToBeReconciled() {
		// Do nothing
	}

	@Override
	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		// Ensure at most one thread can be reconciling at any time.
		synchronized (fReconcileLock) {
			if (fIsReconciling)
				return;
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
			
			PositionCollector collector= new PositionCollector(requiresImplicitNames());

			startReconcilingPositions();
			
			if (!fJobPresenter.isCanceled())
				reconcilePositions(ast, collector);
			
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

	private boolean requiresImplicitNames() {
		for (int i = 0; i < fSemanticHighlightings.length; i++) {
			SemanticHighlighting sh = fSemanticHighlightings[i];
			if (sh.requiresImplicitNames() && fHighlightings[i].isEnabled()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Starts reconciling positions.
	 */
	protected void startReconcilingPositions() {
		fJobPresenter.addAllPositions(fRemovedPositions);
		fNOfRemovedPositions= fRemovedPositions.size();
	}

	/**
	 * Reconciles positions based on the AST.
	 *
	 * @param ast the AST
	 * @param visitor the AST visitor
	 */
	private void reconcilePositions(IASTTranslationUnit ast, PositionCollector visitor) {
		ast.accept(visitor);
		List<HighlightedPosition> oldPositions= fRemovedPositions;
		List<HighlightedPosition> newPositions= new ArrayList<HighlightedPosition>(fNOfRemovedPositions);
		for (int i= 0, n= oldPositions.size(); i < n; i ++) {
			HighlightedPosition current= oldPositions.get(i);
			if (current != null)
				newPositions.add(current);
		}
		fRemovedPositions= newPositions;
		// Positions need to be sorted by ascending offset
		Collections.sort(fAddedPositions, new Comparator<Position>() {
			@Override
			public int compare(final Position p1, final Position p2) {
				return p1.getOffset() - p2.getOffset();
			}
		});
	}

	/**
	 * Updates the presentation.
	 *
	 * @param textPresentation the text presentation
	 * @param addedPositions the added positions
	 * @param removedPositions the removed positions
	 */
	protected void updatePresentation(TextPresentation textPresentation, List<HighlightedPosition> addedPositions, List<HighlightedPosition> removedPositions) {
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
	 * Stops reconciling positions.
	 */
	protected void stopReconcilingPositions() {
		fRemovedPositions.clear();
		fNOfRemovedPositions= 0;
		fAddedPositions.clear();
	}

	/**
	 * Installs this reconciler on the given editor, presenter and highlightings.
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
	 * Uninstalls this reconciler from the editor
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
	 * Schedules a background job for retrieving the AST and reconciling the Semantic Highlighting model.
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
				fJob= new Job(CEditorMessages.SemanticHighlighting_job) { 
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						if (oldJob != null) {
							try {
								oldJob.join();
							} catch (InterruptedException e) {
								CUIPlugin.log(e);
								return Status.CANCEL_STATUS;
							}
						}
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						
						final Job me= this;
						ASTProvider astProvider= CUIPlugin.getDefault().getASTProvider();
						IStatus status= astProvider.runOnAST(element, ASTProvider.WAIT_IF_OPEN, monitor, new ASTCache.ASTRunnable() {
							@Override
							public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
								reconciled(ast, true, monitor);
								synchronized (fJobLock) {
									// allow the job to be gc'ed
									if (fJob == me)
										fJob= null;
								}
								return Status.OK_STATUS;
							}
						});
						return status;
					}
				};
//				fJob.setSystem(true);
				fJob.setPriority(Job.SHORT);
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
