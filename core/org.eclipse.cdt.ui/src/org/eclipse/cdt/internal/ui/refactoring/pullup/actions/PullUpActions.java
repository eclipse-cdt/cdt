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
 * Factory class for creating {@link MoveAction} instances which perform tasks of pulling
 * up members.
 * 
 * @author Simon Taddiken
 */
public final class PullUpActions  {
	
	/**
	 * Provides {@link MoveAction} implementations for the 'pull up' refactoring. The 
	 * following {@link TargetActions} are supported:
	 * <ul>
	 *   <li><b>TargetActions.PULL_UP</b> - The method's declaration and definition will be 
	 *     removed in the source class and declared in the target class</li>
	 *   <li><b>TargetActions.DECLARE_VIRTUAL</b> - The method will be declared as pure 
	 *     abstract method in the target class and its declaration is removed in the source 
	 *     class. Its definitions remains in the source</li>
	 *   <li><b>TargetActions.REMOVE</b> - A member is completely removed from its
	 *   	current source class.</li>
	 *   <li><b>TargetActions.METHOD_STUB</b> - A definition with empty body is inserted in 
	 *     the target class. Everything else remains unchanged.</li>
	 * </ul>
	 * The last case is kind of a HACK as it does not move a member, but abuses this class'
	 * infrastructure to create the empty method stub.
	 * 
	 * <p>Any different target action passed to this method will raise an exception.</p>
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
		
		if (targetAction == TargetActions.PULL_UP) {
			return new MoveMethodAction(group, context, method, targetClass, 
					targetVisibility);
		} else if (targetAction == TargetActions.DECLARE_VIRTUAL) {
			return new DeclareVirtualAction(group, context, method, targetClass, 
					targetVisibility);
		} else if (targetAction == TargetActions.METHOD_STUB) {
			return new DeclareStubAction(group, context, method, targetClass, 
					targetVisibility);
		} else if (targetAction == TargetActions.REMOVE_METHOD) {
			return new RemoveFromSourceAction(group, context, method, targetClass, 
					targetVisibility);
		}
		throw new IllegalArgumentException("invalid action: '" + targetAction + "'");  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	
	
	private PullUpActions() {}
}