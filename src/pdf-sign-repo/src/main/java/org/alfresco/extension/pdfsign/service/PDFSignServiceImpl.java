/**
 * *****************************************************************************
 *
 * @file PDFSignServiceImpl.java
 * @description Implementation of the PDFSignService interface. Provides methods
 *              for applying digital signatures to PDF documents using iText library.
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

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.pdfsign.constants.PDFSignConstants;
import org.alfresco.extension.pdfsign.model.PDFSignModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;

import java.io.*;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Enumeration;


/**
 * Implementation of the PDFSignService interface. Provides methods for applying
 * digital signatures to PDF documents using the iText library.
 */
public class PDFSignServiceImpl extends PDFSignConstants implements PDFSignService {

    private ServiceRegistry serviceRegistry;
    private NodeService ns;
    private ContentService cs;
    private FileFolderService ffs;
    private DictionaryService ds;
    private PersonService ps;
    private AuthenticationService as;

    private boolean useSignatureAspect = true;
    private boolean createNew = false;

    private int defaultWidth = 200;
    private int defaultHeight = 100;


    /**
     * Loads a KeyStore from the given input stream.
     *
     * @param keyStream the input stream containing the KeyStore data
     * @param storePassword the password for the KeyStore
     * @return the loaded KeyStore
     */
    private KeyStore loadKeyStore(InputStream keyStream, String storePassword) {
        try {
            KeyStore ks = KeyStore.getInstance("pkcs12");
            ks.load(keyStream, storePassword.toCharArray());
            return ks;
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Error loading KeyStore", e);
        }
    }

    /**
     * Retrieves a PrivateKey from the given KeyStore.
     *
     * @param ks the KeyStore
     * @param alias the alias for the key
     * @param keyPassword the password for the key
     * @return the PrivateKey
     */
    private PrivateKey getPrivateKey(KeyStore ks, String alias, String keyPassword) {
        try {
            Key key = ks.getKey(alias, keyPassword.toCharArray());
            if (key instanceof PrivateKey) {
                return (PrivateKey) key;
            } else {
                throw new AlfrescoRuntimeException("Key for alias " + alias + " is not a private key");
            }
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Error retrieving private key", e);
        }
    }

    /**
     * Retrieves the certificate chain for the given alias from the KeyStore.
     *
     * @param ks the KeyStore
     * @param alias the alias for the certificate chain
     * @return the certificate chain
     */
    private Certificate[] getCertificateChain(KeyStore ks, String alias) {
        try {
            Certificate[] certChain = ks.getCertificateChain(alias);
            if (certChain == null) {
                throw new AlfrescoRuntimeException("Certificate chain for alias " + alias + " is null");
            }
            return certChain;
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Error retrieving certificate chain", e);
        }
    }

    /**
     * Retrieves the CN (Common Name) from the KeyStore's certificate subject.
     *
     * @param ks the KeyStore
     * @return the CN (Common Name) or null if not found
     */
    private String getFriendlyName(KeyStore ks) {
        try {
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate[] certChain = ks.getCertificateChain(alias);
                if (certChain != null && certChain.length > 0) {
                    X509Certificate cert = (X509Certificate) certChain[0];
                    X500Principal principal = cert.getSubjectX500Principal();
                    String dn = principal.getName();

                    // Extract CN value from DN
                    return extractCNFromDN(dn);
                }
            }
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Error retrieving friendly name", e);
        }
        return null;
    }

    /**
     * Extracts the CN (Common Name) from a DN (Distinguished Name).
     *
     * @param dn the Distinguished Name
     * @return the CN (Common Name) or null if not found
     */
    private String extractCNFromDN(String dn) {
        // Split the DN by commas to get individual components
        String[] dnComponents = dn.split(",");

        // Loop through the components to find the CN
        for (String component : dnComponents) {
            String[] keyValue = component.split("=", 2); // Split only once to handle cases with '=' in the value
            if (keyValue.length == 2 && "CN".equalsIgnoreCase(keyValue[0].trim())) {
                // Return the value of CN, removing any leading/trailing spaces
                String cnValue = keyValue[1].trim();
                // Return only the part before the first comma if it exists
                int commaIndex = cnValue.indexOf(',');
                if (commaIndex != -1) {
                    return cnValue.substring(0, commaIndex).trim();
                }
                return cnValue;
            }
        }
        return null;
    }

    /**
     * Retrieves the alias of the first entry in the provided KeyStore.
     *
     * @param ks the KeyStore object from which to retrieve the alias
     * @return the alias of the first entry in the KeyStore, or {@code null} if the KeyStore is empty
     * @throws Exception if an error occurs while accessing the KeyStore
     */
    private static String getAlias(KeyStore ks) throws Exception {
        Enumeration<String> aliases = ks.aliases();
        if (aliases.hasMoreElements()) {
            return aliases.nextElement();
        }
        return null;
    }

    /**
     * Applies a digital signature to a PDF document.
     *
     * @param targetNodeRef the NodeRef pointing to the PDF document to be signed
     * @param params a map of parameters required for signing the PDF
     * @return a NodeRef pointing to the signed PDF
     */
    @Override
    public NodeRef signPDF(NodeRef targetNodeRef, Map<String, Serializable> params) {
        NodeRef privateKey = (NodeRef) params.get(PARAM_PRIVATE_KEY);
        String position = (String) params.get(PARAM_POSITION);
        String visibility = (String) params.get(PARAM_VISIBILITY);
        String keyPassword = (String) params.get(PARAM_KEY_PASSWORD);
        int height = getInteger(params.get(PARAM_HEIGHT));
        int width = getInteger(params.get(PARAM_WIDTH));
        int pageNumber = getInteger(params.get(PARAM_PAGE));

        if (height == 0) {
            height = defaultHeight;
        }
        if (width == 0) {
            width = defaultWidth;
        }

        boolean appendToExisting = true;
        if (params.get(PARAM_NEW_REVISION) != null) {
            appendToExisting = Boolean.parseBoolean(String.valueOf(params.get(PARAM_NEW_REVISION)));
        }

        String storePassword = (String) params.get(PARAM_STORE_PASSWORD);

        int locationX = getInteger(params.get(PARAM_LOCATION_X));
        int locationY = getInteger(params.get(PARAM_LOCATION_Y));

        Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));

        File tempDir = null;
        ContentWriter writer = null;
        KeyStore ks = null;
        NodeRef destinationNode = null;
        PdfStamper stamper = null;
        FileOutputStream fout = null;

        try {
            ContentReader keyReader = getReader(privateKey);
            ks = loadKeyStore(keyReader.getContentInputStream(), storePassword);

            // Obtener el friendlyName del KeyStore
            String friendlyName = getFriendlyName(ks);

            if (friendlyName == null) {
                throw new AlfrescoRuntimeException("friendlyName was not found in the KeyStore");
            }

            String alias = getAlias(ks);

            PrivateKey key = getPrivateKey(ks, alias, keyPassword);
            Certificate[] chain = getCertificateChain(ks, alias);

            ContentReader pdfReader = getReader(targetNodeRef);
            PdfReader reader = new PdfReader(pdfReader.getContentInputStream());

            int numPages = reader.getNumberOfPages();
            if (pageNumber < 1 || pageNumber > numPages) {
                pageNumber = numPages;
            }

            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
            tempDir.mkdir();
            File file = new File(tempDir, ffs.getFileInfo(targetNodeRef).getName());

            fout = new FileOutputStream(file);

            if (appendToExisting) {
                stamper = PdfStamper.createSignature(reader, fout, '\0', tempDir, true);
            } else {
                stamper = PdfStamper.createSignature(reader, fout, '\0');
            }

            PdfSignatureAppearance sap = stamper.getSignatureAppearance();

            if (visibility.equalsIgnoreCase(VISIBILITY_VISIBLE)) {
                if (position != null && !position.trim().isEmpty() && !position.trim().equalsIgnoreCase(POSITION_MANUAL)) {
                    Rectangle pageRect = reader.getPageSizeWithRotation(pageNumber);
                    sap.setVisibleSignature(positionSignature(position, pageRect, width, height), pageNumber, null);
                } else {
                    sap.setVisibleSignature(new Rectangle(locationX, locationY, locationX + width, locationY - height), pageNumber, null);
                }
            }

            ExternalDigest digest = new BouncyCastleDigest();
            ExternalSignature signature = new PrivateKeySignature(key, DigestAlgorithms.SHA256, "BC");

            MakeSignature.signDetached(sap, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);

            String fileName = getFilename(params, targetNodeRef);

            destinationNode = createDestinationNode(fileName, (NodeRef) params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
            writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

            writer.setEncoding(pdfReader.getEncoding());
            writer.setMimetype(FILE_MIMETYPE);
            writer.putContent(file);

            file.delete();

            if (useSignatureAspect) {
                ns.addAspect(destinationNode, PDFSignModel.ASPECT_SIGNED, new HashMap<QName, Serializable>());
                ns.setProperty(destinationNode, PDFSignModel.PROP_SIGNATUREDATE, new Date());
                ns.setProperty(destinationNode, PDFSignModel.PROP_SIGNEDBY, AuthenticationUtil.getRunAsUser());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tempDir != null) {
                try {
                    for (File tempFile : tempDir.listFiles()) {
                        tempFile.delete();
                    }
                    tempDir.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return destinationNode;
    }

    /**
     * Retrieves a ContentReader for the specified node.
     *
     * @param nodeRef the NodeRef pointing to the content node
     * @return the ContentReader
     */
    private ContentReader getReader(NodeRef nodeRef) {
        if (!ns.exists(nodeRef)) {
            throw new AlfrescoRuntimeException("NodeRef: " + nodeRef + " does not exist");
        }

        QName typeQName = ns.getType(nodeRef);
        if (!ds.isSubClass(typeQName, ContentModel.TYPE_CONTENT)) {
            throw new AlfrescoRuntimeException("The selected node is not a content node");
        }

        ContentReader contentReader = cs.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (contentReader == null) {
            throw new AlfrescoRuntimeException("The content reader for NodeRef: " + nodeRef + " is null");
        }

        return contentReader;
    }

    /**
     * Creates or retrieves a destination node for the signed PDF.
     *
     * @param filename the name of the destination file
     * @param destinationParent the parent folder for the destination node
     * @param target the original node
     * @param inplace whether to overwrite the original node
     * @return the NodeRef of the destination node
     */
    private NodeRef createDestinationNode(String filename, NodeRef destinationParent, NodeRef target, boolean inplace) {
        NodeRef destinationNode;

        if (inplace) {
            return target;
        }

        if (createNew) {
            FileInfo fileInfo = ffs.create(destinationParent, filename, ContentModel.TYPE_CONTENT);
            destinationNode = fileInfo.getNodeRef();
        } else {
            try {
                FileInfo fileInfo = ffs.copy(target, destinationParent, filename);
                destinationNode = fileInfo.getNodeRef();
            } catch (FileNotFoundException fnf) {
                throw new AlfrescoRuntimeException(fnf.getMessage(), fnf);
            }
        }

        return destinationNode;
    }

    /**
     * Converts a Serializable value to an integer.
     *
     * @param val the Serializable value
     * @return the integer value, or 0 if the conversion fails
     */
    private int getInteger(Serializable val) {
        if (val == null) {
            return 0;
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    /**
     * Generates a filename for the signed PDF based on parameters.
     *
     * @param params the parameters map
     * @param targetNodeRef the NodeRef of the target node
     * @return the filename
     */
    private String getFilename(Map<String, Serializable> params, NodeRef targetNodeRef) {
        Serializable providedName = params.get(PARAM_DESTINATION_NAME);
        String fileName;
        if (providedName != null) {
            fileName = String.valueOf(providedName);
            if (!fileName.endsWith(FILE_EXTENSION)) {
                fileName = fileName + FILE_EXTENSION;
            }
        } else {
            fileName = String.valueOf(ns.getProperty(targetNodeRef, ContentModel.PROP_NAME));
        }
        return fileName;
    }

    /**
     * Calculates the position and dimensions for the signature based on the specified position.
     *
     * @param position the position of the signature
     * @param pageRect the rectangle representing the page size
     * @param width the width of the signature
     * @param height the height of the signature
     * @return the Rectangle representing the position of the signature
     */
    private Rectangle positionSignature(String position, Rectangle pageRect, int width, int height) {
        float pageHeight = pageRect.getHeight();
        float pageWidth = pageRect.getWidth();

        Rectangle r = null;

        switch (position) {
            case POSITION_BOTTOMLEFT:
                r = new Rectangle(0, height, width, 0);
                break;
            case POSITION_BOTTOMRIGHT:
                r = new Rectangle(pageWidth - width, height, pageWidth, 0);
                break;
            case POSITION_TOPLEFT:
                r = new Rectangle(0, pageHeight, width, pageHeight - height);
                break;
            case POSITION_TOPRIGHT:
                r = new Rectangle(pageWidth - width, pageHeight, pageWidth, pageHeight - height);
                break;
            case POSITION_CENTER:
                r = new Rectangle((pageWidth / 2) - (width / 2), (pageHeight / 2) - (height / 2), (pageWidth / 2) + (width / 2), (pageHeight / 2) + (height / 2));
                break;
        }

        return r;
    }

    /**
     * Sets the ServiceRegistry used by this service.
     *
     * @param serviceRegistry the ServiceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        ns = serviceRegistry.getNodeService();
        cs = serviceRegistry.getContentService();
        ffs = serviceRegistry.getFileFolderService();
        ds = serviceRegistry.getDictionaryService();
        ps = serviceRegistry.getPersonService();
        as = serviceRegistry.getAuthenticationService();
    }

    /**
     * Sets whether to use the signature aspect.
     *
     * @param useSignatureAspect true to use the signature aspect, false otherwise
     */
    public void setUseSignatureAspect(boolean useSignatureAspect) {
        this.useSignatureAspect = useSignatureAspect;
    }
}
