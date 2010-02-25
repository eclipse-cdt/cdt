/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Set whether to show error in editor when moving to next/prev error in Build Console
 */
public class ShowErrorAction extends Action {

	public ShowErrorAction() {
		super(ConsoleMessages.ShowErrorAction_Tooltip); 
		setChecked(true);
		setToolTipText(ConsoleMessages.ShowErrorAction_Tooltip); 
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();		
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED_DISABLED));
	}

}
