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
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateParameter implements IASTTemplateParameter
{
    private final List templateParms;
    private final IASTParameterDeclaration parameter;
    private final ParamKind kind;
    private final String identifier;
    private final String defaultValue;
    /**
     * @param kind
     * @param identifier
     * @param defaultValue
     * @param parameter
     */
    public ASTTemplateParameter(ParamKind kind, String identifier, String defaultValue, IASTParameterDeclaration parameter, List templateParms)
    {
        this.kind = kind; 
        this.identifier = identifier; 
        this.defaultValue = defaultValue; 
        this.parameter = parameter;
        this.templateParms = templateParms;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getTemplateParameterKind()
     */
    public ParamKind getTemplateParameterKind()
    {
        return kind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getIdentifier()
     */
    public String getIdentifier()
    {
        return identifier;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getDefaultValueIdExpression()
     */
    public String getDefaultValueIdExpression()
    {
        return defaultValue;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameterList#getTemplateParameters()
     */
    public Iterator getTemplateParameters()
    {
        return templateParms.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getParameterDeclaration()
     */
    public IASTParameterDeclaration getParameterDeclaration()
    {
        return parameter;
    }
}
