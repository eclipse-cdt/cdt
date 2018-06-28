/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lidia Popescu - [536255] initial API and implementation. Extension point for open call hierarchy view 
 *******************************************************************************/
package org.eclipse.cdt.ui;


import org.eclipse.cdt.core.model.ICElement;

/**
 * The Call Hierarchy Extension Node
 * @since 6.4
 */
public interface ICHENode {

	ICElement getRepresentedDeclaration();
}
