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

import com.itextpdf.forms.form.FormProperty;
import com.itextpdf.layout.element.AbstractElement;

/**
 * Implementation of the {@link AbstractElement} class for form fields.
 *
 * @param <T> the generic type of the form field (e.g. input field, button, text area)
 */
public abstract class FormField<T extends IFormField> extends AbstractElement<T> implements IFormField {

    /** The id. */
    private final String id;

    /**
     * Instantiates a new {@link FormField} instance.
     *
     * @param id the id
     */
    FormField(String id) {
        if (id == null || id.contains(".")) {
            throw new IllegalArgumentException("id should not contain '.'");
        }
        this.id = id;
    }

    /* (non-Javadoc)
     * @see IFormField#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.itextpdf.layout.ElementPropertyContainer#getDefaultProperty(int)
     */
    @Override
    public <T1> T1 getDefaultProperty(int property) {
        switch (property) {
            case FormProperty.FORM_FIELD_FLATTEN:
                return (T1) (Object) true;
            case FormProperty.FORM_FIELD_VALUE:
                return (T1) (Object) "";
            default:
                return super.<T1>getDefaultProperty(property);
        }
    }

    /**
     * Set the form field to be interactive and added into Acroform instead of drawing it on a page.
     *
     * @param interactive {@code true} if the form field element shall be added into Acroform, {@code false} otherwise.
     *                By default, the form field element is not interactive and drawn on a page.
     * @return this same {@link FormField} instance.
     */
    public FormField<T> setInteractive(boolean interactive) {
        setProperty(FormProperty.FORM_FIELD_FLATTEN, !interactive);
        return this;
    }
}