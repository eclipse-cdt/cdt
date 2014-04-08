/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.disassembly;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AbstractDisassemblyRulerActionDelegate;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

/**
 * Ruler action delegate for the "Add Dynamic Printf..." action.
 */
public class AddDynamicPrintfRulerActionDelegate extends AbstractDisassemblyRulerActionDelegate {

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AbstractDisassemblyRulerActionDelegate#createAction(org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart, org.eclipse.jface.text.source.IVerticalRulerInfo)
	 */
	@Override
	protected IAction createAction(IDisassemblyPart disassemblyPart, IVerticalRulerInfo rulerInfo) {
		return new AddDynamicPrintfRulerAction(disassemblyPart, rulerInfo);
	}
}
