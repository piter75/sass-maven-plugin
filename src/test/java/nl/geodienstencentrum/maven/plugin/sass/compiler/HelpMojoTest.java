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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Rule;
import org.junit.Test;

/**
 * Testcase for
 * {@link nl.geodienstencentrum.maven.plugin.sass.compiler.HelpMojo }
 * (which is a generated class).
 *
 * @author Mark C. Prins
 */
public class HelpMojoTest {

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

	/**
	 * Test method for
	 * {@link nl.geodienstencentrum.maven.plugin.sass.compiler.HelpMojo#execute() }
	 * , it tests execution.
	 *
	 * @throws Exception if any
	 */
	@Test
	public void testExecute() throws Exception {
        final File projectCopy = this.resources.getBasedir("maven-sass-test");
		final File pom = new File(projectCopy, "pom.xml");
		assumeNotNull("POM file should not be null.", pom);
		assumeTrue("POM file should exist as file.",pom.exists() && pom.isFile());

		final HelpMojo myMojo = (HelpMojo) this.rule.lookupEmptyMojo("help", pom);
		assertNotNull("The 'help' mojo should exist", myMojo);
		// set -Ddetail=true
		rule.setVariableValueToObject(myMojo, "detail", true);
		// should execute and not error
		myMojo.execute();
	}
}
