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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionParseException;

/**
 * @author jcamelon
 */
public class GCCScannerExtension implements IScannerExtension {


	private IScannerData scannerData;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerExtension#initializeMacroValue(java.lang.String)
	 */
	public String initializeMacroValue(String original) {
		if( original == null || original.trim().equals( "") ) //$NON-NLS-1$
			return "1"; //$NON-NLS-1$
		return original;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerExtension#setupBuiltInMacros()
	 */
	public void setupBuiltInMacros(ParserLanguage language) {
		if( language == ParserLanguage.CPP )
			if( scannerData.getScanner().getDefinition( IScanner.__CPLUSPLUS ) == null )
				scannerData.getScanner().addDefinition( IScanner.__CPLUSPLUS, new ObjectMacroDescriptor( IScanner.__CPLUSPLUS, "1")); //$NON-NLS-1$
		if( scannerData.getScanner().getDefinition(IScanner.__STDC_HOSTED__) == null )
			scannerData.getScanner().addDefinition(IScanner.__STDC_HOSTED__, new ObjectMacroDescriptor( IScanner.__STDC_HOSTED__, "0")); //$NON-NLS-1$
		if( scannerData.getScanner().getDefinition( IScanner.__STDC_VERSION__) == null )
			scannerData.getScanner().addDefinition( IScanner.__STDC_VERSION__, new ObjectMacroDescriptor( IScanner.__STDC_VERSION__, "199001L")); //$NON-NLS-1$
	}

	public void setScannerData(IScannerData scannerData) {
		this.scannerData = scannerData;
	}
	
	public Object clone( ) {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	private static final Set directives;
	static
	{
		directives = new HashSet();
		directives.add( "#include_next" ); //$NON-NLS-1$
		directives.add( "#warning"); //$NON-NLS-1$
		directives.add( "#ident"); //$NON-NLS-1$
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
	public void handlePreprocessorDirective(String directive, String restOfLine) {
		if( directive.equals("#include_next") ) //$NON-NLS-1$
		{
			TraceUtil.outputTrace(scannerData.getLogService(), "GCCScannerExtension handling #include_next directive", null, null, null, null);
			// figure out the name of the current file and its path
			IScannerContext context = scannerData.getContextStack().getCurrentContext();
			if( context.getKind() != IScannerContext.ContextKind.INCLUSION ) 
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
				duple = ScannerUtility.createReaderDuple( path, parsedDirective.getFilename(), scannerData.getClientRequestor() );
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
		else if( directive.equals( "#warning") || directive.equals("#ident"))
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

}
