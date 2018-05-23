/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14.constexpr;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ASTComparer;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.CStringValue;
import org.eclipse.cdt.internal.core.dom.parser.FloatingPointValue;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.index.tests.IndexBindingResolutionTestBase;

public class TestBase extends IndexBindingResolutionTestBase {
	private static final String TEST_CODE = "<testcode>";
	private static final IParserLogService NULL_LOG = new NullLogService();
	private static final ScannerInfo SCANNER_INFO = new ScannerInfo(getStdMap());

	private static Map<String, String> getStdMap() {
		Map<String, String> map = new HashMap<>();
		map.put("__SIZEOF_SHORT__", "2");
		map.put("__SIZEOF_INT__", "4");
		map.put("__SIZEOF_LONG__", "8");
		map.put("__SIZEOF_POINTER__", "8");
		return map;
	}

	protected void assertEvaluationProblem() throws Exception {
		IValue value = getValue();
		assertTrue(IntegralValue.ERROR.equals(value) || IntegralValue.UNKNOWN.equals(value));
	}

	protected void assertEvaluationEquals(boolean expectedValue) throws Exception {
		IValue value = getValue();
		Number num = value.numberValue();
		assertNotNull(num);
		assertEquals(expectedValue == false, num.longValue() == 0);
	}

	protected void assertEvaluationEquals(char expectedValue) throws Exception {
		IValue value = getValue();
		Number num = value.numberValue();
		assertNotNull(num);
		assertEquals(expectedValue, num.longValue());
	}

	protected void assertEvaluationEquals(long expectedValue) throws Exception {
		IValue value = getValue();
		Number num = value.numberValue();
		assertNotNull(num);
		assertEquals(expectedValue, num.longValue());
	}

	protected void assertEvaluationEquals(IValue expectedValue) throws Exception {
		IValue value = getValue();
		assertEquals(expectedValue, value);
	}

	protected void assertEvaluationEquals(String expectedValue) throws Exception {
		IValue value = getValue();
		assertInstance(value, CStringValue.class);
		CStringValue cstrValue = (CStringValue) value;
		assertEquals(expectedValue, cstrValue.cStringValue());
	}

	protected void assertEvaluationEquals(double expectedValue) throws Exception {
		IValue value = getValue();
		assertInstance(value, FloatingPointValue.class);
		FloatingPointValue floatingPointValue = (FloatingPointValue) value;
		assertEquals(expectedValue, floatingPointValue.numberValue().doubleValue(), 0.001);
	}

	private IValue getValue() throws Exception {
		ICPPASTInitializerClause point = getLastDeclarationInitializer();
		ICPPEvaluation evaluation = point.getEvaluation();
		try {
			CPPSemantics.pushLookupPoint(point);
			return evaluation.getValue();
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	protected ICPPASTInitializerClause getLastDeclarationInitializer() throws Exception {
		IASTTranslationUnit tu = strategy.getAst(0);
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getChildren()[tu.getChildren().length - 1];
		IASTEqualsInitializer initializer = (IASTEqualsInitializer) declaration.getDeclarators()[0].getInitializer();
		return (ICPPASTInitializerClause) initializer.getInitializerClause();
	}

	protected class NonIndexingTestStrategy implements ITestStrategy {
		private ICProject cproject;
		private StringBuilder[] testData;
		private IASTTranslationUnit ast;

		@Override
		public ICProject getCProject() {
			return cproject;
		}

		@Override
		public StringBuilder[] getTestData() {
			return testData;
		}

		@Override
		public int getAstCount() {
			return 1;
		}

		@Override
		public IASTTranslationUnit getAst(int index) {
			return ast;
		}

		@Override
		public StringBuilder getAstSource(int index) {
			return testData[0];
		}

		@Override
		public void setUp() throws Exception {
			CTestPlugin plugin = CTestPlugin.getDefault();
			StringBuilder[] builders = TestSourceReader.getContentsForTest(plugin.getBundle(), "parser",
					TestBase.this.getClass(), getName(), 2);
			if (builders.length == 2) {
				builders[0].append(builders[1].toString());
			}
			testData = new StringBuilder[] { builders[0] };
			ast = parse(testData[0].toString());
		}

		@Override
		public void tearDown() throws Exception {
		}

		@Override
		public IIndex getIndex() {
			return null;
		}

		@Override
		public boolean isCompositeIndex() {
			return false;
		}
	}

	protected static IASTTranslationUnit parse(String code) throws ParserException {
		IScanner scanner = createScanner(FileContent.create(TEST_CODE, code.toCharArray()), ParserLanguage.CPP,
				ParserMode.COMPLETE_PARSE, SCANNER_INFO);
		AbstractGNUSourceCodeParser parser = null;
		ICPPParserExtensionConfiguration config = new ANSICPPParserExtensionConfiguration();
		parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config, null);
		parser.setMaximumTrivialExpressionsInAggregateInitializers(Integer.MAX_VALUE);

		IASTTranslationUnit tu = parser.parse();
		assertTrue(tu.isFrozen());

		validateCopy(tu);

		if (parser.encounteredError()) {
			throw new ParserException("FAILURE"); //$NON-NLS-1$
		}

		assertEquals(CPPVisitor.getProblems(tu).length, 0);
		assertEquals(0, tu.getPreprocessorProblems().length);
		return tu;
	}

	private static IScanner createScanner(FileContent codeReader, ParserLanguage lang, ParserMode mode,
			IScannerInfo scannerInfo) {
		IScannerExtensionConfiguration configuration = GPPScannerExtensionConfiguration.getInstance(scannerInfo);
		IScanner scanner = new CPreprocessor(codeReader, scannerInfo, lang, NULL_LOG, configuration,
				IncludeFileContentProvider.getSavedFilesProvider());
		return scanner;
	}

	private static <T extends IASTNode> T validateCopy(T tu) {
		IASTNode copy = tu.copy();
		assertFalse(copy.isFrozen());
		ASTComparer.assertCopy(tu, copy);
		return (T) copy;
	}
}