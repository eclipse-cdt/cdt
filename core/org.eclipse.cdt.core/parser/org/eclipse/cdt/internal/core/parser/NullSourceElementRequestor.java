package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserCallback;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTConstructor;
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


public class NullSourceElementRequestor implements ISourceElementRequestor, IParserCallback {

//  Here goes ISourceElementRequestor interface

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
     */
    public void acceptProblem(IProblem problem) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
     */
    public void acceptMacro(IASTMacro macro) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
     */
    public void acceptVariable(IASTVariable variable) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void acceptFunctionDeclaration(IASTFunction function) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsageDirective(org.eclipse.cdt.core.parser.ast.IASTUsageDirective)
     */
    public void acceptUsingDirective(IASTUsingDirective usageDirective) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsageDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsageDeclaration)
     */
    public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
     */
    public void acceptASMDefinition(IASTASMDefinition asmDefinition) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedef(org.eclipse.cdt.core.parser.ast.IASTTypedef)
     */
    public void acceptTypedef(IASTTypedef typedef) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void enterFunctionBody(IASTFunction function) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void exitFunctionBody(IASTFunction function) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
     */
    public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
     */
    public void enterInclusion(IASTInclusion inclusion) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
     */
    public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecification)
     */
    public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
     */
    public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
     */
    public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
     */
    public void enterTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void acceptMethodDeclaration(IASTMethod method) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void enterMethodBody(IASTMethod method) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void exitMethodBody(IASTMethod method) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
     */
    public void acceptField(IASTField field) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptConstructor(org.eclipse.cdt.core.parser.ast.IASTConstructor)
     */
    public void acceptConstructor(IASTConstructor constructor) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
     */
    public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
     */
    public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
     */
    public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecification)
     */
    public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
     */
    public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
     */
    public void exitInclusion(IASTInclusion inclusion) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
     */
    public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {
    }


// Here goes IParserCallback interface

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
	public Object inclusionBegin(String includeFile, int offset, int inclusionBeginOffset, boolean local) {
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
	public Object simpleDeclarationBegin(Object Container, IToken firstToken) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void simpleDeclSpecifier(Object Container, IToken specifier) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifierName(java.lang.Object)
	 */
	public void simpleDeclSpecifierName(Object declaration) {
	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclarationEnd(java.lang.Object)
	 */
	public void simpleDeclarationEnd(Object declaration, IToken lastToken) {
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
	public void nameBegin(IToken firstToken) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void nameEnd(IToken lastToken) {
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
	public void declaratorCVModifier(Object declarator, IToken modifier) {
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
	public void pointerOperatorType(Object ptrOperator, IToken type) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorName(java.lang.Object)
	 */
	public void pointerOperatorName(Object ptrOperator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorCVModifier(Object ptrOperator, IToken modifier) {
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
    
    /**
     * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#oldKRParametersBegin()
     */
    public Object oldKRParametersBegin( Object parameterDeclarationClause ) {
        return null; 
    }

    /**
     * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#oldKRParametersEnd()
     */
    public void oldKRParametersEnd(Object oldKRParameterDeclarationClause) {
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
	public Object classSpecifierBegin(Object container, IToken classKey) {
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
	public void classMemberVisibility(Object classSpecifier, IToken visibility) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierEnd(java.lang.Object)
	 */
	public void classSpecifierEnd(Object classSpecifier, IToken closingBrace) {
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
	public void baseSpecifierVisibility(Object baseSpecifier, IToken visibility) {
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
	public void expressionOperator(Object expression, IToken operator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionTerminal(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void expressionTerminal(Object expression, IToken terminal) {
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
	public Object elaboratedTypeSpecifierBegin(Object container, IToken classKey) {
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
	public Object namespaceDefinitionBegin(Object container, IToken namespace) {
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
	public void namespaceDefinitionEnd(Object namespace, IToken closingBrace) {
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
	public Object enumSpecifierBegin(Object container, IToken enumKey) {
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
	public void enumSpecifierEnd(Object enumSpec, IToken closingBrace) {
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
	public void enumeratorEnd(Object enumDefn, IToken lastToken) {
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
	public Object templateDeclarationBegin(Object container, IToken exported) {
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
	public void templateDeclarationEnd(Object templateDecl, IToken lastToken) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object templateTypeParameterBegin(Object templDecl, IToken kind) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionName(java.lang.Object)
	 */
	public void expressionName(Object expression) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#startBitfield(java.lang.Object)
	 */
	public Object startBitfield(Object declarator) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#endBitfield(java.lang.Object)
	 */
	public void endBitfield(Object bitfield) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifierType(java.lang.Object, java.lang.Object)
	 */
	public void simpleDeclSpecifierType(Object declaration, Object type) {
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
