/**********************************************************************
 * Copyright (c) 2004 IBM - Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

/*
 * Created on Jun 8, 2004
 */
package org.eclipse.cdt.internal.core.parser.ast.complete.gcc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.internal.core.parser.DeclarationWrapper;
import org.eclipse.cdt.internal.core.parser.Declarator;
import org.eclipse.cdt.internal.core.parser.ast.GCCASTExtension;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTBinaryExpression;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTTypeIdExpression;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTUnaryExpression;
import org.eclipse.cdt.internal.core.parser.ast.complete.ExpressionFactory;
import org.eclipse.cdt.internal.core.parser.token.ImagedToken;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;

/**
 * @author aniefer
 */
public class GCCASTCompleteExtension extends GCCASTExtension {

	private static final char [] __BUILTIN_VA_LIST = "__builtin_va_list".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_EXPECT  = "__builtin_expect".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_PREFETCH  = "__builtin_prefetch".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_HUGE_VAL  = "__builtin_huge_val".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_HUGE_VALF = "__builtin_huge_valf".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_HUGE_VALL = "__builtin_huge_vall".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_INF  = "__builtin_inf".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_INFF = "__builtin_inff".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_INFL = "__builtin_infl".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_NAN  = "__builtin_nan".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_NANF  = "__builtin_nanf".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_NANL  = "__builtin_nanl".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_NANS  = "__builtin_nans".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_NANSF  = "__builtin_nansf".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_NANSL  = "__builtin_nansl".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_FFS    = "__builtin_ffs".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_CLZ    = "__builtin_clz".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_CTZ    = "__builtin_ctz".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_POPCOUNT = "__builtin_popcount".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_PARITY = "__builtin_parity".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_FFSL   = "__builtin_ffsl".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_CLZL   = "__builtin_clzl".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_CTZL   = "__builtin_ctzl".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_POPCOUNTL = "__builtin_popcountl".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_PARITYL   = "__builtin_parityl".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_FFSLL   = "__builtin_ffsll".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_CLZLL   = "__builtin_clzll".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_CTZLL   = "__builtin_ctzll".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_POPCOUNTLL = "__builtin_popcountll".toCharArray(); //$NON-NLS-1$
	private static final char [] __BUILTIN_PARITYLL   = "__builtin_parityll".toCharArray(); //$NON-NLS-1$	
	
	/**
	 * @param mode
	 */
	public GCCASTCompleteExtension(ParserMode mode) {
		super(mode);
	}

	public boolean overrideCreateExpressionMethod() {
		if( mode == ParserMode.STRUCTURAL_PARSE || mode == ParserMode.COMPLETE_PARSE )
			return true;
		return false;
	}
	
	protected IASTExpression createExpression(IASTExpression.Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, ITokenDuple idExpression, char[] literal, IASTNewExpressionDescriptor newDescriptor, List references) {			
		if( lhs != null && rhs != null && 
		   (kind == IASTGCCExpression.Kind.RELATIONAL_MAX || 
			kind == IASTGCCExpression.Kind.RELATIONAL_MIN ) )
		{
			return new ASTBinaryExpression( kind, references, lhs, rhs ){
				public String toString(){
					IASTExpression.Kind k = getExpressionKind();
					StringBuffer buffer = new StringBuffer();
					buffer.append( ASTUtil.getExpressionString( getLHSExpression() ) );
					if( k == IASTGCCExpression.Kind.RELATIONAL_MAX )
						buffer.append( " >? " ); //$NON-NLS-1$
					else 
						buffer.append( " <? " ); //$NON-NLS-1$
					buffer.append( ASTUtil.getExpressionString( getRHSExpression() ) );
					return buffer.toString();
				}
			};
		}
		else if( lhs != null &&
				(kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION ||
				 kind == IASTGCCExpression.Kind.UNARY_TYPEOF_UNARYEXPRESSION) )
		{
			return new ASTUnaryExpression( kind, references, lhs ){
				public String toString(){
					IASTExpression.Kind k = getExpressionKind();
					StringBuffer buffer = new StringBuffer();
					if( k == IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION )
						buffer.append( GCCKeywords.__ALIGNOF__ );
					else
						buffer.append( GCCKeywords.TYPEOF );
					buffer.append( ' ' );
					buffer.append( ASTUtil.getExpressionString( getLHSExpression() ) );
					return buffer.toString();
				}
			};
		}
		else if( typeId != null && lhs == null &&
				(kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID ||
				 kind == IASTGCCExpression.Kind.UNARY_TYPEOF_TYPEID) )
		{
			return new ASTTypeIdExpression( kind, references, typeId ){
				public String toString(){
					IASTExpression.Kind k = getExpressionKind();
					StringBuffer buffer = new StringBuffer();
					if( k == IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID )
						buffer.append( GCCKeywords.__ALIGNOF__ );
					else
						buffer.append( GCCKeywords.TYPEOF );
					buffer.append( '(' );
					buffer.append( ASTUtil.getTypeId( getTypeId() ) );
					buffer.append( ')' );
					return buffer.toString();
				}
			};
		}
		
		return ExpressionFactory.createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, references );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#createExpression(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTTypeId, org.eclipse.cdt.core.parser.ITokenDuple, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor, java.util.List)
	 */
	public IASTExpression createExpression(IASTScope scope, Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, ITokenDuple idExpression, char[] literal, IASTNewExpressionDescriptor newDescriptor, List references) {
		if( canHandleExpressionKind( kind ) )
			return createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, references );
		
		return ExpressionFactory.createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, references );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#initialize(org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable)
	 */
	public void initialize(IASTFactory factory, IASTCompilationUnit compilationUnit) {
		__builtin_va_list( factory, compilationUnit );
		__builtin_expect( factory, compilationUnit );
        __builtin_prefetch( factory, compilationUnit );
        __builtin_huge_val( factory, compilationUnit );
        __builtin_inf( factory, compilationUnit );
        __builtin_nan( factory, compilationUnit );
        __builtin_unsigned_int( factory, compilationUnit );
        __builtin_unsigned_long( factory, compilationUnit );
        __builtin_unsigned_long_long( factory, compilationUnit );
	}

    /**
     * @param factory
     * @param compilationUnit
     */
    private void __builtin_inf( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        try {
            //double __builtin_inf (void)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.DOUBLE, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_INF, __BUILTIN_INF.length, EMPTY_STRING, 0 ) );
		    DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
		    param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.VOID, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP ) );
			param.addDeclarator( new Declarator( param ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //float __builtin_inff (void)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.FLOAT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_INFF, __BUILTIN_INFF.length, EMPTY_STRING, 0 ) );
		    DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
		    param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.VOID, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP ) );
			param.addDeclarator( new Declarator( param ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //long double __builtin_infl (void)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.DOUBLE, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, true, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_INFL, __BUILTIN_INFL.length, EMPTY_STRING, 0 ) );
		    DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
		    param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.VOID, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP ) );
			param.addDeclarator( new Declarator( param ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
    }

    /**
     * @param factory
     * @param compilationUnit
     */
    private void __builtin_huge_val( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        try {
            //double __builtin_huge_val (void)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.DOUBLE, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_HUGE_VAL, __BUILTIN_HUGE_VAL.length, EMPTY_STRING, 0 ) );
		    DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
		    param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.VOID, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP ) );
			param.addDeclarator( new Declarator( param ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //float __builtin_huge_valf (void)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.FLOAT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_HUGE_VALF, __BUILTIN_HUGE_VALF.length, EMPTY_STRING, 0 ) );
		    DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
		    param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.VOID, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP ) );
			param.addDeclarator( new Declarator( param ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //long double __builtin_huge_vall (void)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.DOUBLE, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, true, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_HUGE_VALL, __BUILTIN_HUGE_VALL.length, EMPTY_STRING, 0 ) );
		    DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
		    param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.VOID, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP ) );
			param.addDeclarator( new Declarator( param ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
    }

    /**
     * @param factory
     * @param compilationUnit
     */
    private void __builtin_prefetch( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        try {
            //void __builtin_prefetch (const void *addr, ...)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.VOID, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_PREFETCH, __BUILTIN_PREFETCH.length, EMPTY_STRING, 0 ) );
		    DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
		    param.setTypeSpecifier( typeSpec );
		    param.setConst( true );
			Declarator declarator = new Declarator( param );
			declarator.addPointerOperator(ASTPointerOperator.POINTER);
		    param.addDeclarator( declarator );
		    d.addParameter( param );
		    d.setIsVarArgs( true );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
    }

    /**
     * @param factory
     * @param compilationUnit
     */
    private void __builtin_expect( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        try {
            //long __buildin_expect( long exp, long c )
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.INT, new SimpleToken( IToken.t_long, -1, EMPTY_STRING, -1 ), false, true, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_EXPECT, __BUILTIN_EXPECT.length, EMPTY_STRING, 0 ) );
		    DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
		    param.setTypeSpecifier( typeSpec );
		    param.addDeclarator( new Declarator( param ) );
		    d.addParameter( param );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
    }

    /**
     * @param factory
     * @param compilationUnit
     */
    private void __builtin_va_list( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        try
		{
			IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.CHAR, new SimpleToken( IToken.t_char, -1, EMPTY_STRING, -1), false, false, false, false, false, false, false, true, Collections.EMPTY_MAP );
			List pointers = new ArrayList( 1 );
			pointers.add( ASTPointerOperator.POINTER );
			IASTAbstractDeclaration abs = factory.createAbstractDeclaration( false, false, typeSpec, pointers, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null );
			factory.createTypedef( compilationUnit, __BUILTIN_VA_LIST, abs, -1, -1, -1, -1, -1, EMPTY_STRING );
		} catch( ASTSemanticException ase )	{ //nothing
		}
    }
    
    private void __builtin_nan( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        //const char *
        DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
	    try {
            param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.CHAR, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP ) );
        } catch ( ASTSemanticException e1 ) {//nothing
        }
	    Declarator paramDecl = new Declarator( param );
	    paramDecl.addPointerOperator( ASTPointerOperator.POINTER );
	    paramDecl.setConst(true);
		param.addDeclarator( paramDecl );
		
        try {
            //double __builtin_nan (const char * str)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.DOUBLE, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_NAN, __BUILTIN_NAN.length, EMPTY_STRING, 0 ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //float __builtin_nanf (const char * str)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.FLOAT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_NANF, __BUILTIN_NANF.length, EMPTY_STRING, 0 ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //long double __builtin_nanl (const char * str)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.DOUBLE, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, true, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_NANL, __BUILTIN_NANL.length, EMPTY_STRING, 0 ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //double __builtin_nans (const char * str)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.DOUBLE, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_NANS, __BUILTIN_NANS.length, EMPTY_STRING, 0 ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //float __builtin_nansf (const char * str)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.FLOAT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_NANSF, __BUILTIN_NANSF.length, EMPTY_STRING, 0 ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
        
        try {
            //long double __builtin_nansl (const char * str)
            DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.DOUBLE, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, true, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
		    Declarator d = new Declarator(sdw);
		    d.setIsFunction( true );
		    d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_NANSL, __BUILTIN_NANSL.length, EMPTY_STRING, 0 ) );
		    d.addParameter( param );
		    sdw.addDeclarator( d );
	        sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e ) { //nothing
        } catch ( BacktrackException e ) { //nothing
        }
    }
    
    private void __builtin_unsigned_int( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        //unsigned int
        DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
	    try {
            param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.INT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, true, false, false, false, false, Collections.EMPTY_MAP ) );
    		param.addDeclarator( new Declarator( param ) );
        } catch ( ASTSemanticException e1 ) {//nothing
        }
		
        DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
        Declarator d = new Declarator(sdw);
	    d.setIsFunction( true );
	    
	    d.addParameter( param );
	    sdw.addDeclarator( d );

        try {
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.INT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
        } catch ( ASTSemanticException e ) { //nothing
        }

        try {//int __builtin_ffs(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_FFS, __BUILTIN_FFS.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) { //nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_cls(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_CLZ, __BUILTIN_CLZ.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_ctz(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_CTZ, __BUILTIN_CTZ.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_popcount(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_POPCOUNT, __BUILTIN_POPCOUNT.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }
        try {//int __builtin_parity(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_PARITY, __BUILTIN_PARITY.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }
    }
    
    private void __builtin_unsigned_long( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        //unsigned long
        DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
	    try {
            param.setTypeSpecifier( factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.INT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, true, false, true, false, false, false, false, Collections.EMPTY_MAP ) );
    		param.addDeclarator( new Declarator( param ) );
        } catch ( ASTSemanticException e1 ) {//nothing
        }
		
        DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
        Declarator d = new Declarator(sdw);
	    d.setIsFunction( true );
	    
	    d.addParameter( param );
	    sdw.addDeclarator( d );

        try {
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.INT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
        } catch ( ASTSemanticException e ) { //nothing
        }

        try {//int __builtin_ffsl(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_FFSL, __BUILTIN_FFSL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) { //nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_clsl(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_CLZL, __BUILTIN_CLZL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_ctzl(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_CTZL, __BUILTIN_CTZL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_popcountl(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_POPCOUNTL, __BUILTIN_POPCOUNTL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }
        try {//int __builtin_parityl(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_PARITYL, __BUILTIN_PARITYL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }
    }
    
    private void __builtin_unsigned_long_long( IASTFactory factory, IASTCompilationUnit compilationUnit ) {
        //unsigned long long
        DeclarationWrapper param = new DeclarationWrapper( compilationUnit, 0, 0, null, EMPTY_STRING );
	    try {
	        //TODO: this is just a long, we need to make it long long]
	        IASTSimpleTypeSpecifier spec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.INT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, true, false, true, false, false, false, false, Collections.EMPTY_MAP );
            param.setTypeSpecifier( spec );
    		param.addDeclarator( new Declarator( param ) );
        } catch ( ASTSemanticException e1 ) {//nothing
        }
		
        DeclarationWrapper sdw = new DeclarationWrapper(compilationUnit, 0, 0, null, EMPTY_STRING );
        Declarator d = new Declarator(sdw);
	    d.setIsFunction( true );
	    
	    d.addParameter( param );
	    sdw.addDeclarator( d );

        try {
            IASTSimpleTypeSpecifier typeSpec = factory.createSimpleTypeSpecifier( compilationUnit, IASTSimpleTypeSpecifier.Type.INT, new SimpleToken( IToken.t_void, -1, EMPTY_STRING, -1 ), false, false, false, false, false, false, false, false, Collections.EMPTY_MAP );
	        sdw.setTypeSpecifier( typeSpec );
        } catch ( ASTSemanticException e ) { //nothing
        }

        try {//int __builtin_ffsll(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_FFSLL, __BUILTIN_FFSLL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) { //nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_clsll(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_CLZLL, __BUILTIN_CLZLL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_ctzll(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_CTZLL, __BUILTIN_CTZLL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }

        try {//int __builtin_popcountll(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_POPCOUNTLL, __BUILTIN_POPCOUNTLL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }
        try {//int __builtin_parityll(unsigned int x)
            d.setName( new ImagedToken( IToken.tIDENTIFIER, __BUILTIN_PARITYLL, __BUILTIN_PARITYLL.length, EMPTY_STRING, 0 ) );
            sdw.createASTNodes( factory );
        } catch ( ASTSemanticException e2 ) {//nothing
        } catch ( BacktrackException e2 ) {//nothing
        }
    }
}
