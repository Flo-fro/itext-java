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
package com.itextpdf.io.font.otf;

import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.TrueTypeFont;
import com.itextpdf.test.ExtendedITextTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class GposLookupType6Test extends ExtendedITextTest {

    private static final String RESOURCE_FOLDER = "./src/test/resources/com/itextpdf/io/font/otf/";

    private static final String FREE_SANS_FONT_PATH = RESOURCE_FOLDER + "FreeSans.ttf";

    @Test
    public void idxEqualToEndLineGpos6Test() throws IOException {
        TrueTypeFont fontProgram = (TrueTypeFont) FontProgramFactory.createFont(FREE_SANS_FONT_PATH);
        GlyphPositioningTableReader gposTableReader = fontProgram.getGposTable();
        GposLookupType6 lookup = new GposLookupType6(gposTableReader, 0, new int[0]);
        List<Glyph> glyphs = Arrays.asList(new Glyph(fontProgram.getGlyphByCode(445)),
                new Glyph(fontProgram.getGlyphByCode(394)));
        GlyphLine gl = new GlyphLine(glyphs);
        gl.setIdx(2);
        boolean transform = lookup.transformOne(gl);
        Assertions.assertFalse(transform);
    }

    @Test
    public void idxSmallerThanEndLineGpos6Test() throws IOException {
        TrueTypeFont font = new TrueTypeFont(FREE_SANS_FONT_PATH);

        GlyphPositioningTableReader gposTableReader = font.getGposTable();
        GposLookupType6 lookup = new GposLookupType6(gposTableReader, 0, new int[0]);


        List<Glyph> glyphs = Arrays.asList(new Glyph(font.getGlyphByCode(174)),
                new Glyph(font.getGlyphByCode(5)));
        GlyphLine gl = new GlyphLine(glyphs);
        gl.setIdx(0);

        boolean transform = lookup.transformOne(gl);
        Assertions.assertFalse(transform);
    }
}
