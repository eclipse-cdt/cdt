/*******************************************************************************
 * Copyright (c) 2006, 2016 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.buildmodel.IBuildIOType;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.ITool;

/*
 * this is the build description debug utility class
 */
public class DbgUtil {
	public static boolean DEBUG = false;
	private static PrintStream out = System.out;
	private static final String TRACE_PREFIX = "BuildModel[ "; //$NON-NLS-1$
	private static final String TRACE_SUFIX = " ]"; //$NON-NLS-1$

	public static void trace(String str) {
		out.println(formatMsg(str));
	}

	public static String formatMsg(String msg) {
		return TRACE_PREFIX + msg + TRACE_SUFIX;
	}

	public static String stepName(IBuildStep action) {
		ITool tool = action instanceof BuildStep ? ((BuildStep) action).getTool() : null;
		if (tool != null)
			return tool.getName();
		if (action.getBuildDescription().getInputStep() == action)
			return "input step"; //$NON-NLS-1$
		if (action.getBuildDescription().getOutputStep() == action)
			return "output step"; //$NON-NLS-1$
		return "<undefined name>"; //$NON-NLS-1$
	}

	public static String resourceName(IBuildResource rc) {
		if (rc.getFullPath() != null)
			return rc.getFullPath().toString();
		return rc.getLocation().toString();
	}

	public static String dumpType(IBuildIOType type) {
		StringBuilder buf = new StringBuilder();

		buf.append("dumping type: "); //$NON-NLS-1$
		buf.append(type.isInput() ? "INPUT" : "OUTPUT"); //$NON-NLS-1$	//$NON-NLS-2$
		buf.append(ioTypeResources(type));
		buf.append("end dumping type"); //$NON-NLS-1$

		return buf.toString();
	}

	public static String ioTypeResources(IBuildIOType type) {
		StringBuilder buf = new StringBuilder();

		IBuildResource rcs[] = type.getResources();

		buf.append("\n"); //$NON-NLS-1$

		for (int i = 0; i < rcs.length; i++) {
			buf.append(resourceName(rcs[i]));
			buf.append("\n"); //$NON-NLS-1$
		}

		return buf.toString();
	}

	public static String dumpStep(IBuildStep step, boolean inputs) {
		StringBuilder buf = new StringBuilder();

		buf.append("dumping step ").append(stepName(step)).append(inputs ? " inputs" : " outputs"); //$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$

		IBuildIOType types[] = inputs ? step.getInputIOTypes() : step.getOutputIOTypes();

		buf.append('\n');

		for (int i = 0; i < types.length; i++) {
			buf.append("ioType ").append(i).append(':'); //$NON-NLS-1$
			buf.append(ioTypeResources(types[i]));
		}

		buf.append("end dump step\n"); //$NON-NLS-1$
		return buf.toString();
	}

	public static String dumpStep(IBuildStep step) {
		return dumpStep(step, true) + dumpStep(step, false);
	}

	public static String dumpResource(IBuildResource rc) {
		return dumpResource(rc, true) + dumpResource(rc, false);
	}

	public static String dumpResource(IBuildResource rc, boolean producer) {
		StringBuilder buf = new StringBuilder();

		buf.append("dumping resource ").append(resourceName(rc)).append(producer ? " producer:" : " deps:"); //$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$

		if (producer) {
			if (rc.getProducerIOType() != null)
				buf.append(dumpStep(rc.getProducerIOType().getStep()));
			else
				buf.append("\nresourse has no producer\n"); //$NON-NLS-1$
		} else {
			IBuildIOType types[] = rc.getDependentIOTypes();

			if (types.length > 0) {
				Set<IBuildStep> set = new HashSet<>();

				for (int i = 0; i < types.length; i++) {
					if (set.add(types[i].getStep())) {
						buf.append(dumpStep(types[i].getStep()));
					}
				}
			} else {
				buf.append("\n resource has no deps\n"); //$NON-NLS-1$
			}

		}

		buf.append("end dump resource\n"); //$NON-NLS-1$
		return buf.toString();
	}

	public static void flush() {
		out.flush();
	}
}
