/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Aug 6, 2003
 */
package org.eclipse.cdt.core.search;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OrPattern extends CSearchPattern {

	public OrPattern(){
		super();
		patterns = new LinkedList();
	}

	/**
	 * @param pattern
	 */
	public void addPattern( ICSearchPattern pattern) {
		if( pattern != null )
			patterns.add( pattern );
	}

	public int matchLevel(ISourceElementCallbackDelegate node, LimitTo limit ) {
		Iterator iter = patterns.iterator();
		int size = patterns.size();
		
		int result = IMPOSSIBLE_MATCH;
		
		for( int i = 0; i < size; i++ ){
			ICSearchPattern pattern = (ICSearchPattern) iter.next();
			result = pattern.matchLevel( node, limit );
			if( result != IMPOSSIBLE_MATCH )
				break;
		}
		
		return result;
	}

	public boolean canAccept(LimitTo limit) {
		if( limit == ALL_OCCURRENCES ){
			return true;
		}
		
		Iterator iter = patterns.iterator();
		int size = patterns.size();
		
		for( int i = 0; i < size; i++ ){
			ICSearchPattern pattern = (ICSearchPattern) iter.next();
			if( pattern.canAccept( limit ) )
				return true;
		}
		
		return false;
	}
	
	public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, ICSearchScope scope) throws IOException {
	   Iterator iter = patterns.iterator();
	   int size = patterns.size();
		
	   for( int i = 0; i < size; i++ ){
		   CSearchPattern pattern = (CSearchPattern) iter.next();
		   pattern.findIndexMatches( input, requestor, detailLevel, progressMonitor, scope );
	   }
   }

   public void feedIndexRequestor(	IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, ICSearchScope scope )
   throws IOException {
	   //never called for OrPattern
   }
	   
	protected void resetIndexInfo() {
		//never called for OrPattern
	}

	protected void decodeIndexEntry(IEntryResult entryResult) {
		//never called for OrPattern
	}

	public char[] indexEntryPrefix() {
		//never called for OrPattern
		return null;
	}

	protected boolean matchIndexEntry() {
		//never called for OrPattern
		return false;
	}

	private LinkedList patterns;

}
