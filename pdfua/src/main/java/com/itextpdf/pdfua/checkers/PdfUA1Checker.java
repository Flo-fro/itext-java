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
package com.itextpdf.pdfua.checkers;

import com.itextpdf.commons.datastructures.Tuple2;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.IsoKey;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfCatalog;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.tagging.PdfMcr;
import com.itextpdf.kernel.pdf.tagging.PdfNamespace;
import com.itextpdf.kernel.pdf.tagging.PdfStructTreeRoot;
import com.itextpdf.kernel.pdf.tagging.StandardRoles;
import com.itextpdf.kernel.pdf.tagutils.IRoleMappingResolver;
import com.itextpdf.kernel.pdf.tagutils.TagStructureContext;
import com.itextpdf.kernel.pdf.tagutils.TagTreeIterator;
import com.itextpdf.kernel.utils.IValidationChecker;
import com.itextpdf.kernel.utils.ValidationContext;
import com.itextpdf.kernel.utils.checkers.FontCheckUtil;
import com.itextpdf.pdfua.checkers.utils.FormulaCheckUtil;
import com.itextpdf.kernel.xmp.XMPConst;
import com.itextpdf.kernel.xmp.XMPException;
import com.itextpdf.kernel.xmp.XMPMeta;
import com.itextpdf.kernel.xmp.XMPMetaFactory;
import com.itextpdf.pdfua.checkers.utils.GraphicsCheckUtil;
import com.itextpdf.pdfua.checkers.utils.LayoutCheckUtil;
import com.itextpdf.pdfua.checkers.utils.PdfUAValidationContext;
import com.itextpdf.pdfua.checkers.utils.headings.HeadingsChecker;
import com.itextpdf.pdfua.checkers.utils.tables.TableCheckUtil;
import com.itextpdf.pdfua.exceptions.PdfUAConformanceException;
import com.itextpdf.pdfua.exceptions.PdfUAExceptionMessageConstants;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * The class defines the requirements of the PDF/UA-1 standard.
 * <p>
 * The specification implemented by this class is ISO 14289-1
 */
public class PdfUA1Checker implements IValidationChecker {

    private final PdfDocument pdfDocument;

    private final TagStructureContext tagStructureContext;

    private final HeadingsChecker headingsChecker;

    private final PdfUAValidationContext context;
    /**
     * Creates PdfUA1Checker instance with PDF document which will be validated against PDF/UA-1 standard.
     *
     * @param pdfDocument the document to validate
     */
    public PdfUA1Checker(PdfDocument pdfDocument) {
        this.pdfDocument = pdfDocument;
        this.tagStructureContext = new TagStructureContext(pdfDocument);
        this.context = new PdfUAValidationContext(pdfDocument);
        this.headingsChecker = new HeadingsChecker(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateDocument(ValidationContext validationContext) {
        checkCatalog(validationContext.getPdfDocument().getCatalog());
        checkStructureTreeRoot(validationContext.getPdfDocument().getStructTreeRoot());
        checkFonts(validationContext.getFonts());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateObject(Object obj, IsoKey key, PdfResources resources, PdfStream contentStream, Object extra) {
        switch (key) {
            case LAYOUT:
                new LayoutCheckUtil(context).checkRenderer(obj);
                headingsChecker.checkLayoutElement(obj);
                break;
            case CANVAS_WRITING_CONTENT:
                checkOnWritingCanvasToContent(obj);
                break;
            case CANVAS_BEGIN_MARKED_CONTENT:
                checkOnOpeningBeginMarkedContent(obj, extra);
                break;
            case FONT:
                checkText((String) obj, (PdfFont) extra);
                break;
        }
    }

    private void checkText(String str, PdfFont font) {
        if (!FontCheckUtil.doesFontContainAllUsedGlyphs(str, font)) {
            throw new PdfUAConformanceException(
                    PdfUAExceptionMessageConstants.EMBEDDED_FONTS_SHALL_DEFINE_ALL_REFERENCED_GLYPHS);
        }
    }

    protected void checkMetadata(PdfCatalog catalog) {
        if (catalog.getDocument().getPdfVersion().compareTo(PdfVersion.PDF_1_7) > 0) {
            throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.INVALID_PDF_VERSION);
        }

        PdfObject pdfMetadata = catalog.getPdfObject().get(PdfName.Metadata);
        if (pdfMetadata == null || !pdfMetadata.isStream()) {
            throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.DOCUMENT_SHALL_CONTAIN_XMP_METADATA_STREAM);
        }
        byte[] metaBytes = ((PdfStream) pdfMetadata).getBytes();

        try {
            XMPMeta metadata = XMPMetaFactory.parseFromBuffer(metaBytes);
            Integer part = metadata.getPropertyInteger(XMPConst.NS_PDFUA_ID, XMPConst.PART);
            if (!Integer.valueOf(1).equals(part)) {
                throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.METADATA_SHALL_CONTAIN_UA_VERSION_IDENTIFIER);
            }
            if (metadata.getProperty(XMPConst.NS_DC, XMPConst.TITLE) == null) {
                throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.METADATA_SHALL_CONTAIN_DC_TITLE_ENTRY);
            }
        } catch (XMPException e) {
            throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.DOCUMENT_SHALL_CONTAIN_XMP_METADATA_STREAM, e);
        }
    }

    private void checkViewerPreferences(PdfCatalog catalog) {
        PdfDictionary viewerPreferences = catalog.getPdfObject().getAsDictionary(PdfName.ViewerPreferences);
        if (viewerPreferences == null) {
            throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.MISSING_VIEWER_PREFERENCES);
        }
        PdfObject displayDocTitle = viewerPreferences.get(PdfName.DisplayDocTitle);
        if (!(displayDocTitle instanceof PdfBoolean)) {
            throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.MISSING_VIEWER_PREFERENCES);
        }
        if (PdfBoolean.FALSE.equals(displayDocTitle)) {
            throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.VIEWER_PREFERENCES_IS_FALSE);
        }

    }

    private void checkOnWritingCanvasToContent(Object data) {
        Stack<Tuple2<PdfName, PdfDictionary>> tagStack = getTagStack(data);
        if (tagStack.isEmpty()) {
            throw new PdfUAConformanceException(
                    PdfUAExceptionMessageConstants.TAG_HASNT_BEEN_ADDED_BEFORE_CONTENT_ADDING);
        }

        final boolean insideRealContent = isInsideRealContent(tagStack);
        final boolean insideArtifact = isInsideArtifact(tagStack);
        if (insideRealContent && insideArtifact) {
            throw new PdfUAConformanceException(
                    PdfUAExceptionMessageConstants.REAL_CONTENT_INSIDE_ARTIFACT_OR_VICE_VERSA);
        } else if (!insideRealContent && !insideArtifact) {
            throw new PdfUAConformanceException(
                    PdfUAExceptionMessageConstants.CONTENT_IS_NOT_REAL_CONTENT_AND_NOT_ARTIFACT);
        }
    }

    private Stack<Tuple2<PdfName, PdfDictionary>> getTagStack(Object data) {
        return (Stack<Tuple2<PdfName, PdfDictionary>>) data;
    }

    private void checkOnOpeningBeginMarkedContent(Object obj, Object extra) {
        Tuple2<PdfName, PdfDictionary> currentBmc = (Tuple2<PdfName, PdfDictionary>) extra;
        checkStandardRoleMapping(currentBmc);

        Stack<Tuple2<PdfName, PdfDictionary>> stack = getTagStack(obj);
        if (stack.isEmpty()) {
            return;
        }

        boolean isRealContent = isRealContent(currentBmc);
        boolean isArtifact = PdfName.Artifact.equals(currentBmc.getFirst());

        if (isArtifact && isInsideRealContent(stack)) {
            throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.ARTIFACT_CANT_BE_INSIDE_REAL_CONTENT);
        }
        if (isRealContent && isInsideArtifact(stack)) {
            throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.REAL_CONTENT_CANT_BE_INSIDE_ARTIFACT);
        }
    }

    private void checkStandardRoleMapping(Tuple2<PdfName, PdfDictionary> tag) {
        final PdfNamespace namespace = tagStructureContext.getDocumentDefaultNamespace();
        final String role = tag.getFirst().getValue();
        if (!StandardRoles.ARTIFACT.equals(role) && !tagStructureContext.checkIfRoleShallBeMappedToStandardRole(role,
                namespace)) {
            throw new PdfUAConformanceException(
                    MessageFormatUtil.format(
                            PdfUAExceptionMessageConstants.TAG_MAPPING_DOESNT_TERMINATE_WITH_STANDARD_TYPE, role));
        }
    }

    private boolean isInsideArtifact(Stack<Tuple2<PdfName, PdfDictionary>> tagStack) {
        for (Tuple2<PdfName, PdfDictionary> tag : tagStack) {
            if (PdfName.Artifact.equals(tag.getFirst())) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideRealContent(Stack<Tuple2<PdfName, PdfDictionary>> tagStack) {
        for (Tuple2<PdfName, PdfDictionary> tag : tagStack) {
            if (isRealContent(tag)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRealContent(Tuple2<PdfName, PdfDictionary> tag) {
        if (PdfName.Artifact.equals(tag.getFirst())) {
            return false;
        }
        PdfDictionary properties = tag.getSecond();
        if (properties == null || !properties.containsKey(PdfName.MCID)) {
            return false;
        }
        PdfMcr mcr = this.pdfDocument.getStructTreeRoot()
                .findMcrByMcid(pdfDocument, (int) properties.getAsInt(PdfName.MCID));
        if (mcr == null) {
            throw new PdfUAConformanceException(
                    PdfUAExceptionMessageConstants.CONTENT_WITH_MCID_BUT_MCID_NOT_FOUND_IN_STRUCT_TREE_ROOT);
        }
        return true;
    }

    private void checkCatalog(PdfCatalog catalog) {
        PdfDictionary catalogDict = catalog.getPdfObject();
        if (!catalogDict.containsKey(PdfName.Metadata)) {
            throw new PdfUAConformanceException(
                    PdfUAExceptionMessageConstants.METADATA_SHALL_BE_PRESENT_IN_THE_CATALOG_DICTIONARY);
        }
        PdfDictionary markInfo = catalogDict.getAsDictionary(PdfName.MarkInfo);
        if (markInfo != null && markInfo.containsKey(PdfName.Suspects)) {
            PdfBoolean markInfoSuspects = markInfo.getAsBoolean(PdfName.Suspects);
            if (markInfoSuspects != null && markInfoSuspects.getValue()) {
                throw new PdfUAConformanceException(
                        PdfUAExceptionMessageConstants.
                                SUSPECTS_ENTRY_IN_MARK_INFO_DICTIONARY_SHALL_NOT_HAVE_A_VALUE_OF_TRUE);
            }
        }
        checkViewerPreferences(catalog);
        checkMetadata(catalog);
    }

    private void checkStructureTreeRoot(PdfStructTreeRoot structTreeRoot) {
        PdfDictionary roleMap = structTreeRoot.getRoleMap();
        for (Map.Entry<PdfName, PdfObject> entry : roleMap.entrySet()) {
            final String role = entry.getKey().getValue();
            final IRoleMappingResolver roleMappingResolver = pdfDocument.getTagStructureContext()
                    .getRoleMappingResolver(role);

            if (roleMappingResolver.currentRoleIsStandard()) {
                throw new PdfUAConformanceException(PdfUAExceptionMessageConstants.ONE_OR_MORE_STANDARD_ROLE_REMAPPED);
            }
        }

        TagTreeIterator tagTreeIterator = new TagTreeIterator(structTreeRoot);
        tagTreeIterator.addHandler(new GraphicsCheckUtil.GraphicsHandler(context));
        tagTreeIterator.addHandler(new FormulaCheckUtil.FormulaTagHandler(context));
        tagTreeIterator.addHandler(new HeadingsChecker.HeadingHandler(context));
        tagTreeIterator.addHandler(new TableCheckUtil.TableHandler(context));
        tagTreeIterator.traverse();
    }

    private void checkFonts(Collection<PdfFont> fontsInDocument) {
        Set<String> fontNamesThatAreNotEmbedded = new HashSet<>();
        for (PdfFont font : fontsInDocument) {
            if (!font.isEmbedded()) {
                fontNamesThatAreNotEmbedded.add(font.getFontProgram().getFontNames().getFontName());
            }
        }
        if (!fontNamesThatAreNotEmbedded.isEmpty()) {
            throw new PdfUAConformanceException(
                    MessageFormatUtil.format(
                            PdfUAExceptionMessageConstants.FONT_SHOULD_BE_EMBEDDED,
                            String.join(", ", fontNamesThatAreNotEmbedded)
                    ));
        }
    }
}