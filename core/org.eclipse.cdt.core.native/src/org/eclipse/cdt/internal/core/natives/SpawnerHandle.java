/*******************************************************************************
 * Copyright (c) 2021 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - Initial implementation (Bug 572944)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.natives;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpawnerHandle implements ProcessHandle {

	private static class Info implements ProcessHandle.Info {

		final String fCommandLine;

		public Info(String commandLine) {
			fCommandLine = commandLine;
		}

		@Override
		public Optional<String> command() {
			return Optional.empty();
		}

		@Override
		public Optional<String> commandLine() {
			return Optional.of(fCommandLine);
		}

		@Override
		public Optional<String[]> arguments() {
			return Optional.empty();
		}

		@Override
		public Optional<Instant> startInstant() {
			return Optional.empty();
		}

		@Override
		public Optional<Duration> totalCpuDuration() {
			return Optional.empty();
		}

		@Override
		public Optional<String> user() {
			return Optional.empty();
		}

	}

	private Info fInfo;
	private long fPid;

	public SpawnerHandle(String[] cmdarray, long pid) {
		fInfo = new Info(arrayToString(cmdarray, "\n")); //$NON-NLS-1$
		fPid = pid;
	}

	@Override
	public long pid() {
		return fPid;
	}

	@Override
	public Optional<ProcessHandle> parent() {
		return Optional.empty();
	}

	@Override
	public Stream<ProcessHandle> children() {
		return Stream.empty();
	}

	@Override
	public Stream<ProcessHandle> descendants() {
		return Stream.empty();
	}

	@Override
	public Info info() {
		return fInfo;
	}

	@Override
	public CompletableFuture<ProcessHandle> onExit() {
		throw new UnsupportedOperationException(this.getClass() + ".onExit() not supported"); //$NON-NLS-1$
	}

	@Override
	public boolean supportsNormalTermination() {
		throw new UnsupportedOperationException(this.getClass() + ".supportNormalTermination() not supported"); //$NON-NLS-1$
	}

	@Override
	public boolean destroy() {
		throw new UnsupportedOperationException(this.getClass() + ".destroy() not supported"); //$NON-NLS-1$
	}

	@Override
	public boolean destroyForcibly() {
		throw new UnsupportedOperationException(this.getClass() + ".destroyForcibly() not supported"); //$NON-NLS-1$
	}

	@Override
	public boolean isAlive() {
		throw new UnsupportedOperationException(this.getClass() + ".isAlive() not supported"); //$NON-NLS-1$
	}

	@Override
	public int compareTo(ProcessHandle other) {
		return Long.valueOf(fPid).compareTo(Long.valueOf(other.pid()));
	}

	private static String arrayToString(String[] array, CharSequence delimiter) {
		return Arrays.stream(array).collect(Collectors.joining(delimiter));
	}

}
