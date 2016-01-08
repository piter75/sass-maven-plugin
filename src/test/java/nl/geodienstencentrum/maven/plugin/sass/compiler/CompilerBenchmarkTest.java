/*
 * Copyright 2015-2016 Mark Prins, GeoDienstenCentrum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.geodienstencentrum.maven.plugin.sass.compiler;

import com.google.common.base.Stopwatch;
import java.io.File;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * benchmark sass compilation.
 *
 * @author Mark Prins
 */
public class CompilerBenchmarkTest {

	/**
	 * Test resources.
	 */
	@Rule
	public TestResources resources = new TestResources();

	/**
	 * test rule.
	 */
	@Rule
	public MojoRule rule = new MojoRule();

	private static long TEST_ITERATIONS;

	@BeforeClass
	public static void readEnvironment() {
		try {
			TEST_ITERATIONS = Long.parseLong(System.getProperty("CompilerBenchmarkTest.iterations"));
			if (TEST_ITERATIONS < 2) {
				TEST_ITERATIONS = 2;
			}
		} catch (NumberFormatException e) {
			TEST_ITERATIONS = 2;
		}
	}

	/**
	 * Execute the compiler {@code TEST_ITERATIONS} times. Use
	 * -DCompilerBenchmarkTest.iterations=n to set the number of executions
	 * (defaults to 5).
	 *
	 * @throws Exception if any
	 * @see
	 * nl.geodienstencentrum.maven.plugin.sass.compiler.UpdateStylesheetsMojo#execute()
	 */
	@Test
	public void testExecuteNTimes() throws Exception {
		final File projectCopy = this.resources.getBasedir("complete-test");
		final File pom = new File(projectCopy, "pom.xml");
		assumeNotNull("POM file should not be null.", pom);
		assumeTrue("POM file should exist as file.",
				pom.exists() && pom.isFile());

		final UpdateStylesheetsMojo myMojo = (UpdateStylesheetsMojo) this.rule
				.lookupConfiguredMojo(projectCopy, "update-stylesheets");
		assumeNotNull(myMojo);

		// execute the mojo a number of times and time that
		Stopwatch stopwatch = Stopwatch.createStarted();
		for (long i = 0; i < TEST_ITERATIONS; i++) {
			myMojo.execute();
		}
		stopwatch.stop();

		myMojo.getLog().info("------------------------------------------------------------------------");
		myMojo.getLog().info("Elapsed benchmark time (running " + TEST_ITERATIONS + " times): "
				+ stopwatch.elapsed(MILLISECONDS) + "ms");
		myMojo.getLog().info("------------------------------------------------------------------------");

		TestResources.assertDirectoryContents(
				new File(projectCopy.getAbsolutePath() + "/target/css/"),
				"compiled.css.map", "compiled.css", "print.css.map",
				"print.css");
	}

}
