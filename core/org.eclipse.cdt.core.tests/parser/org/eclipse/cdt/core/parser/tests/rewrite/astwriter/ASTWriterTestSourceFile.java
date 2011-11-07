/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.astwriter;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.rewrite.TestSourceFile;

/**
 * @author Guido Zgraggen IFS
 */
public class ASTWriterTestSourceFile extends TestSourceFile {
	private ParserLanguage parserLanguage = ParserLanguage.CPP;
	private boolean useGNUExtensions = false;

	public ASTWriterTestSourceFile(String name) {
		super(name);
	}
	
	public void setParserLanguage(ParserLanguage lang) {
		this.parserLanguage = lang;
	}

	public ParserLanguage getParserLanguage() {
		return parserLanguage;
	}

	public boolean isUseGNUExtensions() {
		return useGNUExtensions;
	}

	public void setUseGNUExtensions(boolean useGNUExtensions) {
		this.useGNUExtensions = useGNUExtensions;
	}
}
