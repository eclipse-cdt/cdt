package org.eclipse.cdt.internal.core.dom;


import org.eclipse.cdt.internal.core.parser.IParserCallback;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.util.AccessSpecifier;
import org.eclipse.cdt.internal.core.parser.util.ClassKey;
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
		
		int kind = ClassKey.t_struct;
		int visibility = AccessSpecifier.v_public; 
		
		switch (classKey.getType()) {
			case Token.t_class:
				kind = ClassKey.t_class;
				visibility = AccessSpecifier.v_private; 
				break;
			case Token.t_struct:
				kind = ClassKey.t_struct;
				break;
			case Token.t_union:
				kind = ClassKey.t_union;
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
	public void classSpecifierEnd(Object classSpecifier, Token closingBrace) {
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
	public void expressionOperator(Object expression, Token operator){
		Expression e = (Expression)expression;
		e.add( operator ); 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionTerminal(Object expression, Token terminal){
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
	public void functionBodyEnd(Object functionBody ) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionBegin(java.lang.String)
	 */
	public void inclusionBegin(String includeFile, int offset, int inclusionBeginOffset) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(java.lang.String)
	 */
	public void macro(String macroName, int offset, int macroBeginOffset, int macroEndOffset) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object simpleDeclarationBegin(Object container, Token firstToken) {
		SimpleDeclaration decl = new SimpleDeclaration();
		((IScope)container).addDeclaration(decl);
		return decl;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationEnd(Object declaration, Token lastToken) {
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
		int access = AccessSpecifier.v_public;  
		switch( visibility.type )
		{
		case Token.t_public:
			access = AccessSpecifier.v_public; 
			break; 
		case Token.t_protected:
			access = AccessSpecifier.v_protected;		 
			break;
		case Token.t_private:
			access = AccessSpecifier.v_private;
			break; 		
		default: 
			break;
		}
	
		((BaseSpecifier)baseSpecifier).setAccess(access);  
	}

	
	public void baseSpecifierName( Object baseSpecifier )
	{
		((BaseSpecifier)baseSpecifier).setName(currName);		
	}
	
	public Object parameterDeclarationBegin( Object container )
	{
		IScope clause = (IScope)container; 
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
		ClassSpecifier cs = (ClassSpecifier)classSpecifier;
		cs.getDeclaration().setTypeSpecifier(null);
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
		SimpleDeclaration declaration = (SimpleDeclaration)container;
		int kind = ClassKey.t_struct;
		
		switch (classKey.getType()) {
			case Token.t_class:
				kind = ClassKey.t_class;
				break;
			case Token.t_struct:
				kind = ClassKey.t_struct;
				break;
			case Token.t_union:
				kind = ClassKey.t_union;
				break;			
		}

		ElaboratedTypeSpecifier elab = new ElaboratedTypeSpecifier( kind, declaration );
		declaration.setTypeSpecifier( elab );
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
				spec.setCurrentVisibility( AccessSpecifier.v_public );
				break;
			case Token.t_protected:
				spec.setCurrentVisibility( AccessSpecifier.v_protected );
				break;
			case Token.t_private:
				spec.setCurrentVisibility( AccessSpecifier.v_private );
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
		decl.getExceptionSpecifier().addTypeName( currName ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorThrowsException(java.lang.Object)
	 */
	public void declaratorThrowsException(Object declarator) {
		Declarator decl = (Declarator)declarator; 
		decl.getExceptionSpecifier().setThrowsException(true); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationBegin(java.lang.Object)
	 */
	public Object namespaceDefinitionBegin(Object container, Token namespace) {
		IScope ownerScope = (IScope)container;
		NamespaceDefinition namespaceDef = new NamespaceDefinition(ownerScope);
		return namespaceDef;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationId(java.lang.Object)
	 */
	public void namespaceDefinitionId(Object namespace) {
		NamespaceDefinition ns = (NamespaceDefinition)namespace;
		ns.setName( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationAbort(java.lang.Object)
	 */
	public void namespaceDefinitionAbort(Object namespace) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationEnd(java.lang.Object)
	 */
	public void namespaceDefinitionEnd(Object namespace, Token closingBrace) {
		NamespaceDefinition ns = (NamespaceDefinition)namespace; 
		ns.getOwnerScope().addDeclaration(ns);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationBegin(java.lang.Object, java.lang.String)
	 */
	public Object linkageSpecificationBegin(Object container, String literal) {
		IScope scope = (IScope)container; 
		LinkageSpecification linkage = new LinkageSpecification( scope, literal );
		return linkage; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationEnd(java.lang.Object)
	 */
	public void linkageSpecificationEnd(Object linkageSpec) {
		LinkageSpecification linkage = (LinkageSpecification)linkageSpec;
		linkage.getOwnerScope().addDeclaration(linkage );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveBegin(java.lang.Object)
	 */
	public Object usingDirectiveBegin(Object container) {
		IScope scope = (IScope)container;
		UsingDirective directive = new UsingDirective( scope );
		return directive;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveNamespaceId(java.lang.Object)
	 */
	public void usingDirectiveNamespaceId(Object dir) {
		UsingDirective directive = (UsingDirective)dir;
		directive.setNamespaceName( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveEnd(java.lang.Object)
	 */
	public void usingDirectiveEnd(Object dir) {
		UsingDirective directive = (UsingDirective)dir;
		directive.getOwnerScope().addDeclaration( directive );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationBegin(java.lang.Object)
	 */
	public Object usingDeclarationBegin(Object container) {
		IScope scope = (IScope)container;
		UsingDeclaration declaration = new UsingDeclaration( scope );
		return declaration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationMapping(java.lang.Object)
	 */
	public void usingDeclarationMapping(Object decl, boolean isTypename) {
		UsingDeclaration declaration = (UsingDeclaration)decl;
		declaration.setMappedName( currName );
		declaration.setTypename( isTypename );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationEnd(java.lang.Object)
	 */
	public void usingDeclarationEnd(Object decl) {
		UsingDeclaration declaration = (UsingDeclaration)decl;
		declaration.getOwnerScope().addDeclaration( declaration );		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveAbort(java.lang.Object)
	 */
	public void usingDirectiveAbort(Object directive) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationAbort(java.lang.Object)
	 */
	public void usingDeclarationAbort(Object declaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierBegin(java.lang.Object)
	 */
	public Object enumSpecifierBegin(Object container, Token enumKey) {
		SimpleDeclaration decl = (SimpleDeclaration)container;
		EnumerationSpecifier es = new EnumerationSpecifier( decl );
		decl.setTypeSpecifier(es); 
		return es;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierId(java.lang.Object)
	 */
	public void enumSpecifierId(Object enumSpec) {
		EnumerationSpecifier es = (EnumerationSpecifier)enumSpec;
		es.setName( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierAbort(java.lang.Object)
	 */
	public void enumSpecifierAbort(Object enumSpec) {
		EnumerationSpecifier es = (EnumerationSpecifier)enumSpec;
		es.getDeclaration().setTypeSpecifier(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierEnd(java.lang.Object)
	 */
	public void enumSpecifierEnd(Object enumSpec, Token closingBrace) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionBegin(java.lang.Object)
	 */
	public Object enumeratorBegin(Object enumSpec) {
		EnumerationSpecifier es = (EnumerationSpecifier)enumSpec;
		EnumeratorDefinition definition = new EnumeratorDefinition();
		es.addEnumeratorDefinition(definition);
		return definition; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionId(java.lang.Object)
	 */
	public void enumeratorId(Object enumDefn) {
		EnumeratorDefinition definition = (EnumeratorDefinition)enumDefn;
		definition.setName( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionEnd(java.lang.Object)
	 */
	public void enumeratorEnd(Object enumDefn, Token lastToken) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#asmDefinition(java.lang.String)
	 */
	public void asmDefinition(Object container, String assemblyCode) {
		IScope scope = (IScope)container;
		ASMDefinition definition = new ASMDefinition( assemblyCode );
		scope.addDeclaration( definition );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainBegin(java.lang.Object)
	 */
	public Object constructorChainBegin(Object declarator) {
		Declarator d = (Declarator)declarator; 
		ConstructorChain chain = new ConstructorChain(d); 
		return chain;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainAbort(java.lang.Object)
	 */
	public void constructorChainAbort(Object ctor) {
		ctor = null; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainEnd(java.lang.Object)
	 */
	public void constructorChainEnd(Object ctor) {
		ConstructorChain chain = (ConstructorChain)ctor; 
		chain.getOwnerDeclarator().setCtorChain(chain);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementBegin(Object ctor) {
		return new ConstructorChainElement( (ConstructorChain)ctor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementEnd(java.lang.Object)
	 */
	public void constructorChainElementEnd(Object element) {
		ConstructorChainElement ele = (ConstructorChainElement)element;
		ele.getOwnerChain().addChainElement( ele );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainId(java.lang.Object)
	 */
	public void constructorChainElementId(Object element) {
		ConstructorChainElement ele = (ConstructorChainElement)element;
		ele.setName(currName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementExpressionListElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementExpressionListElementBegin(Object element) {
		return new ConstructorChainElementExpression( (ConstructorChainElement)element );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementExpressionListElementEnd(java.lang.Object)
	 */
	public void constructorChainElementExpressionListElementEnd(Object expression) {
		ConstructorChainElementExpression exp = (ConstructorChainElementExpression)expression;
		exp.getOwnerElement().addExpression( exp );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationBegin(java.lang.Object)
	 */
	public Object explicitInstantiationBegin(Object container) {
		IScope scope = (IScope)container;
		ExplicitTemplateDeclaration etd = new ExplicitTemplateDeclaration( ExplicitTemplateDeclaration.k_instantiation ); 
		scope.addDeclaration(etd);
		return etd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationEnd(java.lang.Object)
	 */
	public void explicitInstantiationEnd(Object instantiation) {	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationBegin(java.lang.Object)
	 */
	public Object explicitSpecializationBegin(Object container) {
		IScope scope = (IScope)container;
		ExplicitTemplateDeclaration etd = new ExplicitTemplateDeclaration( ExplicitTemplateDeclaration.k_specialization); 
		scope.addDeclaration(etd);
		return etd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationEnd(java.lang.Object)
	 */
	public void explicitSpecializationEnd(Object instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorPureVirtual(java.lang.Object)
	 */
	public void declaratorPureVirtual(Object declarator) {
		Declarator d = (Declarator)declarator;
		d.setPureVirtual(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationBegin(java.lang.Object, boolean)
	 */
	public Object templateDeclarationBegin(Object container, boolean exported) {
		return new TemplateDeclaration( (IScope)container, exported );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationAbort(java.lang.Object)
	 */
	public void templateDeclarationAbort(Object templateDecl) {
		templateDecl = null; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationEnd(java.lang.Object)
	 */
	public void templateDeclarationEnd(Object templateDecl) {
		TemplateDeclaration decl = (TemplateDeclaration)templateDecl;
		decl.getOwnerScope().addDeclaration(decl);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object templateTypeParameterBegin(Object templDecl, Token kind) {
		TemplateParameterList list = (TemplateParameterList)templDecl;
		int k; 
		switch( kind.getType() )
		{
			case Token.t_class:
				k = TemplateParameter.k_class;
				break;
			case Token.t_typename:
				k= TemplateParameter.k_typename;
				break;
			case Token.t_template:
				k= TemplateParameter.k_template;
				break;
			default:
				k = 0;  
		}
		TemplateParameter p = new TemplateParameter( k );
		list.addDeclaration(p);
		return p;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterName(java.lang.Object)
	 */
	public void templateTypeParameterName(Object typeParm) {
		((TemplateParameter)typeParm).setName( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeInitialTypeId(java.lang.Object)
	 */
	public void templateTypeParameterInitialTypeId(Object typeParm) {
		((TemplateParameter)typeParm).setTypeId( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterEnd(java.lang.Object)
	 */
	public void templateTypeParameterEnd(Object typeParm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterAbort(java.lang.Object)
	 */
	public void templateTypeParameterAbort(Object typeParm) {
		typeParm = null; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorAbort(java.lang.Object)
	 */
	public void pointerOperatorAbort(Object ptrOperator) {
		ptrOperator = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateParameterListBegin(java.lang.Object)
	 */
	public Object templateParameterListBegin(Object declaration) {
		ITemplateParameterListOwner d = (ITemplateParameterListOwner)declaration;
		TemplateParameterList list = new TemplateParameterList(); 
		d.setTemplateParms(list);
		return list;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateParameterListEnd(java.lang.Object)
	 */
	public void templateParameterListEnd(Object parameterList) {
	}
}