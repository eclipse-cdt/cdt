package org.eclipse.cdt.managedbuilder.core;

/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     TimeSys Corporation - initial API and implementation
 **********************************************************************/

public abstract class AbstractToolReference implements IToolReference {

	protected ITool parent;
	
	public AbstractToolReference() {
	}
	
	public AbstractToolReference(ITool parent) {
		this.parent = parent;
	}
	
	public boolean references(ITool target) {
		if (equals(target)) {
			// we are the target
			return true;
		}
		else if (parent instanceof IToolReference) {
			// check the reference we are overriding
			return ((IToolReference)parent).references(target);
		}
		else if (target instanceof IToolReference) {
			return parent.equals(((IToolReference)target).getTool()); 
		}
		else {
			// the real reference
			return parent.equals(target);
		}
	}

	public ITool getTool() {
		return parent;
	}

	public boolean buildsFileType(String extension) {
		return parent.buildsFileType(extension);
	}

	public int getNatureFilter() {
		return parent.getNatureFilter();
	}

	public IOption getOption(String id) {
		return parent.getOption(id);
	}

	public IOption[] getOptions() {
		return parent.getOptions();
	}

	public String getOutputExtension(String inputExtension) {
		return parent.getOutputExtension(inputExtension);
	}

	public String getOutputFlag() {
		return parent.getOutputFlag();
	}

	public String getOutputPrefix() {
		return parent.getOutputPrefix();
	}

	public String getToolCommand() {
		return parent.getToolCommand();
	}

	public String getToolFlags() throws BuildException {
		return parent.getToolFlags();
	}

	public IOptionCategory getTopOptionCategory() {
		return parent.getTopOptionCategory();
	}

	public boolean isHeaderFile(String ext) {
		return parent.isHeaderFile(ext);
	}

	public boolean producesFileType(String outputExtension) {
		return parent.producesFileType(outputExtension);
	}

	public String getId() {
		return parent.getId();
	}

	public String getName() {
		return parent.getName();
	}
}
