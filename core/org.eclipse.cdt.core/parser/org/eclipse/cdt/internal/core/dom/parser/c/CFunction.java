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

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CFunction implements IFunction, ICInternalBinding {
	private IASTStandardFunctionDeclarator [] declarators = null;
	private IASTFunctionDeclarator definition;
	
	private static final int FULLY_RESOLVED         = 1;
	private static final int RESOLUTION_IN_PROGRESS = 1 << 1;
	private static final int IS_STATIC              = 3 << 2;
	private int bits = 0;
	
	IFunctionType type = null;
	
	public CFunction( IASTFunctionDeclarator declarator ){
	    if( declarator.getParent() instanceof IASTFunctionDefinition || declarator instanceof ICASTKnRFunctionDeclarator )
	        definition = declarator;
	    else {
	        declarators = new IASTStandardFunctionDeclarator [] { (IASTStandardFunctionDeclarator) declarator };
	    }
	}
	
    public IASTNode getPhysicalNode(){
        return ( definition != null ) ? definition : declarators[0];
    }
    public void addDeclarator( IASTFunctionDeclarator fnDeclarator ){
        updateParameterBindings( fnDeclarator );
        if( fnDeclarator.getParent() instanceof IASTFunctionDefinition || fnDeclarator instanceof ICASTKnRFunctionDeclarator ) 
            definition = fnDeclarator;
        else {
            if( declarators == null ){
                declarators = new IASTStandardFunctionDeclarator[] { (IASTStandardFunctionDeclarator) fnDeclarator };
            	return;
            }
            for( int i = 0; i < declarators.length; i++ ){
                if( declarators[i] == null ){
                    declarators[i] = (IASTStandardFunctionDeclarator) fnDeclarator;
                    return;
                }
            }
            IASTStandardFunctionDeclarator tmp [] = new IASTStandardFunctionDeclarator [ declarators.length * 2 ];
            System.arraycopy( declarators, 0, tmp, 0, declarators.length );
            tmp[ declarators.length ] = (IASTStandardFunctionDeclarator) fnDeclarator;
            declarators = tmp;
        }
    }
	
    private void resolveAllDeclarations(){
	    if( (bits & (FULLY_RESOLVED | RESOLUTION_IN_PROGRESS)) == 0 ){
	        bits |= RESOLUTION_IN_PROGRESS;
		    IASTTranslationUnit tu = null;
	        if( definition != null )
	            tu = definition.getTranslationUnit();
	        else if( declarators != null )
	            tu = declarators[0].getTranslationUnit();
	        
	        if( tu != null ){
	            CPPVisitor.getDeclarations( tu, this );
	        }
	        declarators = (IASTStandardFunctionDeclarator[]) ArrayUtil.trim( IASTStandardFunctionDeclarator.class, declarators );
	        bits |= FULLY_RESOLVED;
	        bits &= ~RESOLUTION_IN_PROGRESS;
	    }
	}
    
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
			IASTName[] names = ((ICASTKnRFunctionDeclarator)dtor).getParameterNames();
			result = new IParameter[ names.length ];
			if( names.length > 0 ){
				// ensures that the List of parameters is created in the same order as the K&R C parameter names
				for( int i=0; i<names.length; i++ ) {
				    IASTDeclarator decl = CVisitor.getKnRParameterDeclarator( (ICASTKnRFunctionDeclarator) dtor, names[i] );
				    if( decl != null ) {
				        result[i] = (IParameter) decl.getName().resolveBinding();
				    } else {
				        result[i] = new CParameter.CParameterProblem( names[i], IProblemBinding.SEMANTIC_KNR_PARAMETER_DECLARATION_NOT_FOUND, names[i].toCharArray() );
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
	    return dtor.getName().toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
	    IASTFunctionDeclarator dtor = ( definition != null ) ? definition : declarators[0];
		return CVisitor.getContainingScope( dtor.getParent() );
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
        	
        	while (functionName.getNestedDeclarator() != null)
        		functionName = functionName.getNestedDeclarator();
        	
        	IType tempType = CVisitor.createType( functionName );
        	if (tempType instanceof IFunctionType)
        		type = (IFunctionType)tempType;
        }
        
        return type;
    }
	
    public IBinding resolveParameter( IASTName paramName ){
    	if( paramName.getBinding() != null )
    	    return paramName.getBinding();

    	IBinding binding = null;
    	int idx = 0;
    	IASTNode parent = paramName.getParent();
    	while( parent instanceof IASTDeclarator && !(parent instanceof ICASTKnRFunctionDeclarator ) )
    	    parent = parent.getParent();
    	
    	ICASTKnRFunctionDeclarator fKnRDtor = null;
    	IASTDeclarator knrParamDtor = null;
    	if( parent instanceof IASTParameterDeclaration ){
    	    IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) parent.getParent();
    	    IASTParameterDeclaration [] ps = fdtor.getParameters();
        	for( ; idx < ps.length; idx++ ){
        		if( parent == ps[idx] )
        			break;
        	}
    	} else if( parent instanceof IASTSimpleDeclaration ){ 
    	    //KnR: name in declaration list
    	    fKnRDtor = (ICASTKnRFunctionDeclarator) parent.getParent();
    	    IASTName [] ps = fKnRDtor.getParameterNames();
    	    char [] n = paramName.toCharArray();
        	for( ; idx < ps.length; idx++ ){
        		if( CharArrayUtils.equals( ps[idx].toCharArray(), n ) )
        			break;
        	}    	    
    	} else {
    	    //KnR: name in name list
    	    fKnRDtor = (ICASTKnRFunctionDeclarator) parent;
    	    IASTName [] ps = fKnRDtor.getParameterNames();
        	for( ; idx < ps.length; idx++ ){
        		if( ps[idx] == paramName)
        			break;
        	}
        	knrParamDtor = CVisitor.getKnRParameterDeclarator( fKnRDtor, paramName );
        	paramName = knrParamDtor.getName();
    	}
    	
    	//create a new binding and set it for the corresponding parameter in all known defns and decls
    	binding = new CParameter( paramName );
    	IASTParameterDeclaration temp = null;
    	if( definition != null ){
    	    if( definition instanceof IASTStandardFunctionDeclarator ){
    	        temp = ((IASTStandardFunctionDeclarator)definition).getParameters()[idx];
        		temp.getDeclarator().getName().setBinding( binding );
    	    } else if( definition instanceof ICASTKnRFunctionDeclarator ){
    	        IASTName n = fKnRDtor.getParameterNames()[idx];
    	        n.setBinding( binding );
    	        IASTDeclarator dtor = CVisitor.getKnRParameterDeclarator( fKnRDtor, n );
    	        if( dtor != null ){
    	            dtor.getName().setBinding( binding );
    	        }
    	    }
    	}
    	if( declarators != null ){
    		for( int j = 0; j < declarators.length && declarators[j] != null; j++ ){
    		    if( declarators[j].getParameters().length > idx ){
					temp = declarators[j].getParameters()[idx];
		    		temp.getDeclarator().getName().setBinding( binding );
    		    }
    		}
    	}
    	return binding;
    }
    

    
    protected void updateParameterBindings( IASTFunctionDeclarator fdtor ){
        CParameter temp = null;
        if( fdtor instanceof IASTStandardFunctionDeclarator ){
            IASTStandardFunctionDeclarator orig = (IASTStandardFunctionDeclarator) getPhysicalNode();
        	IASTParameterDeclaration [] ops = orig.getParameters();
        	IASTParameterDeclaration [] nps = ((IASTStandardFunctionDeclarator)fdtor).getParameters();

        	for( int i = 0; i < nps.length; i++ ){
        	    IASTName origname = ops[i].getDeclarator().getName();
        	    if( origname.getBinding() != null ){
        	        temp = (CParameter) origname.getBinding();
            		if( temp != null ){
            		    IASTName name = nps[i].getDeclarator().getName();
            			name.setBinding( temp );
            			temp.addDeclaration( name );
            		}    
        	    }
        		
        	}
        } else {
            IASTParameterDeclaration [] ops = declarators[0].getParameters();
            IASTName [] ns = ((ICASTKnRFunctionDeclarator)fdtor).getParameterNames();
            if( ops.length > 0 && ops.length != ns.length )
                return; //problem
            
            for( int i = 0; i < ops.length; i++ ){
        	    IASTName origname = ops[i].getDeclarator().getName();
        	    if( origname.getBinding() != null ){
        	        temp = (CParameter) origname.resolveBinding();
            		if( temp != null ){
            		    IASTName name = ns[i];
            			name.setBinding( temp );
            			
            			IASTDeclarator dtor = CVisitor.getKnRParameterDeclarator( (ICASTKnRFunctionDeclarator) fdtor, name );
            			if( dtor != null ){
            			    dtor.getName().setBinding( temp );
            			    temp.addDeclaration( dtor.getName() );
            			}
            		}    
        	    }
        	}
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isStatic()
     */
    public boolean isStatic() {
        if( (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }

        //2 state bits, most significant = whether or not we've figure this out yet
        //least significant = whether or not we are static
        int state = ( bits & IS_STATIC ) >> 2;
        if( state > 1 ) return (state % 2 != 0);
        
        
        IASTFunctionDeclarator dtor = definition;
        IASTDeclSpecifier declSpec = null;
        if( dtor != null ){
	        declSpec = ((IASTFunctionDefinition)dtor.getParent()).getDeclSpecifier();
	        if( declSpec.getStorageClass() == IASTDeclSpecifier.sc_static ){
	            bits |= 3 << 2;
	            return true;
	        }
        }
        
        for( int i = 0; i < declarators.length; i++ ){
            IASTNode parent = declarators[i].getParent();
            declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
            if( declSpec.getStorageClass() == IASTDeclSpecifier.sc_static ){
                bits |= 3 << 2;
                return true;
            }
        }
        bits |= 2 << 2;
        return false;
    }
}
