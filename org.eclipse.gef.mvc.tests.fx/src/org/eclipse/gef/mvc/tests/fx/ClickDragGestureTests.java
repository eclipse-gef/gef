/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG and others.
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

import static org.junit.Assert.assertEquals;

import java.awt.Point;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.mvc.fx.MvcFxModule;
import org.eclipse.gef.mvc.fx.domain.HistoricizingDomain;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.gestures.ClickDragGesture;
import org.eclipse.gef.mvc.fx.gestures.IGesture;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gef.mvc.tests.fx.rules.FXNonApplicationThreadRule;
import org.eclipse.gef.mvc.tests.fx.rules.FXNonApplicationThreadRule.RunnableWithResult;
import org.eclipse.gef.mvc.tests.fx.stubs.NullContentPartFactory;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.scene.Scene;

public class ClickDragGestureTests {

	private static class FXDomainDriver extends HistoricizingDomain {
		protected int openedExecutionTransactions = 0;
		protected int closedExecutionTransactions = 0;

		@Override
		public void closeExecutionTransaction(IGesture tool) {
			if (tool instanceof ClickDragGesture) {
				closedExecutionTransactions++;
			}
			super.closeExecutionTransaction(tool);
		}

		@Override
		public void openExecutionTransaction(IGesture tool) {
			super.openExecutionTransaction(tool);
			if (tool instanceof ClickDragGesture) {
				openedExecutionTransactions++;
			}
		}
	}

	/**
	 * Ensure all tests are executed on the JavaFX application thread (and the
	 * JavaFX toolkit is properly initialized).
	 */
	@Rule
	public FXNonApplicationThreadRule ctx = new FXNonApplicationThreadRule();;

	/**
	 * It is important that a single execution transaction (see
	 * {@link IDomain#openExecutionTransaction(org.eclipse.gef.mvc.fx.gestures.IGesture)}
	 * ) is used for a complete click/drag interaction gesture, because
	 * otherwise the transactional results of the gesture could not be undone in
	 * a single step, as it would result in more than one operation within the
	 * domain's {@link IOperationHistory}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void singleExecutionTransactionUsedForInteraction() throws Throwable {
		// create injector (adjust module bindings for test)
		Injector injector = Guice.createInjector(new MvcFxModule() {
			protected void bindIContentPartFactory() {
				binder().bind(IContentPartFactory.class).to(NullContentPartFactory.class);
			}

			@Override
			protected void bindIDomain() {
				// stub the domain to be able to keep track of opened execution
				// transactions
				binder().bind(IDomain.class).to(FXDomainDriver.class);
			}

			@Override
			protected void configure() {
				super.configure();
				bindIContentPartFactory();
			}
		});

		// inject domain
		final FXDomainDriver domain = injector.getInstance(FXDomainDriver.class);
		final IViewer viewer = domain.getAdapter(AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));

		final Scene scene = ctx.createScene(viewer.getCanvas(), 100, 100);

		// activate domain, so tool gets activated and can register listeners
		ctx.runAndWait(new Runnable() {
			@Override
			public void run() {
				domain.activate();
				assertEquals("No execution transaction should have been opened", 0, domain.openedExecutionTransactions);
				assertEquals("No execution transaction should have been closed", 0, domain.closedExecutionTransactions);
			}
		});

		// move mouse to viewer center
		Point sceneCenter = ctx.runAndWait(new RunnableWithResult<Point>() {
			@Override
			public Point run() {
				// XXX: It seems to be important to compute the position from
				// within the JavaFX application thread.
				return new Point((int) (scene.getX() + scene.getWidth() / 2),
						(int) (scene.getY() + scene.getHeight() / 2));
			}
		});
		ctx.mouseMove(scene.getRoot(), sceneCenter.x, sceneCenter.y);

		// simulate click gesture
		ctx.mousePress();
		ctx.runAndWait(new Runnable() {
			@Override
			public void run() {
				assertEquals("A single execution transaction should have been opened", 1,
						domain.openedExecutionTransactions);
				assertEquals("No execution transaction should have been closed", 0, domain.closedExecutionTransactions);
			}
		});
		ctx.mouseRelease();
		ctx.runAndWait(new Runnable() {
			@Override
			public void run() {
				assertEquals("A single execution transaction should have been opened", 1,
						domain.openedExecutionTransactions);
				assertEquals("A single execution transaction should have been closed", 1,
						domain.closedExecutionTransactions);
			}
		});

		// re-initialize
		ctx.runAndWait(new Runnable() {
			@Override
			public void run() {
				domain.openedExecutionTransactions = 0;
				domain.closedExecutionTransactions = 0;
				assertEquals("No execution transaction should have been opened", 0, domain.openedExecutionTransactions);
				assertEquals("No execution transaction should have been closed", 0, domain.closedExecutionTransactions);
			}
		});

		// simulate click/drag
		ctx.mousePress();
		ctx.runAndWait(new Runnable() {
			@Override
			public void run() {
				assertEquals("A single execution transaction should have been opened", 1,
						domain.openedExecutionTransactions);
				assertEquals("No execution transaction should have been closed", 0, domain.closedExecutionTransactions);
			}
		});
		ctx.mouseDrag(20, 20);
		ctx.runAndWait(new Runnable() {
			@Override
			public void run() {
				assertEquals("A single execution transaction should have been opened", 1,
						domain.openedExecutionTransactions);
				assertEquals("No execution transaction should have been closed", 0, domain.closedExecutionTransactions);
			}
		});
		ctx.mouseRelease();
		ctx.runAndWait(new Runnable() {
			@Override
			public void run() {
				assertEquals("A single execution transaction should have been opened", 1,
						domain.openedExecutionTransactions);
				assertEquals("A single execution transaction should have been closed", 1,
						domain.closedExecutionTransactions);
			}
		});
	}

}
