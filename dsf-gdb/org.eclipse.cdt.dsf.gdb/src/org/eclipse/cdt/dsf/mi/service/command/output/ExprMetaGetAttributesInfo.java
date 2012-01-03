/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson           - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;

public class ExprMetaGetAttributesInfo implements ICommandResult {
    
	private final boolean editable;

    public ExprMetaGetAttributesInfo(boolean e) {
    	editable = e;
    }
    
    public boolean getEditable() { return editable; }
	
	@Override
	public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + getEditable() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}