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
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;

/**
 * <p>This operation adds a using declaration/directive to an existing translation unit.
 *
 * <p>Required Attributes:<ul>
 *  <li>Translation unit
 *  <li>using name - the name of the using to add
 * </ul>
 */
public class CreateUsingOperation extends CreateElementInTUOperation {

	/**
	 * The name of the using to be created.
	 */
	protected String fUsingName;

	/**
	 * Whether it is a declaration or a directive.
	 */
	protected boolean fIsDirective;

	/**
	 * When executed, this operation will add an include to the given translation unit.
	 */
	public CreateUsingOperation(String usingName, boolean isDirective, ITranslationUnit parentElement) {
		super(parentElement);
		fIsDirective = isDirective;
		fUsingName = usingName;
	}

	/**
	 * @see CreateElementInCUOperation#generateResultHandle
	 */
	protected ICElement generateResultHandle() {
		return getTranslationUnit().getUsing(fUsingName);
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName(){
		return "operation.createUsingProgress"; //$NON-NLS-1$
	}

	/**
	 * Sets the correct position for the new using:<ul>
	 * <li> after the last using
	 * </ul>
	 *  if no using
	 */
	protected void initializeDefaultPosition() {
		try {
			ITranslationUnit cu = getTranslationUnit();
			IUsing[] usings = cu.getUsings();
			if (usings.length > 0) {
				createAfter(usings[usings.length - 1]);
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
		sb.append("using "); //$NON-NLS-1$;
		if (fIsDirective) {
			sb.append("namespace "); //$NON-NLS-1$
		}
		sb.append(fUsingName);
		sb.append(';');
		sb.append(Util.LINE_SEPARATOR);
		return sb.toString();
	}
}
