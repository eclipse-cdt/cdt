package org.eclipse.rse.useractions.ui.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for compile commands in the work with compile commands
 *  dialog.
 */
public class SystemCompileCommandLabelProvider extends LabelProvider {
	/**
	 * Constructor
	 */
	public SystemCompileCommandLabelProvider() {
		super();
	}

	/**
	 * Override of parent to return the visual label for the given compile command
	 */
	public String getText(Object element) {
		if (element instanceof SystemCompileCommand)
			return ((SystemCompileCommand) element).getLabel();
		else if (element != null)
			return element.toString();
		else
			return null;
	}

	/**
	 * Override of parent so we can supply an image, if we desire.
	 */
	public Image getImage(Object element) {
		if (element instanceof SystemCompileCommand) return RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_COMPILE_ID);
		return null;
	}
}
