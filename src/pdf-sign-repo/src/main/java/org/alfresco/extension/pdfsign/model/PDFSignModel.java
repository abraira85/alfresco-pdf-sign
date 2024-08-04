package org.alfresco.extension.pdfsign.model;

import org.alfresco.service.namespace.QName;

public interface PDFSignModel {
    //namespace
    static final String PDFSIGN_MODEL_1_0_URI = "http://www.alfresco.com/model/pdfsign/1.0";

    //signed aspect and properties
    static final QName ASPECT_SIGNED = QName.createQName(PDFSIGN_MODEL_1_0_URI, "signed");
    static final QName PROP_SIGNATUREDATE = QName.createQName(PDFSIGN_MODEL_1_0_URI, "signaturedate");
    static final QName PROP_REASON = QName.createQName(PDFSIGN_MODEL_1_0_URI, "reason");
    static final QName PROP_LOCATION = QName.createQName(PDFSIGN_MODEL_1_0_URI, "location");
    static final QName PROP_SIGNEDBY = QName.createQName(PDFSIGN_MODEL_1_0_URI, "signedby");
}
