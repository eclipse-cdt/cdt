package org.eclipse.cdt.internal.core.parser;

public class NullParserCallback implements IParserCallback {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#translationUnitBegin()
	 */
	public Object translationUnitBegin() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#translationUnitEnd(java.lang.Object)
	 */
	public void translationUnitEnd(Object unit) {	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#inclusionBegin(java.lang.String, int)
	 */
	public Object inclusionBegin(String includeFile, int offset, int inclusionBeginOffset) {
		return null; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd(Object inclusion) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#macro(java.lang.String, int)
	 */
	public Object macro(String macroName, int offset, int macroBeginOffset, int macroEndOffset) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclarationBegin(java.lang.Object)
	 */
	public Object simpleDeclarationBegin(Object Container, Token firstToken) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void simpleDeclSpecifier(Object Container, Token specifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifierName(java.lang.Object)
	 */
	public void simpleDeclSpecifierName(Object declaration) {
	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclarationEnd(java.lang.Object)
	 */
	public void simpleDeclarationEnd(Object declaration, Token lastToken) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#parameterDeclarationBegin(java.lang.Object)
	 */
	public Object parameterDeclarationBegin(Object Container) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#parameterDeclarationEnd(java.lang.Object)
	 */
	public void parameterDeclarationEnd(Object declaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void nameBegin(Token firstToken) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void nameEnd(Token lastToken) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorBegin(java.lang.Object)
	 */
	public Object declaratorBegin(Object container) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorId(java.lang.Object)
	 */
	public void declaratorId(Object declarator) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorAbort(java.lang.Object, java.lang.Object)
	 */
	public void declaratorAbort(Object declarator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void declaratorCVModifier(Object declarator, Token modifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorThrowExceptionName(java.lang.Object)
	 */
	public void declaratorThrowExceptionName(Object exceptionSpec) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorEnd(java.lang.Object)
	 */
	public void declaratorEnd(Object declarator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayDeclaratorBegin(java.lang.Object)
	 */
	public Object arrayDeclaratorBegin(Object declarator) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayDeclaratorEnd(java.lang.Object)
	 */
	public void arrayDeclaratorEnd(Object arrayQualifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorBegin(java.lang.Object)
	 */
	public Object pointerOperatorBegin(Object container) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorType(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorType(Object ptrOperator, Token type) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorName(java.lang.Object)
	 */
	public void pointerOperatorName(Object ptrOperator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorCVModifier(Object ptrOperator, Token modifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorEnd(java.lang.Object)
	 */
	public void pointerOperatorEnd(Object ptrOperator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#argumentsBegin(java.lang.Object)
	 */
	public Object argumentsBegin(Object declarator) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#argumentsEnd(java.lang.Object)
	 */
	public void argumentsEnd(Object parameterDeclarationClause) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#functionBodyBegin(java.lang.Object)
	 */
	public Object functionBodyBegin(Object declaration) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#functionBodyEnd(java.lang.Object)
	 */
	public void functionBodyEnd(Object functionBody) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierName(java.lang.Object)
	 */
	public void classSpecifierName(Object classSpecifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierAbort(java.lang.Object)
	 */
	public void classSpecifierAbort(Object classSpecifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classMemberVisibility(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void classMemberVisibility(Object classSpecifier, Token visibility) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierEnd(java.lang.Object)
	 */
	public void classSpecifierEnd(Object classSpecifier, Token closingBrace) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierBegin(java.lang.Object)
	 */
	public Object baseSpecifierBegin(Object containingClassSpec) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierName(java.lang.Object)
	 */
	public void baseSpecifierName(Object baseSpecifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierVisibility(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void baseSpecifierVisibility(Object baseSpecifier, Token visibility) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierVirtual(java.lang.Object, boolean)
	 */
	public void baseSpecifierVirtual(Object baseSpecifier, boolean virtual) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierEnd(java.lang.Object)
	 */
	public void baseSpecifierEnd(Object baseSpecifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionBegin(java.lang.Object)
	 */
	public Object expressionBegin(Object container) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionOperator(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void expressionOperator(Object expression, Token operator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionTerminal(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void expressionTerminal(Object expression, Token terminal) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionAbort(java.lang.Object)
	 */
	public void expressionAbort(Object expression) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionEnd(java.lang.Object)
	 */
	public void expressionEnd(Object expression) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object elaboratedTypeSpecifierBegin(Object container, Token classKey) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierName(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierName(Object elab) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierEnd(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierEnd(Object elab) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorThrowsException(java.lang.Object)
	 */
	public void declaratorThrowsException(Object declarator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationBegin(java.lang.Object)
	 */
	public Object namespaceDefinitionBegin(Object container, Token namespace) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationId(java.lang.Object)
	 */
	public void namespaceDefinitionId(Object namespace) {
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationBegin(java.lang.Object, java.lang.String)
	 */
	public Object linkageSpecificationBegin(Object container, String literal) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationEnd(java.lang.Object)
	 */
	public void linkageSpecificationEnd(Object linkageSpec) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveBegin(java.lang.Object)
	 */
	public Object usingDirectiveBegin(Object container) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveNamespaceId(java.lang.Object)
	 */
	public void usingDirectiveNamespaceId(Object container) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveEnd(java.lang.Object)
	 */
	public void usingDirectiveEnd(Object directive) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationBegin(java.lang.Object)
	 */
	public Object usingDeclarationBegin(Object container) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationMapping(java.lang.Object)
	 */
	public void usingDeclarationMapping(Object container, boolean isTypename) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationEnd(java.lang.Object)
	 */
	public void usingDeclarationEnd(Object directive) {
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
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierId(java.lang.Object)
	 */
	public void enumSpecifierId(Object enumSpec) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierAbort(java.lang.Object)
	 */
	public void enumSpecifierAbort(Object enumSpec) {
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
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionId(java.lang.Object)
	 */
	public void enumeratorId(Object enumDefn) {
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainBegin(java.lang.Object)
	 */
	public Object constructorChainBegin(Object declarator) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainAbort(java.lang.Object)
	 */
	public void constructorChainAbort(Object ctor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainEnd(java.lang.Object)
	 */
	public void constructorChainEnd(Object ctor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementBegin(Object ctor) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementEnd(java.lang.Object)
	 */
	public void constructorChainElementEnd(Object element) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainId(java.lang.Object)
	 */
	public void constructorChainElementId(Object ctor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementExpressionListElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementExpressionListElementBegin(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementExpressionListElementEnd(java.lang.Object)
	 */
	public void constructorChainElementExpressionListElementEnd(Object expression) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationBegin(java.lang.Object)
	 */
	public Object explicitInstantiationBegin(Object container) {
		return null;
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
		return null;
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationBegin(java.lang.Object, boolean)
	 */
	public Object templateDeclarationBegin(Object container, Token exported) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationAbort(java.lang.Object)
	 */
	public void templateDeclarationAbort(Object templateDecl) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationEnd(java.lang.Object)
	 */
	public void templateDeclarationEnd(Object templateDecl, Token lastToken) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object templateTypeParameterBegin(Object templDecl, Token kind) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterName(java.lang.Object)
	 */
	public void templateTypeParameterName(Object typeParm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeInitialTypeId(java.lang.Object)
	 */
	public void templateTypeParameterInitialTypeId(Object typeParm) {
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorAbort(java.lang.Object)
	 */
	public void pointerOperatorAbort(Object ptrOperator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateParameterListBegin(java.lang.Object)
	 */
	public Object templateParameterListBegin(Object declaration) {
		return null;
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
	}

}
