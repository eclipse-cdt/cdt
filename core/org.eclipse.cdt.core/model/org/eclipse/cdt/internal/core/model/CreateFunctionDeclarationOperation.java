package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.CModelException;

/**
 * <p>This operation adds an include declaration to an existing translation unit.
 * If the translation unit already includes the specified include declaration,
 * the include is not generated (it does not generate duplicates).
 *
 * <p>Required Attributes:<ul>
 *  <li>Translation unit
 *  <li>Include name - the name of the include to add to the
 *      translation unit. For example: <code>"stdio.h"</code>
 * </ul>
 */
public class CreateFunctionDeclarationOperation extends CreateElementInTUOperation {

	/**
	 * The name of the include to be created.
	 */
	protected String fFunction;

	/**
	 * When executed, this operation will add an include to the given translation unit.
	 */
	public CreateFunctionDeclarationOperation(String function, ITranslationUnit parentElement) {
		super(parentElement);
		fFunction = function;
	}

	/**
	 * @see CreateElementInCUOperation#generateResultHandle
	 */
	protected ICElement generateResultHandle() {
		try {
			return getTranslationUnit().getElement(fFunction);
		} catch (CModelException e) {
		}
		return null;
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName(){
		return "operation.createIncludeProgress"; //$NON-NLS-1$
	}

	/**
	 * Sets the correct position for the new include:<ul>
	 * <li> after the last include
	 * <li> if no include, before the first type
	 * <li> if no type, after the package statement
	 * <li> and if no package statement - first thing in the CU
	 */
	protected void initializeDefaultPosition() {
		try {
			ITranslationUnit tu = getTranslationUnit();
			IInclude[] includes = tu.getIncludes();
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
	 * @see CNamingConventions
	 */
	public ICModelStatus verify() {
		ICModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		//if (CConventions.validateInclude(fIncludeName).getSeverity() == IStatus.ERROR) {
		//	return new CModelStatus(ICModelStatusConstants.INVALID_NAME, fIncludeName);
		//}
		return CModelStatus.VERIFIED_OK;
	}
}
