/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ICPartitions;


/**
 * Abstract implementation of a merge viewer.
 */
abstract class AbstractMergeViewer extends TextMergeViewer {

	private IPropertyChangeListener fPreferenceChangeListener;
	private IPreferenceStore fPreferenceStore;

	protected boolean fUseSystemColors;

	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	protected static RGB createColor(IPreferenceStore store, String key) {
		if (!store.contains(key))
			return null;
		if (store.isDefault(key))
			return PreferenceConverter.getDefaultColor(store, key);
		return PreferenceConverter.getColor(store, key);
	}

	/**
	 * Create a new merge viewer.
	 * 
	 * @param parent
	 * @param style
	 * @param configuration
	 */
	public AbstractMergeViewer(Composite parent, int style, CompareConfiguration configuration) {
		super(parent, style | SWT.LEFT_TO_RIGHT, configuration);

		IPreferenceStore store = getPreferenceStore();

		fUseSystemColors= store.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		if (! fUseSystemColors) {
			RGB bg= createColor(store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
			setBackgroundColor(bg);
			RGB fg= createColor(store, ICColorConstants.C_DEFAULT);
			setForegroundColor(fg);
		}

	}

	protected IPreferenceStore getPreferenceStore() {
		if (fPreferenceStore == null) {
			fPreferenceStore= CUIPlugin.getDefault().getCombinedPreferenceStore();
			fPreferenceChangeListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					handlePropertyChange(event);
				}
			};
			fPreferenceStore.addPropertyChangeListener(fPreferenceChangeListener);
		}
		return fPreferenceStore;
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		if (fPreferenceChangeListener != null) {
			fPreferenceStore.removePropertyChangeListener(fPreferenceChangeListener);
			fPreferenceChangeListener= null;
		}
		super.handleDispose(event);
	}

	protected void handlePropertyChange(PropertyChangeEvent event) {
		
		String key= event.getProperty();
		
		if (key.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)) {
	
			if (!fUseSystemColors) {
				RGB bg= createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
				setBackgroundColor(bg);
			}
						
		} else if (key.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
	
			fUseSystemColors= fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
			if (fUseSystemColors) {
				setBackgroundColor(null);
				setForegroundColor(null);
			} else {
				RGB bg= createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
				setBackgroundColor(bg);
				RGB fg= createColor(fPreferenceStore, ICColorConstants.C_DEFAULT);
				setForegroundColor(fg);
			}
		} else if (key.equals(ICColorConstants.C_DEFAULT)) {
	
			if (!fUseSystemColors) {
				RGB fg= createColor(fPreferenceStore, ICColorConstants.C_DEFAULT);
				setForegroundColor(fg);
			}
		}
	}

	@Override
	protected String getDocumentPartitioning() {
		return ICPartitions.C_PARTITIONING;
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			((SourceViewer)textViewer).configure(getSourceViewerConfiguration());
		}
	}

	/*
	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#getTitle()
	 */
	@Override
	public abstract String getTitle();

	/**
	 * @return a source configuration for the viewer
	 */
	protected abstract SourceViewerConfiguration getSourceViewerConfiguration();

}