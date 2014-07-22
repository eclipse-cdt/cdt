/*******************************************************************************
 * Copyright (c) 2014, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.ui.viewmodel;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @since 2.5
 */
public interface IGdbViewModelServicesFactory {

	GdbViewModelAdapter createGdbViewModelAdapter(DsfSession session, SteppingController steppingController);
	
}