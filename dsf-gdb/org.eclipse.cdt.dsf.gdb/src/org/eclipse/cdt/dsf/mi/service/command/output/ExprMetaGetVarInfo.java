/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson           - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.gdb.GDBTypeParser.GDBType;
import org.eclipse.cdt.dsf.mi.service.command.commands.ExprMetaGetChildCount;

public class ExprMetaGetVarInfo implements ICommandResult {
    
	private final String expression;
	private final int numChildHint;
	private final String type;
	private final boolean editable;
	private final GDBType gdbType;
	/** If <code>true</code>, the variable is a collection, i.e. it may have children. */
	private final boolean isCollectionHint;
	private final boolean isSafeToAskForAllChildren;
	
    public ExprMetaGetVarInfo(String e, int n, String t, boolean edit) {
        this (e, n, t, null, edit);
    }

	/**
     * @since 3.0
     */
	public ExprMetaGetVarInfo(String e, int n, String t, GDBType gt, boolean edit) {
		this(e, true, n, t, gt, edit, false);
    }

	/**
     * @since 4.0
     */
	public ExprMetaGetVarInfo(String e, boolean isSafeToAskForAllChildren, int n,
			String t, GDBType gt, boolean edit, boolean isCollectionHint) {
    	expression = e;
    	this.isSafeToAskForAllChildren = isSafeToAskForAllChildren;
    	numChildHint = n;
    	type = t;    	
    	editable = edit;
    	gdbType = gt;
    	this.isCollectionHint = isCollectionHint;
    }
    
    public String getExpr() { return expression; }
    
	/**
	 * This method only returns a 'hint' to the number of children. In the case
	 * of C++ complex structures, this number will not be the actual number of
	 * children. This is because GDB considers 'private/protected/public' as an
	 * actual level of children, but we do not.
	 * In case of variable backed by a pretty printer, the number represents
	 * only the number of currently fetched children, not all children that
	 * might be available.
	 * 
	 * @return The hint on the number of children.
	 * 
	 * @deprecated Its not possible to tell the exact number of children, but
	 *             you can use {@link #hasChildren()} in order to find out
	 *             whether the variable has children at all. In order to find
	 *             out about the correct number of children, use {@link ExprMetaGetChildCount}.
	 */
    @Deprecated
	public int getNumChildren() { return numChildHint; }
    
    /**
     * @return Whether the variable has children or not (reliable).
     * 
     * @since 4.0
     */
    public boolean hasChildren() {
    	return (numChildHint > 0);
    }
    
    public String getType() { return type; }
    
    /**
     * @since 3.0
     */
    public GDBType getGDBType() { return gdbType; }
    
    public boolean getEditable() { return editable; }

    /**
     * @return If <code>true</code>, the variable is definitely a collection,
     * if <code>false</code>, it's most probably not.
     * 
     * @since 4.0
     */
    public boolean getCollectionHint() {
		return isCollectionHint;
	}
    
	@Override
	public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
		return null;
	}

	/**
	 * @return Whether this variable can be safely ask for all its children, or
	 *         whether clients need to specify a limit on the number of children
	 *         to be fetched, because otherwise the gdb might hang up.
	 *         
	 * @since 4.0
	 */
	public boolean isSafeToAskForAllChildren() {
		return isSafeToAskForAllChildren;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + //$NON-NLS-1$ 
				getExpr() + ", " + getNumChildren() + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
				getType() + ", " + getEditable() + ", " + //$NON-NLS-1$ //$NON-NLS-2$
				getCollectionHint() + ")"; //$NON-NLS-1$
	}
}
