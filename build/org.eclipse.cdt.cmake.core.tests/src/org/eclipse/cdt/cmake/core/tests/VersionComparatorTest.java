package org.eclipse.cdt.cmake.core.tests;

import static org.eclipse.cdt.cmake.core.tests.VersionComparator.compareVersions;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class VersionComparatorTest {

	@Test
	public void testVersionComparator() {
		assertThat(compareVersions("1.0", "1.0"), equalTo(0));
		assertThat(compareVersions("1.0.1", "1.0"), equalTo(-1));
		assertThat(compareVersions("1.0.0", "1.0.0"), equalTo(0));
		assertThat(compareVersions("1.0.1", "1.0.0"), equalTo(1));
		assertThat(compareVersions("1.0.1", "1.0.2"), equalTo(-1));
		assertThat(compareVersions("1.0.10", "1.0.1"), equalTo(1));
		assertThat(compareVersions("1.0.10", "1.0.10.0"), equalTo(1));
		assertThat(compareVersions("1.0.10.0", "1.0.10"), equalTo(-1));
	}
}
