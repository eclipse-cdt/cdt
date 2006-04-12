/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class NewBaseClassSelectionDialog extends TypeSelectionDialog {
    
    private static final String DIALOG_SETTINGS = NewBaseClassSelectionDialog.class.getName();
    private static final int[] VISIBLE_TYPES = { ICElement.C_CLASS, ICElement.C_STRUCT };
    private static final int ADD_ID = IDialogConstants.CLIENT_ID + 1;
	private List fTypeList;
	private List fTypeListeners;
    
	public interface ITypeSelectionListener {
	}
	
    public NewBaseClassSelectionDialog(Shell parent) {
        super(parent);
        setTitle(NewClassWizardMessages.getString("NewBaseClassSelectionDialog.title")); //$NON-NLS-1$
        setMessage(NewClassWizardMessages.getString("NewBaseClassSelectionDialog.message")); //$NON-NLS-1$
        setDialogSettings(DIALOG_SETTINGS);
        setVisibleTypes(VISIBLE_TYPES);
        setFilter("*", true); //$NON-NLS-1$
		setStatusLineAboveButtons(true);
		fTypeList = new ArrayList();
		fTypeListeners = new ArrayList();
    }
    
    public void addListener(ITypeSelectionListener listener) {
        if (!fTypeListeners.contains(listener))
            fTypeListeners.add(listener);
    }
    
    public void removeListener(ITypeSelectionListener listener) {
        fTypeListeners.remove(listener);
    }
    
    /*
	 * @see Dialog#createButtonsForButtonBar
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ADD_ID, NewClassWizardMessages.getString("NewBaseClassSelectionDialog.addButton.label"), true); //$NON-NLS-1$
		super.createButtonsForButtonBar(parent);
	}
	
	/*
	 * @see Dialog#buttonPressed
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == ADD_ID){
			addType(getLowerSelectedElement());
		}
		super.buttonPressed(buttonId);	
	}
	
	/*
	 * @see Dialog#okPressed
	 */
	protected void okPressed() {
	    addType(getLowerSelectedElement());
		super.okPressed();
	}
	
	private void addType(Object elem) {
	}
    
    /**
     * Checks if the base classes need to be verified (ie they must exist in the project)
     * 
     * @return <code>true</code> if the base classes should be verified
     */
    public boolean verifyBaseClasses() {
        return NewClassWizardPrefs.verifyBaseClasses();
    }
	
	/*
	 * @see AbstractElementListSelectionDialog#handleDefaultSelected()
	 */
	protected void handleDefaultSelected() {
		if (validateCurrentSelection())
			buttonPressed(ADD_ID);
	}
}
