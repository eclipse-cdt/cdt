/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionParseException;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * @author jcamelon
 */
public class GCCScannerExtension implements IScannerExtension {
	


	protected static final String POUND_IDENT = "#ident"; //$NON-NLS-1$
	protected static final String POUND_WARNING = "#warning"; //$NON-NLS-1$
	protected static final String POUND_INCLUDE_NEXT = "#include_next"; //$NON-NLS-1$
	
	private static final String __CONST__ = "__const__"; //$NON-NLS-1$
	private static final String __CONST = "__const"; //$NON-NLS-1$
	private static final String __INLINE__ = "__inline__"; //$NON-NLS-1$
	private static final String __VOLATILE__ = "__volatile__"; //$NON-NLS-1$
	private static final String __SIGNED__ = "__signed__"; //$NON-NLS-1$
	private static final String __RESTRICT = "__restrict"; //$NON-NLS-1$
	private static final String __RESTRICT__ = "__restrict__"; //$NON-NLS-1$
	private static final String __ASM__ = "__asm__"; //$NON-NLS-1$
	private static final String __TYPEOF__ = "__typeof__"; //$NON-NLS-1$
	
	
	private static final String __ATTRIBUTE__ = "__attribute__";  //$NON-NLS-1$
	private static final String __DECLSPEC = "__declspec"; //$NON-NLS-1$
	private static final List EMPTY_LIST = new ArrayList();

	private static final List simpleIdentifiersDeclSpec;
	private static final List simpleIdentifiersAttribute;
	
	private static final String __EXTENSION__ = "__extension__"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	static
	{
		simpleIdentifiersDeclSpec = new ArrayList( 1 );
		simpleIdentifiersDeclSpec.add( "x" ); //$NON-NLS-1$
		
		simpleIdentifiersAttribute = new ArrayList( 1 );
		simpleIdentifiersAttribute.add( "xyz"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerExtension#initializeMacroValue(java.lang.String)
	 */
	public String initializeMacroValue(IScannerData scannerData, String original) {
		if( original == null || original.trim().equals( "") ) //$NON-NLS-1$
			return "1"; //$NON-NLS-1$
		return original;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerExtension#setupBuiltInMacros()
	 */
	public void setupBuiltInMacros(IScannerData scannerData) {
				
		if( scannerData.getLanguage() == ParserLanguage.CPP )
			if( scannerData.getScanner().getDefinition( IScanner.__CPLUSPLUS ) == null )
				scannerData.getScanner().addDefinition( IScanner.__CPLUSPLUS, new ObjectMacroDescriptor( IScanner.__CPLUSPLUS, "1")); //$NON-NLS-1$
		
		if( scannerData.getScanner().getDefinition(IScanner.__STDC_HOSTED__) == null )
			scannerData.getScanner().addDefinition(IScanner.__STDC_HOSTED__, new ObjectMacroDescriptor( IScanner.__STDC_HOSTED__, "0")); //$NON-NLS-1$
		if( scannerData.getScanner().getDefinition( IScanner.__STDC_VERSION__) == null )
			scannerData.getScanner().addDefinition( IScanner.__STDC_VERSION__, new ObjectMacroDescriptor( IScanner.__STDC_VERSION__, "199001L")); //$NON-NLS-1$
		
		//TODO - these macros should not be visible as macros in the scanner's definition list
		//need to make a public/private table i think
		if( scannerData.getScanner().getDefinition( __ATTRIBUTE__) == null )
			scannerData.getPrivateDefinitions().put( __ATTRIBUTE__, new FunctionMacroDescriptor( __ATTRIBUTE__, simpleIdentifiersDeclSpec,  EMPTY_LIST, "" )); //$NON-NLS-1$ $NON-NLS-2$
		
		if( scannerData.getScanner().getDefinition( __DECLSPEC) == null )
			scannerData.getPrivateDefinitions().put( __DECLSPEC, new FunctionMacroDescriptor( __ATTRIBUTE__, simpleIdentifiersDeclSpec,  EMPTY_LIST, "" )); //$NON-NLS-1$ $NON-NLS-2$

		if( scannerData.getScanner().getDefinition( __EXTENSION__ ) == null )
			scannerData.getPrivateDefinitions().put( __EXTENSION__, new ObjectMacroDescriptor( __EXTENSION__, EMPTY_STRING ));
		
		setupAlternativeKeyword(scannerData, __CONST__, Keywords.CONST);
		setupAlternativeKeyword(scannerData, __CONST, Keywords.CONST);
		setupAlternativeKeyword(scannerData, __INLINE__, Keywords.INLINE);
		setupAlternativeKeyword(scannerData, __SIGNED__, Keywords.SIGNED);
		setupAlternativeKeyword(scannerData, __VOLATILE__, Keywords.VOLATILE);
		setupAlternativeKeyword( scannerData, __RESTRICT, Keywords.RESTRICT);
		setupAlternativeKeyword( scannerData, __RESTRICT__, Keywords.RESTRICT);
		setupAlternativeKeyword( scannerData, __TYPEOF__, GCCKeywords.TYPEOF );
		if( scannerData.getLanguage() == ParserLanguage.CPP )
			setupAlternativeKeyword( scannerData, __ASM__, Keywords.ASM );
		
	}

	/**
	 * @param scannerData TODO
	 * 
	 */
	protected void setupAlternativeKeyword(IScannerData scannerData, String keyword, String mapping) {
		// alternate keyword forms
		// TODO - make this more efficient - update TokenFactory to avoid a context push for these token to token cases
		if( scannerData.getScanner().getDefinition( keyword ) == null )
			scannerData.getPrivateDefinitions().put( keyword, new ObjectMacroDescriptor( __CONST__, mapping )); //$NON-NLS-1$
	}

	private static final Set directives;
	static
	{
		directives = new HashSet();
		directives.add( POUND_INCLUDE_NEXT );
		directives.add( POUND_WARNING);
		directives.add( POUND_IDENT); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#canHandlePreprocessorDirective(java.lang.String)
	 */
	public boolean canHandlePreprocessorDirective(String directive) {
		return directives.contains( directive );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#handlePreprocessorDirective(java.lang.String, java.lang.String)
	 */
	public void handlePreprocessorDirective(IScannerData scannerData, String directive, String restOfLine) {
		if( directive.equals(POUND_INCLUDE_NEXT) ) 
		{
			TraceUtil.outputTrace(scannerData.getLogService(), "GCCScannerExtension handling #include_next directive", null, null, null, null); //$NON-NLS-1$
			// figure out the name of the current file and its path
			IScannerContext context = scannerData.getContextStack().getCurrentContext();
			if( context == null || context.getKind() != IScannerContext.ContextKind.INCLUSION ) 
				return;
			
			String fullInclusionPath = context.getFilename();
			IASTInclusion inclusion = context.getExtension();
			
			Iterator iter = scannerData.getIncludePathNames().iterator();
			
			while (iter.hasNext()) {
				String path = (String)iter.next();
				String completePath = ScannerUtility.createReconciledPath(path, inclusion.getName() );
				if( completePath.equals( fullInclusionPath ) )
					break;
			}
			
			ScannerUtility.InclusionDirective parsedDirective = null;
			try {
				parsedDirective = ScannerUtility.parseInclusionDirective( scannerData, this, restOfLine, scannerData.getContextStack().getCurrentContext().getOffset() );
			} catch (InclusionParseException e) {
				return;
			}
			CodeReader duple = null;
			// search through include paths
			while (iter.hasNext()) {	
				String path = (String)iter.next();
				duple = ScannerUtility.createReaderDuple( path, parsedDirective.getFilename(), scannerData.getClientRequestor(), scannerData.getWorkingCopies() );
				if( duple != null )
					break;
			}

			if( duple != null )
			{
				try			
				{
					scannerData.getContextStack().updateContext(duple.getUnderlyingReader(), duple.getFilename(), ScannerContext.ContextKind.INCLUSION, inclusion, scannerData.getClientRequestor() );
					TraceUtil.outputTrace( scannerData.getLogService(), "GCCScannerExtension handling #include_next directive successfully pushed on new include file" ); //$NON-NLS-1$
				}
				catch (ContextException e1)
				{
					return;
				}
			}
			
		}
		else if( directive.equals( POUND_WARNING) || directive.equals(POUND_IDENT)) 
			return; // good enough -- the rest of the line has been consumed
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#offersDifferentIdentifierCharacters()
	 */
	public boolean offersDifferentIdentifierCharacters() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#isValidIdentifierStartCharacter(int)
	 */
	public boolean isValidIdentifierStartCharacter(int c) {
		return Character.isLetter((char)c) || ( c == '_') || ( c == '$' );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#isValidIdentifierCharacter(int)
	 */
	public boolean isValidIdentifierCharacter(int c) {
		return ((c >= 'a') && (c <= 'z'))
		|| ((c >= 'A') && (c <= 'Z'))
		|| ((c >= '0') && (c <= '9'))
		|| (c == '_') || ( c== '$' ) || 
		Character.isUnicodeIdentifierPart( (char)c);
	}

	private static final Map additionalCPPKeywords;
	private static final Map additionalCKeywords;
	private static final Map additionalCPPOperators;
	private static final Map additionalCOperators;
	private static final String MAX_OPERATOR = ">?"; //$NON-NLS-1$
	private static final String MIN_OPERATOR = "<?"; //$NON-NLS-1$
	
	static
	{
		additionalCKeywords = new HashMap();
		additionalCKeywords.put( GCCKeywords.__ALIGNOF__, new Integer( IGCCToken.t___alignof__ ));
		additionalCKeywords.put( GCCKeywords.TYPEOF, new Integer( IGCCToken.t_typeof ));
		additionalCPPKeywords = new HashMap(additionalCKeywords);
		additionalCPPKeywords.put( Keywords.RESTRICT, new Integer( IToken.t_restrict ));
		
		additionalCOperators = new HashMap();
		
		additionalCPPOperators = new HashMap();
		additionalCPPOperators.put( MAX_OPERATOR, new Integer( IGCCToken.tMAX ) );
		additionalCPPOperators.put( MIN_OPERATOR, new Integer( IGCCToken.tMIN ) );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#isExtensionKeyword()
	 */
	public boolean isExtensionKeyword(ParserLanguage language, String tokenImage) {
		if( language == ParserLanguage.CPP )
			return ( additionalCPPKeywords.get( tokenImage ) != null );
		else if( language == ParserLanguage.C )
			return ( additionalCKeywords.get( tokenImage ) != null );
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#createExtensionToken()
	 */
	public IToken createExtensionToken(IScannerData scannerData, String image) {
		Integer get = null;
		if( scannerData.getLanguage() == ParserLanguage.CPP )
		{
			get = (Integer) additionalCPPKeywords.get( image );
			if( get == null )
				get = (Integer) additionalCPPOperators.get( image );
		}
		else if( scannerData.getLanguage()  == ParserLanguage.C )
		{
			get = (Integer) additionalCKeywords.get( image );
			if( get == null )
				get = (Integer) additionalCOperators.get( image );
		}
		if( get == null ) return null;
		return TokenFactory.createToken(get.intValue(),image,scannerData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#isExtensionOperator(java.lang.String)
	 */
	public boolean isExtensionOperator(ParserLanguage language, String query) {
		if( language == ParserLanguage.CPP )
			return ( additionalCPPOperators.get( query ) != null );
		else if (language == ParserLanguage.C )
			return ( additionalCOperators.get( query ) != null );
		return false;
	}

}
