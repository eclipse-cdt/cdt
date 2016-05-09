/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.swt.widgets.Composite;

public class EditButton extends CButton {

	public EditButton(Composite parent, int style) {
		super(parent, style);
		setHotImage(Activator.getDefault().getImageRegistry().get(Activator.IMG_CONFIG_CONFIG));
		setColdImage(Activator.getDefault().getImageRegistry().get(Activator.IMG_EDIT_COLD));
		setToolTipText(Messages.EditButton_0);
	}

}
