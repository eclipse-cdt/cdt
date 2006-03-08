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
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 * 
 * This is the search query to be used for searching the PDOM.
 */
public class PDOMSearchBindingQuery extends PDOMSearchQuery {

	private PDOMBinding binding;
	
	public PDOMSearchBindingQuery(PDOMBinding binding, int flags) {
		super(flags);
		this.binding = binding;
	}
	
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		try {
			if ((flags & FIND_DECLARATIONS) != 0) {
				PDOMName name = binding.getFirstDeclaration();
				while (name != null) {
					IASTFileLocation loc = name.getFileLocation();
					result.addMatch(new PDOMSearchMatch(name, loc.getNodeOffset(), loc.getNodeLength()));
					name = name.getNextInBinding();
				}
			}
			if ((flags & (FIND_DECLARATIONS)) != 0) {
				// for decls we do defs too
				PDOMName name = binding.getFirstDefinition();
				while (name != null) {
					IASTFileLocation loc = name.getFileLocation();
					result.addMatch(new PDOMSearchMatch(name, loc.getNodeOffset(), loc.getNodeLength()));
					name = name.getNextInBinding();
				}
			}
			if ((flags & FIND_REFERENCES) != 0) {
				PDOMName name = binding.getFirstReference();
				while (name != null) {
					IASTFileLocation loc = name.getFileLocation();
					result.addMatch(new PDOMSearchMatch(name, loc.getNodeOffset(), loc.getNodeLength()));
					name = name.getNextInBinding();
				}
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e);
		}
	}

	public String getLabel() {
		return super.getLabel() + " " + binding.getName();
	}

}
