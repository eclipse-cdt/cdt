package org.eclipse.cdt.internal.core.dom;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTMethodReference;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTPointerToMethod;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.internal.core.parser.ast.IASTArrayModifier;
/**
 * This is the parser callback that creates objects in the DOM.
 */
public class DOMBuilder implements ISourceElementRequestor
{
    public DOMBuilder()
    {
    }
    protected TranslationUnit translationUnit = new TranslationUnit();
    public TranslationUnit getTranslationUnit()
    {
        return translationUnit;
    }
    public SimpleDeclaration getTypeSpecOwner(
        IScope scope,
        int startingOffset)
    {
        List declarations = scope.getDeclarations();
        for (int i = 0; i < declarations.size(); ++i)
        {
            if (declarations.get(i) instanceof SimpleDeclaration )
            {
                SimpleDeclaration s = (SimpleDeclaration)declarations.get(i);
                if (s.getStartingOffset() == startingOffset)
                    return s;
            }
        }
        return null;
    }
    protected void createPDC(Declarator decl)
    {
        ParameterDeclarationClause clause =
            new ParameterDeclarationClause(decl);
    }
    //    /**
    //     * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#oldKRParametersBegin()
    //     */
    //    public Object oldKRParametersBegin( Object parameterDeclarationClause ) {
    //        ParameterDeclarationClause clause = ((ParameterDeclarationClause)parameterDeclarationClause);
    //        OldKRParameterDeclarationClause KRclause = new OldKRParameterDeclarationClause( clause );
    //        domScopes.push(KRclause);
    //        return KRclause; 
    //    }
    //
    //    /**
    //     * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#oldKRParametersEnd()
    //     */
    //    public void oldKRParametersEnd(Object oldKRParameterDeclarationClause) {
    //        domScopes.pop();
    //    }
    //
    //
    //	/**
    //	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorBegin()
    //	 */
    //	public Object declaratorBegin(Object container) {
    //		if( container instanceof DeclSpecifier.IContainer )
    //		{
    //			DeclSpecifier.IContainer decl = (DeclSpecifier.IContainer )container; 
    //			Declarator declarator = new Declarator(decl);
    //			return declarator;
    //		}
    //		else if( container instanceof IDeclaratorOwner )
    //		{
    //			IDeclaratorOwner owner = (IDeclaratorOwner)container;
    //			Declarator declarator = new Declarator(owner); 
    //			return declarator; 
    //		}
    //		return null; 
    //	}
    //
    //	/**
    //	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorEnd()
    //	 */
    //	public void declaratorEnd(Object declarator) {
    //		Declarator d = (Declarator)declarator;
    //		if( d.getDeclaration() != null ) 
    //			d.getDeclaration().addDeclarator(d);
    //		else if( d.getOwnerDeclarator() != null )
    //			d.getOwnerDeclarator().setDeclarator(d); 
    //	}
    //
    //	/**
    //	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(org.eclipse.cdt.internal.core.newparser.Token)
    //	 */
    //	public void declaratorId(Object declarator) {
    //		((Declarator)declarator).setName(currName);
    //	}
    //
    //	/**
    //	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declSpecifier(org.eclipse.cdt.internal.core.newparser.Token)
    //	 */
    //	public void simpleDeclSpecifier(Object Container, IToken specifier) {
    //		DeclSpecifier.IContainer decl = (DeclSpecifier.IContainer)Container;
    //		DeclSpecifier declSpec = decl.getDeclSpecifier(); 
    //		declSpec.setType( specifier );
    //	}
    //
    //	/**
    //	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyBegin()
    //	 */
    //	public Object functionBodyBegin(Object declaration) {
    //		SimpleDeclaration simpleDec = (SimpleDeclaration)declaration;
    //		simpleDec.setFunctionDefinition(true);
    //		return null;
    //	}
    //
    //	/**
    //	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationBegin(org.eclipse.cdt.internal.core.newparser.Token)
    //	 */
    //	public Object simpleDeclarationBegin(Object container, IToken firstToken) {
    //		SimpleDeclaration decl = new SimpleDeclaration( getCurrentDOMScope() );
    //		if( getCurrentDOMScope() instanceof IAccessable )
    //			decl.setAccessSpecifier(new AccessSpecifier( ((IAccessable)getCurrentDOMScope()).getVisibility() ));
    //		((IOffsetable)decl).setStartingOffset( firstToken.getOffset() );
    //	}
    //
    //	/**
    //	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(org.eclipse.cdt.internal.core.newparser.Token)
    //	 */
    //	public void simpleDeclarationEnd(Object declaration, IToken lastToken) {
    //		SimpleDeclaration decl = (SimpleDeclaration)declaration;
    //		IOffsetable offsetable = (IOffsetable)decl;
    //		offsetable.setTotalLength( lastToken.getOffset() + lastToken.getLength() - offsetable.getStartingOffset());
    //		getCurrentDOMScope().addDeclaration(decl);
    //	}
    //
    //
    //
    protected void createBaseSpecifier(ClassSpecifier cs, IASTBaseSpecifier bs)
    {
        BaseSpecifier baseSpec = new BaseSpecifier(cs);
        baseSpec.setVirtual(bs.isVirtual());
        int access = AccessSpecifier.v_public;
        if (bs.getAccess() == ASTAccessVisibility.PUBLIC)
            access = AccessSpecifier.v_public;
        else if (bs.getAccess() == ASTAccessVisibility.PROTECTED)
            access = AccessSpecifier.v_protected;
        else if (bs.getAccess() == ASTAccessVisibility.PRIVATE)
            access = AccessSpecifier.v_private;
        baseSpec.setAccess(access);
        baseSpec.setName(bs.getParentClassName());
    }
    //	
    //	public Object parameterDeclarationBegin( Object container )
    //	{
    //		IScope clause = (IScope)container; 
    //		ParameterDeclaration pd = new ParameterDeclaration(clause);
    //		return pd;
    //	}
    //	
    //	public void  parameterDeclarationEnd( Object declaration ){
    //		ParameterDeclaration d = (ParameterDeclaration)declaration;
    //		d.getOwnerScope().addDeclaration(d);
    //	}
    //
    	

    //
    //	/**
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifierName(java.lang.Object)
    //	 */
    //	public void simpleDeclSpecifierName(Object declaration) {
    //		DeclSpecifier.IContainer decl = (DeclSpecifier.IContainer)declaration;
    //		DeclSpecifier declSpec = decl.getDeclSpecifier(); 
    //		declSpec.setName( currName ); 
    //	}
    //
    //
    protected void addPointerOperator(
        Declarator d,
        ASTPointerOperator pointerOp,
        String name)
    {
        PointerOperator ptrOp = new PointerOperator(d);
        if (pointerOp == ASTPointerOperator.REFERENCE)
        {
            ptrOp.setType(PointerOperator.t_reference);
        }
        else if (pointerOp == ASTPointerOperator.POINTER)
        {
            ptrOp.setType(PointerOperator.t_pointer);
        }
        else if (pointerOp == ASTPointerOperator.CONST_POINTER)
        {
            ptrOp.setType(PointerOperator.t_pointer);
            ptrOp.setConst(true);
        }
        else if (pointerOp == ASTPointerOperator.VOLATILE_POINTER)
        {
            ptrOp.setType(PointerOperator.t_pointer);
            ptrOp.setVolatile(true);
        }
        if (d != null)
            d.addPointerOperator(ptrOp);
    }
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
    //	 */
    //	public void declaratorCVModifier(Object declarator, IToken modifier) {
    //		Declarator decl = (Declarator)declarator;
    //		switch( modifier.getType() )
    //		{
    //			case IToken.t_const:
    //				decl.setConst(true);
    //				break; 
    //			case IToken.t_volatile:
    //				decl.setVolatile( true );
    //				break;
    //			default:
    //				break;
    //		}
    //
    //	}
    protected void addArrayDeclarator(
        Declarator decl,
        IASTArrayModifier arrayModifier)
    {
        ArrayQualifier qual = new ArrayQualifier(decl);
        decl.addArrayQualifier(qual);
    }
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#exceptionSpecificationTypename(java.lang.Object)
    //	 */
    //	public void declaratorThrowExceptionName(Object declarator ) 
    //	{
    //		Declarator decl = (Declarator)declarator; 
    //		decl.getExceptionSpecifier().addTypeName( currName );
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorThrowsException(java.lang.Object)
    //	 */
    //	public void declaratorThrowsException(Object declarator) {
    //		Declarator decl = (Declarator)declarator; 
    //		decl.getExceptionSpecifier().setThrowsException(true);
    //	}
    //
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainBegin(java.lang.Object)
    //	 */
    //	public Object constructorChainBegin(Object declarator) {
    //		Declarator d = (Declarator)declarator; 
    //		ConstructorChain chain = new ConstructorChain(d); 
    //		return chain;
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainEnd(java.lang.Object)
    //	 */
    //	public void constructorChainEnd(Object ctor) {
    //		ConstructorChain chain = (ConstructorChain)ctor; 
    //		chain.getOwnerDeclarator().setCtorChain(chain);
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementBegin(java.lang.Object)
    //	 */
    //	public Object constructorChainElementBegin(Object ctor) {
    //		return new ConstructorChainElement( (ConstructorChain)ctor );
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementEnd(java.lang.Object)
    //	 */
    //	public void constructorChainElementEnd(Object element) {
    //		ConstructorChainElement ele = (ConstructorChainElement)element;
    //		ele.getOwnerChain().addChainElement( ele );
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainId(java.lang.Object)
    //	 */
    //	public void constructorChainElementId(Object element) {
    //		ConstructorChainElement ele = (ConstructorChainElement)element;
    //		ele.setName(currName);
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationBegin(java.lang.Object)
    //	 */
    //	public Object explicitInstantiationBegin(Object container) {
    //		ExplicitTemplateDeclaration etd = new ExplicitTemplateDeclaration( getCurrentDOMScope(), ExplicitTemplateDeclaration.k_instantiation );
    //		domScopes.push( etd ); 
    //		return etd;
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationEnd(java.lang.Object)
    //	 */
    //	public void explicitInstantiationEnd(Object instantiation) {
    //		ExplicitTemplateDeclaration declaration = (ExplicitTemplateDeclaration)domScopes.pop();
    //		declaration.getOwnerScope().addDeclaration(declaration);
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationBegin(java.lang.Object)
    //	 */
    //	public Object explicitSpecializationBegin(Object container) {
    //		ExplicitTemplateDeclaration etd = new ExplicitTemplateDeclaration( getCurrentDOMScope(), ExplicitTemplateDeclaration.k_specialization);
    //		domScopes.push( etd ); 
    //		return etd;
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationEnd(java.lang.Object)
    //	 */
    //	public void explicitSpecializationEnd(Object instantiation) {
    //		ExplicitTemplateDeclaration etd = (ExplicitTemplateDeclaration)domScopes.pop();
    //		etd.getOwnerScope().addDeclaration(etd);
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorPureVirtual(java.lang.Object)
    //	 */
    //	public void declaratorPureVirtual(Object declarator) {
    //		Declarator d = (Declarator)declarator;
    //		d.setPureVirtual(true);
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationBegin(java.lang.Object, boolean)
    //	 */
    //	public Object templateDeclarationBegin(Object container, IToken exported) {
    //		TemplateDeclaration d = new TemplateDeclaration( (IScope)getCurrentDOMScope(), exported );
    //		if( getCurrentDOMScope() instanceof IAccessable )
    //			d.setVisibility( ((IAccessable)container).getVisibility() );
    //		d.setStartingOffset( exported.getOffset() );
    //		domScopes.push( d ); 
    //		return d;
    //	}
    //
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationEnd(java.lang.Object)
    //	 */
    //	public void templateDeclarationEnd(Object templateDecl, IToken lastToken) {
    //		TemplateDeclaration decl = (TemplateDeclaration)domScopes.pop();
    //		decl.setLastToken(lastToken);
    //		decl.getOwnerScope().addDeclaration(decl);
    //		decl.setTotalLength(lastToken.getOffset() + lastToken.getLength() - decl.getStartingOffset() );
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
    //	 */
    //	public Object templateTypeParameterBegin(Object templDecl, IToken kind) {
    //		TemplateParameterList list = (TemplateParameterList)templDecl;
    //		int k; 
    //		switch( kind.getType() )
    //		{
    //			case IToken.t_class:
    //				k = TemplateParameter.k_class;
    //				break;
    //			case IToken.t_typename:
    //				k= TemplateParameter.k_typename;
    //				break;
    //			case IToken.t_template:
    //				k= TemplateParameter.k_template;
    //				break;
    //			default:
    //				k = 0;  
    //		}
    //		TemplateParameter p = new TemplateParameter( list, k );
    //		return p;
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterName(java.lang.Object)
    //	 */
    //	public void templateTypeParameterName(Object typeParm) {
    //		((TemplateParameter)typeParm).setName( currName );
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeInitialTypeId(java.lang.Object)
    //	 */
    //	public void templateTypeParameterInitialTypeId(Object typeParm) {
    //		((TemplateParameter)typeParm).setTypeId( currName );
    //	}
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterEnd(java.lang.Object)
    //	 */
    //	public void templateTypeParameterEnd(Object typeParm) {
    //		TemplateParameter parameter = (TemplateParameter)typeParm;
    //		parameter.getOwnerScope().addDeclaration( parameter );
    //	}
    //
    //
    //
    //
    //	/* (non-Javadoc)
    //	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateParameterListBegin(java.lang.Object)
    //	 */
    //	public Object templateParameterListBegin(Object declaration) {
    //		ITemplateParameterListOwner d = (ITemplateParameterListOwner)declaration;
    //		TemplateParameterList list = new TemplateParameterList(); 
    //		d.setTemplateParms(list);
    //		return list;
    //	}
    protected void addBitfield(
        Declarator declarator,
        IASTExpression bitfieldExpression)
    {
        declarator.setBitField(new BitField(declarator));
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
     */
    public void acceptProblem(IProblem problem)
    {
        // ignore 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
     */
    public void acceptMacro(IASTMacro macro)
    {
        Macro m =
            new Macro(
                macro.getName(),
                macro.getNameOffset(),
                macro.getStartingOffset(),
                macro.getEndingOffset()
                    - macro.getStartingOffset());
        translationUnit.addMacro(m);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
     */
    public void acceptVariable(IASTVariable variable)
    {
        SimpleDeclaration declaration =
            createStructuralSimpleDeclaration(variable);
        Declarator d = new Declarator(declaration);
        d.setName(variable.getName());
        declaration.addDeclarator(d);
    }
    protected SimpleDeclaration createStructuralSimpleDeclaration(IASTVariable variable)
    {
        SimpleDeclaration declaration =
            getTypeSpecOwner(
                getCurrentDOMScope(),
                variable.getStartingOffset());
        if (declaration == null)
        {
            declaration =
                startSimpleDeclaration(variable.getStartingOffset());
            declaration.getDeclSpecifier().setConst(
                variable.getAbstractDeclaration().isConst());
            declaration.getDeclSpecifier().setExtern(variable.isExtern());
            declaration.getDeclSpecifier().setAuto(variable.isAuto());
            declaration.getDeclSpecifier().setRegister(variable.isRegister());
            declaration.getDeclSpecifier().setStatic(variable.isStatic());
            IASTTypeSpecifier typeSpec =
                variable.getAbstractDeclaration().getTypeSpecifier();
            if (typeSpec == null)
            {
                // what to do here? 
            }
            else if (typeSpec instanceof IASTSimpleTypeSpecifier)
            {
                IASTSimpleTypeSpecifier simpleTypeSpec =
                    (IASTSimpleTypeSpecifier)typeSpec;
                declaration.getDeclSpecifier().setLong(simpleTypeSpec.isLong());
                declaration.getDeclSpecifier().setShort(
                    simpleTypeSpec.isShort());
                declaration.getDeclSpecifier().setUnsigned(
                    simpleTypeSpec.isUnsigned());
                if (simpleTypeSpec.getType()
                    == IASTSimpleTypeSpecifier.Type.BOOL)
                    declaration.getDeclSpecifier().setType(
                        DeclSpecifier.t_bool);
                else if (
                    simpleTypeSpec.getType()
                        == IASTSimpleTypeSpecifier.Type.CHAR)
                    declaration.getDeclSpecifier().setType(
                        DeclSpecifier.t_char);
                else if (
                    simpleTypeSpec.getType()
                        == IASTSimpleTypeSpecifier.Type.DOUBLE)
                    declaration.getDeclSpecifier().setType(
                        DeclSpecifier.t_double);
                else if (
                    simpleTypeSpec.getType()
                        == IASTSimpleTypeSpecifier.Type.FLOAT)
                    declaration.getDeclSpecifier().setType(
                        DeclSpecifier.t_float);
                else if (
                    simpleTypeSpec.getType()
                        == IASTSimpleTypeSpecifier.Type.INT)
                    declaration.getDeclSpecifier().setType(DeclSpecifier.t_int);
                else if (
                    simpleTypeSpec.getType()
                        == IASTSimpleTypeSpecifier.Type.TEMPLATE)
                    declaration.getDeclSpecifier().setType(
                        DeclSpecifier.t_type);
                else if (
                    simpleTypeSpec.getType()
                        == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME)
                    declaration.getDeclSpecifier().setType(
                        DeclSpecifier.t_type);
                else if (
                    simpleTypeSpec.getType()
                        == IASTSimpleTypeSpecifier.Type.VOID)
                    declaration.getDeclSpecifier().setType(
                        DeclSpecifier.t_void);
                else if (
                    simpleTypeSpec.getType()
                        == IASTSimpleTypeSpecifier.Type.WCHAR_T)
                    declaration.getDeclSpecifier().setType(
                        DeclSpecifier.t_wchar_t);
            }
            else if (typeSpec instanceof IASTClassSpecifier)
            {
            }
            else if (typeSpec instanceof IASTEnumerationSpecifier)
            {
            }
            else if (typeSpec instanceof IASTElaboratedTypeSpecifier)
            {
            }
            getCurrentDOMScope().addDeclaration(declaration);
        }
        return declaration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void acceptFunctionDeclaration(IASTFunction function)
    {
        SimpleDeclaration simpleDeclaration =
            getTypeSpecOwner(
                getCurrentDOMScope(),
                function.getStartingOffset());
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsageDirective(org.eclipse.cdt.core.parser.ast.IASTUsageDirective)
     */
    public void acceptUsingDirective(IASTUsingDirective usageDirective)
    {
        UsingDirective directive = new UsingDirective(getCurrentDOMScope());
        directive.setNamespaceName(usageDirective.getNamespaceName());
        directive.getOwnerScope().addDeclaration(directive);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsageDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsageDeclaration)
     */
    public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration)
    {
        UsingDeclaration declaration =
            new UsingDeclaration(getCurrentDOMScope());
        declaration.setTypename(usageDeclaration.isTypename());
        declaration.setMappedName(usageDeclaration.usingTypeName());
        declaration.getOwnerScope().addDeclaration(declaration);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
     */
    public void acceptASMDefinition(IASTASMDefinition asmDefinition)
    {
        IScope scope = getCurrentDOMScope();
        ASMDefinition definition =
            new ASMDefinition(scope, asmDefinition.getBody());
        scope.addDeclaration(definition);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedef(org.eclipse.cdt.core.parser.ast.IASTTypedef)
     */
    public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void enterFunctionBody(IASTFunction function)
    {
        // ignore
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void exitFunctionBody(IASTFunction function)
    {
        //ignore
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
     */
    public void enterCompilationUnit(IASTCompilationUnit compilationUnit)
    {
        domScopes.push(translationUnit);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
     */
    public void enterInclusion(IASTInclusion inclusion)
    {
        Inclusion i =
            new Inclusion(
                inclusion.getName(),
                inclusion.getNameOffset(),
                inclusion.getStartingOffset(),
                inclusion.getEndingOffset(),
                inclusion.isLocal());
        translationUnit.addInclusion(i);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
     */
    public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
    {
        NamespaceDefinition namespaceDef =
            new NamespaceDefinition(getCurrentDOMScope());
        namespaceDef.setName(namespaceDefinition.getName());
        ((IOffsetable)namespaceDef).setStartingOffset(
            namespaceDefinition.getStartingOffset());
        if (!namespaceDefinition.getName().equals(""))
            namespaceDef.setNameOffset(
                namespaceDefinition.getNameOffset());
        this.domScopes.push(namespaceDef);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecification)
     */
    public void enterClassSpecifier(IASTClassSpecifier classSpecification)
    {
        SimpleDeclaration decl =
            startSimpleDeclaration(
                classSpecification.getStartingOffset());
        int kind = ClassKey.t_struct;
        int visibility = AccessSpecifier.v_public;
        if (classSpecification.getClassKind() == ASTClassKind.CLASS)
        {
            kind = ClassKey.t_class;
            visibility = AccessSpecifier.v_private;
        }
        else if (classSpecification.getClassKind() == ASTClassKind.STRUCT)
        {
            kind = ClassKey.t_struct;
        }
        else if (classSpecification.getClassKind() == ASTClassKind.UNION)
        {
            kind = ClassKey.t_union;
        }
        ClassSpecifier classSpecifier = new ClassSpecifier(kind, decl);
        classSpecifier.setVisibility(visibility);
        classSpecifier.setStartingOffset(
            classSpecification.getStartingOffset());
        decl.setTypeSpecifier(classSpecifier);
        classSpecifier.setName(classSpecification.getName());
        classSpecifier.setNameOffset(classSpecification.getNameOffset());
        domScopes.push(classSpecifier);
    }
    protected SimpleDeclaration startSimpleDeclaration(int startingOffset)
    {
        SimpleDeclaration decl = new SimpleDeclaration(getCurrentDOMScope());
        if (getCurrentDOMScope() instanceof IAccessable)
            decl.setAccessSpecifier(
                new AccessSpecifier(
                    ((IAccessable)getCurrentDOMScope()).getVisibility()));
        ((IOffsetable)decl).setStartingOffset(startingOffset);
        return decl;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
     */
    public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec)
    {
        LinkageSpecification linkage =
            new LinkageSpecification(
                getCurrentDOMScope(),
                linkageSpec.getLinkageString());
        domScopes.push(linkage);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public void enterTemplateDeclaration(IASTTemplateDeclaration declaration)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
     */
    public void enterTemplateSpecialization(IASTTemplateSpecialization specialization)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
     */
    public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void acceptMethodDeclaration(IASTMethod method)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void enterMethodBody(IASTMethod method)
    {
        // ignore
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void exitMethodBody(IASTMethod method)
    {
        // ignore
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
     */
    public void acceptField(IASTField field)
    {
        SimpleDeclaration declaration =
            createStructuralSimpleDeclaration(field);
        Declarator d = new Declarator(declaration);
        d.setName(field.getName());
        declaration.addDeclarator(d);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public void exitTemplateDeclaration(IASTTemplateDeclaration declaration)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
     */
    public void exitTemplateSpecialization(IASTTemplateSpecialization specialization)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
     */
    public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
     */
    public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec)
    {
        LinkageSpecification linkage = (LinkageSpecification)domScopes.pop();
        getCurrentDOMScope().addDeclaration(linkage);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecification)
     */
    public void exitClassSpecifier(IASTClassSpecifier classSpecification)
    {
        ClassSpecifier c = (ClassSpecifier)getCurrentDOMScope();
        c.setTotalLength(
            classSpecification.getEndingOffset()
                + 1
                - classSpecification.getStartingOffset());
        domScopes.pop();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
     */
    public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
    {
        NamespaceDefinition definition = (NamespaceDefinition)domScopes.pop();
        definition.setTotalLength(
            namespaceDefinition.getEndingOffset()
                - namespaceDefinition.getStartingOffset());
        getCurrentDOMScope().addDeclaration(definition);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
     */
    public void exitInclusion(IASTInclusion inclusion)
    {
        // ignore
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
     */
    public void exitCompilationUnit(IASTCompilationUnit compilationUnit)
    {
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
    public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration)
    {
        SimpleDeclaration decl =
            startSimpleDeclaration(enumeration.getStartingOffset());
        EnumerationSpecifier es = new EnumerationSpecifier(decl);
        es.setStartingOffset(enumeration.getStartingOffset());
        es.setStartImage("enum");
        decl.setTypeSpecifier(es);
        es.setName(enumeration.getName());
        es.setTotalLength(
            enumeration.getEndingOffset()
                + 1
                - enumeration.getStartingOffset());
        Iterator i = enumeration.getEnumerators();
        while (i.hasNext())
        {
            IASTEnumerator enumerator = (IASTEnumerator)i.next();
            EnumeratorDefinition definition = new EnumeratorDefinition();
            es.addEnumeratorDefinition(definition);
            definition.setName(enumerator.getName());
            ((IOffsetable)definition).setStartingOffset(
                enumerator.getNameOffset());
            definition.setTotalLength(
                enumerator.getEndingOffset()
                    + 1
                    - enumerator.getStartingOffset());
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, int)
     */
    public void acceptClassReference(IASTClassReference reference)
    {
        // ignore
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
     */
    public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptPointerToFunction(org.eclipse.cdt.core.parser.ast.IASTPointerToFunction)
     */
    public void acceptPointerToFunction(IASTPointerToFunction function)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptPointerToMethod(org.eclipse.cdt.core.parser.ast.IASTPointerToMethod)
     */
    public void acceptPointerToMethod(IASTPointerToMethod method)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.cdt.core.parser.ast.IASTTypedefReference)
     */
    public void acceptTypedefReference(IASTTypedefReference reference)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.cdt.core.parser.ast.IASTNamespaceReference)
     */
    public void acceptNamespaceReference(IASTNamespaceReference reference)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
     */
    public void acceptEnumerationReference(IASTEnumerationReference reference)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.cdt.core.parser.ast.IASTVariableReference)
     */
    public void acceptVariableReference(IASTVariableReference reference)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.cdt.core.parser.ast.IASTFunctionReference)
     */
    public void acceptFunctionReference(IASTFunctionReference reference)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.cdt.core.parser.ast.IASTFieldReference)
     */
    public void acceptFieldReference(IASTFieldReference reference)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.cdt.core.parser.ast.IASTMethodReference)
     */
    public void acceptMethodReference(IASTMethodReference reference)
    {
        // TODO Auto-generated method stub
        
    }
}