/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A message line. It distinguishs between "normal" messages and errors.
 * Setting an error message hides a currently displayed message until
 * <code>clearErrorMessage</code> is called.
 */
public class MessageLine extends CLabel {

	private String fMessage;

	private Color fNormalMsgAreaBackground;

	private boolean hasErrorMessage;

	/**
	 * Creates a new message line as a child of the given parent.
	 */
	public MessageLine(Composite parent) {
		this(parent, SWT.LEFT);
	}

	/**
	 * Creates a new message line as a child of the parent and with the given SWT stylebits.
	 */
	public MessageLine(Composite parent, int style) {
		super(parent, style);
		fNormalMsgAreaBackground = getBackground();
	}

	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message) {
		if (message != null && message.length() > 0) {
			hasErrorMessage = true;
			setText(message);
			setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
			setBackground(JFaceColors.getErrorBackground(getDisplay()));
			return;
		}
		hasErrorMessage = false;
		setText(fMessage);
		setImage(null);
		setBackground(fNormalMsgAreaBackground);
	}

	public void setMessage(String message) {
		fMessage = message;
		setText(message);
	}

	public boolean hasErrorMessage() {
		return hasErrorMessage;
	}
}
