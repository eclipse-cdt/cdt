/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMInclude {

	private final PDOM pdom;
	private final int record;
	
	private final int INCLUDES = 0;
	private final int INCLUDED_BY = 4;
	private final int INCLUDES_NEXT = 8;
	private final int INCLUDED_BY_NEXT = 12;
	private final int INCLUDED_BY_PREV = 16;
	
	private final int RECORD_SIZE = 20;

	public PDOMInclude(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public PDOMInclude(PDOM pdom) throws CoreException {
		this.pdom = pdom;
		this.record = pdom.getDB().malloc(RECORD_SIZE);
	}
	
	public int getRecord() {
		return record;
	}
	
	public void delete() throws CoreException {
		// Remove us from the includedBy chain
		PDOMInclude prevInclude = getPrevInIncludedBy();
		PDOMInclude nextInclude = getNextInIncludedBy();
		if (prevInclude != null)
			prevInclude.setNextInIncludedBy(nextInclude);
		else
			getIncludes().setFirstIncludedBy(null);
		
		if (nextInclude != null)
			nextInclude.setPrevInIncludedBy(prevInclude);
		
		// Delete our record
		pdom.getDB().free(record);
	}
	
	public PDOMFile getIncludes() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDES);
		return rec != 0 ? new PDOMFile(pdom, rec) : null;
	}
	
	public void setIncludes(PDOMFile includes) throws CoreException {
		pdom.getDB().putInt(record + INCLUDES, includes.getRecord());
	}
	
	public PDOMFile getIncludedBy() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDED_BY);
		return rec != 0 ? new PDOMFile(pdom, rec) : null;
	}
	
	public void setIncludedBy(PDOMFile includedBy) throws CoreException {
		int rec = includedBy != null ? includedBy.getRecord() : 0;
		pdom.getDB().putInt(record + INCLUDED_BY, rec);
	}
	
	public PDOMInclude getNextInIncludes() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDES_NEXT);
		return rec != 0 ? new PDOMInclude(pdom, rec) : null;
	}
	
	public void setNextInIncludes(PDOMInclude include) throws CoreException {
		pdom.getDB().putInt(record + INCLUDES_NEXT, include.getRecord());
	}
	
	public PDOMInclude getNextInIncludedBy() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDED_BY_NEXT);
		return rec != 0 ? new PDOMInclude(pdom, rec) : null;
	}
	
	public void setNextInIncludedBy(PDOMInclude include) throws CoreException {
		pdom.getDB().putInt(record + INCLUDED_BY_NEXT, include.getRecord());
	}
	
	public PDOMInclude getPrevInIncludedBy() throws CoreException {
		int rec = pdom.getDB().getInt(record + INCLUDED_BY_PREV);
		return rec != 0 ? new PDOMInclude(pdom, rec) : null;
	}
	
	public void setPrevInIncludedBy(PDOMInclude include) throws CoreException {
		pdom.getDB().putInt(record + INCLUDED_BY_PREV, include.getRecord());
	}
	
}
