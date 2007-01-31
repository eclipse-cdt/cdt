/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.typehierarchy;

import junit.framework.TestSuite;

public class TypeHierarchyTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new TypeHierarchyTestSuite();
    }
    
    public TypeHierarchyTestSuite() {
        super("Tests in package org.eclipse.cdt.ui.tests.typehierarchy");
        addTest(CTypeHierarchyTest.suite());
        addTest(CppTypeHierarchyTest.suite());
        addTest(QuickTypeHierarchyTest.suite());
        addTest(TypeHierarchyAcrossProjectsTest.suite());
    }
}
