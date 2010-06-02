/*******************************************************************************
 * Copyright (c) 2005, 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 ******************************************************************************/ 

package org.eclipse.cdt.ui.tests.refactoring.rename;

import org.eclipse.core.runtime.*;
import org.eclipse.ltk.core.refactoring.*;
import org.eclipse.ltk.core.refactoring.participants.*;

public class TestRenameParticipant extends RenameParticipant {
    private static Object sElement= null;
    private static RenameArguments sArguments= null;
    private static int sConditionCheck= 0;
    private static int sCreateChange= 0;
    
    public static int getConditionCheckCount() {
        return sConditionCheck;
    }
    
    public static int getCreateChangeCount() {
        return sCreateChange;
    }
    
    public static Object getElement() {
        return sElement;
    }

    public static RenameArguments staticGetArguments() {
        return sArguments;
    }

    public static void reset() {
        sElement= null;
        sArguments= null;
        sConditionCheck= sCreateChange= 0;
    }

    protected boolean initialize(Object element) {
        sElement= element;
        return true;
    }

    public String getName() {
        return "TestRenameParticipant"; //$NON-NLS-1$
    }

    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
        sConditionCheck++;
        sArguments= getArguments();
        return new RefactoringStatus();
    }

    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        sCreateChange++;
        return null;
    }
}
