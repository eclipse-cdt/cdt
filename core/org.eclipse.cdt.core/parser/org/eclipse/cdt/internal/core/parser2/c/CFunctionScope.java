
/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser2.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.internal.core.parser2.c.CVisitor.BaseVisitorAction;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CFunctionScope implements ICFunctionScope {
	private final CFunction function;
	
	public CFunctionScope( CFunction function ){
		this.function = function;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() {
		return function.getScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public List find(String name) {
		return null;
	}

	public List getLabels(){
	    FindLabelsAction action = new FindLabelsAction();
	    CVisitor.visitDeclaration( function.getDeclaration(), action );
	    
	    List bindings = new ArrayList();
	    for( int i = 0; i < action.labels.size(); i++ ){
	        IASTLabelStatement labelStatement = (IASTLabelStatement) action.labels.get(i);
	        IBinding binding = labelStatement.getName().resolveBinding();
	        if( binding != null )
	            bindings.add( binding );
	    }
	    return bindings;
	}
	
	static private class FindLabelsAction extends BaseVisitorAction {
        public List labels = new ArrayList();
        public boolean ambiguous = false;
        
        public FindLabelsAction(){
            processStatements = true;
        }
        
        public boolean processStatement( IASTStatement statement ) {
            if( statement instanceof IASTLabelStatement ){
               labels.add( statement );
            }
            return true;
        }
	}
}
