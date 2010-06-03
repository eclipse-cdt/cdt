/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for additional features in DSF Reference implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.datamodel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;


/**
 * Holder for utility static methods for manipulating IDMContext objects.
 * 
 * @since 1.0
 */
public class DMContexts {

    /**
     * Convenience constant.
     */
    public static final IDMContext[] EMPTY_CONTEXTS_ARRAY = new IDMContext[0];

    /**
     * Finds a data model context of given type among ancestors of the 
     * specified context.  The returned ancestor is the one closest to the
     * specified context, in terms of depth.
     * 
     * Note that for efficiency, this method does not re-use getAllAncestorsOfType()
     * to avoid the unnecessary creation of an array.
     * 
     * @param ctx DMC to search.
     * @param ancestorType Class type of the desired DMC ancestor.
     * @return Returns the ancestor if found, null otherwise.
     */
    @ThreadSafe
    @SuppressWarnings("unchecked")
    public static <V extends IDMContext> V getAncestorOfType(IDMContext ctx, Class<V> ancestorType) {
    	if(ctx == null)
    		return null;
    	// Check the first context here for efficiency
        if (ancestorType.isAssignableFrom(ctx.getClass())) {
            return (V)ctx;
        }
        
        // Use a LinkedHashSet to avoid duplicates and preserver insertion-order
    	Set<IDMContext> nodes = new LinkedHashSet<IDMContext>();
        nodes.addAll(Arrays.asList(ctx.getParents()));
        while (nodes.isEmpty() == false) {
        	Set<IDMContext> parents = nodes;
        	nodes = new LinkedHashSet<IDMContext>();
        	for (IDMContext parent : parents) {
                if (ancestorType.isAssignableFrom(parent.getClass())) {
                	return (V)parent;
                }

                nodes.addAll(Arrays.asList(parent.getParents()));
        	}
        }
        
        return null;
    }

    /**
     * Finds all data model contexts of given type among ancestors of the 
     * specified context.  Ancestors are returned in order of closest to farthest,
     * in terms of depth.
     * @param ctx DMC to search.
     * @param ancestorType Class type of the desired DMC ancestor.
     * @return Returns all ancestors found, null if none.
     * @since 1.1
     */
    @ThreadSafe
    @SuppressWarnings("unchecked")
    public static <V extends IDMContext> V[] getAllAncestorsOfType(IDMContext ctx, Class<V> ancestorType) {
    	if(ctx == null)
    		return null;
    	
        // Use a LinkedHashSet to avoid duplicates and preserver insertion-order
    	Set<V> requestedAncestors = new LinkedHashSet<V>();
    	Set<IDMContext> nodes = new LinkedHashSet<IDMContext>();
        nodes.add(ctx);
        while (nodes.isEmpty() == false) {
        	Set<IDMContext> parents = nodes;
        	nodes = new LinkedHashSet<IDMContext>();
        	for (IDMContext parent : parents) {
                if (ancestorType.isAssignableFrom(parent.getClass())) {
                	requestedAncestors.add((V)parent);
                }

                nodes.addAll(Arrays.asList(parent.getParents()));
        	}
        }
        
        if (requestedAncestors.isEmpty()) return null;
        else {
        	V[] v = (V[])Array.newInstance(ancestorType, 0);
        	return requestedAncestors.toArray(v);
        }
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
