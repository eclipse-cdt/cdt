/*******************************************************************************
 * Copyright (c) 2013 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;


/**
 * Interface provides an additional method to the CDT model property page.
 * 
 * @see ICPropertyProvider
 * @since 5.7
 */

public interface ICPropertyProvider2 extends ICPropertyProvider {
	
	/**
	 * Return the selected tab, or null if the select tab isn't initialized
	 * @return the select tab in the page
	 */
	ICPropertyTab getSelectedTab();
}
