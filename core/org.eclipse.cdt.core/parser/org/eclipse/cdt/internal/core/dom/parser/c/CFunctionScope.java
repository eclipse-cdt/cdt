
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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CFunctionScope extends CScope implements ICFunctionScope {
	public CFunctionScope( IASTFunctionDefinition function ){
	    super( function );
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICFunctionScope#getBinding(char[])
     */
    public IBinding getBinding( char[] name ) {
        return super.getBinding( NAMESPACE_TYPE_OTHER, name );
    }

    
	public IScope getBodyScope(){
	    IASTNode node = getPhysicalNode();
	    IASTStatement statement = ((IASTFunctionDefinition)node).getBody();
	    if( statement instanceof IASTCompoundStatement ){
	        return ((IASTCompoundStatement)statement).getScope();
	    }
	    return null;
	}

	public ILabel[] getLabels(){
	    FindLabelsAction action = new FindLabelsAction();
	    
        getPhysicalNode().accept( action );
	    
	    ILabel [] result = null;
	    if( action.labels != null ){
		    for( int i = 0; i < action.labels.length && action.labels[i] != null; i++ ){
		        IASTLabelStatement labelStatement = action.labels[i];
		        IBinding binding = labelStatement.getName().resolveBinding();
		        if( binding != null )
		            result = (ILabel[]) ArrayUtil.append( ILabel.class, result, binding );
		    }
	    }
	    return (ILabel[]) ArrayUtil.trim( ILabel.class, result );
	}
	
	static private class FindLabelsAction extends CASTVisitor {
        public IASTLabelStatement [] labels = null;
        
        public FindLabelsAction(){
            shouldVisitStatements = true;
        }
        
        public int visit( IASTStatement statement ) {
            if( statement instanceof IASTLabelStatement ){
               labels = (IASTLabelStatement[]) ArrayUtil.append( IASTLabelStatement.class, labels, statement );
            }
            return PROCESS_CONTINUE;
        }
	}
}
