/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for additional features in DSF Reference implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;


/**
 * Holder for utility static methods for manipulating IDMContext objects.
 */
public class DMContexts {

    /**
     * Convenience constant.
     */
    public static final IDMContext[] EMPTY_CONTEXTS_ARRAY = new IDMContext[0];
    
    /**
     * Finds a data model context of given type among ancestors of the 
     * specified context.
     * @param ctx DMC to search.
     * @param ancestorType Class type of the desired DMC ancestor.
     * @return Returns the ancestor if found, null otherwise.
     */
    @ThreadSafe
    @SuppressWarnings("unchecked")
    public static <V extends IDMContext> V getAncestorOfType(IDMContext ctx, Class<V> ancestorType) {
    	if(ctx == null)
    		return null;
        if (ancestorType.isAssignableFrom(ctx.getClass())) {
            return (V)ctx;
        }
        
        for (IDMContext parent : ctx.getParents()) {
            if (ancestorType.isAssignableFrom(parent.getClass())) {
                return (V)parent;
            }
        }

        for (IDMContext parent : ctx.getParents()) {
            V ancestor = getAncestorOfType(parent, ancestorType);
            if (ancestor != null) return ancestor;
        }
        return null;
    }
    
    /**
     * Checks all ancestors for a given context to see if the given 
     * potentialAncestor is in fact an ancestor.
     * @param dmc DM Contexts who's ancestors to check.
     * @param potentialAncestor Ancestor context to look for.
     * @return true if a match is found.
     */
    @ThreadSafe
    public static boolean isAncestorOf(IDMContext dmc, IDMContext potentialAncestor) {
        // Check the direct parents for a match.
        for (IDMContext parentDmc : dmc.getParents()) {
            if (potentialAncestor.equals(parentDmc)) {
                return true;
            }
        }

        // Recursively check the parents' parents for a match.
        for (IDMContext parentDmc : dmc.getParents()) {
            if (isAncestorOf(parentDmc, potentialAncestor)) {
                return true;
            }
        }
        
        // No match.
        return false;
    }

    /**
     * Traverses all the parents of a context and converts the whole
     * into a list.
     */
    @ThreadSafe
    public static List<IDMContext> toList(IDMContext dmc) {
        /*
         * This method is implemented recursively, which is not necessarily
         * the most efficient way to do this.
         */
        List<IDMContext> list = new ArrayList<IDMContext>();
        list.add(dmc);

        for (IDMContext parentDmc : dmc.getParents()) {
            list.addAll(toList(parentDmc));
        }
        return list;
    }
}
