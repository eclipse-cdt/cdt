/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * Enter type comment.
 * 
 * @since Mar 31, 2003
 */
public interface ICRegisterManager extends ICUpdateManager, IAdaptable
{
	void initialize();
	
	IRegisterGroup[] getRegisterGroups() throws DebugException;
	
	void addRegisterGroup( IRegisterGroup group );
	
	void removeRegisterGroup( IRegisterGroup group );

	void removeAllRegisterGroups();

	void reset();
	
	void dispose();
}
