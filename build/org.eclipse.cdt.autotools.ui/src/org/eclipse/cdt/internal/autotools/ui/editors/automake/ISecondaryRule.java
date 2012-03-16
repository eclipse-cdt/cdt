/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

/**
 * .SECONDARY
 *  The targets which `.SECONDARY' depends on are treated as
 *  intermediate files, except that they are never automatically deleted.
 *
 *  `.SECONDARY' with no prerequisites causes all targets to be treated
 *  as secondary (i.e., no target is removed because it is considered intermediate).
 *  
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISecondaryRule extends ISpecialRule {
}
