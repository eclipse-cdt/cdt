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
package org.eclipse.cdt.core.parser;

/**
 * @author jcamelon
 */
public interface IFilenameProvider {
	
	public char [] getCurrentFilename();
	public int     getCurrentFileIndex();
	public String  getFilenameForIndex( int index );
	
}
