/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser.typehierarchy;

import java.io.OutputStream;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A type hierarchy provides navigations between a type and its resolved
 * supertypes and subtypes for a specific type or for all types within a region.
 * Supertypes may extend outside of the type hierarchy's region in which it was
 * created such that the root of the hierarchy is always included. For example, if a type
 * hierarchy is created for a <code>java.io.File</code>, and the region the hierarchy was
 * created in is the package fragment <code>java.io</code>, the supertype
 * <code>java.lang.Object</code> will still be included.
 * <p>
 * A type hierarchy is static and can become stale. Although consistent when 
 * created, it does not automatically track changes in the model.
 * As changes in the model potentially invalidate the hierarchy, change notifications
 * are sent to registered <code>ICElementHierarchyChangedListener</code>s. Listeners should
 * use the <code>exists</code> method to determine if the hierarchy has become completely
 * invalid (for example, when the type or project the hierarchy was created on
 * has been removed). To refresh a hierarchy, use the <code>refresh</code> method. 
 * </p>
 * <p>
 * The type hierarchy may contain cycles due to malformed supertype declarations.
 * Most type hierarchy queries are oblivious to cycles; the <code>getAll* </code>
 * methods are implemented such that they are unaffected by cycles.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface ITypeHierarchy {
/**
 * Adds the given listener for changes to this type hierarchy. Listeners are
 * notified when this type hierarchy changes and needs to be refreshed.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener the listener
 */
void addTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener);
/**
 * Returns whether the given type is part of this hierarchy.
 * 
 * @param type the given type
 * @return true if the given type is part of this hierarchy, false otherwise
 */
boolean contains(ICElement type);
/**
 * Returns whether the type and project this hierarchy was created on exist.
 * @return true if the type and project this hierarchy was created on exist, false otherwise
 */
boolean exists();

/**
 * Returns all resolved subtypes (direct and indirect) of the
 * given type, in no particular order, limited to the
 * types in this type hierarchy's graph. An empty array
 * is returned if there are no resolved subtypes for the
 * given type.
 * 
 * @param type the given type
 * @return all resolved subtypes (direct and indirect) of the given type
 */
ICElement[] getAllSubtypes(ICElement type);
/**
 * Returns all resolved superclasses of the
 * given class, in bottom-up order. An empty array
 * is returned if there are no resolved superclasses for the
 * given class.
 *
 * <p>NOTE: once a type hierarchy has been created, it is more efficient to
 * query the hierarchy for superclasses than to query a class recursively up
 * the superclass chain. Querying an element performs a dynamic resolution,
 * whereas the hierarchy returns a pre-computed result.
 * 
 * @param type the given type
 * @return all resolved superclasses of the given class, in bottom-up order, an empty
 * array if none.
 */
ICElement[] getAllSupertypes(ICElement type);

/**
 * Returns all classes in the graph which have no resolved superclass,
 * in no particular order.
 * 
 * @return all classes in the graph which have no resolved superclass
 */
ICElement[] getRootClasses();

/**
 * Returns the direct resolved subtypes of the given type,
 * in no particular order, limited to the types in this
 * type hierarchy's graph.
 * If the type is a class, this returns the resolved subclasses.
 * If the type is an interface, this returns both the classes which implement 
 * the interface and the interfaces which extend it.
 * 
 * @param type the given type
 * @return the direct resolved subtypes of the given type limited to the types in this
 * type hierarchy's graph
 */
ICElement[] getSubtypes(ICElement type);

/**
 * Returns the resolved supertypes of the given type,
 * in no particular order, limited to the types in this
 * type hierarchy's graph.
 * For classes, this returns its superclass and the interfaces that the class implements.
 * For interfaces, this returns the interfaces that the interface extends. As a consequence 
 * <code>java.lang.Object</code> is NOT considered to be a supertype of any interface 
 * type.
 * 
 * @param type the given type
 * @return the resolved supertypes of the given type limited to the types in this
 * type hierarchy's graph
 */
ICElement[] getSupertypes(ICElement type);
/**
 * Returns the type this hierarchy was computed for.
 * Returns <code>null</code> if this hierarchy was computed for a region.
 * 
 * @return the type this hierarchy was computed for
 */
ICElement getType();
/**
 * Re-computes the type hierarchy reporting progress.
 *
 * @param monitor the given progress monitor
 * @exception JavaModelException if unable to refresh the hierarchy
 */
void refresh(IProgressMonitor monitor) throws CModelException;
/**
 * Removes the given listener from this type hierarchy.
 * Has no affect if an identical listener is not registered.
 *
 * @param listener the listener
 */
void removeTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener);
/**
 * Stores the type hierarchy in an output stream. This stored hierarchy can be load by
 * ICElement#loadTypeHierachy(IJavaProject, InputStream, IProgressMonitor).
 * Listeners of this hierarchy are not stored.
 * 
 * Only hierarchies created by the following methods can be store:
 * <ul>
 * <li>ICElement#newSupertypeHierarchy(IProgressMonitor)</li>
 * <li>ICElement#newTypeHierarchy(IJavaProject, IProgressMonitor)</li>
 * <li>ICElement#newTypeHierarchy(IProgressMonitor)</li>
 * </u>
 * 
 * @param outputStream output stream where the hierarchy will be stored
 * @param monitor the given progress monitor
 * @exception JavaModelException if unable to store the hierarchy in the ouput stream
 * @see ICElement#loadTypeHierachy(java.io.InputStream, IProgressMonitor)
 * @since 2.1
 */
void store(OutputStream outputStream, IProgressMonitor monitor) throws CModelException;
}
