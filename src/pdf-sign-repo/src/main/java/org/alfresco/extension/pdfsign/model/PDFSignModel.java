/**
 * *****************************************************************************
 *
 * @file PDFSignModel.java
 * @description Defines the model for PDF signing within the Alfresco extension.
 *
 * @author Rober de Avila Abraira
 * @version 1.0
 * @date 2024/08/04
 *
 * @copyright © 2024 Rober de Avila Abraira
 *
 * @license Licensed under the Apache License, Version 2.0 (the "License");
 *          you may not use this file except in compliance with the License.
 *          You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 *          Unless required by applicable law or agreed to in writing, software
 *          distributed under the License is distributed on an "AS IS" BASIS,
 *          WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *          See the License for the specific language governing permissions and
 *          limitations under the License.
 *
 * *****************************************************************************
 */

package org.alfresco.extension.pdfsign.model;

import org.alfresco.service.namespace.QName;

/**
 * This interface defines the model for PDF signing within the Alfresco extension.
 */
public interface PDFSignModel {
    /** The namespace URI for the PDF signing model. */
    static final String PDFSIGN_MODEL_1_0_URI = "http://www.alfresco.com/model/pdfsign/1.0";

    /** QName for the signed aspect. */
    static final QName ASPECT_SIGNED = QName.createQName(PDFSIGN_MODEL_1_0_URI, "signed");

    /** QName for the signature date property. */
    static final QName PROP_SIGNATUREDATE = QName.createQName(PDFSIGN_MODEL_1_0_URI, "signaturedate");

    /** QName for the reason property. */
    static final QName PROP_REASON = QName.createQName(PDFSIGN_MODEL_1_0_URI, "reason");

    /** QName for the location property. */
    static final QName PROP_LOCATION = QName.createQName(PDFSIGN_MODEL_1_0_URI, "location");

    /** QName for the signed by property. */
    static final QName PROP_SIGNEDBY = QName.createQName(PDFSIGN_MODEL_1_0_URI, "signedby");
}
