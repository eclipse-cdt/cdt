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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
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
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
/**
 * @author jcamelon
 *
 */
public class DeclarationWrapper implements IDeclaratorOwner
{ 
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
    /**
     * @param scope
     */
    public DeclarationWrapper(
        IASTScope scope,
        int startingOffset,
        IASTTemplate templateDeclaration)
    {
        this.scope = scope;
        this.startingOffset = startingOffset;
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
        return Collections.unmodifiableList(declarators).iterator();
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
    /**
     * @param requestor
     */
    public List createASTNodes(IASTFactory astFactory) throws ASTSemanticException
    {
        this.astFactory = astFactory;
        Iterator i = declarators.iterator();
        List l = new ArrayList();
        while (i.hasNext())
            l.add(createASTNode((Declarator)i.next()));
        return l;
    }
    /**
     * @param declarator
     */
    private IASTDeclaration createASTNode(Declarator declarator) throws ASTSemanticException
    {
        boolean isWithinClass = (getScope() instanceof IASTClassSpecifier); //TODO fix this for COMPLETE_PARSE
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
            	return createFieldASTNode(declarator, false );
        }
        else 
        {	
        	if (isFunction)
               		return createFunctionASTNode(declarator, false);
        	else 
            	return createVariableASTNode(declarator, false);
        }
    }
    /**
     * @param declarator
     * @return
     */
    private IASTDeclaration createIndirectDeclaration(Declarator declarator) throws ASTSemanticException
    {    
        if( declarator.getOwnedDeclarator().getOwnedDeclarator() == null )
        {

        	Declarator d = declarator.getOwnedDeclarator();
        	Iterator i = d.getPtrOps().iterator();
        	if( !i.hasNext() )
        	{
                boolean isWithinClass = scope instanceof IASTClassSpecifier;
				boolean isFunction = (declarator.getParameters().size() != 0); 
				if (isTypedef())
					return createTypedef(declarator, true);

				if (isWithinClass )
				{
					if( isFunction)
						return createMethodASTNode(declarator, true);
					else 
						return createFieldASTNode(declarator, true );
				}
				else 
				{	
					if (isFunction)
						return createFunctionASTNode(declarator, true);
					else 
						return createVariableASTNode(declarator, true);
				}

        	}
        	
			List convertedParms = createParameterList( declarator.getParameters() );        	
        	IASTAbstractDeclaration abs = astFactory.createAbstractDeclaration(
        		constt, volatil, getTypeSpecifier(), declarator.getPtrOps(), declarator.getArrayModifiers(), 
        			convertedParms, (ASTPointerOperator)i.next() );
        	String name = ( d.getPointerOperatorNameDuple() != null ) ? d.getPointerOperatorNameDuple().toString() + d.getName() : d.getName(); 
        	if( typedef )
				return astFactory.createTypedef(
					scope,
					name,
					abs, getStartingOffset(), d.getNameStartOffset() ); 
        	else
        		return astFactory.createVariable( scope, name, auto, d.getInitializerClause(), d.getBitFieldExpression(), abs, mutable, extern, register, staticc, getStartingOffset(), d.getNameStartOffset(), d.getConstructorExpression() );
        	
        }
        else
        {
        	throw new ASTSemanticException(); 	
        }
        
    }
    
    /**
     * @param declarator
     * @return
     */
    private IASTTypedefDeclaration createTypedef(Declarator declarator, boolean nested ) throws ASTSemanticException
    {
        return astFactory.createTypedef(
            scope,
            nested ? declarator.getOwnedDeclarator().getName() : declarator.getName(),
            astFactory.createAbstractDeclaration(
                constt,
                volatil,
                getTypeSpecifier(),
                declarator.getPtrOps(), declarator.getArrayModifiers(), null, null), startingOffset, declarator.getNameStartOffset());
    }
    /**
     * @param declarator
     * @return
     */
    private IASTMethod createMethodASTNode(Declarator declarator, boolean nested) throws ASTSemanticException
    {
        return astFactory
            .createMethod(
                scope,
                nested ? declarator.getOwnedDeclarator().getName() : declarator.getName(),
                createParameterList(declarator.getParameters()),
                astFactory.createAbstractDeclaration(
                    constt,
					volatil,
                    getTypeSpecifier(),
                    declarator.getPtrOps(), declarator.getArrayModifiers(), null, null),
                declarator.getExceptionSpecification(),
                inline,
                friend,
                staticc,
                startingOffset,
                declarator.getNameStartOffset(),
                templateDeclaration,
                declarator.isConst(),
                declarator.isVolatile(),
		        virtual,
	            explicit,
	            declarator.isPureVirtual(),
	            ((IASTClassSpecifier)scope).getCurrentVisibilityMode(), declarator.getConstructorMemberInitializers(), 
	            declarator.hasFunctionBody());
    }
    /**
     * @param declarator
     * @return
     */
    private IASTFunction createFunctionASTNode(Declarator declarator, boolean nested) throws ASTSemanticException
    {
        return astFactory.createFunction(
            scope,
			nested ? declarator.getOwnedDeclarator().getName() : declarator.getName(),
            createParameterList(declarator.getParameters()),
            astFactory.createAbstractDeclaration(
                constt,
				volatil,
                getTypeSpecifier(),
                declarator.getPtrOps(), declarator.getArrayModifiers(), null, null),
            declarator.getExceptionSpecification(),
            inline,
            friend,
            staticc,
            startingOffset,
            declarator.getNameStartOffset(),
            templateDeclaration,
		declarator.isConst(),
		declarator.isVolatile(),
		virtual,
		explicit,
		declarator.isPureVirtual(),
		ASTAccessVisibility.PUBLIC,
		declarator.getConstructorMemberInitializers(), 
		declarator.hasFunctionBody() );
    }
    /**
     * @param declarator
     * @return
     */
    private IASTField createFieldASTNode(Declarator declarator, boolean nested) throws ASTSemanticException
    {
        return astFactory.createField(
            scope,
			nested ? declarator.getOwnedDeclarator().getName() : declarator.getName(),
            auto,
            declarator.getInitializerClause(),
            declarator.getBitFieldExpression(),
            astFactory.createAbstractDeclaration(
                constt,
				volatil,
                getTypeSpecifier(),
                declarator.getPtrOps(), declarator.getArrayModifiers(), null, null),
            mutable,
            extern,
            register,
            staticc,
            startingOffset,
            declarator.getNameStartOffset(),
            declarator.getConstructorExpression(), ((IASTClassSpecifier)scope).getCurrentVisibilityMode());
    }
    private List createParameterList(List currentParameters)
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
                        declarator.getPtrOps(),
                        declarator.getArrayModifiers(),
                        null, null, declarator.getName() == null
                                        ? ""
                                        : declarator.getName(), declarator.getInitializerClause()));
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
			nested ? declarator.getOwnedDeclarator().getName() : declarator.getName(),
            isAuto(),
            declarator.getInitializerClause(),
            declarator.getBitFieldExpression(),
            astFactory.createAbstractDeclaration(
                constt,
                volatil,
                getTypeSpecifier(),
                declarator.getPtrOps(), declarator.getArrayModifiers(), null, null),
            mutable,
            extern,
            register,
            staticc,
            getStartingOffset(),
            declarator.getNameStartOffset(), declarator.getConstructorExpression());
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
}
