/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.newui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * A message line displaying a status.
 * See also org.eclipse.jface.dialogs.StatusDialog.MessageLine.
 */
public class StatusMessageLine {
	private CLabel fLabel;
	
	/**
	 * Constructor.
	 * 
	 * @param parent - parent element.
	 * @param style - the style of the control. Refer to {@link CLabel#CLabel(Composite, int)}.
	 * @param span - how many columns it should span.
	 */
	public StatusMessageLine(Composite parent, int style, int span) {
		fLabel = new CLabel(parent, style);
		if (span!=1) {
			GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
			gd.horizontalSpan = span;
			fLabel.setLayoutData(gd);
		}
	}

	/**
	 * Find an image associated with the status.
	 */
	private Image findImage(IStatus status) {
		if (status.isOK()) {
			return null;
		} else if (status.matches(IStatus.ERROR)) {
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
		} else if (status.matches(IStatus.WARNING)) {
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		} else if (status.matches(IStatus.INFO)) {
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
		}
		return null;
	}

	/**
	 * Assign {@link IStatus} object to the message line. The status should provide
	 * severity for the icon and message to display.
	 * 
	 * @param status - status object for the message line.
	 */
	public void setErrorStatus(IStatus status) {
		if (status != null && !status.isOK()) {
			String message = status.getMessage();
			if (message != null && message.length() > 0) {
				fLabel.setText(message);
				fLabel.setImage(findImage(status));
				fLabel.layout();
				return;
			}
		}
		fLabel.setText(""); //$NON-NLS-1$	
		fLabel.setImage(null);
	}
}