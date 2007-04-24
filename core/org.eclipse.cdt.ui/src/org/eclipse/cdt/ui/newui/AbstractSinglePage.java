/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;

/**
 * Bug #183341 : Single property page which does not  
 * require separate cPropertyTab to display data.
 *
 */
public abstract class AbstractSinglePage extends AbstractPage {

	/**
	 * Implement this method to create your own widgets
	 */
	public abstract void createWidgets(Composite c);
	
	/**
	 * Implement this method to perform apply:
	 * copy all data affected by this page 
	 * from src resource description to dst
	 * @param src
	 * @param dst
	 */
	protected abstract void performApply(ICResourceDescription src, ICResourceDescription dst);

    /**
     * Rewrite this method to handle configuration change
     * Do not forget to call super.cfgChanged(_cfgd); 
     */
	protected void cfgChanged(ICConfigurationDescription _cfgd) {
		super.cfgChanged(_cfgd);
	//	if (displayedConfig) {
			// update widgets according to getResDesc() values   
	//	}
	}

	/**
	 * Usually, this method needs not to be rewritten
	 */
    public boolean performCancel() {
	//	if (! noContentOnPage && displayedConfig) {
			// do nothing in most cases
	//	}
        return true;
    }
    
    /**
     * Rewrite this method to restore default 
     * values in current ResourceDescription
     */
	public void performDefaults() {
	//	if (! noContentOnPage && displayedConfig) {
			// do something with getResDesc() fields
	//	}
	}
    
	/**
	 * Usually, this method needs not to be rewritten
	 */
    public boolean performOk() {
		if (! noContentOnPage && displayedConfig) {
			// do nothing in most cases
		}
    	return super.performOk();
    }

	/**
	 * 
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			cfgChanged(getResDesc().getConfiguration());
		}
	}

	/**
	 * No need to rewrite
	 */
	protected boolean isSingle() { return true; }
	
	/**
	 * Call to "foreach" does not really matter, since we have not tabs
	 * But we intercept this call to perform other operations (apply). 
	 */
	protected void forEach(int m, Object data) {
		if (m == ICPropertyTab.APPLY)
			performApply(getResDesc(), (ICResourceDescription)data);
	}

}
