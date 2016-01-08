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
package nl.geodienstencentrum.maven.plugin.sass.report;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import nl.geodienstencentrum.maven.plugin.sass.Resource;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;

/**
 * SCSSLintMojo executes scss-lint-report goal.
 *
 * @author mprins
 * @since 2.3
 */
@Mojo(name = "scss-lint-report",
        defaultPhase = LifecyclePhase.SITE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = false)
@Execute(goal = "scss-lint"
//        , phase = LifecyclePhase.COMPILE
)
public class SCSSLintReportMojo extends AbstractMavenReport {

	/**
	 * ignored for linting, sass options are not used.
	 *
	 * @since 2.0
	 */
	@Parameter
	private Map<String, String> sassOptions;

	/**
	 * Directory containing Sass files, defaults to the Maven Web application
	 * sources directory (${basedir}/src/main/sass).
	 *
	 * @since 2.3
	 */
	@Parameter(defaultValue = "${basedir}/src/main/sass", property = "sassSourceDirectory")
	private File sassSourceDirectory;

	/**
	 * Sources for compilation with their destination directory containing Sass
	 * files. Allows for multiple resource sources and destinations. If
	 * specified it precludes the direct specification of
	 * sassSourceDirectory/relativeOutputDirectory/destination parameters.
	 *
	 * Example configuration:
	 *
	 * <pre>
	 *      &lt;resource&gt;
	 *          &lt;source&gt;
	 *              &lt;directory&gt;${basedir}/src/main/webapp&lt;/directory&gt;
	 *              &lt;includes&gt;
	 *                  &lt;include&gt;**&#47;scss&lt;/include&gt;
	 *              &lt;/includes&gt;
	 *          &lt;/source&gt;
	 *          &lt;relativeOutputDirectory&gt;..&lt;/relativeOutputDirectory&gt;
	 *          &lt;destination&gt;
	 *              ${project.build.directory}/${project.build.finalName}
	 *          &lt;/destination&gt;
	 *      &lt;/resource&gt;
	 * </pre>
	 *
	 * <em>Only {@code source/directory} paths are considered during
	 * linting.</em>
	 *
	 * @since 2.0
	 */
	@Parameter
	private List<Resource> resources = Collections.emptyList();

	/**
	 * Specifies if the build should fail upon an error violation.
	 */
	@Parameter(defaultValue = "false")
	private boolean failOnError;

	/**
	 * Output file name for the plugin.
	 *
	 * @since 2.3
	 */
	@Parameter(defaultValue = "scss-lint", property = "outputName", required = true)
	private String outputName;

	/**
	 * Output directory for the plugin.
	 *
	 * @since 2.3
	 */
	@Parameter(defaultValue = "${project.build.directory}/site/", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Component
	private Renderer siteRenderer;

	/**
	 * skip execution.
	 *
	 * @since 2.9
	 */
	@Parameter(defaultValue = "false")
	private boolean skip;

	/**
	 * Build the report, for now ignoring the locale.
	 *
	 * @param locale ignored
	 *
	 * @see org.apache.maven.plugin.Mojo#execute()
	 * @see #getBundle(java.util.Locale)
	 */
	@Override
	public void executeReport(final Locale locale) {
		if (this.skip) {
			return;
		}
		try {
			final SCSSLintReportGenerator generator = new SCSSLintReportGenerator(
                    getSink(),
                    this.getDescription(locale),
                    new File(getProject().getBasedir() + "/target", "scss-lint.xml"),
                    getLog());
			generator.generateReport();
		} catch (Exception t) {
			getLog().error("Error during SCSS Lint report generation", t);
			if (failOnError) {
				throw t;
			}
		}
	}

	@Override
	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getAbsolutePath();
	}

	@Override
	public void setReportOutputDirectory(final File reportOutputDirectory) {
		this.outputDirectory = reportOutputDirectory;
	}

	@Override
	public File getReportOutputDirectory() {
		return outputDirectory;
	}

	@Override
	protected MavenProject getProject() {
		return project;
	}

	@Override
	public String getOutputName() {
		return this.outputName;
	}

	@Override
    public String getName(final Locale locale) {
		return "scss-lint report";
	}

	@Override
	public String getDescription(final Locale locale) {
		return "A scss-lint report.";
	}

	private ResourceBundle getBundle(final Locale locale) {
		return ResourceBundle.getBundle("scss-lint-report", locale,
                this.getClass().getClassLoader());
	}
}
