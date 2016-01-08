/*
 * Copyright 2014-2016 Mark Prins, GeoDienstenCentrum.
 * Copyright 2010-2014 Jasig.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
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

import nl.geodienstencentrum.maven.plugin.sass.AbstractSassMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The Class WatchMojo runs the Sass compiler's {@code watch} process.
 */
@Mojo(name = "watch")
public class WatchMojo extends AbstractSassMojo {

	/** true when we are running on Windows. */
	private static final boolean IS_WINDOWS = 
	        System.getProperty("os.name").toLowerCase().contains("win");

	/**
	 * Start the watch process.
	 * 
	 * @throws MojoExecutionException when the execution of the plugin
	 *         errored
	 * @throws MojoFailureException when the Sass compilation fails
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
    @Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.isSkip()) {
			return;
		}
		this.getLog().info("Watching Sass Templates");

		// build sass script
		final StringBuilder sassBuilder = new StringBuilder();
		this.buildBasicSassScript(sassBuilder);
		if (IS_WINDOWS) {
			sassBuilder.append("require 'listen'\nSass::Plugin.options.merge!(:poll => true)\n");
		}
		sassBuilder.append("Sass::Plugin.watch");
		final String sassScript = sassBuilder.toString();

		// ...and execute
		this.executeSassScript(sassScript);
	}
}
