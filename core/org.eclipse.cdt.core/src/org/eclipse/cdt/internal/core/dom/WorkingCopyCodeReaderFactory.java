/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.model.IWorkingCopyProvider;

/**
 * @author jcamelon
 */
@Deprecated
public class WorkingCopyCodeReaderFactory extends PartialWorkingCopyCodeReaderFactory {
    /**
     * @param provider
     */
    public WorkingCopyCodeReaderFactory(IWorkingCopyProvider provider, IIncludeFileResolutionHeuristics heuristics) {
        super(provider, heuristics);
    }
    
    @Override
	public int getUniqueIdentifier() {
        return CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE;
    }
}
