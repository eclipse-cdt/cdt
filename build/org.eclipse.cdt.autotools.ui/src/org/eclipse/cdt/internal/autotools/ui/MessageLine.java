/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;


/**
 * A message line. It distinguishes between "normal" messages and errors. 
 * Setting an error message hides a currently displayed message until 
 * <code>clearErrorMessage</code> is called.
 */
public class MessageLine {

	private String fMessage;

	private Color fNormalMsgAreaBackground;

	private boolean hasErrorMessage;
	
	private CLabel clabel;

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
		clabel = new CLabel(parent, style);
		fNormalMsgAreaBackground= clabel.getBackground();
	}

	
	/**
     * Display the given error message. A currently displayed message
     * is saved and will be redisplayed when the error message is cleared.
     */
	public void setErrorMessage(String message) {
		if (message != null && message.length() > 0) {
			hasErrorMessage = true;
			clabel.setText(message);
			clabel.setImage(MakeUIImages.getImage(MakeUIImages.IMG_OBJS_ERROR));
			clabel.setBackground(JFaceColors.getErrorBackground(clabel.getDisplay()));
			return;
		}
		hasErrorMessage = false;
		clabel.setText(fMessage);	
		clabel.setImage(null);
		clabel.setBackground(fNormalMsgAreaBackground);	
	}

	public void setMessage(String message) {
		fMessage = message;
		clabel.setText(message);
	}
	
	public boolean hasErrorMessage() {
		return hasErrorMessage;
	}
}
