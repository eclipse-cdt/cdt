/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 ******************************************************************************/
/*
 * Created on May 30, 2003
 */
package org.eclipse.cdt.internal.core.index;

public interface IEntryResult {
	public int[] getFileReferences();
	public char[] getWord();
}

