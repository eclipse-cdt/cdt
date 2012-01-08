/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Elazar Leibovich (IDF) - Code folding of compound statements (bug 174597)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.folding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.folding.ICFoldingPreferenceBlock;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore;
import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;

/**
 */
public class DefaultCFoldingPreferenceBlock implements ICFoldingPreferenceBlock {

	private IPreferenceStore fStore;
	protected OverlayPreferenceStore fOverlayStore;
	private OverlayKey[] fKeys;
	protected Map<Button, String> fCheckBoxes= new HashMap<Button, String>();

	private SelectionListener fCheckBoxListener= new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			String key= fCheckBoxes.get(button);
			fOverlayStore.setValue(key, button.getSelection());
			updateEnablement(key);
		}
	};
	private Button fInactiveCodeFoldingCheckBox;
	

	public DefaultCFoldingPreferenceBlock() {
		fStore= CUIPlugin.getDefault().getPreferenceStore();
		fKeys= createKeys();
		fOverlayStore= new OverlayPreferenceStore(fStore, fKeys);
	}
	
	private OverlayKey[] createKeys() {
		ArrayList<OverlayKey> overlayKeys= new ArrayList<OverlayKey>();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_MACROS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_FUNCTIONS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_METHODS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_STRUCTURES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_COMMENTS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_HEADERS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_STATEMENTS));
		
		return overlayKeys.toArray(new OverlayKey[overlayKeys.size()]);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.ICFoldingPreferences#createControl(org.eclipse.swt.widgets.Group)
	 */
	@Override
	public Control createControl(Composite composite) {
		fOverlayStore.load();
		fOverlayStore.start();
		
		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout(1, true);
		layout.verticalSpacing= 3;
		layout.marginWidth= 0;
		inner.setLayout(layout);

		addCheckBox(inner, FoldingMessages.DefaultCFoldingPreferenceBlock_preprocessor_enabled, PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, 1);
		addCheckBox(inner, FoldingMessages.DefaultCFoldingPreferenceBlock_statements_enabled, PreferenceConstants.EDITOR_FOLDING_STATEMENTS, 1);
		ControlFactory.createEmptySpace(inner);

		Composite group= ControlFactory.createGroup(inner, FoldingMessages.DefaultCFoldingPreferenceBlock_title, 1);

		addCheckBox(group, FoldingMessages.DefaultCFoldingPreferenceBlock_macros, PreferenceConstants.EDITOR_FOLDING_MACROS, 0); 
		addCheckBox(group, FoldingMessages.DefaultCFoldingPreferenceBlock_functions, PreferenceConstants.EDITOR_FOLDING_FUNCTIONS, 0); 
		addCheckBox(group, FoldingMessages.DefaultCFoldingPreferenceBlock_methods, PreferenceConstants.EDITOR_FOLDING_METHODS, 0); 
		addCheckBox(group, FoldingMessages.DefaultCFoldingPreferenceBlock_structures, PreferenceConstants.EDITOR_FOLDING_STRUCTURES, 0); 
		addCheckBox(group, FoldingMessages.DefaultCFoldingPreferenceBlock_comments, PreferenceConstants.EDITOR_FOLDING_COMMENTS, 0); 
		addCheckBox(group, FoldingMessages.DefaultCFoldingPreferenceBlock_headers, PreferenceConstants.EDITOR_FOLDING_HEADERS, 0);
		fInactiveCodeFoldingCheckBox= addCheckBox(group, FoldingMessages.DefaultCFoldingPreferenceBlock_inactive_code, PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE, 0); 
		
		return inner;
	}
	
	private Button addCheckBox(Composite parent, String label, String key, int indentation) {		
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 1;
		gd.grabExcessVerticalSpace= false;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);
		
		fCheckBoxes.put(checkBox, key);
		
		return checkBox;
	}
	
	private void initializeFields() {
		Iterator<Button> it= fCheckBoxes.keySet().iterator();
		while (it.hasNext()) {
			Button b= it.next();
			String key= fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
			updateEnablement(key);
		}
	}

	protected void updateEnablement(String key) {
		if (PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED.equals(key)) {
			fInactiveCodeFoldingCheckBox.setEnabled(fOverlayStore.getBoolean(key));
		}
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.AbstractCFoldingPreferences#performOk()
	 */
	@Override
	public void performOk() {
		fOverlayStore.propagate();
	}
	
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.AbstractCFoldingPreferences#initialize()
	 */
	@Override
	public void initialize() {
		initializeFields();
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.AbstractCFoldingPreferences#performDefaults()
	 */
	@Override
	public void performDefaults() {
		fOverlayStore.loadDefaults();
		initializeFields();
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.AbstractCFoldingPreferences#dispose()
	 */
	@Override
	public void dispose() {
		fOverlayStore.stop();
	}

}
