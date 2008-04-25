/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.search.IHostSearchResult;

/**
 * Interface that remote objects must implement in order to be identifiable for
 * drag and drop, clipboard support, and finding multiple occurrences of the
 * same remote object in different contexts in the SystemView.
 * <p>
 * This is the functional opposite of {@link IRemoteObjectResolver}.
 * </p>
 * 
 * @see IRemoteObjectResolver
 */
public interface IRemoteObjectIdentifier {

	/**
	 * Return a String ID for the given remote object, that is unique within the
	 * subsystem.
	 * <p>
	 * This must be implemented by subsystem element adapters in order to
	 * marshal a reference to the remote object for drag and drop, and clipboard
	 * support. It is also used for uniquely identifying objects with changing
	 * properties in the SystemView. This method is the functional opposite of
	 * {@link IRemoteObjectResolver#getObjectWithAbsoluteName(String, IProgressMonitor)}.
	 * </p>
	 * <p>
	 * The unique ID for an object must remain the same over the entire lifetime
	 * of that object, such that it can always be identified. When an object is
	 * renamed, it should be removed from the views with the old ID and then
	 * re-added with the new ID. This is especially important for the
	 * SystemView, where the String ID is used for finding multiple occurrences
	 * of the same remote resource in different contexts during refresh events.
	 * In this case, the String ID can be used to find the remote object even if
	 * its hashCode changes due to updated properties. So even if a subsystem
	 * does not support drag and drop, or clipboard operations, it does need to
	 * return unique IDs for its object to support refresh in the SystemView.
	 * </p>
	 * <p>
	 * Because each subsystem maintains its own objects, it is the
	 * responsibility of the subsystem and its adapters to come up with a
	 * mapping that is unique for the subsystem. Some subsystems use fully
	 * qualified path names, while others may use other methods. Extenders just
	 * need to ensure that objects of different type (such as filters, actual
	 * resources or error messages) all have different IDs within the subsystem,
	 * and the corresponding
	 * {@link IRemoteObjectResolver#getObjectWithAbsoluteName(String, IProgressMonitor)}
	 * method actually finds the object by the given ID. Other subsystems do not
	 * need to be considered.
	 * </p>
	 * <p>
	 * <strong>Uniqueness and Multiple Contexts</strong><br/> The RSE
	 * SystemView allows the same remote object to be displayed in multiple
	 * different contexts, i.e. under multiple different filters. In this case,
	 * each occurrence of the same object must return the same absolute name.
	 * For the reverse mapping, however, it is up to the subsystem whether its
	 * {@link IRemoteObjectResolver} returns only one internal model object for
	 * the given identifier, or multiple context objects which all refer to the
	 * same remote object but also hold context information.
	 * </p>
	 * <p>
	 * <strong>Examples</strong><br/> In the File Subsystem, a fully qualified
	 * pathname is used to uniquely identify remote objects. For other kinds of
	 * objects maintained by the same subsystem, the following schemes are used:
	 * <ul>
	 * <li>The subsystem itself is identified as<br/> subsystemID ::=
	 * (profileName).(connectionName).(subsystemName)<br/> - see
	 * SystemViewSubSystemAdapter</li>
	 * <li>Filter Pool References are identified as<br/> filterPoolID ::=
	 * (subsystemID).(poolManagerName).(poolReferenceName)<br/> - see
	 * SystemViewFilterPoolReferenceAdapter</li>
	 * <li>Filter References are identified as <br/> filterRefID ::=
	 * (filterPoolID).(filterName)<br/> - see SystemViewFilterReferenceAdapter</li>
	 * <li>Search Results are identified by the
	 * {@link IHostSearchResult#SEARCH_RESULT_DELIMITER} embedded in the ID.</li>
	 * </ul>
	 * All these IDs for internal elements like the subsystem itself or the
	 * filters start with a profile name which must not contain any of the / \
	 * or : characters. Fully qualified path names, on the other hand, always
	 * start with a / or \ character (UNIX style paths, Windows UNC paths) or
	 * have a : character on the second position (Windows drive letters). The
	 * SEARCH_RESULT_DELIMITER is ":SEARCH" which cannot be part of a valid
	 * filename. Therefore, this naming scheme is guaranteed to be unique.
	 * </p>
	 *
	 * @see IRemoteObjectResolver#getObjectWithAbsoluteName(String,
	 *      IProgressMonitor)
	 *
	 * @param object the remote element to be identified.
	 * @return a String uniquely identifying the remote object within the
	 *         subsystem. Must not return <code>null</code>.
	 */
	public String getAbsoluteName(Object object);

}