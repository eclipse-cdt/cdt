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
import java.util.Iterator;
import java.util.List;

public class ResolverChangeEvent {
	private ICFileTypeResolver	fResolver;
	private List				fDeltas = new ArrayList();

	/**
	 * Create a new resolver change event.  The event is empty
	 * of any change deltas, and references the provided file
	 * type resolver.
	 * 
	 * @param resolver file type resolver this event applies to
	 */
	public ResolverChangeEvent(ICFileTypeResolver resolver) {
		fResolver = resolver;
	}
	
	/**
	 * @return resolver affected by this change
	 */
	public ICFileTypeResolver getResolver() {
		return fResolver;
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
		
		buf.append("ResolverChangeEvent [");
		buf.append(fDeltas.size());
		buf.append(" delta(s)]");
		
		for (Iterator iter = fDeltas.iterator(); iter.hasNext();) {
			ResolverDelta element = (ResolverDelta) iter.next();
			buf.append("\n  ");
			buf.append(element.toString());
		}
		
		return buf.toString();
	}

}
