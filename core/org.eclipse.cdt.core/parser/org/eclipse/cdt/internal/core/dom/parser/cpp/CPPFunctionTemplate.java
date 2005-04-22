/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 31, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPFunctionTemplate extends CPPTemplateDefinition implements ICPPFunctionTemplate, ICPPFunction {
	public static final class CPPFunctionTemplateProblem extends ProblemBinding	implements ICPPFunctionTemplate, ICPPFunction {
		public CPPFunctionTemplateProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}
		public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPTemplateSpecialization[] getTemplateSpecializations() throws DOMException {
			throw new DOMException( this );		
		}
		public String[] getQualifiedName() throws DOMException {
			throw new DOMException( this );		
		}
		public char[][] getQualifiedNameCharArray() throws DOMException {
			throw new DOMException( this );
		}
		public boolean isGloballyQualified() throws DOMException {
			throw new DOMException( this );
		}
		public boolean isMutable() throws DOMException {
			throw new DOMException( this );
		}
		public boolean isInline() throws DOMException {
			throw new DOMException( this );
		}
		public IParameter[] getParameters() throws DOMException {
			throw new DOMException( this );
		}
		public IScope getFunctionScope() throws DOMException {
			throw new DOMException( this );
		}
		public IFunctionType getType() throws DOMException {
			throw new DOMException( this );
		}
		public boolean isStatic() throws DOMException {
			throw new DOMException( this );
		}
		public boolean isExtern() throws DOMException {
			throw new DOMException( this );
		}
		public boolean isAuto() throws DOMException {
			throw new DOMException( this );
		}
		public boolean isRegister() throws DOMException {
			throw new DOMException( this );
		}
		public boolean takesVarArgs() throws DOMException {
			throw new DOMException( this );
		}
	}
	protected IFunctionType type = null;
	/**
	 * @param decl
	 */
	public CPPFunctionTemplate(IASTName name) {
		super(name);
	}

	public void addDefinition(IASTNode node) {
		if( !(node instanceof IASTName) )
			return;
		updateFunctionParameterBindings( (IASTName) node );
		super.addDefinition( node );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
		if( !(node instanceof IASTName) )
			return;
		updateFunctionParameterBindings( (IASTName) node );
		super.addDeclaration( node );
	}	
	/**
	 * @param name
	 */
	private void updateFunctionParameterBindings(IASTName paramName) {
		IASTName defName = definition != null ? definition : declarations[0];
		ICPPASTFunctionDeclarator orig = (ICPPASTFunctionDeclarator) defName.getParent();
    	IASTParameterDeclaration [] ops = orig.getParameters();
    	IASTParameterDeclaration [] nps = ((ICPPASTFunctionDeclarator)paramName.getParent()).getParameters();
    	CPPParameter temp = null;
    	for( int i = 0; i < nps.length; i++ ){
    		temp = (CPPParameter) ops[i].getDeclarator().getName().getBinding();
    		if( temp != null ){
    		    IASTName name = nps[i].getDeclarator().getName();
    			name.setBinding( temp );
    			temp.addDeclaration( name );
    		}
    	}
	}

	/**
	 * @param templateParameter
	 * @return
	 */
//	public IBinding resolveParameter(ICPPASTTemplateParameter templateParameter) {
//		return null;
//	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter[] getParameters() {
		IASTName name = getTemplateName();
		IASTNode parent = name.getParent();
		if( parent instanceof ICPPASTQualifiedName )
			parent = parent.getParent();
		if( parent instanceof ICPPASTFunctionDeclarator ){
			ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parent;
			IASTParameterDeclaration[] params = dtor.getParameters();
			int size = params.length;
			IParameter [] result = new IParameter[ size ];
			if( size > 0 ){
				for( int i = 0; i < size; i++ ){
					IASTParameterDeclaration p = params[i];
					result[i] = (IParameter) p.getDeclarator().getName().resolveBinding();
				}
			}
			return result;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
	 */
	public IFunctionType getType() {
		if( type == null ) {
			IASTName name = getTemplateName();
			IASTNode parent = name.getParent();
			while( parent.getParent() instanceof IASTDeclarator )
				parent = parent.getParent();
			
			IType temp = CPPVisitor.createType( (IASTDeclarator)parent );
			if( temp instanceof IFunctionType )
				type = (IFunctionType) temp;
		}
		return type;
	}

	public boolean hasStorageClass( int storage ){
	    IASTName name = (IASTName) getDefinition();
        IASTNode[] ns = getDeclarations();
        int i = -1;
        do{
            if( name != null ){
                IASTNode parent = name.getParent();
	            while( !(parent instanceof IASTDeclaration) )
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = null;
	            if( parent instanceof IASTSimpleDeclaration )
	                declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            else if( parent instanceof IASTFunctionDefinition )
	                declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
                if( declSpec.getStorageClass() == storage )
                    return true;
            }
            if( ns != null && ++i < ns.length )
                name = (IASTName) ns[i];
            else
                break;
        } while( name != null );
        return false;
	}

	/**
	 * @param param
	 * @return
	 */
	public IBinding resolveFunctionParameter(ICPPASTParameterDeclaration param) {
	   	IASTName name = param.getDeclarator().getName();
    	IBinding binding = name.getBinding();
    	if( binding != null )
    		return binding;
		
    	ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) param.getParent();
    	IASTParameterDeclaration [] ps = fdtor.getParameters();
    	int i = 0;
    	for( ; i < ps.length; i++ ){
    		if( param == ps[i] )
    			break;
    	}
    	
    	//create a new binding and set it for the corresponding parameter in all known defns and decls
    	binding = new CPPParameter( name );
    	IASTParameterDeclaration temp = null;
    	if( definition != null ){
    		IASTNode node = definition.getParent();
    		if( node instanceof ICPPASTQualifiedName )
    			node = node.getParent();
    		temp = ((ICPPASTFunctionDeclarator)node).getParameters()[i];
    		IASTName n = temp.getDeclarator().getName();
    		if( n != name ) {
    		    n.setBinding( binding );
    		    ((CPPParameter)binding).addDeclaration( n );
    		}
    	}
    	if( declarations != null ){
    		for( int j = 0; j < declarations.length && declarations[j] != null; j++ ){
    			temp = ((ICPPASTFunctionDeclarator)declarations[j].getParent()).getParameters()[i];
        		IASTName n = temp.getDeclarator().getName();
        		if( n != name ) {
        		    n.setBinding( binding );
        		    ((CPPParameter)binding).addDeclaration( n );
        		}

    		}
    	}
    	return binding;	
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateDefinition#deferredInstance(org.eclipse.cdt.core.dom.ast.IType[])
	 */
	public ICPPTemplateInstance deferredInstance(IType[] arguments) {
		return new CPPDeferredFunctionInstance( this, arguments );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#isStatic()
	 */
	public boolean isStatic() {
		return hasStorageClass( IASTDeclSpecifier.sc_static );
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isMutable()
     */
    public boolean isMutable() {
        return hasStorageClass( ICPPASTDeclSpecifier.sc_mutable );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isInline()
     */
    public boolean isInline() throws DOMException {
        IASTName name = (IASTName) getDefinition();
        IASTNode[] ns = getDeclarations();
        int i = -1;
        do{
            if( name != null ){
                IASTNode parent = name.getParent();
	            while( !(parent instanceof IASTDeclaration) )
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = null;
	            if( parent instanceof IASTSimpleDeclaration )
	                declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            else if( parent instanceof IASTFunctionDefinition )
	                declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	            
	            if( declSpec.isInline() )
                    return true;
            }
            if( ns != null && ++i < ns.length )
                name = (IASTName) ns[i];
            else
                break;
        } while( name != null );
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isExtern()
     */
    public boolean isExtern() {
        return hasStorageClass( IASTDeclSpecifier.sc_extern );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isAuto()
     */
    public boolean isAuto() {
        return hasStorageClass( IASTDeclSpecifier.sc_auto );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isRegister()
     */
    public boolean isRegister() {
        return hasStorageClass( IASTDeclSpecifier.sc_register);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#takesVarArgs()
     */
    public boolean takesVarArgs() {
        IASTName name = (IASTName) getDefinition();
        if( name != null ){
            ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) name.getParent();
            return dtor.takesVarArgs();
        }
        IASTName [] ns = (IASTName[]) getDeclarations();
        if( ns != null && ns.length > 0 ){
            ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) ns[0].getParent();
            return dtor.takesVarArgs();
        }
        return false;
    }

}
