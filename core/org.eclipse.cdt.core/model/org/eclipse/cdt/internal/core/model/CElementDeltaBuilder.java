package org.eclipse.cdt.internal.core.model;
/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.model.ICElement;

/**
 * A C element delta biulder creates a C element delta on a C element between
 * the version of the C element at the time the comparator was created and the
 * current version of the C element.
 *
 * It performs this operation by locally caching the contents of 
 * the C element when it is created. When the method buildDeltas() is called, it
 * creates a delta over the cached contents and the new contents.
 * 
 * This class is similar to the JDT CElementDeltaBuilder class.
 */

public class CElementDeltaBuilder {
	
	CElementDelta delta;
	
	public CElementDeltaBuilder(ICElement cElement) {

	}

	public void buildDeltas() {
	}
}