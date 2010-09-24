/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
