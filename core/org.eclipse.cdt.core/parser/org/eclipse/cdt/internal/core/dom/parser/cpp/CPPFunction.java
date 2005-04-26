/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 1, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
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
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPFunction implements ICPPFunction, ICPPInternalFunction {
    
    public static class CPPFunctionDelegate extends CPPDelegate implements ICPPFunction, ICPPInternalFunction {
        public CPPFunctionDelegate( IASTName name, ICPPFunction binding ) {
            super( name, binding );
        }

        public IParameter[] getParameters() throws DOMException {
            return ((ICPPFunction)getBinding()).getParameters();
        }
        public IScope getFunctionScope() throws DOMException {
            return ((ICPPFunction)getBinding()).getFunctionScope();
        }
        public IFunctionType getType() throws DOMException {
            return ((ICPPFunction)getBinding()).getType();
        }
        public boolean isStatic() throws DOMException {
            return ((ICPPFunction)getBinding()).isStatic();
        }
        public boolean isMutable() throws DOMException {
            return ((ICPPFunction)getBinding()).isMutable();
        }
        public boolean isInline() throws DOMException {
            return ((ICPPFunction)getBinding()).isInline();
        }
        public boolean isExtern() throws DOMException {
            return ((ICPPFunction)getBinding()).isExtern();
        }
        public boolean isAuto() throws DOMException {
            return ((ICPPFunction)getBinding()).isAuto();
        }
        public boolean isRegister() throws DOMException {
            return ((ICPPFunction)getBinding()).isRegister();
        }
        public boolean takesVarArgs() throws DOMException {
            return ((ICPPFunction)getBinding()).takesVarArgs();
        }
        public boolean isStatic( boolean resolveAll ) {
            return ((ICPPInternalFunction)getBinding()).isStatic( resolveAll );
        }
    }
    public static class CPPFunctionProblem extends ProblemBinding implements ICPPFunction {
        public CPPFunctionProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
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
    
	protected ICPPASTFunctionDeclarator [] declarations;
	protected ICPPASTFunctionDeclarator definition;
	protected IFunctionType type = null;
	
	private static final int FULLY_RESOLVED         = 1;
	private static final int RESOLUTION_IN_PROGRESS = 1 << 1;
	private static final int IS_STATIC              = 3 << 2;
	private int bits = 0;
	
	public CPPFunction( ICPPASTFunctionDeclarator declarator ){
	    if( declarator != null ) {
			IASTNode parent = declarator.getParent();
			if( parent instanceof IASTFunctionDefinition )
				definition = declarator;
			else
				declarations = new ICPPASTFunctionDeclarator [] { declarator };
	    
		    IASTName name = declarator.getName();
		    if( name instanceof ICPPASTQualifiedName ){
		        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
		        name = ns[ ns.length - 1 ];
		    }
		    name.setBinding( this );
	    }
	}
	
	private void resolveAllDeclarations(){
	    if( (bits & (FULLY_RESOLVED | RESOLUTION_IN_PROGRESS)) == 0 ){
	        bits |= RESOLUTION_IN_PROGRESS;
		    IASTTranslationUnit tu = null;
	        if( definition != null )
	            tu = definition.getTranslationUnit();
	        else if( declarations != null )
	            tu = declarations[0].getTranslationUnit();
	        else {
	            //implicit binding
	            IScope scope = getScope();
                try {
                    IASTNode node = scope.getPhysicalNode();
                    while( !( node instanceof IASTTranslationUnit) )
    	                node = node.getParent();
    	            tu = (IASTTranslationUnit) node;
                } catch ( DOMException e ) {
                }
	        }
	        if( tu != null ){
	            CPPVisitor.getDeclarations( tu, this );
	        }
	        declarations = (ICPPASTFunctionDeclarator[]) ArrayUtil.trim( ICPPASTFunctionDeclarator.class, declarations );
	        bits |= FULLY_RESOLVED;
	        bits &= ~RESOLUTION_IN_PROGRESS;
	    }
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return declarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return definition;
    }
    
	public void addDefinition( IASTNode node ){
		if( node instanceof IASTName )
			node = node.getParent();
		if( !(node instanceof ICPPASTFunctionDeclarator) )
			return;
		ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) node;
		updateParameterBindings( dtor );
		definition = dtor;
	}
	public void addDeclaration( IASTNode node ){
		if( node instanceof IASTName )
			node = node.getParent();
		if( !(node instanceof ICPPASTFunctionDeclarator) )
			return;
		
		ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) node;
		updateParameterBindings( dtor );
		
		if( declarations == null ){
			declarations = new ICPPASTFunctionDeclarator [] { dtor };
			return;
		}
		
		//keep the lowest offset declaration in [0]
		if( declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset() ){
		    ICPPASTFunctionDeclarator temp = declarations[0];
		    declarations[0] = dtor;
		    dtor = temp;
		}
		
		declarations = (ICPPASTFunctionDeclarator[]) ArrayUtil.append( ICPPASTFunctionDeclarator.class, declarations, dtor );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter [] getParameters() {
	    IASTStandardFunctionDeclarator dtor = ( definition != null ) ? definition : declarations[0];
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
	    resolveAllDeclarations();
	    if( definition != null ){
			return definition.getFunctionScope();
	    } 
	        
	    return declarations[0].getFunctionScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
	    IASTName name = (definition != null ) ? definition.getName() : declarations[0].getName();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ ns.length - 1 ];
	    }
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    IASTName name = (definition != null ) ? definition.getName() : declarations[0].getName();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ ns.length - 1 ];
	    }
		return name.toCharArray();	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
	    IASTName n = definition != null ? definition.getName() : declarations[0].getName();
	    if( n instanceof ICPPASTQualifiedName ){
	    	IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
	    	n = ns[ ns.length - 1 ];
	    }
	    IScope scope = CPPVisitor.getContainingScope( n );
	    if( scope instanceof ICPPClassScope ){
	    	ICPPASTDeclSpecifier declSpec = null;
		    if( definition != null ){
		    	IASTNode node = definition.getParent();
		    	while( node instanceof IASTDeclarator )
		    		node = node.getParent();
		        IASTFunctionDefinition def = (IASTFunctionDefinition) node;
			    declSpec = (ICPPASTDeclSpecifier) def.getDeclSpecifier();    
		    } else {
		    	IASTNode node = declarations[0].getParent();
		    	while( node instanceof IASTDeclarator )
		    		node = node.getParent();
		        IASTSimpleDeclaration decl = (IASTSimpleDeclaration)node; 
		        declSpec = (ICPPASTDeclSpecifier) decl.getDeclSpecifier();
		    }
		    if( declSpec.isFriend() ) {
		        try {
	                while( scope instanceof ICPPClassScope ){
		                scope = scope.getParent();
	                }
		        } catch ( DOMException e ) {
	            }
		    }
	    }
		return scope;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
     */
    public IFunctionType getType() {
        if( type == null )
            type = (IFunctionType) CPPVisitor.createType( ( definition != null ) ? definition : declarations[0] );
        return type;
    }

    public IBinding resolveParameter( IASTParameterDeclaration param ){
    	IASTName name = param.getDeclarator().getName();
    	IBinding binding = name.getBinding();
    	if( binding != null )
    		return binding;
		
    	IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) param.getParent();
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
    		temp = definition.getParameters()[i];
    		IASTName n = temp.getDeclarator().getName();
    		if( n != name ) {
    		    n.setBinding( binding );
    		    ((CPPParameter)binding).addDeclaration( n );
    		}
    	}
    	if( declarations != null ){
    		for( int j = 0; j < declarations.length && declarations[j] != null; j++ ){
    			temp = declarations[j].getParameters()[i];
        		IASTName n = temp.getDeclarator().getName();
        		if( n != name ) {
        		    n.setBinding( binding );
        		    ((CPPParameter)binding).addDeclaration( n );
        		}

    		}
    	}
    	return binding;
    }
    
    protected void updateParameterBindings( ICPPASTFunctionDeclarator fdtor ){
    	ICPPASTFunctionDeclarator orig = definition != null ? definition : declarations[0];
    	IASTParameterDeclaration [] ops = orig.getParameters();
    	IASTParameterDeclaration [] nps = fdtor.getParameters();
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isStatic()
     */
    public boolean isStatic( ) {
        return isStatic( true );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction#isStatic(boolean)
     */
    public boolean isStatic( boolean resolveAll ) {
        if( resolveAll && (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }

        //2 state bits, most significant = whether or not we've figure this out yet
        //least significant = whether or not we are static
        int state = ( bits & IS_STATIC ) >> 2;
        if( state > 1 ) return (state % 2 != 0);
        
        IASTDeclSpecifier declSpec = null;
        IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) getDefinition();
        if( dtor != null ){
	        declSpec = ((IASTFunctionDefinition)dtor.getParent()).getDeclSpecifier();
	        if( declSpec.getStorageClass() == IASTDeclSpecifier.sc_static ){
	            bits |= 3 << 2;
	            return true;
	        }
        }
        
        IASTFunctionDeclarator[] dtors = (IASTFunctionDeclarator[]) getDeclarations();
        if( dtors != null ) {
	        for( int i = 0; i < dtors.length; i++ ){
	            IASTNode parent = dtors[i].getParent();
	            declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            if( declSpec.getStorageClass() == IASTDeclSpecifier.sc_static ){
	                bits |= 3 << 2;
	                return true;
	            }
	        }
        }
        bits |= 2 << 2;
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope )
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPFunctionDelegate( name, this );
    }

	public boolean hasStorageClass( int storage ){
	    ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) getDefinition();
        ICPPASTFunctionDeclarator[] ds = (ICPPASTFunctionDeclarator[]) getDeclarations();
        int i = -1;
        do{
            if( dtor != null ){
                IASTNode parent = dtor.getParent();
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
            if( ds != null && ++i < ds.length )
                dtor = ds[i];
            else
                break;
        } while( dtor != null );
        return false;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isInline()
     */
    public boolean isInline() throws DOMException {
	    ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) getDefinition();
        ICPPASTFunctionDeclarator[] ds = (ICPPASTFunctionDeclarator[]) getDeclarations();
        int i = -1;
        do{
            if( dtor != null ){
                IASTNode parent = dtor.getParent();
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
            if( ds != null && ++i < ds.length )
                dtor = ds[i];
            else
                break;
        } while( dtor != null );
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
        return hasStorageClass( IASTDeclSpecifier.sc_register );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#takesVarArgs()
     */
    public boolean takesVarArgs() {
        ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) getDefinition();
        if( dtor != null ){
            return dtor.takesVarArgs();
        }
        ICPPASTFunctionDeclarator [] ds = (ICPPASTFunctionDeclarator[]) getDeclarations();
        if( ds != null && ds.length > 0 ){
            return ds[0].takesVarArgs();
        }
        return false;
    }
}
