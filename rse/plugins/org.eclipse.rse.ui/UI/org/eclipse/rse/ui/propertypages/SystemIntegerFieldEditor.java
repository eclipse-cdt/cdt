/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.propertypages;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.widgets.Composite;

/**
 * A version of an IntegerFieldEditor which allows setting tooltip and help
 * If value is outside of valid range, message will tell user what the valid range is
 */
public class SystemIntegerFieldEditor extends IntegerFieldEditor {

   public static final String Copyright = "(C) Copyright IBM Corp. 2003  All Rights Reserved.";


	/**
	 * the message for an empty field
	 */
   private static final String RANGE_MESSAGE = "RSEG0402";
	
	protected Composite composite_parent = null;
	
	/**
	 * @see java.lang.Object#Object()
	 */
	public SystemIntegerFieldEditor() {
		super();
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#FieldEditor(String, String, Composite)
	 */
	public SystemIntegerFieldEditor(
		String name,
		String labelText,
		Composite parent) {
		super(name, labelText, parent);
		composite_parent = parent;
	}

	/**
	 * @see org.eclipse.jface.preference.IntegerFieldEditor#setValidRange(int, int)
	 */
	public void setValidRange(int min,int max) {
		super.setValidRange(min, max);
		SystemMessage msg = RSEUIPlugin.getPluginMessage(RANGE_MESSAGE);
		setErrorMessage(msg.makeSubstitution(getLabelControl().getText(), new Integer(min), new Integer(max)).getLevelOneText());
	}
	
	/**
	 * Method setHelp. 
	 * Sets the info pop help for this field editor
	 * @param contextId the context ID for the help
	 */
	public void setHelp(String contextId) {
		SystemWidgetHelpers.setHelp(getTextControl(), contextId);
	}

	/**
	 * Method setToolTipText.
	 * The tooltip text
	 * @param tip the tip to set for the entry field
	 */
	public void setToolTipText(String tip) {
		getTextControl().setToolTipText(tip);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean, org.eclipse.swt.widgets.Composite)
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled, composite_parent);
	}

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	public void refreshValidState() {
		super.refreshValidState();
	}
}
