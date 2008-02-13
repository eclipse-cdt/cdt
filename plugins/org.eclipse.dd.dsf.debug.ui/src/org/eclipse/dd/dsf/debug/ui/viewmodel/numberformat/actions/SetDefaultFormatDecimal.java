/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.numberformat.actions;

import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.ui.viewmodel.numberformat.AbstractSetFormatStyle;

@SuppressWarnings("restriction")
public class SetDefaultFormatDecimal extends AbstractSetFormatStyle {

    @Override
    protected String getFormatStyle() {
        return IFormattedValues.DECIMAL_FORMAT;
    }
}
