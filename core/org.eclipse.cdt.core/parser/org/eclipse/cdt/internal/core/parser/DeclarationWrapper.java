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

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTPointerToMethod;
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
    public List createASTNodes(IASTFactory astFactory)
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
    private IASTDeclaration createASTNode(Declarator declarator)
    {
        boolean isWithinClass = (getScope() instanceof IASTClassSpecifier); //TODO fix this for COMPLETE_PARSE
        boolean isFunction = declarator.isFunction();
        boolean hasInnerDeclarator = ( declarator.getOwnedDeclarator() != null );
        
        if( hasInnerDeclarator )
        {
        	ITokenDuple innerPointerName = declarator.getOwnedDeclarator().getPointerOperatorNameDuple();
        	if( innerPointerName != null && innerPointerName.getLastToken().getType() == IToken.tCOLONCOLON )
        		return createP2MethodASTNode(declarator);
        	else
        		return createP2FunctionASTNode( declarator );
        }
        
        if (isTypedef())
            return createTypedef(declarator);

        if (isWithinClass )
        {
        	if( isFunction)
           		return createMethodASTNode(declarator);
        	else 
            	return createFieldASTNode(declarator);
        }
        else 
        {	
        	if (isFunction)
               		return createFunctionASTNode(declarator);
        	else 
            	return createVariableASTNode(declarator);
        }
    }
    /**
     * @param declarator
     * @return
     */
    private IASTTypedefDeclaration createTypedef(Declarator declarator)
    {
        return astFactory.createTypedef(
            scope,
            declarator.getName(),
            astFactory.createAbstractDeclaration(
                constt,
                getTypeSpecifier(),
                declarator.getPtrOps(),
                declarator.getArrayModifiers()), startingOffset, declarator.getNameStartOffset());
    }
    /**
     * @param declarator
     * @return
     */
    private IASTMethod createMethodASTNode(Declarator declarator)
    {
        return astFactory
            .createMethod(
                scope,
                declarator.getName(),
                createParameterList(declarator.getParameters()),
                astFactory.createAbstractDeclaration(
                    constt,
                    getTypeSpecifier(),
                    declarator.getPtrOps(),
                    declarator.getArrayModifiers()),
                declarator.getExceptionSpecification(),
                inline,
                friend,
                staticc,
                startingOffset,
                declarator.getNameStartOffset(),
                templateDeclaration,
                declarator.isConst(),
                declarator.isVolatile(),
                false,
        // isConstructor
        false, // isDestructor
        virtual,
            explicit,
            declarator.isPureVirtual(),
            ((IASTClassSpecifier)scope).getCurrentVisibilityMode());
    }
    /**
     * @param declarator
     * @return
     */
    private IASTFunction createFunctionASTNode(Declarator declarator)
    {
        return astFactory.createFunction(
            scope,
            declarator.getName(),
            createParameterList(declarator.getParameters()),
            astFactory.createAbstractDeclaration(
                constt,
                getTypeSpecifier(),
                declarator.getPtrOps(),
                declarator.getArrayModifiers()),
            declarator.getExceptionSpecification(),
            inline,
            friend,
            staticc,
            startingOffset,
            declarator.getNameStartOffset(),
            templateDeclaration);
    }
    /**
     * @param declarator
     * @return
     */
    private IASTField createFieldASTNode(Declarator declarator)
    {
        return astFactory.createField(
            scope,
            declarator.getName(),
            auto,
            declarator.getInitializerClause(),
            declarator.getBitFieldExpression(),
            astFactory.createAbstractDeclaration(
                constt,
                getTypeSpecifier(),
                declarator.getPtrOps(),
                declarator.getArrayModifiers()),
            mutable,
            extern,
            register,
            staticc,
            startingOffset,
            declarator.getNameEndOffset(),
            ((IASTClassSpecifier)scope).getCurrentVisibilityMode());
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
                        wrapper.getTypeSpecifier(),
                        declarator.getPtrOps(),
                        declarator.getArrayModifiers(),
                        declarator.getName() == null
                            ? ""
                            : declarator.getName(),
                        declarator.getInitializerClause()));
            }
        }
        return result;
    }
    /**
     * @param declarator
     * @return
     */
    private IASTVariable createVariableASTNode(Declarator declarator)
    {
        return astFactory.createVariable(
            scope,
            declarator.getName(),
            isAuto(),
            declarator.getInitializerClause(),
            declarator.getBitFieldExpression(),
            astFactory.createAbstractDeclaration(
                constt,
                getTypeSpecifier(),
                declarator.getPtrOps(),
                declarator.getArrayModifiers()),
            mutable,
            extern,
            register,
            staticc,
            getStartingOffset(),
            declarator.getNameEndOffset());
    }
    
	/**
	  * @param declarator
	  * @return
	  */
	 private IASTPointerToMethod createP2MethodASTNode(Declarator declarator)
	 {
		
		 return astFactory
			 .createPointerToMethod(
				 scope,
				 declarator.getOwnedDeclarator().getPointerOperatorNameDuple().toString().trim() + 
				 	declarator.getOwnedDeclarator().getName().trim(),
				 createParameterList(declarator.getParameters()),
				 astFactory.createAbstractDeclaration(
					 constt,
					 getTypeSpecifier(),
					 declarator.getPtrOps(),
					 declarator.getArrayModifiers()),
				 declarator.getExceptionSpecification(),
				 inline,
				 friend,
				 staticc,
				 startingOffset,
				 declarator.getNameStartOffset(),
				 templateDeclaration,
				 declarator.isConst(),
				 declarator.isVolatile(),
				 false,
		 // isConstructor
		 false, // isDestructor
		 virtual,
			 explicit,
			 declarator.isPureVirtual(),
			 ((scope instanceof IASTClassSpecifier )? ((IASTClassSpecifier)scope).getCurrentVisibilityMode() : ASTAccessVisibility.PUBLIC )
			 , (ASTPointerOperator)declarator.getOwnedDeclarator().getPtrOps().get(0));
	 }
	 /**
	  * @param declarator
	  * @return
	  */
	 private IASTPointerToFunction createP2FunctionASTNode(Declarator declarator)
	 {
		 return astFactory.createPointerToFunction(
			 scope,
			 declarator.getOwnedDeclarator().getName(),
			 createParameterList(declarator.getParameters()),
			 astFactory.createAbstractDeclaration(
				 constt,
				 getTypeSpecifier(),
				 declarator.getPtrOps(),
				 declarator.getArrayModifiers()),
			 declarator.getExceptionSpecification(),
			 inline,
			 friend,
			 staticc,
			 startingOffset,
			 declarator.getNameStartOffset(),
			 templateDeclaration, (ASTPointerOperator)declarator.getOwnedDeclarator().getPtrOps().get(0));
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
