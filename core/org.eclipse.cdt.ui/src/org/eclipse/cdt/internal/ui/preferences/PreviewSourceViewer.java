/*******************************************************************************
 * Copyright (c) 2005 QnX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Qnx Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

class PreviewSourceViewer extends SourceViewer implements IPropertyChangeListener {

	/**
	 * This viewer's foreground color.
	 * 
	 * @since 3.0
	 */
	private Color fForegroundColor;

	/**
	 * The viewer's background color.
	 * 
	 * @since 3.0
	 */
	private Color fBackgroundColor;

	/**
	 * This viewer's selection foreground color.
	 * 
	 * @since 3.0
	 */
	private Color fSelectionForegroundColor;

	/**
	 * The viewer's selection background color.
	 * 
	 * @since 3.0
	 */
	private Color fSelectionBackgroundColor;

	/**
	 * The preference store.
	 * 
	 * @since 3.0
	 */
	private IPreferenceStore fPreferenceStore;

	/**
	 * Is this source viewer configured?
	 * 
	 * @since 3.0
	 */
	private boolean fIsConfigured;

	public PreviewSourceViewer(Composite parent, int styles) {
		super(parent, null, styles);
	}

	protected void initializeViewerColors() {
		if (fPreferenceStore != null) {

			StyledText styledText = getTextWidget();

			// ----------- foreground color --------------------
			Color color = fPreferenceStore
					.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT) ? null
					: createColor(fPreferenceStore,
							AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND,
							styledText.getDisplay());
			styledText.setForeground(color);

			if (fForegroundColor != null)
				fForegroundColor.dispose();

			fForegroundColor = color;

			// ---------- background color ----------------------
			color = fPreferenceStore
					.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT) ? null
					: createColor(fPreferenceStore,
							AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
							styledText.getDisplay());
			styledText.setBackground(color);

			if (fBackgroundColor != null)
				fBackgroundColor.dispose();

			fBackgroundColor = color;

			// ----------- selection foreground color --------------------
			color = fPreferenceStore
					.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR) ? null
					: createColor(
							fPreferenceStore,
							AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR,
							styledText.getDisplay());
			styledText.setSelectionForeground(color);

			if (fSelectionForegroundColor != null)
				fSelectionForegroundColor.dispose();

			fSelectionForegroundColor = color;

			// ---------- selection background color ----------------------
			color = fPreferenceStore
					.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR) ? null
					: createColor(
							fPreferenceStore,
							AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR,
							styledText.getDisplay());
			styledText.setSelectionBackground(color);

			if (fSelectionBackgroundColor != null)
				fSelectionBackgroundColor.dispose();

			fSelectionBackgroundColor = color;
		}
	}

	/*
	 * @see ISourceViewer#configure(SourceViewerConfiguration)
	 */
	public void configure(SourceViewerConfiguration configuration) {

		/*
		 * Prevent access to colors disposed in unconfigure(), see:
		 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=53641
		 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=86177
		 */
		StyledText textWidget = getTextWidget();
		if (textWidget != null && !textWidget.isDisposed()) {
			Color foregroundColor = textWidget.getForeground();
			if (foregroundColor != null && foregroundColor.isDisposed())
				textWidget.setForeground(null);
			Color backgroundColor = textWidget.getBackground();
			if (backgroundColor != null && backgroundColor.isDisposed())
				textWidget.setBackground(null);
		}

		super.configure(configuration);

		if (fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(this);
			initializeViewerColors();
		}

		fIsConfigured = true;
	}

	/*
	 * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
	 * @since 3.0
	 */
	public void unconfigure() {
		if (fForegroundColor != null) {
			fForegroundColor.dispose();
			fForegroundColor = null;
		}
		if (fBackgroundColor != null) {
			fBackgroundColor.dispose();
			fBackgroundColor = null;
		}

		 if (fPreferenceStore != null) {
			 fPreferenceStore.removePropertyChangeListener(this);
		 }

		super.unconfigure();

		fIsConfigured = false;
	}

	/**
	 * Creates a color from the information stored in the given preference
	 * store. Returns <code>null</code> if there is no such information
	 * available.
	 * 
	 * @param store
	 *            the store to read from
	 * @param key
	 *            the key used for the lookup in the preference store
	 * @param display
	 *            the display used create the color
	 * @return the created color according to the specification in the
	 *         preference store
	 * @since 3.0
	 */
	private Color createColor(IPreferenceStore store, String key,
			Display display) {

		RGB rgb = null;

		if (store.contains(key)) {

			if (store.isDefault(key))
				rgb = PreferenceConverter.getDefaultColor(store, key);
			else
				rgb = PreferenceConverter.getColor(store, key);

			if (rgb != null)
				return new Color(display, rgb);
		}

		return null;
	}

	/**
	 * Sets the preference store on this viewer.
	 * 
	 * @param store
	 *            the preference store
	 * 
	 * @since 3.0
	 */
	public void setPreferenceStore(IPreferenceStore store) {
		if (fIsConfigured && fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(this);
		}

		fPreferenceStore = store;

		if (fIsConfigured && fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(this);
			initializeViewerColors();
		}
	}

	/*
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT
						.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND
						.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT
						.equals(property)
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR
						.equals(property)
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR
						.equals(property)
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR
						.equals(property)
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR
						.equals(property)) {
			initializeViewerColors();
		}
	}

}