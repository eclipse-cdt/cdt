/*************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 */
/*
 * Created on Apr 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author aniefer
 *
 */
public abstract class CPPSpecialization implements ICPPSpecialization, ICPPInternalBinding {
	private IBinding specialized;
	private ICPPScope scope;
	protected ObjectMap argumentMap;
	
	private IASTNode definition = null;
	private IASTNode [] declarations = null;
	
	public CPPSpecialization( IBinding specialized, ICPPScope scope, ObjectMap argumentMap ){
		this.specialized = specialized;
		this.scope = scope;
		this.argumentMap = argumentMap;
		
		if( specialized instanceof ICPPInternalBinding ){
			definition = ((ICPPInternalBinding)specialized).getDefinition();
			IASTNode [] decls = ((ICPPInternalBinding)specialized).getDeclarations();
			if( decls != null && decls.length > 0 )
				declarations = new IASTNode[]{ decls[0] };
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization#getSpecializedBinding()
	 */
	public IBinding getSpecializedBinding() {
		return specialized;
	}
	public IASTNode[] getDeclarations() {
		return declarations;
	}
	public IASTNode getDefinition() {
		return definition;
	}

	public void addDefinition(IASTNode node) {
		definition = node;
	}
	public void addDeclaration(IASTNode node) {
		if( declarations == null )
	        declarations = new IASTNode[] { node };
	    else {
	        //keep the lowest offset declaration in [0]
			if( declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset() ){
				declarations = (IASTNode[]) ArrayUtil.prepend( IASTNode.class, declarations, node );
			} else {
				declarations = (IASTNode[]) ArrayUtil.append( IASTNode.class, declarations, node );
			}
	    }
	}
	
	public void removeDeclaration(IASTNode node) {
		if( node == definition ){
			definition = null;
			return;
		}
		if( declarations != null ) {
			for (int i = 0; i < declarations.length; i++) {
				if( node == declarations[i] ) {
					if( i == declarations.length - 1 )
						declarations[i] = null;
					else
						System.arraycopy( declarations, i + 1, declarations, i, declarations.length - 1 - i );
				}
			}
		}
	}
	
	public String getName() {
		return specialized.getName();
	}
	public char[] getNameCharArray() {
		return specialized.getNameCharArray();
	}
	public IScope getScope() {
		return scope;
	}
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName( this );
	}
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray( this );
	}
	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPInternalBinding)specialized).isGloballyQualified();
	}

}
