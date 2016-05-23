/*******************************************************************************
 * Copyright (c) 2008, 2016 Stefan Sprenger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * 
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.actions;

import java.util.List;


/**

 * @since 5.0
 */
public interface IResolve {
	
	/*
	 * 
	 * Resolves the entries from an entered string list of filepaths
	 */
	public void resolve(List<String> fileList);
}
