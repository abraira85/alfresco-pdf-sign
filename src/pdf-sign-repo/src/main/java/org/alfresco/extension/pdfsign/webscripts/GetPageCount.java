/**
 * *****************************************************************************
 *
 * @file GetPageCount.java
 * @description Web Script for retrieving the page count of a PDF document stored
 *              in Alfresco. This script uses the iText library to read the PDF and
 *              count the number of pages.
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

package org.alfresco.extension.pdfsign.webscripts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.itextpdf.text.pdf.PdfReader;

/**
 * Web Script for retrieving the page count of a PDF document stored in Alfresco.
 * This script uses the iText library to read the PDF and count the number of pages.
 */
public class GetPageCount extends DeclarativeWebScript {
    private static final Log logger = LogFactory.getLog(GetPageCount.class);
    private ServiceRegistry serviceRegistry;
    private int count = -1;

    /**
     * Executes the web script to retrieve the page count of a PDF document.
     *
     * @param req the web script request
     * @param status the status of the web script
     * @param cache the cache for the web script
     * @return a map containing the page count of the PDF
     */
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String nodeRef = req.getParameter("nodeRef");
        Map<String, Object> model = new HashMap<String, Object>();

        try {
            ContentReader reader = serviceRegistry.getContentService().getReader(new NodeRef(nodeRef), ContentModel.PROP_CONTENT);
            PdfReader pdfReader = new PdfReader(reader.getContentInputStream());
            count = pdfReader.getNumberOfPages();
            pdfReader.close();
        } catch (IOException ioex) {
            logger.error("Error fetching page count for document: " + ioex);
        }

        model.put("pageCount", count);
        return model;
    }

    /**
     * Sets the ServiceRegistry used by this web script.
     *
     * @param serviceRegistry the ServiceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
