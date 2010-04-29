/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.cdt.ui.text.doctools.doxygen.DoxygenHelper;

import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlighting;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedRange;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.internal.ui.text.SimpleCSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;

/**
 * Configures C/C++ Editor code coloring preferences.
 * 
 * @since 4.0
 */
class CEditorColoringConfigurationBlock extends AbstractConfigurationBlock {
	
	/**
	 * Item in the highlighting color list.
	 */
	private static class HighlightingColorListItem {
		/** Display name */
		private String fDisplayName;
		/** Color preference key */
		private String fColorKey;
		/** Bold preference key */
		private String fBoldKey;
		/** Italic preference key */
		private String fItalicKey;
		/** Strikethrough preference key */
		private String fStrikethroughKey;
		/** Underline preference key */
		private String fUnderlineKey;
		
		/**
		 * Initialize the item with the given values.
		 * @param displayName the display name
		 * @param colorKey the color preference key
		 * @param boldKey the bold preference key
		 * @param italicKey the italic preference key
		 * @param strikethroughKey the strikethrough preference key
		 * @param underlineKey the underline preference key
		 */
		public HighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey) {
			fDisplayName= displayName;
			fColorKey= colorKey;
			fBoldKey= boldKey;
			fItalicKey= italicKey;
			fStrikethroughKey= strikethroughKey;
			fUnderlineKey= underlineKey;
		}
		
		/**
		 * @return the bold preference key
		 */
		public String getBoldKey() {
			return fBoldKey;
		}
		
		/**
		 * @return the bold preference key
		 */
		public String getItalicKey() {
			return fItalicKey;
		}
		
		/**
		 * @return the strikethrough preference key
		 */
		public String getStrikethroughKey() {
			return fStrikethroughKey;
		}
		
		/**
		 * @return the underline preference key
		 */
		public String getUnderlineKey() {
			return fUnderlineKey;
		}
		
		/**
		 * @return the color preference key
		 */
		public String getColorKey() {
			return fColorKey;
		}
		
		/**
		 * @return the display name
		 */
		public String getDisplayName() {
			return fDisplayName;
		}
	}
	
	private static class SemanticHighlightingColorListItem extends HighlightingColorListItem {
	
		/** Enablement preference key */
		private final String fEnableKey;
		
		/**
		 * Initialize the item with the given values.
		 * @param displayName the display name
		 * @param colorKey the color preference key
		 * @param boldKey the bold preference key
		 * @param italicKey the italic preference key
		 * @param strikethroughKey the strikethroughKey preference key
		 * @param underlineKey the underlineKey preference key
		 * @param enableKey the enable preference key
		 */
		public SemanticHighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey, String enableKey) {
			super(displayName, colorKey, boldKey, italicKey, strikethroughKey, underlineKey);
			fEnableKey= enableKey;
		}
	
		/**
		 * @return the enablement preference key
		 */
		public String getEnableKey() {
			return fEnableKey;
		}
	}

	/**
	 * Color list label provider.
	 */
	private class ColorListLabelProvider extends LabelProvider implements IColorProvider {
		/*
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			if (element instanceof String)
				return (String) element;
			return ((HighlightingColorListItem)element).getDisplayName();
		}

		/*
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			if (element instanceof SemanticHighlightingColorListItem) {
				if (!getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED)) {
					return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
				}
			}
			return null;
		}
	}

	/**
	 * Color list content provider.
	 */
	private class ColorListContentProvider implements ITreeContentProvider {
	
		/*
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return new String[] {fCodeCategory, fAssemblyCategory, fCommentsCategory, fPreprocessorCategory, fDoxygenCategory};
		}
	
		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}
	
		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof String) {
				String entry= (String) parentElement;
				if (fCodeCategory.equals(entry))
					return fListModel.subList(11, fListModel.size()).toArray();
				if (fAssemblyCategory.equals(entry))
					return fListModel.subList(6, 8).toArray();
				if (fCommentsCategory.equals(entry))
					return fListModel.subList(0, 3).toArray();
				if (fPreprocessorCategory.equals(entry))
					return fListModel.subList(3, 6).toArray();
				if (fDoxygenCategory.equals(entry))
					return fListModel.subList(8, 11).toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof String)
				return null;
			int index= fListModel.indexOf(element);
			if (index >= 11)
				return fCodeCategory;
			if (index >= 8)
				return fDoxygenCategory;
			if (index >= 6)
				return fAssemblyCategory;
			if (index >= 3)
				return fPreprocessorCategory;
			return fCommentsCategory;
		}

		public boolean hasChildren(Object element) {
			return element instanceof String;
		}
	}

	/**
	 * Preference key suffix for bold preferences.
	 */
	private static final String BOLD= PreferenceConstants.EDITOR_BOLD_SUFFIX;
	/**
	 * Preference key suffix for italic preferences.
	 */
	private static final String ITALIC= PreferenceConstants.EDITOR_ITALIC_SUFFIX;
	/**
	 * Preference key suffix for strikethrough preferences.
	 */
	private static final String STRIKETHROUGH= PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
	/**
	 * Preference key suffix for underline preferences.
	 */
	private static final String UNDERLINE= PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;
	
	/**
	 * The keys of the overlay store.
	 */
	private final String[][] fSyntaxColorListModel= new String[][] {
			{ PreferencesMessages.CEditorColoringConfigurationBlock_MultiLine, PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_singleLine, PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_cCommentTaskTags, PreferenceConstants.EDITOR_TASK_TAG_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_ppDirectives, PreferenceConstants.EDITOR_PP_DIRECTIVE_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_ppOthers, PreferenceConstants.EDITOR_PP_DEFAULT_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_ppHeaders, PreferenceConstants.EDITOR_PP_HEADER_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_asmLabels, PreferenceConstants.EDITOR_ASM_LABEL_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_asmDirectives, PreferenceConstants.EDITOR_ASM_DIRECTIVE_COLOR },
         	{ PreferencesMessages.CEditorColoringConfigurationBlock_DoxygenTagRecognized, DoxygenHelper.DOXYGEN_TAG_RECOGNIZED },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_DoxygenSingleLineComment, DoxygenHelper.DOXYGEN_SINGLE_TOKEN },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_DoxygenMultiLineComment, DoxygenHelper.DOXYGEN_MULTI_TOKEN },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_keywords, PreferenceConstants.EDITOR_C_KEYWORD_COLOR },
//			{ PreferencesMessages.CEditorColoringConfigurationBlock_returnKeyword, PreferenceConstants.EDITOR_C_KEYWORD_RETURN_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_builtInTypes, PreferenceConstants.EDITOR_C_BUILTIN_TYPE_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_operators, PreferenceConstants.EDITOR_C_OPERATOR_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_strings, PreferenceConstants.EDITOR_C_STRING_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_braces, PreferenceConstants.EDITOR_C_BRACES_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_numbers, PreferenceConstants.EDITOR_C_NUMBER_COLOR },
			{ PreferencesMessages.CEditorColoringConfigurationBlock_others, PreferenceConstants.EDITOR_C_DEFAULT_COLOR },
	};
		
	private final String fCodeCategory= PreferencesMessages.CEditorColoringConfigurationBlock_coloring_category_code;
	private final String fCommentsCategory= PreferencesMessages.CEditorColoringConfigurationBlock_coloring_category_comments;
	private final String fPreprocessorCategory= PreferencesMessages.CEditorColoringConfigurationBlock_coloring_category_preprocessor;
	private final String fAssemblyCategory= PreferencesMessages.CEditorColoringConfigurationBlock_coloring_category_assembly;
	private final String fDoxygenCategory= PreferencesMessages.CEditorColoringConfigurationBlock_coloring_category_doxygen;
	
	private ColorSelector fSyntaxForegroundColorEditor;
	private Label fColorEditorLabel;
	private Button fEnableSemanticHighlightingCheckbox;
	private Button fBoldCheckBox;
	private Button fEnableCheckbox;
	/**
	 * Check box for italic preference.
	 */
	private Button fItalicCheckBox;
	/**
	 * Check box for strikethrough preference.
	 */
	private Button fStrikethroughCheckBox;
	/**
	 * Check box for underline preference.
	 */
	private Button fUnderlineCheckBox;
	/**
	 * Highlighting color list
	 */
	private final java.util.List<HighlightingColorListItem> fListModel= new ArrayList<HighlightingColorListItem>();
	/**
	 * Highlighting color list viewer
	 */
	private StructuredViewer fListViewer;
	/**
	 * Semantic highlighting manager
	 */
	private SemanticHighlightingManager fSemanticHighlightingManager;
	/**
	 * The previewer.
	 */
	private CSourceViewer fPreviewViewer;
	/**
	 * The color manager.
	 */
	private IColorManager fColorManager;
	/**
	 * The font metrics.
	 */
	private FontMetrics fFontMetrics;

	public CEditorColoringConfigurationBlock(OverlayPreferenceStore store) {
		super(store);
		
		fColorManager= new CColorManager(false);
		
		for (String[] element : fSyntaxColorListModel)
			fListModel.add(new HighlightingColorListItem (
					element[0],
					element[1],
					element[1] + BOLD,
					element[1] + ITALIC,
					element[1] + STRIKETHROUGH,
					element[1] + UNDERLINE));

		SemanticHighlighting[] semanticHighlightings= SemanticHighlightings.getSemanticHighlightings();
		for (SemanticHighlighting semanticHighlighting : semanticHighlightings)
			fListModel.add(
					new SemanticHighlightingColorListItem(
							semanticHighlighting.getDisplayName(),
							SemanticHighlightings.getColorPreferenceKey(semanticHighlighting),
							SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting),
							SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting),
							SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlighting),
							SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlighting),
							SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting)
					));
		
		store.addKeys(createOverlayStoreKeys());
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		List<OverlayPreferenceStore.OverlayKey> overlayKeys= new ArrayList<OverlayPreferenceStore.OverlayKey>();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED));
		
		for (int i= 0, n= fListModel.size(); i < n; i++) {
			HighlightingColorListItem item= fListModel.get(i);
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, item.getColorKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getBoldKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getItalicKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getStrikethroughKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getUnderlineKey()));
			
			if (item instanceof SemanticHighlightingColorListItem)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ((SemanticHighlightingColorListItem) item).getEnableKey()));
		}
		
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	/**
	 * Creates page for hover preferences.
	 * 
	 * @param parent the parent composite
	 * @return the control for the preference page
	 */
	public Control createControl(Composite parent) {
		initializeDialogUnits(parent);
		return createSyntaxPage(parent);
	}
	
	/**
     * Returns the number of pixels corresponding to the width of the given
     * number of characters.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param chars
     *            the number of characters
     * @return the number of pixels
     */
    private int convertWidthInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fFontMetrics == null)
            return 0;
        return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
    }

	/**
     * Returns the number of pixels corresponding to the height of the given
     * number of characters.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param chars
     *            the number of characters
     * @return the number of pixels
     */
    private int convertHeightInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fFontMetrics == null)
            return 0;
        return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
    }
    
	@Override
	public void initialize() {
		super.initialize();
		
		fListViewer.setInput(fListModel);
		fListViewer.setSelection(new StructuredSelection(fCodeCategory));
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		
		fListViewer.refresh();

		handleSyntaxColorListSelection();

		uninstallSemanticHighlighting();
		installSemanticHighlighting();

		fPreviewViewer.invalidateTextPresentation();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.IPreferenceConfigurationBlock#dispose()
	 */
	@Override
	public void dispose() {
		uninstallSemanticHighlighting();
		fColorManager.dispose();

		super.dispose();
	}

	private void handleSyntaxColorListSelection() {
		HighlightingColorListItem item= getHighlightingColorListItem();
		if (item == null) {
			fEnableCheckbox.setEnabled(false);
			fSyntaxForegroundColorEditor.getButton().setEnabled(false);
			fColorEditorLabel.setEnabled(false);
			fBoldCheckBox.setEnabled(false);
			fItalicCheckBox.setEnabled(false);
			fStrikethroughCheckBox.setEnabled(false);
			fUnderlineCheckBox.setEnabled(false);
			return;
		}
		RGB rgb= PreferenceConverter.getColor(getPreferenceStore(), item.getColorKey());
		fSyntaxForegroundColorEditor.setColorValue(rgb);
		fBoldCheckBox.setSelection(getPreferenceStore().getBoolean(item.getBoldKey()));
		fItalicCheckBox.setSelection(getPreferenceStore().getBoolean(item.getItalicKey()));
		fStrikethroughCheckBox.setSelection(getPreferenceStore().getBoolean(item.getStrikethroughKey()));
		fUnderlineCheckBox.setSelection(getPreferenceStore().getBoolean(item.getUnderlineKey()));
		if (item instanceof SemanticHighlightingColorListItem) {
			boolean semanticHighlightingEnabled= getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED);
			fEnableCheckbox.setEnabled(semanticHighlightingEnabled);
			boolean enable= semanticHighlightingEnabled && getPreferenceStore().getBoolean(((SemanticHighlightingColorListItem) item).getEnableKey());
			fEnableCheckbox.setSelection(enable);
			fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
			fColorEditorLabel.setEnabled(enable);
			fBoldCheckBox.setEnabled(enable);
			fItalicCheckBox.setEnabled(enable);
			fStrikethroughCheckBox.setEnabled(enable);
			fUnderlineCheckBox.setEnabled(enable);
		} else {
			fSyntaxForegroundColorEditor.getButton().setEnabled(true);
			fColorEditorLabel.setEnabled(true);
			fBoldCheckBox.setEnabled(true);
			fItalicCheckBox.setEnabled(true);
			fStrikethroughCheckBox.setEnabled(true);
			fUnderlineCheckBox.setEnabled(true);
			fEnableCheckbox.setEnabled(false);
			fEnableCheckbox.setSelection(true);
		}
	}
	
	private Control createSyntaxPage(final Composite parent) {
		
		Composite colorComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		colorComposite.setLayout(layout);

		Link link= new Link(colorComposite, SWT.NONE);
		link.setText(PreferencesMessages.CEditorColoringConfigurationBlock_link);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
			}
		});
		// TODO replace by link-specific tooltips when
		// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=88866 gets fixed
//		link.setToolTipText(PreferencesMessages.CEditorColoringConfigurationBlock_link_tooltip);
		
		GridData gridData= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.widthHint= 150; // only expand further if anyone else requires it
		gridData.horizontalSpan= 2;
		link.setLayoutData(gridData);

		addFiller(colorComposite, 1);

		fEnableSemanticHighlightingCheckbox= addCheckBox(colorComposite,
				PreferencesMessages.CEditorColoringConfigurationBlock_enable_semantic_highlighting,
				PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED, 0);
		
		Label label;
		label= new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages.CEditorColoringConfigurationBlock_coloring_element);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		Composite editorComposite= new Composite(colorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		GridData gd= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		editorComposite.setLayoutData(gd);
	
		fListViewer= new TreeViewer(editorComposite, SWT.SINGLE | SWT.BORDER);
		fListViewer.setLabelProvider(new ColorListLabelProvider());
		fListViewer.setContentProvider(new ColorListContentProvider());
		fListViewer.setSorter(new ViewerSorter() {
			@Override
			public int category(Object element) {
				// don't sort the top level categories
				if (fCodeCategory.equals(element))
					return 0;
				if (fAssemblyCategory.equals(element))
					return 1;
				if (fCommentsCategory.equals(element))
					return 2;
				if (fPreprocessorCategory.equals(element))
					return 3;
				if (fDoxygenCategory.equals(element))
					return 4;
				return 0;
			}
		});
		gd= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
		gd.heightHint= convertHeightInCharsToPixels(9);
		int maxWidth= 0;
		for (HighlightingColorListItem item : fListModel) {
			maxWidth= Math.max(maxWidth, convertWidthInCharsToPixels(item.getDisplayName().length()));
		}
		ScrollBar vBar= ((Scrollable) fListViewer.getControl()).getVerticalBar();
		if (vBar != null)
			maxWidth += vBar.getSize().x * 3; // scrollbars and tree indentation guess
		gd.widthHint= maxWidth;
		
		fListViewer.getControl().setLayoutData(gd);
		
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fEnableCheckbox= new Button(stylesComposite, SWT.CHECK);
		fEnableCheckbox.setText(PreferencesMessages.CEditorColoringConfigurationBlock_enable);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fEnableCheckbox.setLayoutData(gd);
		
		fColorEditorLabel= new Label(stylesComposite, SWT.LEFT);
		fColorEditorLabel.setText(PreferencesMessages.CEditorColoringConfigurationBlock_color);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		fColorEditorLabel.setLayoutData(gd);
	
		fSyntaxForegroundColorEditor= new ColorSelector(stylesComposite);
		Button foregroundColorButton= fSyntaxForegroundColorEditor.getButton();
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		foregroundColorButton.setLayoutData(gd);
		
		fBoldCheckBox= new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText(PreferencesMessages.CEditorColoringConfigurationBlock_bold);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		gd.horizontalSpan= 2;
		fBoldCheckBox.setLayoutData(gd);
		
		fItalicCheckBox= new Button(stylesComposite, SWT.CHECK);
		fItalicCheckBox.setText(PreferencesMessages.CEditorColoringConfigurationBlock_italic);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		gd.horizontalSpan= 2;
		fItalicCheckBox.setLayoutData(gd);
		
		fStrikethroughCheckBox= new Button(stylesComposite, SWT.CHECK);
		fStrikethroughCheckBox.setText(PreferencesMessages.CEditorColoringConfigurationBlock_strikethrough);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		gd.horizontalSpan= 2;
		fStrikethroughCheckBox.setLayoutData(gd);
		
		fUnderlineCheckBox= new Button(stylesComposite, SWT.CHECK);
		fUnderlineCheckBox.setText(PreferencesMessages.CEditorColoringConfigurationBlock_underline);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		gd.horizontalSpan= 2;
		fUnderlineCheckBox.setLayoutData(gd);
		
		label= new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages.CEditorColoringConfigurationBlock_preview);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Control previewer= createPreviewer(colorComposite);
		gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(20);
		gd.heightHint= convertHeightInCharsToPixels(5);
		previewer.setLayoutData(gd);
		
		fListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSyntaxColorListSelection();
			}
		});
		
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				PreferenceConverter.setValue(getPreferenceStore(), item.getColorKey(), fSyntaxForegroundColorEditor.getColorValue());
			}
		});
	
		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getPreferenceStore().setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
			}
		});
				
		fItalicCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getPreferenceStore().setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
			}
		});
		fStrikethroughCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getPreferenceStore().setValue(item.getStrikethroughKey(), fStrikethroughCheckBox.getSelection());
			}
		});
		
		fUnderlineCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getPreferenceStore().setValue(item.getUnderlineKey(), fUnderlineCheckBox.getSelection());
			}
		});
				
		fEnableCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				if (item instanceof SemanticHighlightingColorListItem) {
					boolean enable= fEnableCheckbox.getSelection();
					getPreferenceStore().setValue(((SemanticHighlightingColorListItem) item).getEnableKey(), enable);
					fEnableCheckbox.setSelection(enable);
					fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
					fColorEditorLabel.setEnabled(enable);
					fBoldCheckBox.setEnabled(enable);
					fItalicCheckBox.setEnabled(enable);
					fStrikethroughCheckBox.setEnabled(enable);
					fUnderlineCheckBox.setEnabled(enable);
					uninstallSemanticHighlighting();
					installSemanticHighlighting();
				}
			}
		});
		
		fEnableSemanticHighlightingCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				fListViewer.refresh(true);
				HighlightingColorListItem item= getHighlightingColorListItem();
				if (item instanceof SemanticHighlightingColorListItem) {
					handleSyntaxColorListSelection();
					uninstallSemanticHighlighting();
					installSemanticHighlighting();
				}
			}
		});
		
		colorComposite.layout(false);
				
		return colorComposite;
	}
	
	private void addFiller(Composite composite, int horizontalSpan) {
		PixelConverter pixelConverter= new PixelConverter(composite);
		Label filler= new Label(composite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= horizontalSpan;
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	private Control createPreviewer(Composite parent) {
		IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore();
		IPreferenceStore store= new ChainedPreferenceStore(new IPreferenceStore[] { getPreferenceStore(), generalTextStore });
		fPreviewViewer = new CSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, store);
		SimpleCSourceViewerConfiguration configuration = new SimpleCSourceViewerConfiguration(fColorManager, store, null, ICPartitions.C_PARTITIONING, false);
		fPreviewViewer.configure(configuration);
		Font font= JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		fPreviewViewer.getTextWidget().setFont(font);
		CSourcePreviewerUpdater.registerPreviewer(fPreviewViewer, configuration, store);
		fPreviewViewer.setEditable(false);
		
		String content= loadPreviewContentFromFile("ColorSettingPreviewCode.txt"); //$NON-NLS-1$
		IDocument document= new Document(content);
		CUIPlugin.getDefault().getTextTools().setupCDocumentPartitioner(document, ICPartitions.C_PARTITIONING, null);
		fPreviewViewer.setDocument(document);
	
		installSemanticHighlighting();
		
		return fPreviewViewer.getControl();
	}

	private String loadPreviewContentFromFile(String filename) {
		String line;
		String separator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer(512);
		BufferedReader reader= null;
		try {
			reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			CUIPlugin.log(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return buffer.toString();
	}

	/**
	 * Install Semantic Highlighting on the previewer
	 */
	private void installSemanticHighlighting() {
		if (fSemanticHighlightingManager == null) {
			fSemanticHighlightingManager= new SemanticHighlightingManager();
			fSemanticHighlightingManager.install(fPreviewViewer, fColorManager, getPreferenceStore(), createPreviewerRanges());
		}
	}

	/**
	 * Uninstall Semantic Highlighting from the previewer
	 */
	private void uninstallSemanticHighlighting() {
		if (fSemanticHighlightingManager != null) {
			fSemanticHighlightingManager.uninstall();
			fSemanticHighlightingManager= null;
		}
	}

	/**
	 * Create the hard coded previewer ranges. Must be sorted by ascending offset.
	 * 
	 * @return the hard coded previewer ranges
	 */
	private SemanticHighlightingManager.HighlightedRange[][] createPreviewerRanges() {
		return new SemanticHighlightingManager.HighlightedRange[][] {
			{ createHighlightedRange( 2,  8,  5, SemanticHighlightings.MACRO_DEFINITION) },
			{ createHighlightedRange( 3, 16,  3, SemanticHighlightings.NAMESPACE) },
			{ createHighlightedRange( 5, 21,  4, SemanticHighlightings.TYPEDEF) },
			{ createHighlightedRange( 6, 11,  6, SemanticHighlightings.FUNCTION_DECLARATION),  createHighlightedRange( 6, 11,  6, SemanticHighlightings.FUNCTION) },
			{ createHighlightedRange( 6, 18,  4, SemanticHighlightings.TYPEDEF) },
			{ createHighlightedRange( 6, 23,  9, SemanticHighlightings.PARAMETER_VARIABLE) },
			{ createHighlightedRange( 7,  6,  9, SemanticHighlightings.PARAMETER_VARIABLE) },
			{ createHighlightedRange( 7, 22,  7, SemanticHighlightings.EXTERNAL_SDK), createHighlightedRange( 7, 22,  7, SemanticHighlightings.FUNCTION) },
			{ createHighlightedRange( 7, 30,  6, SemanticHighlightings.GLOBAL_VARIABLE) },
			{ createHighlightedRange( 8, 2,   4, SemanticHighlightings.GLOBAL_VARIABLE) },
			{ createHighlightedRange( 8, 7,   2, SemanticHighlightings.OVERLOADED_OPERATOR) },
			{ createHighlightedRange( 9,  9,  9, SemanticHighlightings.PARAMETER_VARIABLE) },
			{ createHighlightedRange(11,  6,  7, SemanticHighlightings.CLASS) },
			{ createHighlightedRange(13,  7,  6, SemanticHighlightings.ENUM) },
			{ createHighlightedRange(13, 16,  4, SemanticHighlightings.ENUMERATOR) },
			{ createHighlightedRange(13, 22,  3, SemanticHighlightings.ENUMERATOR) },
			{ createHighlightedRange(13, 27,  3, SemanticHighlightings.ENUMERATOR) },
			{ createHighlightedRange(14, 14, 11, SemanticHighlightings.STATIC_FIELD), createHighlightedRange(13, 14, 11, SemanticHighlightings.FIELD) },
			{ createHighlightedRange(15,  6,  5, SemanticHighlightings.FIELD) },
			{ createHighlightedRange(16, 10,  6, SemanticHighlightings.ENUM) },
			{ createHighlightedRange(16, 17,  7, SemanticHighlightings.METHOD_DECLARATION), createHighlightedRange(15, 17,  7, SemanticHighlightings.METHOD) },
			{ createHighlightedRange(17,  7,  6, SemanticHighlightings.METHOD_DECLARATION), createHighlightedRange(16,  7,  6, SemanticHighlightings.METHOD) },
			{ createHighlightedRange(17, 14,  6, SemanticHighlightings.ENUM) },
			{ createHighlightedRange(17, 21,  1, SemanticHighlightings.PARAMETER_VARIABLE) },
			{ createHighlightedRange(18,  8,  5, SemanticHighlightings.LOCAL_VARIABLE_DECLARATION) },
			{ createHighlightedRange(18, 20,  5, SemanticHighlightings.MACRO_REFERENCE) },
			{ createHighlightedRange(19,  0,  5, SemanticHighlightings.LABEL) },
			{ createHighlightedRange(19,  7,  6, SemanticHighlightings.FUNCTION) },
			{ createHighlightedRange(19, 14,  5, SemanticHighlightings.LOCAL_VARIABLE) },
			{ createHighlightedRange(20,  4,  7, SemanticHighlightings.METHOD) },
			{ createHighlightedRange(21,  4, 12, SemanticHighlightings.STATIC_METHOD_INVOCATION), createHighlightedRange(20,  4, 12, SemanticHighlightings.METHOD) },
			{ createHighlightedRange(22,  4,  7, SemanticHighlightings.PROBLEM) },
			{ createHighlightedRange(24, 14, 12, SemanticHighlightings.METHOD_DECLARATION), createHighlightedRange(23, 14, 12, SemanticHighlightings.METHOD) },
		};
	}

	/**
	 * Create a highlighted range on the previewers document with the given line, column, length and key.
	 * 
	 * @param line the line
	 * @param column the column
	 * @param length the length
	 * @param key the key
	 * @return the highlighted range
	 */
	private HighlightedRange createHighlightedRange(int line, int column, int length, String key) {
		try {
			IDocument document= fPreviewViewer.getDocument();
			int offset= document.getLineOffset(line) + column;
			return new HighlightedRange(offset, length, key);
		} catch (BadLocationException x) {
			CUIPlugin.log(x);
		}
		return null;
	}

	/**
	 * Returns the current highlighting color list item.
	 * 
	 * @return the current highlighting color list item
	 */
	private HighlightingColorListItem getHighlightingColorListItem() {
		IStructuredSelection selection= (IStructuredSelection) fListViewer.getSelection();
		Object element= selection.getFirstElement();
		if (element instanceof String)
			return null;
		return (HighlightingColorListItem) element;
	}
	
	/**
     * Initializes the computation of horizontal and vertical dialog units based
     * on the size of current font.
     * <p>
     * This method must be called before any of the dialog unit based conversion
     * methods are called.
     * </p>
     * 
     * @param testControl
     *            a control from which to obtain the current font
     */
    private void initializeDialogUnits(Control testControl) {
        // Compute and store a font metric
        GC gc = new GC(testControl);
        gc.setFont(JFaceResources.getDialogFont());
        fFontMetrics = gc.getFontMetrics();
        gc.dispose();
    }
}
