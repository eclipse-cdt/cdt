/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial implementation
  *******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.tm.internal.terminal.view.ITerminalView;
import org.eclipse.tm.internal.terminal.view.ImageConsts;

public class TerminalActionToggleCommandInputField extends TerminalAction
{
    public TerminalActionToggleCommandInputField(ITerminalView target)
    {
        super(target,
              TerminalActionToggleCommandInputField.class.getName(),IAction.AS_RADIO_BUTTON);

        setupAction(ActionMessages.TOGGLE_COMMAND_INPUT_FIELD,
                    ActionMessages.TOGGLE_COMMAND_INPUT_FIELD,
                    ImageConsts.IMAGE_CLCL_COMMAND_INPUT_FIELD,
                    ImageConsts.IMAGE_ELCL_COMMAND_INPUT_FIELD,
                    ImageConsts.IMAGE_DLCL_COMMAND_INPUT_FIELD,
                    true);
        setChecked(fTarget.hasCommandInputField());
    }
	public void run() {
		fTarget.setCommandInputField(!fTarget.hasCommandInputField());
		setChecked(fTarget.hasCommandInputField());
	}
}
