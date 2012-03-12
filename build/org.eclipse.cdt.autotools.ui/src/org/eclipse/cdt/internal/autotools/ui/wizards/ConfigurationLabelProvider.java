/*******************************************************************************
 * Copyright (c) 2002, 2005, 2007 Rational Software Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Red Hat Inc. - Copy from CDT 3.1.2 to here
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;


import org.eclipse.cdt.internal.autotools.ui.AutotoolsUIPluginImages;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


public class ConfigurationLabelProvider	extends LabelProvider implements ITableLabelProvider {
	private final Image IMG_CFG =
		AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_BUILD_CONFIG);

	// 
	public String getColumnText(Object obj, int index) {
		if (obj instanceof IConfiguration) {
			IConfiguration tmpConfig = (IConfiguration) obj;
			
			if( (tmpConfig.getDescription() == null)|| (tmpConfig.getDescription().equals("")) )	//$NON-NLS-1$
				return ((IConfiguration) obj).getName();
			else
				return ( tmpConfig.getName() + " ( " + tmpConfig.getDescription() + " )");	//$NON-NLS-1$	//$NON-NLS-2$
		}
		return "";
	}

	public Image getColumnImage(Object obj, int index) {
		return IMG_CFG;
	}
}

