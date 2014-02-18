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


/**
 * Defines actions which can be performed on members. The action names are simultaneously 
 * names that are displayed in the GUI. Instances of this class represent subsets of 
 * these actions that may be supported by certain refactorings. Like the 
 * PullUpMethodRefactoring only supports the actions {@link #PULL_UP}, 
 * {@link #DECLARE_VIRTUAL}, and {@link #NONE}.
 * 
 * @author Simon Taddiken
 */
public class TargetActions {

	/** Member is to be pushed down */
	public final static String PUSH_DOWN = Messages.PushDownRefactoring_actionPushDown;
	/** Member will be declared pure virtual in its current class */
	public final static String LEAVE_VIRTUAL = Messages.PushDownRefactoring_actionLeaveVirtual;
	/** Member will be pushed down */
	public final static String PULL_UP = Messages.PullUpRefactoring_actionPullUp;
	/** Member will be declared pure virtual in target class */
	public final static String DECLARE_VIRTUAL = Messages.PullUpRefactoring_actionDeclareVirtual;
	/** Existing definition will be pushed to target class */
	public final static String EXISTING_DEFINITION = Messages.PushDownRefactoring_actionExistingDefiniton;
	/** Method stub definition will be inserted in target class */
	public final static String METHOD_STUB = Messages.PushDownRefactoring_actionMethodStub;
	/** Removes a member from on class. Used for Pull Up refactoring and is never shown in gui*/
	public final static String REMOVE_METHOD = "TARGET.ACTIONS.REMOVE"; //$NON-NLS-1$
	/** Member will not be touched at all */
	public final static String NONE = ""; //$NON-NLS-1$
	
	
	
	private final String[] supported;
	
	/**
	 * Creates a new instance with a set of supported actions. The provided strings must 
	 * be any of the constants defined by this class.
	 * @param supported List of supported actions for the new instance.
	 */
	public TargetActions(String...supported) {
		this.supported = supported;
	}
	
	
	
	/**
	 * Gets the index within the array returned by {@link #getSupported()} of the 
	 * provided action. If the given action is not supported by this instance, -1 is 
	 * returned.
	 * @param action Action of which the index is to be retrieved.
	 * @return The index of the action or -1 if it is not supported by this instance.
	 */
	public int index(String action) {
		for (int i = 0; i < this.supported.length; ++i) {
			if (this.supported[i] == action) {
				return i;
			}
		}
		return -1;
	}
	
	
	
	/**
	 * Returns the set of all supported actions of this instance.
	 * @return The supported actions.
	 */
	public String[] getSupported() {
		return this.supported;
	}
}
