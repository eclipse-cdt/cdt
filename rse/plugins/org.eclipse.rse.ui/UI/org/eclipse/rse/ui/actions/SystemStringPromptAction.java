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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Shell;



/**
 * This is a default action for prompting for a string. 
 * It puts up a simple dialog with an entry field. You specify the button/menuitem
 *  strings, and the title and prompt on the resulting dialog.
 * You can also optionally specify an input validator that is called to verify the
 *  contents of the entry field.
 */
public class SystemStringPromptAction extends SystemBaseDialogAction
{

	private String          title, prompt;
	private ISystemValidator inputValidator;

	/**
	 * @param rb Resource bundle where to find following keys
	 * @param key Key used as prefix in resource bundle to get:
	 * <sl>
	 *   <li>button/menuitem label -> appends ".label" to key     
	 *   <li>button/menuitem tooltip -> appends ".tooltip" to key
	 *   <li>button/menuitem description on status line -> appends ".description" to key
	 *   <li>title of generic prompt dialog box displayed -> appends ".title"
	 *   <li>prompt text in generic prompt dialog box displayed -> appends ".prompt"
	 * </sl>
	 * @param parent Shell of parent window
	 * 
	 * @deprecated use fields from resource class directly now
	 */
	public SystemStringPromptAction(String label, String tooltip, String title, String prompt, Shell parent)
	{
	    super(label, tooltip, null, parent); // null => no image
	    this.title  = title;
	    this.prompt = prompt;
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_OPEN);  
	}

   
	/** 
	 * Set validator to use to check validity of input
	 * @param text validator     
	 */ 
	public void setValidator(ISystemValidator inputValidator)
	{
		this.inputValidator = inputValidator;
	}
	
	/**
	 * We use the default inherited actionPerformed method,
	 *  which calls this method to create our dialog.
	 */
	public Dialog createDialog(Shell parent) 
	{
		InputDialog dlg= new InputDialog(parent, title, prompt,null,inputValidator);		
		//dlg.getShell().setSize(300, 200); // ? right thing to do ?
		dlg.setBlockOnOpen(true); // modal
		return dlg;
	}	        

	/**
	 * We use the default inherited actionPerformed method,
	 *  which calls this method after the dialog is closed in
	 *  order to retrieve the dialog's data. This is then
	 *  placed in this object's value property and can be
	 *  retrieved publicly via getValue().
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		String value = ((InputDialog)dlg).getValue();
		if (value != null)
		  value = value.trim();
		return value;	    	
	}
}