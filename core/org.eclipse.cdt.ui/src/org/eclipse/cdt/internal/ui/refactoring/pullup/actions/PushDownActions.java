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
package org.eclipse.cdt.internal.ui.refactoring.pullup.actions;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;

/**
 * Factory class for creating {@link MoveAction} instances which perform tasks of pushing
 * down members.
 * 
 * @author Simon Taddiken
 */
public final class PushDownActions {

	/**
	 * Provides {@link MoveAction} implementations for the 'push down' refactoring. The 
	 * following {@link TargetActions} are supported:
	 * <ul>
	 *   <li><b>TargetActions.PUSH_DOWN</b> - The method's declaration and definition will be 
	 *     removed in the source class. No changes within the target class are performed</li>
	 *   <li><b>TargetActions.LEAVE_VIRTUAL</b> - Removes any existing declaration/definition
	 *     from the source class and creates a new pure virtual declaration within the
	 *     source class. No changes within the target class are performed.</li>
	 *   <li><b>TargetActions.EXISTING_DEFINITION</b> - Inserts the existing definition
	 *     of the provided member in the provided target class. No changes within the
	 *     source class are performed</li>
	 *   <li><b>TargetActions.METHOD_STUB</b> - Inserts a method stub of the provided member 
	 *     in the provided target class. No changes within the source class are performed</li>     
	 * </ul>
	 * 
	 * Any different target action passed to this method will raise an exception.
	 * 
	 * @param targetAction The target action to perform
	 * @param context Current refactoring context.
	 * @param group Current move action group
	 * @param method Method binding which is subject to the generated action.
	 * @param targetClass The class to which the method is moved.
	 * @param targetVisibility The visibility within the target.
	 * @return A new MoveAction instance.
	 */
	public static MoveAction forAction(String targetAction, 
			CRefactoringContext context, MoveActionGroup group, ICPPMethod method, 
			ICPPClassType targetClass, int targetVisibility) {
		
		if (targetAction == TargetActions.LEAVE_VIRTUAL) {
			return new LeaveVirtualAction(group, context, method, targetClass, 
					targetVisibility);
		} else if (targetAction == TargetActions.PUSH_DOWN) {
			return new RemoveFromSourceAction(group, context, method, targetClass, 
					targetVisibility);
		} else if (targetAction == TargetActions.EXISTING_DEFINITION) {
			return new PushExistingToAction(group, context, method, targetClass, 
					targetVisibility);
		} else if (targetAction == TargetActions.METHOD_STUB) {
			return new DeclareStubAction(group, context, method, targetClass, 
					targetVisibility);
		}
		throw new IllegalArgumentException("invalid action: '" + targetAction + "'");  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	

	private PushDownActions() {}
}
