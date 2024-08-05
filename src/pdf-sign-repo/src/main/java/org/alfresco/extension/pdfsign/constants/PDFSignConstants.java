/**
 * *****************************************************************************
 *
 * @file PDFSignConstants.java
 * @description Constants used for PDF signing in the Alfresco extension.
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

package org.alfresco.extension.pdfsign.constants;

/**
 * This class defines constants used for configuring PDF signing operations
 * within the Alfresco extension.
 */
public abstract class PDFSignConstants
{
    /** Specifies if the signing should be done in place. */
    public static final String PARAM_INPLACE    	 				= "inplace";

    /** Specifies the destination folder for the signed PDF. */
    public static final String PARAM_DESTINATION_FOLDER 			= "destination-folder";

    /** Specifies the name for the signed PDF. */
    public static final String PARAM_DESTINATION_NAME  				= "destination-name";

    /** Specifies if a new PDF should be created. */
    public static final String PARAM_CREATE_NEW						= "create-new";

    /** Specifies the position of the signature on the PDF. */
    public static final String PARAM_POSITION       				= "position";

    /** Specifies the X-coordinate for the signature position. */
    public static final String PARAM_LOCATION_X     				= "location-x";

    /** Specifies the Y-coordinate for the signature position. */
    public static final String PARAM_LOCATION_Y     				= "location-y";

    /** Specifies the page number where the signature should be placed. */
    public static final String PARAM_PAGE			 				= "page";

    /** Specifies the private key used for signing. */
    public static final String PARAM_PRIVATE_KEY        			= "private-key";

    /** Specifies the visibility of the signature. */
    public static final String PARAM_VISIBILITY         			= "visibility";

    /** Specifies the location of the signature. */
    public static final String PARAM_LOCATION           			= "location";

    /** Specifies the reason for the signature. */
    public static final String PARAM_REASON             			= "reason";

    /** Specifies the password for the private key. */
    public static final String PARAM_KEY_PASSWORD       			= "key-password";

    /** Specifies the width of the signature. */
    public static final String PARAM_WIDTH              			= "width";

    /** Specifies the height of the signature. */
    public static final String PARAM_HEIGHT             			= "height";

    /** Specifies the type of key used for signing. */
    public static final String PARAM_KEY_TYPE          				= "key-type";

    /** Specifies the alias of the private key. */
    public static final String PARAM_ALIAS              			= "alias";

    /** Specifies the password for the key store. */
    public static final String PARAM_STORE_PASSWORD     			= "store-password";

    /** Specifies if a new revision should be created. */
    public static final String PARAM_NEW_REVISION    				= "new-revision";

    /** Constant for visible signature visibility. */
    public static final String VISIBILITY_VISIBLE       			= "visible";

    /** Constant for PKCS12 key type. */
    public static final String KEY_TYPE_PKCS12          			= "pkcs12";

    /** Constant for default key type. */
    public static final String KEY_TYPE_DEFAULT         			= "default";

    /** Constant for center position of the signature. */
    public static final String POSITION_CENTER      				= "center";

    /** Constant for top-left position of the signature. */
    public static final String POSITION_TOPLEFT     				= "topleft";

    /** Constant for top-right position of the signature. */
    public static final String POSITION_TOPRIGHT    				= "topright";

    /** Constant for bottom-left position of the signature. */
    public static final String POSITION_BOTTOMLEFT  				= "bottomleft";

    /** Constant for bottom-right position of the signature. */
    public static final String POSITION_BOTTOMRIGHT 				= "bottomright";

    /** Constant for manual position of the signature. */
    public static final String POSITION_MANUAL 	 					= "manual";

    /** Constant for PDF file extension. */
    public static final String FILE_EXTENSION 						= ".pdf";

    /** Constant for PDF MIME type. */
    public static final String FILE_MIMETYPE  						= "application/pdf";

    /** Constant for PDF format. */
    public static final String PDF 									= "pdf";
}
