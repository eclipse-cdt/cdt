/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Xuan Chen (IBM) - [225506] initial contribution 
 *********************************************************************************/
package org.eclipse.rse.files.ui.dialogs;

import org.eclipse.rse.internal.files.ui.dialogs.SaveAsDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author xuanchen
 *
 */
public class SaveAsDialogFactory {
	
	public static ISaveAsDialog getSaveAsDialog(Shell shell, String title)
	{
		return new SaveAsDialog(shell, title);
	}
	
	public static ISaveAsDialog getSaveAsDialog(Shell shell)
	{
		return new SaveAsDialog(shell);
	}	

}
