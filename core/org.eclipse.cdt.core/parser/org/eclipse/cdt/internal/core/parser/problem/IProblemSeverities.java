/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.problem;

public interface IProblemSeverities {
	
    final int Ignore       = -1;
    
    final int Error        = 0x00001;
    final int Warning      = 0x00002;
    
    final int Task         = 0x10000; // when bit is set: the problem is a task
}
