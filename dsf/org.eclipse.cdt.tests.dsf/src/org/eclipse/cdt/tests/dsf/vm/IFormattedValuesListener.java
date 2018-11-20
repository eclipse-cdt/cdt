/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;

/**
 * @since 2.2
 */
public interface IFormattedValuesListener {

	public void formattedValueUpdated(FormattedValueDMContext formattedValueDmc);

}
