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


public final class DestructorMethodStub extends AbstractMethodStub {
    
    private static String NAME = NewClassWizardMessages.getString("NewClassCodeGeneration.stub.destructor.name"); //$NON-NLS-1$
    
    public DestructorMethodStub() {
        this(ASTAccessVisibility.PUBLIC, true, false);
    }

    public DestructorMethodStub(ASTAccessVisibility access, boolean isVirtual, boolean isInline) {
        super(NAME, access, isVirtual, isInline);
    }

    public String createMethodDeclaration(String className, IBaseClassInfo[] baseClasses, String lineDelimiter) {
        //TODO should use code templates
        StringBuffer buf = new StringBuffer();
    	if (fIsVirtual){
    	    buf.append("virtual "); //$NON-NLS-1$
    	}
    	buf.append("~"); //$NON-NLS-1$
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
        if (fIsInline)
            return ""; //$NON-NLS-1$
        else {
            StringBuffer buf = new StringBuffer();
            buf.append(className);
            buf.append("::~"); //$NON-NLS-1$
            buf.append(className);
            buf.append("()"); //$NON-NLS-1$
            buf.append(" {"); //$NON-NLS-1$
            buf.append(lineDelimiter);
            //buf.append("// TODO Auto-generated destructor stub");
            //buf.append(lineDelimiter);
            buf.append("}"); //$NON-NLS-1$
            return buf.toString();
        }
    }

    public boolean isDestructor() {
        return true;
    }
}