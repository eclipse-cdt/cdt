/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.tm.internal.terminal.view.ITerminalView;
import org.eclipse.tm.internal.terminal.view.ImageConsts;

public class TerminalActionPin extends TerminalAction
{
    public TerminalActionPin(ITerminalView target)
    {
        super(target,
              TerminalActionPin.class.getName(),IAction.AS_RADIO_BUTTON);

        setupAction(ActionMessages.PIN,
                    ActionMessages.PIN,
                    ImageConsts.IMAGE_CLCL_PIN,
                    ImageConsts.IMAGE_ELCL_PIN,
                    ImageConsts.IMAGE_DLCL_PIN,
                    true);
		setChecked(fTarget.isPinned());
   }
	public void run() {
		fTarget.setPinned(!fTarget.isPinned());
		setChecked(fTarget.isPinned());
	}
}
