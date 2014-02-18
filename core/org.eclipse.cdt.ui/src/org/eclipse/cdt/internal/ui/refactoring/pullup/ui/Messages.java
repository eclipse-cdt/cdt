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
package org.eclipse.cdt.internal.ui.refactoring.pullup.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String PullUpRefactoring_columnMember;
	public static String PullUpRefactoring_columnCurrentVisibility;
	public static String PullUpRefactoring_columnTargetVisibility;
	public static String PullUpRefactoring_columnTargetAction;
	public static String PullUpRefactoring_actionPullUp;
	public static String PullUpRefactoring_actionDeclareVirtual;

	public static String PullUpRefactoring_selectRequired;
	public static String PullUpRefactoring_selectAll;
	public static String PullUpRefactoring_deselectAll;
	public static String PullUpRefactoring_insertStubs;
	public static String PullUpRefactoring_pullUpIntoPureAbstract;
	public static String PullUpRefactoring_pullUpFrom;
	public static String PullUpRefactoring_selectTarget;
	public static String PullUpRefactoring_selectMembers;

	public static String PushDownRefactoring_actionLeaveVirtual;
	public static String PushDownRefactoring_actionPushDown;
	public static String PushDownRefactoring_actionExistingDefiniton;
	public static String PushDownRefactoring_actionMethodStub;
	public static String PushDownRefactoring_selectMemberPage;
	public static String PushDownRefactoring_selectTargetsPage;
	public static String PushDownRefactoring_pushDownFrom;
	public static String PushDownRefactoring_selectMembersToPushDown;
	public static String PushDownRefactoring_selectTargetClasses;
	public static String PushDownRefactoring_columnInsert;
	public static String PushDownRefactoring_columnInfo;
	public static String PushDownRefactoring_cantModifyMandatory;
	
	public static String PullUpRefactoring_selectMemberPage;
	public static String PullUpRefactoring_selectRemovePage;
	public static String PullUpRefactoring_checkAdditionalMembers;
	
	
	private Messages() {
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
