/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.cdt.internal.ui.search.CSearchPage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WorkInProgressPreferencePage extends PreferencePage
		implements
			IWorkbenchPreferencePage {
	
	private Button fBackgroundTypeCacheEnabled;
	protected OverlayPreferenceStore fOverlayStore;

	public WorkInProgressPreferencePage(){
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		fOverlayStore  = createOverlayStore();
	}

	private OverlayPreferenceStore createOverlayStore() {
		ArrayList overlayKeys = new ArrayList();		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CSearchPage.EXTERNALMATCH_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, CSearchPage.EXTERNALMATCH_VISIBLE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, SourceIndexer.CDT_INDEXER_TIMEOUT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AllTypesCache.ENABLE_BACKGROUND_TYPE_CACHE));
		
        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();
		
		initializeDialogUnits(parent);
		
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		result.setLayout(layout);
		
		Group backgroundTypeCacheGroup= new Group(result, SWT.NONE);
		backgroundTypeCacheGroup.setLayout(new GridLayout());
		backgroundTypeCacheGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		backgroundTypeCacheGroup.setText("Open Type"); //$NON-NLS-1$

		fBackgroundTypeCacheEnabled = createCheckButton(backgroundTypeCacheGroup, "Cache types in background"); //$NON-NLS-1$
		fBackgroundTypeCacheEnabled.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				fOverlayStore.setValue(AllTypesCache.ENABLE_BACKGROUND_TYPE_CACHE, button.getSelection());
			}
		});
		
		Group editorCorrectionGroup= new Group(result, SWT.NONE);
		editorCorrectionGroup.setLayout(new GridLayout());
		editorCorrectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editorCorrectionGroup.setText("Editor"); //$NON-NLS-1$

		initialize(); 
		
		return result;
	
	}
	
	private void initialize(){
		fBackgroundTypeCacheEnabled.setSelection(fOverlayStore.getBoolean(AllTypesCache.ENABLE_BACKGROUND_TYPE_CACHE));
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Combo createComboBox( Composite parent, String label, String[] items, String selection )
	{
		ControlFactory.createLabel( parent, label );
		Combo combo = ControlFactory.createSelectCombo( parent, items, selection );
		combo.setLayoutData( new GridData() );
		return combo;
	}
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Button createCheckButton( Composite parent, String label )
	{
		Button button = new Button( parent, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
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
		
		return textControl;
	}
	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		fOverlayStore.propagate();
		
//		Store IProblem Marker value in CCorePlugin Preferences 
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		
		prefs.setValue(AllTypesCache.ENABLE_BACKGROUND_TYPE_CACHE, fOverlayStore.getString(AllTypesCache.ENABLE_BACKGROUND_TYPE_CACHE));
		CCorePlugin.getDefault().savePluginPreferences();
		
		return true;
	}
	
	/**
	 * @param store
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(AllTypesCache.ENABLE_BACKGROUND_TYPE_CACHE, false);
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		
		if (prefs != null){
			prefs.setValue(AllTypesCache.ENABLE_BACKGROUND_TYPE_CACHE, store.getString(AllTypesCache.ENABLE_BACKGROUND_TYPE_CACHE));
			CCorePlugin.getDefault().savePluginPreferences();
		}
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fOverlayStore.loadDefaults();
		initialize();
		super.performDefaults();
	}
}
