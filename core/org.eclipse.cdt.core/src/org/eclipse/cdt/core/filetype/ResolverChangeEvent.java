/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.filetype;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;


public class ResolverChangeEvent extends EventObject {

	private List fDeltas = new ArrayList();
	private ICFileTypeResolver fNewResolver;
	private ICFileTypeResolver fOldResolver;

	/**
	 * Create a new resolver change event.  The event is empty
	 * of any change deltas, and references the provided file
	 * type resolver.
	 * 
	 * @param resolver file type resolver this event applies to
	 */
	public ResolverChangeEvent(IResolverModel model, ICFileTypeResolver resolver) {
		this(model, resolver, null);
	}

	/**
	 * Create a new resolver change event.  The event is empty
	 * of any change deltas, and references the provided file
	 * type resolver.
	 * 
	 * @param resolver file type resolver this event applies to
	 */
	public ResolverChangeEvent(IResolverModel model, ICFileTypeResolver newResolver, ICFileTypeResolver oldResolver) {
		super(model);
		fNewResolver = newResolver;
		fOldResolver = oldResolver;
	}

	public boolean resolverHasChanged() {
		return fOldResolver != null;
	}

	/**
	 * @return resolver affected by this change
	 */
	public ICFileTypeResolver getResolver() {
		return fNewResolver;
	}

	/**
	 * @return number of resolver deltas involved in this change
	 */
	public int getDeltaCount() {
		return fDeltas.size();
	}
	
	/**
	 * @return ResolverDelta[] for this change
	 */
	public ResolverDelta[] getDeltas() {
		return (ResolverDelta[]) fDeltas.toArray(new ResolverDelta[fDeltas.size()]);
	}

	/**
	 * Add a new delta to the list of deltas.
	 * 
	 * @param delta instance of ResolverDelta to add to the list.
	 */
	public void addDelta(ResolverDelta delta) {
		fDeltas.add(delta);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("ResolverChangeEvent ["); //$NON-NLS-1$
		buf.append(fDeltas.size());
		buf.append(" delta(s)]"); //$NON-NLS-1$
		
		for (Iterator iter = fDeltas.iterator(); iter.hasNext();) {
			ResolverDelta element = (ResolverDelta) iter.next();
			buf.append("\n  "); //$NON-NLS-1$
			buf.append(element.toString());
		}
		
		return buf.toString();
	}


}
