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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CFunction implements IFunction {
	private IASTFunctionDeclarator [] declarators = null;
	private IASTFunctionDeclarator definition;
	IFunctionType type = null;
	
	public CFunction( IASTFunctionDeclarator declarator ){
	    if( declarator.getParent() instanceof IASTFunctionDefinition )
	        definition = declarator;
	    else {
	        declarators = new IASTFunctionDeclarator [] { declarator };
	    }
	}
	
    public IASTNode getPhysicalNode(){
        return ( definition != null ) ? definition : declarators[0];
    }
    public void addDeclarator( IASTFunctionDeclarator fnDeclarator ){
        if( fnDeclarator instanceof IASTFunctionDefinition || fnDeclarator instanceof ICASTKnRFunctionDeclarator ) 
            definition = fnDeclarator;
        else {
            if( declarators == null ){
                declarators = new IASTFunctionDeclarator[] { fnDeclarator };
            	return;
            }
            for( int i = 0; i < declarators.length; i++ ){
                if( declarators[i] == null ){
                    declarators[i] = fnDeclarator;
                    return;
                }
            }
            IASTFunctionDeclarator tmp [] = new IASTFunctionDeclarator [ declarators.length * 2 ];
            System.arraycopy( declarators, 0, tmp, 0, declarators.length );
            tmp[ declarators.length ] = fnDeclarator;
            declarators = tmp;
        }
    }
//	private IASTFunctionDeclarator checkForDefinition( IASTFunctionDeclarator dtor ){
//		if( dtor.getParent() instanceof IASTFunctionDefinition )
//			return dtor;
//		
//		IASTFunctionDeclarator def = CVisitor.findDefinition( dtor );
//		if( def != null && def != dtor ){
//			dtor = def;
//			((CASTName)dtor.getName()).setBinding( this );
//		}
//		return dtor;
//	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter[] getParameters() {
		IParameter [] result = null;
		
	    IASTFunctionDeclarator dtor = ( definition != null ) ? definition : declarators[0];
		if (dtor instanceof IASTStandardFunctionDeclarator) {
			IASTParameterDeclaration[] params = ((IASTStandardFunctionDeclarator)dtor).getParameters();
			int size = params.length;
			result = new IParameter[ size ];
			if( size > 0 ){
				for( int i = 0; i < size; i++ ){
					IASTParameterDeclaration p = params[i];
					result[i] = (IParameter) p.getDeclarator().getName().resolveBinding();
				}
			}
		} else if (dtor instanceof ICASTKnRFunctionDeclarator) {
			IASTDeclaration[] params = ((ICASTKnRFunctionDeclarator)dtor).getParameterDeclarations();
			IASTName[] names = ((ICASTKnRFunctionDeclarator)dtor).getParameterNames();
			result = new IParameter[ names.length ];
			if( names.length > 0 ){
				// ensures that the List of parameters is created in the same order as the K&R C parameter names
				for( int i=0; i<names.length; i++ ) {
					for( int j=0; j<params.length; j++) {
						if ( params[j] instanceof IASTSimpleDeclaration ) {
							IASTDeclarator[] decltors = ((IASTSimpleDeclaration)params[j]).getDeclarators();
							for (int k=0; k<decltors.length; k++) {
								if ( CharArrayUtils.equals(names[i].toCharArray(), decltors[k].getName().toCharArray()) ) {
									result[i] = (IParameter) decltors[k].getName().resolveBinding();
								}
							}						}
					}
				}
			}
		}
		
		return result;	    
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
	    IASTFunctionDeclarator dtor = ( definition != null ) ? definition : declarators[0];
		return dtor.getName().toString();
	}
	public char[] getNameCharArray(){
	    IASTFunctionDeclarator dtor = ( definition != null ) ? definition : declarators[0];
	    return ((CASTName) dtor.getName()).toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
	    IASTFunctionDeclarator dtor = ( definition != null ) ? definition : declarators[0];
		return CVisitor.getContainingScope( (IASTDeclaration) dtor.getParent() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
		if( definition != null ){
			IASTFunctionDefinition def = (IASTFunctionDefinition) definition.getParent();
			return def.getScope();
		}
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
     */
    public IFunctionType getType() {
        if( type == null ) {
        	IASTDeclarator functionName = ( definition != null ) ? definition : declarators[0];
        	
        	while (functionName.getName().toString() == null)
        		functionName = functionName.getNestedDeclarator();
        	
        	IType tempType = CVisitor.createType( functionName.getName() );
        	if (tempType instanceof IFunctionType)
        		type = (IFunctionType)tempType;
        }
        
        return type;
    }
	
//	public IASTDeclaration getDeclaration(){
//	    return (IASTDeclaration) declarator.getParent();
//	}
}
