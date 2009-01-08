/*******************************************************************************
 * Copyright (c) 2007 Ericsson and others.
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

public class ExprMetaGetVarInfo implements ICommandResult {
    
	private final String expression;
	private final int numChild;
	private final String type;
	private final boolean editable;
	
	public ExprMetaGetVarInfo(String e, int n, String t, boolean edit) {
    	expression = e;
    	numChild = n;
    	type = t;    	
    	editable = edit;
    }
    
    public String getExpr() { return expression; }
    public int getNumChildren() { return numChild; }
    public String getType() { return type; }
    public boolean getEditable() { return editable; }
	
	public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
		return null;
	}
}
