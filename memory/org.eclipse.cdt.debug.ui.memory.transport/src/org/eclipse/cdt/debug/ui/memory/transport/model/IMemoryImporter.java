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

import org.eclipse.cdt.debug.ui.memory.transport.ImportMemoryDialog;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IMemoryImporter 
{
	public static final String TRANSFER_FILE = "File";
	public static final String TRANSFER_START = "Start";
	public static final String TRANSFER_END = "End";
	public static final String TRANSFER_CUSTOM_START_ADDRESS = "CustomStartAddress";
	public static final String TRANSFER_SCROLL_TO_START = "ScrollToStart";
	
	public Control createControl(Composite parent, IMemoryBlock memBlock, Properties properties, ImportMemoryDialog parentDialog);
	
	public void importMemory();
	
	public String getId();
	
	public String getName();
}
