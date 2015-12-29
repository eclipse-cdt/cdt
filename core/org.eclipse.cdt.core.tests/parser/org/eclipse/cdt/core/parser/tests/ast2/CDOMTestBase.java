/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.tests.FileBasePluginTestCase;
import org.eclipse.core.resources.IFile;

/**
 * @author dsteffle
 */
public class CDOMTestBase extends FileBasePluginTestCase {

	public CDOMTestBase() {
	}

	public CDOMTestBase(String name) {
		super(name);
	}

	public CDOMTestBase(String name, Class className) {
		super(name, className);
	}
	
	protected IASTTranslationUnit parse(String fileName, String contents) {
		IFile file = null;
		
		try {
			file = importFile(fileName, contents);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return parse(file);
	}
	
	protected IASTTranslationUnit parse(IFile file) {
		IASTTranslationUnit tu = null;
		
		try {
			tu = CDOM.getInstance().getASTService().getTranslationUnit(
			        file,
			        CDOM.getInstance().getCodeReaderFactory(
			              CDOM.PARSE_SAVED_RESOURCES));
		} catch (UnsupportedDialectException e) {
			e.printStackTrace();
		}
		
		return tu;
	}
	
}
