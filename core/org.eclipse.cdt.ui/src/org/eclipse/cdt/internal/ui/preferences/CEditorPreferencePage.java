package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

/*
 * The page for setting the editor options.
 */
public class CEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	protected final String[][] fListModel = new String[][] { { PreferencesMessages.getString("CEditorPreferencePage.cCommentTaskTags.MultiLine"), ICColorConstants.C_MULTI_LINE_COMMENT }, { //$NON-NLS-1$
		PreferencesMessages.getString("CEditorPreferencePage.cCommentTaskTags.singleLine"), ICColorConstants.C_SINGLE_LINE_COMMENT }, { //$NON-NLS-1$
		PreferencesMessages.getString("CEditorPreferencePage.cCommentTaskTags.keywords"), ICColorConstants.C_KEYWORD }, { //$NON-NLS-1$
		PreferencesMessages.getString("CEditorPreferencePage.cCommentTaskTags.builtInTypes"), ICColorConstants.C_TYPE }, { //$NON-NLS-1$
		PreferencesMessages.getString("CEditorPreferencePage.cCommentTaskTags.strings"), ICColorConstants.C_STRING }, { //$NON-NLS-1$
		PreferencesMessages.getString("CEditorPreferencePage.cCommentTaskTags.others"), ICColorConstants.C_DEFAULT }, { //$NON-NLS-1$
        PreferencesMessages.getString("CEditorPreferencePage.cCommentTaskTags"), PreferenceConstants.EDITOR_TASK_TAG_COLOR } //$NON-NLS-1$
	};

	protected final String[][] fAppearanceColorListModel = new String[][] { { PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.lineNumberColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, null }, //$NON-NLS-1$
		{PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.matchingBracketColor"), CEditor.MATCHING_BRACKETS_COLOR, null }, //$NON-NLS-1$
		{PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.currentLineHighlightColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, null }, //$NON-NLS-1$
		{PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.printMarginColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, null }, //$NON-NLS-1$
		{PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.linkedPositionColor"), CEditor.LINKED_POSITION_COLOR, null }, //$NON-NLS-1$
		{PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.selectionForegroundColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR}, //$NON-NLS-1$
		{PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.selectionBackgroundColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR}, //$NON-NLS-1$
	};

	protected OverlayPreferenceStore fOverlayStore;
	private CTextTools fCTextTools;

	protected Map fColorButtons = new HashMap();
	private SelectionListener fColorButtonListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			ColorEditor editor = (ColorEditor) e.widget.getData();
			PreferenceConverter.setValue(fOverlayStore, (String) fColorButtons.get(editor), editor.getColorValue());
		}
	};

	protected Map fCheckBoxes = new HashMap();
	private SelectionListener fCheckBoxListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
		}
	};

	protected Map fTextFields = new HashMap();
	private ModifyListener fTextFieldListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Text text = (Text) e.widget;
			fOverlayStore.setValue((String) fTextFields.get(text), text.getText());
		}
	};
	/**
	 * List of master/slave listeners when there's a dependency.
	 * 
	 * @see #createDependency(Button, String, Control)
	 * @since 3.0
	 */
	private ArrayList fMasterSlaveListeners= new ArrayList();

	protected List fList;
	protected ColorEditor fForegroundColorEditor;
	protected ColorEditor fBackgroundColorEditor;
	private Button fBackgroundDefaultRadioButton;
	protected Button fBackgroundCustomRadioButton;
	protected Button fBackgroundColorButton;
	protected Button fBoldCheckBox;
	protected SourceViewer fPreviewViewer;

	protected List fAppearanceColorList;
	protected ColorEditor fAppearanceColorEditor;
	private Button fAppearanceColorDefault;
	private CEditorHoverConfigurationBlock fCEditorHoverConfigurationBlock;
	private FoldingConfigurationBlock fFoldingConfigurationBlock;


	public CEditorPreferencePage() {
		setDescription(CUIPlugin.getResourceString("CEditorPreferencePage.description")); //$NON-NLS-1$
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(), createOverlayStoreKeys());
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList overlayKeys = new ArrayList();
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.PREFERENCE_COLOR_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,CEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.PREFERENCE_COLOR_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_MULTI_LINE_COMMENT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_MULTI_LINE_COMMENT + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_SINGLE_LINE_COMMENT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_SINGLE_LINE_COMMENT + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_KEYWORD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_KEYWORD + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_TYPE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_TYPE + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_STRING));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_STRING + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_DEFAULT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_DEFAULT + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.MATCHING_BRACKETS_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.MATCHING_BRACKETS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.SPACES_FOR_TABS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.LINKED_POSITION_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ContentAssistPreference.AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ContentAssistPreference.TIMEOUT_DELAY));		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PROPOSALS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PROPOSALS_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PARAMETERS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PARAMETERS_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.SHOW_DOCUMENTED_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ORDER_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ADD_INCLUDE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE));        
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.PROJECT_SEARCH_SCOPE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, PreferenceConstants.EDITOR_TASK_TAG_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_TASK_TAG_BOLD));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, PreferenceConstants.EDITOR_TASK_INDICATION_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_TASK_INDICATION));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.HYPERLINK_ENABLED));
      
        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	public static void initDefaults(IPreferenceStore store) {

		store.setDefault(CEditor.MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(store, CEditor.MATCHING_BRACKETS_COLOR, new RGB(170,170,170));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, true);
		PreferenceConverter.setDefault(store, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, new RGB(225, 235, 224));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, true);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 80);
		PreferenceConverter.setDefault(store, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, new RGB(176, 180, 185));

		store.setDefault(CEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, true);

		store.setDefault(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);

		store.setDefault(CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH, 4);

		store.setDefault(CEditor.SPACES_FOR_TABS, false);

		PreferenceConverter.setDefault(store, ICColorConstants.C_MULTI_LINE_COMMENT, new RGB(63, 127, 95));
		store.setDefault(ICColorConstants.C_MULTI_LINE_COMMENT + "_bold", false); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, ICColorConstants.C_SINGLE_LINE_COMMENT, new RGB(63, 125, 95));
		store.setDefault(ICColorConstants.C_SINGLE_LINE_COMMENT + "_bold", false); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, ICColorConstants.C_KEYWORD, new RGB(127, 0, 85));
		store.setDefault(ICColorConstants.C_KEYWORD + "_bold", true); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, ICColorConstants.C_TYPE, new RGB(127, 0, 85));
		store.setDefault(ICColorConstants.C_TYPE + "_bold", true); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, ICColorConstants.C_STRING, new RGB(42, 0, 255));
		store.setDefault(ICColorConstants.C_STRING + "_bold", false); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, ICColorConstants.C_DEFAULT, new RGB(0, 0, 0));
		store.setDefault(ICColorConstants.C_DEFAULT + "_bold", false); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, CEditor.LINKED_POSITION_COLOR, new RGB(0, 200, 100));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, false);
		PreferenceConverter.setDefault(store, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, new RGB(0, 0, 0));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, true);

		store.setDefault(ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE, true);
		store.setDefault(ContentAssistPreference.PROJECT_SEARCH_SCOPE, false);

		store.setDefault(ContentAssistPreference.TIMEOUT_DELAY, 3000);
		
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_DELAY, 500);
		
		store.setDefault(ContentAssistPreference.AUTOINSERT, true);
		PreferenceConverter.setDefault(store, ContentAssistPreference.PROPOSALS_BACKGROUND, new RGB(254, 241, 233));
		PreferenceConverter.setDefault(store, ContentAssistPreference.PROPOSALS_FOREGROUND, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, ContentAssistPreference.PARAMETERS_BACKGROUND, new RGB(254, 241, 233));
		PreferenceConverter.setDefault(store, ContentAssistPreference.PARAMETERS_FOREGROUND, new RGB(0, 0, 0));
		store.setDefault(ContentAssistPreference.ORDER_PROPOSALS, false);
		store.setDefault(ContentAssistPreference.ADD_INCLUDE, true);
		
		store.setDefault(CEditor.HYPERLINK_ENABLED,true);

		// override default extended text editor prefs
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, true);
	}

	/**
	 * 
	 */
	private void initializeDefaultColors() {
		if (!getPreferenceStore().contains(CEditor.PREFERENCE_COLOR_FOREGROUND)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB();
			PreferenceConverter.setDefault(fOverlayStore, CEditor.PREFERENCE_COLOR_FOREGROUND, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), CEditor.PREFERENCE_COLOR_FOREGROUND, rgb);
		}

		if (!getPreferenceStore().contains(CEditor.PREFERENCE_COLOR_BACKGROUND)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
			PreferenceConverter.setDefault(fOverlayStore, CEditor.PREFERENCE_COLOR_BACKGROUND, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), CEditor.PREFERENCE_COLOR_BACKGROUND, rgb);
		}
		
		if (!getPreferenceStore().contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
			PreferenceConverter.setDefault(fOverlayStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, rgb);
		}
		if (!getPreferenceStore().contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT).getRGB();
			PreferenceConverter.setDefault(fOverlayStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, rgb);
		}
		
	}

	/*
	 * @see IWorkbenchPreferencePage#init()
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(parent, ICHelpContextIds.C_EDITOR_PREF_PAGE);	
	}

	protected void handleListSelection() {
		int i = fList.getSelectionIndex();
		String key = fListModel[i][1];
		RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
		fForegroundColorEditor.setColorValue(rgb);
		fBoldCheckBox.setSelection(fOverlayStore.getBoolean(key + "_bold")); //$NON-NLS-1$
	}	

	private Control createColorPage(Composite parent) {

		Composite colorComposite = new Composite(parent, SWT.NULL);
		colorComposite.setLayout(new GridLayout());

		Composite backgroundComposite = new Composite(colorComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		backgroundComposite.setLayout(layout);

		Label label = new Label(backgroundComposite, SWT.NULL);
		label.setText(PreferencesMessages.getString("CEditorPreferencePage.colorPage.backgroundColor")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		SelectionListener backgroundSelectionListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean custom = fBackgroundCustomRadioButton.getSelection();
				fBackgroundColorButton.setEnabled(custom);
				fOverlayStore.setValue(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, !custom);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		fBackgroundDefaultRadioButton = new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
		fBackgroundDefaultRadioButton.setText(PreferencesMessages.getString("CEditorPreferencePage.colorPage.systemDefault")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fBackgroundDefaultRadioButton.setLayoutData(gd);
		fBackgroundDefaultRadioButton.addSelectionListener(backgroundSelectionListener);

		fBackgroundCustomRadioButton = new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
		fBackgroundCustomRadioButton.setText(PreferencesMessages.getString("CEditorPreferencePage.colorPage.custom")); //$NON-NLS-1$
		fBackgroundCustomRadioButton.addSelectionListener(backgroundSelectionListener);

		fBackgroundColorEditor = new ColorEditor(backgroundComposite);
		fBackgroundColorButton = fBackgroundColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		fBackgroundColorButton.setLayoutData(gd);

		label = new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages.getString("CEditorPreferencePage.colorPage.foreground")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite editorComposite = new Composite(colorComposite, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);

		fList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(5);
		fList.setLayoutData(gd);

		Composite stylesComposite = new Composite(editorComposite, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		label = new Label(stylesComposite, SWT.LEFT);
		label.setText(PreferencesMessages.getString("CEditorPreferencePage.colorPage.color")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		fForegroundColorEditor = new ColorEditor(stylesComposite);
		Button foregroundColorButton = fForegroundColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText(PreferencesMessages.getString("CEditorPreferencePage.colorPage.bold"));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		gd.horizontalAlignment = GridData.BEGINNING;
		fBoldCheckBox.setLayoutData(gd);

		label = new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages.getString("CEditorPreferencePage.colorPage.preview")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control previewer = createPreviewer(colorComposite);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = convertWidthInCharsToPixels(80);
		gd.heightHint = convertHeightInCharsToPixels(15);
		previewer.setLayoutData(gd);

		fList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleListSelection();
			}
		});

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i = fList.getSelectionIndex();
				String key = fListModel[i][1];

				PreferenceConverter.setValue(fOverlayStore, key, fForegroundColorEditor.getColorValue());
			}
		});

		fBackgroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				PreferenceConverter.setValue(
					fOverlayStore,
					CEditor.PREFERENCE_COLOR_BACKGROUND,
					fBackgroundColorEditor.getColorValue());
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i = fList.getSelectionIndex();
				String key = fListModel[i][1];
				fOverlayStore.setValue(key + "_bold", fBoldCheckBox.getSelection()); //$NON-NLS-1$
			}
		});

		WorkbenchHelp.setHelp(colorComposite, ICHelpContextIds.C_EDITOR_COLORS_PREF_PAGE);	
		return colorComposite;
	}

	private Control createPreviewer(Composite parent) {

		fCTextTools = new CTextTools(fOverlayStore);

		fPreviewViewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
		fPreviewViewer.configure(new CSourceViewerConfiguration(fCTextTools, null));
		fPreviewViewer.getTextWidget().setFont(JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
		fPreviewViewer.setEditable(false);

		initializeViewerColors(fPreviewViewer);

		String content = loadPreviewContentFromFile("ColorSettingPreviewCode.txt"); //$NON-NLS-1$
		IDocument document = new Document(content);
		IDocumentPartitioner partitioner = fCTextTools.createDocumentPartitioner();
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);

		fPreviewViewer.setDocument(document);

		fOverlayStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String p = event.getProperty();
				if (p.equals(CEditor.PREFERENCE_COLOR_BACKGROUND)
					|| p.equals(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
					initializeViewerColors(fPreviewViewer);
				}

				fPreviewViewer.getDocument().set(fPreviewViewer.getDocument().get());
				//fPreviewViewer.refresh();
			}
		});

		return fPreviewViewer.getControl();
	}

	private Color fBackgroundColor;

	/**
	 * Initializes the given viewer's colors.
	 * 
	 * @param viewer the viewer to be initialized
	 */
	protected void initializeViewerColors(ISourceViewer viewer) {

		IPreferenceStore store = fOverlayStore;
		if (store != null) {

			StyledText styledText = viewer.getTextWidget();

			// ---------- background color ----------------------
			Color color =
				store.getBoolean(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
					? null
					: createColor(store, CEditor.PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);

			if (fBackgroundColor != null)
				fBackgroundColor.dispose();

			fBackgroundColor = color;
		}
	}

	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {

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

	// sets enabled flag for a control and all its sub-tree
	protected static void setEnabled(Control control, boolean enable) {
		control.setEnabled(enable);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++)
				setEnabled(children[i], enable);
		}
	}

	private ArrayList fNumberFields = new ArrayList();
	private ModifyListener fNumberFieldListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text) e.widget);
		}
	};

	protected void handleAppearanceColorListSelection() {
		int i = fAppearanceColorList.getSelectionIndex();
		String key = fAppearanceColorListModel[i][1];
		RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
		fAppearanceColorEditor.setColorValue(rgb);
		updateAppearanceColorWidgets(fAppearanceColorListModel[i][2]);
	}

	private void updateAppearanceColorWidgets(String systemDefaultKey) {
		if (systemDefaultKey == null) {
			fAppearanceColorDefault.setSelection(false);
			fAppearanceColorDefault.setVisible(false);
			fAppearanceColorEditor.getButton().setEnabled(true);
		} else {
			boolean systemDefault= fOverlayStore.getBoolean(systemDefaultKey);
			fAppearanceColorDefault.setSelection(systemDefault);
			fAppearanceColorDefault.setVisible(true);
			fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
		}
	}

	private Control createBehaviorPage(Composite parent) {

		Composite behaviorComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		behaviorComposite.setLayout(layout);

		String label = PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.tabWidth"); //$NON-NLS-1$
		addTextField(behaviorComposite, label, CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH, 3, 0, true);

		label = PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.marginColumn"); //$NON-NLS-1$
		addTextField(behaviorComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 3, 0, true);

		label = PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.showOverviewRuler"); //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.showLineNumbers"); //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.matchingBrackets"); //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, CEditor.MATCHING_BRACKETS, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.highlightLine"); //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.printMargin"); //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.tabSpace"); //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, CEditor.SPACES_FOR_TABS, 0);

		label= PreferencesMessages.getString("CEditorPreferencePage.accessibility.disableCustomCarets"); //$NON-NLS-1$
		Button master= addCheckBox(behaviorComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, 0);

		label= PreferencesMessages.getString("CEditorPreferencePage.accessibility.wideCaret"); //$NON-NLS-1$
		Button slave= addCheckBox(behaviorComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET, 0);
		createDependency(master, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, slave);

	
		Label l = new Label(behaviorComposite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);

		l = new Label(behaviorComposite, SWT.LEFT);
		l.setText(PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.appearanceColorOptions")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		l.setLayoutData(gd);

		Composite editorComposite = new Composite(behaviorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan = 2;
		editorComposite.setLayoutData(gd);

		fAppearanceColorList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(5);
		fAppearanceColorList.setLayoutData(gd);

		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		l = new Label(stylesComposite, SWT.LEFT);
		l.setText(PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.Color")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		l.setLayoutData(gd);

		fAppearanceColorEditor = new ColorEditor(stylesComposite);
		Button foregroundColorButton = fAppearanceColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		SelectionListener colorDefaultSelectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean systemDefault= fAppearanceColorDefault.getSelection();
				fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
				
				int i= fAppearanceColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][2];
				if (key != null)
					fOverlayStore.setValue(key, systemDefault);
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		
		fAppearanceColorDefault= new Button(stylesComposite, SWT.CHECK);
		fAppearanceColorDefault.setText(PreferencesMessages.getString("CEditorPreferencePage.behaviorPage.systemDefault")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fAppearanceColorDefault.setLayoutData(gd);
		fAppearanceColorDefault.setVisible(false);
		fAppearanceColorDefault.addSelectionListener(colorDefaultSelectionListener);
		
		fAppearanceColorList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i = fAppearanceColorList.getSelectionIndex();
				String key = fAppearanceColorListModel[i][1];

				PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColorEditor.getColorValue());
			}
		});

		return behaviorComposite;
	}

	private static void indent(Control control) {
		GridData gridData= new GridData();
		gridData.horizontalIndent= 20;
		control.setLayoutData(gridData);		
	}

	private void createDependency(final Button master, String masterKey, final Control slave) {
		indent(slave);
		boolean masterState= fOverlayStore.getBoolean(masterKey);
		slave.setEnabled(masterState);
		SelectionListener listener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				slave.setEnabled(master.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		master.addSelectionListener(listener);
		fMasterSlaveListeners.add(listener);
	}

	private Control createContentAssistPage(Composite parent) {

		Composite contentAssistComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		contentAssistComposite.setLayout(layout);
		
		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// The following three radio buttons are grouped together
		String label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.searchGroupTitle"); //$NON-NLS-1$
		Group searchGroup = addGroupBox(contentAssistComposite, label, 2);
		
		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.searchGroupCurrentFileOption"); //$NON-NLS-1$
		addRadioButton(searchGroup, label, ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE, 0);
		
		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.searchGroupCurrentProjectOption"); //$NON-NLS-1$
		addRadioButton(searchGroup, label, ContentAssistPreference.PROJECT_SEARCH_SCOPE, 0);
		
		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.insertSingleProposalAutomatically"); //$NON-NLS-1$
		addCheckBox(contentAssistComposite, label, ContentAssistPreference.AUTOINSERT, 0);
		
		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.showProposalsInAlphabeticalOrder"); //$NON-NLS-1$
		addCheckBox(contentAssistComposite, label, ContentAssistPreference.ORDER_PROPOSALS, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.timeoutDelay"); //$NON-NLS-1$
		addTextField(contentAssistComposite, label, ContentAssistPreference.TIMEOUT_DELAY, 6, 0, true);


		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// The following items are grouped for Auto Activation
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationGroupTitle"); //$NON-NLS-1$
		Group enableGroup = addGroupBox(contentAssistComposite, label, 2);
		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationEnableDot"); //$NON-NLS-1$
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationEnableArrow"); //$NON-NLS-1$
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW, 0);
		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationEnableDoubleColon"); //$NON-NLS-1$
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON, 0);
		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationDelay"); //$NON-NLS-1$
		addTextField(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_DELAY, 4, 0, true);

		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.completionProposalBackgroundColor"); //$NON-NLS-1$
		addColorButton(contentAssistComposite, label, ContentAssistPreference.PROPOSALS_BACKGROUND, 0);

		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.completionProposalForegroundColor"); //$NON-NLS-1$
		addColorButton(contentAssistComposite, label, ContentAssistPreference.PROPOSALS_FOREGROUND, 0);

//		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.parameterBackgroundColor"); 
//		addColorButton(contentAssistComposite, label, ContentAssistPreference.PARAMETERS_BACKGROUND, 0);
//
//		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.parameterForegroundColor");
//		addColorButton(contentAssistComposite, label, ContentAssistPreference.PARAMETERS_FOREGROUND, 0);

		WorkbenchHelp.setHelp(contentAssistComposite, ICHelpContextIds.C_EDITOR_CONTENT_ASSIST_PREF_PAGE);	

		return contentAssistComposite;
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		initializeDefaultColors();

		fCEditorHoverConfigurationBlock= new CEditorHoverConfigurationBlock(this, fOverlayStore);
		fFoldingConfigurationBlock= new FoldingConfigurationBlock(fOverlayStore);

		fOverlayStore.load();
		fOverlayStore.start();

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("CEditorPreferencePage.generalTabTitle")); //$NON-NLS-1$
		item.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT));
		item.setControl(createBehaviorPage(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("CEditorPreferencePage.colorsTabTitle")); //$NON-NLS-1$
		item.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT));
		item.setControl(createColorPage(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("CEditorPreferencePage.contentAssistTabTitle")); //$NON-NLS-1$
		item.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT));
		item.setControl(createContentAssistPage(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("CEditorPreferencePage.hoverTab.title")); //$NON-NLS-1$
		item.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT));
		item.setControl(fCEditorHoverConfigurationBlock.createControl(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("CEditorPreferencePage.folding.title")); //$NON-NLS-1$
		item.setControl(fFoldingConfigurationBlock.createControl(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("CEditorPreferencePage.Navigation")); //$NON-NLS-1$
		item.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT));
		item.setControl(createNavPage(folder));
		
		initialize();

		return folder;
	}

	/**
	 * @param folder
	 * @return
	 */
	private Control createNavPage(Composite parent) {
		Composite navComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		navComposite.setLayout(layout);

		Button navCheck = new Button(navComposite,SWT.CHECK);
		navCheck.setText(PreferencesMessages.getString("CEditorPreferencePage.Enable_Hyperlink_Navigation")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		navCheck.setLayoutData(gd);
		navCheck.addSelectionListener(fCheckBoxListener);
		fCheckBoxes.put(navCheck, CEditor.HYPERLINK_ENABLED);
		
		WorkbenchHelp.setHelp(navComposite, ICHelpContextIds.C_EDITOR_NAVIGATION_PAGE);	
		return navComposite;
	}

	private void initialize() {

		initializeFields();

		for (int i = 0; i < fListModel.length; i++)
			fList.add(fListModel[i][0]);
		fList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fList.select(0);
				handleListSelection();
			}
		});

		for (int i = 0; i < fAppearanceColorListModel.length; i++)
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fAppearanceColorList.select(0);
				handleAppearanceColorListSelection();
			}
		});

		fFoldingConfigurationBlock.initialize();

	}

	private void initializeFields() {

		Iterator e = fColorButtons.keySet().iterator();
		while (e.hasNext()) {
			ColorEditor c = (ColorEditor) e.next();
			String key = (String) fColorButtons.get(c);
			RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
			c.setColorValue(rgb);
		}

		e = fCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b = (Button) e.next();
			String key = (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}

		e = fTextFields.keySet().iterator();
		while (e.hasNext()) {
			Text t = (Text) e.next();
			String key = (String) fTextFields.get(t);
			t.setText(fOverlayStore.getString(key));
		}

		RGB rgb = PreferenceConverter.getColor(fOverlayStore, CEditor.PREFERENCE_COLOR_BACKGROUND);
		fBackgroundColorEditor.setColorValue(rgb);

		boolean default_ = fOverlayStore.getBoolean(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		fBackgroundDefaultRadioButton.setSelection(default_);
		fBackgroundCustomRadioButton.setSelection(!default_);
		fBackgroundColorButton.setEnabled(!default_);

        // Update slaves
        Iterator iter= fMasterSlaveListeners.iterator();
        while (iter.hasNext()) {
            SelectionListener listener= (SelectionListener)iter.next();
            listener.widgetSelected(null);
        }
		//updateAutoactivationControls();
	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		fCEditorHoverConfigurationBlock.performOk();
		fFoldingConfigurationBlock.performOk();
		fOverlayStore.propagate();
		return true;
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {

		fOverlayStore.loadDefaults();
		initializeFields();
		handleListSelection();
		handleAppearanceColorListSelection();

		fCEditorHoverConfigurationBlock.performDefaults();
		fFoldingConfigurationBlock.performDefaults();

		super.performDefaults();

		fPreviewViewer.invalidateTextPresentation();
	}

	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {

		fFoldingConfigurationBlock.dispose();

		if (fCTextTools != null) {
			fCTextTools = null;
		}

		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore = null;
		}

		super.dispose();
	}

	private Control addColorButton(Composite parent, String label, String key, int indentation) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		composite.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(label);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		labelControl.setLayoutData(gd);

		ColorEditor editor = new ColorEditor(composite);
		Button button = editor.getButton();
		button.setData(editor);

		gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		button.setLayoutData(gd);
		button.addSelectionListener(fColorButtonListener);

		fColorButtons.put(editor, key);

		return composite;
	}

	private Group addGroupBox(Composite parent, String label, int nColumns ){
		Group group = new Group(parent, SWT.NONE);
		group.setText(label);
		GridLayout layout = new GridLayout();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		layout.numColumns = nColumns;
		group.setLayout(layout);
		group.setLayoutData(gd);
		return group;
	}
	
	private Button addCheckBox(Composite parent, String label, String key, int indentation) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);

		fCheckBoxes.put(checkBox, key);

		return checkBox;
	}

	private Button addRadioButton(Composite parent, String label, String key, int indentation) {
		Button radioButton = new Button(parent, SWT.RADIO);
		radioButton.setText(label);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 2;
		radioButton.setLayoutData(gd);
		radioButton.addSelectionListener(fCheckBoxListener);

		fCheckBoxes.put(radioButton, key);

		return radioButton;
	}
	
	private Control addTextField(Composite composite, String label, String key, int textLimit, int indentation, boolean isNumber) {

		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indentation;
		labelControl.setLayoutData(gd);

		Text textControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		fTextFields.put(textControl, key);
		if (isNumber) {
			fNumberFields.add(textControl);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}

		return textControl;
	}

	private String loadPreviewContentFromFile(String filename) {
		String line;
		String separator = System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer(512);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			CUIPlugin.getDefault().log(io);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return buffer.toString();
	}

	protected void numberFieldChanged(Text textControl) {
		String number = textControl.getText();
		IStatus status = validatePositiveNumber(number);
		if (!status.matches(IStatus.ERROR))
			fOverlayStore.setValue((String) fTextFields.get(textControl), number);
		updateStatus(status);
	}

	private IStatus validatePositiveNumber(String number) {
		StatusInfo status = new StatusInfo();
		if (number.length() == 0) {
			status.setError(PreferencesMessages.getString("CEditorPreferencePage.empty_input")); //$NON-NLS-1$
		} else {
			try {
				int value = Integer.parseInt(number);
				if (value < 0)
					status.setError(PreferencesMessages.getString("CEditorPreferencePage.invalid_input")); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				status.setError(PreferencesMessages.getString("CEditorPreferencePage.invalid_input")); //$NON-NLS-1$
			}
		}
		return status;
	}

	void updateStatus(IStatus status) {
		if (!status.matches(IStatus.ERROR)) {
			for (int i = 0; i < fNumberFields.size(); i++) {
				Text text = (Text) fNumberFields.get(i);
				IStatus s = validatePositiveNumber(text.getText());
				status = StatusUtil.getMoreSevere(s, status);
			}
		}
		status= StatusUtil.getMoreSevere(fCEditorHoverConfigurationBlock.getStatus(), status);
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}
}
