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
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;
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
	public CSearchPattern(int matchMode, boolean caseSensitive, LimitTo limitTo ) {
		_matchMode = matchMode;
		_caseSensitive = caseSensitive;
		_limitTo = limitTo;
	}

	public CSearchPattern() {
		super();
	}

	public LimitTo getLimitTo(){
		return _limitTo;
	}

	public boolean canAccept(LimitTo limit) {
		return ( limit == getLimitTo() );
	}

	public static CSearchPattern createPattern( String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive ){
		if( patternString == null || patternString.length() == 0 ){
			return null;
		}
		
		CSearchPattern pattern = null;
		if( searchFor == TYPE || searchFor == CLASS || searchFor == STRUCT || searchFor == ENUM || searchFor == UNION || searchFor == CLASS_STRUCT ){
			pattern = createClassPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == METHOD || searchFor == FUNCTION ){
			pattern = createMethodPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == FIELD || searchFor == VAR ){
			pattern = createFieldPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == NAMESPACE ){
			pattern = createNamespacePattern( patternString, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == MACRO ){
			pattern = createMacroPattern( patternString, limitTo, matchMode, caseSensitive );
		}
	
		return pattern;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createMacroPattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo != DECLARATIONS )
			return null;
			
		return new MacroDeclarationPattern( patternString.toCharArray(), matchMode, limitTo, caseSensitive );	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createNamespacePattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo == ALL_OCCURRENCES ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createNamespacePattern( patternString, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createNamespacePattern( patternString, REFERENCES, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		IScanner scanner = ParserFactory.createScanner( new StringReader( patternString ), "TEXT", new ScannerInfo(), ParserMode.QUICK_PARSE, ParserLanguage.CPP, null );
		LinkedList list = scanForNames( scanner, null );
		
		char [] name = (char []) list.removeLast();
		char [][] qualifications = new char [0][];
		
		return new NamespaceDeclarationPattern( name, (char[][]) list.toArray( qualifications ), matchMode, limitTo, caseSensitive );
	}

//	/**
//	 * @param patternString
//	 * @param limitTo
//	 * @param matchMode
//	 * @param caseSensitive
//	 * @return
//	 */
//	private static CSearchPattern createFunctionPattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
//		if( limitTo == ALL_OCCURRENCES ){
//			OrPattern orPattern = new OrPattern();
//			orPattern.addPattern( createFunctionPattern( patternString, DECLARATIONS, matchMode, caseSensitive ) );
//			orPattern.addPattern( createFunctionPattern( patternString, REFERENCES, matchMode, caseSensitive ) );
//			orPattern.addPattern( createFunctionPattern( patternString, DEFINITIONS, matchMode, caseSensitive ) );
//			return orPattern;
//		}
//		
//		int index = patternString.indexOf( '(' );
//		
//		String paramString = ( index == -1 ) ? "" : patternString.substring( index );
//		
//		String nameString = ( index == -1 ) ? patternString : patternString.substring( 0, index );
//				
//		IScanner scanner = ParserFactory.createScanner( new StringReader( paramString ), "TEXT", new ScannerInfo(), ParserMode.QUICK_PARSE, null );
//				
//		LinkedList params = scanForParameters( scanner );
//				
//		char [] name = nameString.toCharArray();
//		char [][] parameters = new char [0][];
//		parameters = (char[][])params.toArray( parameters );
//				
//		return new MethodDeclarationPattern( name, parameters, matchMode, FUNCTION, limitTo, caseSensitive );
//	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createFieldPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo == ALL_OCCURRENCES ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createFieldPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createFieldPattern( patternString, searchFor, REFERENCES, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		IScanner scanner = ParserFactory.createScanner( new StringReader( patternString ), "TEXT", new ScannerInfo(), ParserMode.QUICK_PARSE, ParserLanguage.CPP, null );
		LinkedList list = scanForNames( scanner, null );
		
		char [] name = (char []) list.removeLast();
		char [][] qualifications = new char[0][];
		
		return new FieldDeclarationPattern( name, (char[][]) list.toArray( qualifications ), matchMode, searchFor, limitTo, caseSensitive );
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createMethodPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {

		if( limitTo == ALL_OCCURRENCES ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createMethodPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createMethodPattern( patternString, searchFor, REFERENCES, matchMode, caseSensitive ) );
			orPattern.addPattern( createMethodPattern( patternString, searchFor, DEFINITIONS, matchMode, caseSensitive ) );
			return orPattern;
		}
				
		int index = patternString.indexOf( '(' );
		String paramString = ( index == -1 ) ? "" : patternString.substring( index );
		String nameString = ( index == -1 ) ? patternString : patternString.substring( 0, index );
		
		IScanner scanner = ParserFactory.createScanner( new StringReader( nameString ), "TEXT", new ScannerInfo(), ParserMode.QUICK_PARSE, ParserLanguage.CPP, null );
		
		LinkedList names = scanForNames( scanner, null );
		
		scanner = ParserFactory.createScanner( new StringReader( paramString ), "TEXT", new ScannerInfo(), ParserMode.QUICK_PARSE, ParserLanguage.CPP, null );
		
		LinkedList params = scanForParameters( scanner );
		
		char [] name = (char [])names.removeLast();
		char [][] qualifications = new char[0][];
		qualifications = (char[][])names.toArray( qualifications );
		char [][] parameters = new char [0][];
		parameters = (char[][])params.toArray( parameters );
		
		return new MethodDeclarationPattern( name, qualifications, parameters, matchMode, searchFor, limitTo, caseSensitive );
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createClassPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		
		if( limitTo == ALL_OCCURRENCES ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createClassPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createClassPattern( patternString, searchFor, REFERENCES, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		if( searchFor == CLASS_STRUCT ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createClassPattern( patternString, CLASS, limitTo, matchMode, caseSensitive ) );
			orPattern.addPattern( createClassPattern( patternString, STRUCT, limitTo, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		IScanner scanner = ParserFactory.createScanner( new StringReader( patternString ), "TEXT", new ScannerInfo(), ParserMode.QUICK_PARSE, ParserLanguage.CPP, null );
		
		IToken token = null;
		ASTClassKind kind = null;
		
		try {
			token = scanner.nextToken();
		} catch (EndOfFile e) {
		} catch (ScannerException e) {
		}
		
		if( token != null ){
			if( token.getType() == IToken.t_class ){
				kind = ASTClassKind.CLASS;
			} else if ( token.getType() == IToken.t_struct ){
				kind = ASTClassKind.STRUCT;
			} else if ( token.getType() == IToken.t_union ){
				kind = ASTClassKind.UNION;
			} else if ( token.getType() == IToken.t_enum ){
				kind = ASTClassKind.ENUM;
			}
			if( kind != null ){
				token = null;
			} else {
				if( searchFor == CLASS ){
					kind = ASTClassKind.CLASS;
				} else if( searchFor == STRUCT ) {
					kind = ASTClassKind.STRUCT;
				} else if ( searchFor == ENUM ) {
					kind = ASTClassKind.ENUM;
				} else if ( searchFor == UNION ) {
					kind = ASTClassKind.UNION;
				}		
			}
		}
			
		LinkedList list = scanForNames( scanner, token );
		
		char[] name = (char [])list.removeLast();
		char [][] qualifications = new char[0][];
		
		return new ClassDeclarationPattern( name, (char[][])list.toArray( qualifications ), kind, matchMode, limitTo, caseSensitive );
	}



	/**
	 * @param scanner
	 * @param object
	 * @return
	 */
	private static LinkedList scanForParameters(IScanner scanner) {
		LinkedList list = new LinkedList();
		
		String param = new String("");
		
		boolean lastTokenWasWild = false;
		try{
			IToken token = scanner.nextToken();
			
			tokenConsumption:
			while( true ){
				switch( token.getType() ){
					case IToken.tCOMMA :
						list.addLast( param.toCharArray() );
						param = new String("");
						break;
						
					case IToken.tLPAREN :
						break;
						
					case IToken.tRPAREN :
						list.addLast( param.toCharArray() );
						break tokenConsumption;
						
					case IToken.tSTAR:
					case IToken.tQUESTION:
						lastTokenWasWild = true;
						param += token.getImage();
						break;
						
					default:
						if( !lastTokenWasWild && param.length() > 0 )
							param += " ";
						param += token.getImage();
						break;
				}
				
				token = scanner.nextToken();
			}
		} catch ( EndOfFile e ){
			list.addLast( param.toCharArray() );
		} catch( ScannerException e ){
		}
		
		return list;
	}
		
	static private LinkedList scanForNames( IScanner scanner, IToken unusedToken ){
		LinkedList list = new LinkedList();
		
		String name  = new String("");
		
		try {
			IToken token = ( unusedToken != null ) ? unusedToken : scanner.nextToken();
			
			boolean lastTokenWasWild = false;
			
			while( true ){
				switch( token.getType() ){
					case IToken.tCOLONCOLON :
						list.addLast( name.toCharArray() );
						name = new String("");
						break;
					default:
						if( token.getType() == IToken.tSTAR || 
						    token.getType() == IToken.tQUESTION ||
						    token.getType() == IToken.tCOMPL //Need this for destructors
						    ){
							lastTokenWasWild = true;
						} else if( !lastTokenWasWild && name.length() > 0 ) {
							name += " ";
						}
						
						name += token.getImage();
						break;
				}
				
				token = scanner.nextToken();
			}
		} catch (EndOfFile e) {	
			list.addLast( name.toCharArray() );
		} catch (ScannerException e) {
		}
		
		return list;	
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
	
	protected boolean matchQualifications( char[][] qualifications, char[][] candidate ){
		
		int qualLength = qualifications != null ? qualifications.length : 0;
		int candidateLength = candidate != null ? candidate.length : 0;
		
		if( qualLength == 0 ){
			return true;
		}
		
		int root = ( qualifications[0].length == 0 ) ? 1 : 0;
		
		if( (root == 1 && candidateLength != qualLength - 1 ) ||
			(root == 0 && candidateLength < qualLength ) )
		{
			return false;
		}
		
		for( int i = 1; i <= qualLength - root; i++ ){
			if( !matchesName( qualifications[ qualLength - i ], candidate[ candidateLength - i ] ) ){
				return false;		
			}
		}
		
		return true;
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
		char [] prefix = indexEntryPrefix();
		if( prefix == null ) return;
		
	   IEntryResult[] entries = input.queryEntriesPrefixedBy( prefix );
	   if (entries == null) return;
	
	   /* only select entries which actually match the entire search pattern */
	   for (int i = 0, max = entries.length; i < max; i++){

		   if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

		   /* retrieve and decode entry */	
		   IEntryResult entry = entries[i];
		   resetIndexInfo();
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
    * Called to reset any variables used in the decoding of index entries, 
    * this ensures that the matchIndexEntry is not polluted by index info
    * from previous entries.
    */
   protected abstract void resetIndexInfo();
   
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
	protected LimitTo   _limitTo;
}
