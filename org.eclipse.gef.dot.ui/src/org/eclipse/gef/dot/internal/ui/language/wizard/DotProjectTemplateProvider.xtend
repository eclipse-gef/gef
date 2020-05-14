/*******************************************************************************
 * Copyright (c) 2020 itemis AG and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zoey Prigge (itemis AG) - initial API & implementation (bug #561084)
 *
 *******************************************************************************/
package org.eclipse.gef.dot.internal.ui.language.wizard

import org.eclipse.xtext.ui.XtextProjectHelper
import org.eclipse.xtext.ui.util.ProjectFactory
import org.eclipse.xtext.ui.wizard.template.IProjectGenerator
import org.eclipse.xtext.ui.wizard.template.IProjectTemplateProvider
import org.eclipse.xtext.ui.wizard.template.ProjectTemplate

/**
 * Create a list with all project templates to be shown in the template new project wizard.
 *
 * Each template is able to generate one or more projects. Each project can be configured such that any number of files are included.
 */
class DotProjectTemplateProvider implements IProjectTemplateProvider {
	override getProjectTemplates() {
		#[new EmptyProject, new DirectedGraph]
	}
}

@ProjectTemplate(label="Simple Directed Graph", icon="project_template.png", description="<p><b>Simple Directed Graph</b></p>
<p>This wizard creates a GEF DOT project containing a sample directed graph. I.e. a graph whose edges are directed.</p>")
final class DirectedGraph {
	override generateProjects(IProjectGenerator generator) {
		generator.generate(new ProjectFactory => [
			projectName = projectInfo.projectName
			location = projectInfo.locationPath
			projectNatures += #[XtextProjectHelper.NATURE_ID]
			builderIds += #[XtextProjectHelper.BUILDER_ID]
			addFile('''example.dot''', '''
				/*
				 * This is an example of a directed graph
				 * To get help creating further graphs use the add file wizard.
				 */
				digraph sampleGraphName {
					1 [label="Hello World"]
					1->2
				}
			''')
		])
	}
}

@ProjectTemplate(label="Empty Project", icon="project_template.png", description="<p><b>Create an empty GEF DOT project.</b></p>
<p>This wizard creates an empty GEF DOT project.</p>")
final class EmptyProject {
	override generateProjects(IProjectGenerator generator) {
		generator.generate(new ProjectFactory => [
			projectName = projectInfo.projectName
			location = projectInfo.locationPath
			projectNatures += #[XtextProjectHelper.NATURE_ID]
			builderIds += #[XtextProjectHelper.BUILDER_ID]
		])
	}
}