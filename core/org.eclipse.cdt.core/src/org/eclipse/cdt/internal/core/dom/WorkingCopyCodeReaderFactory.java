/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.model.IWorkingCopyProvider;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScanner;

/**
 * @author jcamelon
 */
public class WorkingCopyCodeReaderFactory extends
        PartialWorkingCopyCodeReaderFactory implements ICodeReaderFactory {

    /**
     * @param provider
     */
    public WorkingCopyCodeReaderFactory(IWorkingCopyProvider provider) {
        super(provider);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
     */
    public int getUniqueIdentifier() {
        return CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
    public CodeReader createCodeReaderForInclusion(IScanner scanner, String path) {
        return checkWorkingCopyThenCache(path);
    }

}
