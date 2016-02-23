/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.launch;

import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.swt.graphics.Image;

public class QtLaunchDescriptorLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchDescriptor) {
			return ((ILaunchDescriptor) element).getName();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		return Activator.getImage(Activator.IMG_QT_16);
	}

}
