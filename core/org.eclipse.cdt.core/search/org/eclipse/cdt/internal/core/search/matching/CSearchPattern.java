/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IQuickParseCallback;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
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
	
	protected static class Requestor extends NullSourceElementRequestor
	{
	    public int badCharacterOffset = -1;
		public Requestor( ParserMode mode )
		{
			super( mode );
		}
		
		public boolean acceptProblem( IProblem problem )
		{
			if( problem.getID() == IProblem.SCANNER_BAD_CHARACTER ){
			    badCharacterOffset = problem.getSourceStart();
			    return false;
			}
			return super.acceptProblem( problem );
		}
	}
	
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
		if( searchFor == TYPE || searchFor == CLASS || searchFor == STRUCT || 
			searchFor == FWD_CLASS || searchFor == FWD_STRUCT || searchFor == FWD_UNION ||
			searchFor == ENUM || searchFor == UNION || searchFor == CLASS_STRUCT  ||
			searchFor == TYPEDEF )
		{
			pattern = createClassPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == DERIVED){
			pattern = createDerivedPattern(patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == FRIEND){
			pattern = createFriendPattern(patternString, searchFor, limitTo, matchMode, caseSensitive );
		} 
		else if ( searchFor == METHOD || searchFor == FUNCTION ){
			pattern = createMethodPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == FIELD || searchFor == VAR || searchFor == ENUMTOR){
			pattern = createFieldPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == NAMESPACE ){
			pattern = createNamespacePattern( patternString, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == MACRO ){
			pattern = createMacroPattern( patternString, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == INCLUDE){
			pattern = createIncludePattern( patternString, limitTo, matchMode, caseSensitive);
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
	private static CSearchPattern createIncludePattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo != REFERENCES )
			return null;
			
		return new IncludePattern ( patternString.toCharArray(), matchMode, limitTo, caseSensitive );	
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createMacroPattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo != DECLARATIONS && limitTo != ALL_OCCURRENCES )
			return null;
			
		return new MacroDeclarationPattern( patternString.toCharArray(), matchMode, DECLARATIONS, caseSensitive );	
	}

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
			orPattern.addPattern( createNamespacePattern( patternString, DEFINITIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createNamespacePattern( patternString, REFERENCES, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		char [] patternArray = patternString.toCharArray();
		
		IScanner scanner = null;
		Requestor callback = new Requestor( ParserMode.COMPLETE_PARSE );
		try {
			scanner =
				ParserFactory.createScanner(
					new CodeReader(patternArray),
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, 
					nullLog, null);
		} catch (ParserFactoryError e) {

		}
		LinkedList list = scanForNames( scanner, callback, null, patternArray );
		
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
			orPattern.addPattern( createFieldPattern( patternString, searchFor, DEFINITIONS, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		char [] patternArray = patternString.toCharArray();
		Requestor callback = new Requestor( ParserMode.COMPLETE_PARSE );
		IScanner scanner=null;
		try {
			scanner =
				ParserFactory.createScanner(
					new CodeReader(patternArray),
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, nullLog, null);
		} catch (ParserFactoryError e) {

		}
		LinkedList list = scanForNames( scanner, callback, null, patternArray );
		
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
		String paramString = ( index == -1 ) ? "" : patternString.substring( index ); //$NON-NLS-1$
		String nameString = ( index == -1 ) ? patternString : patternString.substring( 0, index );
		char [] nameArray = nameString.toCharArray();
		IScanner scanner=null;
		Requestor callback = new Requestor( ParserMode.COMPLETE_PARSE );
		try {
			scanner =
				ParserFactory.createScanner(
					new CodeReader(nameArray),
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, nullLog, null);
		} catch (ParserFactoryError e) {
		}
		
		LinkedList names = scanForNames( scanner, callback, null, nameArray );

		LinkedList params = scanForParameters( paramString );
		
		char [] name = (char [])names.removeLast();
		char [][] qualifications = new char[0][];
		qualifications = (char[][])names.toArray( qualifications );
		char [][] parameters = new char [0][];
		parameters = (char[][])params.toArray( parameters );
		
		return new MethodDeclarationPattern( name, qualifications, parameters, matchMode, searchFor, limitTo, caseSensitive );
	}

	private static final IParserLogService nullLog = new NullLogService();
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
		
		boolean isForward = false;
		if (searchFor == FWD_CLASS || searchFor == FWD_STRUCT || searchFor == FWD_UNION){
			isForward = true;
		}
		
		char [] patternArray = patternString.toCharArray();
		
		IScanner scanner =null;
		Requestor callback = new Requestor( ParserMode.COMPLETE_PARSE );
		try {
			scanner =
				ParserFactory.createScanner(
					new CodeReader(patternArray),
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, nullLog, null );
		} catch (ParserFactoryError e1) {
		}
		
		IToken token = null;
		
		try {
			token = scanner.nextToken();
		} catch (EndOfFileException e) {
		}
		
		if( token != null ){
			boolean nullifyToken = true;
			if( token.getType() == IToken.t_class ){
				searchFor = CLASS;
			} else if ( token.getType() == IToken.t_struct ){
				searchFor = STRUCT;
			} else if ( token.getType() == IToken.t_union ){
				searchFor = UNION;
			} else if ( token.getType() == IToken.t_enum ){
				searchFor = ENUM;
			} else if ( token.getType() == IToken.t_typedef ){
				searchFor = TYPEDEF;
			} else {
				nullifyToken = false;
			}
			if( nullifyToken ){
				patternArray = CharOperation.subarray( patternArray, token.getLength() + 1, -1 );
				token = null;
			}
		}
			
		LinkedList list = scanForNames( scanner, callback, token, patternArray );
		
		char[] name = (char [])list.removeLast();
		char [][] qualifications = new char[0][];
		
		return new ClassDeclarationPattern( name, (char[][])list.toArray( qualifications ), searchFor, limitTo, matchMode, caseSensitive, isForward );
	}


	private static CSearchPattern createDerivedPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		char [] patternArray = patternString.toCharArray();
		
		IScanner scanner =null;
		Requestor callback = new Requestor( ParserMode.COMPLETE_PARSE );
		try {
			scanner =
				ParserFactory.createScanner(
					new CodeReader(patternArray),
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, nullLog, null);
		} catch (ParserFactoryError e1) {
		}
		
		searchFor = DERIVED;
		
		LinkedList list = scanForNames( scanner, callback, null, patternArray );
		
		char[] name = (char [])list.removeLast();
		char [][] qualifications = new char[0][];
		
		return new DerivedTypesPattern( name, (char[][])list.toArray( qualifications ), searchFor, limitTo, matchMode, caseSensitive );
	}
	
	private static CSearchPattern createFriendPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		
		char [] patternArray = patternString.toCharArray();
		IScanner scanner =null;
		Requestor callback = new Requestor( ParserMode.COMPLETE_PARSE );
		try {
			scanner =
				ParserFactory.createScanner(
					new CodeReader(patternArray),
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, nullLog, null);
		} catch (ParserFactoryError e1) {
		}
		
		searchFor = FRIEND;
		
		LinkedList list = scanForNames( scanner, callback, null, patternArray );
		
		char[] name = (char [])list.removeLast();
		char [][] qualifications = new char[0][];
		
		return new FriendPattern( name, (char[][])list.toArray( qualifications ), searchFor, limitTo, matchMode, caseSensitive );
	}
	/**
	 * @param scanner
	 * @param object
	 * @return
	 */
	private static LinkedList scanForParameters( String paramString ) {
		LinkedList list = new LinkedList();
		
		if( paramString == null || paramString.equals("") ) //$NON-NLS-1$
			return list;
		
		String functionString = "void f " + paramString + ";"; //$NON-NLS-1$ //$NON-NLS-2$
				
		IScanner scanner=null;
		IQuickParseCallback callback = ParserFactory.createQuickParseCallback();
		try {
			scanner =
				ParserFactory.createScanner(
					new CodeReader(functionString.toCharArray()),
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback,new NullLogService(), null);
		} catch (ParserFactoryError e1) {
		}
					   
		IParser parser=null;
		try {
			parser =
				ParserFactory.createParser(
					scanner,
					callback,
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP, ParserUtil.getParserLogService());
		} catch (ParserFactoryError e2) {
		} 

		if( parser.parse() ){
			IASTCompilationUnit compUnit = callback.getCompilationUnit();
			Iterator declarations = null;
			try {
				declarations = compUnit.getDeclarations();
			} catch (ASTNotImplementedException e) {
			}
			
			if( declarations == null || ! declarations.hasNext() )
				return null;
			
			IASTDeclaration decl = (IASTDeclaration) declarations.next();
			if( !(decl instanceof IASTFunction) ){
				//if the user puts something not so good in the brackets, we might not get a function back
				return list;
			}
			
			IASTFunction function = (IASTFunction) decl;
			
			String [] paramTypes = ASTUtil.getFunctionParameterTypes(function);
			if( paramTypes.length == 0 )
			{
				//This means that no params have been added (i.e. empty brackets - void case)
				list.add ("void".toCharArray() ); //$NON-NLS-1$ 
			} else {
				for( int i = 0; i < paramTypes.length; i++ ){
					list.add( paramTypes[i].toCharArray() );
				}
			}
		}
		
		return list;
	}
		
	static private LinkedList scanForNames( IScanner scanner, Requestor callback, IToken unusedToken, char[] pattern ){
		LinkedList list = new LinkedList();
		
		String name  = new String(""); //$NON-NLS-1$
		int idx = 0;
		
		try {
			IToken token = ( unusedToken != null ) ? unusedToken : scanner.nextToken();
			IToken prev = null;

			boolean encounteredWild = false;
			boolean lastTokenWasOperator = false;
			
			while( true ){
				switch( token.getType() ){
					case IToken.tCOLONCOLON :
						list.addLast( name.toCharArray() );
						name = new String(""); //$NON-NLS-1$
						lastTokenWasOperator = false;
						idx += token.getLength();
						while( idx < pattern.length && CharOperation.isWhitespace( pattern[idx] ) ){ idx++; }
						break;
					
					case IToken.t_operator :
						name += token.getImage();
						name += ' ';
						lastTokenWasOperator = true;
						idx += token.getLength();
						while( idx < pattern.length && CharOperation.isWhitespace( pattern[idx] ) ){ idx++; }
						break;
					
					default:
						if( token.getType() == IToken.tSTAR || 
						    token.getType() == IToken.tQUESTION 
						    )
						{
							if( idx > 0 && idx < pattern.length && CharOperation.isWhitespace( pattern[ idx - 1 ] ) && !lastTokenWasOperator )
								name += ' ';
							encounteredWild = true;
						} else if( !encounteredWild && !lastTokenWasOperator && name.length() > 0 &&
									prev.getType() != IToken.tIDENTIFIER &&
									prev.getType() != IToken.tLT &&
									prev.getType() != IToken.tCOMPL &&
									prev.getType() != IToken.tARROW &&
									prev.getType() != IToken.tLBRACKET && 
									token.getType() != IToken.tRBRACKET &&
									token.getType()!= IToken.tGT
								 ){
							name += ' ';
						} else {
							encounteredWild = false;
						}
						
						name += token.getImage();
						
						if( encounteredWild && idx < pattern.length - 1 && CharOperation.isWhitespace( pattern[ idx + 1 ] ) )
						{
							name += ' ';
						}
						idx += token.getLength();
						while( idx < pattern.length && CharOperation.isWhitespace( pattern[idx] ) ){ idx++; }
						
						lastTokenWasOperator = false;
						break;
				}
				prev = token;
				
				token = null;
				while( token == null ){
				    token = scanner.nextToken();
				    if( callback.badCharacterOffset != -1 && token.getOffset() > callback.badCharacterOffset ){
				        //TODO : This may not be \\, it could be another bad character
						if( !encounteredWild && !lastTokenWasOperator && prev.getType() != IToken.tARROW ) name += " "; //$NON-NLS-1$
						name += "\\"; //$NON-NLS-1$
						idx++;
						encounteredWild = true;
						lastTokenWasOperator = false;
						prev = null;
						callback.badCharacterOffset = -1;
				    }
				}
			}
		} catch (EndOfFileException e) {	
			list.addLast( name.toCharArray() );
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
	    return matchQualifications( qualifications, candidate, false );
	}
	protected boolean matchQualifications( char[][] qualifications, char[][] candidate, boolean skipLastName ){
		
		int qualLength = qualifications != null ? qualifications.length : 0;
		int candidateLength = candidate != null ? candidate.length - ( skipLastName ? 1 : 0 ) : 0;
		
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
