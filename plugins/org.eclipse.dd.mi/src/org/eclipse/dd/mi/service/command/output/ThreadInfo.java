/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.output;

import org.eclipse.dd.dsf.concurrent.Immutable;

/**
 * @since 1.1
 */
@Immutable
public class ThreadInfo implements IThreadInfo {

	final private String       fThreadId;
	final private String       fTargetId;
	final private String       fOsId;
	final private String       fParentId;
	final private IThreadFrame fTopFrame;
	final private String       fDetails;
	final private String       fState;
	
	public ThreadInfo(String threadId, String targetId, String osId, String parentId,
			          IThreadFrame topFrame, String details, String state) {
		fThreadId  = threadId;
		fTargetId  = targetId;
		fOsId      = osId;
		fParentId  = parentId;
		fTopFrame  = topFrame;
		fDetails   = details;
		fState     = state;
	}

	public String getThreadId()       { return fThreadId; }
	public String getTargetId()       { return fTargetId; }
	public String getOsId()           { return fOsId;     }
	public String getParentId()       { return fParentId; }
	public IThreadFrame getTopFrame() { return fTopFrame; } 
	public String getDetails()        { return fDetails;  }
	public String getState()          { return fState;    }
}
