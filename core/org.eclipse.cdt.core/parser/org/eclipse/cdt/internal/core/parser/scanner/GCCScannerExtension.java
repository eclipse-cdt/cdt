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
import java.util.Set;

import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;

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
			scannerData.getLogService().traceLog( "GCCScannerExtension handling #include_next directive" ); //$NON-NLS-1$
			// figure out the name of the current file and its path
//			IScannerContext context = scannerData.getContextStack().getCurrentContext();
//			if( context.getKind() != IScannerContext.ContextKind.INCLUSION ) 
//			{
//				//handle appropriate error
//			}
//			String fullInclusionPath = context.getFilename();
//			IASTInclusion inclusion = context.getExtension();
			
			
			// search through include paths 
		}
	}

}
