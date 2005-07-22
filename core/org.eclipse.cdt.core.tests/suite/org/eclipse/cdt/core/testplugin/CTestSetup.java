/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;

import junit.extensions.TestSetup;
import junit.framework.Test;



public class CTestSetup extends TestSetup {
	
	/**
	 * @deprecated
	 * Not needed anymore. No added value
	 */
	public CTestSetup(Test test) {
		super(test);
	}	
	
	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}
	

	
	
}