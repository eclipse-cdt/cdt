/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CFileTypeDialog extends Dialog {
	
	public CFileTypeDialog(Shell parentShell) {
		super(parentShell);
	}

	private Text		fTextPattern;
	private Combo		fComboType;

	private String		fPattern;
	private IContentType fType;
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(PreferencesMessages.getString("CFileTypeDialog.title")); //$NON-NLS-1$
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		getOkayButton().setEnabled(getPatternFromControl().length() > 0);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 2;

		// Pattern row
		
		Label pattern = new Label(composite, SWT.NONE);
				
		pattern.setText(PreferencesMessages.getString("CFileTypeDialog.patternLabel")); //$NON-NLS-1$

		fTextPattern = new Text(composite, SWT.BORDER | SWT.SINGLE);
		
		fTextPattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if (null != fPattern) {
			fTextPattern.setText(fPattern);
		}

		fTextPattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getOkayButton().setEnabled(getPatternFromControl().length() > 0);
			}
		});
		
		// Type row
		
		Label type = new Label(composite, SWT.NONE);
		
		type.setText(PreferencesMessages.getString("CFileTypeDialog.typeLabel")); //$NON-NLS-1$
		
		fComboType = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SINGLE);

		fComboType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		populateTypesCombo();
		
		return composite;
	}

	public void setPattern(String pattern) {
		fPattern = pattern;
	}

	public String getPattern() {
		return fPattern;
	}
	
	public IContentType getContentType() {
		return fType;
	}

	private void populateTypesCombo() {
		IContentTypeManager manager = Platform.getContentTypeManager();
		String[]	ids = CoreModel.getRegistedContentTypeIds();
		ArrayList list = new ArrayList(ids.length);
		for (int i = 0; i < ids.length; i++) {
			IContentType ctype = manager.getContentType(ids[i]);
			if (ctype != null) {
				list.add(ctype);
			}
		}

		IContentType[] ctypes = new IContentType[list.size()];
		list.toArray(ctypes);
		int	index = -1;

		for (int i = 0; i < ctypes.length; i++) {
			fComboType.add(ctypes[i].getName());
		}
		
		fComboType.setData(ctypes);

		if (null != fType) {
			index = fComboType.indexOf(fType.getName());
		}

		fComboType.select((index < 0) ? 0 : index);
	}

	Button getOkayButton() {
		return getButton(IDialogConstants.OK_ID);
	}
	
	String getPatternFromControl() {
		return fTextPattern.getText().trim();
	}
	
	private IContentType getTypeFromControl() {
		IContentType type	= null;
		int		index	= fComboType.getSelectionIndex();
		
		if (-1 != index) {
			String			name	= fComboType.getItem(index);
			IContentType[]	types	= (IContentType[]) fComboType.getData();
			for (int i = 0; i < types.length; i++) {
				if (name.equals(types[i].getName())) {
					type = types[i];
				}
			}
		}
		return type;
	}
	
	protected void okPressed() {
		fPattern 	=	getPatternFromControl();
		fType		=	getTypeFromControl();
		
		super.okPressed();
	}

	
}
