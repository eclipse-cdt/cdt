/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gcc.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.tests.ast2.DOMLocationInclusionTests;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.core.resources.IFile;

@SuppressWarnings("restriction")
public class LRDOMLocationInclusionTests extends DOMLocationInclusionTests {
	
	public static TestSuite suite() {
    	return new TestSuite(LRDOMLocationInclusionTests.class);
    }
	
	public LRDOMLocationInclusionTests() {
	}

	public LRDOMLocationInclusionTests(String name, Class<Object> className) {
		super(name, className);
	}

	public LRDOMLocationInclusionTests(String name) {
		super(name);
	}

	@Override
	protected IASTTranslationUnit parse(IFile code, IScannerInfo s)throws Exception {
		// TODO: total freakin hack! the test suite needs to be refactored
		ILanguage lang = code.getName().endsWith("cc") ? getCPPLanguage() : getCLanguage(); //$NON-NLS-1$
		
		CodeReader codeReader = new CodeReader(code.getLocation().toOSString());
		IASTTranslationUnit tu = lang.getASTTranslationUnit(codeReader, s, SavedCodeReaderFactory.getInstance(), null, ILanguage.OPTION_ADD_COMMENTS, ParserUtil.getParserLogService());

		return tu;
	}

	@Override
	protected IASTTranslationUnit parse(IFile code) throws Exception {
	
		return parse(code, new ExtendedScannerInfo());
	}

	
	protected ILanguage getCLanguage() {
    	return GCCLanguage.getDefault();
    }
	
	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}


}
