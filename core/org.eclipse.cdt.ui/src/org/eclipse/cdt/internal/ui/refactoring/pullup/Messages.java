/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String PullUpRefactoring_name;
	public static String PullUpRefactoring_noSelection;
	public static String PullUpRefactoring_invalidSelection;
	public static String PullUpRefactoring_noClass;
	public static String PullUpRefactoring_noBaseClass;
	public static String PullUpRefactoring_membersNotVisible;
	public static String PullUpRefactoring_memberStillReferenced;
	public static String PullUpRefactoring_diamond;
	public static String PullUpRefactoring_declarationExists;
	public static String PullUpRefactoring_requiredDependency;
	public static String PullUpRefactoring_targetIsAbstract;
	public static String PullUpRefactoring_canNotPullUpField;
	public static String PullUpRefactoring_canUniquelyResolveDeclaration;
	
	public static String PushDownRefactoring_noSubclasses;
	public static String PushDownRefactoring_memberIsReferenced;
	public static String PushDownRefactoring_requiredDependency;
	
	
	private Messages() {
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
