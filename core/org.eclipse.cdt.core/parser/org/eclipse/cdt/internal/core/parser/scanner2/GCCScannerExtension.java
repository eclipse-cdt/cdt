/**********************************************************************
 * Copyright (c) 2002,2004 IBM Rational Software and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * @author jcamelon
 */
public class GCCScannerExtension implements IScannerExtension {
	
//	protected static final ObjectMacroDescriptor STDC_VERSION_MACRO = new ObjectMacroDescriptor( IScanner.__STDC_VERSION__, "199001L"); //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor STDC_HOSTED_MACRO = new ObjectMacroDescriptor( IScanner.__STDC_HOSTED__, "0"); //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor CPLUSPLUS_MACRO = new ObjectMacroDescriptor( IScanner.__CPLUSPLUS, "1"); //$NON-NLS-1$
	private static final String [] simpleIdentifiersDeclSpec;
	private static final String [] simpleIdentifiersAttribute;
	static
	{
		simpleIdentifiersDeclSpec = new String[ 1 ];
		simpleIdentifiersDeclSpec[0]= "x"; //$NON-NLS-1$
		
		simpleIdentifiersAttribute = new String[ 1 ];
		simpleIdentifiersAttribute[0] = "xyz"; //$NON-NLS-1$
	}


	protected static final String POUND_IDENT = "#ident"; //$NON-NLS-1$
	protected static final String POUND_WARNING = "#warning"; //$NON-NLS-1$
	protected static final String POUND_INCLUDE_NEXT = "#include_next"; //$NON-NLS-1$
	
//	private static final String __CONST__ = "__const__"; //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor __CONST__MACRO = new ObjectMacroDescriptor( __CONST__, Keywords.CONST );
//	private static final String __CONST = "__const"; //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor __CONST_MACRO = new ObjectMacroDescriptor( __CONST, Keywords.CONST );
//	private static final String __INLINE__ = "__inline__"; //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor __INLINE__MACRO = new ObjectMacroDescriptor( __INLINE__, Keywords.INLINE );
//	private static final String __VOLATILE__ = "__volatile__"; //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor __VOLATILE__MACRO = new ObjectMacroDescriptor( __VOLATILE__, Keywords.VOLATILE );
//	private static final String __SIGNED__ = "__signed__"; //$NON-NLS-1$
//	private static final ObjectMacroDescriptor __SIGNED__MACRO = new ObjectMacroDescriptor( __SIGNED__, Keywords.SIGNED );
//	private static final String __RESTRICT = "__restrict"; //$NON-NLS-1$
//	private static final String __RESTRICT__ = "__restrict__"; //$NON-NLS-1$
//	private static final ObjectMacroDescriptor __RESTRICT__MACRO = new ObjectMacroDescriptor( __RESTRICT__, Keywords.RESTRICT );
//	private static final String __ASM__ = "__asm__"; //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor __ASM__MACRO = new ObjectMacroDescriptor( __ASM__, Keywords.ASM );
//	private static final String __TYPEOF__ = "__typeof__"; //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor __TYPEOF__MACRO = new ObjectMacroDescriptor( __TYPEOF__, GCCKeywords.TYPEOF );
	
	
//	private static final String __ATTRIBUTE__ = "__attribute__";  //$NON-NLS-1$
//	private static final String __DECLSPEC = "__declspec"; //$NON-NLS-1$
//	private static final IToken [] EMPTY_TOKEN_ARRAY = new IToken[0];
//	protected static final FunctionMacroDescriptor DECLSPEC_MACRO = new FunctionMacroDescriptor( __ATTRIBUTE__, simpleIdentifiersDeclSpec,  EMPTY_TOKEN_ARRAY, "" ); //$NON-NLS-1$
//	
//	protected static final FunctionMacroDescriptor ATTRIBUTE_MACRO = new FunctionMacroDescriptor( __ATTRIBUTE__, simpleIdentifiersAttribute,  EMPTY_TOKEN_ARRAY, "" ); //$NON-NLS-1$
	
//	private static final String __EXTENSION__ = "__extension__"; //$NON-NLS-1$
//	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
//	protected static final ObjectMacroDescriptor EXTENSION_MACRO = new ObjectMacroDescriptor( __EXTENSION__, EMPTY_STRING );
	
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
				
//		if( scannerData.getLanguage() == ParserLanguage.CPP )
//			if( scannerData.getScanner().getDefinition( IScanner.__CPLUSPLUS ) == null )
//				scannerData.getScanner().addDefinition( IScanner.__CPLUSPLUS, CPLUSPLUS_MACRO); 
//		
//		if( scannerData.getScanner().getDefinition(IScanner.__STDC_HOSTED__) == null )
//			scannerData.getScanner().addDefinition(IScanner.__STDC_HOSTED__, STDC_HOSTED_MACRO); 
//		if( scannerData.getScanner().getDefinition( IScanner.__STDC_VERSION__) == null )
//			scannerData.getScanner().addDefinition( IScanner.__STDC_VERSION__, STDC_VERSION_MACRO); 
//		
//		// add these to private table
////		if( scannerData.getScanner().getDefinition( __ATTRIBUTE__) == null )
////			scannerData.getPrivateDefinitions().put( __ATTRIBUTE__, ATTRIBUTE_MACRO); 
////		
////		if( scannerData.getScanner().getDefinition( __DECLSPEC) == null )
////			scannerData.getPrivateDefinitions().put( __DECLSPEC, DECLSPEC_MACRO );
////
//		if( scannerData.getScanner().getDefinition( __EXTENSION__ ) == null )
//			scannerData.getPrivateDefinitions().put( __EXTENSION__, EXTENSION_MACRO);
//		
//		if( scannerData.getScanner().getDefinition( __CONST__ ) == null )
//		scannerData.getPrivateDefinitions().put( __CONST__, __CONST__MACRO);
//		if( scannerData.getScanner().getDefinition( __CONST ) == null )
//		scannerData.getPrivateDefinitions().put( __CONST, __CONST_MACRO);
//		if( scannerData.getScanner().getDefinition( __INLINE__ ) == null )
//		scannerData.getPrivateDefinitions().put( __INLINE__, __INLINE__MACRO);
//		if( scannerData.getScanner().getDefinition( __SIGNED__ ) == null )
//		scannerData.getPrivateDefinitions().put( __SIGNED__, __SIGNED__MACRO);
//		if( scannerData.getScanner().getDefinition( __VOLATILE__ ) == null )
//		scannerData.getPrivateDefinitions().put( __VOLATILE__, __VOLATILE__MACRO);
//		ObjectMacroDescriptor __RESTRICT_MACRO = new ObjectMacroDescriptor( __RESTRICT, Keywords.RESTRICT );
//		if( scannerData.getScanner().getDefinition( __RESTRICT ) == null )
//		scannerData.getPrivateDefinitions().put( __RESTRICT, __RESTRICT_MACRO);
//		if( scannerData.getScanner().getDefinition( __RESTRICT__ ) == null )
//		scannerData.getPrivateDefinitions().put( __RESTRICT__, __RESTRICT__MACRO);
//		if( scannerData.getScanner().getDefinition( __TYPEOF__ ) == null )
//		scannerData.getPrivateDefinitions().put( __TYPEOF__, __TYPEOF__MACRO);
//		if( scannerData.getLanguage() == ParserLanguage.CPP )
//			if( scannerData.getScanner().getDefinition( __ASM__ ) == null )
//			scannerData.getPrivateDefinitions().put( __ASM__, __ASM__MACRO);
//		
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
	public void handlePreprocessorDirective(IScannerData iscanner, String directive, String restOfLine) {
		if( directive.equals(POUND_INCLUDE_NEXT) ) 
		{
			TraceUtil.outputTrace(iscanner.getLogService(), "GCCScannerExtension handling #include_next directive" ); //$NON-NLS-1$
			// figure out the name of the current file and its path
//			IScannerContext context = iscanner.getContextStack().getCurrentContext();
//			if( context == null || context.getKind() != IScannerContext.ContextKind.INCLUSION ) 
//				return;
//			
//			String fullInclusionPath = context.getContextName();
//			IASTInclusion inclusion = ((ScannerContextInclusion)context).getExtension();
//			
//			Iterator iter = iscanner.getIncludePathNames().iterator();
//			
//			while (iter.hasNext()) {
//				String path = (String)iter.next();
//				String completePath = ScannerUtility.createReconciledPath(path, inclusion.getName() );
//				if( completePath.equals( fullInclusionPath ) )
//					break;
//			}
//			
//			ScannerUtility.InclusionDirective parsedDirective = null;
//			try {
//				parsedDirective = iscanner.parseInclusionDirective( restOfLine, iscanner.getContextStack().getCurrentContext().getOffset() );
//			} catch (InclusionParseException e) {
//				return;
//			}
//			CodeReader duple = null;
//			// search through include paths
//			while (iter.hasNext()) {	
//				String path = (String)iter.next();
//				String finalPath = ScannerUtility.createReconciledPath(path, parsedDirective.getFilename());
//				duple = (CodeReader)iscanner.getFileCache().get(finalPath);
//				if (duple == null) {
//					duple = ScannerUtility.createReaderDuple( finalPath, iscanner.getClientRequestor(), iscanner.getWorkingCopies() );
//					if (duple != null && duple.isFile())
//						iscanner.getFileCache().put(duple.filename, duple);
//				}
//				if( duple != null )
//					break;
//			}
//
//			if( duple != null )
//			{
//				try			
//				{
//					iscanner.getContextStack().updateInclusionContext(duple, inclusion, iscanner.getClientRequestor() );
//					TraceUtil.outputTrace( iscanner.getLogService(), "GCCScannerExtension handling #include_next directive successfully pushed on new include file" ); //$NON-NLS-1$
//				}
//				catch (ContextException e1)
//				{
//					return;
//				}
//			}
//			
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
		return null; 
//		return TokenFactory.createUniquelyImagedToken(get.intValue(),image,scannerData);
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
