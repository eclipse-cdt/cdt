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
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IResource;

/**
 * @author jcamelon
 */
public interface IASTLocationFactory {

    public IASTFileLocation createUnresolvedFileLocation( String path, int offset, int length );
    public IASTResourceLocation createUnresolvedResourceLocation( IResource resource, int offset, int length );
    public IASTResourceLocation createUnresolvedWorkingCopyLocation( IWorkingCopy workingCopy, int offset, int length );
}
