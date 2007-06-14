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

package org.eclipse.cdt.ui.tests.text.contentassist;

import junit.framework.TestSuite;

public class ContentAssistTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new ContentAssistTestSuite();
    }
    
    public ContentAssistTestSuite() {
        super(ContentAssistTestSuite.class.getName());
        
		addTest( ContentAssistTests.suite() );
        
    }
}
