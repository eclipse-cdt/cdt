/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.List;

/** 
 * Represents the include search path
 */
public final class IncludeSearchPath {
	private final boolean fInhibitUseOfCurrentFileDirectory;
	private final IncludeSearchPathElement[] fElements;

	IncludeSearchPath(List<IncludeSearchPathElement> elements, boolean inhibitUseOfCurrentFileDirectory) {
		fElements= elements.toArray(new IncludeSearchPathElement[elements.size()]);
		fInhibitUseOfCurrentFileDirectory= inhibitUseOfCurrentFileDirectory;
	}
	
	/**
	 * @return the elements of the include search path.
	 */
	public IncludeSearchPathElement[] getElements() {
		return fElements;
	}
	
	/**
	 * @return whether the use of the directory of the current file is inhibited.
	 */
	public boolean isInhibitUseOfCurrentFileDirectory() {
		return fInhibitUseOfCurrentFileDirectory;
	}
}