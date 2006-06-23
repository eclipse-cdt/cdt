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
package org.eclipse.cdt.make.core.makefile;

/**
 * .DEFAULT
 * If the makefile uses this special target, the application shall ensure that it is
 * specified with commands, but without prerequisites.
 * The commands shall be used by make if there are no other rules available to build a target.
 */
public interface IDefaultRule extends ISpecialRule {
}
