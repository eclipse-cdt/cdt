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

import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;

/**
 * @author jcamelon
 */
public class GCCScannerExtension implements IScannerExtension {

	private IScanner scanner;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerExtension#initializeMacroValue(java.lang.String)
	 */
	public String initializeMacroValue(String original) {
		if( original == null || original.trim().equals( "") )
			return "1";
		return original;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerExtension#setupBuiltInMacros()
	 */
	public void setupBuiltInMacros(ParserLanguage language) {
		if( language == ParserLanguage.CPP )
			if( scanner.getDefinition( IScanner.__CPLUSPLUS ) == null )
				scanner.addDefinition( IScanner.__CPLUSPLUS, new ObjectMacroDescriptor( IScanner.__CPLUSPLUS, "1"));
		if( scanner.getDefinition(IScanner.__STDC_HOSTED__) == null )
			scanner.addDefinition(IScanner.__STDC_HOSTED__, new ObjectMacroDescriptor( IScanner.__STDC_HOSTED__, "0"));
		if( scanner.getDefinition( IScanner.__STDC_VERSION__) == null )
			scanner.addDefinition( IScanner.__STDC_VERSION__, new ObjectMacroDescriptor( IScanner.__STDC_VERSION__, "199001L"));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#setScanner(org.eclipse.cdt.core.parser.IScanner)
	 */
	public void setScanner(IScanner scanner) {
		this.scanner = scanner;
	}
	
	public Object clone( ) {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
