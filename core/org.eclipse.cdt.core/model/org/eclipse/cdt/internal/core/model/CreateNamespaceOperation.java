/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * @author User
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CreateNamespaceOperation extends CreateElementInTUOperation {


	/**
	 * The name of the include to be created.
	 */
	protected String fNamespace;

	/**
	 * When executed, this operation will add an include to the given translation unit.
	 */
	public CreateNamespaceOperation(String namespace, ITranslationUnit parentElement) {
		super(parentElement);
		fNamespace = namespace;
	}

	/**
	 * @see CreateElementInCUOperation#generateResultHandle
	 */
	protected ICElement generateResultHandle() {
		return getTranslationUnit().getNamespace(fNamespace);
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName(){
		return "operation.createNamespaceProgress"; //$NON-NLS-1$
	}

	/**
	 * Sets the correct position for the new namespace:<ul>
	 * <li> after the last namespace
	 * </ul>
	 */
	protected void initializeDefaultPosition() {
		try {
			ITranslationUnit cu = getTranslationUnit();
			IInclude[] includes = cu.getIncludes();
			if (includes.length > 0) {
				createAfter(includes[includes.length - 1]);
				return;
			}
		} catch (CModelException npe) {
		}
	}

	/*
	 * TODO: Use the ASTRewrite once it is available.
	 */
	protected String generateElement(ITranslationUnit unit) throws CModelException {
		StringBuffer sb = new StringBuffer();
		sb.append("namespace "); //$NON-NLS-1$;
		sb.append(fNamespace).append(' ').append('{');
		sb.append(Util.LINE_SEPARATOR);
		sb.append('}'); //$NON-NLS-1$;
		sb.append(Util.LINE_SEPARATOR);
		return sb.toString();
	}
}
