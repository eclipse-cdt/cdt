/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Apr 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DerivedTypesPattern extends ClassDeclarationPattern {

	/**
	 * @param name
	 * @param containers
	 * @param searchFor
	 * @param limit
	 * @param mode
	 * @param caseSensitive
	 */
	public DerivedTypesPattern(char[] name, char[][] containers, SearchFor searchFor, LimitTo limit, int mode, boolean caseSensitive) {
		super(name, containers, searchFor, limit, mode, caseSensitive);
	}
	
	public char[] indexEntryPrefix() {
		return AbstractIndexer.bestTypePrefix(
				searchFor,
				getLimitTo(),
				simpleName,
				qualifications,
				_matchMode,
				_caseSensitive
		);
	}
	
	protected boolean matchIndexEntry() {
	    if( decodedType != DERIVED_SUFFIX ){
			return false;
		}
	    
		return super.matchIndexEntry();
	}
	
	public int matchLevel( ISourceElementCallbackDelegate node, LimitTo limit ){
		
		if (!( node instanceof IASTClassSpecifier )) {
			return IMPOSSIBLE_MATCH;
		}
		
		if( ! canAccept( limit ) )
			return IMPOSSIBLE_MATCH;
		
		IASTClassSpecifier tempNode = (IASTClassSpecifier) node;
		Iterator i = tempNode.getBaseClauses();
		
		boolean matchFlag=false;
		
		while (i.hasNext()){
			IASTBaseSpecifier baseSpec = (IASTBaseSpecifier) i.next();
			IASTTypeSpecifier typeSpec = null;
			try {
				typeSpec = baseSpec.getParentClassSpecifier();
			} catch (ASTNotImplementedException e) {}
			if (typeSpec instanceof IASTClassSpecifier){
				IASTClassSpecifier baseClassSpec = (IASTClassSpecifier) typeSpec;
				//check name, if simpleName == null, its treated the same as "*"	
				if( simpleName != null && !matchesName( simpleName, baseClassSpec.getNameCharArray() ) ){
					continue;
				}
				char [][] qualName = baseClassSpec.getFullyQualifiedNameCharArrays();
				//check containing scopes
				if( !matchQualifications( qualifications, qualName, true ) ){
					continue;
				}
				
				matchFlag = true;
				break;
			}
		}
		
	
/*		//check type
		if( classKind != null ){
			if( node instanceof IASTClassSpecifier ){
				IASTClassSpecifier clsSpec = (IASTClassSpecifier) node;
				return ( classKind == clsSpec.getClassKind() ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			} 
		}*/
		
		if (matchFlag)
			return ACCURATE_MATCH;
		
		return IMPOSSIBLE_MATCH;
	}
}
