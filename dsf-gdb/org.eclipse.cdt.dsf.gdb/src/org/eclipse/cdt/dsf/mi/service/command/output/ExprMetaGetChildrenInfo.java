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
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.ExpressionInfo;

public class ExprMetaGetChildrenInfo implements ICommandResult {
    
	private final ExpressionInfo[] childrenExpressions;

    public ExprMetaGetChildrenInfo(ExpressionInfo[] c) {
    	childrenExpressions = c;
    }
    
    public ExpressionInfo[] getChildrenExpressions() { return childrenExpressions; }
    
	public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
		return null;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [Array of " +  //$NON-NLS-1$
				(getChildrenExpressions() == null ? 0 : getChildrenExpressions().length) +
				" children]"; //$NON-NLS-1$
	}
}
