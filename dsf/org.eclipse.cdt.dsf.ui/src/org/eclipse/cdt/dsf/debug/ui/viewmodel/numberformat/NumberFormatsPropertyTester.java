/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Property tester for number format information available through the given
 * object.  The object being tested should be either an {@link IVMContext},
 * through which an instance of {@link IVMProvider} could be obtained.
 * Or it could be an {@link IWorkbenchPart}, which is tested to see if it
 * is a debug view through which a caching VM provider can be obtained.
 * The view's presentation context is used to test the given property.
 * <p>
 * Three properties are supported:
 * <ul>
 * <li> "areNumberFormatsSupported" - Checks whether number formats are
 * available at all given the receiver.</li>
 * <li> "isNumberFormatAvailable" - Checks whether the number format ID in the
 * expected value is available for the given receiver.</li>
 * <li> "isNumberFormatActive" - Checks whether the number format ID in the expected
 * value is the currently active number format for the given receiver.</li>
 * </ul>
 * </p>
 *
 * @since 1.0
 */
public class NumberFormatsPropertyTester extends PropertyTester {

	private static final String SUPPORTED = "areNumberFormatsSupported"; //$NON-NLS-1$
	private static final String ELEMENT_FORMATS_SUPPORTED = "areElementNumberFormatsSupported"; //$NON-NLS-1$
	private static final String AVAILABLE = "isNumberFormatAvailable"; //$NON-NLS-1$
	private static final String ACTIVE = "isNumberFormatActive"; //$NON-NLS-1$

	private static final List<String> AVAILABLE_FORMATS = new ArrayList<>();
	static {
		AVAILABLE_FORMATS.add(IFormattedValues.NATURAL_FORMAT);
		AVAILABLE_FORMATS.add(IFormattedValues.HEX_FORMAT);
		AVAILABLE_FORMATS.add(IFormattedValues.DECIMAL_FORMAT);
		AVAILABLE_FORMATS.add(IFormattedValues.OCTAL_FORMAT);
		AVAILABLE_FORMATS.add(IFormattedValues.BINARY_FORMAT);
		AVAILABLE_FORMATS.add(IFormattedValues.STRING_FORMAT);
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IVMContext) {
			IVMProvider provider = ((IVMContext) receiver).getVMNode().getVMProvider();
			if (provider != null) {
				return testProvider(provider, property, expectedValue, (IVMContext) receiver);
			}
		} else if (receiver instanceof IDebugView) {
			IVMProvider provider = VMHandlerUtils.getVMProviderForPart((IDebugView) receiver);
			if (provider != null) {
				return testProvider(provider, property, expectedValue, null);
			}
		}
		return false;
	}

	private boolean testProvider(IVMProvider provider, String property, Object expectedValue, IVMContext vmctx) {
		if (SUPPORTED.equals(property)) {
			return true;
		} else if (AVAILABLE.equals(property)) {
			return AVAILABLE_FORMATS.contains(expectedValue);
		} else if (ACTIVE.equals(property)) {
			Object activeId = provider.getPresentationContext()
					.getProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE);
			return expectedValue != null && expectedValue.equals(activeId);
		} else if (ELEMENT_FORMATS_SUPPORTED.equals(property)) {
			if (provider instanceof IElementFormatProvider) {
				return ((IElementFormatProvider) provider).supportFormat(vmctx);
			}
		}
		return false;
	}

}
