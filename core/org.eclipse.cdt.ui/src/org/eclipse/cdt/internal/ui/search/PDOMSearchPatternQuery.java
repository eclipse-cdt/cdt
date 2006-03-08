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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchPatternQuery extends PDOMSearchQuery {

	// First bit after the FINDs in PDOMSearchQuery.
	public static final int FIND_CLASS_STRUCT = 0x10;
	public static final int FIND_FUNCTION = 0x20;
	public static final int FIND_VARIABLE = 0x40;
	public static final int FIND_UNION = 0x100;
	public static final int FIND_METHOD = 0x200;
	public static final int FIND_FIELD = 0x400;
	public static final int FIND_ENUM = 0x1000;
	public static final int FIND_ENUMERATOR = 0x2000;
	public static final int FIND_NAMESPACE = 0x4000;
	public static final int FIND_TYPEDEF = 0x10000;
	public static final int FIND_MACRO = 0x20000;
	public static final int FIND_ALL_TYPES
		= FIND_CLASS_STRUCT | FIND_FUNCTION | FIND_VARIABLE
		| FIND_UNION | FIND_METHOD | FIND_FIELD | FIND_ENUM
		| FIND_ENUMERATOR | FIND_NAMESPACE | FIND_TYPEDEF | FIND_MACRO;
	
	private IResource[] scope;
	private String scopeDesc;
	private String pattern;
	
	public PDOMSearchPatternQuery(
			IResource[] scope,
			String scopeDesc,
			String pattern,
			boolean isCaseSensitive,
			int flags) {
		super(flags);
		this.scope = scope;
		this.scopeDesc = scopeDesc;
		this.pattern = pattern;
	}
	
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		// TODO Auto-generated method stub
		return Status.OK_STATUS;
	}
	
	public String getLabel() {
		return super.getLabel() + " " + pattern + " in " + scopeDesc;
	}
	
}
