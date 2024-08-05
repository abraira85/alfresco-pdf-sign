/**
 * *****************************************************************************
 *
 * @file BasePDFStampActionExecuter.java
 * @description Abstract base class for executing PDF stamp actions within the Alfresco extension.
 *              This class sets up parameter definitions and constraints for stamping actions.
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
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * Abstract base class for executing PDF stamp actions within the Alfresco extension.
 * This class sets up parameter definitions and constraints for stamping actions.
 */
public abstract class BasePDFStampActionExecuter extends BasePDFActionExecuter {

    /** Constraints for page and position parameters */
    public static HashMap<String, String> pageConstraint = new HashMap<String, String>();
    public static HashMap<String, String> positionConstraint = new HashMap<String, String>();

    /**
     * Sets the position constraint.
     *
     * @param mc the map constraint containing allowable values for positions
     */
    public void setPositionConstraint(MapConstraint mc) {
        positionConstraint.putAll(mc.getAllowableValues());
    }

    /**
     * Sets the page constraint.
     *
     * @param mc the map constraint containing allowable values for pages
     */
    public void setPageConstraint(MapConstraint mc) {
        pageConstraint.putAll(mc.getAllowableValues());
    }

    /**
     * Adds parameter definitions for the stamping action.
     *
     * @param paramList the list of parameter definitions to add to
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_POSITION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PDFSignConstants.PARAM_POSITION), false, "pdfc-position"));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_LOCATION_X, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PDFSignConstants.PARAM_LOCATION_X)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_LOCATION_Y, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PDFSignConstants.PARAM_LOCATION_Y)));
        paramList.add(new ParameterDefinitionImpl(PDFSignConstants.PARAM_PAGE, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PDFSignConstants.PARAM_PAGE), false));

        super.addParameterDefinitions(paramList);
    }
}
