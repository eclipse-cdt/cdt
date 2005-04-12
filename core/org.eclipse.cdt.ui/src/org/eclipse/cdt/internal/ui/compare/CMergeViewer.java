package org.eclipse.cdt.internal.ui.compare;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.CUIPlugin;


import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.TokenComparator;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class CMergeViewer extends TextMergeViewer {
	
	private static final String TITLE= "CMergeViewer.title"; //$NON-NLS-1$

	private IPropertyChangeListener fPreferenceChangeListener;
	private IPreferenceStore fPreferenceStore;
	private boolean fUseSystemColors;
	private CSourceViewerConfiguration fSourceViewerConfiguration;
		
	public CMergeViewer(Composite parent, int styles, CompareConfiguration mp) {
		super(parent, styles, mp);
		fPreferenceStore= CUIPlugin.getDefault().getCombinedPreferenceStore();
		if (fPreferenceStore != null) {
			 fPreferenceChangeListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					handlePropertyChange(event);
				}
			};
			fPreferenceStore.addPropertyChangeListener(fPreferenceChangeListener);
		}
		
		fUseSystemColors= fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		if (! fUseSystemColors) {
			RGB bg= createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
			setBackgroundColor(bg);
			RGB fg= createColor(fPreferenceStore, ICColorConstants.C_DEFAULT);
			setForegroundColor(fg);
		}

	}

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
		
		if (getSourceViewerConfiguration().affectsBehavior(event)) {
			getSourceViewerConfiguration().adaptToPreferenceChange(event);
			invalidateTextPresentation();
		}
	}
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private static RGB createColor(IPreferenceStore store, String key) {
		if (!store.contains(key))
			return null;
		if (store.isDefault(key))
			return PreferenceConverter.getDefaultColor(store, key);
		return PreferenceConverter.getColor(store, key);
	}

	private CSourceViewerConfiguration getSourceViewerConfiguration() {
		if (fSourceViewerConfiguration == null) {
			CTextTools tools= CUIPlugin.getDefault().getTextTools();
			fSourceViewerConfiguration = new CSourceViewerConfiguration(tools, null);
		}
		return fSourceViewerConfiguration;
	}

	public String getTitle() {
		return CUIPlugin.getResourceString(TITLE);
	}

	protected ITokenComparator createTokenComparator(String s) {
		return new TokenComparator(s);
	}
	
	protected IDocumentPartitioner getDocumentPartitioner() {
		return CUIPlugin.getDefault().getTextTools().createDocumentPartitioner();
	}
		
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			CTextTools tools= CUIPlugin.getDefault().getTextTools();
			((SourceViewer)textViewer).configure(getSourceViewerConfiguration());
		}
	}

}
