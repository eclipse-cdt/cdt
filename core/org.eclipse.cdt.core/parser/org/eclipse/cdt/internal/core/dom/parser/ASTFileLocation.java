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
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

/**
 * @author jcamelon
 */
public class ASTFileLocation implements IASTFileLocation {

    private String fn;
    private int o;
    private int l;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getFileName()
     */
    public String getFileName() {
        return fn;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#setFileName(java.lang.String)
     */
    public void setFileName(String fileName) {
        fn = fileName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#getNodeOffset()
     */
    public int getNodeOffset() {
        return o;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#setNodeOffset(int)
     */
    public void setNodeOffset(int offset) {
        o = offset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#getNodeLength()
     */
    public int getNodeLength() {
        return l;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#setNodeLength(int)
     */
    public void setNodeLength(int length) {
        l = length;
    }

}
