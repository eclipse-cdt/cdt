/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui.dialogfields;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Dialog field containing a label and a combo control.
 */
public class ComboDialogField extends DialogField {
		
	private String fText;
	private int fSelectionIndex;
	private String[] fItems;
	private Combo fComboControl;
	private ModifyListener fModifyListener;
	private int fFlags;
	
	public ComboDialogField(int flags) {
		super();
		fText= ""; //$NON-NLS-1$
		fItems= new String[0];
		fFlags= flags;
		fSelectionIndex= -1;
	}
			
	// ------- layout helpers
		
	/*
	 * @see DialogField#doFillIntoGrid
	 */
	@Override
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);
		
		Label label= getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Combo combo= getComboControl(parent);
		combo.setLayoutData(gridDataForCombo(nColumns - 1));
		
		return new Control[] { label, combo };
	} 

	/*
	 * @see DialogField#getNumberOfControls
	 */
	@Override
	public int getNumberOfControls() {
		return 2;	
	}
	
	protected static GridData gridDataForCombo(int span) {
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= span;
		return gd;
	}	
	
	// ------- focus methods
	
	/*
	 * @see DialogField#setFocus
	 */
	@Override
	public boolean setFocus() {
		if (isOkToUse(fComboControl)) {
			fComboControl.setFocus();
		}
		return true;
	}
		
	// ------- ui creation			

	/**
	 * Creates or returns the created combo control.
	 * @param parent The parent composite or <code>null</code> when the widget has
	 * already been created.
	 */		
	public Combo getComboControl(Composite parent) {
		if (fComboControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener= new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};
			SelectionListener selectionListener= new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doSelectionChanged(e);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {	
				}
			};
			
			fComboControl= new Combo(parent, fFlags);
			// moved up due to 1GEUNW2
			fComboControl.setItems(fItems);
			if (fSelectionIndex != -1) {
				fComboControl.select(fSelectionIndex);
			} else {
				fComboControl.setText(fText);
			}
			fComboControl.setFont(parent.getFont());
			fComboControl.addModifyListener(fModifyListener);
			fComboControl.addSelectionListener(selectionListener);
			fComboControl.setEnabled(isEnabled());
		}
		return fComboControl;
	}	
	
	protected void doModifyText(ModifyEvent e) {
		if (isOkToUse(fComboControl)) {
			fText= fComboControl.getText();
			fSelectionIndex= fComboControl.getSelectionIndex();
		}
		dialogFieldChanged();
	}
	
	protected void doSelectionChanged(SelectionEvent e) {
		if (isOkToUse(fComboControl)) {
			fItems= fComboControl.getItems();
			fText= fComboControl.getText();
			fSelectionIndex= fComboControl.getSelectionIndex();
		}
		dialogFieldChanged();	
	}
	
	// ------ enable / disable management
	
	/*
	 * @see DialogField#updateEnableState
	 */		
	@Override
	protected void updateEnableState() {
		super.updateEnableState();		
		if (isOkToUse(fComboControl)) {
			fComboControl.setEnabled(isEnabled());
		}	
	}		
		
	// ------ text access 
	
	/**
	 * Gets the combo items.
	 */	
	public String[] getItems() {
		return fItems;
	}
	
	/**
	 * Sets the combo items. Triggers a dialog-changed event.
	 */
	public void setItems(String[] items) {
		fItems= items;
		if (isOkToUse(fComboControl)) {
			fComboControl.setItems(items);
		}
		dialogFieldChanged();
	}
	
	/**
	 * Gets the text.
	 */	
	public String getText() {
		return fText;
	}
	
	/**
	 * Sets the text. Triggers a dialog-changed event.
	 */
	public void setText(String text) {
		fText= text;
		if (isOkToUse(fComboControl)) {
			fComboControl.setText(text);
		} else {
			dialogFieldChanged();
		}	
	}

	/**
	 * Selects an item.
	 */	
	public void selectItem(int index) {
		if (isOkToUse(fComboControl)) {
			fComboControl.select(index);
			fSelectionIndex= index;
		} else {
			if (index >= 0 && index < fItems.length) {
				fText= fItems[index];
				fSelectionIndex= index;	
			}
		}
		dialogFieldChanged();
	}
	
	public int getSelectionIndex() {
		return fSelectionIndex;
	}
	

	/**
	 * Sets the text without triggering a dialog-changed event.
	 */
	public void setTextWithoutUpdate(String text) {
		fText= text;
		if (isOkToUse(fComboControl)) {
			fComboControl.removeModifyListener(fModifyListener);
			fComboControl.setText(text);
			fComboControl.addModifyListener(fModifyListener);
		}
	}
	
	public void dispose() {
		if (fComboControl != null) fComboControl.dispose();
		if (fItems != null) fItems = null;
		if (fModifyListener != null) fModifyListener = null;
		super.dispose();
	}
	
}