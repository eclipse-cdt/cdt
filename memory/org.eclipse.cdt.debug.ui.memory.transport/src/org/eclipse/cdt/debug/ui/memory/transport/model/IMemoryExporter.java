/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport.model;

import java.util.Properties;

import org.eclipse.cdt.debug.ui.memory.transport.ExportMemoryDialog;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IMemoryExporter 
{
	public static final String TRANSFER_FILE = "File";
	public static final String TRANSFER_START = "Start";
	public static final String TRANSFER_END = "End";
	
	/**
	 * @param parent
	 * @param memBlock
	 * @param properties This will contain initial values for TRANSFER_START and TRANSFER_END. 
	 * @param parentDialog
	 * @return
	 */
	public Control createControl(Composite parent, IMemoryBlock memBlock, Properties properties, ExportMemoryDialog parentDialog);
		
	public void exportMemory();
	
	public String getId();
	
	public String getName();
}
