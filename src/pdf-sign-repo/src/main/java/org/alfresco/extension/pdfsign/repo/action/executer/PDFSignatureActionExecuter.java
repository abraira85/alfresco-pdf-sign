/**
 * *****************************************************************************
 *
 * @file PDFSignatureActionExecuter.java
 * @description Executes the PDF signature action within the Alfresco extension.
 *              This class sets up parameter definitions and constraints for the action.
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

import java.util.HashMap;
import java.util.List;

import org.alfresco.extension.pdfsign.constants.PDFSignConstants;
import org.alfresco.extension.pdfsign.constraints.MapConstraint;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Executes the PDF signature action within the Alfresco extension.
 * This class sets up parameter definitions and constraints for the action.
 */
public class PDFSignatureActionExecuter extends BasePDFStampActionExecuter {

    /** The logger */
    private static Log logger = LogFactory.getLog(PDFSignatureActionExecuter.class);

    /** Constraints */
    public static HashMap<String, String> visibilityConstraint = new HashMap<String, String>();
    public static HashMap<String, String> keyTypeConstraint = new HashMap<String, String>();

    /** Action constants */
    public static final String NAME = "pdf-signature";

    /**
     * Sets the key type constraint.
     *
     * @param mc the map constraint containing allowable values for key types
     */
    public void setKeyTypeConstraint(MapConstraint mc) {
        keyTypeConstraint.putAll(mc.getAllowableValues());
    }

    /**
     * Sets the visibility constraint.
     *
     * @param mc the map constraint containing allowable values for visibility
     */
    public void setVisibilityConstraint(MapConstraint mc) {
        visibilityConstraint.putAll(mc.getAllowableValues());
    }

    /**
     * Adds parameter definitions for the action.
     *
     * @param paramList the list of parameter definitions to add to
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PDFSignConstants.PARAM_DESTINATION_FOLDER)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_PRIVATE_KEY, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PDFSignConstants.PARAM_PRIVATE_KEY)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_VISIBILITY, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PDFSignConstants.PARAM_VISIBILITY), false, "pdfc-visibility"));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_KEY_PASSWORD, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PDFSignConstants.PARAM_KEY_PASSWORD)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_WIDTH, DataTypeDefinition.INT, false, getParamDisplayLabel(PDFSignConstants.PARAM_WIDTH)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_HEIGHT, DataTypeDefinition.INT, false, getParamDisplayLabel(PDFSignConstants.PARAM_HEIGHT)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_KEY_TYPE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PDFSignConstants.PARAM_KEY_TYPE), false, "pdfc-keytype"));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_STORE_PASSWORD, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PDFSignConstants.PARAM_STORE_PASSWORD)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_DESTINATION_NAME, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PDFSignConstants.PARAM_DESTINATION_NAME)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_NEW_REVISION, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PDFSignConstants.PARAM_NEW_REVISION), false));

        super.addParameterDefinitions(paramList);
    }

    /**
     * Executes the PDF signature action.
     *
     * @param action the action containing parameters for signing
     * @param actionedUponNodeRef the node reference to the PDF document to be signed
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        NodeRef result = pdfSignService.signPDF(actionedUponNodeRef, action.getParameterValues());
        action.setParameterValue(PARAM_RESULT, result);
    }
}
