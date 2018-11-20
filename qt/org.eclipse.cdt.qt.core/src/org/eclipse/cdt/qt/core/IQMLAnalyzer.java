/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptException;

import org.eclipse.cdt.qt.core.qmljs.IQmlASTNode;

public interface IQMLAnalyzer {

	void addFile(String fileName, String code) throws NoSuchMethodException, ScriptException;

	void deleteFile(String fileName) throws NoSuchMethodException, ScriptException;

	IQmlASTNode parseFile(String fileName, String text) throws NoSuchMethodException, ScriptException;

	IQmlASTNode parseString(String text) throws NoSuchMethodException, ScriptException;

	IQmlASTNode parseString(String text, String mode, boolean locations, boolean ranges)
			throws NoSuchMethodException, ScriptException;

	Collection<QMLTernCompletion> getCompletions(String fileName, String text, int pos)
			throws NoSuchMethodException, ScriptException;

	Collection<QMLTernCompletion> getCompletions(String fileName, String text, int pos, boolean includeKeywords)
			throws NoSuchMethodException, ScriptException;

	List<Bindings> getDefinition(String identifier, String fileName, String text, int pos)
			throws NoSuchMethodException, ScriptException;

	void load() throws ScriptException, IOException, NoSuchMethodException;

}