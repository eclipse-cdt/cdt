/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import org.eclipse.cdt.core.parser.EndOfFile;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.search.CharOperation;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * @author aniefer
 */
public abstract class CSearchPattern implements ICSearchConstants, ICSearchPattern, IIndexConstants {
	
	public static final int IMPOSSIBLE_MATCH = 0;
	public static final int POSSIBLE_MATCH   = 1;
	public static final int ACCURATE_MATCH   = 2;
	public static final int INACCURATE_MATCH = 3;

	/**
	 * @param matchMode
	 * @param caseSensitive
	 */
	public CSearchPattern(int matchMode, boolean caseSensitive) {
		_matchMode = matchMode;
		_caseSensitive = caseSensitive;
	}

	public CSearchPattern() {
		super();
	}

	public static CSearchPattern createPattern( String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive ){
		if( patternString == null || patternString.length() == 0 ){
			return null;
		}
		
		CSearchPattern pattern = null;
		if( searchFor == TYPE || searchFor == CLASS || searchFor == STRUCT || searchFor == ENUM || searchFor == UNION ){
			pattern = createClassPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == MEMBER ){
			//	pattern = createMethodPattern( patternString, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == CONSTRUCTOR ){
			pattern = createConstructorPattern( patternString, limitTo, matchMode, caseSensitive );
		}
			//case ICSearchConstants.FIELD:
			//	pattern = createFieldPattern( patternString, limitTo, matchMode, caseSensitive );
			//	break;
		return pattern;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createFieldPattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createMethodPattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createConstructorPattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createClassPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		IScanner scanner = ParserFactory.createScanner( new StringReader( patternString ), "TEXT", new ScannerInfo(), ParserMode.QUICK_PARSE );
		
		LinkedList list  = new LinkedList();
		IToken 	   token = null;
		String 	   name  = new String("");
		
		try {
			while( true ){
				token = scanner.nextToken();
				
				switch( token.getType() ){
					case IToken.tCOLONCOLON :
						list.addLast( name.toCharArray() );
						name = new String("");
						break;
					default:
						name += token.getImage();
						break;
				}
			}
		} catch (EndOfFile e) {	
		} catch (ScannerException e) {
		}
		
		ASTClassKind kind = null;
		if( searchFor == CLASS ){
			kind = ASTClassKind.CLASS;
		} else if( searchFor == STRUCT ) {
			kind = ASTClassKind.STRUCT;
		} else if ( searchFor == ENUM ) {
			kind = ASTClassKind.ENUM;
		} else if ( searchFor == UNION ) {
			kind = ASTClassKind.UNION;
		}

		char [][] qualifications = new char[0][];
		return new ClassDeclarationPattern( name.toCharArray(), (char[][])list.toArray( qualifications ), kind, matchMode, caseSensitive );
	}
	
	protected boolean matchesName( char[] pattern, char[] name ){
		if( pattern == null ){
			return true;  //treat null as "*"
		}
		
		if( name != null ){
			switch( _matchMode ){
				case EXACT_MATCH:
					return CharOperation.equals( pattern, name, _caseSensitive );
				case PREFIX_MATCH:
					return CharOperation.prefixEquals( pattern, name, _caseSensitive );
				case PATTERN_MATCH:
					if( !_caseSensitive ){
						pattern = CharOperation.toLowerCase( pattern );
					}
					
					return CharOperation.match( pattern, name, _caseSensitive );
			}
		}
		return false;
	}
	
    /**
	* Query a given index for matching entries. 
	*/
   public void findIndexMatches(IIndex index, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, ICSearchScope scope) throws IOException {

	   if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	   IndexInput input = new BlocksIndexInput(index.getIndexFile());
	   try {
		   input.open();
		   findIndexMatches(input, requestor, detailLevel, progressMonitor,scope);
	   } finally {
		   input.close();
	   }
   }
   /**
	* Query a given index for matching entries. 
	*/
   public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, ICSearchScope scope) throws IOException {

	   if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	
	   /* narrow down a set of entries using prefix criteria */
	   IEntryResult[] entries = input.queryEntriesPrefixedBy(indexEntryPrefix());
	   if (entries == null) return;
	
	   /* only select entries which actually match the entire search pattern */
	   for (int i = 0, max = entries.length; i < max; i++){

		   if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

		   /* retrieve and decode entry */	
		   IEntryResult entry = entries[i];
		   decodeIndexEntry(entry);
		   if (matchIndexEntry()){
			   feedIndexRequestor(requestor, detailLevel, entry.getFileReferences(), input, scope);
		   }
	   }
   }

   /**
   * Feed the requestor according to the current search pattern
   */
   public abstract void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, ICSearchScope scope)  throws IOException ;
   /**
   * Decodes the index entry
   */
   protected abstract void decodeIndexEntry(IEntryResult entryResult);
   /**
	* Answers the suitable prefix that should be used in order
	* to query indexes for the corresponding item.
	* The more accurate the prefix and the less false hits will have
	* to be eliminated later on.
	*/
   public abstract char[] indexEntryPrefix();
   /**
	* Checks whether an entry matches the current search pattern
	*/
   protected abstract boolean matchIndexEntry();
   
	protected int 		_matchMode;
	protected boolean 	_caseSensitive;
}
