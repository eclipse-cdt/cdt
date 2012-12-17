/*******************************************************************************
 * Copyright (c) 2007, 2012 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.widgets.Composite;

/**
 *@since 7.3
 */
public class ReverseDebugActionPage extends PlatformObject implements IBreakpointActionPage{
	
	private ReverseDebugActionComposite reverseDebugActionComposite;
	private ReverseDebugAction reverseDebugAction;

	@Override
	public void actionDialogCanceled() {
	}

	@Override
	public void actionDialogOK() {
		reverseDebugAction.setOperation(reverseDebugActionComposite.getOperation());
	}

	@Override
	public Composite createComposite(IBreakpointAction action,
			Composite composite, int style) {
		reverseDebugAction = (ReverseDebugAction) action;
		reverseDebugActionComposite = new ReverseDebugActionComposite(composite, style, this);
		return reverseDebugActionComposite;
	}

}
