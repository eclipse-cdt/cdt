/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - [225506] initial contribution
 * Martin Oberhuber (Wind River) - [225506] Adding Javadoc
 *********************************************************************************/
package org.eclipse.rse.files.ui.dialogs;

import org.eclipse.rse.internal.files.ui.dialogs.SaveAsDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Utility class with factory methods for creating some RSE Standard Dialogs,
 * for working with remote files.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since org.eclipse.rse.files.ui 3.0
 */
public class FileDialogFactory {

	/**
	 * Create an ISaveAsDialog instance with the given title, initialized for
	 * selecting a file to save to.
	 *
	 * @param shell parent shell for the dialog
	 * @param title title for the dialog
	 * @return New ISaveAsDialog instance
	 */
	public static ISaveAsDialog makeSaveAsDialog(Shell shell, String title)
	{
		return new SaveAsDialog(shell, title);
	}

	/**
	 * Create an ISaveAsDialog instance, initialized for selecting a folder to
	 * save to. The dialog title will be a standard title ("Browse for Folder").
	 *
	 * @param shell parent shell for the dialog
	 * @return new ISaveAsDialog instance
	 */
	public static ISaveAsDialog makeSaveAsDialog(Shell shell)
	{
		return new SaveAsDialog(shell);
	}

}
