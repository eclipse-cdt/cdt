/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;

/**
 * Source viewer for C/C++ et al.
 */
public class CSourceViewer extends ProjectionViewer implements IPropertyChangeListener {

    /** Show outline operation id. */
    public static final int SHOW_OUTLINE = 101;
    public static final int SHOW_HIERARCHY = 102;
    
    /** Presents outline. */
    private IInformationPresenter fOutlinePresenter;
    /** Presents type hierarchy. */
    private IInformationPresenter fHierarchyPresenter;

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

		super.configure(configuration);
		if (configuration instanceof CSourceViewerConfiguration) {
			CSourceViewerConfiguration cConfiguration= (CSourceViewerConfiguration)configuration;
			fOutlinePresenter= cConfiguration.getOutlinePresenter(this);
			if (fOutlinePresenter != null)
				fOutlinePresenter.install(this);
			fHierarchyPresenter= cConfiguration.getHierarchyPresenter(this);
			if (fHierarchyPresenter != null) 
				fHierarchyPresenter.install(this);
		}
		if (fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(this);
			initializeViewerColors();
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
    public void unconfigure() {
        if (fOutlinePresenter != null) {
            fOutlinePresenter.uninstall();  
            fOutlinePresenter= null;
        }
        if (fHierarchyPresenter != null) {
        	fHierarchyPresenter.uninstall();  
        	fHierarchyPresenter= null;
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
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
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
	protected void createControl(Composite parent, int styles) {

		// Use LEFT_TO_RIGHT unless otherwise specified.
		if ((styles & SWT.RIGHT_TO_LEFT) == 0 && (styles & SWT.LEFT_TO_RIGHT) == 0)
			styles |= SWT.LEFT_TO_RIGHT;
			
		super.createControl(parent, styles);
	}

	/*
     * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
	 */
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
		}
		super.doOperation(operation);
	}

    /*
     * @see org.eclipse.jface.text.source.projection.ProjectionViewer#canDoOperation(int)
     */
    public boolean canDoOperation(int operation) {
        if (operation == SHOW_OUTLINE) {
            return fOutlinePresenter != null;
        }
        else if (operation == SHOW_HIERARCHY) {
        	return fHierarchyPresenter != null;
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

		if (fTextPresentationListeners == null)
			fTextPresentationListeners= new ArrayList();

		fTextPresentationListeners.remove(listener);
		fTextPresentationListeners.add(0, listener);
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
	public void resetVisibleRegion() {
		super.resetVisibleRegion();
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=195808
		if (fWasProjectionMode) {
			fWasProjectionMode= false;
			enableProjection();
		}
	}
}
