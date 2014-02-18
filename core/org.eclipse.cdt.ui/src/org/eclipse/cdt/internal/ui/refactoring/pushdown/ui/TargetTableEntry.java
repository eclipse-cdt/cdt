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
package org.eclipse.cdt.internal.ui.refactoring.pushdown.ui;

import java.beans.PropertyChangeSupport;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.HasActions;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;

public class TargetTableEntry implements HasActions {
	
	/** Key for {@link PropertyChangeSupport} */
	public final static String TARGET_ACTION = "targetAction"; //$NON-NLS-1$
	
	/** Actions that can be performed per class per member */
    public final static TargetActions PER_CLASS_TARGET_ACTIONS = new TargetActions(
    		TargetActions.NONE,
    		TargetActions.EXISTING_DEFINITION, 
    		TargetActions.METHOD_STUB);
    
	/** Actions that can be performed per class per member */
    public final static TargetActions MANDATORY_PER_CLASS_TARGET_ACTIONS = new TargetActions(
    		TargetActions.EXISTING_DEFINITION, 
    		TargetActions.METHOD_STUB);
	
    
	protected final InheritanceLevel parent;
	protected final PushDownMemberTableEntry mte;
	protected PropertyChangeSupport pcSupport;
	protected TargetActions supportedActions;
	private String selectedAction;
	
	
	public TargetTableEntry(InheritanceLevel parent, PushDownMemberTableEntry mte, 
			TargetActions supportedAction, String defaultAction) {
		this.supportedActions = supportedAction;
		this.parent = parent;
		this.mte = mte;
		this.pcSupport = new PropertyChangeSupport(this);
		this.selectedAction = defaultAction;
	}
	
	
	
	public PushDownMemberTableEntry getMember() {
		return this.mte;
	}
	
	
	
	public InheritanceLevel getParent() {
		return this.parent;
	}
	
	
	@Override
	public String getSelectedAction() {
		return this.selectedAction;
	}
	
	
	
	public void setSupported(TargetActions supported) {
		this.supportedActions = supported;
	}
	
	
	
	@Override
	public void setSelectedAction(String selectedAction) {
		this.pcSupport.firePropertyChange(TARGET_ACTION, this.selectedAction, 
				this.selectedAction = selectedAction);
	}



	@Override
	public TargetActions getActions() {
		return this.supportedActions;
	}
}
