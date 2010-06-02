/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

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
public class CSourceViewerDecorationSupport extends SourceViewerDecorationSupport {

	/** The key to use for the {@link LineBackgroundPainter} */
	private static final String INACTIVE_CODE_KEY = "inactiveCode"; //$NON-NLS-1$

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
	/** The editor we are associated with */
	private CEditor fEditor;
	/** The inactive code highlighting */
	private InactiveCodeHighlighting fInactiveCodeHighlighting;

	/**
	 * Inherited constructor.
	 * 
	 * @param sourceViewer
	 * @param overviewRuler
	 * @param annotationAccess
	 * @param sharedTextColors
	 */
	CSourceViewerDecorationSupport(
		CEditor editor,
		ISourceViewer sourceViewer,
		IOverviewRuler overviewRuler,
		IAnnotationAccess annotationAccess,
		ISharedTextColors sharedTextColors) {
		super(sourceViewer, overviewRuler, annotationAccess, sharedTextColors);
		fEditor = editor;
		// we have to save our own references, because super class members are all private
		fViewer = sourceViewer;
		fSharedColors = sharedTextColors;
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
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
				showInactiveCodePositions(true);
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
			fLineBackgroundPainter.setBackgroundColor(INACTIVE_CODE_KEY, getColor(fInactiveCodeColorKey));
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
	@Override
	public void install(IPreferenceStore store) {
		super.install(store);
		fPrefStore = store;
		if (isCLPActive()) {
			showCLP();
		}
		if (isInactiveCodePositionsActive()) {
			showInactiveCodePositions(false);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#uninstall()
	 */
	@Override
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
				fLineBackgroundPainter.setBackgroundColor(INACTIVE_CODE_KEY, getColor(fInactiveCodeColorKey));
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
			if (fInactiveCodeHighlighting != null) {
				fInactiveCodeHighlighting.uninstall();
				fInactiveCodeHighlighting= null;
			}
			if (fViewer instanceof ITextViewerExtension2) {
				((ITextViewerExtension2)fViewer).removePainter(fLineBackgroundPainter);
			}
			fLineBackgroundPainter.deactivate(true);
			fLineBackgroundPainter.dispose();
			fLineBackgroundPainter = null;
		}
	}

	/**
	 * Show inactive code positions.
	 * 
	 * @param refresh trigger a refresh of the positions
	 */
	private void showInactiveCodePositions(boolean refresh) {
		installLineBackgroundPainter();
		if (fLineBackgroundPainter != null) {
			fInactiveCodeHighlighting= new InactiveCodeHighlighting(INACTIVE_CODE_KEY);
			fInactiveCodeHighlighting.install(fEditor, fLineBackgroundPainter);
			if (refresh) {
				fInactiveCodeHighlighting.refresh();
			}
		}
	}

	/**
	 * Hide inactive code positions.
	 */
	private void hideInactiveCodePositions() {
		if (fLineBackgroundPainter != null) {
			if (fInactiveCodeHighlighting != null) {
				fInactiveCodeHighlighting.uninstall();
				fInactiveCodeHighlighting= null;
			}
			if (!isCLPActive()) {
				uninstallLineBackgroundPainter();
			}
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.SourceViewerDecorationSupport#setCursorLinePainterPreferenceKeys(java.lang.String, java.lang.String)
	 */
	@Override
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
