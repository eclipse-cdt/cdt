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
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.macros.IOptionContextData;

/**
 * This is a trivial implementation of the IOptionContextData used internaly by the MBS
 * 
 * @since 3.0
 */
public class OptionContextData implements IOptionContextData {
	private IOption fOption;
	private IBuildObject fParent;

	public OptionContextData(IOption option, IBuildObject parent){
		fOption = option;
		fParent = parent;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IOptionContextData#getOption()
	 */
	public IOption getOption() {
		return fOption;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IOptionContextData#getParent()
	 */
	public IBuildObject getParent() {
		return fParent;
	}

	public static IHoldsOptions getHolder(IOptionContextData data){
		IBuildObject buildObj = data.getParent();
		IToolChain tCh = null;
		IHoldsOptions ho = null;
		IResourceConfiguration rcCfg = null;
		if(buildObj instanceof ITool)
			ho = (ITool)buildObj;
		else if(buildObj instanceof IToolChain)
			tCh = (IToolChain)buildObj;
		else if(buildObj instanceof IResourceConfiguration)
			rcCfg = (IResourceConfiguration)buildObj;
		else if(buildObj instanceof IConfiguration)
			tCh = ((IConfiguration)buildObj).getToolChain();

		if(ho == null){
			IOption option = data.getOption();
			if(option == null)
				return null;

			ho = option.getOptionHolder();
			ITool tools[] = null;
			if(tCh != null){
				for(IToolChain cur = tCh; cur != null; cur = cur.getSuperClass()){
					if(cur == ho)
						return tCh;
				}
				tools = tCh.getTools();
			} else if(rcCfg != null){
				tools = rcCfg.getTools();
			}
			
			if(tools != null){
				for(int i = 0; i < tools.length; i++){
					for(ITool cur = tools[i]; cur != null; cur = cur.getSuperClass()){
						if(cur == ho)
							return tools[i];
					}
				}
			}
		}
		return ho;
	}
}
