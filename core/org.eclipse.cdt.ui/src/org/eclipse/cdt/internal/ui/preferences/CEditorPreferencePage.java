/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.editor.CEditor;

/*
 * The page for setting the editor options.
 */
public class CEditorPreferencePage extends AbstractPreferencePage implements IWorkbenchPreferencePage {

	protected final String[][] fAppearanceColorListModel = new String[][] { 
			{PreferencesMessages.CEditorPreferencePage_behaviorPage_matchingBracketColor, CEditor.MATCHING_BRACKETS_COLOR, null }, 
			{PreferencesMessages.CEditorPreferencePage_behaviorPage_inactiveCodeColor, CEditor.INACTIVE_CODE_COLOR, null }, 
	};

	protected List fList;
	protected ColorSelector fForegroundColorEditor;
	protected Button fBoldCheckBox;

	private CEditorHoverConfigurationBlock fCEditorHoverConfigurationBlock;
	private FoldingConfigurationBlock fFoldingConfigurationBlock;

	private List fAppearanceColorList;

	private ColorSelector fAppearanceColorEditor;

	private Button fAppearanceColorDefault;


	public CEditorPreferencePage() {
		super();
		setDescription(CUIPlugin.getResourceString("CEditorPreferencePage.description")); //$NON-NLS-1$
	}

	protected OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList overlayKeys = new ArrayList();
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.SUB_WORD_NAVIGATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.MATCHING_BRACKETS_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.MATCHING_BRACKETS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.INACTIVE_CODE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.INACTIVE_CODE_ENABLE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.SPACES_FOR_TABS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.ENSURE_NEWLINE_AT_EOF));
      
        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	public static void initDefaults(IPreferenceStore store) {

		// bug 84414: enable smart home/end handling
		// This is a hidden feature of the Eclipse TextEditor:
		// If enabled, the HOME button positions the cursor to the first
		// non-whitespace character of the line (ie. the logical start of 
		// the line). Pressing it a second time, positions the cursor to the 
		// first character of the line, ie. to the physical start of the line.
		// The END button works correspondingly for the end of the line.
		// JDT also enables this feature.
		store.setDefault(AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END, true);    

		store.setDefault(CEditor.SUB_WORD_NAVIGATION, true);
		
		store.setDefault(CEditor.MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(store, CEditor.MATCHING_BRACKETS_COLOR, new RGB(170,170,170));

		store.setDefault(CEditor.INACTIVE_CODE_ENABLE, true);
		PreferenceConverter.setDefault(store, CEditor.INACTIVE_CODE_COLOR, new RGB(224, 224, 224));

		store.setDefault(CEditor.SPACES_FOR_TABS, false);

	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.C_EDITOR_PREF_PAGE);
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

	private Control createAppearancePage(Composite parent) {

		Composite behaviorComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		behaviorComposite.setLayout(layout);

		String label= PreferencesMessages.CEditorPreferencePage_behaviorPage_subWordNavigation; 
		addCheckBox(behaviorComposite, label, CEditor.SUB_WORD_NAVIGATION, 0);

		label = PreferencesMessages.CEditorPreferencePage_behaviorPage_matchingBrackets; 
		addCheckBox(behaviorComposite, label, CEditor.MATCHING_BRACKETS, 0);

		label = PreferencesMessages.CEditorPreferencePage_behaviorPage_inactiveCode; 
		addCheckBox(behaviorComposite, label, CEditor.INACTIVE_CODE_ENABLE, 0);

		label = PreferencesMessages.CEditorPreferencePage_behaviorPage_tabSpace; 
		addCheckBox(behaviorComposite, label, CEditor.SPACES_FOR_TABS, 0);

		label = PreferencesMessages.CEditorPreferencePage_behaviorPage_ensureNewline; 
		addCheckBox(behaviorComposite, label, PreferenceConstants.ENSURE_NEWLINE_AT_EOF, 0);

		Label l = new Label(behaviorComposite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);

		l = new Label(behaviorComposite, SWT.LEFT);
		l.setText(PreferencesMessages.CEditorPreferencePage_behaviorPage_appearanceColorOptions); 
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
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint = convertHeightInCharsToPixels(8);
		fAppearanceColorList.setLayoutData(gd);

		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		l = new Label(stylesComposite, SWT.LEFT);
		l.setText(PreferencesMessages.CEditorPreferencePage_behaviorPage_Color); 
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		l.setLayoutData(gd);

		fAppearanceColorEditor = new ColorSelector(stylesComposite);
		Button foregroundColorButton = fAppearanceColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		SelectionListener colorDefaultSelectionListener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean systemDefault= fAppearanceColorDefault.getSelection();
				fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
				
				int i= fAppearanceColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][2];
				if (key != null)
					fOverlayStore.setValue(key, systemDefault);
			}
		};
		
		fAppearanceColorDefault= new Button(stylesComposite, SWT.CHECK);
		fAppearanceColorDefault.setText(PreferencesMessages.CEditorPreferencePage_colorPage_systemDefault); 
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fAppearanceColorDefault.setLayoutData(gd);
		fAppearanceColorDefault.setVisible(false);
		fAppearanceColorDefault.addSelectionListener(colorDefaultSelectionListener);

		fAppearanceColorList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i = fAppearanceColorList.getSelectionIndex();
				String key = fAppearanceColorListModel[i][1];

				PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColorEditor.getColorValue());
			}
		});
		return behaviorComposite;
	}

	private void handleAppearanceColorListSelection() {
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

	private Control createHeader(Composite parent) {
		String text = PreferencesMessages.CEditorPreferencePage_link; 
		Link link = new Link(parent, SWT.NONE);
		link.setText(text);
		link.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				String u = event.text;
				PreferencesUtil.createPreferenceDialogOn(getShell(), u, null, null);
			}
		});

		GridData gridData= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.widthHint= 150; // only expand further if anyone else requires it
		link.setLayoutData(gridData);
		return link;
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		fCEditorHoverConfigurationBlock= new CEditorHoverConfigurationBlock(this, fOverlayStore);
		fFoldingConfigurationBlock= new FoldingConfigurationBlock(fOverlayStore);

		fOverlayStore.load();
		fOverlayStore.start();

		createHeader(parent);

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.CEditorPreferencePage_generalTabTitle); 
		item.setControl(createAppearancePage(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.CEditorPreferencePage_hoverTab_title); 
		item.setControl(fCEditorHoverConfigurationBlock.createControl(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.CEditorPreferencePage_folding_title); 
		item.setControl(fFoldingConfigurationBlock.createControl(folder));

		initialize();

		return folder;
	}

	private void initialize() {

		initializeFields();

		for (int i = 0; i < fAppearanceColorListModel.length; i++) {
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		}
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fAppearanceColorList.select(0);
				handleAppearanceColorListSelection();
			}
		});

		fFoldingConfigurationBlock.initialize();

	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		fCEditorHoverConfigurationBlock.performOk();
		fFoldingConfigurationBlock.performOk();
		return super.performOk();
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {

		super.performDefaults();

		handleAppearanceColorListSelection();

		fCEditorHoverConfigurationBlock.performDefaults();
		fFoldingConfigurationBlock.performDefaults();

	}

	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {

		fFoldingConfigurationBlock.dispose();

		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore = null;
		}

		super.dispose();
	}

}
