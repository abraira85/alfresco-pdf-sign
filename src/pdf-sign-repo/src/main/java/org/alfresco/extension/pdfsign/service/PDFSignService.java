/**
 * *****************************************************************************
 *
 * @file PDFSignService.java
 * @description Interface for PDF signing services within the Alfresco extension.
 *              This service provides methods for applying digital signatures to PDF documents.
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

package org.alfresco.extension.pdfsign.service;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface for PDF signing services within the Alfresco extension.
 * This service provides methods for applying digital signatures to PDF documents.
 */
public interface PDFSignService {

    /**
     * Applies a digital signature to a PDF document.
     *
     * @param targetNodeRef the NodeRef pointing to the PDF document to be signed
     * @param params a map of parameters required for signing the PDF
     * @return a NodeRef pointing to the signed PDF
     */
    NodeRef signPDF(NodeRef targetNodeRef, Map<String, Serializable> params);
}
