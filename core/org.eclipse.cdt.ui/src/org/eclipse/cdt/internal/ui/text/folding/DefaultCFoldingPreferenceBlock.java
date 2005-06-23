/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.folding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore;
import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.folding.ICFoldingPreferenceBlock;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 */
public class DefaultCFoldingPreferenceBlock implements ICFoldingPreferenceBlock {

	private IPreferenceStore fStore;
	protected OverlayPreferenceStore fOverlayStore;
	private OverlayKey[] fKeys;
	protected Map fCheckBoxes= new HashMap();

	private SelectionListener fCheckBoxListener= new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
		}
	};
	

	public DefaultCFoldingPreferenceBlock() {
		fStore= CUIPlugin.getDefault().getPreferenceStore();
		fKeys= createKeys();
		fOverlayStore= new OverlayPreferenceStore(fStore, fKeys);
	}
	
	private OverlayKey[] createKeys() {
		ArrayList overlayKeys= new ArrayList();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_MACROS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_FUNCTIONS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_METHODS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_FOLDING_STRUCTURES));
		
		return (OverlayKey[]) overlayKeys.toArray(new OverlayKey[overlayKeys.size()]);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.folding.ICFoldingPreferences#createControl(org.eclipse.swt.widgets.Group)
	 */
	public Control createControl(Composite composite) {
		fOverlayStore.load();
		fOverlayStore.start();
		
		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout(1, true);
		layout.verticalSpacing= 3;
		layout.marginWidth= 0;
		inner.setLayout(layout);
		
		Label label= new Label(inner, SWT.LEFT);
		label.setText(FoldingMessages.getString("DefaultCFoldingPreferenceBlock.title")); //$NON-NLS-1$
		
		addCheckBox(inner, FoldingMessages.getString("DefaultCFoldingPreferenceBlock.macros"), PreferenceConstants.EDITOR_FOLDING_MACROS, 0); //$NON-NLS-1$
		addCheckBox(inner, FoldingMessages.getString("DefaultCFoldingPreferenceBlock.functions"), PreferenceConstants.EDITOR_FOLDING_FUNCTIONS, 0); //$NON-NLS-1$
		addCheckBox(inner, FoldingMessages.getString("DefaultCFoldingPreferenceBlock.methods"), PreferenceConstants.EDITOR_FOLDING_METHODS, 0); //$NON-NLS-1$
		addCheckBox(inner, FoldingMessages.getString("DefaultCFoldingPreferenceBlock.structures"), PreferenceConstants.EDITOR_FOLDING_STRUCTURES, 0); //$NON-NLS-1$
		
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
		Iterator it= fCheckBoxes.keySet().iterator();
		while (it.hasNext()) {
			Button b= (Button) it.next();
			String key= (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.AbstractCFoldingPreferences#performOk()
	 */
	public void performOk() {
		fOverlayStore.propagate();
	}
	
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.AbstractCFoldingPreferences#initialize()
	 */
	public void initialize() {
		initializeFields();
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.AbstractCFoldingPreferences#performDefaults()
	 */
	public void performDefaults() {
		fOverlayStore.loadDefaults();
		initializeFields();
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.AbstractCFoldingPreferences#dispose()
	 */
	public void dispose() {
		fOverlayStore.stop();
	}

}
