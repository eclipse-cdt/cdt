/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.List;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;


/**
 * Interface that will indicate that the implementing service supports
 * Glob-style expression pattern matching.
 * @since 4.2
 */
public interface IGDBPatternMatchingExpressions {
	
	public interface IGroupExpressionDMContext extends IExpressionDMContext {
		List<String> getExpressionsInGroup();
		
	}
}
