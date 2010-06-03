/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson           - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;

public class ExprMetaCommand<V extends ICommandResult> implements ICommand<V> {

    private final IDMContext fCtx;
    
    public ExprMetaCommand(IDMContext ctx) {
    	fCtx = ctx;
    }
    
    /*
     * Takes the supplied command and coalesces it with this one.
     * The result is a new third command which represent the two
     * original command.
     */
    public ICommand<? extends ICommandResult> coalesceWith( ICommand<? extends ICommandResult> command ) {
        return null ;
    }  

    public IDMContext getContext(){
    	return fCtx;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
    	if (!(other.getClass().equals(getClass()))) return false;
    	
    	// Since other is the same class is this, we are sure it is of type DsfExprMetaCommand also
    	ExprMetaCommand<?> otherCmd = (ExprMetaCommand<?>)other;
        return fCtx == null ? otherCmd.fCtx == null : fCtx.equals(otherCmd.fCtx);	
    }
    
    @Override
    public int hashCode() {
    	return fCtx == null ? getClass().hashCode() : getClass().hashCode() ^ fCtx.hashCode();
    }
    
    @Override
    public String toString() {
    	IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(fCtx, IExpressionDMContext.class);
    	if (exprDmc != null) {
    		return getClass().getSimpleName() + "(\"" + //$NON-NLS-1$
    				exprDmc.getExpression() + "\")"; //$NON-NLS-1$
    	} else {
    		return getClass().getName() + (fCtx == null ? "null" : fCtx.toString()); //$NON-NLS-1$
    	}
    }
    
    public String getCommandControlFilter() {
        return null;
    }
}
