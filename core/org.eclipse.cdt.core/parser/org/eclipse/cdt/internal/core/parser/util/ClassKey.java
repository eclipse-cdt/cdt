/**********************************************************************
 * Created on Mar 26, 2003
 *
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.util;

/**
 * @author jcamelon
 *
 */
public class ClassKey {

	public static final int t_class = 0;
	public static final int t_struct = 1;
	public static final int t_union = 2;
	public static final int t_enum = 3;
	
	private int classKey = t_class;
	
	
	/**
	 * @return int
	 */
	public int getClassKey() {
		return classKey;
	}

	/**
	 * Sets the classKey.
	 * @param classKey The classKey to set
	 */
	public void setClassKey(int classKey) {
		this.classKey = classKey;
	}

}
