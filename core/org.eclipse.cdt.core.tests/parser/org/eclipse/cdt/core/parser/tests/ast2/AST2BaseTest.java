/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 22, 2004
 */
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.parser2.ProblemCollector;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author aniefer
 */
public class AST2BaseTest extends TestCase {

    private static final ISourceElementRequestor NULL_REQUESTOR = new NullSourceElementRequestor();
    private static final IParserLogService NULL_LOG = new NullLogService();

    /**
     * @param string
     * @param c
     * @return
     * @throws ParserException
     */
    protected IASTTranslationUnit parse( String code, ParserLanguage lang ) throws ParserException {
        ProblemCollector collector = new ProblemCollector();
        IScanner scanner = ParserFactory.createScanner(new CodeReader(code
                .toCharArray()), new ScannerInfo(), ParserMode.COMPLETE_PARSE,
                lang, NULL_REQUESTOR,
                NULL_LOG, Collections.EMPTY_LIST);
        ISourceCodeParser parser2 = null;
        if( lang == ParserLanguage.CPP )
        {
            ICPPParserExtensionConfiguration config = null;
            config = new ANSICPPParserExtensionConfiguration();
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, collector,
                NULL_LOG,
                config );
        }
        else
        {
            ICParserExtensionConfiguration config = null;
             config = new ANSICParserExtensionConfiguration();
            
            parser2 = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, collector, 
                NULL_LOG, config );
        }
        IASTTranslationUnit tu = parser2.parse();
        if( parser2.encounteredError() )
            throw new ParserException( "FAILURE"); //$NON-NLS-1$
        
        assertTrue( collector.hasNoProblems() );
    
        return tu;
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
        IASTBinaryExpression e = (IASTBinaryExpression) getExpressionFromStatementInCode( code, ParserLanguage.C ); //$NON-NLS-1$
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

    static protected class CNameCollector extends CVisitor.CBaseVisitorAction {
        {
            processNames = true;
        }
        public List nameList = new ArrayList();
        public boolean processName( IASTName name ){
            nameList.add( name );
            return true;
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
        for( int i = 0; i < collector.size(); i++ )
            if( collector.getName( i ).resolveBinding() == binding )
                count++;
        
        assertEquals( count, num );
    }

    static protected class CPPNameCollector extends CPPVisitor.CPPBaseVisitorAction {
        {
            processNames = true;
        }
        public List nameList = new ArrayList();
        public boolean processName( IASTName name ){
            nameList.add( name );
            return true;
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
        
        assertEquals( count, num );
    }

}
