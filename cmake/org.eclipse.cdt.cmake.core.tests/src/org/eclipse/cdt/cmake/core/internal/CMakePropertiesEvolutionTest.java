/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.eclipse.cdt.cmake.core.internal.properties.CMakePropertiesBean;
import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

/**
 * @author Martin Weber
 */
public class CMakePropertiesEvolutionTest {

	private static final String VALUE_OF_EVOLVED_PROPERTY = "value of evolvedProperty";

	/** Tests whether properties persisted by a previous version of our bundle can be loaded by
	 * a newer version of our bundle.
	 */
	@Test
	public void testSaveLoadEvolution_1() throws IOException {
		CMakePropertiesBean propsAtDefault = new CMakePropertiesBean();
		propsAtDefault.reset(true);

		CMakePropertiesBean props = new CMakePropertiesBean();
		props.setCacheFile("cacheFile");
		props.setClearCache(true);
		props.setDebugOutput(true);
		props.setDebugTryCompile(true);
		props.setTrace(true);
		props.setWarnNoDev(true);
		props.setWarnUnitialized(true);
		props.setWarnUnused(true);
		props.getLinuxOverrides().setGenerator(CMakeGenerator.Ninja);

		String extraArgs = "arg1 arg2";
		props.setExtraArguments(extraArgs);

		Yaml yaml = new Yaml(new CustomClassLoaderConstructor(this.getClass().getClassLoader()));
		String output = yaml.dump(props);

		// try to load as evolved properties..
		CMakePropertiesBean_1 in = yaml.loadAs(output, CMakePropertiesBean_1.class);
		assertNotNull(in);
		assertEquals(CMakePropertiesEvolutionTest.VALUE_OF_EVOLVED_PROPERTY, in.getEvolvedProperty());
		assertThat(props).usingRecursiveComparison().isEqualTo(in);
	}

	private static class CMakePropertiesBean_1 extends CMakePropertiesBean {
		private String evolvedProperty;

		@Override
		public void reset(boolean resetOsOverrides) {
			super.reset(resetOsOverrides);
			evolvedProperty = CMakePropertiesEvolutionTest.VALUE_OF_EVOLVED_PROPERTY;
		}

		public String getEvolvedProperty() {
			return evolvedProperty;
		}

		public void setEvolvedProperty(String evolvedProperty) {
			this.evolvedProperty = evolvedProperty;
		}
	}
}
