/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.launch;

import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.swt.graphics.Image;

public class ArduinoDescriptorLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		return Activator.getDefault().getImage(Activator.IMG_ARDUINO);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchDescriptor)
			return ((ILaunchDescriptor) element).getName();
		return super.getText(element);
	}

}
