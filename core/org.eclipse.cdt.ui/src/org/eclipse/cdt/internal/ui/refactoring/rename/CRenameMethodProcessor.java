/*******************************************************************************
 * Copyright (c) 2005, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *    Markus Schorn - initial API and implementation 
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;


/**
 * Rename processor for methods.
 */
public class CRenameMethodProcessor extends CRenameGlobalProcessor {
    public CRenameMethodProcessor(CRenameProcessor processor, String kind, boolean isVirtual) {
        super(processor, kind);
        if (isVirtual)
        	setAvailableOptions(getAvailableOptions() | CRefactory.OPTION_DO_VIRTUAL);
    }

    @Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) 
            throws OperationCanceledException, CoreException {
        CRefactoringArgument argument= getArgument();
        IBinding binding= argument.getBinding();
        if (binding instanceof ICPPConstructor) {
            return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameConstructor")); //$NON-NLS-1$
        }
        String identifier= argument.getName();
        if (identifier.startsWith("~")) { //$NON-NLS-1$
            return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameDestructor")); //$NON-NLS-1$
        }
        if (identifier.startsWith("operator") && //$NON-NLS-1$
                identifier.length() > 8 && 
                !CRefactoringUtils.isIdentifierChar(identifier.charAt(8))) { 
            return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameOperator")); //$NON-NLS-1$
        }
        return super.checkInitialConditions(pm);
    }
    
    @Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor, 
            CheckConditionsContext context) throws OperationCanceledException, CoreException {
        CRefactoringArgument argument= getArgument();
        RefactoringStatus result= new RefactoringStatus();
        IScope scope= argument.getScope();
        if (scope != null) {
            IASTNode node= null;
            try {
                node = ASTInternal.getPhysicalNodeOfScope(scope);
            } catch (DOMException e) {
                getAstManager().handleDOMException(argument.getTranslationUnit(), e, result);
            }
            if (node instanceof IASTCompositeTypeSpecifier) {
                IASTCompositeTypeSpecifier se= (IASTCompositeTypeSpecifier) node;
                IASTName name= ASTManager.getSimpleName(se.getName());
                if (getReplacementText().equals(name.toString())) {
                    return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameToConstructor")); //$NON-NLS-1$
                }
                if (getReplacementText().startsWith("~")) { //$NON-NLS-1$
                    return RefactoringStatus.createFatalErrorStatus(Messages.getString("CRenameMethodProcessor.fatalError.renameToDestructor")); //$NON-NLS-1$);
                }
                if (!CRefactoringUtils.checkIdentifier(getReplacementText())) {
                    result.merge(RefactoringStatus.createErrorStatus(Messages.getString("CRenameMethodProcessor.warning.illegalCharacters"))); //$NON-NLS-1$
                }
            }                
        }
        if (argument.getArgumentKind() == CRefactory.ARGUMENT_VIRTUAL_METHOD && (getSelectedOptions() & CRefactory.OPTION_DO_VIRTUAL) == 0) {
            result.merge(RefactoringStatus.createWarningStatus(Messages.getString("CRenameMethodProcessor.warning.renameVirtual"))); //$NON-NLS-1$
        }

        result.merge(super.checkFinalConditions(monitor, context));
        return result;
    }
    
    @Override
	protected IBinding[] getBindingsToBeRenamed(RefactoringStatus status) {
    	if ((getSelectedOptions() & CRefactory.OPTION_DO_VIRTUAL) == 0) {
    		return super.getBindingsToBeRenamed(status);
    	}
    	
        CRefactoringArgument argument= getArgument();
        IBinding binding= argument.getBinding();
        ArrayList<IBinding> bindings= new ArrayList<IBinding>();
        if (binding != null) {
            bindings.add(binding);
        }
        if (binding instanceof ICPPMethod) {
        	ICPPMethod m= (ICPPMethod) binding;
        	try {
        		IBinding[] bs= ClassTypeHelper.findOverridden(m);
        		bindings.addAll(Arrays.asList(bs));
        		bs= ClassTypeHelper.findOverriders(getIndex(), m);
        		bindings.addAll(Arrays.asList(bs));
            } catch (DOMException e) {
                getAstManager().handleDOMException(argument.getTranslationUnit(), e, status);
            } catch (CoreException e) {
            	status.addError(e.getMessage());
            }
        }
        return bindings.toArray(new IBinding[bindings.size()]);
    }
}
