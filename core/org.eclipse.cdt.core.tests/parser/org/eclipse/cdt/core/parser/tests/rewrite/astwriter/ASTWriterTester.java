/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.astwriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase.ScannerKind;
import org.eclipse.cdt.core.parser.tests.rewrite.RewriteBaseTest;
import org.eclipse.cdt.core.parser.tests.rewrite.TestHelper;
import org.eclipse.cdt.core.parser.tests.rewrite.TestSourceFile;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Guido Zgraggen
 */
public class ASTWriterTester extends RewriteBaseTest {

	private static final IParserLogService NULL_LOG = new NullLogService();

	private IFile file;

	public static List<Arguments> loadTests() throws Exception {
		return SourceRewriteTest.loadTests();
	}

	@ParameterizedTest
	@MethodSource("loadTests")
	protected void test(ASTWriterTestSourceFile testFile) throws Throwable {
		if (testFile.getSource().length() > 0) {
			importFile(testFile.getName(), testFile.getSource());
		}
		TextSelection sel = testFile.getSelection();
		if (sel != null) {
			setFileWithSelection(testFile.getName());
			setSelection(sel);
		}

		file = project.getFile("ASTWritterTest.h"); //$NON-NLS-1$
		compareFiles(testFile);
	}

	private void compareFiles(ASTWriterTestSourceFile testFile) throws Exception {
		String code = generateSource(testFile);
		assertEquals(TestHelper.unifyNewLines(testFile.getExpectedSource()),
				TestHelper.unifyNewLines(code + System.getProperty("line.separator"))); //$NON-NLS-1$
	}

	private String generateSource(TestSourceFile testFile) throws Exception {
		IASTTranslationUnit unit = getParser(testFile).parse();
		NodeCommentMap commentMap = ASTCommenter.getCommentedNodeMap(unit);
		ASTModificationMap map = new ASTModificationMap();
		map.getModificationsForNode(unit.getDeclarations()[0]);
		ASTWriter writer = new ASTWriter();
		return writer.write(unit, commentMap);
	}

	private ISourceCodeParser getParser(TestSourceFile testFile) throws Exception {
		FileContent codeReader = FileContent.create(file);

		ParserLanguage language = getLanguage(testFile);
		ScannerKind scannerKind = getScannerKind(testFile);
		ScannerInfo scannerInfo = AST2TestBase.createScannerInfo(scannerKind);

		IScanner scanner = AST2TestBase.createScanner(codeReader, language, ParserMode.COMPLETE_PARSE, scannerInfo);

		ISourceCodeParser parser = null;
		if (language == ParserLanguage.CPP) {
			ICPPParserExtensionConfiguration config = null;
			if (scannerKind.isUseGNUExtensions()) {
				config = new GPPParserExtensionConfiguration();
			} else {
				config = new ANSICPPParserExtensionConfiguration();
			}
			parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
		} else {
			ICParserExtensionConfiguration config = null;

			if (scannerKind.isUseGNUExtensions()) {
				config = new GCCParserExtensionConfiguration();
			} else {
				config = new ANSICParserExtensionConfiguration();
			}

			parser = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
		}
		return parser;
	}

	private ScannerKind getScannerKind(TestSourceFile file) {
		if (file instanceof ASTWriterTestSourceFile)
			return ((ASTWriterTestSourceFile) file).getScannerKind();
		return ScannerKind.STD;
	}

	private ParserLanguage getLanguage(TestSourceFile file) {
		if (file instanceof ASTWriterTestSourceFile)
			return ((ASTWriterTestSourceFile) file).getParserLanguage();
		return ParserLanguage.CPP;
	}
}
