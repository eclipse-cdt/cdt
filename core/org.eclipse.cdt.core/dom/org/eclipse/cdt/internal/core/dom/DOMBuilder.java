package org.eclipse.cdt.internal.core.dom;


import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserCallback;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedef;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.Name;

/**
 * This is the parser callback that creates objects in the DOM.
 */
public class DOMBuilder implements IParserCallback, ISourceElementRequestor
{
	public DOMBuilder()
	{
	}
	
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
     * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#oldKRParametersBegin()
     */
    public Object oldKRParametersBegin( Object parameterDeclarationClause ) {
        ParameterDeclarationClause clause = ((ParameterDeclarationClause)parameterDeclarationClause);
        OldKRParameterDeclarationClause KRclause = new OldKRParameterDeclarationClause( clause );
        domScopes.push(KRclause);
        return KRclause; 
    }

    /**
     * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#oldKRParametersEnd()
     */
    public void oldKRParametersEnd(Object oldKRParameterDeclarationClause) {
        domScopes.pop();
    }

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classBegin(java.lang.String, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object classSpecifierBegin(Object container, IToken classKey) {
		TypeSpecifier.IOwner decl = (TypeSpecifier.IOwner)container;
		
		int kind = ClassKey.t_struct;
		int visibility = AccessSpecifier.v_public; 
		
		switch (classKey.getType()) {
			case IToken.t_class:
				kind = ClassKey.t_class;
				visibility = AccessSpecifier.v_private; 
				break;
			case IToken.t_struct:
				kind = ClassKey.t_struct;
				break;
			case IToken.t_union:
				kind = ClassKey.t_union;
				break;			
		}
		
		ClassSpecifier classSpecifier = new ClassSpecifier(kind, decl);
		classSpecifier.setVisibility( visibility );
		classSpecifier.setStartingOffset( classKey.getOffset() );
		
		classSpecifier.setClassKeyToken( classKey );
		decl.setTypeSpecifier(classSpecifier);
		domScopes.push( classSpecifier );
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
	public void classSpecifierEnd(Object classSpecifier, IToken closingBrace) {
		ClassSpecifier c = (ClassSpecifier)classSpecifier;
		c.setTotalLength( closingBrace.getOffset() + closingBrace.getLength() - c.getStartingOffset() );
		domScopes.pop();
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorBegin()
	 */
	public Object declaratorBegin(Object container) {
		if( container instanceof DeclSpecifier.IContainer )
		{
			DeclSpecifier.IContainer decl = (DeclSpecifier.IContainer )container; 
			Declarator declarator = new Declarator(decl);
			return declarator;
		}
		else if( container instanceof IDeclaratorOwner )
		{
			IDeclaratorOwner owner = (IDeclaratorOwner)container;
			Declarator declarator = new Declarator(owner); 
			return declarator; 
		}
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorEnd()
	 */
	public void declaratorEnd(Object declarator) {
		Declarator d = (Declarator)declarator;
		if( d.getDeclaration() != null ) 
			d.getDeclaration().addDeclarator(d);
		else if( d.getOwnerDeclarator() != null )
			d.getOwnerDeclarator().setDeclarator(d); 
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
	public void simpleDeclSpecifier(Object Container, IToken specifier) {
		DeclSpecifier.IContainer decl = (DeclSpecifier.IContainer)Container;
		DeclSpecifier declSpec = decl.getDeclSpecifier(); 
		declSpec.setType( specifier );
	}



	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionOperator(Object expression, IToken operator){
		Expression e = (Expression)expression;
		e.add( operator ); 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionTerminal(Object expression, IToken terminal){
		Expression e = (Expression)expression;
		e.add( terminal );
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyBegin()
	 */
	public Object functionBodyBegin(Object declaration) {
		SimpleDeclaration simpleDec = (SimpleDeclaration)declaration;
		simpleDec.setFunctionDefinition(true);
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
	public Object inclusionBegin(String includeFile, int offset, int inclusionBeginOffset, boolean local) {
//		Inclusion inclusion = new Inclusion( 
//			includeFile, 
//			offset, 
//			inclusionBeginOffset, 
//			offset - inclusionBeginOffset + includeFile.length() + 1, 
//			local );
//		translationUnit.addInclusion( inclusion );
//		return inclusion;
		return null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd(Object inclusion) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(java.lang.String)
	 */
	public Object macro(String macroName, int offset, int macroBeginOffset, int macroEndOffset) {
//		Macro macro = new Macro(  macroName, offset, macroBeginOffset, macroEndOffset - macroBeginOffset);
//		translationUnit.addMacro( macro );
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object simpleDeclarationBegin(Object container, IToken firstToken) {
		SimpleDeclaration decl = new SimpleDeclaration( getCurrentDOMScope() );
		if( getCurrentDOMScope() instanceof IAccessable )
			decl.setAccessSpecifier(new AccessSpecifier( ((IAccessable)getCurrentDOMScope()).getVisibility() ));
		((IOffsetable)decl).setStartingOffset( firstToken.getOffset() );
		return decl;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationEnd(Object declaration, IToken lastToken) {
		SimpleDeclaration decl = (SimpleDeclaration)declaration;
		IOffsetable offsetable = (IOffsetable)decl;
		offsetable.setTotalLength( lastToken.getOffset() + lastToken.getLength() - offsetable.getStartingOffset());
		getCurrentDOMScope().addDeclaration(decl);
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

	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(IToken firstToken) {
		currName = new Name(firstToken);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(IToken lastToken) {
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

	public void baseSpecifierVisibility( Object baseSpecifier, IToken visibility )
	{
		int access = AccessSpecifier.v_public;  
		switch( visibility.getType() )
		{
		case IToken.t_public:
			access = AccessSpecifier.v_public; 
			break; 
		case IToken.t_protected:
			access = AccessSpecifier.v_protected;		 
			break;
		case IToken.t_private:
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
		ParameterDeclaration pd = new ParameterDeclaration(clause);
		return pd;
	}
	
	public void  parameterDeclarationEnd( Object declaration ){
		ParameterDeclaration d = (ParameterDeclaration)declaration;
		d.getOwnerScope().addDeclaration(d);
	}
    
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorAbort(java.lang.Object, java.lang.Object)
	 */
	public void declaratorAbort(Object declarator) {
		currName = null;
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
		cs.getOwner().setTypeSpecifier(null);
		domScopes.pop();
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierBegin(java.lang.Object)
	 */
	public Object elaboratedTypeSpecifierBegin(Object container, IToken classKey) {
		int kind = ClassKey.t_struct;
		
		switch (classKey.getType()) {
			case IToken.t_class:
				kind = ClassKey.t_class;
				break;
			case IToken.t_struct:
				kind = ClassKey.t_struct;
				break;
			case IToken.t_union:
				kind = ClassKey.t_union;
				break;
			case IToken.t_enum:
				kind = ClassKey.t_enum; 
				break;
		}

		ElaboratedTypeSpecifier elab = null;
		TypeSpecifier.IOwner declaration = (TypeSpecifier.IOwner)container;
		elab = new ElaboratedTypeSpecifier( kind, declaration );
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
		DeclSpecifier.IContainer decl = (DeclSpecifier.IContainer)declaration;
		DeclSpecifier declSpec = decl.getDeclSpecifier(); 
		declSpec.setName( currName ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionAbort(java.lang.Object)
	 */
	public void expressionAbort(Object expression) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classMemberVisibility(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void classMemberVisibility(Object classSpecifier, IToken visibility) {
		ClassSpecifier spec = (ClassSpecifier)classSpecifier;
		switch( visibility.getType() )
		{
			case IToken.t_public:
				spec.setVisibility( AccessSpecifier.v_public );
				break;
			case IToken.t_protected:
				spec.setVisibility( AccessSpecifier.v_protected );
				break;
			case IToken.t_private:
				spec.setVisibility( AccessSpecifier.v_private );
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
		if( owner != null ) // can be since operator * 
			owner.addPointerOperator( ptrOp );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorName(java.lang.Object)
	 */
	public void pointerOperatorName(Object ptrOperator) {
        ((PointerOperator)ptrOperator).setNameSpecifier(currName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorType(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorType(Object ptrOperator, IToken type) {
		PointerOperator ptrOp = (PointerOperator)ptrOperator;
		switch( type.getType() )
		{
			case IToken.tSTAR:
				ptrOp.setType( PointerOperator.t_pointer );
				break;
			case IToken.tAMPER:
				ptrOp.setType( PointerOperator.t_reference );
				break;
			case IToken.tCOLONCOLON:
			case IToken.tIDENTIFIER:
				ptrOp.setType( PointerOperator.t_pointer_to_member );
				break;
			default:
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorCVModifier(Object ptrOperator, IToken modifier) {
		PointerOperator ptrOp = (PointerOperator)ptrOperator;
		switch( modifier.getType() )
		{
			case IToken.t_const:
				ptrOp.setConst(true);
				break; 
			case IToken.t_volatile:
				ptrOp.setVolatile( true );
				break;
			default:
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void declaratorCVModifier(Object declarator, IToken modifier) {
		Declarator decl = (Declarator)declarator;
		switch( modifier.getType() )
		{
			case IToken.t_const:
				decl.setConst(true);
				break; 
			case IToken.t_volatile:
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
	public void declaratorThrowExceptionName(Object declarator ) 
	{
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
	public Object namespaceDefinitionBegin(Object container, IToken namespace) {
//		IScope ownerScope = (IScope)container;
//		NamespaceDefinition namespaceDef = new NamespaceDefinition(ownerScope);
//		namespaceDef.setStartToken(namespace);
//		((IOffsetable)namespaceDef).setStartingOffset( namespace.getOffset() );
//		return namespaceDef;
		return null;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationId(java.lang.Object)
	 */
	public void namespaceDefinitionId(Object namespace) {
//		NamespaceDefinition ns = (NamespaceDefinition)namespace;
//		ns.setName( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationAbort(java.lang.Object)
	 */
	public void namespaceDefinitionAbort(Object namespace) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationEnd(java.lang.Object)
	 */
	public void namespaceDefinitionEnd(Object namespace, IToken closingBrace) {
//		NamespaceDefinition ns = (NamespaceDefinition)namespace; 
//		ns.setTotalLength( closingBrace.getOffset() + closingBrace.getLength() - ns.getStartingOffset() );
//		ns.getOwnerScope().addDeclaration(ns);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationBegin(java.lang.Object, java.lang.String)
	 */
	public Object linkageSpecificationBegin(Object container, String literal) {
//		IScope scope = (IScope)container; 
//		LinkageSpecification linkage = new LinkageSpecification( scope, literal );
//		domScopes.push( linkage );
		return null; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationEnd(java.lang.Object)
	 */
	public void linkageSpecificationEnd(Object linkageSpec) {
//		LinkageSpecification linkage = (LinkageSpecification)domScopes.pop();
//		linkage.getOwnerScope().addDeclaration(linkage );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveBegin(java.lang.Object)
	 */
	public Object usingDirectiveBegin(Object container) {
//		IScope scope = (IScope)container;
//		UsingDirective directive = new UsingDirective( scope );
//		return directive;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveNamespaceId(java.lang.Object)
	 */
	public void usingDirectiveNamespaceId(Object dir) {
//		UsingDirective directive = (UsingDirective)dir;
//		directive.setNamespaceName( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveEnd(java.lang.Object)
	 */
	public void usingDirectiveEnd(Object dir) {
//		UsingDirective directive = (UsingDirective)dir;
//		directive.getOwnerScope().addDeclaration( directive );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationBegin(java.lang.Object)
	 */
	public Object usingDeclarationBegin(Object container) {
//		IScope scope = (IScope)container;
//		UsingDeclaration declaration = new UsingDeclaration( scope );
//		return declaration;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationMapping(java.lang.Object)
	 */
	public void usingDeclarationMapping(Object decl, boolean isTypename) {
//		UsingDeclaration declaration = (UsingDeclaration)decl;
//		declaration.setMappedName( currName );
//		declaration.setTypename( isTypename );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationEnd(java.lang.Object)
	 */
	public void usingDeclarationEnd(Object decl) {
//		UsingDeclaration declaration = (UsingDeclaration)decl;
//		declaration.getOwnerScope().addDeclaration( declaration );		
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
	public Object enumSpecifierBegin(Object container, IToken enumKey) {
		TypeSpecifier.IOwner decl = (TypeSpecifier.IOwner)container;
		EnumerationSpecifier es = new EnumerationSpecifier( decl );
		es.setStartToken(enumKey);
		decl.setTypeSpecifier(es);
		((IOffsetable)es).setStartingOffset( enumKey.getOffset() );
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
		es.getOwner().setTypeSpecifier(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierEnd(java.lang.Object)
	 */
	public void enumSpecifierEnd(Object enumSpec, IToken closingBrace) {
		IOffsetable offsetable = (IOffsetable)enumSpec;
		offsetable.setTotalLength( closingBrace.getOffset() + closingBrace.getLength() - offsetable.getStartingOffset());
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
		((IOffsetable)enumDefn).setStartingOffset( currName.getStartOffset() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionEnd(java.lang.Object)
	 */
	public void enumeratorEnd(Object enumDefn, IToken lastToken) {
		IOffsetable offsetable = (IOffsetable)enumDefn;
		offsetable.setTotalLength( lastToken.getOffset() + lastToken.getLength() - offsetable.getStartingOffset());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#asmDefinition(java.lang.String)
	 */
	public void asmDefinition(Object container, String assemblyCode) {
//		IScope scope = (IScope)container;
//		ASMDefinition definition = new ASMDefinition( scope, assemblyCode );
//		scope.addDeclaration( definition );
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
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationBegin(java.lang.Object)
	 */
	public Object explicitInstantiationBegin(Object container) {
		ExplicitTemplateDeclaration etd = new ExplicitTemplateDeclaration( getCurrentDOMScope(), ExplicitTemplateDeclaration.k_instantiation );
		domScopes.push( etd ); 
		return etd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationEnd(java.lang.Object)
	 */
	public void explicitInstantiationEnd(Object instantiation) {
		ExplicitTemplateDeclaration declaration = (ExplicitTemplateDeclaration)domScopes.pop();
		declaration.getOwnerScope().addDeclaration(declaration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationBegin(java.lang.Object)
	 */
	public Object explicitSpecializationBegin(Object container) {
		ExplicitTemplateDeclaration etd = new ExplicitTemplateDeclaration( getCurrentDOMScope(), ExplicitTemplateDeclaration.k_specialization);
		domScopes.push( etd ); 
		return etd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationEnd(java.lang.Object)
	 */
	public void explicitSpecializationEnd(Object instantiation) {
		ExplicitTemplateDeclaration etd = (ExplicitTemplateDeclaration)domScopes.pop();
		etd.getOwnerScope().addDeclaration(etd);
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
	public Object templateDeclarationBegin(Object container, IToken exported) {
		TemplateDeclaration d = new TemplateDeclaration( (IScope)getCurrentDOMScope(), exported );
		if( getCurrentDOMScope() instanceof IAccessable )
			d.setVisibility( ((IAccessable)container).getVisibility() );
		d.setStartingOffset( exported.getOffset() );
		domScopes.push( d ); 
		return d;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationAbort(java.lang.Object)
	 */
	public void templateDeclarationAbort(Object templateDecl) {
		domScopes.pop(); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationEnd(java.lang.Object)
	 */
	public void templateDeclarationEnd(Object templateDecl, IToken lastToken) {
		TemplateDeclaration decl = (TemplateDeclaration)domScopes.pop();
		decl.setLastToken(lastToken);
		decl.getOwnerScope().addDeclaration(decl);
		decl.setTotalLength(lastToken.getOffset() + lastToken.getLength() - decl.getStartingOffset() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object templateTypeParameterBegin(Object templDecl, IToken kind) {
		TemplateParameterList list = (TemplateParameterList)templDecl;
		int k; 
		switch( kind.getType() )
		{
			case IToken.t_class:
				k = TemplateParameter.k_class;
				break;
			case IToken.t_typename:
				k= TemplateParameter.k_typename;
				break;
			case IToken.t_template:
				k= TemplateParameter.k_template;
				break;
			default:
				k = 0;  
		}
		TemplateParameter p = new TemplateParameter( list, k );
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
		TemplateParameter parameter = (TemplateParameter)typeParm;
		parameter.getOwnerScope().addDeclaration( parameter );
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#setParser(org.eclipse.cdt.internal.core.parser.IParser)
	 */
	public void setParser(IParser parser) {
		this.parser = parser;
	}
	
	protected Name currName;	
	protected IParser parser = null;
	protected TranslationUnit translationUnit;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionName(java.lang.Object)
	 */
	public void expressionName(Object expression) {
		Expression e = (Expression)expression;
		e.add( currName );	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#startBitfield(java.lang.Object)
	 */
	public Object startBitfield(Object declarator) {
		return new BitField((Declarator)declarator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#endBitfield(java.lang.Object)
	 */
	public void endBitfield(Object bitfield) {
		BitField b = (BitField)bitfield;
		b.getOwnerDeclarator().setBitField( b );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifierType(java.lang.Object, java.lang.Object)
	 */
	public void simpleDeclSpecifierType(Object declaration, Object type) {
//		if( type instanceof TypeSpecifier )
//		{
//			System.out.println( "Told you so!");
//		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
	 */
	public void acceptProblem(IProblem problem) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
	 */
	public void acceptMacro(IASTMacro macro) {
		Macro m = new Macro(  macro.getName(), macro.getElementNameOffset(), macro.getElementStartingOffset(), 
			macro.getElementEndingOffset() - macro.getElementStartingOffset());
		translationUnit.addMacro( m );		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
	 */
	public void acceptVariable(IASTVariable variable) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void acceptFunctionDeclaration(IASTFunction function) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsageDirective(org.eclipse.cdt.core.parser.ast.IASTUsageDirective)
	 */
	public void acceptUsingDirective(IASTUsingDirective usageDirective) {
		UsingDirective directive = new UsingDirective( getCurrentDOMScope() );
		directive.setNamespaceName( usageDirective.getNamespaceName() );
		directive.getOwnerScope().addDeclaration( directive );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsageDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsageDeclaration)
	 */
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) {
		UsingDeclaration declaration = new UsingDeclaration( getCurrentDOMScope() );
		declaration.setTypename( usageDeclaration.isTypename());
		declaration.setMappedName(usageDeclaration.usingTypeName());
		declaration.getOwnerScope().addDeclaration( declaration );	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
	 */
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) {
		IScope scope = getCurrentDOMScope();
		ASMDefinition definition = new ASMDefinition( scope, asmDefinition.getBody() );
		scope.addDeclaration( definition );		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedef(org.eclipse.cdt.core.parser.ast.IASTTypedef)
	 */
	public void acceptTypedef(IASTTypedef typedef) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void enterFunctionBody(IASTFunction function) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void exitFunctionBody(IASTFunction function) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		domScopes.push( translationUnit );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void enterInclusion(IASTInclusion inclusion) {
		Inclusion i = new Inclusion( 
			inclusion.getName(), 
			inclusion.getElementNameOffset(), 
			inclusion.getElementStartingOffset(), 
			inclusion.getElementEndingOffset(), 
			inclusion.isLocal() );
		translationUnit.addInclusion( i );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		NamespaceDefinition namespaceDef = new NamespaceDefinition(getCurrentDOMScope());
		namespaceDef.setName( namespaceDefinition.getName() ); 
		((IOffsetable)namespaceDef).setStartingOffset( namespaceDefinition.getElementStartingOffset() );
		if( ! namespaceDefinition.getName().equals( "" ))
			namespaceDef.setNameOffset( namespaceDefinition.getElementNameOffset() );
		this.domScopes.push( namespaceDef ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecification)
	 */
	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {
		LinkageSpecification linkage = new LinkageSpecification( getCurrentDOMScope(), linkageSpec.getLinkageString() );
		domScopes.push( linkage );		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void enterTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void acceptMethodDeclaration(IASTMethod method) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void enterMethodBody(IASTMethod method) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void exitMethodBody(IASTMethod method) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
	 */
	public void acceptField(IASTField field) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) {
		LinkageSpecification linkage = (LinkageSpecification)domScopes.pop();
		getCurrentDOMScope().addDeclaration(linkage );	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecification)
	 */
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		NamespaceDefinition definition = (NamespaceDefinition)domScopes.pop(); 
		definition.setTotalLength( namespaceDefinition.getElementEndingOffset() - namespaceDefinition.getElementStartingOffset());
		getCurrentDOMScope().addDeclaration( definition );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void exitInclusion(IASTInclusion inclusion) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {
		domScopes.pop(); 
	}
 
	private ScopeStack domScopes = new ScopeStack(); 
	
	private IScope getCurrentDOMScope()
	{
		return domScopes.peek(); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier)
	 */
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
		// TODO Auto-generated method stub		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, int)
	 */
	public void acceptClassReference(IASTClassSpecifier classSpecifier, int referenceOffset) {
		// TODO Auto-generated method stub
		
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptElaboratedTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier)
     */
    public void acceptElaboratedTypeSpecifier(IASTElaboratedTypeSpecifier elaboratedTypeSpec)
    {
        // TODO Auto-generated method stub
        
    }
}