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

package org.eclipse.rse.files.ui.resources;
import org.eclipse.rse.ui.messages.ISystemMessageLine;

/**
 * Form class for selecting a file based on the SaveAS 
 */
public class FileSelectionForm extends SaveAsForm 
{
	public static final String Copyright =
		"(C) Copyright IBM Corp. 2003  All Rights Reserved.";

	/**
	 * Constructor for FileSelectionForm
	 */
	public FileSelectionForm(
		ISystemMessageLine msgLine,
		Object caller,
		boolean fileMode)
	{
		super(msgLine, caller, fileMode);
	}

	/**
	 * Completes processing of the dialog.
	 * Intercept of parent method.
	 * 
	 * @return true if no errors
	 */
	public boolean verify()
	{

		return true;
	}

	public boolean isPageComplete()
	{
		//String errMsg = validator.isValid(fileName);

		//if (errMsg != null)
		//{
		//	setErrorMessage(errMsg);
		//	return false;			
		//}
		//else
		//{
		//	clearErrorMessage();
		//}

		return fileNameText != null
			&& fileNameText.getText().length() > 0;
			//&& super.isPageComplete();
	}
	public String getFileName()
	{
		return fileName;
	}

}