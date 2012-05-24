/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.tests.text.selection;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IPDOMManager;

public class CPPSelectionTestsFastIndexer extends CPPSelectionTestsAnyIndexer {
	public CPPSelectionTestsFastIndexer(String name) {
		super(name, IPDOMManager.ID_FAST_INDEXER);
	}
	
	public static Test suite() {
		return suite(CPPSelectionTestsFastIndexer.class);
	}
}