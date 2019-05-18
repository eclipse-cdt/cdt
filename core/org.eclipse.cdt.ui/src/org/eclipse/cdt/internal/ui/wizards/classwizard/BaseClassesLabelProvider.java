/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.ui.wizards.classwizard.IMethodStub.EImplMethod;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public final class BaseClassesLabelProvider implements ITableLabelProvider {
	private static final String YES_VALUE = NewClassWizardMessages.BaseClassesLabelProvider_boolean_yes_label;
	private static final String NO_VALUE = NewClassWizardMessages.BaseClassesLabelProvider_boolean_no_label;
	private static final String ACCESS_PUBLIC = NewClassWizardMessages.BaseClassesLabelProvider_access_public_label;
	private static final String ACCESS_PROTECTED = NewClassWizardMessages.BaseClassesLabelProvider_access_protected_label;
	private static final String ACCESS_PRIVATE = NewClassWizardMessages.BaseClassesLabelProvider_access_private_label;

	private static final String IMPL_DEFINITION = NewClassWizardMessages.BaseClassesLabelProvider_impl_definition;
	private static final String IMPL_DEFAULT = NewClassWizardMessages.BaseClassesLabelProvider_impl_default;
	private static final String IMPL_DELETED = NewClassWizardMessages.BaseClassesLabelProvider_impl_deleted;
	private static final String IMPL_INLINE = NewClassWizardMessages.BaseClassesLabelProvider_impl_inline;

	public static final String getYesNoText(boolean value) {
		return value ? YES_VALUE : NO_VALUE;
	}

	public static final String getAccessText(ASTAccessVisibility access) {
		if (access == ASTAccessVisibility.PRIVATE)
			return ACCESS_PRIVATE;
		if (access == ASTAccessVisibility.PROTECTED)
			return ACCESS_PROTECTED;
		return ACCESS_PUBLIC;
	}

	public static final String getImplText(EImplMethod method) {
		switch (method) {
		case DELETED:
			return IMPL_DELETED;
		case DEFAULT:
			return IMPL_DEFAULT;
		case INLINE:
			return IMPL_INLINE;
		case DEFINITION:
		default:
			return IMPL_DEFINITION;
		}
	}

	private static TypeInfoLabelProvider fTypeInfoLabelProvider = new TypeInfoLabelProvider(
			TypeInfoLabelProvider.SHOW_FULLY_QUALIFIED);

	/*
	 * @see ITableLabelProvider#getColumnImage(Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex != 0)
			return null;

		IBaseClassInfo info = (IBaseClassInfo) element;
		return fTypeInfoLabelProvider.getImage(info.getType());
	}

	/*
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		IBaseClassInfo info = (IBaseClassInfo) element;

		switch (columnIndex) {
		case 0:
			return fTypeInfoLabelProvider.getText(info.getType());
		case 1:
			return getAccessText(info.getAccess());
		case 2:
			return getYesNoText(info.isVirtual());
		default:
			return null;
		}
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}
}
