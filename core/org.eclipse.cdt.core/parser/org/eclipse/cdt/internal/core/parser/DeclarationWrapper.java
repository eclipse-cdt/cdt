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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	private int flag = 0;
	protected void setBit(boolean b, int mask){
		if( b ){
			flag = flag | mask; 
		} else {
			flag = flag & ~mask; 
		} 
	}
	
	protected boolean checkBit(int mask){
		return (flag & mask) != 0;
	}	

	private static final int DEFAULT_LIST_SIZE = 4;	
	
	protected static final int IS_IMAGINARY   = 0x00000010;
	protected static final int IS_COMPLEX     = 0x00000020;
	protected static final int IS_RESTRICT    = 0x00000040;
	protected static final int IS_SIGNED      = 0x00000080;
	protected static final int IS_SHORT       = 0x00000100;
	protected static final int IS_UNSIGNED    = 0x00000200;
	protected static final int IS_LONG        = 0x00000400;
	protected static final int IS_TYPENAMED   = 0x00000800;
	protected static final int IS_VOLATILE    = 0x00001000;
	protected static final int IS_VIRTUAL     = 0x00002000;
	protected static final int IS_TYPEDEF     = 0x00004000;
	protected static final int IS_STATIC      = 0x00008000;
	protected static final int IS_REGISTER    = 0x00010000;
	protected static final int IS_EXTERN      = 0x00020000;
	protected static final int IS_EXPLICIT    = 0x00040000;
	protected static final int IS_CONST       = 0x00080000;
	protected static final int IS_AUTO        = 0x00100000;
	protected static final int IS_GLOBAL      = 0x00200000;
	protected static final int IS_MUTABLE     = 0x00400000;
	protected static final int IS_FRIEND      = 0x00800000;
	protected static final int IS_INLINE      = 0x01000000;


    private int startingOffset = 0;
	private int startingLine;
    private int endOffset;
    
    private ITokenDuple name;
    private Type simpleType = IASTSimpleTypeSpecifier.Type.UNSPECIFIED;
    private final IASTTemplate templateDeclaration;
    private final IASTScope scope;
    private IASTTypeSpecifier typeSpecifier;
	
    private List declarators = Collections.EMPTY_LIST;
    /**
     * @param b
     */
    public void setAuto(boolean b)
    {
        setBit( b, IS_AUTO );
    }
    /**
     * @return
     */
    public IASTScope getScope()
    {
        return scope;
    }
        
    /**
     * @param scope
     * @param filename TODO
     */
    public DeclarationWrapper(
        IASTScope scope,
        int startingOffset,
        int startingLine, IASTTemplate templateDeclaration, char[] filename)
    {
        this.scope = scope;
        this.startingOffset = startingOffset;
        this.startingLine = startingLine;
        this.templateDeclaration = templateDeclaration;
        this.fn = filename;
    }
    /**
     * @param b
     */
    public void setTypenamed(boolean b)
    {
    	setBit( b, IS_TYPENAMED );
    }
    /**
     * @param b
     */
    public void setMutable(boolean b)
    {
    	setBit( b, IS_MUTABLE);
    }
    /**
     * @param b
     */
    public void setFriend(boolean b)
    {
    	setBit( b, IS_FRIEND );
    }
    /**
     * @param b
     */
    public void setInline(boolean b)
    {
        setBit( b, IS_INLINE );
    }
    /**
     * @param b
     */
    public void setRegister(boolean b)
    {
        setBit( b, IS_REGISTER );
    }
    /**
     * @param b
     */
    public void setStatic(boolean b)
    {
        setBit( b, IS_STATIC );
    }
    /**
     * @param b
     */
    public void setTypedef(boolean b)
    {
        setBit( b, IS_TYPEDEF );
    }
    /**
     * @param b
     */
    public void setVirtual(boolean b)
    {
        setBit( b, IS_VIRTUAL );
    }
    /**
     * @param b
     */
    public void setVolatile(boolean b)
    {
        setBit( b, IS_VOLATILE );
    }
    /**
     * @param b
     */
    public void setExtern(boolean b)
    {
        setBit( b, IS_EXTERN );
    }
    /**
     * @param b
     */
    public void setExplicit(boolean b)
    {
        setBit( b, IS_EXPLICIT );
    }
    /**
     * @param b
     */
    public void setConst(boolean b)
    {
        setBit( b, IS_CONST );
    }
    /**
     * @return
     */
    public boolean isAuto()
    {
        return checkBit( IS_AUTO );
    }
    /**
     * @return
     */
    public boolean isConst()
    {
        return checkBit( IS_CONST );
    }
    /**
     * @return
     */
    public boolean isExplicit()
    {
    	return checkBit( IS_EXPLICIT );
    }
    /**
     * @return
     */
    public boolean isExtern()
    {
    	return checkBit( IS_EXTERN );
    }
    /**
     * @return
     */
    public boolean isFriend()
    {
    	return checkBit( IS_FRIEND );
    }
    /**
     * @return
     */
    public boolean isInline()
    {
    	return checkBit( IS_INLINE );
    }
    /**
     * @return
     */
    public boolean isMutable()
    {
    	return checkBit( IS_MUTABLE );
    }
    /**
     * @return
     */
    public boolean isRegister()
    {
    	return checkBit( IS_REGISTER );
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
    	return checkBit( IS_STATIC );
    }
    /**
     * @return
     */
    public boolean isTypedef()
    {
    	return checkBit( IS_TYPEDEF );
    }
    /**
     * @return
     */
    public boolean isTypeNamed()
    {
    	return checkBit( IS_TYPENAMED );
    }
    /**
     * @return
     */
    public boolean isVirtual()
    {
    	return checkBit( IS_VIRTUAL );
    }
    /**
     * @return
     */
    public boolean isVolatile()
    {
    	return checkBit( IS_VOLATILE );
    }
    public void addDeclarator(Declarator d)
    {
    	if( declarators == Collections.EMPTY_LIST )
    		declarators = new ArrayList(DEFAULT_LIST_SIZE);
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

	private final char[] fn;
    /**
     * @param requestor
     */
    public List createASTNodes(IASTFactory astFactoryToWorkWith) throws ASTSemanticException, BacktrackException
    {
        this.astFactory = astFactoryToWorkWith;
        if( declarators.isEmpty() ) return Collections.EMPTY_LIST;
        List l = new ArrayList(declarators.size());
        for( int i = 0; i < declarators.size(); ++i )
            l.add(createASTNode((Declarator)declarators.get(i)));
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
                        isConst(),
                        isVolatile(),
                        getTypeSpecifier(),
                        declarator.getPointerOperators(),
                        declarator.getArrayModifiers(),
                        convertedParms,
                        (ASTPointerOperator)i.next());
            
        	ITokenDuple nameDuple = ( d.getPointerOperatorNameDuple() != null ) ? TokenFactory.createTokenDuple( d.getPointerOperatorNameDuple(), d.getNameDuple() ) : d.getNameDuple(); 
        	
        	if( isTypedef() )
				return astFactory.createTypedef(scope, nameDuple.toCharArray(), abs,
						getStartingOffset(), getStartingLine(), d
								.getNameStartOffset(), d.getNameEndOffset(), d
								.getNameLine(), fn);
        	
        	if( isWithinClass )
        		return astFactory.createField( scope, nameDuple, isAuto(), d.getInitializerClause(), d.getBitFieldExpression(), abs, isMutable(), isExtern(), isRegister(), isStatic(), getStartingOffset(), getStartingLine(), d.getNameStartOffset(), d.getNameEndOffset(), d.getNameLine(), d.getConstructorExpression(), ((IASTClassSpecifier)scope).getCurrentVisibilityMode(), fn );
        	 
        	return astFactory.createVariable( scope, nameDuple, isAuto(), d.getInitializerClause(), d.getBitFieldExpression(), abs, isMutable(), isExtern(), isRegister(), isStatic(), getStartingOffset(), getStartingLine(), d.getNameStartOffset(), d.getNameEndOffset(), d.getNameLine(), d.getConstructorExpression(), fn );        	
        	
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
				astFactory.createAbstractDeclaration(isConst(), isVolatile(),
						getTypeSpecifier(), declarator.getPointerOperators(),
						declarator.getArrayModifiers(), null, null),
				startingOffset, getStartingLine(), declarator
						.getNameStartOffset(), declarator.getNameEndOffset(),
				declarator.getNameLine(), fn);
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
						.createAbstractDeclaration(isConst(), isVolatile(),
								getTypeSpecifier(), declarator
										.getPointerOperators(), declarator
										.getArrayModifiers(), null, null),
				declarator.getExceptionSpecification(), isInline(), isFriend(),
				isStatic(), startingOffset, getStartingLine(), declarator
						.getNameStartOffset(), declarator.getNameEndOffset(),
				declarator.getNameLine(), templateDeclaration, declarator
						.isConst(), declarator.isVolatile(), isVirtual(), isExplicit(),
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
						.createAbstractDeclaration(isConst(), isVolatile(),
								getTypeSpecifier(), declarator
										.getPointerOperators(), declarator
										.getArrayModifiers(), null, null),
				declarator.getExceptionSpecification(), isInline(), isFriend(),
				isStatic(), startingOffset, getStartingLine(), declarator
						.getNameStartOffset(), declarator.getNameEndOffset(),
				declarator.getNameLine(), templateDeclaration, declarator
						.isConst(), declarator.isVolatile(), isVirtual(), isExplicit(),
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
                isAuto(),
                declarator.getInitializerClause(),
                declarator.getBitFieldExpression(),
                astFactory.createAbstractDeclaration(
                    isConst(),
            		isVolatile(),
                    getTypeSpecifier(),
                    declarator.getPointerOperators(), declarator.getArrayModifiers(), null, null),
                isMutable(),
                isExtern(),
                isRegister(),
                isStatic(),
                startingOffset,
                getStartingLine(),
            	declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), declarator.getConstructorExpression(), ((IASTClassSpecifier)scope).getCurrentVisibilityMode(), fn);
    }

    private List createParameterList(List currentParameters) throws ASTSemanticException
    {
    	if( currentParameters.isEmpty() ) return Collections.EMPTY_LIST;
        List result = new ArrayList(currentParameters.size());
        for( int i = 0; i < currentParameters.size(); ++i )
        {
            DeclarationWrapper wrapper = (DeclarationWrapper)currentParameters.get(i);
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
                        null, null, declarator.getName(), declarator.getInitializerClause(), wrapper.getStartingOffset(), getStartingLine(), declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), wrapper.getEndOffset(), getEndLine(), wrapper.fn ));
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
                isConst(),
                isVolatile(),
                getTypeSpecifier(),
                declarator.getPointerOperators(), declarator.getArrayModifiers(), null, null),
            isMutable(),
            isExtern(),
            isRegister(),
            isStatic(),
            getStartingOffset(),
            getStartingLine(), declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), declarator.getConstructorExpression(), fn);

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
    	return checkBit( IS_UNSIGNED );
    }
    /**
     * @return
     */
    public boolean isSigned()
    {
    	return checkBit( IS_SIGNED );
    }
    /**
     * @return
     */
    public boolean isShort()
    {
    	return checkBit( IS_SHORT );
    }
    /**
     * @return
     */
    public boolean isLong()
    {
    	return checkBit( IS_LONG );
    }
    /**
     * @param b
     */
    public void setLong(boolean b)
    {
    	setBit( b, IS_LONG );
    }
    /**
     * @param b
     */
    public void setShort(boolean b)
    {
    	setBit( b, IS_SHORT );
    }
    /**
     * @param b
     */
    public void setSigned(boolean b)
    {
    	setBit( b, IS_SIGNED );
    }
    /**
     * @param b
     */
    public void setUnsigned(boolean b)
    {
        setBit( b, IS_UNSIGNED );
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
    public final ITokenDuple getName()
    {
        return name;
    }

    /**
     * @return
     */
    public final IASTTemplate getOwnerTemplate()
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
        setBit( b, IS_RESTRICT );
    }
    

    /**
     * @return
     */
    public boolean isRestrict()
    {
    	return checkBit( IS_RESTRICT );
    }
    /**
     * @param b
     */
    public void setImaginary(boolean b)
    {
    	setBit( b, IS_IMAGINARY );
    }

    /**
     * @return
     */
    public boolean isComplex()
    {
    	return checkBit( IS_COMPLEX );
    }

    /**
     * @return
     */
    public boolean isImaginary()
    {
    	return checkBit( IS_IMAGINARY );
    }

    /**
     * @param b
     */
    public void setComplex(boolean b)
    {
        setBit( b, IS_COMPLEX );
    }
	/**
	 * @param b
	 */
	public void setGloballyQualified(boolean b) {
		setBit( b, IS_GLOBAL );
	}
	
	public boolean isGloballyQualified(){
		return checkBit( IS_GLOBAL );
	}
	
	private Map extensionParameters = Collections.EMPTY_MAP;
	/**
	 * @param key
	 * @param typeOfExpression
	 */
	public void setExtensionParameter(String key, Object value) {
		if( extensionParameters == Collections.EMPTY_MAP )
			extensionParameters = new Hashtable( 4 );
		extensionParameters.put( key, value );
	}
	
	public Map getExtensionParameters()
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
