/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jul 10, 2003
 */
package org.eclipse.cdt.core.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IMatch {

	int getElementType();

	int getVisibility();

	String getName();

	String getParentName();

	IResource getResource();
	
	IPath getLocation();
	
	IPath getReferenceLocation();

	int getStartOffset();
	
	int getEndOffset();

	boolean isStatic();
	boolean isConst();
	boolean isVolatile();
}
