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
 * Created on May 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.core.search.matching;


import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FriendPattern extends ClassDeclarationPattern {

	/**
	 * @param name
	 * @param containers
	 * @param searchFor
	 * @param limit
	 * @param mode
	 * @param caseSensitive
	 */
	public FriendPattern(char[] name, char[][] containers, SearchFor searchFor, LimitTo limit, int mode, boolean caseSensitive) {
		super(name, containers, searchFor, limit, mode, caseSensitive, false);
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
	    if( decodedType != FRIEND_SUFFIX ){
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
		Iterator i = tempNode.getFriends();
		
		boolean matchFlag=false;

		while (i.hasNext()){
			Object friend =  i.next();
			char [][] qualName = null;
			if (friend instanceof IASTClassSpecifier)
			{
				IASTClassSpecifier classSpec = (IASTClassSpecifier) friend;
				qualName = classSpec.getFullyQualifiedNameCharArrays();
	
				//check name, if simpleName == null, its treated the same as "*"	
				if( simpleName != null && !matchesName( simpleName, classSpec.getNameCharArray() ) ){
					continue;
				}
			}	
			else if (friend instanceof IASTElaboratedTypeSpecifier ){
			    IASTElaboratedTypeSpecifier elabType = (IASTElaboratedTypeSpecifier) friend;
			    qualName = elabType.getFullyQualifiedNameCharArrays();
			    
				//check name, if simpleName == null, its treated the same as "*"	
				if( simpleName != null && !matchesName( simpleName, elabType.getNameCharArray() ) ){
					continue;
				}
			}
			
			if (qualName!= null){
				//check containing scopes
				if( !matchQualifications( qualifications, qualName, true ) ){
					continue;
				}
				
				matchFlag = true;
				break;
			}
		}
		

		
		if (matchFlag)
			return ACCURATE_MATCH;
		
		return IMPOSSIBLE_MATCH;
	}
	
	
}
