/*******************************************************************************
 * Copyright (c) 2006, 2017 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.callhierarchy;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BasicCallHierarchyTest.class,
    BasicCppCallHierarchyTest.class,
    InitializersInCallHierarchyTest.class,
    CppCallHierarchyTest.class,
    CallHierarchyAcrossProjectsTest.class,
    CallHierarchyBugs.class,
})
public class CallHierarchyTestSuite {

}
