/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.texteditor.IUpdate;

public abstract class AbstractDisassemblyAction extends Action implements IUpdate, IPropertyListener {

	protected IDisassemblyPart fDisassemblyPart;

	AbstractDisassemblyAction() {
	}

	/**
	 * Create a disassembly action.
	 * 
	 * @param disassemblyPart
	 */
	public AbstractDisassemblyAction(IDisassemblyPart disassemblyPart) {
		Assert.isLegal(disassemblyPart != null);
		fDisassemblyPart= disassemblyPart;
		fDisassemblyPart.addPropertyListener(this);
	}

	/**
	 * @return the disassembly part
	 */
	public final IDisassemblyPart getDisassemblyPart() {
		return fDisassemblyPart;
	}
	
	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public abstract void run();

	public void update() {
		boolean enabled= fDisassemblyPart == null || fDisassemblyPart.isConnected();
		setEnabled(enabled);
	}
	
	/*
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
		if (source == fDisassemblyPart && (propId & 0x500) != 0) {
			update();
		}
	}
}
