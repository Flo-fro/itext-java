package com.itextpdf.core.pdf.navigation;

import com.itextpdf.core.pdf.PdfArray;
import com.itextpdf.core.pdf.PdfName;
import com.itextpdf.core.pdf.PdfNumber;
import com.itextpdf.core.pdf.PdfObject;
import com.itextpdf.core.pdf.PdfPage;

import java.util.HashMap;

public class PdfExplicitDestination extends PdfDestination<PdfArray> {

    public PdfExplicitDestination() {
        this(new PdfArray());
    }

    public PdfExplicitDestination(PdfArray pdfObject) {
        super(pdfObject);
    }

    @Override
    public PdfObject getDestinationPage(HashMap<Object, PdfObject> names) {
        return getPdfObject().get(0, false);
    }

    @Override
    public PdfDestination replaceNamedDestination(HashMap<Object, PdfObject> names) {
        return this;
    }

    static public PdfExplicitDestination createXYZ(PdfPage page, float left, float top, float zoom) {
        return create(page, PdfName.XYZ, left, Float.NaN, Float.NaN, top, zoom);
    }

    static public PdfExplicitDestination createXYZ(int pageNum, float left, float top, float zoom) {
        return create(pageNum, PdfName.XYZ, left, Float.NaN, Float.NaN, top, zoom);
    }

    static public PdfExplicitDestination createFit(PdfPage page) {
        return create(page, PdfName.Fit, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    static public PdfExplicitDestination createFit(int pageNum) {
        return create(pageNum, PdfName.Fit, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    static public PdfExplicitDestination createFitH(PdfPage page, float top) {
        return create(page, PdfName.FitH, Float.NaN, Float.NaN, Float.NaN, top, Float.NaN);
    }

    static public PdfExplicitDestination createFitH(int pageNum, float top) {
        return create(pageNum, PdfName.FitH, Float.NaN, Float.NaN, Float.NaN, top, Float.NaN);
    }

    static public PdfExplicitDestination createFitV(PdfPage page, float left) {
        return create(page, PdfName.FitV, left, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    static public PdfExplicitDestination createFitV(int pageNum, float left) {
        return create(pageNum, PdfName.FitV, left, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    static public PdfExplicitDestination createFitR(PdfPage page, float left, float bottom, float right, float top) {
        return create(page, PdfName.FitR, left, bottom, right, top, Float.NaN);
    }

    static public PdfExplicitDestination createFitR(int pageNum, float left, float bottom, float right, float top) {
        return create(pageNum, PdfName.FitR, left, bottom, right, top, Float.NaN);
    }

    static public PdfExplicitDestination createFitB(PdfPage page) {
        return create(page, PdfName.FitB, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    static public PdfExplicitDestination createFitB(int pageNum) {
        return create(pageNum, PdfName.FitB, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    static public PdfExplicitDestination createFitBH(PdfPage page, float top) {
        return create(page, PdfName.FitBH, Float.NaN, Float.NaN, Float.NaN, top, Float.NaN);
    }

    static public PdfExplicitDestination createFitBH(int pageNum, float top) {
        return create(pageNum, PdfName.FitBH, Float.NaN, Float.NaN, Float.NaN, top, Float.NaN);
    }

    static public PdfExplicitDestination createFitBV(PdfPage page, float left) {
        return create(page, PdfName.FitBH, left, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    static public PdfExplicitDestination createFitBV(int pageNum, float left) {
        return create(pageNum, PdfName.FitBH, left, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    static public PdfExplicitDestination create(PdfPage page, PdfName type, float left, float bottom, float right, float top, float zoom) {
        return new PdfExplicitDestination().add(page).add(type).add(left).add(bottom).add(right).add(top).add(zoom);
    }

    static public PdfExplicitDestination create(int pageNum, PdfName type, float left, float bottom, float right, float top, float zoom) {
        return new PdfExplicitDestination().add(--pageNum).add(type).add(left).add(bottom).add(right).add(top).add(zoom);
    }

    private PdfExplicitDestination add(float value) {
        if (!Float.isNaN(value))
            getPdfObject().add(new PdfNumber(value));
        return this;
    }

    private PdfExplicitDestination add(int value) {
        getPdfObject().add(new PdfNumber(value));
        return this;
    }

    private PdfExplicitDestination add(PdfPage page) {
        getPdfObject().add(page.getPdfObject());
        return this;
    }

    private PdfExplicitDestination add(PdfName type) {
        getPdfObject().add(type);
        return this;
    }

}
