/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.parser2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.parser2.IProblemRequestor;


/**
 * @author jcamelon
 */
public class ProblemCollector implements IProblemRequestor {

    List problems = new ArrayList();

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.IProblemRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
     */
    public boolean acceptProblem(IProblem problem) {
        problems.add(problem);
        return true;
    }

    /**
     * @return
     */
    public boolean hasNoProblems() {
        return problems.isEmpty();
    }

}