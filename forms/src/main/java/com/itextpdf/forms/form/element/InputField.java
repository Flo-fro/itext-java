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

import com.itextpdf.forms.form.renderer.InputFieldRenderer;
import com.itextpdf.forms.form.FormProperty;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.renderer.IRenderer;

/**
 * Extension of the {@link FormField} class representing a button so that
 * a {@link InputFieldRenderer} is used.
 */
public class InputField extends FormField<InputField> implements IPlaceholderable {

    /**
     * Creates a new input field.
     *
     * @param id the id
     */
    public InputField(String id) {
        super(id);
    }

    /**
     * The placeholder paragraph.
     */
    private Paragraph placeholder;

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
         * @see FormField#getDefaultProperty(int)
         */
    @Override
    public <T1> T1 getDefaultProperty(int property) {
        switch (property) {
            case FormProperty.FORM_FIELD_PASSWORD_FLAG:
                return (T1) (Object) false;
            case FormProperty.FORM_FIELD_SIZE:
                return (T1) (Object) 20;
            default:
                return super.<T1>getDefaultProperty(property);
        }
    }

    /* (non-Javadoc)
     * @see com.itextpdf.layout.element.AbstractElement#makeNewRenderer()
     */
    @Override
    protected IRenderer makeNewRenderer() {
        return new InputFieldRenderer(this);
    }
}