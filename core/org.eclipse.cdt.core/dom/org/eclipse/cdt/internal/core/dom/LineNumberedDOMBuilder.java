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
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.internal.core.parser.Token;

/**
 * @author jcamelon
 */
public class LineNumberedDOMBuilder extends DOMBuilder {

	protected LineNumberedDOMBuilder()
	{
	}
	
	protected void setLineNumber( IOffsetable element, int offset, boolean topLine )
	{
		try
		{
			if( topLine )
				element.setTopLine( parser.getLineNumberForOffset( offset ));
			else
				element.setBottomLine( parser.getLineNumberForOffset( offset ));
		}
		catch( NoSuchMethodException nsm )
		{
			System.out.println( "Incorrect parser setup to get line numbers");
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		Object returnValue = super.classSpecifierBegin(container, classKey);
		setLineNumber( (IOffsetable)returnValue, classKey.getOffset(), true );
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierEnd(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void classSpecifierEnd(Object classSpecifier, Token closingBrace) {
		super.classSpecifierEnd(classSpecifier, closingBrace);
		setLineNumber( (IOffsetable)classSpecifier, closingBrace.getOffset() + closingBrace.getLength(), false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumeratorEnd(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void enumeratorEnd(Object enumDefn, Token lastToken) {
		super.enumeratorEnd(enumDefn, lastToken);
		setLineNumber( (IOffsetable)enumDefn, lastToken.getOffset() + lastToken.getLength(), false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumeratorId(java.lang.Object)
	 */
	public void enumeratorId(Object enumDefn) {
		super.enumeratorId(enumDefn);
		setLineNumber( (IOffsetable)enumDefn, currName.getStartOffset(), true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object enumSpecifierBegin(Object container, Token enumKey) {
		Object returnValue = super.enumSpecifierBegin(container, enumKey);
		setLineNumber( (IOffsetable)returnValue, enumKey.getOffset(), true);
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierEnd(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void enumSpecifierEnd(Object enumSpec, Token closingBrace) {
		super.enumSpecifierEnd(enumSpec, closingBrace);
		setLineNumber( (IOffsetable)enumSpec, closingBrace.getOffset() + closingBrace.getLength(), false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#inclusionBegin(java.lang.String, int, int)
	 */
	public Object inclusionBegin(
		String includeFile,
		int offset,
		int inclusionBeginOffset) {
		Object inclusion = super.inclusionBegin(includeFile, offset, inclusionBeginOffset);
		setLineNumber( (IOffsetable)inclusion, inclusionBeginOffset, true );
		setLineNumber( (IOffsetable)inclusion, offset + includeFile.length() + 1, false );
		return inclusion;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#macro(java.lang.String, int, int, int)
	 */
	public Object macro(
		String macroName,
		int offset,
		int macroBeginOffset,
		int macroEndOffset) {
		Object macro = super.macro(macroName, offset, macroBeginOffset, macroEndOffset);
		setLineNumber( (IOffsetable) macro, macroBeginOffset, true );
		setLineNumber( (IOffsetable) macro, macroEndOffset, false );
		return macro; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDefinitionBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object namespaceDefinitionBegin(Object container, Token namespace) {
		Object namespaceDef = super.namespaceDefinitionBegin(container, namespace);
		setLineNumber( (IOffsetable)namespaceDef, namespace.getOffset(), true);
		return namespaceDef;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDefinitionEnd(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void namespaceDefinitionEnd(Object namespace, Token closingBrace) {
		super.namespaceDefinitionEnd(namespace, closingBrace);
		setLineNumber( (IOffsetable)namespace, closingBrace.getOffset() + closingBrace.getLength(), false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclarationBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object simpleDeclarationBegin(Object container, Token firstToken) {
		Object retval = super.simpleDeclarationBegin(container, firstToken);
		setLineNumber( (IOffsetable)retval, firstToken.getOffset(), true );
		return retval; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclarationEnd(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void simpleDeclarationEnd(Object declaration, Token lastToken) {
		super.simpleDeclarationEnd(declaration, lastToken);
		setLineNumber( (IOffsetable)declaration, lastToken.getOffset() + lastToken.getLength(), false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object templateDeclarationBegin(Object container, Token exported) {
		Object template = super.templateDeclarationBegin(container, exported);
		setLineNumber( (IOffsetable)template, exported.getOffset(), true );
		return template;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationEnd(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void templateDeclarationEnd(Object templateDecl, Token lastToken) {
		super.templateDeclarationEnd(templateDecl, lastToken);
		setLineNumber( (IOffsetable)templateDecl, lastToken.getOffset() + lastToken.getLength(), false);
	}

}
