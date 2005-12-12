/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.internal.enablement.OptionEnablementExpression;

public class BooleanExpressionApplicabilityCalculator implements IOptionApplicability {
	private OptionEnablementExpression fExpressions[];

	public BooleanExpressionApplicabilityCalculator(IManagedConfigElement optionElement){
		this(optionElement.getChildren(OptionEnablementExpression.NAME));
	}

	public BooleanExpressionApplicabilityCalculator(IManagedConfigElement enablementElements[]){
		fExpressions = new OptionEnablementExpression[enablementElements.length];
		
		for(int i = 0; i < enablementElements.length; i++){
			fExpressions[i] = new OptionEnablementExpression(enablementElements[i]);
		}
	}
	
	public boolean isOptionVisible(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option){
		return evaluate(configuration, holder, option, OptionEnablementExpression.FLAG_UI_VISIBILITY);
	}

	public boolean isOptionEnabled(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option){
		return evaluate(configuration, holder, option, OptionEnablementExpression.FLAG_UI_ENABLEMENT);
	}

	public boolean isOptionUsedInCommandLine(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option){
		return evaluate(configuration, holder, option, OptionEnablementExpression.FLAG_CMD_USAGE);
	}

	public boolean evaluate(IBuildObject configuration,
			IHoldsOptions holder, IOption option, int flags){
		for(int i = 0; i < fExpressions.length; i++){
			if(!fExpressions[i].evaluate(configuration, holder, option, flags))
				return false;
		}
		return true;
	}
	
	public boolean performAdjustment(IBuildObject configuration,
			IHoldsOptions holder, IOption option){
		boolean adjusted = false;
		for(int i = 0; i < fExpressions.length; i++){
			if(fExpressions[i].performAdjustment(configuration, holder, option))
				adjusted = true;
		}
		return adjusted;
	}
}
