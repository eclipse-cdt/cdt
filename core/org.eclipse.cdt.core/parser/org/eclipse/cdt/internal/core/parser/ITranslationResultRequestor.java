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
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.ITranslationResult;

/**
 * A callback interface for receiving translation results.
 */
public interface ITranslationResultRequestor {
	
	/**
	 * Accept a translation result.
	 */
	public void acceptResult(ITranslationResult result);
}
