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
package com.itextpdf.signatures.validation;

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.commons.utils.DateTimeUtil;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.signatures.CertificateUtil;
import com.itextpdf.signatures.IssuingCertificateRetriever;
import com.itextpdf.signatures.TimestampConstants;
import com.itextpdf.signatures.logs.SignLogMessageConstant;
import com.itextpdf.signatures.testutils.PemFileHelper;
import com.itextpdf.signatures.testutils.TimeTestUtil;
import com.itextpdf.signatures.testutils.builder.TestCrlBuilder;
import com.itextpdf.signatures.validation.extensions.CertificateExtension;
import com.itextpdf.signatures.validation.extensions.KeyUsage;
import com.itextpdf.signatures.validation.extensions.KeyUsageExtension;
import com.itextpdf.signatures.validation.report.ValidationReport;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.BouncyCastleUnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Category(BouncyCastleUnitTest.class)
public class CRLValidatorTest extends ExtendedITextTest {

    private static final String SOURCE_FOLDER = "./src/test/resources/com/itextpdf/signatures/validation/CRLValidatorTest/";
    private static final IBouncyCastleFactory FACTORY = BouncyCastleFactoryCreator.getFactory();

    private static final char[] KEY_PASSWORD = "testpassphrase".toCharArray();

    private CRLValidator validator;
    private MockChainValidator mockChainValidator;
    private X509Certificate crlIssuerCert;
    private X509Certificate signCert;
    private PrivateKey crlIssuerKey;
    private PrivateKey intermediateKey;

    @BeforeClass
    public static void setUpOnce() {
        Security.addProvider(FACTORY.getProvider());
    }

    @Before
    public void setUp() {
        validator = new CRLValidator();
        mockChainValidator = new MockChainValidator();
        validator.setCertificateChainValidator(mockChainValidator);
    }

    @Test
    public void happyPathTest() throws Exception {
        retrieveTestResources("happyPath");
        byte[] crl = createCrl(
                crlIssuerCert,
                crlIssuerKey,
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -5),
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +5)
        );
        ValidationReport report = performValidation("happyPath", TimeTestUtil.TEST_DATE_TIME, crl);
        Assert.assertEquals(ValidationReport.ValidationResult.VALID, report.getValidationResult());
        Assert.assertTrue(report.getFailures().isEmpty());
    }

    @Test
    public void nextUpdateBeforeValidationTest() throws Exception {
        retrieveTestResources("happyPath");
        Date nextUpdate = DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -5);
        byte[] crl = createCrl(
                crlIssuerCert,
                crlIssuerKey,
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -15),
                nextUpdate
        );
        ValidationReport report = performValidation("happyPath", TimeTestUtil.TEST_DATE_TIME, crl);
        Assert.assertEquals(ValidationReport.ValidationResult.INDETERMINATE, report.getValidationResult());
        Assert.assertEquals(1, report.getFailures().size());
        Assert.assertEquals(MessageFormatUtil.format(CRLValidator.UPDATE_DATE_BEFORE_CHECK_DATE,
                nextUpdate, TimeTestUtil.TEST_DATE_TIME), report.getFailures().get(0).getMessage());
    }

    @Test
    public void chainValidatorUsageTest() throws Exception {
        retrieveTestResources("happyPath");
        byte[] crl = createCrl(
                crlIssuerCert,
                crlIssuerKey,
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -5),
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +5)
        );
        ValidationReport report = performValidation("happyPath", TimeTestUtil.TEST_DATE_TIME, crl);
        Assert.assertEquals(ValidationReport.ValidationResult.VALID, report.getValidationResult());
        Assert.assertTrue(report.getFailures().isEmpty());

        Assert.assertEquals(1, mockChainValidator.verificationCalls.size());
        Assert.assertEquals(1, mockChainValidator.verificationCalls.get(0).requiredExtensions.size());
        Assert.assertEquals(new KeyUsageExtension(KeyUsage.CRL_SIGN),
                mockChainValidator.verificationCalls.get(0).requiredExtensions.get(0));
        Assert.assertEquals(crlIssuerCert, mockChainValidator.verificationCalls.get(0).certificate);
    }


    @Test
    public void issuerCertificateIsNotFoundTest() throws Exception {
        retrieveTestResources("missingIssuer");
        byte[] crl = createCrl(
                crlIssuerCert,
                crlIssuerKey,
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -5),
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +5)
        );
        ValidationReport report = performValidation("missingIssuer", TimeTestUtil.TEST_DATE_TIME, crl);
        Assert.assertEquals(ValidationReport.ValidationResult.INDETERMINATE, report.getValidationResult());
        Assert.assertEquals(CRLValidator.CRL_ISSUER_NOT_FOUND, report.getFailures().get(0).getMessage());
    }

    @Test
    public void crlIssuerAndSignCertHaveNoSharedRootTest() throws Exception {
        retrieveTestResources("crlIssuerAndSignCertHaveNoSharedRoot");
        byte[] crl = createCrl(
                crlIssuerCert,
                crlIssuerKey,
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -5),
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +5)
        );
        ValidationReport report = performValidation("crlIssuerAndSignCertHaveNoSharedRoot",
                TimeTestUtil.TEST_DATE_TIME, crl);
        Assert.assertEquals(ValidationReport.ValidationResult.INDETERMINATE, report.getValidationResult());
        Assert.assertEquals(CRLValidator.CRL_ISSUER_NO_COMMON_ROOT, report.getFailures().get(0).getMessage());
    }

    @Test
    // CRL has the certificate revoked before signing date
    public void crlIssuerRevokedBeforeSigningDate() throws Exception {
        retrieveTestResources("crlIssuerRevokedBeforeSigningDate");
        Date revocationDate = DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -2);
        byte[] crl = createCrl(
                crlIssuerCert,
                crlIssuerKey,
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -5),
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +5),
                signCert, revocationDate, 1

        );
        ValidationReport report = performValidation("crlIssuerRevokedBeforeSigningDate",
                TimeTestUtil.TEST_DATE_TIME, crl);
        Assert.assertEquals(ValidationReport.ValidationResult.INVALID, report.getValidationResult());
        Assert.assertEquals(1, report.getFailures().size());
        Assert.assertEquals(MessageFormatUtil.format(CRLValidator.CERTIFICATE_REVOKED,
                        crlIssuerCert.getSubjectX500Principal(), revocationDate),
                report.getFailures().get(0).getMessage());
    }


    @Test
    // CRL has the certificate revoked after signing date
    public void crlRevokedAfterSigningDate() throws Exception {
        retrieveTestResources("happyPath");
        Date revocationDate = DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +20);
        byte[] crl = createCrl(
                crlIssuerCert,
                crlIssuerKey,
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +18),
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +23),
                signCert, revocationDate, 1

        );
        ValidationReport report = performValidation("happyPath",
                TimeTestUtil.TEST_DATE_TIME, crl);
        Assert.assertEquals(ValidationReport.ValidationResult.VALID, report.getValidationResult());
        Assert.assertEquals(2, report.getLogs().size());
        Assert.assertEquals(
                MessageFormatUtil.format(SignLogMessageConstant.VALID_CERTIFICATE_IS_REVOKED, revocationDate),
                report.getLogs().get(1).getMessage());
    }

    @Test
    //CRL response is invalid (signature not matching)
    public void crlSignatureMismatch() throws Exception {
        retrieveTestResources("happyPath");
        byte[] crl = createCrl(
                crlIssuerCert,
                intermediateKey,
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +18),
                DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +23),
                signCert, DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, +20), 1

        );
        ValidationReport report = performValidation("happyPath",
                TimeTestUtil.TEST_DATE_TIME, crl);
        Assert.assertEquals(ValidationReport.ValidationResult.INDETERMINATE, report.getValidationResult());
        Assert.assertEquals(1, report.getFailures().size());
        Assert.assertEquals(CRLValidator.CRL_INVALID, report.getFailures().get(0).getMessage());
    }


    private void retrieveTestResources(String path) throws Exception {
        String resourcePath = SOURCE_FOLDER + path + "/";
        crlIssuerCert = (X509Certificate) PemFileHelper.readFirstChain(resourcePath + "crl-issuer.cert.pem")[0];
        signCert = (X509Certificate) PemFileHelper.readFirstChain(resourcePath + "sign.cert.pem")[0];
        crlIssuerKey = PemFileHelper.readFirstKey(SOURCE_FOLDER + "keys/crl-key.pem", KEY_PASSWORD);
        intermediateKey = PemFileHelper.readFirstKey(SOURCE_FOLDER + "keys/im_key.pem", KEY_PASSWORD);
    }


    private byte[] createCrl(X509Certificate issuerCert, PrivateKey issuerKey, Date issueDate, Date nextUpdate)
            throws Exception {
        return createCrl(issuerCert, issuerKey, issueDate, nextUpdate,
                null, (Date) TimestampConstants.UNDEFINED_TIMESTAMP_DATE, 0);
    }

    private byte[] createCrl(X509Certificate issuerCert, PrivateKey issuerKey, Date issueDate, Date nextUpdate,
                             X509Certificate revokedCert, Date revocationDate, int reason)
            throws Exception {

        TestCrlBuilder builder = new TestCrlBuilder(issuerCert, issuerKey);
        if (nextUpdate != null) {
            builder.setNextUpdate(nextUpdate);
        }
        if (revocationDate != TimestampConstants.UNDEFINED_TIMESTAMP_DATE && revokedCert != null) {
            builder.addCrlEntry(revokedCert, revocationDate, reason);
        }
        return builder.makeCrl();
    }

    public ValidationReport performValidation(String testName, Date testDate, byte[] encodedCrl)
            throws Exception {
        String resourcePath = SOURCE_FOLDER + testName + '/';
        String missingCertsFileName = resourcePath + "chain.pem";
        Certificate[] knownCerts = PemFileHelper.readFirstChain(missingCertsFileName);
        IssuingCertificateRetriever mockCertificateRetriever = new IssuingCertificateRetriever();
        mockCertificateRetriever.addKnownCertificates(Arrays.asList(knownCerts));
        validator.setIssuingCertificateRetriever(mockCertificateRetriever);

        X509Certificate certificateUnderTest =
                (X509Certificate) PemFileHelper.readFirstChain(resourcePath + "sign.cert.pem")[0];
        ValidationReport result = new ValidationReport();
        validator.validate(result, certificateUnderTest, (X509CRL) CertificateUtil.parseCrlFromStream(
                new ByteArrayInputStream(encodedCrl)), testDate);
        return result;
    }

    private static class MockChainValidator extends CertificateChainValidator {

        public List<ValidationCallBack> verificationCalls = new ArrayList<ValidationCallBack>();

        @Override
        public ValidationReport validate(ValidationReport result, X509Certificate certificate, Date verificationDate,
                                         List<CertificateExtension> requiredExtensions) {
            verificationCalls.add(new ValidationCallBack(certificate, verificationDate, requiredExtensions));
            return result;
        }
    }

    private static class ValidationCallBack {
        public X509Certificate certificate;
        public Date checkDate;
        public List<CertificateExtension> requiredExtensions;

        public ValidationCallBack(X509Certificate certificate, Date checkDate,
                                  List<CertificateExtension> requiredExtensions) {
            this.certificate = certificate;
            this.checkDate = checkDate;
            this.requiredExtensions = requiredExtensions;
        }
    }
}
