/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.IAdapterFactory;

public class QtBuildConfiguration extends CBuildConfiguration {

	public QtBuildConfiguration(IBuildConfiguration config) {
		super(config);
	}

	private static Map<IBuildConfiguration, QtBuildConfiguration> cache = new HashMap<>();

	public static class Factory implements IAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			if (adapterType.equals(QtBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
				synchronized (cache) {
					IBuildConfiguration config = (IBuildConfiguration) adaptableObject;
					QtBuildConfiguration qtConfig = cache.get(config);
					if (qtConfig == null) {
						qtConfig = new QtBuildConfiguration(config);
						cache.put(config, qtConfig);
					}
					return (T) qtConfig;
				}
			}
			return null;
		}

		@Override
		public Class<?>[] getAdapterList() {
			return new Class<?>[] { QtBuildConfiguration.class };
		}
	}

}
