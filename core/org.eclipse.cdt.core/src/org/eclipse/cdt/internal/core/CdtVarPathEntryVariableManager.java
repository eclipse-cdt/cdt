/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.resources.IPathEntryVariableChangeListener;
import org.eclipse.cdt.core.resources.IPathEntryVariableManager;
import org.eclipse.cdt.core.resources.PathEntryVariableChangeEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.cdtvariables.ICdtVariableChangeListener;
import org.eclipse.cdt.internal.core.cdtvariables.ICoreVariableContextInfo;
import org.eclipse.cdt.internal.core.cdtvariables.UserDefinedVariableSupplier;
import org.eclipse.cdt.internal.core.cdtvariables.VariableChangeEvent;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;

/**
 * the path entry variable manager is kept for the backward compatibility purposes
 * currently it presents workspace Cdt variables that hold either a file or folder value
 *
 */
public class CdtVarPathEntryVariableManager implements
		IPathEntryVariableManager,  ICdtVariableChangeListener {
	private UserDefinedVariableSupplier fUserVarSupplier = UserDefinedVariableSupplier.getInstance();
	private VarSubstitutor fSubstitutor = new VarSubstitutor();
	private VarSupplier fVarSupplier = new VarSupplier();
	private Set<IPathEntryVariableChangeListener> fListeners;

	private class VarSubstitutor extends SupplierBasedCdtVariableSubstitutor {
		public VarSubstitutor() {
			super(new VarContextInfo(), "", " "); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private class VarContextInfo implements IVariableContextInfo {

		@Override
		public IVariableContextInfo getNext() {
			return null;
		}

		@Override
		public ICdtVariableSupplier[] getSuppliers() {
			return new ICdtVariableSupplier[]{fVarSupplier};
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;

			if(!(obj instanceof VarContextInfo))
				return false;

			return true;
		}

	}

	private class VarSupplier implements ICdtVariableSupplier {

		@Override
		public ICdtVariable getVariable(String macroName,
				IVariableContextInfo context) {
			ICdtVariable var = fUserVarSupplier.getMacro(macroName, ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
			if(var != null && getVariablePath(var) != null)
				return var;
			return null;
		}

		@Override
		public ICdtVariable[] getVariables(IVariableContextInfo context) {
			ICdtVariable vars[] = fUserVarSupplier.getMacros(ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
			List<ICdtVariable> list = new ArrayList<ICdtVariable>();
			for (ICdtVariable var : vars) {
				if(getVariablePath(var) != null)
					list.add(var);
			}
			return list.toArray(new ICdtVariable[list.size()]);
		}

	}



	public CdtVarPathEntryVariableManager(){
		fListeners = Collections.synchronizedSet(new HashSet<IPathEntryVariableChangeListener>());
		fUserVarSupplier.addListener(this);
	}

	@Override
	public IPath getValue(String name) {
		ICdtVariable var = fUserVarSupplier.getMacro(name, ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
		return getVariablePath(var);
	}

	public static IPath getVariablePath(ICdtVariable var){
		if(var != null){
			switch(var.getValueType()){
			case ICdtVariable.VALUE_PATH_ANY:
			case ICdtVariable.VALUE_PATH_DIR:
			case ICdtVariable.VALUE_PATH_FILE:
				try {
						String value = var.getStringValue();
						if(value != null)
							return new Path(value);
						return Path.EMPTY;
					} catch (CdtVariableException e) {
						CCorePlugin.log(e);
					}
			}
		}
		return null;
	}

	public static boolean isPathEntryVariable(ICdtVariable var, ICConfigurationDescription cfg){
		return isPathEntryVariable(var, cfg, CCorePlugin.getDefault().getCdtVariableManager());
	}

	public static boolean isPathEntryVariable(ICdtVariable var, ICConfigurationDescription cfg, ICdtVariableManager mngr){
		if(mngr.isUserVariable(var, cfg))
			return false;

		if(!mngr.isUserVariable(var, null))
			return false;

		if(getVariablePath(var) == null)
			return false;

		return true;
	}

	@Override
	public String[] getVariableNames() {
		ICdtVariable[] vars = fUserVarSupplier.getMacros(ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i > vars.length; i++){
			if(getVariablePath(vars[i]) != null)
				list.add(vars[i].getName());
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public boolean isDefined(String name) {
		return getValue(name) != null;
	}

	@Override
	public IPath resolvePath(IPath path) {
		if(path == null)
			return null;

		String str = path.toPortableString();

		try {
			str = CdtVariableResolver.resolveToString(str, fSubstitutor);
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}

		return new Path(str);
	}

	@Override
	public void setValue(String name, IPath value) throws CoreException {
		if(value != null)
			fUserVarSupplier.createMacro(name, ICdtVariable.VALUE_PATH_ANY, value.toString(), ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
		else
			fUserVarSupplier.deleteMacro(name, ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);

		fUserVarSupplier.storeWorkspaceVariables(false);
	}

	/**
	 * Fires a property change event corresponding to a change to the
	 * current value of the variable with the given name.
	 *
	 * @param name the name of the variable, to be used as the variable
	 *      in the event object
	 * @param value the current value of the path variable or <code>null</code> if
	 *      the variable was deleted
	 * @param type one of <code>IPathVariableChangeEvent.VARIABLE_CREATED</code>,
	 *      <code>PathEntryVariableChangeEvent.VARIABLE_CHANGED</code>, or
	 *      <code>PathEntryVariableChangeEvent.VARIABLE_DELETED</code>
	 * @see PathEntryVariableChangeEvent
	 * @see PathEntryVariableChangeEvent#VARIABLE_CREATED
	 * @see PathEntryVariableChangeEvent#VARIABLE_CHANGED
	 * @see PathEntryVariableChangeEvent#VARIABLE_DELETED
	 */
	private void fireVariableChangeEvent(String name, IPath value, int type) {
		if (this.fListeners.size() == 0)
			return;
		// use a separate collection to avoid interference of simultaneous additions/removals
		Object[] listenerArray = this.fListeners.toArray();
		final PathEntryVariableChangeEvent pve = new PathEntryVariableChangeEvent(this, name, value, type);
		for (int i = 0; i < listenerArray.length; ++i) {
			final IPathEntryVariableChangeListener l = (IPathEntryVariableChangeListener) listenerArray[i];
			ISafeRunnable job = new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					// already being logged in Platform#run()
				}

				@Override
				public void run() throws Exception {
					l.pathVariableChanged(pve);
				}
			};
			SafeRunner.run(job);
		}
	}

	/**
	 * @see org.eclipse.cdt.core.resources.IPathEntryVariableManager#addChangeListener(IPathEntryVariableChangeListener)
	 */
	@Override
	public void addChangeListener(IPathEntryVariableChangeListener listener) {
		fListeners.add(listener);
	}

	/**
	 * @see org.eclipse.cdt.core.resources.
	 * IPathEntryVariableManager#removeChangeListener(IPathEntryVariableChangeListener)
	 */
	@Override
	public void removeChangeListener(IPathEntryVariableChangeListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public void variablesChanged(VariableChangeEvent event) {
		ICdtVariable[] added = event.getAddedVariables();
		ICdtVariable[] removed = event.getRemovedVariables();
		ICdtVariable[] changed = event.getChangedVariables();

		if(added.length != 0){
			fireEvent(added, PathEntryVariableChangeEvent.VARIABLE_CREATED);
		}

		if(removed.length != 0){
			fireEvent(removed, PathEntryVariableChangeEvent.VARIABLE_DELETED);
		}

		if(changed.length != 0){
			fireEvent(changed, PathEntryVariableChangeEvent.VARIABLE_CHANGED);
		}
	}

	private void fireEvent(ICdtVariable vars[], int type){
		for (ICdtVariable var : vars) {
			IPath path = getVariablePath(var);
			if(path != null)
				fireVariableChangeEvent(var.getName(), path, type);
		}

	}

	public void startup(){
	}

	public void shutdown(){
	}

}
