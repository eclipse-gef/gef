/*******************************************************************************
 * Copyright (c) 2013, 2015 Fabian Steeg and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Steeg                - initial API and implementation (bug #372365)
 *     Alexander Nyßen (itemis AG) - refactoring of builder API (bug #480293)
 *
 *******************************************************************************/
package org.eclipse.gef4.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.gef4.common.attributes.IAttributeStore;
import org.eclipse.gef4.common.beans.property.ReadOnlyMapWrapperEx;
import org.eclipse.gef4.graph.Graph.Builder.Context;

import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * An {@link Edge} represents a (directed) connection between two {@link Node}s
 * in a {@link Graph}.
 *
 * @author Fabian Steeg
 * @author anyssen
 *
 */
public class Edge implements IAttributeStore {

	/**
	 * The {@link Builder} can be used to construct an {@link Edge} little by
	 * little.
	 */
	public static class Builder {

		private Graph.Builder.Context context;
		private Map<String, Object> attrs = new HashMap<>();
		private Object sourceNodeOrKey;
		private Object targetNodeOrKey;

		/**
		 * Constructs a new (anonymous) {@link Edge.Builder} for the given
		 * {@link Context}.
		 *
		 * @param context
		 *            The context in which the {@link Edge.Builder} is used.
		 * @param sourceNodeOrKey
		 *            The source {@link Node} or a key to identify the source
		 *            {@link Node} (or its {@link Node.Builder}).
		 *
		 * @param targetNodeOrKey
		 *            The target {@link Node} or a key to identify the target
		 *            {@link Node} (or its {@link Node.Builder}).
		 */
		public Builder(Graph.Builder.Context context, Object sourceNodeOrKey, Object targetNodeOrKey) {
			this.context = context;
			if (context != null) {
				context.edgeBuilders.add(this);
			}
			this.sourceNodeOrKey = sourceNodeOrKey;
			this.targetNodeOrKey = targetNodeOrKey;
		}

		/**
		 * Constructs a new (anonymous) context-free {@link Edge.Builder}, which
		 * can only be used to construct a single edge via {@link #buildEdge()},
		 * i.e. which cannot be chained.
		 *
		 * @param sourceNode
		 *            The source {@link Node}.
		 *
		 * @param targetNode
		 *            The target {@link Node}.
		 */
		public Builder(Node sourceNode, Node targetNode) {
			this(null, sourceNode, targetNode);
		}

		/**
		 * Puts the given <i>key</i>-<i>value</i>-pair into the
		 * {@link Edge#attributesProperty() attributesProperty map} of the
		 * {@link Edge} which is constructed by this {@link Builder}.
		 *
		 * @param key
		 *            The attribute name which is inserted.
		 * @param value
		 *            The attribute value which is inserted.
		 * @return <code>this</code> for convenience.
		 */
		public Edge.Builder attr(String key, Object value) {
			attrs.put(key, value);
			return this;
		}

		/**
		 * Constructs a new {@link Graph} from the values which have been
		 * supplied to the builder chain.
		 *
		 * @return A new {@link Graph}.
		 */
		public Graph build() {
			if (context == null) {
				throw new IllegalStateException("The builder is not chained.");
			}
			return context.builder.build();
		}

		/**
		 * Creates a new {@link Edge}, setting the values specified via this
		 * {@link Edge.Builder}.
		 *
		 * @return A newly created {@link Edge}.
		 */
		public Edge buildEdge() {
			if (context == null && !(sourceNodeOrKey instanceof Node)) {
				throw new IllegalStateException("May only use builder keys in case of chained builders.");
			}
			if (context == null && !(targetNodeOrKey instanceof Node)) {
				throw new IllegalStateException("May only use builder keys in case of chained builders.");
			}
			Node sourceNode = sourceNodeOrKey instanceof Node ? (Node) sourceNodeOrKey
					: context.builder.findOrCreateNode(sourceNodeOrKey);
			Node targetNode = targetNodeOrKey instanceof Node ? (Node) targetNodeOrKey
					: context.builder.findOrCreateNode(targetNodeOrKey);

			if (sourceNode == null) {
				throw new IllegalArgumentException("Could not resolve source node (key='" + sourceNodeOrKey + "').");
			}
			if (targetNode == null) {
				throw new IllegalArgumentException("Could not resolve target node (key='" + targetNodeOrKey + "').");
			}

			return new Edge(attrs, sourceNode, targetNode);

		}

		/**
		 * Constructs a new {@link Edge.Builder}.
		 *
		 * @param sourceNodeOrKey
		 *            The source {@link Node} or a key to identify the source
		 *            {@link Node} (or its {@link Node.Builder}).
		 *
		 * @param targetNodeOrKey
		 *            The target {@link Node} or a key to identify the target
		 *            {@link Node} (or its {@link Node.Builder}).
		 *
		 * @return A new {@link Edge.Builder}.
		 */
		public Edge.Builder edge(Object sourceNodeOrKey, Object targetNodeOrKey) {
			Edge.Builder eb = new Edge.Builder(context, sourceNodeOrKey, targetNodeOrKey);
			return eb;
		}

		/**
		 * Constructs a new (anonymous) {@link Node.Builder} for a node.
		 *
		 * @return A {@link Node.Builder}.
		 */
		public Node.Builder node() {
			Node.Builder nb = new Node.Builder(context);
			return nb;
		}

		/**
		 * Constructs a new (identifiable) {@link Node.Builder} for a node.
		 *
		 * @param key
		 *            The key that can be used to identify the
		 *            {@link Node.Builder}
		 *
		 * @return A {@link Node.Builder}.
		 */
		public Node.Builder node(Object key) {
			Node.Builder nb = new Node.Builder(context, key);
			return nb;
		}
	}

	private final ReadOnlyMapWrapper<String, Object> attributesProperty = new ReadOnlyMapWrapperEx<>(this,
			ATTRIBUTES_PROPERTY, FXCollections.<String, Object> observableHashMap());
	private Node source;
	private Node target;
	private Graph graph; // associated graph

	/**
	 * Constructs a new {@link Edge} which connects the given <i>source</i>
	 * {@link Node} with the given <i>target</i> {@link Node}. The given
	 * <i>attributesProperty</i> are copied into the
	 * {@link #attributesProperty() attributesProperty map} of this {@link Edge}
	 * .
	 *
	 * @param attributes
	 *            A {@link Map} containing the attributesProperty which are
	 *            copied into the {@link #attributesProperty()
	 *            attributesProperty map} of this {@link Edge}.
	 * @param source
	 *            The source {@link Node} for this {@link Edge}.
	 * @param target
	 *            The target {@link Node} for this {@link Edge}.
	 */
	public Edge(Map<String, Object> attributes, Node source, Node target) {
		this.attributesProperty.putAll(attributes);
		this.source = source;
		this.target = target;
	}

	/**
	 * Constructs a new {@link Edge} which connects the given <i>source</i>
	 * {@link Node} with the given <i>target</i> {@link Node}.
	 *
	 * @param source
	 *            The source {@link Node} for this {@link Edge}.
	 * @param target
	 *            The target {@link Node} for this {@link Edge}.
	 */
	public Edge(Node source, Node target) {
		this(new HashMap<String, Object>(), source, target);
	}

	@Override
	public ReadOnlyMapProperty<String, Object> attributesProperty() {
		return attributesProperty.getReadOnlyProperty();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Edge)) {
			return false;
		}
		Edge otherEdge = (Edge) other;
		// XXX: In JavaFX 2.2, a property's equals() falls back to equality of
		// the enclosing bean; to prevent a StackOverflowError here, we fall
		// back comparing the observed map value instead.
		boolean attrsEqual = attributesProperty.get() == null ? false
				: attributesProperty().get().equals(((Node) other).attributesProperty());
		boolean sourceEqual = this.getSource().equals(otherEdge.getSource());
		boolean targetEqual = this.getTarget().equals(otherEdge.getTarget());
		return attrsEqual && sourceEqual && targetEqual;
	}

	@Override
	public ObservableMap<String, Object> getAttributes() {
		return attributesProperty.get();
	}

	/**
	 * Returns the {@link Graph} to which this {@link Edge} belongs.
	 *
	 * @return The {@link Graph} to which this {@link Edge} belongs.
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Returns the source {@link Node} of this {@link Edge}.
	 *
	 * @return The source {@link Node} of this {@link Edge}.
	 */
	public Node getSource() {
		return source;
	}

	/**
	 * Returns the target {@link Node} of this {@link Edge}.
	 *
	 * @return The target {@link Node} of this {@link Edge}.
	 */
	public Node getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		int result = 17;
		// XXX: In JavaFX 2.2, hashCode() falls back on the hash code of the
		// enclosing bean; to prevent a StackOverflowError, we fall back to the
		// hash code of the contained map value, rather than the property itself
		result = 31 * result + (attributesProperty.get() == null ? 0 : attributesProperty.get().hashCode());
		result = 31 * result + getSource().hashCode();
		result = 31 * result + getTarget().hashCode();
		return result;
	}

	/**
	 * Sets the {@link Graph} to which this {@link Edge} belongs to the given
	 * value.
	 *
	 * @param graph
	 *            The new {@link Graph} for this {@link Edge}.
	 */
	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Sets the source {@link Node} of this {@link Edge} to the given value.
	 *
	 * @param source
	 *            The new source {@link Node} for this {@link Edge}.
	 */
	public void setSource(Node source) {
		this.source = source;
	}

	/**
	 * Sets the target {@link Node} of this {@link Edge} to the given value.
	 *
	 * @param target
	 *            The new target {@link Node} for this {@link Edge}.
	 */
	public void setTarget(Node target) {
		this.target = target;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Edge {");
		boolean separator = false;
		TreeMap<String, Object> sortedAttrs = new TreeMap<>();
		sortedAttrs.putAll(attributesProperty);
		for (Object attrKey : sortedAttrs.keySet()) {
			if (separator) {
				sb.append(", ");
			} else {
				separator = true;
			}
			sb.append(attrKey.toString() + " : " + attributesProperty.get(attrKey));
		}
		sb.append("} from " + getSource() + " to " + getTarget());
		return sb.toString();
	}

}
