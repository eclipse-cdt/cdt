/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Nov 22, 2004
 */
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;

/**
 * @author aniefer
 */
public class AST2BaseTest extends BaseTestCase {
	
    private static final IParserLogService NULL_LOG = new NullLogService();

    public AST2BaseTest() {
    	super();
    }
    
    public AST2BaseTest(String name) {
    	super(name);
    }
    
    protected IASTTranslationUnit parse( String code, ParserLanguage lang ) throws ParserException {
    	return parse(code, lang, false, true );
    }
    
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions ) throws ParserException {
    	return parse( code, lang, useGNUExtensions, true );
    }
    /**
     * @param string
     * @param c
     * @return
     * @throws ParserException
     */
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException{
    	return parse(code, lang, useGNUExtensions, expectNoProblems, false);
    }
    
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems , boolean parseComments) throws ParserException {
        IScanner scanner = createScanner(new CodeReader(code.toCharArray()), lang, ParserMode.COMPLETE_PARSE, 
        		new ScannerInfo(), parseComments);
        ISourceCodeParser parser2 = null;
        if( lang == ParserLanguage.CPP )
        {
            ICPPParserExtensionConfiguration config = null;
            if (useGNUExtensions)
            	config = new GPPParserExtensionConfiguration();
            else
            	config = new ANSICPPParserExtensionConfiguration();
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG,config, null);
        }
        else
        {
            ICParserExtensionConfiguration config = null;

            if (useGNUExtensions)
            	config = new GCCParserExtensionConfiguration();
            else
            	config = new ANSICParserExtensionConfiguration();
            
            parser2 = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config, null);
        }
        
        IASTTranslationUnit tu = parser2.parse();

        if( parser2.encounteredError() && expectNoProblems )
            throw new ParserException( "FAILURE"); //$NON-NLS-1$
         
        if( lang == ParserLanguage.C && expectNoProblems )
        {
        	assertEquals( CVisitor.getProblems(tu).length, 0 );
        	assertEquals( tu.getPreprocessorProblems().length, 0 );
        }
        else if ( lang == ParserLanguage.CPP && expectNoProblems )
        {
        	assertEquals( CPPVisitor.getProblems(tu).length, 0 );
        	assertEquals( 0, tu.getPreprocessorProblems().length);
        }
        if( expectNoProblems )
            assertEquals( 0, tu.getPreprocessorProblems().length );
        
        
        return tu;
    }

	public static IScanner createScanner(CodeReader codeReader, ParserLanguage lang, ParserMode mode,
			ScannerInfo scannerInfo, boolean parseComments) {
		IScannerExtensionConfiguration configuration = null;
        if( lang == ParserLanguage.C )
            configuration = new GCCScannerExtensionConfiguration();
        else
            configuration = new GPPScannerExtensionConfiguration();
        IScanner scanner = new DOMScanner( codeReader, scannerInfo, mode, lang, NULL_LOG, configuration, FileCodeReaderFactory.getInstance() );
        scanner.setScanComments(parseComments);
		return scanner;
	}

    /**
     * @param string
     */
    protected void validateSimplePostfixInitializerExpressionC( String code ) throws ParserException {
        ICASTTypeIdInitializerExpression e = (ICASTTypeIdInitializerExpression) getExpressionFromStatementInCode(code, ParserLanguage.C );
        assertNotNull( e );
        assertNotNull( e.getTypeId() );
        assertNotNull( e.getInitializer() );
    }

    /**
     * @param string
     * @throws ParserException
     */
    protected void validateSimpleUnaryTypeIdExpression( String code, int op ) throws ParserException {
        IASTCastExpression e = (IASTCastExpression) getExpressionFromStatementInCode( code, ParserLanguage.C );
        assertNotNull( e );
        assertEquals( e.getOperator(), op );
        assertNotNull( e.getTypeId() );
        IASTIdExpression x = (IASTIdExpression) e.getOperand();
        assertEquals( x.getName().toString(), "x"); //$NON-NLS-1$
    }

    /**
     * @param code
     * @param op
     * @throws ParserException
     */
    protected void validateSimpleTypeIdExpressionC( String code, int op ) throws ParserException {
        IASTTypeIdExpression e = (IASTTypeIdExpression) getExpressionFromStatementInCode( code, ParserLanguage.C );
        assertNotNull( e );
        assertEquals( e.getOperator(), op );
        assertNotNull( e.getTypeId() );
    }

    /**
     * @param string
     * @param op_prefixIncr
     * @throws ParserException
     */
    protected void validateSimpleUnaryExpressionC( String code, int operator ) throws ParserException {
        IASTUnaryExpression e = (IASTUnaryExpression) getExpressionFromStatementInCode( code, ParserLanguage.C );
        assertNotNull( e );
        assertEquals( e.getOperator(), operator );
        IASTIdExpression x = (IASTIdExpression) e.getOperand();
        assertEquals( x.getName().toString(), "x"); //$NON-NLS-1$
    }

    /**
     * @param code 
     * @throws ParserException
     */
    protected void validateConditionalExpressionC( String code ) throws ParserException {
        IASTConditionalExpression e = (IASTConditionalExpression) getExpressionFromStatementInCode( code , ParserLanguage.C );
        assertNotNull( e );
        IASTIdExpression x = (IASTIdExpression) e.getLogicalConditionExpression();
        assertEquals( x.getName().toString(), "x" ); //$NON-NLS-1$
        IASTIdExpression y = (IASTIdExpression) e.getPositiveResultExpression();
        assertEquals( y.getName().toString(), "y"); //$NON-NLS-1$
        IASTIdExpression x2 = (IASTIdExpression) e.getNegativeResultExpression();
        assertEquals( x.getName().toString(), x2.getName().toString() );
    }

    /**
     * @param operand
     * @throws ParserException
     */
    protected void validateSimpleBinaryExpressionC( String code, int operand ) throws ParserException {
        IASTBinaryExpression e = (IASTBinaryExpression) getExpressionFromStatementInCode( code, ParserLanguage.C ); 
        assertNotNull( e );
        assertEquals( e.getOperator(), operand );
        IASTIdExpression x = (IASTIdExpression) e.getOperand1();
        assertEquals( x.getName().toString(), "x"); //$NON-NLS-1$
        IASTIdExpression y = (IASTIdExpression) e.getOperand2();
        assertEquals( y.getName().toString(), "y"); //$NON-NLS-1$
    }

    protected IASTExpression getExpressionFromStatementInCode( String code, ParserLanguage language ) throws ParserException {
        StringBuffer buffer = new StringBuffer( "void f() { "); //$NON-NLS-1$
        buffer.append( "int x, y;\n"); //$NON-NLS-1$
        buffer.append( code );
        buffer.append( ";\n}"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse( buffer.toString(), language );
        IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
        IASTCompoundStatement cs = (IASTCompoundStatement) f.getBody();
        IASTExpressionStatement s = (IASTExpressionStatement) cs.getStatements()[1];
        return s.getExpression();
    }

    static protected class CNameCollector extends CASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        public int visit( IASTName name ){
            nameList.add( name );
            return PROCESS_CONTINUE;
        }
        public IASTName getName( int idx ){
            if( idx < 0 || idx >= nameList.size() )
                return null;
            return (IASTName) nameList.get( idx );
        }
        public int size() { return nameList.size(); } 
    }

    protected void assertInstances( CNameCollector collector, IBinding binding, int num ) throws Exception {
        int count = 0;
        
        if (binding == null) assertTrue(false);
        
        for( int i = 0; i < collector.size(); i++ )
            if( collector.getName( i ).resolveBinding() == binding )
                count++;
        
        assertEquals( count, num );
    }

    static protected class CPPNameCollector extends CPPASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        public int visit( IASTName name ){
            nameList.add( name );
            return PROCESS_CONTINUE;
        }
        public IASTName getName( int idx ){
            if( idx < 0 || idx >= nameList.size() )
                return null;
            return (IASTName) nameList.get( idx );
        }
        public int size() { return nameList.size(); } 
    }

    protected void assertInstances( CPPNameCollector collector, IBinding binding, int num ) throws Exception {
        int count = 0;
        for( int i = 0; i < collector.size(); i++ )
            if( collector.getName( i ).resolveBinding() == binding )
                count++;
        
        assertEquals( num, count );
    }


	protected void isExpressionStringEqual(IASTExpression exp, String str) {
		String expressionString = ASTSignatureUtil.getExpressionString(exp);
		assertEquals(str, expressionString);
	}
	
	protected void isParameterSignatureEqual(IASTDeclarator decltor, String str) {
		assertEquals(str, ASTSignatureUtil.getParameterSignature(decltor));
	}
	
	protected void isSignatureEqual(IASTDeclarator decltor, String str) {
		assertEquals(str, ASTSignatureUtil.getSignature(decltor));
	}
	
	protected void isSignatureEqual(IASTDeclSpecifier declSpec, String str) {
		assertEquals(str, ASTSignatureUtil.getSignature(declSpec));
	}
	
	protected void isSignatureEqual(IASTTypeId typeId, String str) {
		assertEquals(str, ASTSignatureUtil.getSignature(typeId));
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
	
	static protected class CNameResolver extends CASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		public List nameList = new ArrayList();
		public int visit( IASTName name ){
			nameList.add( name );
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
		public IASTName getName( int idx ){
			if( idx < 0 || idx >= nameList.size() )
				return null;
			return (IASTName) nameList.get( idx );
		}
		public int size() { return nameList.size(); } 
	}
	
	static protected class CPPNameResolver extends CPPASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		public List nameList = new ArrayList();
		public int visit( IASTName name ){
			nameList.add( name );
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
		public IASTName getName( int idx ){
			if( idx < 0 || idx >= nameList.size() )
				return null;
			return (IASTName) nameList.get( idx );
		}
		public int size() { return nameList.size(); } 
	}
	
	protected StringBuffer[] getContents(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), sections);
	}
	
	protected static void assertInstance(Object o, Class c) {
		assertNotNull("Expected object of "+c.getName()+" but got a null value", o);
		assertTrue("Expected "+c.getName()+" but got "+o.getClass().getName(), c.isInstance(o));
	}
}
