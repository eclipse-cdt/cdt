
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

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CStructure implements ICompositeType {
	final private IASTDeclSpecifier declSpec;
	
	public CStructure( IASTDeclSpecifier declSpec ){
		declSpec = checkForDefinition( declSpec );
		this.declSpec = declSpec;
	}
	
	private IASTDeclSpecifier checkForDefinition( IASTDeclSpecifier declSpec ){
		if( declSpec instanceof ICASTCompositeTypeSpecifier )
			return declSpec;
		
		IASTDeclSpecifier spec = CVisitor.findDefinition( (ICASTElaboratedTypeSpecifier) declSpec );
		if( spec != null && spec instanceof ICASTCompositeTypeSpecifier ){
			declSpec = spec;
			ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) spec;
			((CASTName)compTypeSpec.getName()).setBinding( this );
		}
		return declSpec;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		if( declSpec instanceof ICASTCompositeTypeSpecifier )
			return ((ICASTCompositeTypeSpecifier)declSpec).getName().toString();
		else if( declSpec instanceof ICASTElaboratedTypeSpecifier )
			return ((ICASTElaboratedTypeSpecifier)declSpec).getName().toString();
		
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CVisitor.getContainingScope( declSpec );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public List getFields() {
		if( !( declSpec instanceof ICASTCompositeTypeSpecifier ) ){
			//error
			return null;
		}
		
		ICASTCompositeTypeSpecifier compositeTypeSpec = (ICASTCompositeTypeSpecifier) declSpec;
		List members = compositeTypeSpec.getMembers();
		int size = members.size();
		List fields = new ArrayList( size );
		if( size > 0 ){

			for( int i = 0; i < size; i++ ){
				IASTNode node = (IASTNode) members.get(i);
				if( node instanceof IASTSimpleDeclaration ){
					List declarators = ((IASTSimpleDeclaration)node).getDeclarators();
					for( int j = 0; j < declarators.size(); j++ ){
						IASTDeclarator declarator = (IASTDeclarator) declarators.get(i);
						IBinding binding = declarator.getName().resolveBinding();
						if( binding != null )
							fields.add( binding );
					}
				}
			}
			
		}
		return fields;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public IField findField(String name) {
		if( !( declSpec instanceof ICASTCompositeTypeSpecifier ) ){
			//error
			return null;
		}
		
		ICASTCompositeTypeSpecifier compositeTypeSpec = (ICASTCompositeTypeSpecifier) declSpec;
		List members = compositeTypeSpec.getMembers();
		int size = members.size();
		if( size > 0 ){
			for( int i = 0; i < size; i++ ){
				IASTNode node = (IASTNode) members.get(i);
				if( node instanceof IASTSimpleDeclaration ){
					List declarators = ((IASTSimpleDeclaration)node).getDeclarators();
					for( int j = 0; j < declarators.size(); j++ ){
						IASTDeclarator declarator = (IASTDeclarator) declarators.get(i);
						if( name.equals( declarator.getName().toString() ) ){
							IBinding binding = declarator.getName().resolveBinding();
							if( binding instanceof IField )
								return (IField) binding;
						}
					}
				}
			}
		}
		return null;
	}
}
