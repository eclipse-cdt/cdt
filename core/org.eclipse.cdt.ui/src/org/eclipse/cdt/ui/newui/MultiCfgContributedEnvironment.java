/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Comparator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.MultiItemsHolder;

import org.eclipse.cdt.internal.core.envvar.ContributedEnvironment;

/**
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MultiCfgContributedEnvironment implements IContributedEnvironment {
	private static final IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment();
	private boolean isMulti = false;
	private ICConfigurationDescription[] mono = new ICConfigurationDescription[1];
	private static final EnvCmp comparator = new EnvCmp();

	private static class EnvCmp implements Comparator<Object> {

		@Override
		public int compare(Object a0, Object a1) {
			if (a0 == null || a1 == null)
				return 0;
			if (a0 instanceof IEnvironmentVariable &&
				a1 instanceof IEnvironmentVariable) {
				IEnvironmentVariable x0 = (IEnvironmentVariable)a0;
				IEnvironmentVariable x1 = (IEnvironmentVariable)a1;
				String s0 = x0.getName();
				if (s0 == null)
					s0 = AbstractPage.EMPTY_STR;
				String s1 = x1.getName();
				if (s1 == null)
					s1 = AbstractPage.EMPTY_STR;
				return(s0.compareTo(s1));
			}
			return 0;
		}
	}

	public void setMulti(boolean val) {
		isMulti = val;
	}

	@Override
	public IEnvironmentVariable addVariable(String name, String value,
			int op, String delimiter, ICConfigurationDescription des) {
		IEnvironmentVariable v = null;
		for (ICConfigurationDescription c : getCfs(des))
			v = ice.addVariable(name, value, op, delimiter, c);
		doReplace(des);
		return v;
	}

	private void doReplace(ICConfigurationDescription des) {
		if (isMulti && ! isModifyMode()) {
			IEnvironmentVariable[] vars = getVariables(des);
			for (int i=0; i<vars.length; i++)
				if (! ice.isUserVariable(des, vars[i]))
					vars[i] = null;
			for (ICConfigurationDescription c : getCfs(des)) {
				ice.restoreDefaults(c);
				for (IEnvironmentVariable v : vars)
					if (v != null)
						ice.addVariable(v, c);
			}
		}
	}

	@Override
	public boolean appendEnvironment(ICConfigurationDescription des) {
		for (ICConfigurationDescription c : getCfs(des))
			if (! ice.appendEnvironment(c))
				return false;
		return true;
	}

	@Override
	public IEnvironmentVariable getVariable(String name,
			ICConfigurationDescription des) {
		if (!isMulti)
			return ice.getVariable(name, des);
		// should we show ANY vars, even if they exist not in all cfgs ?
		boolean any = (getDispMode(des) == CDTPrefUtil.DMODE_DISJUNCTION);
		ICConfigurationDescription[] cfs = getCfs(des);
		IEnvironmentVariable v = ice.getVariable(name, cfs[0]);
		// if ((any && v != null) || (! any && v == null))
		if (any ^ (v == null))
			return v;
		for (int i=1; i<cfs.length; i++) {
			IEnvironmentVariable w = ice.getVariable(name, cfs[i]);
			if (any && (w != null))
				return w;
			// if (! any  &&  ! v==w)
			if (! (any || (v==null && w==null) || (v != null && v.equals(w))))
				return null;
		}
		return v;
	}

	@Override
	public IEnvironmentVariable[] getVariables(
			ICConfigurationDescription des) {
		if (!isMulti)
			return ice.getVariables(des);
		ICConfigurationDescription[] cfs = getCfs(des);
		IEnvironmentVariable[][] evs = new IEnvironmentVariable[cfs.length][];
		int i = 0;
		for (ICConfigurationDescription c : cfs)
			 evs[i++] = ice.getVariables(c);

		Object[] obs = CDTPrefUtil.getListForDisplay(evs, comparator);
		IEnvironmentVariable[] ev = new IEnvironmentVariable[obs.length];
		System.arraycopy(obs, 0, ev, 0, obs.length);
		return ev;

	}

	@Override
	public boolean isUserVariable(ICConfigurationDescription des,
			IEnvironmentVariable var) {
		for (ICConfigurationDescription c : getCfs(des))
			if (! ice.isUserVariable(c, var))
				return false;
		return true; // only if for each cfg
	}

	@Override
	public IEnvironmentVariable removeVariable(String name,
			ICConfigurationDescription des) {
		IEnvironmentVariable res = null;
		for (ICConfigurationDescription c : getCfs(des))
			res = ice.removeVariable(name, c);
		doReplace(des);
		return res;
	}

	@Override
	public void restoreDefaults(ICConfigurationDescription des) {
		for (ICConfigurationDescription c : getCfs(des))
			ice.restoreDefaults(c);
	}

	@Override
	public void setAppendEnvironment(boolean append,ICConfigurationDescription des) {
		for (ICConfigurationDescription c : getCfs(des))
			ice.setAppendEnvironment(append, c);
	}

	private ICConfigurationDescription[] getCfs(ICConfigurationDescription des) {
		if (isMulti && des instanceof ICMultiConfigDescription) {
			return (ICConfigurationDescription[])((ICMultiConfigDescription)des).getItems();
		}
		mono[0] = des;
		return mono;
	}

	private int getDispMode(ICConfigurationDescription des) {
		if (isMulti && des instanceof MultiItemsHolder)
			return CDTPrefUtil.getMultiCfgStringListDisplayMode();
		return 0;
	}

	private boolean isModifyMode() {
		int wmode = CDTPrefUtil.getMultiCfgStringListWriteMode();
		return (wmode == CDTPrefUtil.WMODE_MODIFY);
	}

	@Override
	public IEnvironmentVariable addVariable(IEnvironmentVariable var,
			ICConfigurationDescription des) {
		IEnvironmentVariable v = null;
		for (ICConfigurationDescription c : getCfs(des))
			v = ice.addVariable(var, c);
		doReplace(des);
		return v;
	}

	@Override
	public void addVariables(IEnvironmentVariable[] vars,
			ICConfigurationDescription des) {
		for (ICConfigurationDescription c : getCfs(des))
			ice.addVariables(vars, c);
		doReplace(des);
	}

	public String getOrigin(IEnvironmentVariable var) {
		if (ice instanceof ContributedEnvironment)
			return ((ContributedEnvironment)ice).getOrigin(var);
		return AbstractPage.EMPTY_STR;
	}
}
