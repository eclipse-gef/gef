/*******************************************************************************
 * Copyright (c) 2018 KDM Analytics Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kyle Girard (KDM Analytics Inc.) - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.gef.mvc.fx.gestures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.fx.nodes.InfiniteCanvas;
import org.eclipse.gef.mvc.fx.gestures.AbstractGesture;
import org.eclipse.gef.mvc.fx.gestures.IGesture;
import org.eclipse.gef.mvc.fx.parts.PartUtils;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;
import org.eclipse.gef.mvc.fx.handlers.IOnDragDropHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;

/**
 * An {@link IGesture} to handle drag & drop interaction gestures.
 *
 * @author kgirard
 */
public class DragDropGesture extends AbstractGesture {

	/**
	 * The typeKey used to retrieve those policies that are able to handle the dragdrop gesture.
	 */
	public static final Class<IOnDragDropHandler> ON_DRAGDROP_POLICY_KEY = IOnDragDropHandler.class;

	private IViewer activeViewer;

	private final Set<Scene> scenes = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * This {@link EventHandler} is registered as an event filter on the {@link Scene} to handle
	 * dragEntered events.
	 */
	private final EventHandler<DragEvent> dragEnteredFilter = new EventHandler<DragEvent>() {

		@Override
		public void handle(final DragEvent event) {
			final EventTarget target = event.getTarget();
			if (target instanceof Node) {
				dragEntered((Node) target, event);
			}
		}
	};

	/**
	 * This {@link EventHandler} is registered as an event filter on the {@link Scene} to handle
	 * dragExited events.
	 */
	private final EventHandler<DragEvent> dragExitedFilter = new EventHandler<DragEvent>() {

		@Override
		public void handle(final DragEvent event) {
			final EventTarget target = event.getTarget();
			if (target instanceof Node) {
				dragExited((Node) target, event);
			}
		}
	};

	/**
	 * This {@link EventHandler} is registered as an event filter on the {@link Scene} to handle
	 * dragOver events.
	 */
	private final EventHandler<DragEvent> dragOverFilter = new EventHandler<DragEvent>() {

		@Override
		public void handle(final DragEvent event) {
			final EventTarget target = event.getTarget();
			if (event.getGestureSource() != target &&
					(target instanceof Node)) {
				final Node targetNode = (Node) target;
				dragOver(targetNode, event);
			}
		}
	};

	/**
	 * This {@link EventHandler} is registered as an event filter on the {@link Scene} to handle
	 * dragDropped events.
	 */
	private final EventHandler<DragEvent> dragDroppedFilter = new EventHandler<DragEvent>() {

		@Override
		public void handle(final DragEvent event) {
			dragDropped(event);
		}
	};

	private final ChangeListener<Boolean> viewerFocusChangeListener = new ChangeListener<Boolean>() {

		@Override
		public void changed(final ObservableValue<? extends Boolean> observable,
				final Boolean oldValue, final Boolean newValue) {
			// cannot abort if no activeViewer
			if (activeViewer == null) {
				return;
			}
			// check if any viewer is focused
			for (final IViewer v : getDomain().getViewers().values()) {
				if (v.isViewerFocused()) {
					return;
				}
			}
			// no viewer is focused => abort
			// clear active policies
			clearActiveHandlers(activeViewer);
			activeViewer = null;
		}
	};

	@Override
	protected void doActivate() {
		super.doActivate();
		for (final IViewer viewer : getDomain().getViewers().values()) {
			// register a viewer focus change listener
			viewer.viewerFocusedProperty().addListener(viewerFocusChangeListener);

			final Scene scene = viewer.getCanvas().getScene();
			if (scenes.contains(scene)) {
				// already registered for this scene
				continue;
			}

			// register a drag entered filter for forwarding event to drag/drop policies
			scene.addEventFilter(DragEvent.DRAG_ENTERED, dragEnteredFilter);
			// register a drag exited filter for forwarding event to drag/drop policies
			scene.addEventFilter(DragEvent.DRAG_EXITED, dragExitedFilter);
			// register a drag over filter for forwarding event to drag/drop policies
			scene.addEventFilter(DragEvent.DRAG_OVER, dragOverFilter);
			// register a drag dropped filter for forwarding event to drag/drop policies
			scene.addEventFilter(DragEvent.DRAG_DROPPED, dragDroppedFilter);

			scenes.add(scene);
		}
	}

	protected void dragEntered(final Node target, final DragEvent event) {
		final IViewer viewer = PartUtils.retrieveViewer(getDomain(), target);
		final List<? extends IOnDragDropHandler> dragDropPolicies = getHandlerResolver()
				.resolve(DragDropGesture.this, target, viewer, ON_DRAGDROP_POLICY_KEY);
		if (dragDropPolicies != null && !dragDropPolicies.isEmpty()) {
			for (final IOnDragDropHandler dragDropPolicy : dragDropPolicies) {
				dragDropPolicy.dragEntered(event);
			}
		}
	}

	protected void dragExited(final Node target, final DragEvent event) {
		final IViewer viewer = PartUtils.retrieveViewer(getDomain(), target);
		final List<? extends IOnDragDropHandler> dragDropPolicies = getHandlerResolver()
				.resolve(DragDropGesture.this, target, viewer, ON_DRAGDROP_POLICY_KEY);
		if (dragDropPolicies != null && !dragDropPolicies.isEmpty()) {
			for (final IOnDragDropHandler dragDropPolicy : dragDropPolicies) {
				dragDropPolicy.dragExited(event);
			}
		}
	}

	protected void dragOver(final Node target, final DragEvent event) {

		final IViewer viewer = PartUtils.retrieveViewer(getDomain(), target);
		if (viewer == null) {
			return;
		}

		if (viewer instanceof InfiniteCanvasViewer) {
			final InfiniteCanvas canvas = ((InfiniteCanvasViewer) viewer).getCanvas();
			// if any node in the target hierarchy is a scrollbar,
			// do not process the event
			if (event.getTarget() instanceof Node) {
				Node targetNode = (Node) event.getTarget();
				while (targetNode != null) {
					if (targetNode == canvas.getHorizontalScrollBar()
							|| targetNode == canvas.getVerticalScrollBar()) {
						return;
					}
					targetNode = targetNode.getParent();
				}
			}
		}

		final Scene scene = target.getScene();
		if (scene == null) {
			return;
		}

		// determine viewer that contains the given target part
		activeViewer = PartUtils.retrieveViewer(getDomain(), target);
		final List<? extends IOnDragDropHandler> dragDropPolicies = getHandlerResolver()
				.resolve(DragDropGesture.this, target, activeViewer, ON_DRAGDROP_POLICY_KEY);
		setActiveHandlers(activeViewer, dragDropPolicies);
		if (dragDropPolicies != null && !dragDropPolicies.isEmpty()) {
			for (final IOnDragDropHandler dragDropPolicy : dragDropPolicies) {
				dragDropPolicy.dragOver(event);
			}
		}

	}

	protected void dragDropped(final DragEvent event) {
		if (activeViewer == null) {
			return;
		}

		// enable indication cursor event filters outside of
		// press-drag-release gesture
		final Scene scene = activeViewer.getRootPart().getVisual().getScene();
		if (scene == null) {
			throw new IllegalStateException("Active viewer's root part visual is not in Scene.");
		}

		final List<IOnDragDropHandler> handlers = getActiveHandlers(activeViewer);

		// abort processing of this gesture if no policies could be
		// found that can process it
		if (handlers.isEmpty()) {
			activeViewer = null;
			return;
		}
		getDomain().openExecutionTransaction(DragDropGesture.this);

		// send dragDropped() to all dragDrop policies
		for (final IOnDragDropHandler policy : handlers) {
			policy.dragDropped(event);
		}
		// clear active policies before processing drop
		clearActiveHandlers(activeViewer);
		activeViewer = null;

		// remove this tool from the domain's execution transaction
		getDomain().closeExecutionTransaction(DragDropGesture.this);
	}

	@Override
	protected void doDeactivate() {
		for (final Scene scene : new ArrayList<>(scenes)) {
			scene.removeEventFilter(DragEvent.DRAG_ENTERED, dragEnteredFilter);
			scene.removeEventFilter(DragEvent.DRAG_EXITED, dragExitedFilter);
			scene.removeEventFilter(DragEvent.DRAG_OVER, dragOverFilter);
			scene.removeEventFilter(DragEvent.DRAG_DROPPED, dragDroppedFilter);
		}
		for (final IViewer viewer : getDomain().getViewers().values()) {
			viewer.viewerFocusedProperty()
						.removeListener(viewerFocusChangeListener);
		}
		super.doDeactivate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IOnDragDropHandler> getActiveHandlers(final IViewer viewer) {
		return (List<IOnDragDropHandler>) super.getActiveHandlers(viewer);
	}
}
