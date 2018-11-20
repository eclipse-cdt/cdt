/*******************************************************************************
 * Copyright (c) 2011 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import java.util.HashSet;
import java.util.Set;

import org.junit.internal.builders.IgnoredClassRunner;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * This custom suite runner ensures that a class runs only once, no matter how
 * many times it appears in a suite. Redundant appearances are possible with
 * hierarchical suites. In some cases, this is intentional and desirable--i.e.,
 * we want the same class to run multiple times (usually with some slight
 * variation). However, in some cases, the redundant appearances are
 * unintentional and unavoidable consequences of how the suites are defined and
 * used. This runner caters to the latter scenario.
 *
 * <p>
 * Thanks to Bill Venners and David Saff for suggesting this solution on the
 * junit mailing list. See <a
 * href="http://tech.groups.yahoo.com/group/junit/message/23208"
 * >http://tech.groups.yahoo.com/group/junit/message/23208</a>
 */
@SuppressWarnings("restriction")
public class OnceOnlySuite extends Suite {
	private static Set<Class<?>> alreadySeen = new HashSet<>();

	public OnceOnlySuite(Class<?> testClass, final RunnerBuilder builder) throws InitializationError {
		super(testClass, new RunnerBuilder() {
			@Override
			public Runner runnerForClass(Class<?> testClass) throws Throwable {
				if (alreadySeen.contains(testClass)) {
					return new IgnoredClassRunner(testClass);
				}
				alreadySeen.add(testClass);
				return builder.runnerForClass(testClass);
			}
		});
	}
}
