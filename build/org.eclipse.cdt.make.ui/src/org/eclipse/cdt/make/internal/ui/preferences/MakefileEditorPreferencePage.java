/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.make.internal.ui.text.ColorManager;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;


/**
 * MakeEditorPreferencePage
 * The page for setting the editor options.
 */
public class MakefileEditorPreferencePage extends AbstractMakefileEditorPreferencePage {

	/** The keys of the overlay store. */
	private String[][] fSyntaxColorListModel;

	private TableViewer fHighlightingColorListViewer;
	private final java.util.List fHighlightingColorList= new ArrayList(5);
	List fAppearanceColorList;

	ColorEditor fAppearanceColorEditor;
	Button fAppearanceColorDefault;
	ColorEditor fSyntaxForegroundColorEditor;
	ColorEditor fBackgroundColorEditor;
	private Button fBackgroundDefaultRadioButton;
	Button fBackgroundCustomRadioButton;
	Button fBackgroundColorButton;
	Button fBoldCheckBox;
	Button fItalicCheckBox;
	
	// folding
	protected Button fFoldingCheckbox;
	
	protected Map fWorkingValues;
	protected ArrayList fComboBoxes;
	

	final String[][] fAppearanceColorListModel= new String[][] {
		{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.lineNumberForegroundColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, null}, //$NON-NLS-1$
		{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.currentLineHighlighColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, null}, //$NON-NLS-1$
		{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.printMarginColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, null}, //$NON-NLS-1$
		{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.foreground"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR}, //$NON-NLS-1$
		{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.background"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR}, //$NON-NLS-1$
	};
	

	/**
	 * Item in the highlighting color list.
	 * 
	 * @since 3.0
	 */
	private class HighlightingColorListItem {
		/** Display name */
		private String fDisplayName;
		/** Color preference key */
		private String fColorKey;
		/** Bold preference key */
		private String fBoldKey;
		/** Italic preference key */
		private String fItalicKey;
		/** Item color */
		private Color fItemColor;
		
		/**
		 * Initialize the item with the given values.
		 * 
		 * @param displayName the display name
		 * @param colorKey the color preference key
		 * @param boldKey the bold preference key
		 * @param italicKey the italic preference key
		 * @param itemColor the item color
		 */
		public HighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, Color itemColor) {
			fDisplayName= displayName;
			fColorKey= colorKey;
			fBoldKey= boldKey;
			fItalicKey= italicKey;
			fItemColor= itemColor;
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
		
		/**
		 * @return the item color
		 */
		public Color getItemColor() {
			return fItemColor;
		}
	}

	/**
	 * Color list label provider.
	 * 
	 * @since 3.0
	 */
	private class ColorListLabelProvider extends LabelProvider implements IColorProvider {

		/*
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			return ((HighlightingColorListItem)element).getDisplayName();
		}
		
		/*
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			return ((HighlightingColorListItem)element).getItemColor();
		}

		/*
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			return null;
		}
	}
	
	/**
	 * Color list content provider.
	 * 
	 * @since 3.0
	 */
	private class ColorListContentProvider implements IStructuredContentProvider {

		/*
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return ((java.util.List)inputElement).toArray();
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
	}
	


	/**
	 * 
	 */
	public MakefileEditorPreferencePage() {
		super();
	}

	protected OverlayPreferenceStore createOverlayStore() {
		fSyntaxColorListModel= new String[][] {
				{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_comment"), ColorManager.MAKE_COMMENT_COLOR, null}, //$NON-NLS-1$
				{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_macro_ref"), ColorManager.MAKE_MACRO_REF_COLOR, null}, //$NON-NLS-1$
				{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_macro_def"), ColorManager.MAKE_MACRO_DEF_COLOR, null}, //$NON-NLS-1$
				{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_function"), ColorManager.MAKE_FUNCTION_COLOR, null},  //$NON-NLS-1$
				{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_keyword"),  ColorManager.MAKE_KEYWORD_COLOR, null},  //$NON-NLS-1$
			};
		ArrayList overlayKeys= new ArrayList();
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR));


		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, MakefileEditorPreferenceConstants.EDITOR_FOLDING_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, MakefileEditorPreferenceConstants.EDITOR_FOLDING_CONDITIONAL));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, MakefileEditorPreferenceConstants.EDITOR_FOLDING_MACRODEF));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, MakefileEditorPreferenceConstants.EDITOR_FOLDING_RULE));
		
		for (int i= 0; i < fSyntaxColorListModel.length; i++) {
			String colorKey= fSyntaxColorListModel[i][1];
			addTextKeyToCover(overlayKeys, colorKey);
		}
		
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}

	private void addTextKeyToCover(ArrayList overlayKeys, String mainKey) {
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, mainKey));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, mainKey + MakefileEditorPreferenceConstants.EDITOR_BOLD_SUFFIX));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, mainKey + MakefileEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		initializeDefaultColors();
		WorkbenchHelp.setHelp(getControl(), IMakeHelpContextIds.MAKE_EDITOR_PREFERENCE_PAGE);
		getOverlayStore().load();
		getOverlayStore().start();
		
		TabFolder folder= new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.appearance")); //$NON-NLS-1$
		item.setControl(createAppearancePage(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.syntax")); //$NON-NLS-1$
		item.setControl(createSyntaxPage(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.folding")); //$NON-NLS-1$
		item.setControl(createFoldingTabContent(folder));
					
		initialize();
		
		applyDialogFont(folder);
		return folder;
	}

	private void initializeDefaultColors() {	
		if (!getPreferenceStore().contains(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
			PreferenceConverter.setDefault(getOverlayStore(), AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, rgb);
		}
		if (!getPreferenceStore().contains(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB();
			PreferenceConverter.setDefault(getOverlayStore(), AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, rgb);
		}
		if (!getPreferenceStore().contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
			PreferenceConverter.setDefault(getOverlayStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, rgb);
		}
		if (!getPreferenceStore().contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT).getRGB();
			PreferenceConverter.setDefault(getOverlayStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, rgb);
		}
	}
	
	private void initialize() {
		
		initializeFields();
		
		for (int i= 0, n= fSyntaxColorListModel.length; i < n; i++) {
			fHighlightingColorList.add(
				new HighlightingColorListItem (fSyntaxColorListModel[i][0], fSyntaxColorListModel[i][1],
						fSyntaxColorListModel[i][1] + MakefileEditorPreferenceConstants.EDITOR_BOLD_SUFFIX, 
						fSyntaxColorListModel[i][1] + MakefileEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX, null));
		}
		fHighlightingColorListViewer.setInput(fHighlightingColorList);
		fHighlightingColorListViewer.setSelection(new StructuredSelection(fHighlightingColorListViewer.getElementAt(0)));
		
		for (int i= 0; i < fAppearanceColorListModel.length; i++) {
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		}
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAppearanceColorList != null && !fAppearanceColorList.isDisposed()) {
					fAppearanceColorList.select(0);
					handleAppearanceColorListSelection();
				}
			}
		});

		initializeFolding();
		initializeBackgroundColorFields();
	}

	void initializeFolding() {
		boolean enabled= getOverlayStore().getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		fFoldingCheckbox.setSelection(enabled);		
	}

	void initializeDefaultFolding() {
		boolean enabled= getOverlayStore().getDefaultBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		fFoldingCheckbox.setSelection(enabled);		
	}

	/**
	 * 
	 */
	private void initializeBackgroundColorFields() {
		RGB rgb= PreferenceConverter.getColor(getOverlayStore(), AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
		fBackgroundColorEditor.setColorValue(rgb);		
		
		boolean dflt= getOverlayStore().getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		fBackgroundDefaultRadioButton.setSelection(dflt);
		fBackgroundCustomRadioButton.setSelection(!dflt);
		fBackgroundColorButton.setEnabled(!dflt);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.AbstractMakefileEditorPreferencePage#handleDefaults()
	 */
	protected void handleDefaults() {
		handleAppearanceColorListSelection();
		handleSyntaxColorListSelection();
		initializeBackgroundColorFields();
		initializeDefaultFolding();
	}
	
	private Control createSyntaxPage(Composite parent) {
		
		Composite colorComposite= new Composite(parent, SWT.NONE);
		colorComposite.setLayout(new GridLayout());

		Group backgroundComposite= new Group(colorComposite, SWT.SHADOW_ETCHED_IN);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		backgroundComposite.setLayout(layout);
		backgroundComposite.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.backcolor")); //$NON-NLS-1$
	
		SelectionListener backgroundSelectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {				
				boolean custom= fBackgroundCustomRadioButton.getSelection();
				fBackgroundColorButton.setEnabled(custom);
				getOverlayStore().setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, !custom);
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		};

		fBackgroundDefaultRadioButton= new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
		fBackgroundDefaultRadioButton.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.systemdefault")); //$NON-NLS-1$
		fBackgroundDefaultRadioButton.addSelectionListener(backgroundSelectionListener);

		fBackgroundCustomRadioButton= new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
		fBackgroundCustomRadioButton.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.custom")); //$NON-NLS-1$
		fBackgroundCustomRadioButton.addSelectionListener(backgroundSelectionListener);
		
		fBackgroundColorEditor= new ColorEditor(backgroundComposite);
		fBackgroundColorButton= fBackgroundColorEditor.getButton();
		
		Label label= new Label(colorComposite, SWT.LEFT);
		label.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.Foreground")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite editorComposite= new Composite(colorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);		

		fHighlightingColorListViewer= new TableViewer(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fHighlightingColorListViewer.setLabelProvider(new ColorListLabelProvider());
		fHighlightingColorListViewer.setContentProvider(new ColorListContentProvider());
		fHighlightingColorListViewer.setSorter(new WorkbenchViewerSorter());
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(5);
		fHighlightingColorListViewer.getControl().setLayoutData(gd);
						
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		label= new Label(stylesComposite, SWT.LEFT);
		label.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.color")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fSyntaxForegroundColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fSyntaxForegroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
		
		fBoldCheckBox= new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.bold")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fBoldCheckBox.setLayoutData(gd);
		
		fItalicCheckBox= new Button(stylesComposite, SWT.CHECK);
		fItalicCheckBox.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.italic")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fItalicCheckBox.setLayoutData(gd);

		fHighlightingColorListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
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
				PreferenceConverter.setValue(getOverlayStore(), item.getColorKey(), fSyntaxForegroundColorEditor.getColorValue());
			}
		});

		fBackgroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				PreferenceConverter.setValue(getOverlayStore(), AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, fBackgroundColorEditor.getColorValue());					
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getOverlayStore().setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
			}
		});
				
		fItalicCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getOverlayStore().setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
			}
		});
				
		return colorComposite;
	}
	
	void handleAppearanceColorListSelection() {	
		int i= fAppearanceColorList.getSelectionIndex();
		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(getOverlayStore(), key);
		fAppearanceColorEditor.setColorValue(rgb);	
		updateAppearanceColorWidgets(fAppearanceColorListModel[i][2]);
	}

	private void updateAppearanceColorWidgets(String systemDefaultKey) {
		if (systemDefaultKey == null) {
			fAppearanceColorDefault.setSelection(false);
			fAppearanceColorDefault.setVisible(false);
			fAppearanceColorEditor.getButton().setEnabled(true);
		} else {
			boolean systemDefault= getOverlayStore().getBoolean(systemDefaultKey);
			fAppearanceColorDefault.setSelection(systemDefault);
			fAppearanceColorDefault.setVisible(true);
			fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
		}
	}

	private Control createAppearancePage(Composite parent) {
		Font font= parent.getFont();

		Composite appearanceComposite= new Composite(parent, SWT.NONE);
		appearanceComposite.setFont(font);
		GridLayout layout= new GridLayout(); 
		layout.numColumns= 2;
		appearanceComposite.setLayout(layout);

		String labelText= MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.printMarginColumn"); //$NON-NLS-1$
		String[] errorMessages= new String[]{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.empty_input_print_margin"), MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.invalid_input_print_margin")};  //$NON-NLS-1$//$NON-NLS-2$
		addTextField(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 3, 0, errorMessages);
		
		labelText= MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.tabwidth"); //$NON-NLS-1$
		errorMessages= new String[]{MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.notabwidth"), MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.39")}; //$NON-NLS-1$ //$NON-NLS-2$
		addTextField(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 3, 0, errorMessages);
		
//		labelText= MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.spacetabs"); //$NON-NLS-1$
//		addCheckBox(appearanceComposite, labelText, MakefileEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, 1);
		
		labelText= MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.showOverviewRuler"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 0);
				
		labelText= MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.showLineNumbers"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 0);

		labelText= MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.highlightCurrentLine"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 0);
				
		labelText= MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.showPrintMargin"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 0);


		Label label= new Label(appearanceComposite, SWT.LEFT );
		label.setFont(font);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= convertHeightInCharsToPixels(1) / 2;
		label.setLayoutData(gd);
		
		label= new Label(appearanceComposite, SWT.LEFT);
		label.setFont(font);
		label.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.appearanceOptions")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		Composite editorComposite= new Composite(appearanceComposite, SWT.NONE);
		editorComposite.setFont(font);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		fAppearanceColorList.setFont(font);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(6);
		fAppearanceColorList.setLayoutData(gd);
						
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		stylesComposite.setFont(font);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		label= new Label(stylesComposite, SWT.LEFT);
		label.setFont(font);
		label.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.color")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fAppearanceColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fAppearanceColorEditor.getButton();
		foregroundColorButton.setFont(font);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fAppearanceColorList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		
		SelectionListener colorDefaultSelectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean systemDefault= fAppearanceColorDefault.getSelection();
				fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
				
				int i= fAppearanceColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][2];
				if (key != null)
					getOverlayStore().setValue(key, systemDefault);
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		
		fAppearanceColorDefault= new Button(stylesComposite, SWT.CHECK);
		fAppearanceColorDefault.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.systemdefault")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fAppearanceColorDefault.setLayoutData(gd);
		fAppearanceColorDefault.setVisible(false);
		fAppearanceColorDefault.addSelectionListener(colorDefaultSelectionListener);
		
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i= fAppearanceColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][1];
				
				PreferenceConverter.setValue(getOverlayStore(), key, fAppearanceColorEditor.getColorValue());
			}
		});
		return appearanceComposite;
	}

	private Composite createFoldingTabContent(TabFolder folder) {
		Composite composite= new Composite(folder, SWT.NULL);
		// assume parent page uses griddata
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		composite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);
		
		
		/* check box for new editors */
		fFoldingCheckbox= new Button(composite, SWT.CHECK);
		fFoldingCheckbox.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.foldingenable")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fFoldingCheckbox.setLayoutData(gd);
		fFoldingCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean enabled= fFoldingCheckbox.getSelection(); 
				getOverlayStore().setValue(MakefileEditorPreferenceConstants.EDITOR_FOLDING_ENABLED, enabled);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		return composite;
	}

	void handleSyntaxColorListSelection() {
		HighlightingColorListItem item= getHighlightingColorListItem();
		RGB rgb= PreferenceConverter.getColor(getOverlayStore(), item.getColorKey());
		fSyntaxForegroundColorEditor.setColorValue(rgb);		
		fBoldCheckBox.setSelection(getOverlayStore().getBoolean(item.getBoldKey()));
		fItalicCheckBox.setSelection(getOverlayStore().getBoolean(item.getItalicKey()));
	}
	
	/**
	 * Returns the current highlighting color list item.
	 * 
	 * @return the current highlighting color list item
	 * @since 3.0
	 */
	HighlightingColorListItem getHighlightingColorListItem() {
		IStructuredSelection selection= (IStructuredSelection) fHighlightingColorListViewer.getSelection();
		return (HighlightingColorListItem) selection.getFirstElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		return super.performOk();
	}

	/**
	 * @param preferenceStore
	 */
	public static void initDefaults(IPreferenceStore prefs) {
		EditorsUI.useAnnotationsPreferencePage(prefs);
		EditorsUI.useQuickDiffPreferencePage(prefs);
		
		// Makefile Editor color preferences
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_COMMENT_COLOR, ColorManager.MAKE_COMMENT_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_DEFAULT_COLOR, ColorManager.MAKE_DEFAULT_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_FUNCTION_COLOR, ColorManager.MAKE_FUNCTION_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_KEYWORD_COLOR, ColorManager.MAKE_KEYWORD_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_MACRO_DEF_COLOR, ColorManager.MAKE_MACRO_DEF_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_MACRO_REF_COLOR, ColorManager.MAKE_MACRO_REF_RGB);		
	}

}
