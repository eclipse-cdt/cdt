/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.model;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;


/**
 * Holder for utility static methods for manipulating IDataModelContext 
 * objects.
 */
public class DMCs {

    /**
     * Finds a data model context of given type among ancestors of the 
     * specified context.
     * @param ctx DMC to search.
     * @param ancestorType Class type of the desired DMC ancestor.
     * @return Returns the ancestor if found, null otherwise.
     */
    @ThreadSafe
    @SuppressWarnings("unchecked")
    public static <V extends IDataModelContext> V getAncestorOfType(IDataModelContext ctx, Class<V> ancestorType) {
        if (ancestorType.isAssignableFrom(ctx.getClass())) {
            return (V)ctx;
        }
        
        for (IDataModelContext parent : ctx.getParents()) {
            if (ancestorType.isAssignableFrom(parent.getClass())) {
                return (V)parent;
            }
        }

        for (IDataModelContext parent : ctx.getParents()) {
            V ancestor = getAncestorOfType(parent, ancestorType);
            if (ancestor != null) return ancestor;
        }
        return null;
    }
    
    /**
     * Checks all ancestors for a given DMC to see if the given 
     * potentialAncestor is in fact an ancestor.
     * @param dmc DMC who's ancestors to check.
     * @param potentialAncestor Ancestor DMC to look for.
     * @return true if a match is found.
     */
    @ThreadSafe
    public static boolean isAncestorOf(IDataModelContext dmc, IDataModelContext potentialAncestor) {
        // Check the direct parents for a match.
        for (IDataModelContext parentDmc : dmc.getParents()) {
            if (potentialAncestor.equals(parentDmc)) {
                return true;
            }
        }

        // Recursively check the parents' parents for a match.
        for (IDataModelContext parentDmc : dmc.getParents()) {
            if (isAncestorOf(parentDmc, potentialAncestor)) {
                return true;
            }
        }
        
        // No match.
        return false;
    }
}
