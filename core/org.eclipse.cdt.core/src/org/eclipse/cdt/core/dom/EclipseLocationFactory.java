/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTLocationFactory;
import org.eclipse.cdt.core.dom.ast.IASTResourceLocation;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.dom.parser.location.ASTFileLocation;
import org.eclipse.core.resources.IResource;

/**
 * @author jcamelon
 */
public class EclipseLocationFactory implements IASTLocationFactory {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTLocationFactory#createUnresolvedFileLocation(java.lang.String, int, int)
     */
    public IASTFileLocation createUnresolvedFileLocation(String path,
            int offset, int length) {
        IASTFileLocation result = new ASTFileLocation();
        result.setResolved(false);
        result.setNodeOffset( offset );
        result.setNodeLength( length );
        result.setFileName( path );
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTLocationFactory#createUnresolvedResourceLocation(org.eclipse.core.resources.IResource, int, int)
     */
    public IASTResourceLocation createUnresolvedResourceLocation(
            IResource resource, int offset, int length) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTLocationFactory#createUnresolvedWorkingCopyLocation(org.eclipse.cdt.core.model.IWorkingCopy, int, int)
     */
    public IASTResourceLocation createUnresolvedWorkingCopyLocation(
            IWorkingCopy workingCopy, int offset, int length) {
        // TODO Auto-generated method stub
        return null;
    }

}
