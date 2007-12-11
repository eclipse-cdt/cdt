/**	
 * 
 */
package org.eclipse.cdt.ui.newui;

import java.util.Comparator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.core.settings.model.MultiItemsHolder;

/**
 * 
 *
 */
public class MultiCfgContributedEnvironment implements IContributedEnvironment {
	private static final IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment();
	private boolean isMulti = false;
	private ICConfigurationDescription[] mono = new ICConfigurationDescription[1];
	private ICConfigurationDescription[] cfs;
	private static final EnvCmp comparator = new EnvCmp(); 
	
	private static class EnvCmp implements Comparator {
		
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
			} else 
				return 0;
		}
	}
	
	public void setMulti(boolean val) {
		isMulti = val;
	}
	
	public IEnvironmentVariable addVariable(String name, String value,
			int op, String delimiter, ICConfigurationDescription des) {
		cfs = getCfs(des);
		IEnvironmentVariable v = null;
		for (int i=0; i<cfs.length; i++)
			v = ice.addVariable(name, value, op, delimiter, cfs[i]);
		return v;
	}

	public boolean appendEnvironment(ICConfigurationDescription des) {
		cfs = getCfs(des);
		for (int i=0; i<cfs.length; i++)
			if (! ice.appendEnvironment(cfs[i]))
				return false;
		return true;
	}

	public IEnvironmentVariable getVariable(String name,
			ICConfigurationDescription des) {
		if (!isMulti)
			return ice.getVariable(name, des);
		// should we show ANY vars, even if they exist not in all cfgs ? 
		boolean any = (getDispMode(des) == ICMultiItemsHolder.DMODE_ALL);	
		cfs = getCfs(des);
		IEnvironmentVariable v = ice.getVariable(name, cfs[0]);
		// if ((any && v != null) || (! any && v == null))
		if (any ^ (v == null))
			return v;
		for (int i=1; i<cfs.length; i++) {
			IEnvironmentVariable w = ice.getVariable(name, cfs[i]);
			if (any && (w != null))
				return w;
			// if (! any  &&  ! v==w)
			if (! (any || v.equals(w)))
				return null;
		}
		return v;
	}

	public IEnvironmentVariable[] getVariables(
			ICConfigurationDescription des) {
		if (!isMulti)
			return ice.getVariables(des);
		cfs = getCfs(des);
		IEnvironmentVariable[][] evs = new IEnvironmentVariable[cfs.length][];
		for (int i=0; i<cfs.length; i++)
			 evs[i] = ice.getVariables(cfs[i]);
		
		Object[] obs = ((ICMultiItemsHolder)des).getListForDisplay(evs, comparator); 
		IEnvironmentVariable[] ev = new IEnvironmentVariable[obs.length];
		System.arraycopy(obs, 0, ev, 0, obs.length);
		return ev;
		           
	}

	public boolean isUserVariable(ICConfigurationDescription des,
			IEnvironmentVariable var) {
		cfs = getCfs(des);
		for (int i=0; i<cfs.length; i++)
			if (! ice.isUserVariable(cfs[i], var))
				return false;
		return true; // only if for each cfg
	}

	public IEnvironmentVariable removeVariable(String name,
			ICConfigurationDescription des) {
		IEnvironmentVariable res = null;
		cfs = getCfs(des);
		for (int i=0; i<cfs.length; i++)
			res = ice.removeVariable(name, cfs[i]);
		return res;
	}

	public void restoreDefaults(ICConfigurationDescription des) {
		cfs = getCfs(des);
		for (int i=0; i<cfs.length; i++)
			ice.restoreDefaults(cfs[i]);
	}

	public void setAppendEnvironment(boolean append,ICConfigurationDescription des) {
		cfs = getCfs(des);
		for (int i=0; i<cfs.length; i++)
			ice.setAppendEnvironment(append, cfs[i]);
	}
	
	private ICConfigurationDescription[] getCfs(ICConfigurationDescription des) {
		if (isMulti && des instanceof ICMultiConfigDescription) {
			return (ICConfigurationDescription[])((ICMultiConfigDescription)des).getItems();
		} else {
			mono[0] = des;
			return mono;
		}
	}
	
	private int getDispMode(ICConfigurationDescription des) {
		if (isMulti && des instanceof MultiItemsHolder)
			return ((MultiItemsHolder)des).getStringListMode() & ICMultiItemsHolder.DMODES;
		return 0;	
	}
	
}
