package org.eclipse.cdt.internal.core.dom;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.IParserCallback;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;
import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * This is the parser callback that creates objects in the DOM.
 */
public class DOMBuilder implements IParserCallback 
{

	private TranslationUnit translationUnit;
	
	public TranslationUnit getTranslationUnit() {
		return translationUnit;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsBegin()
	 */
	public Object argumentsBegin( Object declarator ) {
		Declarator decl = ((Declarator)declarator);
		ParameterDeclarationClause clause = new ParameterDeclarationClause( decl ); 
		return clause; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsEnd()
	 */
	public void argumentsEnd(Object parameterDeclarationClause) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classBegin(java.lang.String, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		SimpleDeclaration decl = (SimpleDeclaration)container;
		
		int kind = ClassSpecifier.t_struct;
		int visibility = ClassSpecifier.v_public; 
		
		switch (classKey.getType()) {
			case Token.t_class:
				kind = ClassSpecifier.t_class;
				visibility = ClassSpecifier.v_private; 
				break;
			case Token.t_struct:
				kind = ClassSpecifier.t_struct;
				break;
			case Token.t_union:
				kind = ClassSpecifier.t_union;
				break;			
		}
		
		ClassSpecifier classSpecifier = new ClassSpecifier(kind, decl);
		classSpecifier.setCurrentVisibility( visibility );
		decl.setTypeSpecifier(classSpecifier);
		return classSpecifier;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classSpecifierName()
	 */
	public void classSpecifierName(Object classSpecifier) {
		((ClassSpecifier)classSpecifier).setName(currName);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classEnd()
	 */
	public void classSpecifierEnd(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorBegin()
	 */
	public Object declaratorBegin(Object container) {
		DeclSpecifier.Container decl = (DeclSpecifier.Container )container; 
		Declarator declarator = new Declarator(decl);
		decl.addDeclarator(declarator);
		return declarator; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorEnd()
	 */
	public void declaratorEnd(Object declarator) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void declaratorId(Object declarator) {
		((Declarator)declarator).setName(currName);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declSpecifier(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclSpecifier(Object Container, Token specifier) {
		DeclSpecifier.Container decl = (DeclSpecifier.Container)Container;
		DeclSpecifier declSpec = decl.getDeclSpecifier(); 
		if( declSpec == null )
		{
			declSpec = new DeclSpecifier(); 
			decl.setDeclSpecifier( declSpec ); 
		}

		declSpec.setType( specifier );		
	}



	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionOperator(Object expression, Token operator) throws Exception {
		Expression e = (Expression)expression;
		e.add( operator ); 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionTerminal(Object expression, Token terminal) throws Exception {
		Expression e = (Expression)expression;
		e.add( terminal );
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyBegin()
	 */
	public Object functionBodyBegin(Object declaration) {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyEnd()
	 */
	public void functionBodyEnd(Object functionBody) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionBegin(java.lang.String)
	 */
	public void inclusionBegin(String includeFile, int offset) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(java.lang.String)
	 */
	public void macro(String macroName, int offset) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object simpleDeclarationBegin(Object container) {
		SimpleDeclaration decl = new SimpleDeclaration();
		((IScope)container).addDeclaration(decl);
		return decl;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationEnd(Object declaration) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitBegin()
	 */
	public Object translationUnitBegin() {
		translationUnit = new TranslationUnit();
		return translationUnit; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitEnd()
	 */
	public void translationUnitEnd(Object unit) {
	}

	private Name currName;
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
		currName = new Name(firstToken);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
		currName.setEnd(lastToken);
	}

	public Object baseSpecifierBegin( Object classSpecifier )
	{
		ClassSpecifier cs =(ClassSpecifier)classSpecifier;
		BaseSpecifier baseSpec = new BaseSpecifier( cs );
		return baseSpec; 
	}

	public void baseSpecifierEnd( Object baseSpecifier  )
	{
		
	}

	public void baseSpecifierVirtual( Object baseSpecifier, boolean virtual )
	{
		BaseSpecifier bs = (BaseSpecifier)baseSpecifier; 
		bs.setVirtual( virtual );
	}

	public void baseSpecifierVisibility( Object baseSpecifier, Token visibility )
	{
		int access = BaseSpecifier.t_public;  
		switch( visibility.type )
		{
		case Token.t_public:
			access = BaseSpecifier.t_public; 
			break; 
		case Token.t_protected:
			access = BaseSpecifier.t_protected;		 
			break;
		case Token.t_private:
			access = BaseSpecifier.t_private;
			break; 		
		default: 
			break;
		}
	
		((BaseSpecifier)baseSpecifier).setAccess(access);  
	}

	
	public void baseSpecifierName( Object baseSpecifier )
	{
		((BaseSpecifier)baseSpecifier).setName(currName.toString());		
	}
	
	public Object parameterDeclarationBegin( Object container )
	{
		ParameterDeclarationClause clause = (ParameterDeclarationClause)container; 
		ParameterDeclaration pd = new ParameterDeclaration();
		clause.addDeclaration( pd ); 
		return pd;
	}
	
	public void  parameterDeclarationEnd( Object declaration ){
	}
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorAbort(java.lang.Object, java.lang.Object)
	 */
	public void declaratorAbort(Object container, Object declarator) {
		DeclSpecifier.Container decl = (DeclSpecifier.Container )container;
		Declarator toBeRemoved = (Declarator)declarator;
		decl.removeDeclarator( toBeRemoved ); 
		currName = null;
		toBeRemoved = null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionBegin(java.lang.Object)
	 */
	public Object expressionBegin(Object container) {
		IExpressionOwner owner = (IExpressionOwner)container;
		Expression expression = new Expression();
		owner.setExpression(expression); 
		return expression;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionEnd(java.lang.Object)
	 */
	public void expressionEnd(Object expression) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierAbort(java.lang.Object)
	 */
	public void classSpecifierAbort(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierSafe(java.lang.Object)
	 */
	public void classSpecifierSafe(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierBegin(java.lang.Object)
	 */
	public Object elaboratedTypeSpecifierBegin(Object container, Token classKey) {
		int kind = ClassSpecifier.t_struct;
		
		switch (classKey.getType()) {
			case Token.t_class:
				kind = ClassSpecifier.t_class;
				break;
			case Token.t_struct:
				kind = ClassSpecifier.t_struct;
				break;
			case Token.t_union:
				kind = ClassSpecifier.t_union;
				break;			
		}

		ElaboratedTypeSpecifier elab = new ElaboratedTypeSpecifier( kind, (SimpleDeclaration)container );
		return elab; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierEnd(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierEnd(Object elab) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierName(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierName(Object elab) {
		((ElaboratedTypeSpecifier)elab).setName( currName );
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifierName(java.lang.Object)
	 */
	public void simpleDeclSpecifierName(Object declaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionAbort(java.lang.Object)
	 */
	public void expressionAbort(Object expression) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classMemberVisibility(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void classMemberVisibility(Object classSpecifier, Token visibility) {
		ClassSpecifier spec = (ClassSpecifier)classSpecifier;
		switch( visibility.getType() )
		{
			case Token.t_public:
				spec.setCurrentVisibility( ClassSpecifier.v_public );
				break;
			case Token.t_protected:
				spec.setCurrentVisibility( ClassSpecifier.v_protected );
				break;
			case Token.t_private:
				spec.setCurrentVisibility( ClassSpecifier.v_private );
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object pointerOperatorBegin(Object container) {
		Declarator declarator = (Declarator)container;
		PointerOperator ptrOp = new PointerOperator(declarator);
		return ptrOp; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorEnd(java.lang.Object)
	 */
	public void pointerOperatorEnd(Object ptrOperator) {
		PointerOperator ptrOp = (PointerOperator)ptrOperator;
		Declarator owner = ptrOp.getOwnerDeclarator();
		owner.addPointerOperator( ptrOp );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorName(java.lang.Object)
	 */
	public void pointerOperatorName(Object ptrOperator) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorType(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorType(Object ptrOperator, Token type) {
		PointerOperator ptrOp = (PointerOperator)ptrOperator;
		switch( type.getType() )
		{
			case Token.tSTAR:
				ptrOp.setType( PointerOperator.t_pointer );
				break;
			case Token.tAMPER:
				ptrOp.setType( PointerOperator.t_reference );
				break;
			default:
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorCVModifier(Object ptrOperator, Token modifier) {
		PointerOperator ptrOp = (PointerOperator)ptrOperator;
		switch( modifier.getType() )
		{
			case Token.t_const:
				ptrOp.setConst(true);
				break; 
			case Token.t_volatile:
				ptrOp.setVolatile( true );
				break;
			default:
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void declaratorCVModifier(Object declarator, Token modifier) {
		Declarator decl = (Declarator)declarator;
		switch( modifier.getType() )
		{
			case Token.t_const:
				decl.setConst(true);
				break; 
			case Token.t_volatile:
				decl.setVolatile( true );
				break;
			default:
				break;
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayBegin(java.lang.Object)
	 */
	public Object arrayDeclaratorBegin(Object declarator) {
		Declarator decl = (Declarator)declarator;
		ArrayQualifier qual = new ArrayQualifier( decl );
		return qual;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayEnd(java.lang.Object)
	 */
	public void arrayDeclaratorEnd(Object arrayQualifier ) {
		ArrayQualifier qual = (ArrayQualifier)arrayQualifier; 
		Declarator parent = qual.getOwnerDeclarator(); 
		parent.addArrayQualifier(qual);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#exceptionSpecificationTypename(java.lang.Object)
	 */
	public void declaratorThrowExceptionName(Object declarator ) {
		Declarator decl = (Declarator)declarator; 
		decl.addExceptionSpecifierTypeName( currName ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorThrowsException(java.lang.Object)
	 */
	public void declaratorThrowsException(Object declarator) {
		Declarator decl = (Declarator)declarator; 
		decl.throwsExceptions(); 
	}
}