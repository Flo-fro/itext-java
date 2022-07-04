package com.itextpdf.bouncycastlefips.tsp;


import com.itextpdf.bouncycastlefips.asn1.cmp.PKIFailureInfoBCFips;
import com.itextpdf.commons.bouncycastle.asn1.cmp.IPKIFailureInfo;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampRequest;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampResponse;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampToken;

import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;

public class TimeStampResponseBCFips implements ITimeStampResponse {

    private final TimeStampResponse timeStampResponse;

    public TimeStampResponseBCFips(TimeStampResponse timeStampResponse) {
        this.timeStampResponse = timeStampResponse;
    }

    public TimeStampResponse getTimeStampResponse() {
        return timeStampResponse;
    }

    @Override
    public void validate(ITimeStampRequest request) throws TSPExceptionBCFips {
        try {
            timeStampResponse.validate(((TimeStampRequestBCFips) request).getTimeStampRequest());
        } catch (TSPException e) {
            throw new TSPExceptionBCFips(e);
        }
    }

    @Override
    public IPKIFailureInfo getFailInfo() {
        return new PKIFailureInfoBCFips(timeStampResponse.getFailInfo());
    }

    @Override
    public ITimeStampToken getTimeStampToken() {
        return new TimeStampTokenBCFips(timeStampResponse.getTimeStampToken());
    }

    @Override
    public String getStatusString() {
        return timeStampResponse.getStatusString();
    }
}