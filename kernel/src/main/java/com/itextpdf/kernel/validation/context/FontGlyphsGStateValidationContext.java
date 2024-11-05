/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.kernel.validation.context;

import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.canvas.CanvasGraphicsState;
import com.itextpdf.kernel.validation.IValidationContext;
import com.itextpdf.kernel.validation.ValidationType;

/**
 * Class for canvas font glyphs validation context.
 */
public class FontGlyphsGStateValidationContext implements IValidationContext,
        IGraphicStateValidationParameter, IContentStreamValidationParameter {
    private final CanvasGraphicsState graphicsState;
    private final PdfStream contentStream;

    /**
     * Instantiates a new {@link FontGlyphsGStateValidationContext} based on canvas graphics state and content stream.
     *
     * @param graphicsState the canvas graphics state
     * @param contentStream the content stream
     */
    public FontGlyphsGStateValidationContext(CanvasGraphicsState graphicsState, PdfStream contentStream) {
        this.graphicsState = graphicsState;
        this.contentStream = contentStream;
    }

    @Override
    public PdfStream getContentStream() {
        return contentStream;
    }

    @Override
    public CanvasGraphicsState getGraphicsState() {
        return graphicsState;
    }

    @Override
    public ValidationType getType() {
        return ValidationType.FONT_GLYPHS;
    }
}