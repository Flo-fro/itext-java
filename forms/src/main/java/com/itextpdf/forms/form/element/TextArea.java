/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.forms.form.element;

import com.itextpdf.forms.form.renderer.TextAreaRenderer;
import com.itextpdf.forms.form.FormProperty;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.renderer.IRenderer;

/**
 * Extension of the {@link FormField} class representing a button so that
 * a {@link TextAreaRenderer} is used instead of the default renderer for fields.
 */
public class TextArea extends FormField<TextArea> implements IPlaceholderable {

    /**
     * The placeholder paragraph.
     */
    private Paragraph placeholder;

    /**
     * Creates a new {@link TextArea} instance.
     *
     * @param id the id
     */
    public TextArea(String id) {
        super(id);
    }

    /* (non-Javadoc)
     * @see FormField#getDefaultProperty(int)
     */
    @Override
    public <T1> T1 getDefaultProperty(int property) {
        switch (property) {
            case FormProperty.FORM_FIELD_ROWS:
                return (T1) (Object) 2;
            case FormProperty.FORM_FIELD_COLS:
                return (T1) (Object) 20;
            default:
                return super.<T1>getDefaultProperty(property);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Paragraph getPlaceholder() {
        return placeholder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPlaceholder(Paragraph placeholder) {
        this.placeholder = placeholder;
    }

    /* (non-Javadoc)
     * @see com.itextpdf.layout.element.AbstractElement#makeNewRenderer()
     */
    @Override
    protected IRenderer makeNewRenderer() {
        return new TextAreaRenderer(this);
    }
}