/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Feb 22, 2005
 */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTVisitor;

/**
 * @author aniefer
 */
public interface ICASTVisitor extends IASTVisitor {
    
	public static abstract class CBaseVisitorAction extends BaseVisitorAction {
		public boolean processDesignators    = false;
		
        public int processDesignator( ICASTDesignator designator )  { return PROCESS_CONTINUE; }
	}

	public boolean visitDesignator( ICASTDesignator designator, BaseVisitorAction action );
}