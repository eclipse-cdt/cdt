/*******************************************************************************
 * Copyright (c) 2011, Texas Instruments and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model.provisional;

import org.eclipse.debug.core.DebugException;

/**
 * An interface to retrieve the name of the target.
 * 
 * @author Alain Lee
 */
public interface ITargetLabelProvider {
	
	String getLabel() throws DebugException;

}
