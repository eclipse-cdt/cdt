/*******************************************************************************
 * Copyright (c) 2007, 2015 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import junit.framework.TestSuite;

public class SearchTestSuite extends TestSuite {
    public static TestSuite suite() {
        return new SearchTestSuite();
    }
    
    public SearchTestSuite() {
        super(SearchTestSuite.class.getName());
        addTest(BasicSearchTest.suite());
        addTest(LinkedNamesFinderTest.suite());
        addTest(SearchReferencesAcrossLanguagesTest.suite());
    }
}
