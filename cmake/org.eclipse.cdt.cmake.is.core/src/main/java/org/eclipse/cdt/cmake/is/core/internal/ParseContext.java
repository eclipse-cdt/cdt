/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.cmake.is.core.internal.builtins.RawIndexerInfo;
import org.eclipse.cdt.cmake.is.core.participant.IArglet;
import org.eclipse.cdt.cmake.is.core.participant.IToolCommandlineParser;

/**
 * Default implementation of IArgumentCollector.
 *
 * @author Martin Weber
 */
public final class ParseContext extends RawIndexerInfo
		implements IArglet.IArgumentCollector, IToolCommandlineParser.IResult {
	private final List<String> args = new ArrayList<>();

	@Override
	public void addBuiltinDetectionArgument(String argument) {
		args.add(argument);
	}

	@Override
	public List<String> getBuiltinDetectionArgs() {
		return args;
	}
}
