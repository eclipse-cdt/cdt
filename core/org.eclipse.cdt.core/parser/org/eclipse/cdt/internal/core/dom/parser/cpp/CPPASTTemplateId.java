/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;

/**
 * @author jcamelon
 */
public class CPPASTTemplateId extends CPPASTNode implements ICPPASTTemplateId {
    private static final char[] EMPTY_CHAR_ARRAY = { };
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private IASTName templateName;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#getTemplateName()
     */
    public IASTName getTemplateName() {
        return templateName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#setTemplateName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setTemplateName(IASTName name) {
        templateName = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#addTemplateArgument(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void addTemplateArgument(IASTTypeId typeId) {
        if( templateArguments == null )
        {
            templateArguments = new IASTNode[ DEFAULT_ARGS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( templateArguments.length == currentIndex )
        {
            IASTNode [] old = templateArguments;
            templateArguments = new IASTNode[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                templateArguments[i] = old[i];
        }
        templateArguments[ currentIndex++ ] = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#addTemplateArgument(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void addTemplateArgument(IASTExpression expression) {
        if( templateArguments == null )
        {
            templateArguments = new IASTNode[ DEFAULT_ARGS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( templateArguments.length == currentIndex )
        {
            IASTNode [] old = templateArguments;
            templateArguments = new IASTNode[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                templateArguments[i] = old[i];
        }
        templateArguments[ currentIndex++ ] = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#getTemplateArguments()
     */
    public IASTNode[] getTemplateArguments() {
        if( templateArguments == null ) return ICPPASTTemplateId.EMPTY_ARG_ARRAY;
        removeNullArguments();
        return templateArguments;
    }
    
    private void removeNullArguments() {
        int nullCount = 0; 
        for( int i = 0; i < templateArguments.length; ++i )
            if( templateArguments[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTNode [] old = templateArguments;
        int newSize = old.length - nullCount;
        templateArguments = new IASTNode[ newSize ];
        for( int i = 0; i < newSize; ++i )
            templateArguments[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTNode [] templateArguments = null;
    private static final int DEFAULT_ARGS_LIST_SIZE = 4;
    private IBinding binding = null;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
     */
    public IBinding resolveBinding() {
    	if( binding == null )
    		binding = CPPTemplates.createBinding( this ); 
    	
        return binding;    
    }

	public IBinding[] resolvePrefix() {
		// TODO Auto-generated method stub
		return null;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTName#toCharArray()
     */
    public char[] toCharArray() {
        return EMPTY_CHAR_ARRAY;
    }
    public String toString() {
        return EMPTY_STRING;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitNames ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( templateName != null ) if( !templateName.accept( action ) ) return false;
        
        IASTNode [] nodes = getTemplateArguments();
        for ( int i = 0; i < nodes.length; i++ ) {
            if( !nodes[i].accept( action ) ) return false;
        }
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#isDeclaration()
	 */
	public boolean isDeclaration() {
		return false; //for now this seems to be true
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#isReference()
	 */
	public boolean isReference() {
		return true; //for now this seems to be true
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n) {
		if( n == templateName )
			return r_reference;
		return r_unclear;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#getBinding()
	 */
	public IBinding getBinding() {
		return binding;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#setBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void setBinding(IBinding binding) {
		this.binding = binding;
	}
}
