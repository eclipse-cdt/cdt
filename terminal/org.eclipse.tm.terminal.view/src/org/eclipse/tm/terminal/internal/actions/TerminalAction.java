/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/


package org.eclipse.tm.terminal.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.tm.terminal.internal.view.ITerminalView;
import org.eclipse.tm.terminal.internal.view.TerminalViewPlugin;

abstract public class TerminalAction extends Action {
    protected final ITerminalView fTarget;
    public TerminalAction(ITerminalView target, String strId) {
		super(""); //$NON-NLS-1$

		fTarget = target;

		setId(strId);
	}
	abstract public void run();
	protected void setupAction(String strText, String strToolTip,
			String strImage, String strEnabledImage, String strDisabledImage,
			boolean bEnabled) {
		TerminalViewPlugin plugin;
		ImageRegistry imageRegistry;

		plugin = TerminalViewPlugin.getDefault();
		imageRegistry = plugin.getImageRegistry();
		setupAction(strText, strToolTip, strImage, strEnabledImage,
				strDisabledImage, bEnabled, imageRegistry);
	}
	protected void setupAction(String strText, String strToolTip,
			String strImage, String strEnabledImage, String strDisabledImage,
			boolean bEnabled, ImageRegistry imageRegistry) {
		ImageDescriptor imageDescriptor;

		setText(strText);
		setToolTipText(strToolTip);
		setEnabled(bEnabled);

		imageDescriptor = imageRegistry.getDescriptor(strEnabledImage);
		if (imageDescriptor != null) {
			setImageDescriptor(imageDescriptor);
		}

		imageDescriptor = imageRegistry.getDescriptor(strDisabledImage);
		if (imageDescriptor != null) {
			setDisabledImageDescriptor(imageDescriptor);
		}

		imageDescriptor = imageRegistry.getDescriptor(strImage);
		if (imageDescriptor != null) {
			setHoverImageDescriptor(imageDescriptor);
		}
	}
}
