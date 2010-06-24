/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 *  This interface extends the expressions service with support for 
 *  model data extension requests.
 *  
 *  @since 2.2
 */
public interface IExpressions3 extends IExpressions2 {
    
    /**
     * The model data interface extension.
     */
    public interface IExpressionDMDataExtension extends IExpressionDMData {
        
        /**
         * @return Whether the expression has children.
         */
        boolean hasChildren();
    }

    /**
     * Retrieves the expression data extension object for the given 
     * expression context(<tt>dmc</tt>).
     * 
     * @param dmc
     *            The ExpressionDMC for the expression to be evaluated.
     * @param rm
     *            The data request monitor that will contain the requested data
     */
    void getExpressionDataExtension(
            IExpressionDMContext dmc, 
            DataRequestMonitor<IExpressionDMDataExtension> rm);
}
