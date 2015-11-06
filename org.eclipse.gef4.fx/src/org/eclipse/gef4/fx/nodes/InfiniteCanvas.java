/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *     Alexander Nyßen (itemis AG)  - refactorings and cleanups
 *
 *******************************************************************************/
package org.eclipse.gef4.fx.nodes;

import java.util.Arrays;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.util.Duration;

/**
 * An {@link InfiniteCanvas} provides a means to render a portion of a
 * hypothetically infinite canvas, on which arbitrary contents can be placed.
 * <p>
 *
 * <pre>
 * +----------------+
 * |content area    |
 * |                |
 * |         +----------------+
 * |         |visible area    |
 * |         |                |
 * |         +----------------+
 * |                |
 * +----------------+
 * </pre>
 * <p>
 * The size of the {@link InfiniteCanvas} itself determines the visible area,
 * i.e. it is reflected in its {@link #layoutBoundsProperty()}. The content area
 * is determined by the (visible) bounds of the {@link #getContentGroup()} that
 * contains the content elements. These bounds can be accessed via the
 * {@link #contentBoundsProperty()}.
 * <p>
 * By default, scrollbars are shown when the content area exceeds the visible
 * area. They allow to navigate the {@link #scrollableBoundsProperty()}, which
 * resembles the union of the content area and the visible area. The horizontal
 * and vertical scroll offsets are controlled by the
 * {@link #horizontalScrollOffsetProperty()} and
 * {@link #verticalScrollOffsetProperty()}. The appearance of scrollbars can be
 * controlled with the following properties:
 * <ul>
 * <li>The {@link #horizontalScrollBarPolicyProperty()} determines the
 * horizontal {@link ScrollBarPolicy}.
 * <li>The {@link #verticalScrollBarPolicyProperty()} determines the vertical
 * {@link ScrollBarPolicy}.
 * </ul>
 * <p>
 * An arbitrary transformation can be applied to the contents that is controlled
 * by the {@link #contentTransformProperty()}. It is unrelated to scrolling,
 * i.e. translating the content does not change the scroll offset.
 * <p>
 * A background grid is rendered behind the contents per default. It always
 * covers the complete visible area and can be enabled/disabled and customized
 * via a set of properties:
 * <ul>
 * <li>The {@link #showGridProperty()} determines whether or not to show the
 * background grid
 * <li>The {@link #zoomGridProperty()} determines whether or not to zoom the
 * background grid with the contents.
 * <li>The {@link #gridCellWidthProperty()} determines the grid cell width.
 * <li>The {@link #gridCellHeightProperty()} determines the grid cell height.
 * </ul>
 * <p>
 * Internally, an {@link InfiniteCanvas} consists of four layers:
 *
 * <pre>
 * +--------------------------------+
 * |scrollbar group                 |
 * +--------------------------------+
 * |overlay group                   |
 * +--------------------------------+
 * |scrolled pane (with sub-layers) |
 * +--------------------------------+
 * |underlay group                  |
 * +--------------------------------+
 * </pre>
 * <ul>
 * <li>The {@link #getUnderlayGroup()} is rendered at the bottom, it is neither
 * affected by the {@link #horizontalScrollOffsetProperty()} and
 * {@link #verticalScrollOffsetProperty()} nor by the
 * {@link #contentTransformProperty()}.
 * <li>The {@link #getScrolledPane()} is rendered above the
 * {@link #getUnderlayGroup()} and contains sub-layers. The
 * {@link #getScrolledPane()} and its sub-layers are affected by the
 * {@link #horizontalScrollOffsetProperty()} and
 * {@link #verticalScrollOffsetProperty()}.
 * <li>The {@link #getOverlayGroup()} is rendered above the
 * {@link #getScrolledPane()}. It is neither affected by the
 * {@link #horizontalScrollOffsetProperty()} and
 * {@link #verticalScrollOffsetProperty()} nor by the
 * {@link #contentTransformProperty()}.
 * <li>The {@link #getScrollBarGroup()} is rendered above the
 * {@link #getOverlayGroup()}. It contains the scrollbars.
 * </ul>
 * The {@link #getScrolledPane()} internally consists of the following four
 * sub-layers:
 *
 * <pre>
 * +--------------------------------+
 * |scrolled overlay group          |
 * +--------------------------------+
 * |content group                   |
 * +--------------------------------+
 * |scrolled underlay group         |
 * +--------------------------------+
 * |grid canvas                     |
 * +--------------------------------+
 * </pre>
 * <ul>
 * <li>The {@link #getGridCanvas()} is rendered at the bottom of the
 * {@link #getScrolledPane()}.
 * <li>The {@link #getScrolledUnderlayGroup()} is rendered above the
 * {@link #getGridCanvas()}.
 * <li>The {@link #getContentGroup()} is rendered above the
 * {@link #getScrolledUnderlayGroup()}. It is affected by the
 * {@link #contentTransformProperty()}.
 * <li>The {@link #getScrolledOverlayGroup()} is rendered above the
 * {@link #getContentGroup()}.
 * </ul>
 */
public class InfiniteCanvas extends Region {

	/**
	 * The {@link GridCanvas} is a {@link Canvas} that draws grid points at
	 * configurable steps. The grid points are scaled according to a
	 * configurable transformation.
	 */
	public class GridCanvas extends Canvas {
		private static final int GRID_THRESHOLD = 5000000;

		/**
		 * Constructs a new {@link GridCanvas}.
		 */
		public GridCanvas() {
			final ChangeListener<Number> repaintListener = new ChangeListener<Number>() {
				@Override
				public void changed(
						final ObservableValue<? extends Number> observable,
						final Number oldValue, final Number newValue) {
					repaintGrid();
				}
			};
			Affine gridTransform = gridTransformProperty.get();
			gridTransform.txProperty().addListener(repaintListener);
			gridTransform.tyProperty().addListener(repaintListener);
			gridTransform.mxxProperty().addListener(repaintListener);
			gridTransform.myyProperty().addListener(repaintListener);

			gridCellWidthProperty.addListener(repaintListener);
			gridCellHeightProperty.addListener(repaintListener);

			layoutXProperty().addListener(repaintListener);
			layoutYProperty().addListener(repaintListener);
			widthProperty().addListener(repaintListener);
			heightProperty().addListener(repaintListener);
		}

		@Override
		public boolean isResizable() {
			return true;
		}

		@Override
		public double prefHeight(final double width) {
			return getHeight();
		}

		@Override
		public double prefWidth(final double height) {
			return getWidth();
		}

		/**
		 * Repaints the grid points on this {@link GridCanvas}. This method is
		 * called when the canvas size, the transformation, or the grid cell
		 * size changes.
		 */
		protected void repaintGrid() {
			final double width = getWidth();
			final double height = getHeight();

			final GraphicsContext gc = getGraphicsContext2D();
			gc.clearRect(0, 0, width, height);

			final double xScale = gridTransformProperty.get().getMxx();
			final double yScale = gridTransformProperty.get().getMyy();
			// don't paint grid points if size is to large
			if (((width / xScale) * (height / yScale) > GRID_THRESHOLD)) {
				return;
			}

			final double scaledGridCellWidth = gridCellWidthProperty.get()
					* xScale;
			final double scaledGridCellHeight = gridCellHeightProperty.get()
					* yScale;
			gc.setFill(Color.GREY);
			for (double x = -(getLayoutX()
					- gridTransformProperty.get().getTx())
					% scaledGridCellWidth; x < width; x += scaledGridCellWidth) {
				for (double y = -(getLayoutY()
						- gridTransformProperty.get().getTy())
						% scaledGridCellHeight; y < height; y += scaledGridCellHeight) {
					gc.fillRect(Math.floor(x) - 0.5 * xScale,
							Math.floor(y) - 0.5 * yScale, xScale, yScale);
				}
			}
		}
	}

	// background grid
	private GridCanvas gridCanvas;
	private final DoubleProperty gridCellHeightProperty = new SimpleDoubleProperty(
			10);
	private final DoubleProperty gridCellWidthProperty = new SimpleDoubleProperty(
			10);
	private final ReadOnlyObjectWrapper<Affine> gridTransformProperty = new ReadOnlyObjectWrapper<Affine>(
			new Affine());
	private final BooleanProperty showGridProperty = new SimpleBooleanProperty(
			true);
	private final BooleanProperty zoomGridProperty = new SimpleBooleanProperty(
			true);

	// scrollbars
	private Group scrollBarGroup;
	private ScrollBar horizontalScrollBar;
	private ScrollBar verticalScrollBar;
	private final ObjectProperty<ScrollBarPolicy> horizontalScrollBarPolicyProperty = new SimpleObjectProperty<ScrollBarPolicy>(
			ScrollBarPolicy.AS_NEEDED);
	private final ObjectProperty<ScrollBarPolicy> verticalScrollBarPolicyProperty = new SimpleObjectProperty<ScrollBarPolicy>(
			ScrollBarPolicy.AS_NEEDED);

	// contents
	private Group contentGroup = new Group();
	private ReadOnlyObjectWrapper<Affine> contentTransformProperty = new ReadOnlyObjectWrapper<Affine>(
			new Affine());

	// content and scrollable bounds
	private double[] contentBounds = new double[] { 0d, 0d, 0d, 0d };
	private double[] scrollableBounds = new double[] { 0d, 0d, 0d, 0d };
	private ObjectBinding<Bounds> contentBoundsBinding = new ObjectBinding<Bounds>() {
		@Override
		protected Bounds computeValue() {
			return new BoundingBox(contentBounds[0], contentBounds[1],
					contentBounds[2] - contentBounds[0],
					contentBounds[3] - contentBounds[1]);
		}
	};
	private ObjectBinding<Bounds> scrollableBoundsBinding = new ObjectBinding<Bounds>() {
		@Override
		protected Bounds computeValue() {
			return new BoundingBox(scrollableBounds[0], scrollableBounds[1],
					scrollableBounds[2] - scrollableBounds[0],
					scrollableBounds[3] - scrollableBounds[1]);
		}
	};
	private ReadOnlyObjectWrapper<Bounds> contentBoundsProperty = new ReadOnlyObjectWrapper<Bounds>();
	private ReadOnlyObjectWrapper<Bounds> scrollableBoundsProperty = new ReadOnlyObjectWrapper<Bounds>();

	// layers within the visualization
	private Pane scrolledPane = new Pane();
	private Group underlayGroup = new Group();
	private Group scrolledUnderlayGroup = new Group();
	private Group scrolledOverlayGroup = new Group();
	private Group overlayGroup = new Group();

	// Listener to update the scrollbars in response to Number changes (e.g.
	// width and height).
	private ChangeListener<Number> updateScrollBarsOnSizeChangeListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> observable,
				Number oldHeight, Number newHeight) {
			updateScrollBars();
		}
	};

	// Listener to update the scrollbars in response to Bounds changes (e.g.
	// scrolled pane bounds and content group bounds).
	private ChangeListener<Bounds> updateScrollBarsOnBoundsChangeListener = new ChangeListener<Bounds>() {
		@Override
		public void changed(ObservableValue<? extends Bounds> observable,
				Bounds oldBounds, Bounds newBounds) {
			updateScrollBars();
		}
	};

	// Listener to update the scrollbars in response to ScrollBarPolicy
	// changes.
	private ChangeListener<ScrollBarPolicy> updateScrollBarsOnPolicyChangeListener = new ChangeListener<ScrollBarPolicy>() {
		@Override
		public void changed(
				ObservableValue<? extends ScrollBarPolicy> observable,
				ScrollBarPolicy oldValue, ScrollBarPolicy newValue) {
			updateScrollBars();
		}
	};

	/**
	 * Constructs a new {@link InfiniteCanvas}.
	 */
	public InfiniteCanvas() {
		// bind bounds properties to predefined bindings
		contentBoundsProperty.bind(contentBoundsBinding);
		scrollableBoundsProperty.bind(scrollableBoundsBinding);

		// create scrollbars and grid canvas
		scrollBarGroup = createScrollBarGroup();
		gridCanvas = createGridCanvas();

		// create visualization
		getChildren().addAll(createLayers());
		getScrolledPane().getChildren().addAll(createScrolledLayers());

		// add content transformation to content group
		getContentGroup().getTransforms().add(getContentTransform());

		// register listeners for updating the scrollbars
		registerUpdateScrollBarsOnBoundsChanges();
		registerUpdateScrollBarsOnSizeChanges();
		registerUpdateScrollBarsOnPolicyChanges();

		// enable the background grid
		if (showGridProperty.get()) {
			showGrid();
		}
		// register for "showGrid" changes to enable/disable the grid
		showGridProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					showGrid();
				} else {
					hideGrid();
				}
			}
		});

		// enable grid zooming
		if (showGridProperty.get()) {
			zoomGrid();
		}
		// register for "zoomGrid" changes to enable/disable grid zooming
		zoomGridProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					zoomGrid();
				} else {
					unzoomGrid();
				}
			}
		});
	}

	/**
	 * Computes the bounds <code>[min-x, min-y, max-x, max-y]</code> surrounding
	 * the {@link #getContentGroup() content group} within the coordinate system
	 * of this {@link InfiniteCanvas}.
	 *
	 * @return The bounds <code>[min-x, min-y, max-x, max-y]</code> surrounding
	 *         the {@link #getContentGroup() content group} within the
	 *         coordinate system of this {@link InfiniteCanvas}.
	 */
	protected double[] computeContentBoundsInLocal() {
		Bounds contentBoundsInScrolledPane = getContentGroup()
				.getBoundsInParent();
		double minX = contentBoundsInScrolledPane.getMinX();
		double maxX = contentBoundsInScrolledPane.getMaxX();
		double minY = contentBoundsInScrolledPane.getMinY();
		double maxY = contentBoundsInScrolledPane.getMaxY();

		Point2D minInScrolled = getScrolledPane().localToParent(minX, minY);
		double realMinX = minInScrolled.getX();
		double realMinY = minInScrolled.getY();
		double realMaxX = realMinX + (maxX - minX);
		double realMaxY = realMinY + (maxY - minY);

		return new double[] { realMinX, realMinY, realMaxX, realMaxY };
	}

	/**
	 * Converts a horizontal translation distance into the corresponding
	 * horizontal scrollbar value.
	 *
	 * @param tx
	 *            The horizontal translation distance.
	 * @return The horizontal scrollbar value corresponding to the given
	 *         translation.
	 */
	protected double computeHv(double tx) {
		return lerp(horizontalScrollBar.getMin(), horizontalScrollBar.getMax(),
				norm(scrollableBounds[0], scrollableBounds[2] - getWidth(),
						-tx));
	}

	/**
	 * Computes and returns the bounds of the scrollable area within this
	 * {@link InfiniteCanvas}.
	 *
	 * @return The bounds of the scrollable area, i.e.
	 *         <code>[minx, miny, maxx, maxy]</code>.
	 */
	protected double[] computeScrollableBoundsInLocal() {
		double[] cb = Arrays.copyOf(contentBounds, contentBounds.length);
		Bounds db = getContentGroup().getBoundsInParent();

		// factor in the viewport extending the content bounds
		if (cb[0] < 0) {
			cb[0] = 0;
		}
		if (cb[1] < 0) {
			cb[1] = 0;
		}
		if (cb[2] > getWidth()) {
			cb[2] = 0;
		} else {
			cb[2] = getWidth() - cb[2];
		}
		if (cb[3] > getHeight()) {
			cb[3] = 0;
		} else {
			cb[3] = getHeight() - cb[3];
		}

		return new double[] { db.getMinX() - cb[0], db.getMinY() - cb[1],
				db.getMaxX() + cb[2], db.getMaxY() + cb[3] };
	}

	/**
	 * Converts a horizontal scrollbar value into the corresponding horizontal
	 * translation distance.
	 *
	 * @param hv
	 *            The horizontal scrollbar value.
	 * @return The horizontal translation distance corresponding to the given
	 *         scrollbar value.
	 */
	protected double computeTx(double hv) {
		return -lerp(scrollableBounds[0], scrollableBounds[2] - getWidth(),
				norm(horizontalScrollBar.getMin(), horizontalScrollBar.getMax(),
						hv));
	}

	/**
	 * Converts a vertical scrollbar value into the corresponding vertical
	 * translation distance.
	 *
	 * @param vv
	 *            The vertical scrollbar value.
	 * @return The vertical translation distance corresponding to the given
	 *         scrollbar value.
	 */
	protected double computeTy(double vv) {
		return -lerp(scrollableBounds[1], scrollableBounds[3] - getHeight(),
				norm(verticalScrollBar.getMin(), verticalScrollBar.getMax(),
						vv));
	}

	/**
	 * Converts a vertical translation distance into the corresponding vertical
	 * scrollbar value.
	 *
	 * @param ty
	 *            The vertical translation distance.
	 * @return The vertical scrollbar value corresponding to the given
	 *         translation.
	 */
	protected double computeVv(double ty) {
		return lerp(verticalScrollBar.getMin(), verticalScrollBar.getMax(),
				norm(scrollableBounds[1], scrollableBounds[3] - getHeight(),
						-ty));
	}

	/**
	 * Provides the visual bounds of the content group in the local coordinate
	 * system of this {@link InfiniteCanvas}
	 *
	 * @return The bounds of the content group, i.e.
	 *         <code>minx, miny, maxx, maxy</code>.
	 */
	public ReadOnlyObjectProperty<Bounds> contentBoundsProperty() {
		return contentBoundsProperty.getReadOnlyProperty();
	}

	/**
	 * Returns the viewport transform as a (read-only) property.
	 *
	 * @return The viewport transform as {@link ReadOnlyObjectProperty}.
	 */
	public ReadOnlyObjectProperty<Affine> contentTransformProperty() {
		return contentTransformProperty.getReadOnlyProperty();
	}

	/**
	 * Creates the {@link GridCanvas} that is used to paint the background grid.
	 *
	 * @return The newly created {@link GridCanvas} that is used to paint the
	 *         background grid.
	 */
	protected GridCanvas createGridCanvas() {
		return new GridCanvas();
	}

	/**
	 * Returns a list containing the top level layers in the visualization of
	 * this {@link InfiniteCanvas}. Per default, the underlay group, the
	 * scrolled pane, the overlay group, and the scrollbar group are returned in
	 * that order.
	 *
	 * @return A list containing the top level layers in the visualization of
	 *         this {@link InfiniteCanvas}.
	 */
	protected List<? extends Node> createLayers() {
		return Arrays.asList(getUnderlayGroup(), getScrolledPane(),
				getOverlayGroup(), getScrollBarGroup());
	}

	/**
	 * Creates the {@link Group} designated for holding the scrollbars and
	 * places the scrollbars in it. Furthermore, event listeners are registered
	 * to update the scroll offset upon scrollbar movement.
	 *
	 * @return The {@link Group} designated for holding the scrollbars.
	 */
	protected Group createScrollBarGroup() {
		// create horizontal scrollbar
		horizontalScrollBar = new ScrollBar();
		horizontalScrollBar.setVisible(false);
		horizontalScrollBar.setOpacity(0.5);

		// create vertical scrollbar
		verticalScrollBar = new ScrollBar();
		verticalScrollBar.setOrientation(Orientation.VERTICAL);
		verticalScrollBar.setVisible(false);
		verticalScrollBar.setOpacity(0.5);

		// bind horizontal size
		DoubleBinding vWidth = new DoubleBinding() {
			{
				bind(verticalScrollBar.visibleProperty(),
						verticalScrollBar.widthProperty());
			}

			@Override
			protected double computeValue() {
				return verticalScrollBar.isVisible()
						? verticalScrollBar.getWidth() : 0;
			}
		};
		horizontalScrollBar.prefWidthProperty()
				.bind(widthProperty().subtract(vWidth));

		// bind horizontal y position
		horizontalScrollBar.layoutYProperty().bind(heightProperty()
				.subtract(horizontalScrollBar.heightProperty()));

		// bind vertical size
		DoubleBinding hHeight = new DoubleBinding() {
			{
				bind(horizontalScrollBar.visibleProperty(),
						horizontalScrollBar.heightProperty());
			}

			@Override
			protected double computeValue() {
				return horizontalScrollBar.isVisible()
						? horizontalScrollBar.getHeight() : 0;
			}
		};
		verticalScrollBar.prefHeightProperty()
				.bind(heightProperty().subtract(hHeight));

		// bind vertical x position
		verticalScrollBar.layoutXProperty().bind(
				widthProperty().subtract(verticalScrollBar.widthProperty()));

		// fade in/out on mouse enter/exit
		registerFadeInOutTransitions(horizontalScrollBar);
		registerFadeInOutTransitions(verticalScrollBar);

		// update scrollable bounds on mouse press
		EventHandler<MouseEvent> mousePressFilter = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				updateScrollBars();
			}
		};
		horizontalScrollBar.addEventFilter(MouseEvent.MOUSE_PRESSED,
				mousePressFilter);
		verticalScrollBar.addEventFilter(MouseEvent.MOUSE_PRESSED,
				mousePressFilter);

		// translate on scroll
		horizontalScrollBar.valueProperty()
				.addListener(new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						if (horizontalScrollBar.isVisible()) {
							getScrolledPane().setTranslateX(
									computeTx(newValue.doubleValue()));
						}
					}
				});
		verticalScrollBar.valueProperty()
				.addListener(new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						if (verticalScrollBar.isVisible()) {
							getScrolledPane().setTranslateY(
									computeTy(newValue.doubleValue()));
						}
					}
				});

		// update scrollbars on mouse release
		EventHandler<MouseEvent> mouseReleasedHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				updateScrollBars();
			}
		};
		horizontalScrollBar.setOnMouseReleased(mouseReleasedHandler);
		verticalScrollBar.setOnMouseReleased(mouseReleasedHandler);

		return new Group(horizontalScrollBar, verticalScrollBar);
	}

	/**
	 * Returns a list containing the scrolled layers in the visualization of
	 * this {@link InfiniteCanvas}. Per default, the grid canvas, the scrolled
	 * underlay group, the content group, and the scrolled overlay group are
	 * returned in that order.
	 *
	 * @return A list containing the top level layers in the visualization of
	 *         this {@link InfiniteCanvas}.
	 */
	protected List<? extends Node> createScrolledLayers() {
		return Arrays.asList(getGridCanvas(), getScrolledUnderlayGroup(),
				getContentGroup(), getScrolledOverlayGroup());
	}

	/**
	 * Returns the value of the {@link #contentBoundsProperty()}.
	 *
	 * @return The value of the {@link #contentBoundsProperty()}.
	 */
	public Bounds getContentBounds() {
		return contentBoundsProperty.get();
	}

	/**
	 * Returns the {@link Group} designated for holding the scrolled content.
	 *
	 * @return The {@link Group} designated for holding the scrolled content.
	 */
	public Group getContentGroup() {
		return contentGroup;
	}

	/**
	 * Returns the transformation that is applied to the
	 * {@link #getContentGroup() content group}.
	 *
	 * @return The transformation that is applied to the
	 *         {@link #getContentGroup() content group}.
	 */
	public Affine getContentTransform() {
		return contentTransformProperty.get();
	}

	/**
	 * Returns the {@link GridCanvas} that is used to paint the background grid.
	 *
	 * @return The {@link GridCanvas} that is used to paint the background grid.
	 */
	protected GridCanvas getGridCanvas() {
		return gridCanvas;
	}

	/**
	 * Returns the value of the {@link #gridCellHeightProperty()}.
	 *
	 * @return The value of the {@link #gridCellHeightProperty()}.
	 */
	public double getGridCellHeight() {
		return gridCellHeightProperty.get();
	}

	/**
	 * Returns the value of the {@link #gridCellWidthProperty()}.
	 *
	 * @return The value of the {@link #gridCellWidthProperty()}.
	 */
	public double getGridCellWidth() {
		return gridCellWidthProperty.get();
	}

	/**
	 * Returns the horizontal {@link ScrollBar}, or <code>null</code> if the
	 * horizontal {@link ScrollBar} was not yet created.
	 *
	 * @return The horizontal {@link ScrollBar}.
	 */
	protected ScrollBar getHorizontalScrollBar() {
		return horizontalScrollBar;
	}

	/**
	 * Returns the {@link ScrollBarPolicy} that is currently used to decide when
	 * to show a horizontal scrollbar.
	 *
	 * @return The {@link ScrollBarPolicy} that is currently used to decide when
	 *         to show a horizontal scrollbar.
	 */
	public ScrollBarPolicy getHorizontalScrollBarPolicy() {
		return horizontalScrollBarPolicyProperty.get();
	}

	/**
	 * Returns the current horizontal scroll offset.
	 *
	 * @return The current horizontal scroll offset.
	 */
	public double getHorizontalScrollOffset() {
		return getScrolledPane().getTranslateX();
	}

	/**
	 * Returns the overlay {@link Group} that is rendered above the contents but
	 * below the scrollbars.
	 *
	 * @return The overlay {@link Group} that is rendered above the contents but
	 *         below the scrollbars.
	 */
	public Group getOverlayGroup() {
		return overlayGroup;
	}

	/**
	 * Returns the {@link Group} designated for holding the {@link ScrollBar}s.
	 *
	 * @return The {@link Group} designated for holding the {@link ScrollBar}s.
	 */
	protected Group getScrollBarGroup() {
		return scrollBarGroup;
	}

	/**
	 * Returns the scrolled overlay {@link Group}.
	 *
	 * @return The scrolled overlay {@link Group}.
	 */
	public Group getScrolledOverlayGroup() {
		return scrolledOverlayGroup;
	}

	/**
	 * Returns the {@link Pane} which is translated when scrolling. This
	 * {@link Pane} contains the {@link #getContentGroup()}, therefore, the
	 * {@link #getContentTransform()} does not influence the scroll offset.
	 *
	 * @return The {@link Pane} that is translated when scrolling.
	 */
	protected Pane getScrolledPane() {
		return scrolledPane;
	}

	/**
	 * Returns the scrolled underlay {@link Group}.
	 *
	 * @return The scrolled underlay {@link Group}.
	 */
	public Group getScrolledUnderlayGroup() {
		return scrolledUnderlayGroup;
	}

	/**
	 * Returns the underlay {@link Group}.
	 *
	 * @return The underlay {@link Group}.
	 */
	public Group getUnderlayGroup() {
		return underlayGroup;
	}

	/**
	 * Returns the vertical {@link ScrollBar}, or <code>null</code> if the
	 * vertical {@link ScrollBar} was not yet created.
	 *
	 * @return The vertical {@link ScrollBar}.
	 */
	protected ScrollBar getVerticalScrollBar() {
		return verticalScrollBar;
	}

	/**
	 * Returns the {@link ScrollBarPolicy} that is currently used to decide when
	 * to show a vertical scrollbar.
	 *
	 * @return The {@link ScrollBarPolicy} that is currently used to decide when
	 *         to show a vertical scrollbar.
	 */
	public ScrollBarPolicy getVerticalScrollBarPolicy() {
		return verticalScrollBarPolicyProperty.get();
	}

	/**
	 * Returns the current vertical scroll offset.
	 *
	 * @return The current vertical scroll offset.
	 */
	public double getVerticalScrollOffset() {
		return getScrolledPane().getTranslateY();
	}

	/**
	 * Returns the grid cell height as a (writable) property.
	 *
	 * @return The grid cell height as a {@link DoubleProperty}.
	 */
	public DoubleProperty gridCellHeightProperty() {
		return gridCellHeightProperty;
	}

	/**
	 * Returns the grid cell width as a (writable) property.
	 *
	 * @return The grid cell width as a {@link DoubleProperty}.
	 */
	public DoubleProperty gridCellWidthProperty() {
		return gridCellWidthProperty;
	}

	/**
	 * Disables the background grid.
	 */
	protected void hideGrid() {
		gridCanvas.setVisible(false);
		gridCanvas.layoutXProperty().unbind();
		gridCanvas.layoutYProperty().unbind();
		gridCanvas.widthProperty().unbind();
		gridCanvas.heightProperty().unbind();
	}

	/**
	 * Returns the {@link ObjectProperty} that controls the
	 * {@link ScrollBarPolicy} that decides when to show a horizontal scrollbar.
	 *
	 * @return The {@link ObjectProperty} that controls the
	 *         {@link ScrollBarPolicy} that decides when to show a horizontal
	 *         scrollbar.
	 */
	public ObjectProperty<ScrollBarPolicy> horizontalScrollBarPolicyProperty() {
		return horizontalScrollBarPolicyProperty;
	}

	/**
	 * Returns the horizontal scroll offset as a property.
	 *
	 * @return A {@link DoubleProperty} representing the horizontal scroll
	 *         offset.
	 */
	public DoubleProperty horizontalScrollOffsetProperty() {
		return getScrolledPane().translateXProperty();
	}

	/**
	 * Returns the value of the {@link #showGridProperty()}.
	 *
	 * @return The value of the {@link #showGridProperty()}.
	 */
	public boolean isShowGrid() {
		return showGridProperty.get();
	}

	/**
	 * Returns the value of the {@link #zoomGridProperty()}.
	 *
	 * @return The value of the {@link #zoomGridProperty()}.
	 */
	public boolean isZoomGrid() {
		return zoomGridProperty.get();
	}

	/**
	 * Linear interpolation between <i>min</i> and <i>max</i> at the given
	 * <i>ratio</i>. Returns the interpolated value in the interval
	 * <code>[min;max]</code>.
	 *
	 * @param min
	 *            The lower interval bound.
	 * @param max
	 *            The upper interval bound.
	 * @param ratio
	 *            A value in the interval <code>[0;1]</code>.
	 * @return The interpolated value.
	 */
	protected double lerp(double min, double max, double ratio) {
		double d = (1 - ratio) * min + ratio * max;
		return Double.isNaN(d) ? 0 : Math.min(max, Math.max(min, d));
	}

	/**
	 * Normalizes a given <i>value</i> which is in range <code>[min;max]</code>
	 * to range <code>[0;1]</code>.
	 *
	 * @param min
	 *            The lower bound of the range.
	 * @param max
	 *            The upper bound of the range.
	 * @param value
	 *            The value in the range.
	 * @return The normalized value (in range <code>[0;1]</code>).
	 */
	protected double norm(double min, double max, double value) {
		double d = (value - min) / (max - min);
		return Double.isNaN(d) ? 0 : Math.min(1, Math.max(0, d));
	}

	/**
	 * Registers fade in/out transitions for the given {@link Node}. The
	 * transitions are used when the mouse enters/exits the node.
	 *
	 * @param node
	 *            The {@link Node} to which fade in/out transitions are added
	 *            upon mouse enter/exit.
	 */
	protected void registerFadeInOutTransitions(final Node node) {
		// create transitions
		final FadeTransition fadeInTransition = new FadeTransition(
				Duration.millis(200), node);
		fadeInTransition.setToValue(1.0);
		final FadeTransition fadeOutTransition = new FadeTransition(
				Duration.millis(200), node);
		fadeOutTransition.setToValue(0.5);

		// create actions
		final Runnable fadeIn = new Runnable() {
			@Override
			public void run() {
				fadeOutTransition.stop();
				fadeInTransition.playFromStart();
			}
		};
		final Runnable fadeOut = new Runnable() {
			@Override
			public void run() {
				fadeInTransition.stop();
				fadeOutTransition.playFromStart();
			}
		};

		// register event handlers
		node.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				fadeIn.run();
			}
		});
		node.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (!node.isPressed()) {
					fadeOut.run();
				}
			}
		});
		node.pressedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (oldValue.booleanValue() && !newValue.booleanValue()) {
					fadeOut.run();
				}
			}
		});
	}

	/**
	 * Registers listeners on the bounds-in-local property of the
	 * {@link #getScrolledPane()} and on the bounds-in-parent property of the
	 * {@link #getContentGroup()} that will call {@link #updateScrollBars()}
	 * when one of the bounds is changed.
	 */
	protected void registerUpdateScrollBarsOnBoundsChanges() {
		getScrolledPane().boundsInLocalProperty()
				.addListener(updateScrollBarsOnBoundsChangeListener);
		getContentGroup().boundsInParentProperty()
				.addListener(updateScrollBarsOnBoundsChangeListener);
	}

	/**
	 * Registers listeners on the {@link #horizontalScrollBarPolicyProperty()}
	 * and on the {@link #verticalScrollBarPolicyProperty()} that will call
	 * {@link #updateScrollBars()} when one of the {@link ScrollBarPolicy}s
	 * changes.
	 */
	protected void registerUpdateScrollBarsOnPolicyChanges() {
		horizontalScrollBarPolicyProperty
				.addListener(updateScrollBarsOnPolicyChangeListener);
		verticalScrollBarPolicyProperty
				.addListener(updateScrollBarsOnPolicyChangeListener);
	}

	/**
	 * Registers listeners on the {@link #widthProperty()} and on the
	 * {@link #heightProperty()} that will call {@link #updateScrollBars()} when
	 * the size of this {@link InfiniteCanvas} changes.
	 */
	protected void registerUpdateScrollBarsOnSizeChanges() {
		widthProperty().addListener(updateScrollBarsOnSizeChangeListener);
		heightProperty().addListener(updateScrollBarsOnSizeChangeListener);
	}

	/**
	 * Ensures that the specified child {@link Node} is visible to the user by
	 * scrolling to its position. The effect and style of the node are taken
	 * into consideration. After revealing a node, it will be fully visible, if
	 * it fits within the current viewport bounds.
	 * <p>
	 * When the child node's left side is left to the viewport, it will touch
	 * the left border of the viewport after revealing. When the child node's
	 * right side is right to the viewport, it will touch the right border of
	 * the viewport after revealing. When the child node's top side is above the
	 * viewport, it will touch the top border of the viewport after revealing.
	 * When the child node's bottom side is below the viewport, it will touch
	 * the bottom border of the viewport after revealing.
	 * <p>
	 * TODO: When the child node does not fit within the viewport bounds, it is
	 * not revealed.
	 *
	 * @param child
	 *            The child {@link Node} to reveal.
	 */
	public void reveal(Node child) {
		Bounds bounds = sceneToLocal(
				child.localToScene(child.getBoundsInLocal()));
		if (bounds.getHeight() <= getHeight()) {
			if (bounds.getMinY() < 0) {
				setVerticalScrollOffset(
						getVerticalScrollOffset() - bounds.getMinY());
			} else if (bounds.getMaxY() > getHeight()) {
				setVerticalScrollOffset(getVerticalScrollOffset() + getHeight()
						- bounds.getMaxY());
			}
		}

		if (bounds.getWidth() <= getWidth()) {
			if (bounds.getMinX() < 0) {
				setHorizontalScrollOffset(
						getHorizontalScrollOffset() - bounds.getMinX());
			} else if (bounds.getMaxX() > getWidth()) {
				setHorizontalScrollOffset(getHorizontalScrollOffset()
						+ getWidth() - bounds.getMaxX());
			}
		}
	}

	/**
	 * Returns the bounds of the scrollable area in local coordinates of this
	 * {@link InfiniteCanvas}. The scrollable area corresponds to the visual
	 * bounds of the content group, which is expanded to cover at least the area
	 * of this {@link InfiniteCanvas} (i.e. the viewport) if necessary. It is
	 * thereby also the area that can be navigated via the scroll bars.
	 *
	 * @return The bounds of the scrollable area, i.e.
	 *         <code>minx, miny, maxx, maxy</code>.
	 */
	public ReadOnlyObjectProperty<Bounds> scrollableBoundsProperty() {
		return scrollableBoundsProperty.getReadOnlyProperty();
	}

	/**
	 * Sets the transformation matrix of the {@link #getContentTransform()
	 * viewport transform} to the values specified by the given {@link Affine}.
	 *
	 * @param tx
	 *            The {@link Affine} determining the new
	 *            {@link #getContentTransform() viewport transform}.
	 */
	public void setContentTransform(Affine tx) {
		Affine viewportTransform = contentTransformProperty.get();
		viewportTransform.setMxx(tx.getMxx());
		viewportTransform.setMxy(tx.getMxy());
		viewportTransform.setMyx(tx.getMyx());
		viewportTransform.setMyy(tx.getMyy());
		viewportTransform.setTx(tx.getTx());
		viewportTransform.setTy(tx.getTy());
		updateScrollBars();
	}

	/**
	 * Assigns the given value to the {@link #gridCellHeightProperty()}.
	 *
	 * @param gridCellHeight
	 *            The grid cell height that is assigned to the
	 *            {@link #gridCellHeightProperty()}.
	 */
	public void setGridCellHeight(double gridCellHeight) {
		gridCellHeightProperty.set(gridCellHeight);
	}

	/**
	 * Assigns the given value to the {@link #gridCellWidthProperty()}.
	 *
	 * @param gridCellWidth
	 *            The grid cell width that is assigned to the
	 *            {@link #gridCellWidthProperty()}.
	 */
	public void setGridCellWidth(double gridCellWidth) {
		gridCellWidthProperty.set(gridCellWidth);
	}

	/**
	 * Sets the value of the {@link #horizontalScrollBarPolicyProperty()} to the
	 * given {@link ScrollBarPolicy}.
	 *
	 * @param horizontalScrollBarPolicy
	 *            The new {@link ScrollBarPolicy} for the horizontal scrollbar.
	 */
	public void setHorizontalScrollBarPolicy(
			ScrollBarPolicy horizontalScrollBarPolicy) {
		horizontalScrollBarPolicyProperty.set(horizontalScrollBarPolicy);
	}

	/**
	 * Sets the horizontal scroll offset to the given value.
	 *
	 * @param scrollOffsetX
	 *            The new horizontal scroll offset.
	 * @throws IllegalArgumentException
	 *             when the given horizontal scroll offset is outside of the
	 *             value range of the horizontal scrollbar.
	 */
	public void setHorizontalScrollOffset(double scrollOffsetX) {
		double hv = computeHv(scrollOffsetX);
		if (hv < horizontalScrollBar.getMin()
				|| hv > horizontalScrollBar.getMax()) {
			throw new IllegalArgumentException(
					"Horizontal scrolling offset outside range ["
							+ horizontalScrollBar.getMin() + ";"
							+ horizontalScrollBar.getMax() + "]");
		}
		getScrolledPane().setTranslateX(scrollOffsetX);
	}

	/**
	 * Assigns the given value to the {@link #showGridProperty()}.
	 *
	 * @param showGrid
	 *            The new value that is assigned to the
	 *            {@link #showGridProperty()}.
	 */
	public void setShowGrid(boolean showGrid) {
		showGridProperty.set(showGrid);
	}

	/**
	 * Sets the value of the {@link #verticalScrollBarPolicyProperty()} to the
	 * given {@link ScrollBarPolicy}.
	 *
	 * @param verticalScrollBarPolicy
	 *            The new {@link ScrollBarPolicy} for the vertical scrollbar.
	 */
	public void setVerticalScrollBarPolicy(
			ScrollBarPolicy verticalScrollBarPolicy) {
		verticalScrollBarPolicyProperty.set(verticalScrollBarPolicy);
	}

	/**
	 * Sets the vertical scroll offset to the given value.
	 *
	 * @param scrollOffsetY
	 *            The new vertical scroll offset.
	 * @throws IllegalArgumentException
	 *             when the given vertical scroll offset is outside of the value
	 *             range of the {@link #getVerticalScrollBar() vertical
	 *             scrollbar}.
	 */
	public void setVerticalScrollOffset(double scrollOffsetY) {
		double vv = computeVv(scrollOffsetY);
		if (vv < verticalScrollBar.getMin()
				|| vv > verticalScrollBar.getMax()) {
			throw new IllegalArgumentException(
					"Vertical scrolling offset outside range ["
							+ verticalScrollBar.getMin() + ";"
							+ verticalScrollBar.getMax() + "]");
		}
		getScrolledPane().setTranslateY(scrollOffsetY);
	}

	/**
	 * Assigns the given value to the {@link #showGridProperty()}.
	 *
	 * @param zoomGrid
	 *            The new value that is assigned to the
	 *            {@link #showGridProperty()}.
	 */
	public void setZoomGrid(boolean zoomGrid) {
		zoomGridProperty.set(zoomGrid);
	}

	/**
	 * Enables the background grid.
	 */
	protected void showGrid() {
		gridCanvas.setVisible(true);
		gridCanvas.layoutXProperty().bind(new DoubleBinding() {
			{
				super.bind(scrollableBoundsProperty);
			}

			@Override
			protected double computeValue() {
				return Math.min(0, scrollableBoundsProperty.get().getMinX());
			}
		});
		gridCanvas.layoutYProperty().bind(new DoubleBinding() {
			{
				super.bind(scrollableBoundsProperty);
			}

			@Override
			protected double computeValue() {
				return Math.min(0, scrollableBoundsProperty.get().getMinY());
			}
		});
		gridCanvas.widthProperty().bind(new DoubleBinding() {
			{
				super.bind(scrollableBoundsProperty);
			}

			@Override
			protected double computeValue() {
				if (scrollableBoundsProperty.get() == null) {
					return 0;
				}
				return scrollableBoundsProperty.get().getWidth();
			}
		});
		gridCanvas.heightProperty().bind(new DoubleBinding() {
			{
				super.bind(scrollableBoundsProperty);
			}

			@Override
			protected double computeValue() {
				if (scrollableBoundsProperty.get() == null) {
					return 0;
				}
				return scrollableBoundsProperty.get().getHeight();
			}
		});
	}

	/**
	 * Returns the {@link BooleanProperty} that determines if a background grid
	 * is shown within this {@link InfiniteCanvas}.
	 *
	 * @return The {@link BooleanProperty} that determines if a background grid
	 *         is shown within this {@link InfiniteCanvas}.
	 */
	public BooleanProperty showGridProperty() {
		return showGridProperty;
	}

	/**
	 * Disables zooming of the background grid.
	 *
	 * @see #zoomGrid()
	 * @see #zoomGridProperty()
	 */
	protected void unzoomGrid() {
		gridTransformProperty.unbind();
		gridTransformProperty.set(new Affine());
	}

	/**
	 * Updates the {@link ScrollBar}s' visibilities, value ranges and value
	 * increments based on the {@link #computeContentBoundsInLocal() content
	 * bounds} and the {@link #computeScrollableBoundsInLocal() scrollable
	 * bounds}. The update is not done if any of the {@link ScrollBar}s is
	 * currently in use.
	 */
	protected void updateScrollBars() {
		// do not update while a scrollbar is pressed, so that the scrollable
		// area does not change while using a scrollbar
		if (horizontalScrollBar.isPressed() || verticalScrollBar.isPressed()) {
			return;
		}

		// determine current content bounds
		double[] oldContentBounds = Arrays.copyOf(contentBounds,
				contentBounds.length);
		contentBounds = computeContentBoundsInLocal();
		if (!Arrays.equals(oldContentBounds, contentBounds)) {
			contentBoundsBinding.invalidate();
		}

		// show/hide horizontal scrollbar
		ScrollBarPolicy hbarPolicy = horizontalScrollBarPolicyProperty.get();
		boolean hbarIsNeeded = contentBounds[0] < 0
				|| contentBounds[2] > getWidth();
		if (hbarPolicy.equals(ScrollBarPolicy.ALWAYS)
				|| hbarPolicy.equals(ScrollBarPolicy.AS_NEEDED)
						&& hbarIsNeeded) {
			horizontalScrollBar.setVisible(true);
		} else {
			horizontalScrollBar.setVisible(false);
		}

		// show/hide vertical scrollbar
		ScrollBarPolicy vbarPolicy = verticalScrollBarPolicyProperty.get();
		boolean vbarIsNeeded = contentBounds[1] < 0
				|| contentBounds[3] > getHeight();
		if (vbarPolicy.equals(ScrollBarPolicy.ALWAYS)
				|| vbarPolicy.equals(ScrollBarPolicy.AS_NEEDED)
						&& vbarIsNeeded) {
			verticalScrollBar.setVisible(true);
		} else {
			verticalScrollBar.setVisible(false);
		}

		// determine current scrollable bounds
		double[] oldScrollableBounds = Arrays.copyOf(scrollableBounds,
				scrollableBounds.length);
		scrollableBounds = computeScrollableBoundsInLocal();
		if (!Arrays.equals(oldScrollableBounds, scrollableBounds)) {
			scrollableBoundsBinding.invalidate();
		}

		// update scrollbar ranges
		horizontalScrollBar.setMin(scrollableBounds[0]);
		horizontalScrollBar.setMax(scrollableBounds[2]);
		horizontalScrollBar.setVisibleAmount(getWidth());
		horizontalScrollBar.setBlockIncrement(getWidth() / 2);
		horizontalScrollBar.setUnitIncrement(getWidth() / 10);
		verticalScrollBar.setMin(scrollableBounds[1]);
		verticalScrollBar.setMax(scrollableBounds[3]);
		verticalScrollBar.setVisibleAmount(getHeight());
		verticalScrollBar.setBlockIncrement(getHeight() / 2);
		verticalScrollBar.setUnitIncrement(getHeight() / 10);

		// compute scrollbar values from canvas translation (in case the
		// scrollbar values are incorrect)
		horizontalScrollBar
				.setValue(computeHv(getScrolledPane().getTranslateX()));
		verticalScrollBar
				.setValue(computeVv(getScrolledPane().getTranslateY()));
	}

	/**
	 * Returns the {@link ObjectProperty} that controls the
	 * {@link ScrollBarPolicy} that decides when to show a vertical scrollbar.
	 *
	 * @return The {@link ObjectProperty} that controls the
	 *         {@link ScrollBarPolicy} that decides when to show a vertical
	 *         scrollbar.
	 */
	public ObjectProperty<ScrollBarPolicy> verticalScrollBarPolicyProperty() {
		return horizontalScrollBarPolicyProperty;
	}

	/**
	 * Returns the vertical scroll offset as a property.
	 *
	 * @return A {@link DoubleProperty} representing the vertical scroll offset.
	 */
	public DoubleProperty verticalScrollOffsetProperty() {
		return getScrolledPane().translateYProperty();
	}

	/**
	 * Enables zooming of the background grid when the contents are zoomed.
	 */
	protected void zoomGrid() {
		gridTransformProperty.bind(contentTransformProperty);
	}

	/**
	 * Returns the {@link BooleanProperty} that determines if the background
	 * grid is zoomed when the contents are zoomed.
	 *
	 * @return The {@link BooleanProperty} that determines if the background
	 *         grid is zoomed when the contents are zoomed.
	 */
	public BooleanProperty zoomGridProperty() {
		return zoomGridProperty;
	}

}
