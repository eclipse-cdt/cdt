/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
/**
 * @author jcamelon
 *
 */
public class DeclarationWrapper implements IDeclaratorOwner
{ 
    private boolean imaginary, complex;
    private boolean restrict;
    private int endOffset;
    private ITokenDuple name;
    private Type simpleType =
        IASTSimpleTypeSpecifier.Type.UNSPECIFIED;
    private boolean isSigned;
    private boolean isLong;
    private boolean isShort;
    private boolean isUnsigned;
    private final IASTTemplate templateDeclaration;
    private final IASTScope scope;
    private IASTTypeSpecifier typeSpecifier;
    private List declarators = new ArrayList();
    private boolean typeNamed = false;
    private boolean volatil = false;
    private boolean virtual = false;
    private boolean typedef = false;
    private boolean staticc = false;
    private boolean register = false;
    private boolean extern = false;
    private boolean explicit = false;
    private boolean constt = false;
    private int startingOffset = 0;
    private boolean auto = false,
        mutable = false,
        friend = false,
        inline = false;
	private int startingLine;
	private boolean global = false;
    /**
     * @param b
     */
    public void setAuto(boolean b)
    {
        auto = b;
    }
    /**
     * @return
     */
    public IASTScope getScope()
    {
        return scope;
    }
    
    public DeclarationWrapper( DeclarationWrapper wrapper )
    {
		this( wrapper.getScope(), wrapper.getStartingOffset(), wrapper.getStartingLine(), wrapper.getOwnerTemplate() );
		setAuto( wrapper.isAuto() );
		setComplex( wrapper.isComplex() );
		setConst( wrapper.isConst() );
		setEndingOffsetAndLineNumber( wrapper.getEndOffset(), wrapper.getEndLine() );
		setExplicit( wrapper.isExplicit() );
		setExtern(wrapper.isExtern() );
		setFriend(wrapper.isFriend());
		setGloballyQualified( wrapper.isGloballyQualified() );
		setImaginary( wrapper.isImaginary() );
		setInline( wrapper.isInline());
		setLong( wrapper.isLong() );
		setMutable( wrapper.isMutable() );
		setName( wrapper.getName() );
		setRegister(wrapper.isRegister() );
		setRestrict( wrapper.isRestrict() );
		setShort(wrapper.isShort());
		setSigned(wrapper.isSigned());
		setSimpleType(wrapper.getSimpleType());
		setStatic(wrapper.isStatic());
		setTypedef(wrapper.isTypedef());
		setTypenamed(wrapper.isTypeNamed());
		setTypeName(wrapper.getName());
		setTypeSpecifier(wrapper.getTypeSpecifier());
		setUnsigned(wrapper.isUnsigned());
		setVirtual(wrapper.isVirtual());
		setVolatile(wrapper.isVolatile());

    }
    
    /**
     * @param scope
     */
    public DeclarationWrapper(
        IASTScope scope,
        int startingOffset,
        int startingLine, IASTTemplate templateDeclaration)
    {
        this.scope = scope;
        this.startingOffset = startingOffset;
        this.startingLine = startingLine;
        this.templateDeclaration = templateDeclaration;
    }
    /**
     * @param b
     */
    public void setTypenamed(boolean b)
    {
        typeNamed = b;
    }
    /**
     * @param b
     */
    public void setMutable(boolean b)
    {
        mutable = b;
    }
    /**
     * @param b
     */
    public void setFriend(boolean b)
    {
        friend = b;
    }
    /**
     * @param b
     */
    public void setInline(boolean b)
    {
        inline = b;
    }
    /**
     * @param b
     */
    public void setRegister(boolean b)
    {
        register = b;
    }
    /**
     * @param b
     */
    public void setStatic(boolean b)
    {
        staticc = b;
    }
    /**
     * @param b
     */
    public void setTypedef(boolean b)
    {
        typedef = b;
    }
    /**
     * @param b
     */
    public void setVirtual(boolean b)
    {
        virtual = b;
    }
    /**
     * @param b
     */
    public void setVolatile(boolean b)
    {
        volatil = b;
    }
    /**
     * @param b
     */
    public void setExtern(boolean b)
    {
        extern = b;
    }
    /**
     * @param b
     */
    public void setExplicit(boolean b)
    {
        explicit = b;
    }
    /**
     * @param b
     */
    public void setConst(boolean b)
    {
        constt = b;
    }
    /**
     * @return
     */
    public boolean isAuto()
    {
        return auto;
    }
    /**
     * @return
     */
    public boolean isConst()
    {
        return constt;
    }
    /**
     * @return
     */
    public boolean isExplicit()
    {
        return explicit;
    }
    /**
     * @return
     */
    public boolean isExtern()
    {
        return extern;
    }
    /**
     * @return
     */
    public boolean isFriend()
    {
        return friend;
    }
    /**
     * @return
     */
    public boolean isInline()
    {
        return inline;
    }
    /**
     * @return
     */
    public boolean isMutable()
    {
        return mutable;
    }
    /**
     * @return
     */
    public boolean isRegister()
    {
        return register;
    }
    /**
     * @return
     */
    public int getStartingOffset()
    {
        return startingOffset;
    }
    
    public int getStartingLine()
	{
    	return startingLine;
    }
    /**
     * @return
     */
    public boolean isStatic()
    {
        return staticc;
    }
    /**
     * @return
     */
    public boolean isTypedef()
    {
        return typedef;
    }
    /**
     * @return
     */
    public boolean isTypeNamed()
    {
        return typeNamed;
    }
    /**
     * @return
     */
    public boolean isVirtual()
    {
        return virtual;
    }
    /**
     * @return
     */
    public boolean isVolatile()
    {
        return volatil;
    }
    public void addDeclarator(Declarator d)
    {
        declarators.add(d);
    }
    public Iterator getDeclarators()
    {
        return declarators.iterator();
    }
    /**
     * @return
     */
    public IASTTypeSpecifier getTypeSpecifier()
    {
        return typeSpecifier;
    }
    /**
     * @param specifier
     */
    public void setTypeSpecifier(IASTTypeSpecifier specifier)
    {
        typeSpecifier = specifier;
    }
    private IASTFactory astFactory = null;
	private int endLine;
    /**
     * @param requestor
     */
    public List createASTNodes(IASTFactory astFactoryToWorkWith) throws ASTSemanticException, BacktrackException
    {
        this.astFactory = astFactoryToWorkWith;
        Iterator i = declarators.iterator();
        List l = new ArrayList();
        while (i.hasNext())
            l.add(createASTNode((Declarator)i.next()));
        return l;
    }
    /**
     * @param declarator
     */
    private IASTDeclaration createASTNode(Declarator declarator) throws ASTSemanticException, BacktrackException
    {
        boolean isWithinClass = false;//(getScope() instanceof IASTClassSpecifier); //TODO fix this for COMPLETE_PARSE
    	if( getScope() instanceof IASTClassSpecifier ){
    		isWithinClass = true;
    	} else if ( getScope() instanceof IASTTemplateDeclaration ){
    		isWithinClass = (((IASTTemplateDeclaration)getScope()).getOwnerScope() instanceof IASTClassSpecifier);
    	}
    	
        boolean isFunction = declarator.isFunction();
        boolean hasInnerDeclarator = ( declarator.getOwnedDeclarator() != null );
                
        if( hasInnerDeclarator )
        	return createIndirectDeclaration( declarator ); 
        	
        if (isTypedef())
            return createTypedef(declarator, false);

        if (isWithinClass )
        {
        	if( isFunction)
           		return createMethodASTNode(declarator, false);
        	else 
        		if( declarator.hasFunctionBody() )
        			throw new ASTSemanticException( (IProblem)null );
            	return createFieldASTNode(declarator, false );
        }
       	if (isFunction)
        	return createFunctionASTNode(declarator, false);
       	else 
       		if( declarator.hasFunctionBody() )
       			throw new ASTSemanticException( (IProblem)null );
           	return createVariableASTNode(declarator, false);

    }
    /**
     * @param declarator
     * @return
     */
    private IASTDeclaration createIndirectDeclaration(Declarator declarator) throws BacktrackException, ASTSemanticException
    {    
        if( declarator.getOwnedDeclarator().getOwnedDeclarator() == null )
        {

        	Declarator d = declarator.getOwnedDeclarator();
        	Iterator i = d.getPointerOperators().iterator();
        	boolean isWithinClass = scope instanceof IASTClassSpecifier;
			boolean isFunction = (declarator.getParameters().size() != 0); 
        	if( !i.hasNext() )
        	{
                
				if (isTypedef())
					return createTypedef(declarator, true);

				if (isWithinClass )
				{
					if( isFunction)
						return createMethodASTNode(declarator, true);
					return createFieldASTNode(declarator, true );
				}
				if (isFunction)
					return createFunctionASTNode(declarator, true);
				return createVariableASTNode(declarator, true);
			}
        	
			List convertedParms = createParameterList( declarator.getParameters() );        	
        	IASTAbstractDeclaration abs = null;
            abs =
                    astFactory.createAbstractDeclaration(
                        constt,
                        volatil,
                        getTypeSpecifier(),
                        declarator.getPointerOperators(),
                        declarator.getArrayModifiers(),
                        convertedParms,
                        (ASTPointerOperator)i.next());
            
        	ITokenDuple nameDuple = ( d.getPointerOperatorNameDuple() != null ) ? TokenFactory.createTokenDuple( d.getPointerOperatorNameDuple(), d.getNameDuple() ) : d.getNameDuple(); 
        	
        	if( typedef )
				return astFactory.createTypedef(scope, nameDuple.toString(), abs,
						getStartingOffset(), getStartingLine(), d
								.getNameStartOffset(), d.getNameEndOffset(), d
								.getNameLine());
        	
        	if( isWithinClass )
        		return astFactory.createField( scope, nameDuple, auto, d.getInitializerClause(), d.getBitFieldExpression(), abs, mutable, extern, register, staticc, getStartingOffset(), getStartingLine(), d.getNameStartOffset(), d.getNameEndOffset(), d.getNameLine(), d.getConstructorExpression(), ((IASTClassSpecifier)scope).getCurrentVisibilityMode() );
        	 
        	return astFactory.createVariable( scope, nameDuple, auto, d.getInitializerClause(), d.getBitFieldExpression(), abs, mutable, extern, register, staticc, getStartingOffset(), getStartingLine(), d.getNameStartOffset(), d.getNameEndOffset(), d.getNameLine(), d.getConstructorExpression() );        	
        	
        }
       	throw new BacktrackException();

    }
    
    /**
     * @param declarator
     * @return
     */
    private IASTTypedefDeclaration createTypedef(Declarator declarator, boolean nested ) throws ASTSemanticException
    {
		return astFactory.createTypedef(scope, nested ? declarator
				.getOwnedDeclarator().getName() : declarator.getName(),
				astFactory.createAbstractDeclaration(constt, volatil,
						getTypeSpecifier(), declarator.getPointerOperators(),
						declarator.getArrayModifiers(), null, null),
				startingOffset, getStartingLine(), declarator
						.getNameStartOffset(), declarator.getNameEndOffset(),
				declarator.getNameLine());
    }
    /**
     * @param declarator
     * @return
     */
    private IASTMethod createMethodASTNode(Declarator declarator, boolean nested) throws ASTSemanticException
    {
    	IASTScope classifierScope = getScope();
    	if( classifierScope instanceof IASTTemplateDeclaration ){
    		classifierScope = ((IASTTemplateDeclaration)classifierScope).getOwnerScope();
    	}
		return astFactory.createMethod(scope, nested ? declarator
				.getOwnedDeclarator().getNameDuple() : declarator
				.getNameDuple(),
				createParameterList(declarator.getParameters()), astFactory
						.createAbstractDeclaration(constt, volatil,
								getTypeSpecifier(), declarator
										.getPointerOperators(), declarator
										.getArrayModifiers(), null, null),
				declarator.getExceptionSpecification(), inline, friend,
				staticc, startingOffset, getStartingLine(), declarator
						.getNameStartOffset(), declarator.getNameEndOffset(),
				declarator.getNameLine(), templateDeclaration, declarator
						.isConst(), declarator.isVolatile(), virtual, explicit,
				declarator.isPureVirtual(), ((IASTClassSpecifier) classifierScope)
						.getCurrentVisibilityMode(), declarator
						.getConstructorMemberInitializers(), declarator
						.hasFunctionBody(), declarator.hasFunctionTryBlock(),
				declarator.isVarArgs());
    }
    /**
     * @param declarator
     * @return
     */
    private IASTFunction createFunctionASTNode(Declarator declarator, boolean nested) throws ASTSemanticException
    {
		return astFactory.createFunction(scope, nested ? declarator
				.getOwnedDeclarator().getNameDuple() : declarator
				.getNameDuple(),
				createParameterList(declarator.getParameters()), astFactory
						.createAbstractDeclaration(constt, volatil,
								getTypeSpecifier(), declarator
										.getPointerOperators(), declarator
										.getArrayModifiers(), null, null),
				declarator.getExceptionSpecification(), inline, friend,
				staticc, startingOffset, getStartingLine(), declarator
						.getNameStartOffset(), declarator.getNameEndOffset(),
				declarator.getNameLine(), templateDeclaration, declarator
						.isConst(), declarator.isVolatile(), virtual, explicit,
				declarator.isPureVirtual(), declarator
						.getConstructorMemberInitializers(), declarator
						.hasFunctionBody(), declarator.hasFunctionTryBlock(),
				declarator.isVarArgs());
    }
    /**
     * @param declarator
     * @return
     */
    private IASTField createFieldASTNode(Declarator declarator, boolean nested) throws ASTSemanticException
    {
       return astFactory.createField(
                scope,
            	nested ? declarator.getOwnedDeclarator().getNameDuple() : declarator.getNameDuple(),
                auto,
                declarator.getInitializerClause(),
                declarator.getBitFieldExpression(),
                astFactory.createAbstractDeclaration(
                    constt,
            		volatil,
                    getTypeSpecifier(),
                    declarator.getPointerOperators(), declarator.getArrayModifiers(), null, null),
                mutable,
                extern,
                register,
                staticc,
                startingOffset,
                getStartingLine(),
            	declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), declarator.getConstructorExpression(), ((IASTClassSpecifier)scope).getCurrentVisibilityMode());
    }

    private List createParameterList(List currentParameters) throws ASTSemanticException
    {
        List result = new ArrayList();
        Iterator i = currentParameters.iterator();
        while (i.hasNext())
        {
            DeclarationWrapper wrapper = (DeclarationWrapper)i.next();
            Iterator j = wrapper.getDeclarators();
            while (j.hasNext())
            {
                Declarator declarator = (Declarator)j.next();

                result.add(
                    astFactory.createParameterDeclaration(
                        wrapper.isConst(),
                        wrapper.isVolatile(),
                        wrapper.getTypeSpecifier(),
                        declarator.getPointerOperators(),
                        declarator.getArrayModifiers(),
                        null, null, declarator.getName() == null
                                        ? "" //$NON-NLS-1$
                                        : declarator.getName(), declarator.getInitializerClause(), wrapper.getStartingOffset(), getStartingLine(), declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), wrapper.getEndOffset(), getEndLine()));
            }
        }
        return result;
    }
    /**
     * @param declarator
     * @return
     */
    private IASTVariable createVariableASTNode(Declarator declarator, boolean nested ) throws ASTSemanticException
    {
        return astFactory.createVariable(
            scope,
        	nested ? declarator.getOwnedDeclarator().getNameDuple() : declarator.getNameDuple(),
            isAuto(),
            declarator.getInitializerClause(),
            declarator.getBitFieldExpression(),
            astFactory.createAbstractDeclaration(
                constt,
                volatil,
                getTypeSpecifier(),
                declarator.getPointerOperators(), declarator.getArrayModifiers(), null, null),
            mutable,
            extern,
            register,
            staticc,
            getStartingOffset(),
            getStartingLine(), declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), declarator.getConstructorExpression());

    }        
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclaratorOwner#getDeclarationWrapper()
     */
    public DeclarationWrapper getDeclarationWrapper()
    {
        return this;
    }
    /**
     * @return
     */
    public boolean isUnsigned()
    {
        return isUnsigned;
    }
    /**
     * @return
     */
    public boolean isSigned()
    {
        return isSigned;
    }
    /**
     * @return
     */
    public boolean isShort()
    {
        return isShort;
    }
    /**
     * @return
     */
    public boolean isLong()
    {
        return isLong;
    }
    /**
     * @param b
     */
    public void setLong(boolean b)
    {
        isLong = b;
    }
    /**
     * @param b
     */
    public void setShort(boolean b)
    {
        isShort = b;
    }
    /**
     * @param b
     */
    public void setSigned(boolean b)
    {
        isSigned = b;
    }
    /**
     * @param b
     */
    public void setUnsigned(boolean b)
    {
        isUnsigned = b;
    }
    /**
     * @return
     */
    public Type getSimpleType()
    {
        return simpleType;
    }
    /**
     * @param type
     */
    public void setSimpleType(Type type)
    {
        simpleType = type;
    }
    /**
     * @param duple
     */
    public void setTypeName(ITokenDuple duple)
    {
        name = duple;
    }
    /**
     * @return
     */
    public ITokenDuple getName()
    {
        return name;
    }
    /**
     * @param duple
     */
    public void setName(ITokenDuple duple)
    {
        name = duple;
    }
    /**
     * @return
     */
    public IASTTemplate getOwnerTemplate()
    {
        return templateDeclaration;
    }
    /**
     * @param i
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
        endOffset = offset;
        endLine = lineNumber;
    }
    /**
     * @return
     */
    public int getEndOffset()
    {
        return endOffset;
    }
    
    public int getEndLine()
	{
    	return endLine;
    }
    /**
     * @param b
     */
    public void setRestrict(boolean b)
    {
        restrict = b;
    }
    

    /**
     * @return
     */
    public boolean isRestrict()
    {
        return restrict;
    }
    /**
     * @param b
     */
    public void setImaginary(boolean b)
    {
        imaginary = b;
    }

    /**
     * @return
     */
    public boolean isComplex()
    {
        return complex;
    }

    /**
     * @return
     */
    public boolean isImaginary()
    {
        return imaginary;
    }

    /**
     * @param b
     */
    public void setComplex(boolean b)
    {
        complex = b;
    }
	/**
	 * @param b
	 */
	public void setGloballyQualified(boolean b) {
		global = b;
	}
	
	public boolean isGloballyQualified(){
		return global;
	}
	
	private Hashtable extensionParameters = new Hashtable();
	/**
	 * @param key
	 * @param typeOfExpression
	 */
	public void setExtensionParameter(String key, Object value) {
		extensionParameters.put( key, value );
	}
	
	public Hashtable getExtensionParameters()
	{
		return extensionParameters;
	}
	/**
	 * @return
	 */
	public boolean consumedRawType() {
		return( getSimpleType() != IASTSimpleTypeSpecifier.Type.UNSPECIFIED );
	}
}
