package org.eclipse.cdt.managedbuilder.ui.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIImages;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class ConfigurationLabelProvider	extends LabelProvider implements ITableLabelProvider {
	private final Image IMG_CFG =
		ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CONFIG);

	// 
	public String getColumnText(Object obj, int index) {
		if (obj instanceof IConfiguration) {
			return ((IConfiguration) obj).getName();
		}
		return new String();
	}

	public Image getColumnImage(Object obj, int index) {
		return IMG_CFG;
	}
}

