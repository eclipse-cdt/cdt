/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;


public final class ConstructorMethodStub extends AbstractMethodStub {

    private static String NAME = NewClassWizardMessages.getString("NewClassCodeGeneration.stub.constructor.name"); //$NON-NLS-1$
    
    public ConstructorMethodStub() {
        this(ASTAccessVisibility.PUBLIC, false);
    }

    public ConstructorMethodStub(ASTAccessVisibility access, boolean isInline) {
        super(NAME, access, false, isInline);
    }

    public String createMethodDeclaration(String className, IBaseClassInfo[] baseClasses, String lineDelimiter) {
        //TODO should use code templates
        StringBuffer buf = new StringBuffer();
        buf.append(className);
        buf.append("()"); //$NON-NLS-1$
    	if (fIsInline) {
    	    buf.append(" {}"); //$NON-NLS-1$
    	} else {
    	    buf.append(";"); //$NON-NLS-1$
    	}
        return buf.toString();
    }

    public String createMethodImplementation(String className, IBaseClassInfo[] baseClasses, String lineDelimiter) {
        //TODO should use code templates
        if (fIsInline) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer buf = new StringBuffer();
        buf.append(className);
        buf.append("::"); //$NON-NLS-1$
        buf.append(className.toString());
        buf.append("()"); //$NON-NLS-1$
        buf.append(lineDelimiter);
        buf.append('{');
        buf.append(lineDelimiter);
        //buf.append("// TODO Auto-generated constructor stub");
        //buf.append(lineDelimiter);
        buf.append('}');
        return buf.toString();
    }

    public boolean isConstructor() {
        return true;
    }

    public boolean canModifyVirtual() {
        return false;
    }
}