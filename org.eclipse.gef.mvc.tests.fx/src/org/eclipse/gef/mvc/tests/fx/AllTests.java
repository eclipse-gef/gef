/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef.mvc.tests.fx;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AbstractVisualPartTests.class, BendableTests.class, ContentSynchronizationTests.class,
		FocusTraversalPolicyTests.class, SelectionModelTests.class, AbstractHandlePartTests.class,
		BendConnectionPolicyTests.class, ClickDragGestureTests.class, TypeStrokeGestureTests.class, TransformPolicyTests.class,
		FocusTraversalPolicyTests.class, ResizePolicyTests.class })
public class AllTests {

}
