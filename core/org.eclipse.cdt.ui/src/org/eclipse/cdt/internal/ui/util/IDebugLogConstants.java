/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.util;

/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IDebugLogConstants {
	public class DebugLogConstant {
		private DebugLogConstant( int value )
		{
			this.value = value;
		}
		private final int value;
	}
	
	public static final DebugLogConstant CONTENTASSIST = new DebugLogConstant ( 1 );
	
}
