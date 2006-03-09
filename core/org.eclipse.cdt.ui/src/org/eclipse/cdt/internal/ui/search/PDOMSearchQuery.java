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

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMSearchQuery implements ISearchQuery {

	public static final int FIND_DECLARATIONS = 0x1;
	public static final int FIND_DEFINITIONS = 0x2;
	public static final int FIND_REFERENCES = 0x4;
	public static final int FIND_ALL_OCCURANCES = FIND_DECLARATIONS | FIND_DEFINITIONS | FIND_REFERENCES;
	
	protected PDOMSearchResult result = new PDOMSearchResult(this);
	protected int flags;

	protected PDOMSearchQuery(int flags) {
		this.flags = flags;
	}
	
	public String getLabel() {
		String type;
		if ((flags & FIND_REFERENCES) != 0)
			type = CSearchMessages.getString("PDOMSearch.query.refs.label"); //$NON-NLS-1$
		else if ((flags & FIND_DECLARATIONS) != 0)
			type = CSearchMessages.getString("PDOMSearch.query.decls.label"); //$NON-NLS-1$
		else
 			type = CSearchMessages.getString("PDOMSearch.query.defs.label"); //$NON-NLS-1$
		return type;
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public ISearchResult getSearchResult() {
		return result;
	}

	/**
	 * Return true to filter name out of the match list.
	 * Override in a subclass to add scoping.
	 * @param name
	 * @return true to filter name out of the match list
	 */
	protected boolean filterName(PDOMName name) {
		return false; // i.e. keep it
	}
	
	private void collectNames(PDOMName name) throws CoreException {
		while (name != null) {
			if (!filterName(name)) {
				IASTFileLocation loc = name.getFileLocation();
				result.addMatch(new PDOMSearchMatch(name, loc.getNodeOffset(), loc.getNodeLength()));
				name = name.getNextInBinding();
			}
		}
	}

	protected void createMatches(PDOMBinding binding) throws CoreException {
		if ((flags & FIND_DECLARATIONS) != 0) {
			collectNames(binding.getFirstDeclaration());
		}
		if ((flags & FIND_DECLARATIONS) != 0) {
			collectNames(binding.getFirstDefinition());
		}
		if ((flags & FIND_REFERENCES) != 0) {
			collectNames(binding.getFirstReference());
		}
		
	}

}
