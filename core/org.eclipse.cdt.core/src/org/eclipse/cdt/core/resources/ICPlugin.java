package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;

public interface ICPlugin extends IAdaptable {
	IMessageDialog getMessageDialog();
    IPropertyStore getPropertyStore();
    IConsole getConsole();
}

