/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.parser.CodeReaderCache;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;

/**
 * @author dsteffle
 * @deprecated the one preference found on the page was moved to the 
 * indexer preference page.
 */
@Deprecated
public class CParserPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	protected OverlayPreferenceStore fOverlayStore;
	private Text bufferTextControl;
	
	public CParserPreferencePage(){
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		fOverlayStore  = createOverlayStore();
	}

	private OverlayPreferenceStore createOverlayStore() {
		ArrayList<OverlayKey> overlayKeys = new ArrayList<OverlayKey>();		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CodeReaderCache.CODE_READER_BUFFER));
	
        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
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
		
		Group bufferGroup= new Group(result, SWT.NONE);
		bufferGroup.setLayout(new GridLayout());
		bufferGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bufferGroup.setText(PreferencesMessages.CBufferPreferences_CodeReaderBuffer_CodeReaderBufferGroup); 
	
		bufferTextControl = (Text) addTextField( bufferGroup, PreferencesMessages.CBufferPreferences_CodeReaderBuffer_Size,6,0); 
		
		initialize(); 
		
		return result;
	
	}
	
	private void initialize(){
		bufferTextControl.setText(fOverlayStore.getString(CodeReaderCache.CODE_READER_BUFFER));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
	
	private Control addTextField(Composite composite, String label, int textLimit, int indentation) {

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
	@Override
	public boolean performOk() {
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		
		String bufferSize = bufferTextControl.getText();
		fOverlayStore.setValue(CodeReaderCache.CODE_READER_BUFFER, bufferSize);
		prefs.setValue(CodeReaderCache.CODE_READER_BUFFER, bufferSize);

		ICodeReaderCache cache = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES).getCodeReaderCache();
		if (cache instanceof CodeReaderCache) {
			try {
				// Check the string number
				int size = Integer.parseInt(bufferSize);
				if (size >= 0) {
					((CodeReaderCache)cache).setCacheSize(size);
				} else {
					((CodeReaderCache)cache).setCacheSize(CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
					prefs.setValue(CodeReaderCache.CODE_READER_BUFFER, CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
					fOverlayStore.setValue(CodeReaderCache.CODE_READER_BUFFER, CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
				}
			} catch (NumberFormatException ex){
				((CodeReaderCache)cache).setCacheSize(CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
				prefs.setValue(CodeReaderCache.CODE_READER_BUFFER, CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
				fOverlayStore.setValue(CodeReaderCache.CODE_READER_BUFFER, CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
			}	
		}
		
		fOverlayStore.propagate();
		CCorePlugin.getDefault().savePluginPreferences();
		
		return true;
	}
	
	/**
	 * @param store
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(CodeReaderCache.CODE_READER_BUFFER,CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB_STRING);
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		fOverlayStore.loadDefaults();
		initialize();
		super.performDefaults();
	}
}
