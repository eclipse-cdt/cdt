/*******************************************************************************
 * Copyright (c) 2013, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableListener;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * String variable substitutor that resolves project_name, project_loc and project_path variables
 * in the context of the given project. Resolution of all other variables is delegated to
 * the default string variable manager.
 */
public class DebugStringVariableSubstitutor implements IStringVariableManager {
	private static class ProjectVariable implements IDynamicVariable {
		final String name;
		final IProject project;
		final String description;

		ProjectVariable(String name, String description, IProject project) {
			this.name = name;
			this.description = description;
			this.project = project;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getValue(String argument) throws CoreException {
			IProject project = this.project;
			if (argument != null) {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IPath path = Path.fromOSString(argument);
				project = path.isEmpty() ? null : root.getProject(path.segment(0));
			}
			if (project == null)
				return null;
			if (name.endsWith("_name")) //$NON-NLS-1$
				return project.getName();
			if (name.endsWith("_loc")) //$NON-NLS-1$
				return project.getLocation().toOSString();
			if (name.endsWith("_path")) //$NON-NLS-1$
				return project.getProjectRelativePath().toString();
			return null;
		}

		@Override
		public boolean supportsArgument() {
			return true;
		}
	}

	private final IStringVariableManager variableManager;
	private final IProject project;

	/**
	 * Creates a variable substitutor that resolves project_name, project_loc and project_path
	 * variables in the context of the given project.
	 *
	 * @param project the project used to resolve project_name, project_loc and project_path
	 *     variables. If {@code null}, the project is determined based on the current selection.
	 */
	public DebugStringVariableSubstitutor(IProject project) {
		this.variableManager = VariablesPlugin.getDefault().getStringVariableManager();
		this.project = project;
	}

	/**
	 * Creates a variable substitutor that resolves project_name, project_loc and project_path
	 * variables in the context of the given project.
	 *
	 * @param projectName the name of the project used to resolve project_name, project_loc and
	 *     project_path variables. If {@code null} or empty, the project is determined based on the current
	 *     selection.
	 */
	public DebugStringVariableSubstitutor(String projectName) {
		this(projectName == null || projectName.isEmpty() ? null
				: ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
	}

	@Override
	public IStringVariable[] getVariables() {
		IStringVariable[] variables = variableManager.getVariables();
		for (int i = 0; i < variables.length; i++) {
			IStringVariable var = variables[i];
			if (var instanceof IDynamicVariable)
				variables[i] = substituteVariable((IDynamicVariable) var);
		}
		return variables;
	}

	@Override
	public IValueVariable[] getValueVariables() {
		return variableManager.getValueVariables();
	}

	@Override
	public IValueVariable getValueVariable(String name) {
		return variableManager.getValueVariable(name);
	}

	@Override
	public IDynamicVariable[] getDynamicVariables() {
		IDynamicVariable[] variables = variableManager.getDynamicVariables();
		for (int i = 0; i < variables.length; i++) {
			variables[i] = substituteVariable(variables[i]);
		}
		return variables;
	}

	@Override
	public IDynamicVariable getDynamicVariable(String name) {
		IDynamicVariable var = variableManager.getDynamicVariable(name);
		if (var == null)
			return null;
		return substituteVariable(var);
	}

	private IDynamicVariable substituteVariable(IDynamicVariable var) {
		String name = var.getName();
		if ("project_loc".equals(name) || "project_name".equals(name) || "project_path".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return new ProjectVariable(name, var.getDescription(), project);
		}
		return var;
	}

	@Override
	public String getContributingPluginId(IStringVariable variable) {
		return variableManager.getContributingPluginId(variable);
	}

	@Override
	public String performStringSubstitution(String expression) throws CoreException {
		return performStringSubstitution(expression, true);
	}

	@Override
	public String performStringSubstitution(String expression, boolean reportUndefinedVariables) throws CoreException {
		return new StringSubstitutionEngine().performStringSubstitution(expression, reportUndefinedVariables, true,
				this);
	}

	@Override
	public void validateStringVariables(String expression) throws CoreException {
		new StringSubstitutionEngine().validateStringVariables(expression, this);
	}

	@Override
	public IValueVariable newValueVariable(String name, String description) {
		return variableManager.newValueVariable(name, description);
	}

	@Override
	public IValueVariable newValueVariable(String name, String description, boolean readOnly, String value) {
		return variableManager.newValueVariable(name, description, readOnly, value);
	}

	@Override
	public void addVariables(IValueVariable[] variables) throws CoreException {
		variableManager.addVariables(variables);
	}

	@Override
	public void removeVariables(IValueVariable[] variables) {
		variableManager.removeVariables(variables);
	}

	@Override
	public void addValueVariableListener(IValueVariableListener listener) {
		variableManager.addValueVariableListener(listener);
	}

	@Override
	public void removeValueVariableListener(IValueVariableListener listener) {
		variableManager.removeValueVariableListener(listener);
	}

	@Override
	public String generateVariableExpression(String varName, String arg) {
		return variableManager.generateVariableExpression(varName, arg);
	}
}
