/**
 * *****************************************************************************
 *
 * @file BasePDFActionExecuter.java
 * @description Abstract base class for executing PDF sign actions within the Alfresco extension.
 *              This class provides common functionality for setting up services and parameter definitions.
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

package org.alfresco.extension.pdfsign.repo.action.executer;

import java.util.List;
import org.alfresco.extension.pdfsign.constants.PDFSignConstants;
import org.alfresco.extension.pdfsign.service.PDFSignService;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * Abstract base class for executing PDF sign actions within the Alfresco extension.
 * This class provides common functionality for setting up services and parameter definitions.
 */
public abstract class BasePDFActionExecuter extends ActionExecuterAbstractBase {

    /** The service registry to use for looking up services. */
    protected ServiceRegistry serviceRegistry;

    /** The PDF signing service used to perform signing operations. */
    protected PDFSignService pdfSignService;

    /** Default number of map entries at creation. */
    protected static final int INITIAL_OPTIONS = 5;

    /**
     * Sets the service registry to use, doing away with individual service registrations.
     *
     * @param serviceRegistry the service registry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Adds parameter definitions for the action.
     *
     * @param paramList the list of parameter definitions to add to
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_INPLACE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PDFSignConstants.PARAM_INPLACE), false));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_CREATE_NEW, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PDFSignConstants.PARAM_CREATE_NEW), false));
    }

    /**
     * Sets the PDF signing service to use.
     *
     * @param pdfSignService the PDF signing service to set
     */
    public void setPDFSignService(PDFSignService pdfSignService) {
        this.pdfSignService = pdfSignService;
    }
}
