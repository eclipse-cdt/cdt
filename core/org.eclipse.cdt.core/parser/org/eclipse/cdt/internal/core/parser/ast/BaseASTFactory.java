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
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTDesignator.DesignatorKind;
import org.eclipse.cdt.core.parser.extension.IASTFactoryExtension;


/**
 * @author jcamelon
 *
 */
public class BaseASTFactory  {

	public BaseASTFactory( IASTFactoryExtension extension )
	{
		this.extension = extension;
	}
	
	protected IParserLogService logService;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createMacro(java.lang.String, int, int, int)
	 */
	public IASTMacro createMacro(char[] name, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, IMacroDescriptor info, char[] fn) {
		IASTMacro m = new ASTMacro( name, info, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endingLine, fn );
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createInclusion(java.lang.String, java.lang.String, boolean)
	 */
	public IASTInclusion createInclusion(char[] name, char[] fileName, boolean local, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, char[] fn) {
		IASTInclusion inclusion = new ASTInclusion( name, fileName, local, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endingLine, fn );
		return inclusion;
	}

    public IASTAbstractDeclaration createAbstractDeclaration(boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOperator)
    {
        return new ASTAbstractDeclaration( isConst, isVolatile, typeSpecifier, pointerOperators, arrayModifiers, parameters, pointerOperator );
    }

    public IASTArrayModifier createArrayModifier(IASTExpression exp)
    {
        return new ASTArrayModifier( exp );
    }

    public IASTDesignator createDesignator(DesignatorKind kind, IASTExpression constantExpression, IToken fieldIdentifier, Map extensionParms)
    {
    	if( extension.overrideCreateDesignatorMethod( kind ))
    		return extension.createDesignator( kind, constantExpression, fieldIdentifier, extensionParms );
        return new ASTDesignator( kind, constantExpression, 
        		fieldIdentifier == null ? new char[0] : fieldIdentifier.getCharImage(),  //$NON-NLS-1$
        		fieldIdentifier == null ? -1 : fieldIdentifier.getOffset() );
    }

	public void setLogger(IParserLogService log) {
		logService = log;
	}

	protected final IASTFactoryExtension extension;
    protected static final char[] EMPTY_STRING = new char[0];


}
