/*******************************************************************************
 * Copyright (c) 2019 itemis AG and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zoey Gerrit Prigge (itemis AG) - initial API and implementation (bug #541056)
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.gef.dot.internal.ui.conversion.DotFontUtil;
import org.eclipse.gef.dot.internal.ui.conversion.DotFontUtil.Font;

public class DotFontAccessMock implements DotFontUtil.SystemFontAccess {
	private final String[] availableFonts;

	public DotFontAccessMock(String... availableFonts) {
		this.availableFonts = availableFonts;
	}

	@Override
	public FakeFont font(String family) {
		if (Arrays.stream(availableFonts)
				.filter(availableFont -> availableFont.equalsIgnoreCase(family))
				.collect(Collectors.counting()) > 0) {
			return new FakeFont(family);
		}
		return getDefault();
	}

	@Override
	public FakeFont getDefault() {
		return new FakeFont("System");
	}

	class FakeFont implements DotFontUtil.Font {
		private final String family;

		FakeFont(String family) {
			this.family = family;
		}

		@Override
		public String getFamily() {
			return family;
		}

		@Override
		public boolean equals(Font font) {
			return font instanceof FakeFont
					&& family.equals(((FakeFont) font).family);
		}
	}
}
