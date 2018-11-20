/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * Implementation of a <code>ISelectionValidator</code> to validate the
 * type of an element.
 * Empty selections are not accepted.
 */
public class TypedElementSelectionValidator implements ISelectionStatusValidator {

	private IStatus fgErrorStatus = new StatusInfo(IStatus.ERROR, ""); //$NON-NLS-1$
	private IStatus fgOKStatus = new StatusInfo();

	private Class<?>[] fAcceptedTypes;
	private boolean fAllowMultipleSelection;
	private Collection<?> fRejectedElements;

	/**
	 * @param acceptedTypes The types accepted by the validator
	 * @param allowMultipleSelection If set to <code>true</code>, the validator
	 * allows multiple selection.
	 */
	public TypedElementSelectionValidator(Class<?>[] acceptedTypes, boolean allowMultipleSelection) {
		this(acceptedTypes, allowMultipleSelection, null);
	}

	/**
	 * @param acceptedTypes The types accepted by the validator
	 * @param allowMultipleSelection If set to <code>true</code>, the validator
	 * allows multiple selection.
	 * @param rejectedElements A list of elements that are not accepted
	 */
	public TypedElementSelectionValidator(Class<?>[] acceptedTypes, boolean allowMultipleSelection,
			Collection<Object> rejectedElements) {
		Assert.isNotNull(acceptedTypes);
		fAcceptedTypes = acceptedTypes;
		fAllowMultipleSelection = allowMultipleSelection;
		fRejectedElements = rejectedElements;
	}

	/*
	 * @see org.eclipse.ui.dialogs.ISelectionValidator#isValid(java.lang.Object)
	 */
	@Override
	public IStatus validate(Object[] elements) {
		if (isValid(elements)) {
			return fgOKStatus;
		}
		return fgErrorStatus;
	}

	private boolean isOfAcceptedType(Object o) {
		for (int i = 0; i < fAcceptedTypes.length; i++) {
			if (fAcceptedTypes[i].isInstance(o)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRejectedElement(Object elem) {
		return (fRejectedElements != null) && fRejectedElements.contains(elem);
	}

	private boolean isValid(Object[] selection) {
		if (selection.length == 0) {
			return false;
		}

		if (!fAllowMultipleSelection && selection.length != 1) {
			return false;
		}

		for (int i = 0; i < selection.length; i++) {
			Object o = selection[i];
			if (!isOfAcceptedType(o) || isRejectedElement(o)) {
				return false;
			}
		}
		return true;
	}
}
