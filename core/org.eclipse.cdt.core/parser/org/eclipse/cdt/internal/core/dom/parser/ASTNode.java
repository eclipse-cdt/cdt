/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;

/**
 * @author jcamelon
 */
public abstract class ASTNode implements IASTNode {

    private int length;

    private int offset;

    private static final IASTNodeLocation[] EMPTY_LOCATION_ARRAY = new IASTNodeLocation[0];

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        this.locations = null;
    }

    public void setLength(int length) {
        this.length = length;
        this.locations = null;
    }

    public void setOffsetAndLength(int offset, int length) {
        this.offset = offset;
        this.length = length;
        this.locations = null;
    }

    public void setOffsetAndLength(ASTNode node) {
        setOffsetAndLength(node.getOffset(), node.getLength());
    }

    private IASTNodeLocation[] locations = null;
    private IASTFileLocation fileLocation = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getNodeLocations()
     */
    public IASTNodeLocation[] getNodeLocations() {
        if (locations != null)
            return locations;
        if (length == 0)
            return EMPTY_LOCATION_ARRAY;
        locations = getTranslationUnit().getLocationInfo(offset, length);
        return locations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getRawSignature()
     */
    public String getRawSignature() {
        return getTranslationUnit().getUnpreprocessedSignature(
                getNodeLocations());
    }

    public String getContainingFilename() {
        return getTranslationUnit().getContainingFilename(offset);
    }

    public IASTFileLocation getFileLocation() {
        if( fileLocation != null )
            return fileLocation;
        fileLocation = getTranslationUnit().flattenLocationsToFile( getNodeLocations() );
        return fileLocation;
    }
}
