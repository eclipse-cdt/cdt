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
 * .SCCS_GET
 * The application shall ensure that this special target is specified without prerequesites.
 * The commands specified with this target shall replace the default
 * commands associated with this special target.
 */
public interface ISccsGetRule extends ISpecialRule {
}
