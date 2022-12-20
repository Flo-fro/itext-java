/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.kernel.crypto;

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.kernel.logs.KernelLogMessageConstant;
import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.BouncyCastleIntegrationTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(BouncyCastleIntegrationTest.class)
public class EncryptionInApprovedModeTest extends ExtendedITextTest {
    private static final IBouncyCastleFactory FACTORY = BouncyCastleFactoryCreator.getFactory();

    public static final String destinationFolder = "./target/test/com/itextpdf/kernel/crypto"
            + "/EncryptionInApprovedModeTest/";
    public static final String sourceFolder = "./src/test/resources/com/itextpdf/kernel/crypto"
            + "/EncryptionInApprovedModeTest/";

    /**
     * User password.
     */
    public static byte[] USER = "Hello".getBytes(StandardCharsets.ISO_8859_1);

    /**
     * Owner password.
     */
    public static byte[] OWNER = "World".getBytes(StandardCharsets.ISO_8859_1);

    @BeforeClass
    public static void beforeClass() {
        Assume.assumeTrue(FACTORY.isInApprovedOnlyMode());
        createOrClearDestinationFolder(destinationFolder);
        Security.addProvider(FACTORY.getProvider());
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = KernelLogMessageConstant.MD5_IS_NOT_FIPS_COMPLIANT))
    public void checkMD5LogMessageWhileReadingPdfTest() throws IOException {
        String fileName = "checkMD5LogMessageWhileReadingPdf.pdf";
        try (PdfDocument document = new PdfDocument(new PdfReader(sourceFolder + fileName))) {
            // this test checks log message
        }
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = KernelLogMessageConstant.MD5_IS_NOT_FIPS_COMPLIANT))
    public void checkMD5LogMessageWhileCreatingPdfTest() throws IOException {
        String fileName = "checkMD5LogMessageWhileCreatingPdf.pdf";
        try (PdfDocument document = new PdfDocument(new PdfWriter(destinationFolder + fileName,
                new WriterProperties().setStandardEncryption(USER, OWNER, EncryptionConstants.ALLOW_SCREENREADERS,
                        EncryptionConstants.ENCRYPTION_AES_256).addXmpMetadata()))) {
            // this test checks log message
        }
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = KernelLogMessageConstant.MD5_IS_NOT_FIPS_COMPLIANT,
            count = 3))
    public void checkMD5LogMessageForEachPdfTest() throws IOException {
        String fileName = "checkMD5LogMessageForEachPdf.pdf";
        for (int i = 0; i < 3; ++i) {
            try (PdfDocument document = new PdfDocument(new PdfWriter(destinationFolder + fileName,
                    new WriterProperties().setStandardEncryption(USER, OWNER, EncryptionConstants.ALLOW_SCREENREADERS,
                            EncryptionConstants.ENCRYPTION_AES_256).addXmpMetadata()))) {
                // this test checks log message
            }
        }
    }
}