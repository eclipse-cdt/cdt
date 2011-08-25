/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * Interface for accessing the macro dictionary of the preprocessor.
 * @since 5.4
 */
public interface IMacroDictionary {

	/**
	 * Returns true, when the macro dictionary complies with the significant macros.
	 */
	boolean compliesWith(Map<String, String> significantMacros);

}
