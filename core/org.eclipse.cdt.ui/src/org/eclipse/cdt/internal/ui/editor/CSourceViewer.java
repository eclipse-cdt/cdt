/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;


/**
 * Source viewer for C/C++ et al.
 */
public class CSourceViewer extends ProjectionViewer implements IPropertyChangeListener {

    /** Show outline operation id. */
    public static final int SHOW_OUTLINE= 101;
    /** Show type hierarchy operation id. */
    public static final int SHOW_HIERARCHY= 102;
    /** Show macro explorer operation id. */
    public static final int SHOW_MACRO_EXPLORER= 103;
    
    /** Presents outline. */
    private IInformationPresenter fOutlinePresenter;
    /** Presents type hierarchy. */
    private IInformationPresenter fHierarchyPresenter;
    /** Presents macro explorer. */
    private IInformationPresenter fMacroExplorationPresenter;

	/**
	 * This viewer's foreground color.
	 * @since 4.0
	 */
	private Color fForegroundColor;
	/**
	 * The viewer's background color.
	 * @since 4.0
	 */
	private Color fBackgroundColor;
	/**
	 * This viewer's selection foreground color.
	 * @since 4.0
	 */
	private Color fSelectionForegroundColor;
	/**
	 * The viewer's selection background color.
	 * @since 4.0
	 */
	private Color fSelectionBackgroundColor;
	/**
	 * The preference store.
	 *
	 * @since 4.0
	 */
	private IPreferenceStore fPreferenceStore;
	/**
	 * Is this source viewer configured?
	 *
	 * @since 4.0
	 */
	private boolean fIsConfigured;

	/**
	 * Whether to delay setting the visual document until the projection has been computed.
	 * <p>
	 * Added for performance optimization.
	 * </p>
	 * @see #prepareDelayedProjection()
	 * @since 4.0
	 */
	private boolean fIsSetVisibleDocumentDelayed;
	/**
	 * Whether projection mode was enabled when switching to segmented mode.
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=195808
	 */
	private boolean fWasProjectionMode;
	
	/**
	 * The configured indent width.
	 */
	private int fIndentWidth= 4;
	/**
	 * Flag indicating whether to use spaces exclusively for indentation.
	 */
	private boolean fUseSpaces;

	/**
     * Creates new source viewer. 
     * @param parent
	 * @param ruler
	 * @param overviewRuler
	 * @param isOverviewRulerShowing
	 * @param styles
	 * @param store
	 */
    public CSourceViewer(
    		Composite parent,
    		IVerticalRuler ruler,
    		IOverviewRuler overviewRuler,
    		boolean isOverviewRulerShowing,
    		int styles,
    		IPreferenceStore store) {
		super(parent, ruler, overviewRuler, isOverviewRulerShowing, styles);
        setPreferenceStore(store);
	}
    
	public IContentAssistant getContentAssistant() {
		return fContentAssistant;
	}
	
	/*
	 * @see ISourceViewer#configure(SourceViewerConfiguration)
	 */
	@Override
	public void configure(SourceViewerConfiguration configuration) {
		// Prevent access to colors disposed in unconfigure().
		StyledText textWidget= getTextWidget();
		if (textWidget != null && !textWidget.isDisposed()) {
			Color foregroundColor= textWidget.getForeground();
			if (foregroundColor != null && foregroundColor.isDisposed())
				textWidget.setForeground(null);
			Color backgroundColor= textWidget.getBackground();
			if (backgroundColor != null && backgroundColor.isDisposed())
				textWidget.setBackground(null);
		}

		if (configuration instanceof CSourceViewerConfiguration) {
			CSourceViewerConfiguration cConfiguration= (CSourceViewerConfiguration)configuration;
			cConfiguration.resetScanners();
		}
		
		super.configure(configuration);

		if (configuration instanceof CSourceViewerConfiguration) {
			CSourceViewerConfiguration cConfiguration= (CSourceViewerConfiguration)configuration;
			fOutlinePresenter= cConfiguration.getOutlinePresenter(this);
			if (fOutlinePresenter != null)
				fOutlinePresenter.install(this);
			fHierarchyPresenter= cConfiguration.getHierarchyPresenter(this);
			if (fHierarchyPresenter != null) 
				fHierarchyPresenter.install(this);
			fMacroExplorationPresenter= cConfiguration.getMacroExplorationPresenter(this);
			if (fMacroExplorationPresenter != null) {
				fMacroExplorationPresenter.install(this);
			}
			String[] defaultIndentPrefixes= (String[])fIndentChars.get(IDocument.DEFAULT_CONTENT_TYPE);
			if (defaultIndentPrefixes != null && defaultIndentPrefixes.length > 0) {
				final int indentWidth= cConfiguration.getIndentWidth(this);
				final boolean useSpaces= cConfiguration.useSpacesOnly(this);
				configureIndentation(indentWidth, useSpaces);
			}
		}
		if (fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(this);
			initializeViewerColors();
			// init flag here in case we start in segmented mode
			fWasProjectionMode= fPreferenceStore.getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		}

		fIsConfigured= true;
	}

	protected void initializeViewerColors() {
		if (fPreferenceStore != null) {

			StyledText styledText= getTextWidget();

			// ----------- foreground color --------------------
			Color color= fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, styledText.getDisplay());
			styledText.setForeground(color);

			if (fForegroundColor != null)
				fForegroundColor.dispose();

			fForegroundColor= color;

			// ---------- background color ----------------------
			color= fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);

			if (fBackgroundColor != null)
				fBackgroundColor.dispose();

			fBackgroundColor= color;

			// ----------- selection foreground color --------------------
			color= fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR)
				? null
				: createColor(fPreferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, styledText.getDisplay());
			styledText.setSelectionForeground(color);

			if (fSelectionForegroundColor != null)
				fSelectionForegroundColor.dispose();

			fSelectionForegroundColor= color;

			// ---------- selection background color ----------------------
			color= fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR)
				? null
				: createColor(fPreferenceStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, styledText.getDisplay());
			styledText.setSelectionBackground(color);

			if (fSelectionBackgroundColor != null)
				fSelectionBackgroundColor.dispose();

			fSelectionBackgroundColor= color;
		}
    }

    /**
     * Creates a color from the information stored in the given preference store.
     * Returns <code>null</code> if there is no such information available.
     *
     * @param store the store to read from
     * @param key the key used for the lookup in the preference store
     * @param display the display used create the color
     * @return the created color according to the specification in the preference store
     */
    private Color createColor(IPreferenceStore store, String key, Display display) {

        RGB rgb= null;

        if (store.contains(key)) {

            if (store.isDefault(key))
                rgb= PreferenceConverter.getDefaultColor(store, key);
            else
                rgb= PreferenceConverter.getColor(store, key);

            if (rgb != null)
                return new Color(display, rgb);
        }

        return null;
    }

    /*
     * @see org.eclipse.jface.text.source.SourceViewer#unconfigure()
     */
    @Override
	public void unconfigure() {
        if (fOutlinePresenter != null) {
            fOutlinePresenter.uninstall();  
            fOutlinePresenter= null;
        }
        if (fHierarchyPresenter != null) {
        	fHierarchyPresenter.uninstall();  
        	fHierarchyPresenter= null;
        }
        if (fMacroExplorationPresenter != null) {
        	fMacroExplorationPresenter.uninstall();  
        	fMacroExplorationPresenter= null;
        }
		if (fForegroundColor != null) {
			fForegroundColor.dispose();
			fForegroundColor= null;
		}
		if (fBackgroundColor != null) {
			fBackgroundColor.dispose();
			fBackgroundColor= null;
		}

		if (fPreferenceStore != null)
			fPreferenceStore.removePropertyChangeListener(this);

       super.unconfigure();

		fIsConfigured= false;
    }

	/*
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property= event.getProperty();
		if (AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR.equals(property)
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR.equals(property)
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR.equals(property)
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR.equals(property))
		{
			initializeViewerColors();
		}
	}

	/**
	 * Sets the preference store on this viewer.
	 *
	 * @param store the preference store
	 *
	 * @since 4.0
	 */
	public void setPreferenceStore(IPreferenceStore store) {
		if (fIsConfigured && fPreferenceStore != null)
			fPreferenceStore.removePropertyChangeListener(this);

		fPreferenceStore= store;

		if (fIsConfigured && fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(this);
			initializeViewerColors();
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#createControl(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected void createControl(Composite parent, int styles) {

		// Use LEFT_TO_RIGHT unless otherwise specified.
		if ((styles & SWT.RIGHT_TO_LEFT) == 0 && (styles & SWT.LEFT_TO_RIGHT) == 0)
			styles |= SWT.LEFT_TO_RIGHT;
			
		super.createControl(parent, styles);
	}

	/*
     * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
	 */
    @Override
	public void doOperation(int operation) {

		if (getTextWidget() == null) {
			return;
		}
		switch (operation) {
            case SHOW_OUTLINE:
                fOutlinePresenter.showInformation();
                return;
            case SHOW_HIERARCHY:
            	fHierarchyPresenter.showInformation();
            	return;
            case SHOW_MACRO_EXPLORER:
            	fMacroExplorationPresenter.showInformation();
		}
		super.doOperation(operation);
	}

    /*
     * @see org.eclipse.jface.text.source.projection.ProjectionViewer#canDoOperation(int)
     */
    @Override
	public boolean canDoOperation(int operation) {
		switch (operation) {
        case SHOW_OUTLINE:
            return fOutlinePresenter != null;
        case SHOW_HIERARCHY:
        	return fHierarchyPresenter != null;
        case SHOW_MACRO_EXPLORER:
        	return fMacroExplorationPresenter != null;
		}
        return super.canDoOperation(operation);
    }

	/**
	 * Prepend given listener to the list of presentation listeners
	 * 
	 * @param listener  The listener to be added.
	 * 
	 * @see TextViewer#addTextPresentationListener(ITextPresentationListener)
	 * @since 4.0
	 */
	public void prependTextPresentationListener(ITextPresentationListener listener) {
		Assert.isNotNull(listener);

		@SuppressWarnings("unchecked") // using list from base class
		List<ITextPresentationListener> textPresentationListeners= fTextPresentationListeners;
		
		if (textPresentationListeners == null) 
			fTextPresentationListeners= textPresentationListeners= new ArrayList<ITextPresentationListener>();

		textPresentationListeners.remove(listener);
		textPresentationListeners.add(0, listener);
	}

	/**
	 * Delays setting the visual document until after the projection has been computed.
	 * This method must only be called before the document is set on the viewer.
	 * <p>
	 * This is a performance optimization to reduce the computation of
	 * the text presentation triggered by <code>setVisibleDocument(IDocument)</code>.
	 * </p>
	 * 
	 * @see #setVisibleDocument(IDocument)
	 * @since 4.0
	 */
	void prepareDelayedProjection() {
		Assert.isTrue(!fIsSetVisibleDocumentDelayed);
		fIsSetVisibleDocumentDelayed= true;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This is a performance optimization to reduce the computation of
	 * the text presentation triggered by {@link #setVisibleDocument(IDocument)}
	 * </p>
	 * @since 4.0
	 */
	@Override
	protected void setVisibleDocument(IDocument document) {
		if (fIsSetVisibleDocumentDelayed) {
			fIsSetVisibleDocumentDelayed= false;
			IDocument previous= getVisibleDocument();
			enableProjection(); // will set the visible document if anything is folded
			IDocument current= getVisibleDocument();
			// if the visible document was not replaced, continue as usual
			if (current != null && current != previous)
				return;
		}
		
		super.setVisibleDocument(document);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Performance optimization: since we know at this place
	 * that none of the clients expects the given range to be
	 * untouched we reuse the given range as return value.
	 * </p>
	 */
	@Override
	protected StyleRange modelStyleRange2WidgetStyleRange(StyleRange range) {
		IRegion region= modelRange2WidgetRange(new Region(range.start, range.length));
		if (region != null) {
			// don't clone the style range, but simply reuse it.
			range.start= region.getOffset();
			range.length= region.getLength();
			return range;
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.projection.ProjectionViewer#setVisibleRegion(int, int)
	 */
	@Override
	public void setVisibleRegion(int start, int length) {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=195808
		if (!fWasProjectionMode && isProjectionMode()) {
			fWasProjectionMode= true;
		}
		super.setVisibleRegion(start, length);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.projection.ProjectionViewer#resetVisibleRegion()
	 */
	@Override
	public void resetVisibleRegion() {
		super.resetVisibleRegion();
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=195808
		if (fWasProjectionMode) {
			fWasProjectionMode= false;
			enableProjection();
		}
	}


	/**
	 * Configure the indentation mode for this viewer.
	 * 
	 * @param indentWidth  the indentation width
	 * @param useSpaces  if <code>true</code>, only spaces are used for indentation
	 */
	public void configureIndentation(int indentWidth, boolean useSpaces) {
		fIndentWidth= indentWidth;
		fUseSpaces= useSpaces;
	}

	/*
	 * @see org.eclipse.jface.text.TextViewer#shift(boolean, boolean, boolean)
	 */
	@Override
	protected void shift(boolean useDefaultPrefixes, boolean right, boolean ignoreWhitespace) {
		if (!useDefaultPrefixes) {
			// simple shift case
			adjustIndent(right, fIndentWidth, fUseSpaces);
			return;
		}
		super.shift(useDefaultPrefixes, right, ignoreWhitespace);
	}

	/**
	 * Increase/decrease indentation of current selection.
	 * 
	 * @param increase  if <code>true</code>, indent is increased by one unit
	 * @param shiftWidth  width in spaces of one indent unit
	 * @param useSpaces  if <code>true</code>, only spaces are used for indentation
	 */
	protected void adjustIndent(boolean increase, int shiftWidth, boolean useSpaces) {
		if (fUndoManager != null) {
			fUndoManager.beginCompoundChange();
		}
		IDocument d= getDocument();
		DocumentRewriteSession rewriteSession= null;
		try {
			if (d instanceof IDocumentExtension4) {
				IDocumentExtension4 extension= (IDocumentExtension4) d;
				rewriteSession= extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
			}

			Point selection= getSelectedRange();

			// perform the adjustment
			int tabWidth= getTextWidget().getTabs();
			int startLine= d.getLineOfOffset(selection.x);
			int endLine= selection.y == 0 ? startLine : d.getLineOfOffset(selection.x + selection.y - 1);
			for (int line= startLine; line <= endLine; ++line) {
				IRegion lineRegion= d.getLineInformation(line);
				String indent= IndentUtil.getCurrentIndent(d, line, false);
				int indentWidth= IndentUtil.computeVisualLength(indent, tabWidth);
				int newIndentWidth= Math.max(0, indentWidth + (increase ? shiftWidth : -shiftWidth));
				String newIndent= IndentUtil.changePrefix(indent.trim(), newIndentWidth, tabWidth, useSpaces);
				int commonLen= getCommonPrefixLength(indent, newIndent);
				if (commonLen < Math.max(indent.length(), newIndent.length())) {
					if (commonLen > 0) {
						indent= indent.substring(commonLen);
						newIndent= newIndent.substring(commonLen);
					}
					final int offset= lineRegion.getOffset() + commonLen;
					if (!increase && newIndent.length() > indent.length() && indent.length() > 0) {
						d.replace(offset, indent.length(), ""); //$NON-NLS-1$
						d.replace(offset, 0, newIndent);
					} else {
						d.replace(offset, indent.length(), newIndent);
					}
				}
			}
			
		} catch (BadLocationException x) {
			// ignored
		} finally {
			if (rewriteSession != null) {
				((IDocumentExtension4)d).stopRewriteSession(rewriteSession);
			}
			if (fUndoManager != null) {
				fUndoManager.endCompoundChange();
			}
		}
	}

    /**
     * Compute the length of the common prefix of two strings.
     * 
	 * @param s1
	 * @param s2
	 * @return the length of the common prefix
	 */
	private static int getCommonPrefixLength(String s1, String s2) {
		final int l1= s1.length();
		final int l2= s2.length();
		int i= 0;
		while (i < l1 && i < l2 && s1.charAt(i) == s2.charAt(i)) {
			++i;
		}
		return i;
	}

	/*
     * work around for memory leak in TextViewer$WidgetCommand
     */
    @Override
	protected void updateTextListeners(WidgetCommand cmd) {
        super.updateTextListeners(cmd);
        cmd.preservedText= null;
        cmd.event= null;
        cmd.text= null;
    }
}
