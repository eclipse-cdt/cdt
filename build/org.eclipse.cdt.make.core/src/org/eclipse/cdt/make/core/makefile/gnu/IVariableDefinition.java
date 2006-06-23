/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.IMacroDefinition;

/**
 */
public interface IVariableDefinition extends IMacroDefinition {
	
	boolean isRecursivelyExpanded();
	
	boolean isSimplyExpanded();
	
	boolean isConditional();
	
	boolean isAppend();
	
	boolean isTargetSpecific();
	
	boolean isExport();
	
	boolean isMultiLine();
	
	/**
	 * Variable from an `override' directive.
	 */
	boolean isOverride();

	/**
	 * Automatic variable -- cannot be set.
	 */
	boolean isAutomatic();
	String getTarget();
}
