/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 * @since 1.0
 */
@SuppressWarnings("restriction")
public interface IFormattedValuePreferenceStore {
    /*
     *  Retrieves for the specified Presentation Context the configured format.
     *  
     *  @param context Specified Presentation Context 
     *  @return Format ID.
     */
    public String getCurrentNumericFormat( IPresentationContext context );
}
