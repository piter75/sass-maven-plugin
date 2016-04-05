/*
 * Copyright 2014-2015 Mark Prins, GeoDienstenCentrum.
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

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_SOURCES;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import nl.geodienstencentrum.maven.plugin.sass.AbstractSassMojo;
import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Mojo that compiles Sass sources into CSS files using
 * {@code update_stylesheets}.
 */
@Mojo(name = "update-stylesheets", defaultPhase = PROCESS_SOURCES)
public class UpdateStylesheetsMojo extends AbstractSassMojo {

	/**
	 * Execute the compiler script.
	 *
	 * @see org.apache.maven.plugin.Mojo#execute()
	 * @throws MojoExecutionException when the execution of the plugin
	 *         errored
	 * @throws MojoFailureException when the Sass compilation fails
	 *
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.isSkip()) {
			this.getLog().info("Skip compiling Sass templates");
			return;
		}
		boolean buildRequired = true;
		try {
			buildRequired = buildRequired();
		} catch (IOException e) {
			throw new MojoExecutionException("Could not check file timestamps", e);
		}
		if (!buildRequired) {
			this.getLog().info("Skip compiling Sass templates, no changes.");
			return;
		}

		this.getLog().info("Compiling Sass templates");

		// build sass script
		final StringBuilder sassBuilder = new StringBuilder();
		this.buildBasicSassScript(sassBuilder);
		sassBuilder.append("Sass::Plugin.update_stylesheets");
		final String sassScript = sassBuilder.toString();

		// ...and execute
		this.executeSassScript(sassScript);
	}

	/**
	 * Returns true if a build is required.
	 *
	 * @return true if a build is required
	 * @throws IOException if one occurs checking the files and directories
	 */
	private boolean buildRequired() throws IOException {
		// If the target directory does not exist we need a build
		if (!buildDirectory.exists()) {
			return true;
		}

		boolean buildRequired = false;

		Iterator<Map.Entry<String, String>> templateLocations = getTemplateLocations();
		while (templateLocations.hasNext()) {
			Map.Entry<String, String> locationsEntry = templateLocations.next();

			final LastModifiedWalker sourceWalker =
					new LastModifiedWalker(new File(locationsEntry.getKey()));
			final LastModifiedWalker targetWalker =
					new LastModifiedWalker(new File(locationsEntry.getValue()));

			// If either directory is empty, we do a build to make sure
			buildRequired |= sourceWalker.getCount() == 0 || targetWalker.getCount() == 0;

			// We check the youngest files only if build is not required yet
			if (!buildRequired) {
				buildRequired = sourceWalker.getYoungest() > targetWalker.getYoungest();
			}
		}

		return buildRequired;
	}

	/**
	 * Directorywalker that looks at the lastModified timestamp of files.
	 *
	 * @see File#lastModified()
	 */
	private class LastModifiedWalker extends DirectoryWalker<Void> {
		/**
		 * timestamp of the youngest file.
		 */
		private Long youngest;
		/**
		 * timestamp of the oldest file.
		 */
		private Long oldest;
		/**
		 * number of files in the directory.
		 */
		private int count = 0;

		/**
		 * Create a "last modified" directory walker.
		 * @param startDirectory The direcoty to start walking.
		 * @throws IOException if any occurs while walking the directory tree.
		 */
		public LastModifiedWalker(final File startDirectory) throws IOException {
			walk(startDirectory, null);
			getLog().info("Checked " + count + " files for " + startDirectory);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void handleDirectoryStart(File directory, int depth, Collection<Void> results) throws IOException {
			updateTimestamps(directory);
			super.handleDirectoryStart(directory, depth, results);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void handleFile(final File file, final int depth,
		        final Collection<Void> results) throws IOException {
			updateTimestamps(file);
			count++;
			super.handleFile(file, depth, results);
		}

		/**
		 * Updates min and max timestamp, if necessary, according to current file / directory value
		 * @param file The file or directory for which the timestamp should by processed
         */
		private void updateTimestamps(File file) {
			long lastMod = file.lastModified();
			// CHECKSTYLE:OFF:AvoidInlineConditionals
			youngest = (youngest == null ? lastMod : Math.max(youngest, lastMod));
			oldest = (oldest == null ? lastMod : Math.min(oldest, lastMod));
			// CHECKSTYLE:ON
		}

		/**
		 * Get timestamp of the youngest file in the directory.
		 *
		 * @return timestamp of youngest file
		 * @see File#lastModified()
		 */
		public Long getYoungest() {
			return youngest;
		}

		/**
		 * Get timestamp of the oldest file in the directory.
		 *
		 * @return timestamp of oldest file
		 * @see File#lastModified()
		 */
		public Long getOldest() {
			return oldest;
		}

		/**
		 * get number of files in the directory.
		 *
		 * @return number of files
		 */
		public int getCount() {
			return count;
		}
	}
}
