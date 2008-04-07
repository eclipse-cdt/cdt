/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [216252] MessageFormat.format -> NLS.bind
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.internal.importexport.RemoteImportExportResources;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;

/**
 * This class is used to query whether the user wants to overwrite a file, overwrite all files, not overwrite a file,
 * not overwrite any files, or cancel.
 */
public class RemoteFileOverwriteQuery implements IOverwriteQuery {
	/**
	 * This runnable shows the overwrite query dialog and stores the result.
	 */
	private class RemoteFileOverwriteQueryRunnable implements Runnable {
		private String pathString;
		private String queryResponse;

		/**
		 * Constructor.
		 * @param pathString the path.
		 */
		private RemoteFileOverwriteQueryRunnable(String pathString) {
			this.pathString = pathString;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			Path path = new Path(pathString);
			String messageString;
			//Break the message up if there is a file name and a directory
			//and there are at least 2 segments.
			if (path.getFileExtension() == null || path.segmentCount() < 2) {
				//TODO internal class used
				messageString = NLS.bind(RemoteImportExportResources.WizardDataTransfer_existsQuestion, pathString );
			} else {
				// TODO internal class used
				messageString = NLS.bind(RemoteImportExportResources.WizardDataTransfer_overwriteNameAndPathQuestion, path.lastSegment(),
						path.removeLastSegments(1).toOSString() );
			}
			Shell shell = SystemBasePlugin.getActiveWorkbenchShell();
			// TODO internal class used
			MessageDialog dialog = new MessageDialog(shell, RemoteImportExportResources.Question, null, messageString, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
			String[] response = new String[] { YES, ALL, NO, NO_ALL, CANCEL };
			// open the dialog
			dialog.open();
			if (dialog.getReturnCode() < 0) {
				queryResponse = IOverwriteQuery.CANCEL;
			} else {
				queryResponse = response[dialog.getReturnCode()];
			}
		}

		private String getQueryRresponse() {
			return queryResponse;
		}
	}

	/**
	 * Constructor.
	 */
	public RemoteFileOverwriteQuery() {
		super();
	}

	/**
	 * @see org.eclipse.ui.dialogs.IOverwriteQuery#queryOverwrite(java.lang.String)
	 */
	public String queryOverwrite(String pathString) {
		RemoteFileOverwriteQueryRunnable runnable = new RemoteFileOverwriteQueryRunnable(pathString);
		Display.getDefault().syncExec(runnable);
		return runnable.getQueryRresponse();
	}
}
