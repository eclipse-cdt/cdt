/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import static org.eclipse.cdt.core.parser.ParserLanguage.CPP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
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
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;

import junit.framework.AssertionFailedError;

/**
 * @author aniefer
 */
public class AST2TestBase extends SemanticTestBase {
	public final static String TEST_CODE = "<testcode>";
	protected static final IParserLogService NULL_LOG = new NullLogService();
	protected static boolean sValidateCopy;

	private static final ScannerInfo GNU_SCANNER_INFO = new ScannerInfo(getGnuMap());
	private static final ScannerInfo SCANNER_INFO = new ScannerInfo(getStdMap());

	private static Map<String, String> getGnuMap() {
		Map<String, String> map = new HashMap<>();
		map.put("__GNUC__", Integer.toString(GCC_MAJOR_VERSION_FOR_TESTS));
		map.put("__GNUC_MINOR__", Integer.toString(GCC_MINOR_VERSION_FOR_TESTS));
		map.put("__SIZEOF_SHORT__", "2");
		map.put("__SIZEOF_INT__", "4");
		map.put("__SIZEOF_LONG__", "8");
		map.put("__SIZEOF_DOUBLE__", "8");
		map.put("__SIZEOF_POINTER__", "8");
		return map;
	}

	private static Map<String, String> getStdMap() {
		Map<String, String> map = new HashMap<>();
		map.put("__SIZEOF_SHORT__", "2");
		map.put("__SIZEOF_INT__", "4");
		map.put("__SIZEOF_LONG__", "8");
		map.put("__SIZEOF_DOUBLE__", "8");
		map.put("__SIZEOF_POINTER__", "8");
		return map;
	}

	public AST2TestBase() {
		super();
	}

	public AST2TestBase(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		sValidateCopy = true;
		super.setUp();
	}

	protected IASTTranslationUnit parse(String code, ParserLanguage lang) throws ParserException {
		return parse(code, lang, false, true);
	}

	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions)
			throws ParserException {
		return parse(code, lang, useGNUExtensions, true);
	}

	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems) throws ParserException {
		return parse(code, lang, useGNUExtensions, expectNoProblems, Integer.MAX_VALUE);
	}

	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems, int limitTrivialInitializers) throws ParserException {
		IScanner scanner = createScanner(FileContent.create(TEST_CODE, code.toCharArray()), lang,
				ParserMode.COMPLETE_PARSE, createScannerInfo(useGNUExtensions));
		configureScanner(scanner);
		AbstractGNUSourceCodeParser parser = null;
		if (lang == ParserLanguage.CPP) {
			ICPPParserExtensionConfiguration config = null;
			if (useGNUExtensions) {
				config = new GPPParserExtensionConfiguration();
			} else {
				config = new ANSICPPParserExtensionConfiguration();
			}
			parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config, null);
		} else {
			ICParserExtensionConfiguration config = null;

			if (useGNUExtensions) {
				config = new GCCParserExtensionConfiguration();
			} else {
				config = new ANSICParserExtensionConfiguration();
			}

			parser = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config, null);
		}
		parser.setMaximumTrivialExpressionsInAggregateInitializers(limitTrivialInitializers);

		IASTTranslationUnit tu = parser.parse();
		assertTrue(tu.isFrozen());
		if (sValidateCopy)
			validateCopy(tu);

		if (parser.encounteredError() && expectNoProblems)
			throw new ParserException("FAILURE"); //$NON-NLS-1$

		if (lang == ParserLanguage.C && expectNoProblems) {
			assertEquals(CVisitor.getProblems(tu).length, 0);
			assertEquals(tu.getPreprocessorProblems().length, 0);
		} else if (lang == ParserLanguage.CPP && expectNoProblems) {
			assertEquals(CPPVisitor.getProblems(tu).length, 0);
			assertEquals(0, tu.getPreprocessorProblems().length);
		}
		if (expectNoProblems)
			assertEquals(0, tu.getPreprocessorProblems().length);

		return tu;
	}

	public ScannerInfo createScannerInfo(boolean useGnu) {
		if (useGnu)
			return GNU_SCANNER_INFO;
		return SCANNER_INFO;
	}

	protected void configureScanner(IScanner scanner) {
	}

	public static IScanner createScanner(FileContent codeReader, ParserLanguage lang, ParserMode mode,
			IScannerInfo scannerInfo) {
		IScannerExtensionConfiguration configuration = null;
		if (lang == ParserLanguage.C) {
			configuration = GCCScannerExtensionConfiguration.getInstance(scannerInfo);
		} else {
			configuration = GPPScannerExtensionConfiguration.getInstance(scannerInfo);
		}
		IScanner scanner;
		scanner = new CPreprocessor(codeReader, scannerInfo, lang, NULL_LOG, configuration,
				IncludeFileContentProvider.getSavedFilesProvider());
		return scanner;
	}

	protected void validateSimplePostfixInitializerExpressionC(String code) throws ParserException {
		ICASTTypeIdInitializerExpression e = (ICASTTypeIdInitializerExpression) getExpressionFromStatementInCode(code,
				ParserLanguage.C);
		assertNotNull(e);
		assertNotNull(e.getTypeId());
		assertNotNull(e.getInitializer());
	}

	protected void validateSimpleUnaryTypeIdExpression(String code, int op) throws ParserException {
		IASTCastExpression e = (IASTCastExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
		assertNotNull(e);
		assertEquals(e.getOperator(), op);
		assertNotNull(e.getTypeId());
		IASTIdExpression x = (IASTIdExpression) e.getOperand();
		assertEquals(x.getName().toString(), "x"); //$NON-NLS-1$
	}

	protected void validateSimpleTypeIdExpressionC(String code, int op) throws ParserException {
		IASTTypeIdExpression e = (IASTTypeIdExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
		assertNotNull(e);
		assertEquals(e.getOperator(), op);
		assertNotNull(e.getTypeId());
	}

	protected void validateSimpleUnaryExpressionC(String code, int operator) throws ParserException {
		IASTUnaryExpression e = (IASTUnaryExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
		assertNotNull(e);
		assertEquals(e.getOperator(), operator);
		IASTIdExpression x = (IASTIdExpression) e.getOperand();
		assertEquals(x.getName().toString(), "x"); //$NON-NLS-1$
	}

	protected void validateConditionalExpressionC(String code) throws ParserException {
		IASTConditionalExpression e = (IASTConditionalExpression) getExpressionFromStatementInCode(code,
				ParserLanguage.C);
		assertNotNull(e);
		IASTIdExpression x = (IASTIdExpression) e.getLogicalConditionExpression();
		assertEquals(x.getName().toString(), "x"); //$NON-NLS-1$
		IASTIdExpression y = (IASTIdExpression) e.getPositiveResultExpression();
		assertEquals(y.getName().toString(), "y"); //$NON-NLS-1$
		IASTIdExpression x2 = (IASTIdExpression) e.getNegativeResultExpression();
		assertEquals(x.getName().toString(), x2.getName().toString());
	}

	protected void validateSimpleBinaryExpressionC(String code, int operand) throws ParserException {
		IASTBinaryExpression e = (IASTBinaryExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
		assertNotNull(e);
		assertEquals(e.getOperator(), operand);
		IASTIdExpression x = (IASTIdExpression) e.getOperand1();
		assertEquals(x.getName().toString(), "x"); //$NON-NLS-1$
		IASTIdExpression y = (IASTIdExpression) e.getOperand2();
		assertEquals(y.getName().toString(), "y"); //$NON-NLS-1$
	}

	protected IASTExpression getExpressionFromStatementInCode(String code, ParserLanguage language)
			throws ParserException {
		StringBuilder buffer = new StringBuilder("void f() { "); //$NON-NLS-1$
		buffer.append("int x, y;\n"); //$NON-NLS-1$
		buffer.append(code);
		buffer.append(";\n}"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), language);
		IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement cs = (IASTCompoundStatement) f.getBody();
		IASTExpressionStatement s = (IASTExpressionStatement) cs.getStatements()[1];
		return s.getExpression();
	}

	protected <T extends IASTNode> T validateCopy(T tu) {
		IASTNode copy = tu.copy();
		assertFalse(copy.isFrozen());
		ASTComparer.assertCopy(tu, copy);
		return (T) copy;
	}

	static protected class NameCollector extends ASTVisitor {
		public NameCollector() {
			this(false); // don't visit implicit names by default
		}

		public NameCollector(boolean shouldVisitImplicitNames) {
			this.shouldVisitNames = true;
			this.shouldVisitImplicitNames = shouldVisitImplicitNames;
		}

		public List<IASTName> nameList = new ArrayList<>();

		@Override
		public int visit(IASTName name) {
			nameList.add(name);
			return PROCESS_CONTINUE;
		}

		public IASTName getName(int idx) {
			if (idx < 0 || idx >= nameList.size())
				return null;
			return nameList.get(idx);
		}

		public int size() {
			return nameList.size();
		}

		public void dump() {
			for (int i = 0; i < size(); i++) {
				IASTName name = getName(i);
				String parent = name.getParent() != null ? name.getParent().getRawSignature() : "";
				System.out.println(i + ": #" + name.getRawSignature() + "# " + parent);
			}
		}
	}

	protected void assertInstances(NameCollector collector, IBinding binding, int num) throws Exception {
		int count = 0;
		for (int i = 0; i < collector.size(); i++) {
			if (collector.getName(i).resolveBinding() == binding)
				count++;
		}

		assertEquals(num, count);
	}

	protected void isExpressionStringEqual(IASTInitializerClause exp, String str) {
		String expressionString = ASTStringUtil.getExpressionString((IASTExpression) exp);
		assertEquals(str, expressionString);
	}

	protected void isExpressionStringEqual(IASTExpression exp, String str) {
		String expressionString = ASTStringUtil.getExpressionString(exp);
		assertEquals(str, expressionString);
	}

	protected void isParameterSignatureEqual(IASTDeclarator decltor, String str) {
		assertTrue(decltor instanceof IASTFunctionDeclarator);
		final String[] sigArray = ASTStringUtil.getParameterSignatureArray((IASTFunctionDeclarator) decltor);
		assertEquals(str, "(" + ASTStringUtil.join(sigArray, ", ") + ")");
	}

	protected void isSignatureEqual(IASTDeclarator declarator, String expected) {
		String signature = ASTStringUtil.getSignatureString(declarator);
		assertEquals(expected, signature);
	}

	protected void isSignatureEqual(IASTDeclSpecifier declSpec, String str) {
		assertEquals(str, ASTStringUtil.getSignatureString(declSpec, null));
	}

	protected void isSignatureEqual(IASTTypeId typeId, String str) {
		assertEquals(str, ASTStringUtil.getSignatureString(typeId.getDeclSpecifier(), typeId.getAbstractDeclarator()));
	}

	protected void isTypeEqual(IASTDeclarator decltor, String str) {
		assertEquals(str, ASTTypeUtil.getType(decltor));
	}

	protected void isTypeEqual(IASTTypeId typeId, String str) {
		assertEquals(str, ASTTypeUtil.getType(typeId));
	}

	protected void isTypeEqual(IType type, String str) {
		assertEquals(str, ASTTypeUtil.getType(type));
	}

	protected void isParameterTypeEqual(IFunctionType fType, String str) {
		assertEquals(str, ASTTypeUtil.getParameterTypeString(fType));
	}

	static protected class CNameResolver extends ASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings;
		public int numNullBindings;
		public List<IASTName> nameList = new ArrayList<>();

		@Override
		public int visit(IASTName name) {
			nameList.add(name);
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}

		public IASTName getName(int idx) {
			if (idx < 0 || idx >= nameList.size())
				return null;
			return nameList.get(idx);
		}

		public int size() {
			return nameList.size();
		}
	}

	static protected class CPPNameResolver extends ASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings;
		public int numNullBindings;
		public List<IASTName> nameList = new ArrayList<>();

		@Override
		public int visit(IASTName name) {
			nameList.add(name);
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}

		public IASTName getName(int idx) {
			if (idx < 0 || idx >= nameList.size())
				return null;
			return nameList.get(idx);
		}

		public int size() {
			return nameList.size();
		}
	}

	protected String getAboveComment() throws IOException {
		return getContents(1)[0].toString();
	}

	protected CharSequence[] getContents(int sections) throws IOException {
		CTestPlugin plugin = CTestPlugin.getDefault();
		if (plugin == null)
			throw new AssertionFailedError("This test must be run as a JUnit plugin test");
		return TestSourceReader.getContentsForTest(plugin.getBundle(), "parser", getClass(), getName(), sections);
	}

	protected static void assertField(IBinding binding, String fieldName, String ownerName) {
		assertInstance(binding, IField.class);
		assertEquals(fieldName, binding.getName());
		ICompositeType struct = ((IField) binding).getCompositeTypeOwner();
		assertEquals(ownerName, struct.getName());
	}

	protected static void assertConstantValue(long expected, IVariable constant) {
		IValue value = constant.getInitialValue();
		assertNotNull(value);
		Number numericalValue = value.numberValue();
		assertNotNull(numericalValue);
		assertEquals(expected, numericalValue.longValue());
	}

	protected class AST2AssertionHelper extends BindingAssertionHelper {
		protected boolean isCPP;

		public AST2AssertionHelper(String contents, boolean isCPP) throws ParserException {
			this(contents, isCPP ? ParserLanguage.CPP : ParserLanguage.C);
		}

		public AST2AssertionHelper(String contents, ParserLanguage lang) throws ParserException {
			super(contents, parse(contents, lang, true, false));
			this.isCPP = lang.isCPP();
		}
	}

	final protected IASTTranslationUnit parseAndCheckBindings(String code, ParserLanguage lang) throws Exception {
		return parseAndCheckBindings(code, lang, false);
	}

	final protected IASTTranslationUnit parseAndCheckBindings(String code, ParserLanguage lang,
			boolean useGnuExtensions) throws Exception {
		return parseAndCheckBindings(code, lang, useGnuExtensions, Integer.MAX_VALUE);
	}

	final protected IASTTranslationUnit parseAndCheckBindings(String code, ParserLanguage lang,
			boolean useGnuExtensions, int limitTrivialInitializers) throws Exception {
		IASTTranslationUnit tu = parse(code, lang, useGnuExtensions, true, limitTrivialInitializers);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
		return tu;
	}

	final protected IASTTranslationUnit parseAndCheckImplicitNameBindings() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, false, true);
		NameCollector col = new NameCollector(true /* Visit implicit names */);
		tu.accept(col);
		assertNoProblemBindings(col);
		return tu;
	}

	protected BindingAssertionHelper getAssertionHelper(ParserLanguage lang) throws ParserException, IOException {
		String code = getAboveComment();
		return new AST2AssertionHelper(code, lang);
	}

	final protected void assertNoProblemBindings(NameCollector col) {
		for (IASTName n : col.nameList) {
			assertFalse("ProblemBinding for " + n.getRawSignature(), n.resolveBinding() instanceof IProblemBinding);
		}
	}

	final protected void assertProblemBindings(NameCollector col, int count) {
		int sum = 0;
		for (IASTName n : col.nameList) {
			if (n.resolveBinding() instanceof IProblemBinding)
				++sum;
		}
		assertEquals(count, sum);
	}

	final protected <T extends IASTDeclaration> T getDeclaration(IASTTranslationUnit tu, int i_decl) {
		Class<T> tclass;
		IASTDeclaration[] decls = tu.getDeclarations();
		assertTrue(decls.length > i_decl);
		return (T) decls[i_decl];
	}

	final protected <T extends IASTDeclaration> T getDeclaration(ICPPASTNamespaceDefinition ns, int i_decl) {
		Class<T> tclass;
		IASTDeclaration[] decls = ns.getDeclarations();
		assertTrue(decls.length > i_decl);
		return (T) decls[i_decl];
	}

	final protected <T extends IASTDeclaration> T getDeclaration(ICPPASTLinkageSpecification ls, int i_decl) {
		Class<T> tclass;
		IASTDeclaration[] decls = ls.getDeclarations();
		assertTrue(decls.length > i_decl);
		return (T) decls[i_decl];
	}

	final protected <T extends IASTDeclaration> T getDeclaration(IASTCompositeTypeSpecifier ct, int i_decl) {
		Class<T> tclass;
		IASTDeclaration[] decls = ct.getMembers();
		assertTrue(decls.length > i_decl);
		return (T) decls[i_decl];
	}

	final protected <T extends IASTCompositeTypeSpecifier> T getCompositeType(IASTTranslationUnit tu, int i_decl) {
		IASTSimpleDeclaration sdecl = getDeclaration(tu, i_decl);
		return (T) sdecl.getDeclSpecifier();
	}

	final protected <T extends IASTStatement> T getStatement(IASTFunctionDefinition fdef, int i_stmt) {
		return getStatement((IASTCompoundStatement) fdef.getBody(), i_stmt);
	}

	final protected <T extends IASTStatement> T getStatement(IASTCompoundStatement compound, int i_stmt) {
		IASTStatement[] stmts = compound.getStatements();
		assertTrue(stmts.length > i_stmt);
		return (T) stmts[i_stmt];
	}

	final protected <T extends IASTExpression> T getExpressionOfStatement(IASTFunctionDefinition fdef, int i) {
		IASTStatement stmt = getStatement(fdef, i);
		assertInstance(stmt, IASTExpressionStatement.class);
		return (T) ((IASTExpressionStatement) stmt).getExpression();
	}

	/**
	 * Sort the given array of AST names lexicographically.
	 */
	protected static <T extends IASTName> void sortNames(T[] names) {
		Arrays.sort(names, new Comparator<IASTName>() {
			@Override
			public int compare(IASTName a, IASTName b) {
				return a.toString().compareTo(b.toString());
			}
		});
	}
}
