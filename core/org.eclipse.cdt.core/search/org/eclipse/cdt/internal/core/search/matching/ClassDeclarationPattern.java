/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import org.eclipse.cdt.core.parser.ast.ClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;
import org.eclipse.cdt.internal.core.search.CharOperation;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ClassDeclarationPattern extends CSearchPattern {

	public ClassDeclarationPattern( int matchMode, boolean caseSensitive ){
		super( matchMode, caseSensitive );
	}
	
	public ClassDeclarationPattern( char[] name, char[][] containers, ClassKind kind, int mode, boolean caseSensitive ){
		super( mode, caseSensitive );
		simpleName = caseSensitive ? name : CharOperation.toLowerCase( name );
		if( caseSensitive || containers == null ){
			containingTypes = containers;
		} else {
			int len = containers.length;
			this.containingTypes = new char[ len ][];
			for( int i = 0; i < len; i++ ){
				this.containingTypes[i] = CharOperation.toLowerCase( containers[i] );
			}
		} 
		classKind = kind;
	}
	
	public int matchLevel( IASTOffsetableElement node ){
		if( !( node instanceof IASTClassSpecifier ) )
			return IMPOSSIBLE_MATCH;
			
		IASTClassSpecifier clsSpec = (IASTClassSpecifier) node;
	
		//check name, if simpleName == null, its treated the same as "*"	
		if( simpleName != null && !matchesName( simpleName, clsSpec.getName().toCharArray() ) ){
			return IMPOSSIBLE_MATCH;
		}
		
		//check containing scopes
		String [] qualifications = clsSpec.getFullyQualifiedName();
		if( qualifications != null ){
			
			int size = containingTypes.length;
			if( qualifications.length < size )
				return IMPOSSIBLE_MATCH;
				
			for( int i = 0; i < containingTypes.length; i++ ){
				if( !matchesName( containingTypes[i], qualifications[i].toCharArray() ) ){
					return IMPOSSIBLE_MATCH;
				}
			}
		} else if( containingTypes.length > 0 ) {
			return IMPOSSIBLE_MATCH;
		}
		
		//check type
		if( classKind != null && classKind != clsSpec.getClassKind() ){
			return IMPOSSIBLE_MATCH;
		}
		
		return ACCURATE_MATCH;
	}
	
	public char [] getName() {
		return simpleName;
	}
	public char[] [] getContainingTypes () {
		return containingTypes;
	}
	
	private char[] 	  simpleName;
	private char[][]  containingTypes;
	private ClassKind classKind;
	
}
