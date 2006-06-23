/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.makefile;

/**
 * Target rule that have special meaning for Make.
 */
public interface ISpecialRule extends IRule {

	/**
	 * The meaning of the prerequistes are specific to
	 * each rules.
	 */
	String[] getPrerequisites();
}
