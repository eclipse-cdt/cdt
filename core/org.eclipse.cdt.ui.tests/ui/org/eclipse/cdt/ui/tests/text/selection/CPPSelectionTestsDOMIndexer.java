/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.selection;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IPDOMManager;

/**
 * Test Ctrl_F3/F3 with the DOM Indexer for a C++ project.
 *  
 * @author dsteffle
 */
public class CPPSelectionTestsDOMIndexer extends CPPSelectionTestsAnyIndexer {
	public CPPSelectionTestsDOMIndexer(String name) {
		super(name, IPDOMManager.ID_FULL_INDEXER);
	}
	
	public static Test suite() {
		return suite(CPPSelectionTestsDOMIndexer.class);
	}
}
