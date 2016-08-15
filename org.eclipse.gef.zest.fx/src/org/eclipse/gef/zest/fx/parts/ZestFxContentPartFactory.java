/*******************************************************************************
 * Copyright (c) 2014, 2016 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API & implementation
 *
 *******************************************************************************/
package org.eclipse.gef.zest.fx.parts;

import java.util.Map;

import org.eclipse.gef.graph.Edge;
import org.eclipse.gef.graph.Graph;
import org.eclipse.gef.mvc.behaviors.IBehavior;
import org.eclipse.gef.mvc.parts.IContentPart;
import org.eclipse.gef.mvc.parts.IContentPartFactory;
import org.eclipse.gef.zest.fx.ZestProperties;

import com.google.inject.Inject;
import com.google.inject.Injector;

import javafx.scene.Node;
import javafx.util.Pair;

/**
 * The {@link ZestFxContentPartFactory} is a {@link Graph}-specific
 * {@link IContentPartFactory}. It creates {@link GraphPart}s,
 * {@link NodePart}s, and {@link EdgePart}s for the corresponding
 * {@link Graph}s, {@link Node}s, and {@link Edge}s.
 *
 * @author mwienand
 *
 */
public class ZestFxContentPartFactory implements IContentPartFactory<Node> {

	@Inject
	private Injector injector;

	@SuppressWarnings("rawtypes")
	@Override
	public IContentPart<Node, ? extends Node> createContentPart(Object content, IBehavior<Node> contextBehavior,
			Map<Object, Object> contextMap) {
		IContentPart<Node, ? extends Node> part = null;
		if (content instanceof Graph) {
			part = new GraphPart();
		} else if (content instanceof org.eclipse.gef.graph.Node) {
			part = new NodePart();
		} else if (content instanceof Edge) {
			part = new EdgePart();
		} else if (content instanceof Pair && ((Pair) content).getKey() instanceof Edge
				&& (ZestProperties.LABEL__NE.equals(((Pair) content).getValue())
						|| ZestProperties.EXTERNAL_LABEL__NE.equals(((Pair) content).getValue())
						|| ZestProperties.SOURCE_LABEL__E.equals(((Pair) content).getValue())
						|| ZestProperties.TARGET_LABEL__E.equals(((Pair) content).getValue()))) {
			part = new EdgeLabelPart();
		} else if (content instanceof Pair && ((Pair) content).getKey() instanceof org.eclipse.gef.graph.Node
				&& ZestProperties.EXTERNAL_LABEL__NE.equals(((Pair) content).getValue())) {
			part = new NodeLabelPart();
		}
		if (part != null) {
			injector.injectMembers(part);
		}
		return part;
	}

}