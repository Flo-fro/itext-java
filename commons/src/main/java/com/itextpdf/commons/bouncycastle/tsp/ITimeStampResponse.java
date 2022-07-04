package com.itextpdf.commons.bouncycastle.tsp;

import com.itextpdf.commons.bouncycastle.asn1.cmp.IPKIFailureInfo;

public interface ITimeStampResponse {

    void validate(ITimeStampRequest request) throws AbstractTSPException;

    IPKIFailureInfo getFailInfo();

    ITimeStampToken getTimeStampToken();

    String getStatusString();
}