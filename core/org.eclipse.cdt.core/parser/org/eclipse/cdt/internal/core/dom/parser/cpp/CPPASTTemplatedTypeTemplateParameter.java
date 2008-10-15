/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisitor;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTTemplatedTypeTemplateParameter extends ASTNode implements
        ICPPASTTemplatedTypeTemplateParameter, IASTAmbiguityParent {

	
    public CPPASTTemplatedTypeTemplateParameter() {
	}

	public CPPASTTemplatedTypeTemplateParameter(IASTName name, IASTExpression defaultValue) {
		setName(name);
		setDefaultValue(defaultValue);
	}

	public ICPPASTTemplateParameter[] getTemplateParameters() {
        if( parameters == null ) return ICPPASTTemplateParameter.EMPTY_TEMPLATEPARAMETER_ARRAY;
        parameters = (ICPPASTTemplateParameter[]) ArrayUtil.removeNullsAfter( ICPPASTTemplateParameter.class, parameters, parametersPos );
        return parameters;
    }

    public void addTemplateParamter(ICPPASTTemplateParameter parm) {
    	if(parm != null) {
    		parameters = (ICPPASTTemplateParameter[]) ArrayUtil.append( ICPPASTTemplateParameter.class, parameters, ++parametersPos, parm );
    		parm.setParent(this);
			parm.setPropertyInParent(PARAMETER);
    	}
    }

    private ICPPASTTemplateParameter [] parameters = null;
    private int parametersPos=-1;
    private IASTName name;
    private IASTExpression defaultValue;

    public IASTName getName() {
        return name;
    }

    public void setName(IASTName name) {
        this.name =name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(PARAMETER_NAME);
		}
    }

    public IASTExpression getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(IASTExpression expression) {
        this.defaultValue = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(DEFAULT_VALUE);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
    	if (action.shouldVisitTemplateParameters && action instanceof ICPPASTVisitor) {
		    switch( ((ICPPASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        ICPPASTTemplateParameter [] ps = getTemplateParameters();
        for ( int i = 0; i < ps.length; i++ ) {
            if( !ps[i].accept( action ) ) return false;
        }
        if( name != null ) if( !name.accept( action ) ) return false;
        if( defaultValue != null ) if( !defaultValue.accept( action ) ) return false;
        
    	if (action.shouldVisitTemplateParameters && action instanceof ICPPASTVisitor) {
    		switch( ((ICPPASTVisitor)action).leave( this ) ){
    		case ASTVisitor.PROCESS_ABORT : return false;
    		case ASTVisitor.PROCESS_SKIP  : return true;
    		default : break;
    		}
    	}
        return true;
    }

	public int getRoleForName(IASTName n) {
		if( n == name )
			return r_declaration;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == defaultValue )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            defaultValue  = (IASTExpression) other;
        }
    }
}
