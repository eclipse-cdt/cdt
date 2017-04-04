package org.eclipse.cdt.cmake.core.tests;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.hamcrest.Matcher;

public class CMakeServerTestUtils {
	/**
	 * Return the system path as a stream of Path objects
	 * 
	 * @return
	 */
	public static Stream<Path> getSystemPath() {
		return Arrays.stream(Optional.ofNullable(System.getenv("PATH")).orElseGet(() -> System.getenv("Path"))
				.split(File.pathSeparator)).filter(s -> s.length() > 0).map(p -> Paths.get(p));
	}

	public static void waitUntil(Object object, Matcher<?> matcher) throws InterruptedException, TimeoutException {
		int timeout = 5000;
		long start = System.currentTimeMillis();

		while (true) {
			if (matcher.matches(object))
				return;

			Thread.sleep(10);
			if (System.currentTimeMillis() - start > timeout) {
				throw new TimeoutException(String.format("Timeout while waiting for %s to match %s", object, matcher));
			}
		}
	}
}
