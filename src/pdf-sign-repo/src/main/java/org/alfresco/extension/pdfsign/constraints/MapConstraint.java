/**
 * *****************************************************************************
 *
 * @file MapConstraint.java
 * @description Implementation of a parameter constraint using a map for the Alfresco extension.
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

package org.alfresco.extension.pdfsign.constraints;

import org.alfresco.repo.action.constraint.BaseParameterConstraint;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines a parameter constraint using a map to specify allowable values
 * for the Alfresco extension.
 */
public class MapConstraint extends BaseParameterConstraint {

    /** The map containing the allowable values for the constraint. */
    private final HashMap<String, String> cm = new HashMap<String, String>();

    /**
     * Default constructor for MapConstraint.
     */
    public MapConstraint() {
    }

    /**
     * Sets the constraint map with the given values.
     *
     * @param m the map containing the values to be set as constraints
     */
    public void setConstraintMap(Map<String, String> m) {
        cm.putAll(m);
    }

    /**
     * Retrieves the allowable values for this constraint.
     *
     * @return a map of allowable values
     */
    public Map<String, String> getAllowableValues() {
        return cm;
    }

    /**
     * Retrieves the allowable values for this constraint.
     * This method is used internally by the base class.
     *
     * @return a map of allowable values
     */
    @Override
    protected Map<String, String> getAllowableValuesImpl() {
        return cm;
    }
}
