/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.callhierarchy;

import junit.framework.TestSuite;

public class CallHierarchyTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new CallHierarchyTestSuite();
    }
    
    public CallHierarchyTestSuite() {
        super(CallHierarchyTestSuite.class.getName());
        addTest(BasicCallHierarchyTest.suite());
        addTest(BasicCppCallHierarchyTest.suite());
        addTest(InitializersInCallHierarchyTest.suite());
        addTest(CppCallHierarchyTest.suite());
        addTest(CallHierarchyAcrossProjectsTest.suite());
        addTest(CallHierarchyBugs.suite());
    }
}
