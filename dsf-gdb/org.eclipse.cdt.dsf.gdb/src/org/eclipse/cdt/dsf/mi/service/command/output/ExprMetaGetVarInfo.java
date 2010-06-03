/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
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
import org.eclipse.cdt.dsf.gdb.GDBTypeParser.GDBType;

public class ExprMetaGetVarInfo implements ICommandResult {
    
	private final String expression;
	private final int numChild;
	private final String type;
	private final boolean editable;
	private final GDBType gdbType;

    public ExprMetaGetVarInfo(String e, int n, String t, boolean edit) {
        this (e, n, t, null, edit);
    }

	/**
     * @since 3.0
     */
	public ExprMetaGetVarInfo(String e, int n, String t, GDBType gt, boolean edit) {
    	expression = e;
    	numChild = n;
    	type = t;    	
    	editable = edit;
    	gdbType = gt;
    }
    
    public String getExpr() { return expression; }
    public int getNumChildren() { return numChild; }
    public String getType() { return type; }
    /**
     * @since 3.0
     */
    public GDBType getGDBType() { return gdbType; }
    public boolean getEditable() { return editable; }
	
	public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + //$NON-NLS-1$ 
				getExpr() + ", " + getNumChildren() + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
				getType() + ", " + getEditable() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
