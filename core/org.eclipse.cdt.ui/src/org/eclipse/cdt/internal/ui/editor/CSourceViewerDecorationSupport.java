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
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.LineBackgroundPainter;

/**
 * <code>SourceViewerDecorationSupport</code> with extension(s):
 * <ul>
 *   <li>inactive code painter</li>
 * </ul>
 * 
 * @author anton.leherbauer@windriver.com
 * 
 * @since 4.0
 */
public class CSourceViewerDecorationSupport
	extends SourceViewerDecorationSupport {

	/**
	 * This job takes the current translation unit and produces an
	 * AST  in the background. Upon completion, {@link #inactiveCodePositionsChanged}
	 * is called in the display thread.
	 */
	private class UpdateJob extends Job {

		/**
		 * @param name
		 */
		public UpdateJob(String name) {
			super(name);
			setSystem(true);
			setPriority(Job.DECORATE);
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			IStatus result = Status.OK_STATUS;
			if (fASTTranslationUnit == null && fTranslationUnit != null) {
				try {
					fParseTimeStamp = System.currentTimeMillis();
					int style = ILanguage.AST_SKIP_IF_NO_BUILD_INFO | ILanguage.AST_SKIP_INDEXED_HEADERS;
					fASTTranslationUnit = fTranslationUnit.getLanguage().getASTTranslationUnit(fTranslationUnit, style);
				} catch (CoreException exc) {
					result = exc.getStatus();
				}
			}
			if (monitor.isCanceled() || fViewer == null) {
				result = Status.CANCEL_STATUS;
			} else {
				final List inactiveCodePositions = collectInactiveCodePositions(fASTTranslationUnit);
				Runnable updater = new Runnable() {
					public void run() {
						inactiveCodePositionsChanged(inactiveCodePositions);
					}
				};
				if (fViewer != null && !monitor.isCanceled()) {
					fViewer.getTextWidget().getDisplay().asyncExec(updater);
				}
			}
			return result;
		}

	}

	/**
	 * Implementation of <code>IRegion</code> that can be reused
	 * by setting the offset and the length.
	 */
	private static class ReusableRegion extends Position implements IRegion {
		public ReusableRegion(int offset, int length) {
			super(offset, length);
		}
		public ReusableRegion(IRegion region) {
			super(region.getOffset(), region.getLength());
		}
	}

	/** The preference key for the inactive code highlight color */
	private String fInactiveCodeColorKey;
	/** The preference key for the inactive code highlight enablement */
	private String fInactiveCodeEnableKey;
	/** The generic line background painter instance. */
	private LineBackgroundPainter fLineBackgroundPainter;
	/** The shared colors instance (duplicate of private base class member) */
	private ISharedTextColors fSharedColors;
	/** The preference store (duplicate of private base class member) */
	private IPreferenceStore fPrefStore;
	/** The preference key for the cursor line highlight color (duplicate of private base class member) */
	private String fCLPColorKey;
	/** The preference key for the cursor line highlight enablement (duplicate of private base class member) */
	private String fCLPEnableKey;
	/** The source viewer (duplicate of private base class member) */
	protected ISourceViewer fViewer;
	/** The current translation unit */
	private ITranslationUnit fTranslationUnit;
	/** The corresponding AST translation unit */
	private IASTTranslationUnit fASTTranslationUnit;
	/** The time stamp when the parsing was initiated */
	private long fParseTimeStamp;
	/** The background job doing the AST parsing */
	private Job fUpdateJob;

	/**
	 * Inherited constructor.
	 * 
	 * @param sourceViewer
	 * @param overviewRuler
	 * @param annotationAccess
	 * @param sharedTextColors
	 */
	CSourceViewerDecorationSupport(
		ISourceViewer sourceViewer,
		IOverviewRuler overviewRuler,
		IAnnotationAccess annotationAccess,
		ISharedTextColors sharedTextColors) {
		super(sourceViewer, overviewRuler, annotationAccess, sharedTextColors);
		// we have to save our own reference, because super class members are all private
		fViewer = sourceViewer;
		fSharedColors = sharedTextColors;
	}

	/**
	 * Notify that the associated editor got a new input.
	 * This is currently also used to notify of a reconcilation
	 * to update the inactive code while editing.
	 * 
	 * @param input  the new editor input
	 */
	void editorInputChanged(IEditorInput input) {
		if (fUpdateJob != null) {
			fUpdateJob.cancel();
		}
		fTranslationUnit = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(input);
		fASTTranslationUnit = null;
		if (isInactiveCodePositionsActive()) {
			updateInactiveCodePositions();
		}
	}

	/**
	 * Schedule update of the AST in the background.
	 */
	private void updateInactiveCodePositions() {
		if (fUpdateJob == null) {
			fUpdateJob = new UpdateJob("Update Inactive Code Positions"); //$NON-NLS-1$
		}
		if (fUpdateJob.getState() == Job.NONE) {
			fUpdateJob.schedule();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#dispose()
	 */
	public void dispose() {
		if (fUpdateJob != null) {
			fUpdateJob.cancel();
		}
		fTranslationUnit = null;
		fASTTranslationUnit = null;
		super.dispose();
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String p = event.getProperty();
		if (p.equals(fCLPEnableKey)) {
			if (isCLPActive()) {
				showCLP();
			} else {
				hideCLP();
			}
		} else if (p.equals(fCLPColorKey)) {
			updateCLPColor();
		} else if (p.equals(fInactiveCodeEnableKey)) {
			if (isInactiveCodePositionsActive()) {
				showInactiveCodePositions();
			} else {
				hideInactiveCodePositions();
			}
		} else if (p.equals(fInactiveCodeColorKey)) {
			updateInactiveCodeColor();
		}
		super.handlePreferenceStoreChanged(event);
	}

	/**
	 * Update the color for inactive code positions.
	 */
	private void updateInactiveCodeColor() {
		if (fLineBackgroundPainter != null) {
			fLineBackgroundPainter.setDefaultColor(getColor(fInactiveCodeColorKey));
			if (isInactiveCodePositionsActive()) {
				fLineBackgroundPainter.redraw();
			}
		}
	}

	/**
	 * Update the color for the cursor line highlighting.
	 */
	private void updateCLPColor() {
		if (fLineBackgroundPainter != null) {
			fLineBackgroundPainter.setCursorLineColor(getColor(fCLPColorKey));
			if (isCLPActive()) {
				fLineBackgroundPainter.redraw();
			}
		}
	}

	/**
	 * Hide cursor line highlighting.
	 */
	private void hideCLP() {
		if (fLineBackgroundPainter != null) {
			if (!isInactiveCodePositionsActive()) {
				uninstallLineBackgroundPainter();
			} else {
				fLineBackgroundPainter.enableCursorLine(false);
				fLineBackgroundPainter.redraw();
			}
		}
	}

	/**
	 * Show cursor line highlighting.
	 */
	private void showCLP() {
		installLineBackgroundPainter();
		if (fLineBackgroundPainter != null) {
			fLineBackgroundPainter.enableCursorLine(true);
			fLineBackgroundPainter.redraw();
		}
	}

	/**
	 * @return true if cursor line highlighting is active.
	 */
	private boolean isCLPActive() {
		if (fPrefStore != null) {
			return fPrefStore.getBoolean(fCLPEnableKey);
		}
		return false;
	}

	/**
	 * @return true if inactive code highlighting is active.
	 */
	private boolean isInactiveCodePositionsActive() {
		if (fPrefStore != null) {
			return fPrefStore.getBoolean(fInactiveCodeEnableKey);
		}
		return false;
	}

	/**
	 * Returns the shared color for the given key.
	 * 
	 * @param key the color key string
	 * @return the shared color for the given key
	 */
	private Color getColor(String key) {
		if (fPrefStore != null) {
			RGB rgb = PreferenceConverter.getColor(fPrefStore, key);
			return getColor(rgb);
		}
		return null;
	}

	/**
	 * Returns the shared color for the given RGB.
	 * 
	 * @param rgb the rgb
	 * @return the shared color for the given rgb
	 */
	private Color getColor(RGB rgb) {
		return fSharedColors.getColor(rgb);
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#install(org.eclipse.jface.preference.IPreferenceStore)
	 */
	public void install(IPreferenceStore store) {
		super.install(store);
		fPrefStore = store;
		if (isCLPActive()) {
			showCLP();
		}
		if (isInactiveCodePositionsActive()) {
			showInactiveCodePositions();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#uninstall()
	 */
	public void uninstall() {
		uninstallLineBackgroundPainter();
		super.uninstall();
	}

	/**
	 * Install line background painter (inactive code/cursor line).
	 */
	private void installLineBackgroundPainter() {
		if (fLineBackgroundPainter == null) {
			if (fViewer instanceof ITextViewerExtension2) {
				fLineBackgroundPainter = new LineBackgroundPainter(fViewer);
				fLineBackgroundPainter.setDefaultColor(getColor(fInactiveCodeColorKey));
				fLineBackgroundPainter.setCursorLineColor(getColor(fCLPColorKey));
				fLineBackgroundPainter.enableCursorLine(isCLPActive());
				((ITextViewerExtension2)fViewer).addPainter(fLineBackgroundPainter);
			}
		}
	}

	/**
	 * Uninstall line background painter (inactive code/cursor line).
	 */
	private void uninstallLineBackgroundPainter() {
		if (fLineBackgroundPainter != null) {
			if (fViewer instanceof ITextViewerExtension2) {
				((ITextViewerExtension2)fViewer).removePainter(fLineBackgroundPainter);
				fLineBackgroundPainter.deactivate(true);
				fLineBackgroundPainter.dispose();
				fLineBackgroundPainter = null;
			}
		}
	}

	/**
	 * Show inactive code positions.
	 */
	private void showInactiveCodePositions() {
		installLineBackgroundPainter();
		updateInactiveCodePositions();
	}

	/**
	 * Hide inactive code positions.
	 */
	private void hideInactiveCodePositions() {
		if (fLineBackgroundPainter != null) {
			if (!isCLPActive()) {
				uninstallLineBackgroundPainter();
			} else {
				fLineBackgroundPainter.setHighlightPositions(Collections.EMPTY_LIST);
			}
		}
	}

	private void inactiveCodePositionsChanged(List inactiveCodePositions) {
		if (fLineBackgroundPainter != null) {
			if (!inactiveCodePositions.isEmpty()) {
				IPositionConverter pt = CCorePlugin.getPositionTrackerManager().findPositionConverter(fTranslationUnit.getPath(), fParseTimeStamp);
				if (pt != null) {
					List convertedPositions = new ArrayList(inactiveCodePositions.size());
					for (Iterator iter = inactiveCodePositions.iterator(); iter
							.hasNext();) {
						IRegion pos = (IRegion) iter.next();
						convertedPositions.add(new ReusableRegion(pt.historicToActual(pos)));
					}
					inactiveCodePositions = convertedPositions;
				}
			}
			fLineBackgroundPainter.setHighlightPositions(inactiveCodePositions);
		}
	}

	/**
	 * Collect source positions of preprocessor-hidden branches 
	 * in the given translation unit.
	 * 
	 * @param translationUnit  the {@link IASTTranslationUnit}, may be <code>null</code>
	 * @return a {@link List} of {@link IRegion}s
	 */
	private static List collectInactiveCodePositions(IASTTranslationUnit translationUnit) {
		if (translationUnit == null) {
			return Collections.EMPTY_LIST;
		}
		String fileName = translationUnit.getFilePath();
		if (fileName == null) {
			return Collections.EMPTY_LIST;
		}
		List positions = new ArrayList();
		int inactiveCodeStart = -1;
		boolean inInactiveCode = false;
		Stack inactiveCodeStack = new Stack();
		// TLETODO [performance] This does not look very efficient
		IASTPreprocessorStatement[] preprocStmts = translationUnit.getAllPreprocessorStatements();
		for (int i = 0; i < preprocStmts.length; i++) {
			IASTPreprocessorStatement statement = preprocStmts[i];
			if (!fileName.equals(statement.getContainingFilename())) {
				// preprocessor directive is from a different file
				continue;
			}
			if (statement instanceof IASTPreprocessorIfStatement) {
				IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifStmt.taken()) {
					if (!inInactiveCode) {
						IASTNodeLocation nodeLocation = ifStmt.getNodeLocations()[0];
						inactiveCodeStart = nodeLocation.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfdefStatement) {
				IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifdefStmt.taken()) {
					if (!inInactiveCode) {
						IASTNodeLocation nodeLocation = ifdefStmt.getNodeLocations()[0];
						inactiveCodeStart = nodeLocation.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfndefStatement) {
				IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement)statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifndefStmt.taken()) {
					if (!inInactiveCode) {
						IASTNodeLocation nodeLocation = ifndefStmt.getNodeLocations()[0];
						inactiveCodeStart = nodeLocation.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorElseStatement) {
				IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement)statement;
				if (!elseStmt.taken() && !inInactiveCode) {
					IASTNodeLocation nodeLocation = elseStmt.getNodeLocations()[0];
					inactiveCodeStart = nodeLocation.getNodeOffset();
					inInactiveCode = true;
				} else if (elseStmt.taken() && inInactiveCode) {
					IASTNodeLocation nodeLocation = elseStmt.getNodeLocations()[0];
					int inactiveCodeEnd = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					positions.add(new ReusableRegion(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorElifStatement) {
				IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement)statement;
				if (!elifStmt.taken() && !inInactiveCode) {
					IASTNodeLocation nodeLocation = elifStmt.getNodeLocations()[0];
					inactiveCodeStart = nodeLocation.getNodeOffset();
					inInactiveCode = true;
				} else if (elifStmt.taken() && inInactiveCode) {
					IASTNodeLocation nodeLocation = elifStmt.getNodeLocations()[0];
					int inactiveCodeEnd = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					positions.add(new ReusableRegion(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart));
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorEndifStatement) {
				IASTPreprocessorEndifStatement endifStmt = (IASTPreprocessorEndifStatement)statement;
				boolean wasInInactiveCode = ((Boolean)inactiveCodeStack.pop()).booleanValue();
				if (inInactiveCode && !wasInInactiveCode) {
					IASTNodeLocation nodeLocation = endifStmt.getNodeLocations()[0];
					int inactiveCodeEnd = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
					positions.add(new ReusableRegion(inactiveCodeStart, inactiveCodeEnd - inactiveCodeStart));
				}
				inInactiveCode = wasInInactiveCode;
			}
		}
		if (inInactiveCode) {
			// handle dangling #if?
		}
		return positions;
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#setCursorLinePainterPreferenceKeys(java.lang.String, java.lang.String)
	 */
	public void setCursorLinePainterPreferenceKeys(String enableKey, String colorKey) {
		// this is a dirty hack to override the original cursor line painter
		// and replace it with the generic BackgroundLinePainter
		fCLPEnableKey = enableKey;
		fCLPColorKey = colorKey;
		super.setCursorLinePainterPreferenceKeys(enableKey + "-overridden", colorKey); //$NON-NLS-1$
	}

	/**
	 * Set the preference keys for the inactive code painter.
	 * @param enableKey
	 * @param colorKey
	 */
	public void setInactiveCodePainterPreferenceKeys(String enableKey, String colorKey) {
		fInactiveCodeEnableKey = enableKey;
		fInactiveCodeColorKey = colorKey;
	}
}
