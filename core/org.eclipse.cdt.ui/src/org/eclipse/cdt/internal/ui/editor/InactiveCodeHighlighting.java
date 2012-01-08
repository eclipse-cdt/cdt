/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache;

import org.eclipse.cdt.internal.ui.LineBackgroundPainter;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;

/**
 * Paints code lines disabled by preprocessor directives (#ifdef etc.)
 * with a configurable background color (default light gray).
 * 
 * @see LineBackgroundPainter
 * @since 4.0
 */
public class InactiveCodeHighlighting implements ICReconcilingListener, ITextInputListener {

	/**
	 * Implementation of <code>IRegion</code> that can be reused
	 * by setting the offset and the length.
	 */
	private static class HighlightPosition extends TypedPosition implements IRegion {
		public HighlightPosition(int offset, int length, String type) {
			super(offset, length, type);
		}
		public HighlightPosition(IRegion region, String type) {
			super(region.getOffset(), region.getLength(), type);
		}
	}


	/** The line background painter */
	private LineBackgroundPainter fLineBackgroundPainter;
	/** The key for inactive code positions in the background painter */
	private String fHighlightKey;
	/** The current translation unit */
	private ITranslationUnit fTranslationUnit;
	/** The background job doing the AST parsing */
	private Job fUpdateJob;
	/** The lock for job manipulation */
	private Object fJobLock = new Object();
	/** The editor this is installed on */
	private CEditor fEditor;
	/** The list of currently highlighted positions */
	private List<Position> fInactiveCodePositions= Collections.emptyList();
	private IDocument fDocument;

	/**
	 * Create a highlighter for the given key.
	 * @param highlightKey
	 */
	public InactiveCodeHighlighting(String highlightKey) {
		fHighlightKey= highlightKey;
	}

	/**
	 * Schedule update of the inactive code positions in the background.
	 */
	private void scheduleJob() {
		synchronized (fJobLock) {
			if (fUpdateJob == null) {
				fUpdateJob = new Job(CEditorMessages.InactiveCodeHighlighting_job) { 
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						IStatus result = Status.OK_STATUS;
						if (fTranslationUnit != null) {
							final ASTProvider astProvider= CUIPlugin.getDefault().getASTProvider();
							result= astProvider.runOnAST(fTranslationUnit, ASTProvider.WAIT_IF_OPEN, monitor, new ASTCache.ASTRunnable() {
								@Override
								public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
									reconciled(ast, true, monitor);
									return Status.OK_STATUS;
								}
							});
						}
						if (monitor.isCanceled()) {
							result = Status.CANCEL_STATUS;
						}
						return result;
					}
				};
				fUpdateJob.setPriority(Job.DECORATE);
			}
			if (fUpdateJob.getState() == Job.NONE) {
				fUpdateJob.schedule();
			}
		}
	}

	/**
	 * Install this highlighting on the given editor and line background painter.
	 * 
	 * @param editor
	 * @param lineBackgroundPainter
	 */
	public void install(CEditor editor, LineBackgroundPainter lineBackgroundPainter) {
		assert fEditor == null;
		assert editor != null && lineBackgroundPainter != null;
		fEditor= editor;
		fLineBackgroundPainter= lineBackgroundPainter;
		ICElement cElement= fEditor.getInputCElement();
		if (cElement instanceof ITranslationUnit) {
			fTranslationUnit = (ITranslationUnit)cElement;
		} else {
			fTranslationUnit = null;
		}
		fDocument= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		fEditor.getViewer().addTextInputListener(this);
		fEditor.addReconcileListener(this);
	}

	/**
	 * Uninstall this highlighting from the editor. Does nothing if already uninstalled.
	 */
	public void uninstall() {
		synchronized (fJobLock) {
			if (fUpdateJob != null && fUpdateJob.getState() == Job.RUNNING) {
				fUpdateJob.cancel();
			}
		}
		if (fLineBackgroundPainter != null && !fLineBackgroundPainter.isDisposed()) {
			fLineBackgroundPainter.removeHighlightPositions(fInactiveCodePositions);
			fInactiveCodePositions= Collections.emptyList();
			fLineBackgroundPainter= null;
		}
		if (fEditor != null) {
			fEditor.removeReconcileListener(this);
			if (fEditor.getViewer() != null) {
				fEditor.getViewer().removeTextInputListener(this);
			}
			fEditor= null;
			fTranslationUnit= null;
			fDocument= null;
		}
	}

	/**
	 * Force refresh.
	 */
	public void refresh() {
		scheduleJob();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#aboutToBeReconciled()
	 */
	@Override
	public void aboutToBeReconciled() {
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(IASTTranslationUnit, boolean, IProgressMonitor)
	 */
	@Override
	public void reconciled(IASTTranslationUnit ast, final boolean force, IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) {
			return;
		}
		final List<Position> newInactiveCodePositions= collectInactiveCodePositions(ast);
		Runnable updater = new Runnable() {
			@Override
			public void run() {
				if (fEditor != null && fLineBackgroundPainter != null && !fLineBackgroundPainter.isDisposed()) {
					fLineBackgroundPainter.replaceHighlightPositions(fInactiveCodePositions, newInactiveCodePositions);
					fInactiveCodePositions= newInactiveCodePositions;
				}
			}
		};
		if (fEditor != null) {
			Display.getDefault().asyncExec(updater);
		}
	}

	/**
	 * Collect source positions of preprocessor-hidden branches 
	 * in the given translation unit.
	 * 
	 * @param translationUnit  the {@link IASTTranslationUnit}, may be <code>null</code>
	 * @return a {@link List} of {@link IRegion}s
	 */
	private List<Position> collectInactiveCodePositions(IASTTranslationUnit translationUnit) {
		if (translationUnit == null) {
			return Collections.emptyList();
		}
		String fileName = translationUnit.getFilePath();
		if (fileName == null) {
			return Collections.emptyList();
		}
		List<Position> positions = new ArrayList<Position>();
		int inactiveCodeStart = -1;
		boolean inInactiveCode = false;
		Stack<Boolean> inactiveCodeStack = new Stack<Boolean>();

		IASTPreprocessorStatement[] preprocStmts = translationUnit.getAllPreprocessorStatements();

		for (IASTPreprocessorStatement statement : preprocStmts) {
			IASTFileLocation floc= statement.getFileLocation();
			if (floc == null || !fileName.equals(floc.getFileName())) {
				// preprocessor directive is from a different file
				continue;
			}
			if (statement instanceof IASTPreprocessorIfStatement) {
				IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = floc.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfdefStatement) {
				IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifdefStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = floc.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfndefStatement) {
				IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifndefStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = floc.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorElseStatement) {
				IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement)statement;
				if (!elseStmt.taken() && !inInactiveCode) {
					inactiveCodeStart = floc.getNodeOffset();
					inInactiveCode = true;
				} else if (elseStmt.taken() && inInactiveCode) {
					int inactiveCodeEnd = floc.getNodeOffset();
					positions.add(createHighlightPosition(inactiveCodeStart, inactiveCodeEnd, false, fHighlightKey));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorElifStatement) {
				IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement)statement;
				if (!elifStmt.taken() && !inInactiveCode) {
					inactiveCodeStart = floc.getNodeOffset();
					inInactiveCode = true;
				} else if (elifStmt.taken() && inInactiveCode) {
					int inactiveCodeEnd = floc.getNodeOffset();
					positions.add(createHighlightPosition(inactiveCodeStart, inactiveCodeEnd, false, fHighlightKey));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorEndifStatement) {
				try {
					boolean wasInInactiveCode = inactiveCodeStack.pop().booleanValue();
					if (inInactiveCode && !wasInInactiveCode) {
						int inactiveCodeEnd = floc.getNodeOffset() + floc.getNodeLength();
						positions.add(createHighlightPosition(inactiveCodeStart, inactiveCodeEnd, true, fHighlightKey));
					}
					inInactiveCode = wasInInactiveCode;
				}
		 		catch( EmptyStackException e) {}
			}
		}
		if (inInactiveCode) {
			// handle unterminated #if - http://bugs.eclipse.org/255018
			int inactiveCodeEnd = fDocument.getLength();
			positions.add(createHighlightPosition(inactiveCodeStart, inactiveCodeEnd, true, fHighlightKey));
		}
		return positions;
	}

	/**
	 * Create a highlight position aligned to start at a line offset. The region's start is
	 * decreased to the line offset, and the end offset decreased to the line start if
	 * <code>inclusive</code> is <code>false</code>. 
	 * 
	 * @param startOffset  the start offset of the region to align
	 * @param endOffset  the (exclusive) end offset of the region to align
	 * @param inclusive whether  the last line should be included or not
	 * @param key  the highlight key
	 * @return a position aligned for background highlighting
	 */
	private HighlightPosition createHighlightPosition(int startOffset, int endOffset, boolean inclusive, String key) {
		final IDocument document= fDocument;
		try {
			if (document != null) {
				int start= document.getLineOfOffset(startOffset);
				int end= document.getLineOfOffset(endOffset);
				startOffset= document.getLineOffset(start);
				if (!inclusive) {
					endOffset= document.getLineOffset(end);
				}
			}
		} catch (BadLocationException x) {
			// concurrent modification?
		}
		return new HighlightPosition(startOffset, endOffset - startOffset, key);
	}
	
	/*
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
	 */
	@Override
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (fEditor != null && fLineBackgroundPainter != null && !fLineBackgroundPainter.isDisposed()) {
			fLineBackgroundPainter.removeHighlightPositions(fInactiveCodePositions);
			fInactiveCodePositions= Collections.emptyList();
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
	 */
	@Override
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		fDocument= newInput;
	}

}
