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

import org.eclipse.cdt.core.parser.ast.ASTTemplateDeclarationType;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateDeclaration extends ASTDeclaration implements IASTTemplateDeclaration
{
    private IASTDeclaration ownedDeclaration;
    private List templateParameters;
    private ASTTemplateDeclarationType type;
    /**
     * @param templateParameters
     */
    public ASTTemplateDeclaration(IASTScope scope, List templateParameters)
    {
        super( scope );
        this.templateParameters = templateParameters;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration#getTemplateDeclarationType()
     */
    public ASTTemplateDeclarationType getTemplateDeclarationType()
    {
        return type;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration#getTemplateParameters()
     */
    public Iterator getTemplateParameters()
    {
        // TODO Auto-generated method stub
        return templateParameters.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration#getOwnedDeclaration()
     */
    public IASTDeclaration getOwnedDeclaration()
    {
        // TODO Auto-generated method stub
        return ownedDeclaration;
    }
}
