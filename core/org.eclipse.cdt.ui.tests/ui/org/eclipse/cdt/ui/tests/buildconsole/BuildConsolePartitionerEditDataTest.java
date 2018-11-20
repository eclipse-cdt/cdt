/*******************************************************************************
 * Copyright (c) 2017 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.buildconsole;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.internal.ui.buildconsole.BuildConsolePartition;
import org.eclipse.cdt.internal.ui.buildconsole.BuildConsolePartitionerEditData;
import org.eclipse.cdt.internal.ui.buildconsole.BuildConsolePartitionerEditData.UpdateUIData;
import org.eclipse.cdt.internal.ui.buildconsole.IBuildConsoleStreamDecorator;
import org.junit.Before;
import org.junit.Test;

public class BuildConsolePartitionerEditDataTest {

	private static final int DEFAULT_MAX_LINES = 100;
	private BuildConsolePartitionerEditData data;
	private IBuildConsoleStreamDecorator stream1 = mock(IBuildConsoleStreamDecorator.class);
	private IBuildConsoleStreamDecorator stream2 = mock(IBuildConsoleStreamDecorator.class);
	private ProblemMarkerInfo marker1 = new ProblemMarkerInfo(null, 0, null, 0, null);
	private ProblemMarkerInfo marker2 = new ProblemMarkerInfo(null, 0, null, 0, null);

	@Before
	public void before() {
		data = new BuildConsolePartitionerEditData(DEFAULT_MAX_LINES);
	}

	@Test
	public void testBasicOperation() {
		data.clear();
		UpdateUIData update0 = data.getUpdate();
		assertThat(update0.getNewContents(), is(""));
		assertThat(update0.getStreamsNeedingNotifcation(), is(empty()));
		assertThat(update0.needsClearDocumentMarkerManager(), is(true));
		assertThat(update0.getOffset(), is(0L));

		data.append("Line of text\n", stream1, null);
		UpdateUIData update1 = data.getUpdate();
		assertThat(update1.getNewContents(), is("Line of text\n"));
		assertThat(update1.getStreamsNeedingNotifcation(), is(Arrays.asList(stream1)));
		assertThat(update1.needsClearDocumentMarkerManager(), is(false));
		assertThat(update1.getOffset(), is(0L));

		data.append("Another line of text\n", stream2, null);
		UpdateUIData update2 = data.getUpdate();
		assertThat(update2.getNewContents(), is("Line of text\nAnother line of text\n"));
		assertThat(update2.getStreamsNeedingNotifcation(), is(Arrays.asList(stream2)));
		assertThat(update2.needsClearDocumentMarkerManager(), is(false));
		assertThat(update2.getOffset(), is(0L));
	}

	@Test
	public void testOverflow() {
		StringBuilder all = new StringBuilder();
		for (int i = 0; i < DEFAULT_MAX_LINES * 4; i++) {
			String text = "Line " + i + "\n";
			data.append(text, stream1, null);
			all.append(text);
		}

		UpdateUIData update = data.getUpdate();
		assertThat(update.needsClearDocumentMarkerManager(), is(true));
		assertThat(update.getNewPartitions().size(), is(lessThanOrEqualTo(2)));

		String contents = update.getNewContents();
		int newlines = (int) contents.chars().filter(ch -> ch == '\n').count();
		assertThat(newlines, is(lessThan(DEFAULT_MAX_LINES * 2)));
		assertThat(newlines, is(greaterThanOrEqualTo(DEFAULT_MAX_LINES)));

		int lastLine = DEFAULT_MAX_LINES * 4 - 1;
		assertThat(contents, endsWith("Line " + lastLine + "\n"));
		int firstLine = lastLine - newlines + 1;
		assertThat(contents, startsWith("Line " + firstLine + "\n"));

		long expectedOffset = all.indexOf(contents);
		assertThat(update.getOffset(), is(expectedOffset));
	}

	@Test
	public void testPartitionsCombine() {
		data.append("Line\n", stream1, null);
		data.append("Line\n", stream1, null);
		UpdateUIData update = data.getUpdate();

		assertThat(update.getNewPartitions(), is(Arrays.asList(
				new BuildConsolePartition(stream1, 0, 10, BuildConsolePartition.CONSOLE_PARTITION_TYPE, null, 1))));
	}

	@Test
	public void testPartitionsDontCombineOnDifferentStreams() {
		data.append("Line\n", stream1, null);
		data.append("Line\n", stream2, null);
		UpdateUIData update = data.getUpdate();

		assertThat(update.getNewPartitions(), is(Arrays.asList(
				new BuildConsolePartition(stream1, 0, 5, BuildConsolePartition.CONSOLE_PARTITION_TYPE, null, 1),
				new BuildConsolePartition(stream2, 5, 5, BuildConsolePartition.CONSOLE_PARTITION_TYPE, null, 1))));
	}

	@Test
	public void testPartitionsDontCombineOnDifferentMarkersA() {
		data.append("Line\n", stream1, marker1);
		data.append("Line\n", stream1, marker2);
		UpdateUIData update = data.getUpdate();

		assertThat(update.getNewPartitions(), is(Arrays.asList(
				new BuildConsolePartition(stream1, 0, 5, BuildConsolePartition.INFO_PARTITION_TYPE, marker1, 1),
				new BuildConsolePartition(stream1, 5, 5, BuildConsolePartition.INFO_PARTITION_TYPE, marker2, 1))));
	}

	@Test
	public void testPartitionsDontCombineOnDifferentMarkersB() {
		data.append("Line\n", stream1, null);
		data.append("Line\n", stream1, marker2);
		UpdateUIData update = data.getUpdate();

		assertThat(update.getNewPartitions(), is(Arrays.asList(
				new BuildConsolePartition(stream1, 0, 5, BuildConsolePartition.CONSOLE_PARTITION_TYPE, null, 1),
				new BuildConsolePartition(stream1, 5, 5, BuildConsolePartition.INFO_PARTITION_TYPE, marker2, 1))));
	}

	@Test
	public void testPartitionsDontCombineOnDifferentMarkersC() {
		data.append("Line\n", stream1, marker1);
		data.append("Line\n", stream1, null);
		UpdateUIData update = data.getUpdate();

		assertThat(update.getNewPartitions(), is(Arrays.asList(
				new BuildConsolePartition(stream1, 0, 5, BuildConsolePartition.INFO_PARTITION_TYPE, marker1, 1),
				new BuildConsolePartition(stream1, 5, 5, BuildConsolePartition.CONSOLE_PARTITION_TYPE, null, 1))));
	}

}
