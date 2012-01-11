/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.parser.tests.scanner;

import java.io.IOException;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorErrorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationCtx;
import org.eclipse.cdt.internal.core.parser.scanner.ImageLocationInfo;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.core.parser.scanner.LocationMap;

public class LocationMapTests extends BaseTestCase {
	public class Loc implements IASTFileLocation {
		private String fFile;
		private int fOffset;
		private int fEndOffset;
		public Loc(String file, int offset, int endOffset) {
			fFile= file;
			fOffset= offset;
			fEndOffset= endOffset;
		}
		@Override
		public int getEndingLineNumber() {
			return 0;
		}
		@Override
		public String getFileName() {
			return fFile;
		}
		@Override
		public int getNodeLength() {
			return fEndOffset-fOffset;
		}
		@Override
		public int getNodeOffset() {
			return fOffset;
		}
		@Override
		public int getStartingLineNumber() {
			return 0;
		}
		@Override
		public IASTFileLocation asFileLocation() {
			return this;
		}
		@Override
		public IASTPreprocessorIncludeStatement getContextInclusionStatement() {
			return null;
		}
	}

	private static final String FN = "filename";
	private static final int ROLE_DEFINITION = IASTNameOwner.r_definition;
	private static final int ROLE_UNCLEAR = IASTNameOwner.r_unclear;
	private static final int ROLE_REFERENCE = IASTNameOwner.r_reference;
	private static final ASTNodeProperty PROP_PST = IASTTranslationUnit.PREPROCESSOR_STATEMENT;
	final static char[] DIGITS= "0123456789abcdef".toCharArray();
	final static char[] LONGDIGITS= new char[1024];
	static {
		for (int i = 0; i < LONGDIGITS.length; i++) {
			LONGDIGITS[i]= (char) i;
		}
	}
	private LocationMap fLocationMap;
	private CPPASTTranslationUnit fTu;

	public static TestSuite suite() {
		return suite(LocationMapTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fLocationMap= new LocationMap(new LexerOptions());
	}

	@Override
	protected void tearDown() throws Exception {
		fLocationMap= null;
		super.tearDown();
	}

	protected StringBuilder[] getContents(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), sections);
	}

	private void init(char[] content) {
		fLocationMap.pushTranslationUnit(FN, new CharArray(content));
		fTu= new CPPASTTranslationUnit();
		fTu.setLocationResolver(fLocationMap);
	}

	private void checkASTNode(IASTNode node, IASTNode parent, ASTNodeProperty property,
			String filename, int offset, int length, int line, int endline, String sig) {
		assertTrue(node.contains(node));
		assertSame(parent, node.getParent());
		assertEquals(property, node.getPropertyInParent());
		assertSame(parent.getTranslationUnit(), node.getTranslationUnit());
		assertEquals(filename, node.getContainingFilename());
		
		if (offset >= 0) {
			IASTFileLocation loc= node.getFileLocation();
			checkLocation(loc, filename, offset, length, line, endline);
			assertEquals(sig, node.getRawSignature());
		}
		else {
			assertNull(node.getFileLocation());
		}
	}
	
	private void checkName(IASTName name, IBinding binding, String nameString,
			IASTNode parent, ASTNodeProperty property, int role, 
			String filename, int offset, int length, int line, int endline, String sig) {
		assertSame(binding, name.getBinding());
		assertSame(binding, name.resolveBinding());
		assertEquals(Linkage.CPP_LINKAGE, name.getLinkage());
		assertEquals(nameString, name.toString());
		checkASTNode(name, parent, property, filename, offset, length, line, endline, sig);
		assertEquals(name.isDeclaration(), role == IASTNameOwner.r_declaration);
		assertEquals(name.isDefinition(), role == ROLE_DEFINITION);
		assertEquals(name.isReference(), role == IASTNameOwner.r_reference);
	}
	
	private void checkLocation(IASTFileLocation loc, String filename, int offset, int length, int line, int endline) {
		if (loc == null) {
			assertEquals(0, offset);
			assertEquals(0, length);
		}
		else {
			assertEquals(filename, loc.getFileName());
			assertEquals(offset, loc.getNodeOffset());
			assertEquals(length, loc.getNodeLength());
			assertEquals(line, loc.getStartingLineNumber());
			assertEquals(endline, loc.getEndingLineNumber());
		}
	}
	
	private void checkComment(IASTComment comment, String content, boolean blockComment,  
			String filename, int offset, int length, int line, int endline) {
		assertEquals(content, new String(comment.getComment()));
		assertEquals(blockComment, comment.isBlockComment());
		checkASTNode(comment, fTu, PROP_PST, filename, offset, length, line, endline, content);
		assertEquals(filename, comment.getContainingFilename());
		assertEquals(content, new String(comment.getComment()));
		assertEquals(blockComment, comment.isBlockComment());
	}

	private void checkProblem(IASTProblem problem, int id, String arg, String marked, 
			String filename, int offset, int length, int line, int endline) {
		assertEquals(id, problem.getID());
		if (arg != null) {
			assertEquals(arg, problem.getArguments()[0]);
		}
		assertFalse(problem.isError());
		assertTrue(problem.isWarning());
		checkASTNode(problem, fTu, IASTTranslationUnit.SCANNER_PROBLEM, filename, offset, length, line, endline, marked);
	}

	private void checkError(IASTPreprocessorStatement s, String directive, String condition,  
			String filename, int offset, int length, int line) {
		IASTPreprocessorErrorStatement st= (IASTPreprocessorErrorStatement) s;
		assertEquals(condition, new String(st.getMessage()));
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkPragma(IASTPreprocessorStatement s, String directive, String condition, 
			String filename, int offset, int length, int line) {
		IASTPreprocessorPragmaStatement st= (IASTPreprocessorPragmaStatement) s;
		assertEquals(condition, new String(st.getMessage()));
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkInclude(IASTPreprocessorIncludeStatement include, String directive,
			String nameImage, String name, String fileTarget, boolean user, boolean active,
			String filename, int offset, int length, int line, int nameOffset, int nameLength) {
		assertEquals(fileTarget, include.getPath());
		assertEquals(user, !include.isSystemInclude());
		assertEquals(active, include.isActive());
		assertEquals(fileTarget.length() > 0, include.isResolved());
		
		checkName(include.getName(), null, name, include, IASTPreprocessorIncludeStatement.INCLUDE_NAME, ROLE_UNCLEAR, filename, nameOffset, nameLength, line, line, nameImage);
		checkASTNode(include, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkIf(IASTPreprocessorStatement s, String directive, String condition, boolean active,
			String filename, int offset, int length, int line) {
		IASTPreprocessorIfStatement st= (IASTPreprocessorIfStatement) s;
		assertEquals(condition, new String(st.getCondition()));
		assertEquals(active, st.taken());
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkIfdef(IASTPreprocessorStatement s, String directive, String condition, boolean active,
			String filename, int offset, int length, int line) {
		IASTPreprocessorIfdefStatement st= (IASTPreprocessorIfdefStatement) s;
		assertEquals(condition, new String(st.getCondition()));
		assertEquals(active, st.taken());
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkIfndef(IASTPreprocessorStatement s, String directive, String condition, boolean active,
			String filename, int offset, int length, int line) {
		IASTPreprocessorIfndefStatement st= (IASTPreprocessorIfndefStatement) s;
		assertEquals(condition, new String(st.getCondition()));
		assertEquals(active, st.taken());
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkElif(IASTPreprocessorStatement s, String directive, String condition, boolean active,
			String filename, int offset, int length, int line) {
		IASTPreprocessorElifStatement st= (IASTPreprocessorElifStatement) s;
		assertEquals(condition, new String(st.getCondition()));
		assertEquals(active, st.taken());
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkElse(IASTPreprocessorStatement s, String directive, boolean active,
			String filename, int offset, int length, int line) {
		IASTPreprocessorElseStatement st= (IASTPreprocessorElseStatement) s;
		assertEquals(active, st.taken());
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkEndif(IASTPreprocessorStatement s, String directive,
			String filename, int offset, int length, int line) {
		IASTPreprocessorEndifStatement st= (IASTPreprocessorEndifStatement) s;
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, directive);
	}

	private void checkMacroDefinition(IASTPreprocessorMacroDefinition macro, IMacroBinding binding, String image, String name, 
			String nameImage, String expansion, String[] parameters, 
			String filename, int offset, int length, int line, int nameOffset, int nameLength) {
		assertEquals(expansion, macro.getExpansion());
		checkName(macro.getName(), binding, name, macro, IASTPreprocessorMacroDefinition.MACRO_NAME, ROLE_DEFINITION, filename, nameOffset, nameLength, line, line, nameImage);
		checkASTNode(macro, fTu, PROP_PST, filename, offset, length, line, line, image);
		
		if (parameters != null) {
			IASTPreprocessorFunctionStyleMacroDefinition fd= (IASTPreprocessorFunctionStyleMacroDefinition) macro;
			IASTFunctionStyleMacroParameter[] params= fd.getParameters();
			for (int i = 0; i < params.length; i++) {
				IASTFunctionStyleMacroParameter mp = params[i];
				assertEquals(parameters[i], mp.getParameter());
				checkASTNode(mp, fd, IASTPreprocessorFunctionStyleMacroDefinition.PARAMETER, filename, -1, 0, -1, 0, null);
			}
		}
		IASTName[] decls= fLocationMap.getDeclarations(binding);
		assertEquals(1, decls.length);
		assertSame(macro.getName(), decls[0]);
	}

	private void checkMacroUndef(IASTPreprocessorStatement s, IBinding binding, String image, String name, String nameImage, 
			String filename, int offset, int length, int line, int nameOffset, int nameLength) {
		IASTPreprocessorUndefStatement st= (IASTPreprocessorUndefStatement) s;
		checkName(st.getMacroName(), binding, name, st, IASTPreprocessorStatement.MACRO_NAME, ROLE_UNCLEAR, filename, nameOffset, nameLength, line, line, nameImage);
		checkASTNode(st, fTu, PROP_PST, filename, offset, length, line, line, image);
	}

	public void testComment() {
		init(DIGITS);
		fLocationMap.encounteredComment(0, 0, false);
		fLocationMap.encounteredComment(1,3, true);
		fLocationMap.encounteredComment(5,16,true);
		IASTComment[] comments= fLocationMap.getComments();
		assertEquals(3, comments.length);
		checkComment(comments[0], "", false, FN, 0, 0, 1, 1);
		checkComment(comments[1], "12", true, FN, 1,2,1,1);
		checkComment(comments[2], "56789abcdef", true, FN, 5,11,1,1);
	}


	public void testProblems() {
		init(DIGITS);
		fLocationMap.encounterProblem(0, null, 0, 0);
		fLocationMap.encounterProblem(1, "a".toCharArray(), 1, 3);
		fLocationMap.encounterProblem(2, "b".toCharArray(), 5,16);
		IASTProblem[] problems= fLocationMap.getScannerProblems();
		assertEquals(3, problems.length);
		checkProblem(problems[0], 0, null, "", FN, 0,0,1,1);
		checkProblem(problems[1], 1, "a", "12", FN,1,2,1,1);
		checkProblem(problems[2], 2, "b", "56789abcdef", FN,5,11,1,1);
	}

	public void testPoundError() {
		init(DIGITS);
		fLocationMap.encounterPoundError(0, 0, 0, 0);
		fLocationMap.encounterPoundError(0, 1, 3, 16);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkError(prep[0], "", "",FN,0,0,1);
		checkError(prep[1], "012", "12", FN,0,3,1);
	}

	public void testPragma() {
		init(DIGITS);
		fLocationMap.encounterPoundPragma(0, 0, 0, 0);
		fLocationMap.encounterPoundPragma(0, 1, 3, 16);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkPragma(prep[0], "", "", FN,0,0,1);
		checkPragma(prep[1], "012", "12", FN,0,3,1);
	}

	public void testIncludes() {
		init(DIGITS);
		fLocationMap.encounterPoundInclude(0, 0, 0, 0, "n1".toCharArray(), null, true, false, false, null);
		fLocationMap.encounterPoundInclude(0, 1, 3, 16, "n2".toCharArray(), "f2", false , true, false, null);
		IASTPreprocessorIncludeStatement[] includes= fLocationMap.getIncludeDirectives();
		assertEquals(2, includes.length);
		checkInclude(includes[0], "", "", "n1", "", true, false, FN, 0, 0, 1, 0, 0);
		checkInclude(includes[1], new String(DIGITS), "12", "n2", "f2", false, true, FN, 0, 16, 1, 1, 2);
	}

	public void testIf() {
		init(DIGITS);
		fLocationMap.encounterPoundIf(0, 0, 0, 0, false, IASTName.EMPTY_NAME_ARRAY);
		fLocationMap.encounterPoundIf(0, 1, 3, 16, true, IASTName.EMPTY_NAME_ARRAY);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkIf(prep[0], "", "", false, FN, 0, 0, 1);
		checkIf(prep[1], "012", "12", true, FN, 0, 3, 1);
	}

	public void testIfdef() {
		init(DIGITS);
		fLocationMap.encounterPoundIfdef(0, 0, 0, 0, false, null);
		fLocationMap.encounterPoundIfdef(0, 1, 3, 16, true, null);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkIfdef(prep[0], "", "", false, FN, 0, 0, 1);
		checkIfdef(prep[1], "012", "12", true, FN, 0, 3, 1);
	}

	public void testIfndef() {
		init(DIGITS);
		fLocationMap.encounterPoundIfndef(0, 0, 0, 0, false, null);
		fLocationMap.encounterPoundIfndef(0, 1, 3, 16, true, null);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkIfndef(prep[0], "", "", false, FN, 0, 0, 1);
		checkIfndef(prep[1], "012", "12", true, FN, 0, 3, 1);
	}

	public void testElif() {
		init(DIGITS);
		fLocationMap.encounterPoundElif(0, 0, 0, 0, false, IASTName.EMPTY_NAME_ARRAY);
		fLocationMap.encounterPoundElif(0, 1, 3, 16, true, IASTName.EMPTY_NAME_ARRAY);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkElif(prep[0], "", "", false, FN, 0, 0, 1);
		checkElif(prep[1], "012", "12", true, FN, 0, 3, 1);
	}

	public void testElse() {
		init(DIGITS);
		fLocationMap.encounterPoundElse(0, 0, false);
		fLocationMap.encounterPoundElse(0, 16, true);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkElse(prep[0], "", false, FN, 0, 0, 1);
		checkElse(prep[1], new String(DIGITS),  true, FN, 0, 16, 1);
	}

	public void testEndif() {
		init(DIGITS);
		fLocationMap.encounterPoundEndIf(0, 0);
		fLocationMap.encounterPoundEndIf(0, 16);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkEndif(prep[0], "", FN, 0, 0, 1);
		checkEndif(prep[1], new String(DIGITS), FN, 0, 16, 1);
	}

	public void testDefine() {
		IMacroBinding macro1= new TestMacro("n1", "exp1", null);
		final String[] params = new String[]{"p1", "p2"};
		IMacroBinding macro2= new TestMacro("n2", "exp2", params);
		init(DIGITS);
		fLocationMap.encounterPoundDefine(0, 0, 0, 0, 0, true, macro1);
		fLocationMap.encounterPoundDefine(0, 1, 3, 10, 16, true, macro2);
		IASTPreprocessorMacroDefinition[] prep= fLocationMap.getMacroDefinitions();
		assertEquals(2, prep.length);
		checkMacroDefinition(prep[0], macro1, "", "n1", "", "exp1", null, FN, 0, 0, 1, 0, 0);
		checkMacroDefinition(prep[1], macro2, new String(DIGITS), "n2", "12", "exp2", params, FN, 0, 16, 1, 1, 2);
	}

	public void testPredefine() {
		IMacroBinding macro1= new TestMacro("n1", "exp1", null);
		final String[] params = new String[]{"p1", "p2"};
		IMacroBinding macro2= new TestMacro("n2", "exp2", params);
		init(DIGITS);
		fLocationMap.registerPredefinedMacro(macro1);
		fLocationMap.registerPredefinedMacro(macro2);
		IASTPreprocessorMacroDefinition[] prep= fLocationMap.getBuiltinMacroDefinitions();
		assertEquals(2, prep.length);
		checkMacroDefinition(prep[0], macro1, "", "n1", "n1", "exp1", null, "", -1, 0, 0, -1, 0);
		checkMacroDefinition(prep[1], macro2, "", "n2", "n2", "exp2", params, "", -1, 0, 0, -1, 0);
	}

	public void testIndexDefine() {
		IMacroBinding macro1= new TestMacro("n1", "exp1", null);
		final String[] params = new String[]{"p1", "p2"};
		IMacroBinding macro2= new TestMacro("n2", "exp2", params);
		init(DIGITS);
		fLocationMap.registerMacroFromIndex(macro1, new Loc("fidx1", 0, 0), 0);
		fLocationMap.registerMacroFromIndex(macro2, new Loc("fidx2", 1, 4), 8);
		IASTPreprocessorMacroDefinition[] prep= fLocationMap.getBuiltinMacroDefinitions();
		assertEquals(2, prep.length);
		checkMacroDefinition(prep[0], macro1, "", "n1", "n1", "exp1", null, "fidx1", -1, 0, 0, 0, 0);
		checkMacroDefinition(prep[1], macro2, "", "n2", "n2", "exp2", params, "fidx2", -1, 0, 0, 1, 3);
	}

	public void testUndefine() {
		IMacroBinding macro1= new TestMacro("n1", "exp1", null);

		init(DIGITS);
		fLocationMap.encounterPoundUndef(null, 0, 0, 0, 0, "n1".toCharArray(), true);
		fLocationMap.encounterPoundUndef(macro1, 0, 3, 7, 16, "n2".toCharArray(), true);
		IASTPreprocessorStatement[] prep= fLocationMap.getAllPreprocessorStatements();
		assertEquals(2, prep.length);
		checkMacroUndef(prep[0], null, "", "n1", "", FN, 0, 0, 1, 0, 0);
		checkMacroUndef(prep[1], macro1, "0123456", "n2", "3456", FN, 0, 7, 1, 3, 4);
	}

	public void testMacroExpansion() {
		IMacroBinding macro1= new TestMacro("n1", "exp1", null);
		IMacroBinding macro2= new TestMacro("n2", "exp2", null);
		IMacroBinding macro3= new TestMacro("n3", "exp3", null);
		init(LONGDIGITS);
		assertEquals(1, fLocationMap.getCurrentLineNumber('\n'));
		assertEquals(2, fLocationMap.getCurrentLineNumber('\n'+1));
		fLocationMap.registerPredefinedMacro(macro1);
		fLocationMap.registerMacroFromIndex(macro2, new Loc("ifile", 2, 12), 32);
		fLocationMap.encounterPoundDefine(3, 13, 33, 63, 103, true, macro3);
		IASTName name1= fLocationMap.encounterImplicitMacroExpansion(macro1, null);
		IASTName name2= fLocationMap.encounterImplicitMacroExpansion(macro2, null);
		ILocationCtx me = fLocationMap.pushMacroExpansion(110, 115, 125, 30, macro3, new IASTName[]{name1, name2}, new ImageLocationInfo[0]);
		// Comment in expansion
		fLocationMap.encounteredComment(116, 120, false);
		// Comment right after expansion, reported before expansion completes.
		fLocationMap.encounteredComment(125, 140, false);
		fLocationMap.popContext(me);
		checkComment(fLocationMap.getComments()[0], new String(LONGDIGITS, 116, 4), false, FN, 116, 4, 2, 2);
		checkComment(fLocationMap.getComments()[1], new String(LONGDIGITS, 125, 15), false, FN, 125, 15, 2, 2);
		
		IASTName[] refs= fLocationMap.getReferences(macro3);
		assertEquals(1, refs.length);
		IASTName macro3ref= refs[0];
		checkName(refs[0], macro3, "n3", refs[0].getParent(), IASTPreprocessorMacroExpansion.EXPANSION_NAME, ROLE_REFERENCE, FN, 110, 5, 2, 2, new String(LONGDIGITS, 110, 5));

		refs= fLocationMap.getReferences(macro1);
		assertEquals(1, refs.length);
		checkName(refs[0], macro1, "n1", refs[0].getParent(), IASTPreprocessorMacroExpansion.NESTED_EXPANSION_NAME, ROLE_REFERENCE, FN, 110, 15, 2, 2, new String(LONGDIGITS, 110, 15));

		refs= fLocationMap.getReferences(macro2);
		assertEquals(1, refs.length);
		checkName(refs[0], macro2, "n2", refs[0].getParent(), IASTPreprocessorMacroExpansion.NESTED_EXPANSION_NAME, ROLE_REFERENCE, FN, 110, 15, 2, 2, new String(LONGDIGITS, 110, 15));
	}
	
	public void testContexts() {
		init(DIGITS);
		assertEquals(FN, fLocationMap.getTranslationUnitPath());
		assertEquals(FN, fLocationMap.getCurrentFilePath());
		// number: [30,36)[46,50)
		ILocationCtx pre1= fLocationMap.pushPreInclusion(new CharArray("0102030405"), 0, false);
		assertEquals(FN, fLocationMap.getCurrentFilePath());
		// number: [0,6)[26,30)
		ILocationCtx pre2= fLocationMap.pushPreInclusion(new CharArray("a1a2a3a4a5"), 0, true);
		assertEquals(FN, fLocationMap.getCurrentFilePath());
		// number: [6,15)[25,26)
		ILocationCtx i1= fLocationMap.pushInclusion(0, 2, 4, 6, new CharArray("b1b2b3b4b5"), "pre1", "pre1".toCharArray(), false, false, false);
		assertEquals("pre1", fLocationMap.getCurrentFilePath());
		fLocationMap.encounteredComment(2,4,true);
		// number: [15,25)
		ILocationCtx i2= fLocationMap.pushInclusion(6, 7, 8, 9, new CharArray("c1c2c3c4c5"), "pre11", "pre11".toCharArray(), false, false, false);
		assertEquals("pre11", fLocationMap.getCurrentFilePath());
		fLocationMap.encounteredComment(2,6,true);
		fLocationMap.popContext(i2);
		// add a comment before the include
		fLocationMap.encounteredComment(4,6,false);

		assertEquals("pre1", fLocationMap.getCurrentFilePath());
		fLocationMap.popContext(i1);
		assertEquals(FN, fLocationMap.getCurrentFilePath());
		fLocationMap.popContext(pre2);
		assertEquals(FN, fLocationMap.getCurrentFilePath());
		// number [36, 46)
		ILocationCtx i3= fLocationMap.pushInclusion(0, 2, 4, 6, new CharArray("d1d2d3d4d5"), "pre2", "pre2".toCharArray(), false, false, false);
		assertEquals("pre2", fLocationMap.getCurrentFilePath());
		fLocationMap.encounteredComment(0,2,true);
		fLocationMap.popContext(i3);
		fLocationMap.popContext(pre1);
		assertEquals(FN, fLocationMap.getCurrentFilePath());
		
		
		IASTComment[] comments= fLocationMap.getComments();
		checkComment(comments[0], "b2", true, "pre1", 2, 2, 1, 1);
		checkComment(comments[1], "c2c3", true, "pre11", 2, 4, 1, 1);
		checkComment(comments[2], "b3", false, "pre1", 4, 2, 1, 1);		
		checkComment(comments[3], "d1", true, "pre2", 0, 2, 1, 1);

		checkLocation(fLocationMap.getMappedFileLocation(0, 6), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(6, 9), "pre1", 0, 9, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(15, 10), "pre11", 0, 10, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(25, 1), "pre1", 9, 1, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(26, 4), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(30, 6), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(36, 10), "pre2", 0, 10, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(46, 4), FN, 0, 0, 1, 1);

		checkLocation(fLocationMap.getMappedFileLocation(0, 0), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(5, 0), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(6, 0), "pre1", 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(14, 0), "pre1", 8, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(15, 0), "pre11", 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(24, 0), "pre11", 9, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(25, 0), "pre1", 9, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(26, 0), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(29, 0), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(30, 0), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(35, 0), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(36, 0), "pre2", 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(45, 0), "pre2", 9, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(46, 0), FN, 0, 0, 1, 1);
		
		checkLocation(fLocationMap.getMappedFileLocation(0, 7), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(6, 10), "pre1", 0, 9, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(6, 20), "pre1", 0, 10, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(15, 11), "pre1", 6, 4, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(25, 2), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(26, 5), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(30, 7), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(36, 11), FN, 0, 0, 1, 1);
		checkLocation(fLocationMap.getMappedFileLocation(46, 5), FN, 0, 1, 1, 1);
		
		IDependencyTree tree= fLocationMap.getDependencyTree();
		assertEquals(FN, tree.getTranslationUnitPath());
		IASTInclusionNode[] inclusions= tree.getInclusions();
		assertEquals(2, inclusions.length);
		checkInclude(inclusions[0].getIncludeDirective(), "", "", "pre1", "pre1", false, true, FN, 0, 0, 1, 0, 0);
		checkInclude(inclusions[1].getIncludeDirective(), "", "", "pre2", "pre2", false, true, FN, 0, 0, 1, 0, 0);
		assertEquals(0, inclusions[1].getNestedInclusions().length);
		
		inclusions= inclusions[0].getNestedInclusions();
		assertEquals(1, inclusions.length);
		checkInclude(inclusions[0].getIncludeDirective(), "b4b", "4", "pre11", "pre11", false, true, "pre1", 6, 3, 1, 7, 1);
		assertEquals(0, inclusions[0].getNestedInclusions().length);
	}
}
