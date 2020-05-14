/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef.mvc.fx.parts;

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.fx.utils.NodeUtils;
import org.eclipse.gef.geometry.planar.ICurve;
import org.eclipse.gef.geometry.planar.IGeometry;

import com.google.common.reflect.TypeToken;
import com.google.inject.Provider;

import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;

/**
 * The {@link HoverFeedbackPart} is an {@link AbstractFeedbackPart} that is
 * parameterized by <code>GeometryNode&lt;IGeometry&gt;</code>.
 *
 * @author mwienand
 *
 */
public class HoverFeedbackPart
		extends AbstractFeedbackPart<GeometryNode<IGeometry>> {

	private Provider<? extends IGeometry> feedbackGeometryProvider;

	/**
	 * Default constructor.
	 */
	public HoverFeedbackPart() {
	}

	@Override
	protected GeometryNode<IGeometry> doCreateVisual() {
		GeometryNode<IGeometry> visual = new GeometryNode<>();
		visual.setFill(Color.TRANSPARENT);
		visual.setMouseTransparent(true);
		visual.setManaged(false);
		visual.setStrokeType(StrokeType.OUTSIDE);
		visual.setStrokeWidth(1);

		// hover specific
		visual.setEffect(getHoverFeedbackEffect());
		visual.setStroke(getHoverStroke());

		return visual;
	}

	@Override
	public void doRefreshVisual(GeometryNode<IGeometry> visual) {
		if (getAnchoragesUnmodifiable().size() != 1) {
			return;
		}

		IGeometry feedbackGeometry = getFeedbackGeometry();
		if (feedbackGeometry == null) {
			return;
		}

		// FIXME: Investigate why the StrokeType needs to be set before setting
		// the geometry in order to prevent a vertical offset.

		if (feedbackGeometry instanceof ICurve) {
			// stroke centered
			visual.setStrokeType(StrokeType.CENTERED);
		} else {
			// stroke outside
			visual.setStrokeType(StrokeType.OUTSIDE);
		}

		visual.setGeometry(feedbackGeometry);
	}

	/**
	 * Returns the {@link IGeometry} that is provided by this part's
	 * {@link #setGeometryProvider(Provider) geometry provider}.
	 *
	 * @return The {@link IGeometry} that is provided by this part's geometry
	 *         provider.
	 */
	protected IGeometry getFeedbackGeometry() {
		return NodeUtils.sceneToLocal(getVisual().getParent(),
				feedbackGeometryProvider.get());
	}

	/**
	 * Returns the {@link Effect} that is provided by the
	 * <code>Provider&lt;Effect&gt;</code> of this part's first anchorage.
	 *
	 * @return The {@link Effect} that is provided by the
	 *         <code>Provider&lt;Effect&gt;</code> of this part's first
	 *         anchorage.
	 */
	public Effect getHoverFeedbackEffect() {
		DropShadow effect = new DropShadow();
		effect.setRadius(3);
		return effect;
	}

	/**
	 * Returns the {@link Color} that is used to stroke hover feedback.
	 *
	 * @return The {@link Color} that is used to stroke hover feedback.
	 */
	@SuppressWarnings("serial")
	protected Color getHoverStroke() {
		Provider<Color> hoverFeedbackColorProvider = getViewer()
				.getAdapter(AdapterKey.get(new TypeToken<Provider<Color>>() {
				}, DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_COLOR_PROVIDER));
		return hoverFeedbackColorProvider == null
				? DefaultHoverFeedbackPartFactory.DEFAULT_HOVER_FEEDBACK_COLOR
				: hoverFeedbackColorProvider.get();
	}

	/**
	 * Sets the <code>Provider&lt;IGeometry&gt;</code> of this part to the given
	 * value.
	 *
	 * @param geometryProvider
	 *            The new <code>Provider&lt;IGeometry&gt;</code> for this part.
	 */
	public void setGeometryProvider(
			Provider<? extends IGeometry> geometryProvider) {
		feedbackGeometryProvider = geometryProvider;
	}
}
