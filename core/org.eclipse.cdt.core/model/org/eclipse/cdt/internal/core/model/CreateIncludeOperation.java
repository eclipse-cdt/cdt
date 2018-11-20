/*******************************************************************************
 * Copyright (c) 2002, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>This operation adds an include declaration to an existing translation unit.
 * If the translation unit already includes the specified include declaration,
 * the include is not generated (it does not generate duplicates).
 *
 * <p>Required Attributes:<ul>
 *  <li>Translation unit
 *  <li>Include name - the name of the include to add to the
 *      translation unit. For example: <code>stdio.h</code>
 * </ul>
 */
public class CreateIncludeOperation extends CreateElementInTUOperation {

	/**
	 * The name of the include to be created.
	 */
	protected String fIncludeName;

	/**
	 * Whether the include is a std include.
	 */

	protected boolean fIsStandard;

	/**
	 * When executed, this operation will add an include to the given translation unit.
	 */
	public CreateIncludeOperation(String includeName, boolean isStd, ITranslationUnit parentElement) {
		super(parentElement);
		fIsStandard = isStd;
		fIncludeName = includeName;
	}

	/**
	 * @see CreateElementInTUOperation#generateResultHandle
	 */
	@Override
	protected ICElement generateResultHandle() {
		return getTranslationUnit().getInclude(fIncludeName);
	}

	/**
	 * @see CreateElementInTUOperation#getMainTaskName
	 */
	@Override
	public String getMainTaskName() {
		return "operation.createIncludeProgress"; //$NON-NLS-1$
	}

	/**
	 * Sets the correct position for the new include:<ul>
	 * <li> after the last include
	 * </ul>
	 *  if no include
	 */
	@Override
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

	/**
	 * Possible failures: <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - the compilation unit supplied to the operation is
	 * 		<code>null</code>.
	 *  <li>INVALID_NAME - not a valid include declaration name.
	 * </ul>
	 * @see ICModelStatus
	 * @see CConventions
	 */
	@Override
	public ICModelStatus verify() {
		ICModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		IProject project = getParentElement().getCProject().getProject();
		if (CConventions.validateIncludeName(project, fIncludeName).getSeverity() == IStatus.ERROR) {
			return new CModelStatus(ICModelStatusConstants.INVALID_NAME, fIncludeName);
		}
		return CModelStatus.VERIFIED_OK;
	}

	/*
	 * TODO: Use the ASTRewrite once it is available.
	 */
	@Override
	protected String generateElement(ITranslationUnit unit) throws CModelException {
		StringBuilder sb = new StringBuilder();
		sb.append("#include "); //$NON-NLS-1$;
		if (fIsStandard) {
			sb.append('<');
		} else {
			sb.append('"');
		}
		sb.append(fIncludeName);
		if (fIsStandard) {
			sb.append('>');
		} else {
			sb.append('"');
		}
		sb.append(Util.LINE_SEPARATOR);
		return sb.toString();
	}
}
