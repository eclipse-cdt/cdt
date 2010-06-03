/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.POPCPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.POPCPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;

/**
 * Testcases for non-gnu language extensions.
 */
public class LanguageExtensionsTest extends AST2BaseTest {
	
	protected static final int SIZEOF_EXTENSION   = 0x1;
	protected static final int FUNCTION_STYLE_ASM = 0x2;
	protected static final int SLASH_PERCENT_COMMENT = 0x4;

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

	protected IASTTranslationUnit parse(String code, IScannerExtensionConfiguration sext,
			ICPPParserExtensionConfiguration pext) throws Exception {
		FileContent codeReader = FileContent.create("<test-code>", code.toCharArray());
		IScanner scanner = new CPreprocessor(codeReader, new ScannerInfo(), ParserLanguage.CPP, NULL_LOG,
				sext, IncludeFileContentProvider.getSavedFilesProvider());
		GNUCPPSourceParser parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, pext);
		return parse(parser);
	}

	protected IASTTranslationUnit parse(String code, IScannerExtensionConfiguration sext,
			ICParserExtensionConfiguration pext) throws Exception {
		FileContent codeReader = FileContent.create("<test-code>", code.toCharArray());
		IScanner scanner = new CPreprocessor(codeReader, new ScannerInfo(), ParserLanguage.C, NULL_LOG, sext,
				IncludeFileContentProvider.getSavedFilesProvider());
		GNUCSourceParser parser = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, pext);
		return parse(parser);
	}
	
	protected IASTTranslationUnit parseCPPWithExtension(String code, final int extensions) throws Exception {
		return parse(code,
			new GPPScannerExtensionConfiguration() {
				@Override
				public boolean supportSlashPercentComments() {
					return (extensions & SLASH_PERCENT_COMMENT) != 0;
				}
			},
			new GPPParserExtensionConfiguration() {
				@Override
				public boolean supportExtendedSizeofOperator() {
					return (extensions & SIZEOF_EXTENSION) != 0;
				}
				@Override
				public boolean supportFunctionStyleAssembler() {
					return (extensions & FUNCTION_STYLE_ASM) != 0;
				}
			}
		);
	}

	protected IASTTranslationUnit parseCWithExtension(String code, final int extensions) throws Exception {
		return parse(code, 
			new GCCScannerExtensionConfiguration() {
				@Override
				public boolean supportSlashPercentComments() {
					return (extensions & SLASH_PERCENT_COMMENT) != 0;
				}
			},
			new GCCParserExtensionConfiguration() {
				@Override
				public boolean supportExtendedSizeofOperator() {
					return (extensions & SIZEOF_EXTENSION) != 0;
				}
				@Override
				public boolean supportFunctionStyleAssembler() {
					return (extensions & FUNCTION_STYLE_ASM) != 0;
				}
			}
		);
	}

	// parclass ExampleClass {
    // };
    public void testPOP_parclass() throws Exception {
    	IASTTranslationUnit tu= parse(getAboveComment(), 
				POPCPPScannerExtensionConfiguration.getInstance(), 
				POPCPPParserExtensionConfiguration.getInstance()
		);
    	ICPPASTCompositeTypeSpecifier comp= getCompositeType(tu, 0);
    }
    
	// parclass Table {
    //    void sort([in, out, size=n] int *data, int n);
    // };
    public void testPOP_marshallingData() throws Exception {
    	IASTTranslationUnit tu= parse(getAboveComment(), 
				POPCPPScannerExtensionConfiguration.getInstance(), 
				POPCPPParserExtensionConfiguration.getInstance()
		);
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
    	IASTTranslationUnit tu= parse(getAboveComment(), 
				POPCPPScannerExtensionConfiguration.getInstance(), 
				POPCPPParserExtensionConfiguration.getInstance()
		);
    	ICPPASTCompositeTypeSpecifier comp= getCompositeType(tu, 0);
    	IASTSimpleDeclaration sd= getDeclaration(comp, 1);
    	assertInstance(sd.getDeclarators()[0], IASTFunctionDeclarator.class);
    }
    
    // @pack(Stack, Queue, List)
    // int a();
    public void testPOP_packDirective() throws Exception {
    	IASTTranslationUnit tu= parse(getAboveComment(), 
				POPCPPScannerExtensionConfiguration.getInstance(), 
				POPCPPParserExtensionConfiguration.getInstance()
		);
    	IASTSimpleDeclaration sd= getDeclaration(tu, 0);
    	assertInstance(sd.getDeclarators()[0], IASTFunctionDeclarator.class);
    }
    

    
    // void test() {
    // sizeof(int, 1);
    // sizeof(int, 2, 2);
    // }
    public void testSizeofExtension() throws Exception {
    	IASTTranslationUnit tu= parseCWithExtension(getAboveComment(), SIZEOF_EXTENSION);
    	IASTFunctionDefinition fdef= getDeclaration(tu, 0);
    	IASTUnaryExpression expr= getExpressionOfStatement(fdef, 0);
    	assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
    	assertInstance(expr.getOperand(), IASTExpressionList.class);
    	expr= getExpressionOfStatement(fdef, 1);
    	assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
    	assertInstance(expr.getOperand(), IASTExpressionList.class);

    	tu= parseCPPWithExtension(getAboveComment(), SIZEOF_EXTENSION);
    	fdef= getDeclaration(tu, 0);
    	expr= getExpressionOfStatement(fdef, 0);
    	assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
    	assertInstance(expr.getOperand(), IASTExpressionList.class);
    	expr= getExpressionOfStatement(fdef, 1);
    	assertEquals(IASTUnaryExpression.op_sizeof, expr.getOperator());
    	assertInstance(expr.getOperand(), IASTExpressionList.class);
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
    	IASTTranslationUnit tu= parseCWithExtension(getAboveComment(), FUNCTION_STYLE_ASM);
    	IASTFunctionDefinition fdef= getDeclaration(tu, 0);
    	fdef= getDeclaration(tu, 1);
    	fdef= getDeclaration(tu, 2);
    	fdef= getDeclaration(tu, 3);

    	tu= parseCPPWithExtension(getAboveComment(), FUNCTION_STYLE_ASM);
    	fdef= getDeclaration(tu, 0);
    	fdef= getDeclaration(tu, 1);
    	fdef= getDeclaration(tu, 2);
    	fdef= getDeclaration(tu, 3);
    }
    	
	// /% a comment %/
	// int a;
	public void testSlashPercentComment() throws Exception {
    	IASTTranslationUnit tu= parseCWithExtension(getAboveComment(), SLASH_PERCENT_COMMENT);

		IASTDeclaration d= getDeclaration(tu, 0);
		assertEquals("int a;", d.getRawSignature());
	}
}
