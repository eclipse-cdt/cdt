/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.importexport;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Overrides <code>ErrorDialog</code> to provide a dialog with
 * the image that corresponds to the <code>IStatus</code>.
 * 
 * This behavior should be implemented in the ErrorDialog itself,
 * see: 1GJU7TK: ITPUI:WINNT - DCR: ErrorDialog should not always show the error icon
 * The class can be removed when the above PR is fixed
 *
 * @see org.eclipse.core.runtime.IStatus
 */
public class RemoteImportExportProblemDialog extends ErrorDialog {
	private Image fImage;

	/**
	 * Creates a problem dialog.
	 *
	 * @param parent the shell under which to create this dialog
	 * @param title the title to use for this dialog,
	 *   or <code>null</code> to indicate that the default title should be used
	 * @param message the message to show in this dialog, 
	 *   or <code>null</code> to indicate that the error's message should be shown
	 *   as the primary message
	 * @param image the image to be used
	 * @param status the error to show to the user
	 * @param displayMask the mask to use to filter the displaying of child items,
	 *   as per <code>IStatus.matches</code>
	 * @see org.eclipse.core.runtime.IStatus#matches
	 */
	protected RemoteImportExportProblemDialog(Shell parent, String title, String message, Image image, IStatus status, int displayMask) {
		super(parent, title, message, status, displayMask);
		fImage = image;
	}

	/* 
	 * Overrides method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		if (fImage == null) {
			return composite;
		}
		// find the label that contains the image
		Control[] kids = composite.getChildren();
		int childCount = kids.length;
		Label label = null;
		int i = 0;
		while (i < childCount) {
			if (kids[i] instanceof Label) {
				label = (Label) kids[i];
				if (label.getImage() != null) {
					break;
				}
			}
			i++;
		}
		if (i < childCount && label != null) {
			label.setImage(fImage);
		}
		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Opens a warning dialog to display the given warning.  Use this method if the
	 * warning object being displayed does not contain child items, or if you
	 * wish to display all such items without filtering.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the title to use for this dialog,
	 *   or <code>null</code> to indicate that the default title should be used
	 * @param message the message to show in this dialog, 
	 *   or <code>null</code> to indicate that the error's message should be shown
	 *   as the primary message
	 * @param status the error to show to the user
	 * @return the code of the button that was pressed that resulted in this dialog
	 *     closing.  This will be <code>Dialog.OK</code> if the OK button was 
	 * 	   pressed, or <code>Dialog.CANCEL</code> if this dialog's close window 
	 *     decoration or the ESC key was used.
	 */
	public static int open(Shell parent, String title, String message, IStatus status) {
		return open(parent, title, message, status, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
	}

	/**
	 * Opens a dialog to display either an error or warning dialog.  Use this method if the
	 * status being displayed contains child items <it>and</it> you wish to
	 * specify a mask which will be used to filter the displaying of these
	 * children.  The error dialog will only be displayed if there is at
	 * least one child status matching the mask.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the title to use for this dialog,
	 *   or <code>null</code> to indicate that the default title should be used
	 * @param message the message to show in this dialog, 
	 *   or <code>null</code> to indicate that the error's message should be shown
	 *   as the primary message
	 * @param status the error to show to the user
	 * @param displayMask the mask to use to filter the displaying of child items,
	 *   as per <code>IStatus.matches</code>
	 * @return the code of the button that was pressed that resulted in this dialog
	 *     closing.  This will be <code>Dialog.OK</code> if the OK button was 
	 * 	   pressed, or <code>Dialog.CANCEL</code> if this dialog's close window 
	 *     decoration or the ESC key was used.
	 * @see org.eclipse.core.runtime.IStatus#matches
	 */
	public static int open(Shell parent, String title, String message, IStatus status, int displayMask) {
		Image image;
		Display display = parent.getDisplay();
		if (status == null || status.matches(IStatus.ERROR)) {
			image = display.getSystemImage(SWT.ICON_ERROR);
		} else if (status.matches(IStatus.WARNING)) {
			image = display.getSystemImage(SWT.ICON_WARNING);
		} else {
			image = display.getSystemImage(SWT.ICON_INFORMATION);
		}
		ErrorDialog dialog = new RemoteImportExportProblemDialog(parent, title, message, image, status, displayMask);
		return dialog.open();
	}
}
