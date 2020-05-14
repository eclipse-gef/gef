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
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.gef.dot.internal.ide.language;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.gef.dot.internal.language.DotArrowTypeRuntimeModule;
import org.eclipse.gef.dot.internal.language.DotArrowTypeStandaloneSetup;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
public class DotArrowTypeIdeSetup extends DotArrowTypeStandaloneSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(new DotArrowTypeRuntimeModule(), new DotArrowTypeIdeModule()));
	}

}
