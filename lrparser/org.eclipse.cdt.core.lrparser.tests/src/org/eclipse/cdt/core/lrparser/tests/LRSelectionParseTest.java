/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ast2.AST2SelectionParseTest;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.resources.IFile;

import junit.framework.TestSuite;

@SuppressWarnings("restriction")
public class LRSelectionParseTest extends AST2SelectionParseTest {

	public static TestSuite suite() {
		return new TestSuite(LRSelectionParseTest.class);
	}

	public LRSelectionParseTest() {
	}

	public LRSelectionParseTest(String name) {
		super(name);
	}

	@Override
	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length) throws ParserException {
		return parse(code, lang, false, false, offset, length);
	}

	@Override
	protected IASTNode parse(IFile file, ParserLanguage lang, int offset, int length) throws ParserException {
		IASTTranslationUnit tu = parse(file, lang, false, false);
		return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
	}

	@Override
	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length, boolean expectedToPass)
			throws ParserException {
		return parse(code, lang, false, expectedToPass, offset, length);
	}

	@Override
	protected IASTNode parse(String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems,
			int offset, int length) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckPreprocessorProblems(expectNoProblems);
		options.setCheckSyntaxProblems(expectNoProblems);
		IASTTranslationUnit tu = ParseHelper.parse(code, language, options);
		return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
	}

	protected IASTTranslationUnit parse(IFile file, ParserLanguage lang, IScannerInfo scanInfo,
			boolean useGNUExtensions, boolean expectNoProblems) {

		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();

		String fileName = file.getLocation().toOSString();
		ICodeReaderFactory fileCreator = SavedCodeReaderFactory.getInstance();
		CodeReader reader = fileCreator.createCodeReaderForTranslationUnit(fileName);

		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckPreprocessorProblems(expectNoProblems);
		options.setCheckSyntaxProblems(expectNoProblems);
		options.setCheckBindings(true);

		return ParseHelper.parse(reader, language, scanInfo, fileCreator, options);
	}

	@Override
	protected IASTTranslationUnit parse(IFile file, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems) throws ParserException {
		return parse(file, lang, new ScannerInfo(), useGNUExtensions, expectNoProblems);
	}

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

}
