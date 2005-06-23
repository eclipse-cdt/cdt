/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatchLocatable;
import org.eclipse.cdt.core.search.LineLocatable;
import org.eclipse.cdt.core.search.OffsetLocatable;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * @author aniefer
 */
public abstract class CSearchPattern implements ICSearchConstants, ICSearchPattern {
	
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
			orPattern.addPattern( createNamespacePattern( patternString, DEFINITIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createNamespacePattern( patternString, REFERENCES, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		if( limitTo == DECLARATIONS_DEFINITIONS ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createNamespacePattern( patternString, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createNamespacePattern( patternString, DEFINITIONS, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		char[][] names = scanForNames( patternString );
		
		char[] name = names[names.length - 1];
		char[][] qualifications = new char [names.length - 1][];
        System.arraycopy(names, 0, qualifications, 0, qualifications.length);
		
		return new NamespaceDeclarationPattern( name, qualifications , matchMode, limitTo, caseSensitive );
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
		
		if( limitTo == DECLARATIONS_DEFINITIONS ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createFieldPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createFieldPattern( patternString, searchFor, DEFINITIONS, matchMode, caseSensitive ) );
			return orPattern;
		}

		char[][] names = scanForNames( patternString );
		
		char[] name = names[names.length - 1];
		char[][] qualifications = new char[names.length - 1][];
        System.arraycopy(names, 0, qualifications, 0, qualifications.length);
		
		return new FieldDeclarationPattern( name, qualifications, matchMode, searchFor, limitTo, caseSensitive );
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
			
		if( limitTo == DECLARATIONS_DEFINITIONS ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createMethodPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createMethodPattern( patternString, searchFor, DEFINITIONS, matchMode, caseSensitive ) );
			return orPattern;
		}
		int index = patternString.indexOf( '(' );
		String paramString = ( index == -1 ) ? "" : patternString.substring( index ); //$NON-NLS-1$
		String nameString = ( index == -1 ) ? patternString : patternString.substring( 0, index );
		
		char[][] names = scanForNames( nameString );
        
        char[][] parameters = scanForParameters( paramString );
		
        // TODO implement
		//LinkedList returnType = scanForReturnType();
		
        char[] name = names[names.length - 1];
        char[][] qualifications = new char[names.length - 1][];
        System.arraycopy(names, 0, qualifications, 0, qualifications.length);
		char[] returnType = new char[0];
		
		return new MethodDeclarationPattern( name, qualifications, parameters, returnType, matchMode, searchFor, limitTo, caseSensitive );
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
			orPattern.addPattern( createClassPattern( patternString, searchFor, DEFINITIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createClassPattern( patternString, searchFor, REFERENCES, matchMode, caseSensitive ) );
			return orPattern;
		} 
		
		if ( limitTo == DECLARATIONS_DEFINITIONS ) {
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createClassPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createClassPattern( patternString, searchFor, DEFINITIONS, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		if( searchFor == CLASS_STRUCT ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createClassPattern( patternString, CLASS, limitTo, matchMode, caseSensitive ) );
			orPattern.addPattern( createClassPattern( patternString, STRUCT, limitTo, matchMode, caseSensitive ) );
			return orPattern;
		}

        String[] tokens = patternString.split("\\s+"); //$NON-NLS-1$
        if (tokens.length > 0) {
            boolean removeFirst = true;
            if (tokens[0].equals("class")) { //$NON-NLS-1$
                searchFor = CLASS;
            }
            else if (tokens[0].equals("struct")) { //$NON-NLS-1$
                searchFor = STRUCT;
            }
            else if (tokens[0].equals("union")) { //$NON-NLS-1$
                searchFor = UNION;
            }
            else if (tokens[0].equals("enum")) { //$NON-NLS-1$
                searchFor = ENUM;
            }
            else if (tokens[0].equals("typedef")) { //$NON-NLS-1$
                searchFor = TYPEDEF;
            }
            else {
                removeFirst = false;
            }
            if (removeFirst) {
                patternString = patternString.substring(tokens[0].length()).trim(); 
            }
        }
        char[][] names = scanForNames( patternString ); // return type as first element of the array
        
        char[] name = names[names.length - 1];
        char[][] qualifications = new char[names.length - 1][];
        System.arraycopy(names, 0, qualifications, 0, qualifications.length);
        
		return new ClassDeclarationPattern( name, qualifications, searchFor, limitTo, matchMode, caseSensitive);
	}

	private static CSearchPattern createDerivedPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		searchFor = DERIVED;
		
        char[][] names = scanForNames( patternString );
        
        char[] name = names[names.length - 1];
        char[][] qualifications = new char[names.length - 1][];
        System.arraycopy(names, 0, qualifications, 0, qualifications.length);
        
		return new DerivedTypesPattern( name, qualifications, searchFor, limitTo, matchMode, caseSensitive );
	}
	
	private static CSearchPattern createFriendPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		searchFor = FRIEND;
		
        char[][] names = scanForNames( patternString );
        
        char[] name = names[names.length - 1];
        char[][] qualifications = new char[names.length - 1][];
        System.arraycopy(names, 0, qualifications, 0, qualifications.length);
        
		return new FriendPattern( name, qualifications, searchFor, limitTo, matchMode, caseSensitive );
	}

    /**
     * @param nameString
     * @return
     */
    private static char[][] scanForNames(String nameString) {
        final List nameList = new ArrayList();
        
        if (nameString != null && nameString.length() > 0) {
        
            Requestor callback = new Requestor( ParserMode.COMPLETE_PARSE );
            IScanner scanner = createScanner(nameString, callback);
            if (scanner != null) {
    
                String name  = new String(""); //$NON-NLS-1$
                char[] pattern = nameString.toCharArray();
                int idx = 0;
                
                try {
                    IToken token = scanner.nextToken();
                    IToken prev = null;
        
                    boolean encounteredWild = false;
                    boolean lastTokenWasOperator = false;
                    
                    while( true ){
                        switch( token.getType() ){
                            case IToken.tCOLONCOLON :
                                nameList.add( name.toCharArray() );
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
                                        token.getType() == IToken.tQUESTION ) {
                                    if( idx > 0 && idx < pattern.length && CharOperation.isWhitespace( pattern[ idx - 1 ] ) && !lastTokenWasOperator )
                                        name += ' ';
                                    encounteredWild = true;
                                } 
                                else if( !encounteredWild && !lastTokenWasOperator && name.length() > 0 &&
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
                    nameList.add( name.toCharArray() );
                }
            }
        }        
        return (char[][]) nameList.toArray(new char [nameList.size()] []);
    }


    /**
	 * @param scanner
	 * @param object
	 * @return
	 */
	public static char[][] scanForParameters( String paramString ) {
	    char[][] rv = new char[0][];
        
		if( paramString == null || paramString.equals("") ) //$NON-NLS-1$
			return rv;
		
		String functionString = "void f " + paramString + ";"; //$NON-NLS-1$ //$NON-NLS-2$
				
        try {
            IASTTranslationUnit tu = parse(functionString);
            if (tu != null) {
                IASTDeclaration[] decls = tu.getDeclarations();
                for (int i = 0; i < decls.length; i++) {
                    IASTDeclaration decl = decls[i];
                    if (decl instanceof IASTSimpleDeclaration) {
                        IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) decl;
                        IASTDeclarator[] declarators = simpleDecl.getDeclarators();
                        for (int j = 0; j < declarators.length; j++) {
                            String[] parameters = ASTSignatureUtil.getParameterSignatureArray(declarators[j]);
                            rv = new char[parameters.length][];
                            for (int k = 0; k < parameters.length; k++) {
                                rv[k] = parameters[k].toCharArray();
                            }
                            // take first set of parameters only
                            break;   
                        }
                    }
                    // take first declaration only
                    break;
                }
            }
        }
        catch (ParserException e) {
        }
        
		return rv;
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
			   feedIndexRequestor(requestor, detailLevel, entry.getFileReferences(), entry.getOffsets(), entry.getOffsetLengths(), input, scope);
		   }
	   }
   }

   /**
    * Decodes the passed in offset and returns an IMatchLocatable object of the appropriate type
    * (either IOffsetLocatable or ILineLocatable)
    */
   public static IMatchLocatable getMatchLocatable(int offset, int offsetLength){
	   // pull off the first digit for the offset type
        int encodedVal = offset;
        int offsetType = encodedVal;
        int m = 1;
	    while (offsetType >= 10) {
	      offsetType = offsetType / 10;
	      m *= 10;
	    }
	    
	    int startOffset = encodedVal - offsetType * m;
	    int endOffset = startOffset + offsetLength;
	    
	    IMatchLocatable locatable = null;
	    
	    if (offsetType==IIndex.LINE){
	    	locatable = new LineLocatable(startOffset,0);
	    }else if (offsetType==IIndex.OFFSET){
			locatable = new OffsetLocatable(startOffset, endOffset);
		}
	    
	    return locatable;
   }
   
   /**
   * Feed the requestor according to the current search pattern
   */
   public abstract void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, int[][] offsets, int[][] offsetLengths, IndexInput input, ICSearchScope scope)  throws IOException ;
   
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
    

    protected static IASTTranslationUnit parse( String code ) throws ParserException {
        return parse(code, ParserLanguage.CPP);
    }
    
    /**
     * @param string
     * @param c
     * @return
     * @throws ParserException
     */
    protected static IASTTranslationUnit parse( String code, ParserLanguage lang ) throws ParserException {
        IParserLogService NULL_LOG = new NullLogService();
        CodeReader codeReader = new CodeReader(code .toCharArray());
        ScannerInfo scannerInfo = new ScannerInfo();
        IScannerExtensionConfiguration configuration = null;
        if( lang == ParserLanguage.C )
            configuration = new GCCScannerExtensionConfiguration();
        else
            configuration = new GPPScannerExtensionConfiguration();
        IScanner scanner = new DOMScanner( codeReader, scannerInfo, ParserMode.COMPLETE_PARSE, lang, NULL_LOG, configuration, FileCodeReaderFactory.getInstance() );
        
        ISourceCodeParser parser2 = null;
        if( lang == ParserLanguage.CPP )
        {
            ICPPParserExtensionConfiguration config = new ANSICPPParserExtensionConfiguration();
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config );
        }
        else {
            ICParserExtensionConfiguration config = new ANSICParserExtensionConfiguration();
            parser2 = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config );
        }
        
        IASTTranslationUnit tu = parser2.parse();

        if( parser2.encounteredError())
            throw new ParserException( "FAILURE"); //$NON-NLS-1$
         
        return tu;
    }

    protected static IScanner createScanner( String code, ISourceElementRequestor callback ) {
        return createScanner(code, callback, ParserLanguage.CPP);
    }

    /**
     * @param code
     * @param callback 
     * @param lang
     * @return
     */
    protected static IScanner createScanner( String code, ISourceElementRequestor callback, ParserLanguage lang ) {
        IScanner scanner = null;
        try {
            scanner = ParserFactory.createScanner(
                    new CodeReader(code.toCharArray()),
                    new ScannerInfo(),
                    ParserMode.QUICK_PARSE,
                    lang, callback, nullLog, null);
        } 
        catch (ParserFactoryError e) {
        }
        return scanner;
    }

}
