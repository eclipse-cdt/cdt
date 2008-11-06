/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.scanner.FileCodeReaderFactory;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;

/**
 * Testcases for non-gnu language extensions.
 */
public class LanguageExtensionsTest extends AST2BaseTest {
	
	public static TestSuite suite() {
		return suite(LanguageExtensionsTest.class);
	}
	
	public LanguageExtensionsTest() {
		super();
	}
	
	public LanguageExtensionsTest(String name) {
		super(name);
	}
	
	private IASTTranslationUnit parse(ISourceCodeParser parser) {
		IASTTranslationUnit tu= parser.parse();
		assertFalse(parser.encounteredError());
		assertEquals(0, tu.getPreprocessorProblemsCount());
		assertEquals(0, CPPVisitor.getProblems(tu).length);
		return tu;
	}

    private static GPPScannerExtensionConfiguration popScannerExt= new GPPScannerExtensionConfiguration() {
    	private CharArrayIntMap fAddKeywords;
    	private IMacro[] fAddMacros;

    	{
    		fAddKeywords= new CharArrayIntMap(10, -1);
    		fAddKeywords.putAll(super.getAdditionalKeywords());
    		fAddKeywords.put("parclass".toCharArray(), IToken.t_class);

    		IMacro[] macros= super.getAdditionalMacros();
    		int len= macros.length;

    		fAddMacros= new IMacro[len+4];
    		System.arraycopy(macros, 0, fAddMacros, 0, len);

    		fAddMacros[len++]= createMacro("@pack(...)", "");
    		fAddMacros[len++]= createMacro("__concat__(x,y)",  "x##y"); 
    		fAddMacros[len++]= createMacro("__xconcat__(x,y)",  "__concat__(x,y)"); 
    		fAddMacros[len++]= createMacro("@",  "; void __xconcat__(@, __LINE__)()");
    	}

    	@Override
    	public CharArrayIntMap getAdditionalKeywords() {
    		return fAddKeywords;
    	}

    	@Override
    	public IMacro[] getAdditionalMacros() {
    		return fAddMacros;
    	}
    };

	protected IASTTranslationUnit parsePOPCPP(String code) throws Exception {
		IScanner scanner= 
			new CPreprocessor(new CodeReader(code.toCharArray()), new ScannerInfo(), ParserLanguage.CPP, NULL_LOG, 
					popScannerExt, FileCodeReaderFactory.getInstance(), true);
		GNUCPPSourceParser parser= new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, new GPPParserExtensionConfiguration());
		parser.setSupportParameterInfoBlock(true);
		IASTTranslationUnit tu = parse(parser);
		return tu;
	}

    // parclass ExampleClass {
    // };
    public void testPOP_parclass() throws Exception {
    	IASTTranslationUnit tu= parsePOPCPP(getAboveComment());
    	ICPPASTCompositeTypeSpecifier comp= getCompositeType(tu, 0);
    }
    
    // parclass Table {
    //    void sort([in, out, size=n] int *data, int n);
    // };
    public void testPOP_marshallingData() throws Exception {
    	IASTTranslationUnit tu= parsePOPCPP(getAboveComment());
    	ICPPASTCompositeTypeSpecifier comp= getCompositeType(tu, 0);
    	IASTSimpleDeclaration sd= getDeclaration(comp, 0);
    	assertInstance(sd.getDeclarators()[0], IASTFunctionDeclarator.class);
    }
    
    //    parclass Bird {
    //    	public:
    //    		Bird(float P) @{ od.power(P);
    //    		od.memory(100,60);
    //    		od.protocol("socket http"); };
    //    };
    public void testPOP_objectDescriptor() throws Exception {
    	IASTTranslationUnit tu= parsePOPCPP(getAboveComment());
    	ICPPASTCompositeTypeSpecifier comp= getCompositeType(tu, 0);
    	IASTSimpleDeclaration sd= getDeclaration(comp, 1);
    	assertInstance(sd.getDeclarators()[0], IASTFunctionDeclarator.class);
    }
    
    // @pack(Stack, Queue, List)
    // int a();
    public void testPOP_packDirective() throws Exception {
    	IASTTranslationUnit tu= parsePOPCPP(getAboveComment());
    	IASTSimpleDeclaration sd= getDeclaration(tu, 0);
    	assertInstance(sd.getDeclarators()[0], IASTFunctionDeclarator.class);
    }
    
	protected IASTTranslationUnit parseWithSizeofExtension(String code, ICPPParserExtensionConfiguration ext) throws Exception {
		IScanner scanner= createScanner(new CodeReader(code.toCharArray()), ParserLanguage.CPP, 
				ParserMode.COMPLETE_PARSE, new ScannerInfo());
		GNUCPPSourceParser parser= new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, ext);
		parser.setSupportExtendedSizeofOperator(true);
		IASTTranslationUnit tu = parse(parser);
		return tu;
	}

	protected IASTTranslationUnit parseWithSizeofExtension(String code, ICParserExtensionConfiguration ext) throws Exception {
		IScanner scanner= createScanner(new CodeReader(code.toCharArray()), ParserLanguage.C, 
				ParserMode.COMPLETE_PARSE, new ScannerInfo());
		GNUCSourceParser parser= new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, ext);
		parser.setSupportExtendedSizeofOperator(true);
		IASTTranslationUnit tu = parse(parser);
		return tu;
	}

    
    // void test() {
    // sizeof(int, 1);
    // sizeof(int, 2, 2);
    // }
    public void testSizeofExtension() throws Exception {
    	IASTTranslationUnit tu= parseWithSizeofExtension(getAboveComment(), new GPPParserExtensionConfiguration());
    	IASTFunctionDefinition fdef= getDeclaration(tu, 0);
    	IASTUnaryExpression expr= getExpressionOfStatement(fdef, 0);
    	assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
    	assertInstance(expr.getOperand(), IASTExpressionList.class);
    	expr= getExpressionOfStatement(fdef, 1);
    	assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
    	assertInstance(expr.getOperand(), IASTExpressionList.class);

    	tu= parseWithSizeofExtension(getAboveComment(), new GCCParserExtensionConfiguration());
    	fdef= getDeclaration(tu, 0);
    	expr= getExpressionOfStatement(fdef, 0);
    	assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
    	assertInstance(expr.getOperand(), IASTExpressionList.class);
    	expr= getExpressionOfStatement(fdef, 1);
    	assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
    	assertInstance(expr.getOperand(), IASTExpressionList.class);
    }
    
    
	protected IASTTranslationUnit parseWithAsmExtension(String code, ICPPParserExtensionConfiguration ext) throws Exception {
		IScanner scanner= createScanner(new CodeReader(code.toCharArray()), ParserLanguage.CPP, 
				ParserMode.COMPLETE_PARSE, new ScannerInfo());
		GNUCPPSourceParser parser= new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, ext);
		parser.setSupportFunctionStyleAssembler(true);
		IASTTranslationUnit tu = parse(parser);
		return tu;
	}

	protected IASTTranslationUnit parseWithAsmExtension(String code, ICParserExtensionConfiguration ext) throws Exception {
		IScanner scanner= createScanner(new CodeReader(code.toCharArray()), ParserLanguage.C, 
				ParserMode.COMPLETE_PARSE, new ScannerInfo());
		GNUCSourceParser parser= new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, ext);
		parser.setSupportFunctionStyleAssembler(true);
		IASTTranslationUnit tu = parse(parser);
		return tu;
	}

    //  asm volatile int a1() {
    //     assembler code here
    //  }
    //  asm int a2() {
    //     assembler code here
    //  }
    //  asm volatile a3(int) {
    //     assembler code here
    //  }
    //  asm a4() {
    //     assembler code here
    //  }
    public void testFunctionStyleAssembler() throws Exception {
    	IASTTranslationUnit tu= parseWithAsmExtension(getAboveComment(), new GPPParserExtensionConfiguration());
    	IASTFunctionDefinition fdef= getDeclaration(tu, 0);
    	fdef= getDeclaration(tu, 1);
    	fdef= getDeclaration(tu, 2);
    	fdef= getDeclaration(tu, 3);

    	tu= parseWithAsmExtension(getAboveComment(), new GCCParserExtensionConfiguration());
    	fdef= getDeclaration(tu, 0);
    	fdef= getDeclaration(tu, 1);
    	fdef= getDeclaration(tu, 2);
    	fdef= getDeclaration(tu, 3);
    }
    
	protected IASTTranslationUnit parseSlashPercent(String code) throws Exception {
		IScanner scanner= 
			new CPreprocessor(new CodeReader(code.toCharArray()), new ScannerInfo(), ParserLanguage.CPP, NULL_LOG, 
					new GPPScannerExtensionConfiguration(), FileCodeReaderFactory.getInstance(), false, true);
		GNUCPPSourceParser parser= new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, new GPPParserExtensionConfiguration());
		IASTTranslationUnit tu = parse(parser);
		return tu;
	}
	
	// /% a comment %/
	// int a;
	public void testSlashPercentComment() throws Exception {
		IASTTranslationUnit tu= parseSlashPercent(getAboveComment());
		IASTDeclaration d= getDeclaration(tu, 0);
		assertEquals("int a;", d.getRawSignature());
	}
}
