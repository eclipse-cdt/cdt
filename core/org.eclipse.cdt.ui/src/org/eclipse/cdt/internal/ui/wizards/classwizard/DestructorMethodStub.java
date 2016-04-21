/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CodeGeneration;


public final class DestructorMethodStub extends AbstractMethodStub {
    
    private static String NAME = NewClassWizardMessages.NewClassCodeGeneration_stub_destructor_name; 
    
    public DestructorMethodStub() {
        this(ASTAccessVisibility.PUBLIC, true, false);
    }

    public DestructorMethodStub(ASTAccessVisibility access, boolean isVirtual, boolean isInline) {
        super(NAME, access, isVirtual, isInline);
    }

    @Override
	public String createMethodDeclaration(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses, String lineDelimiter) throws CoreException {
        StringBuilder buf = new StringBuilder();
    	if (fIsVirtual){
    	    buf.append("virtual "); //$NON-NLS-1$
    	}
    	buf.append("~"); //$NON-NLS-1$
    	buf.append(className);
    	buf.append("()"); //$NON-NLS-1$
    	if (fIsInline) {
            buf.append('{');
            buf.append(lineDelimiter);
        	String body= CodeGeneration.getDestructorBodyContent(tu, className, null, lineDelimiter);
        	if (body != null) {
        		buf.append(body);
                buf.append(lineDelimiter);
        	}
            buf.append('}');
    	} else {
    	    buf.append(";"); //$NON-NLS-1$
    	}
        return buf.toString();
    }

    @Override
	public String createMethodImplementation(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses, String lineDelimiter) throws CoreException {
    	if (fIsInline) {
    		return ""; //$NON-NLS-1$
    	}
    	StringBuilder buf = new StringBuilder();
    	buf.append(className);
    	buf.append("::~"); //$NON-NLS-1$
    	buf.append(className);
    	buf.append("()"); //$NON-NLS-1$
    	buf.append(lineDelimiter);
        buf.append('{');
        buf.append(lineDelimiter);
    	String body= CodeGeneration.getDestructorBodyContent(tu, className, null, lineDelimiter);
    	if (body != null) {
    		buf.append(body);
            buf.append(lineDelimiter);
    	}
        buf.append('}');
    	return buf.toString();
    }

    @Override
	public boolean isDestructor() {
        return true;
    }
}
