/*******************************************************************************
 * Copyright (c) 2005, 2012 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.IStorableCdtVariables;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;

/**
 * This class represents the set of Build Macros that could be loaded
 * and stored in XML
 *
 * @since 3.0
 *
 */
public class StorableCdtVariables implements IStorableCdtVariables {
	public static final String MACROS_ELEMENT_NAME = "macros"; //$NON-NLS-1$
	private final HashMap<String, ICdtVariable> fMacros = new HashMap<>();
	private boolean fIsDirty = false;
	private boolean fIsChanged = false;
	private boolean fIsReadOnly;

	private HashMap<String, ICdtVariable> getMap() {
		return fMacros;
	}

	public StorableCdtVariables(boolean readOnly) {
		fIsReadOnly = readOnly;
	}

	public StorableCdtVariables(StorableCdtVariables base, boolean readOnly) {
		fMacros.putAll(base.getMap());
		fIsReadOnly = readOnly;
	}

	public StorableCdtVariables(ICdtVariable vars[], boolean readOnly) {
		for (ICdtVariable var : vars) {
			addMacro(var);
		}
		fIsReadOnly = readOnly;
	}

	public StorableCdtVariables(ICStorageElement element, boolean readOnly) {
		load(element);
		fIsReadOnly = readOnly;
	}

	private void load(ICStorageElement element) {
		//		fExpandInMakefile = TRUE.equals(element.getAttribute(EXPAND_ENVIRONMENT_MACROS));

		ICStorageElement nodeList[] = element.getChildren();
		for (int i = 0; i < nodeList.length; ++i) {
			ICStorageElement node = nodeList[i];
			String name = node.getName();
			if (StorableCdtVariable.STRING_MACRO_ELEMENT_NAME.equals(name)) {
				addMacro(new StorableCdtVariable(node));
			} else if (StorableCdtVariable.STRINGLIST_MACRO_ELEMENT_NAME.equals(name)) {
				addMacro(new StorableCdtVariable(node));
			}
		}
		fIsDirty = false;
		fIsChanged = false;
	}

	public void serialize(ICStorageElement element) {
		for (ICdtVariable v : fMacros.values()) {
			StorableCdtVariable macro = (StorableCdtVariable) v;
			ICStorageElement macroEl;
			if (CdtVariableResolver.isStringListVariable(macro.getValueType()))
				macroEl = element.createChild(StorableCdtVariable.STRINGLIST_MACRO_ELEMENT_NAME);
			else
				macroEl = element.createChild(StorableCdtVariable.STRING_MACRO_ELEMENT_NAME);
			macro.serialize(macroEl);
		}

		fIsDirty = false;
	}

	private void addMacro(ICdtVariable macro) {
		String name = macro.getName();
		if (name == null)
			return;

		getMap().put(name, macro);
	}

	@Override
	public ICdtVariable createMacro(String name, int type, String value) {
		if (name == null || "".equals(name = name.trim()) || CdtVariableResolver.isStringListVariable(type)) //$NON-NLS-1$
			return null;
		return createMacro(new StorableCdtVariable(name, type, value));
	}

	/**
	 * sets the storable macros to hold the geven number of macros
	 * all macros that are present in the store but not included in the given array
	 * will be removed
	 */
	public void setMacros(ICdtVariable macros[]) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		deleteAll();
		if (macros != null) {
			createMacros(macros);
		}
	}

	@Override
	public void createMacros(ICdtVariable macros[]) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		for (ICdtVariable macro : macros) {
			createMacro(macro);
		}
	}

	@Override
	public boolean isEmpty() {
		return fMacros.isEmpty();
	}

	@Override
	public ICdtVariable createMacro(ICdtVariable copy) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		String name = copy.getName();
		if (name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		Optional<ICdtVariable> entry = getEntry(copy);
		if (entry.isPresent()) {
			return entry.get();
		} else {
			int type = copy.getValueType();
			StorableCdtVariable macro;
			try {
				if (CdtVariableResolver.isStringListVariable(type)) {
					macro = new StorableCdtVariable(name, type, copy.getStringListValue());
				} else {
					macro = new StorableCdtVariable(name, type, copy.getStringValue());
				}
				addMacro(macro);
				fIsDirty = true;
				fIsChanged = true;
				return macro;
			} catch (CdtVariableException e) {
				CCorePlugin.log(e);
				// Unreachable in practice, it is a programming error, there
				// probably should have been an assert in getStringListValue/getStringValue
				// instead of a throw.
				// There are no bugzillas ever for a CdtVariableException.
				return null;
			}
		}
	}

	@Override
	public ICdtVariable createMacro(String name, int type, String value[]) {
		if (name == null || "".equals(name = name.trim()) || !CdtVariableResolver.isStringListVariable(type)) //$NON-NLS-1$
			return null;
		return createMacro(new StorableCdtVariable(name, type, value));
	}

	/**
	 * Returns the "dirty" state for this set of macros.
	 * If the dirty state is <code>true</code>, that means that the macros
	 * is out of synch with the repository and the macros need to be serialized.
	 * <br><br>
	 * The dirty state is automatically set to <code>false</code> when the macros are serialized
	 * by calling the serialize() method
	 * @return boolean
	 */
	public boolean isDirty() {
		return fIsDirty;
	}

	/**
	 * sets the "dirty" state for this set of macros.
	 * @see org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables#isDirty()
	 * @param dirty represents the new state
	 */
	public void setDirty(boolean dirty) {
		fIsDirty = dirty;
	}

	/**
	 * Returns the "change" state for this set of macros.
	 * The "change" state represents whether the macros were changed or not.
	 * This state is not reset when the serialize() method is called
	 * Users can use this state to monitor whether the macros were changed or not.
	 * The "change" state can be reset only by calling the setChanged(false) method
	 * @return boolean
	 */
	@Override
	public boolean isChanged() {
		return fIsChanged;
	}

	/*	public boolean isExpanded(){
			return fExpandInMakefile;
		}
	*/
	/*	public void setExpanded(boolean expand){
			if(fExpandInMakefile != expand){
				fExpandInMakefile = expand;
				fIsDirty = true;
				//should we set the change state here?
				fIsChanged = true;
			}
		}
	*/
	/**
	 * sets the "change" state for this set of macros.
	 * @see org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables#isChanged()
	 * @param changed represents the new "change" state
	 */
	public void setChanged(boolean changed) {
		fIsChanged = changed;
	}

	@Override
	public ICdtVariable getMacro(String name) {
		if (name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		ICdtVariable var = getMap().get(name);
		if (var == null) {
			int indx = name.indexOf(':');
			if (indx != -1) {
				String baseName = name.substring(0, indx);
				ICdtVariable tmp = getMap().get(baseName);
				if (tmp != null && CdtVariableManager.getDefault().toEclipseVariable(tmp, null) != null) {
					var = EclipseVariablesVariableSupplier.getInstance().getVariable(name);
				}
			}
		}
		return var;
	}

	@Override
	public ICdtVariable[] getMacros() {
		Collection<ICdtVariable> macros = getMap().values();
		return macros.toArray(new ICdtVariable[macros.size()]);
	}

	@Override
	public ICdtVariable deleteMacro(String name) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		if (name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		ICdtVariable macro = getMap().remove(name);
		if (macro != null) {
			fIsDirty = true;
			fIsChanged = true;
		}

		return macro;
	}

	@Override
	public boolean deleteAll() {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		Map<String, ICdtVariable> map = getMap();
		if (map.size() > 0) {
			fIsDirty = true;
			fIsChanged = true;
			map.clear();
			return true;
		}
		return false;
	}

	/**
	 * ICdtVariable does not have {@link #equals(Object)} implemented,
	 * so this method does the equals.
	 *
	 * XXX: It would be nice if ICdtVariable provided equals, but the
	 * consequences of this change > 10 years after originally written
	 * are unknown, so this helper is used instead.
	 */
	public static boolean equals(ICdtVariable o1, ICdtVariable o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		if (!CDataUtil.objectsEqual(o1.getName(), o2.getName()))
			return false;

		if (o1.getValueType() != o2.getValueType())
			return false;
		try {

			if (CdtVariableResolver.isStringListVariable(o1.getValueType())) {
				if (!Arrays.equals(o1.getStringListValue(), o2.getStringListValue()))
					return false;

			} else {
				if (!CDataUtil.objectsEqual(o1.getStringValue(), o2.getStringValue()))
					return false;
			}
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
			// Unreachable in practice, it is a programming error, there
			// probably should have been an assert in getStringListValue/getStringValue
			// instead of a throw.
			// There are no bugzillas ever for a CdtVariableException.
			return true;
		}
		return true;
	}

	private Optional<ICdtVariable> getEntry(ICdtVariable var) {
		ICdtVariable curVar = getMacro(var.getName());
		if (equals(var, curVar)) {
			return Optional.of(curVar);
		}
		return Optional.empty();
	}

	@Override
	public boolean contains(ICdtVariable var) {
		return getEntry(var).isPresent();
	}
}
