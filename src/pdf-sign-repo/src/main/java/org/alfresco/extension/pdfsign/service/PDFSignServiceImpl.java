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
import java.util.HashMap;
import java.util.Map;

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

    private KeyStore loadKeyStore(String keyType, InputStream keyStream, String storePassword) {
        try {
            KeyStore ks = KeyStore.getInstance(keyType);
            ks.load(keyStream, storePassword.toCharArray());
            return ks;
        } catch (Exception e) {
            System.out.println("Error loading KeyStore: " + e.getMessage());
            throw new AlfrescoRuntimeException("Error loading KeyStore", e);
        }
    }

    private PrivateKey getPrivateKey(KeyStore ks, String alias, String keyPassword) {
        try {
            Key key = ks.getKey(alias, keyPassword.toCharArray());
            if (key instanceof PrivateKey) {
                return (PrivateKey) key;
            } else {
                System.out.println("Key for alias " + alias + " is not a private key");
                throw new AlfrescoRuntimeException("Key for alias " + alias + " is not a private key");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving private key: " + e.getMessage());
            throw new AlfrescoRuntimeException("Error retrieving private key", e);
        }
    }

    private Certificate[] getCertificateChain(KeyStore ks, String alias) {
        try {
            Certificate[] certChain = ks.getCertificateChain(alias);
            if (certChain == null) {
                System.out.println("Certificate chain for alias " + alias + " is null");
                throw new AlfrescoRuntimeException("Certificate chain for alias " + alias + " is null");
            }
            return certChain;
        } catch (Exception e) {
            System.out.println("Error retrieving certificate chain: " + e.getMessage());
            throw new AlfrescoRuntimeException("Error retrieving certificate chain", e);
        }
    }

    @Override
    public NodeRef signPDF(NodeRef targetNodeRef, Map<String, Serializable> params) {
        NodeRef privateKey = (NodeRef) params.get(PARAM_PRIVATE_KEY);
        String location = (String) params.get(PARAM_LOCATION);
        String position = (String) params.get(PARAM_POSITION);
        String reason = (String) params.get(PARAM_REASON);
        String visibility = (String) params.get(PARAM_VISIBILITY);
        String keyPassword = (String) params.get(PARAM_KEY_PASSWORD);
        String keyType = (String) params.get(PARAM_KEY_TYPE);
        int height = getInteger(params.get(PARAM_HEIGHT));
        int width = getInteger(params.get(PARAM_WIDTH));
        int pageNumber = getInteger(params.get(PARAM_PAGE));

        System.out.println("privateKey: " + privateKey);
        System.out.println("location: " + location);
        System.out.println("position: " + position);
        System.out.println("reason: " + reason);
        System.out.println("visibility: " + visibility);
        System.out.println("keyPassword: " + keyPassword);
        System.out.println("keyType: " + keyType);
        System.out.println("height: " + height);
        System.out.println("width: " + width);
        System.out.println("pageNumber: " + pageNumber);

        boolean appendToExisting = true;
        if (params.get(PARAM_NEW_REVISION) != null) {
            appendToExisting = Boolean.valueOf(String.valueOf(params.get(PARAM_NEW_REVISION)));
        }

        System.out.println("appendToExisting: " + appendToExisting);

        String alias = (String) params.get(PARAM_ALIAS);
        String storePassword = (String) params.get(PARAM_STORE_PASSWORD);

        System.out.println("alias: " + alias);
        System.out.println("storePassword: " + storePassword);

        int locationX = getInteger(params.get(PARAM_LOCATION_X));
        int locationY = getInteger(params.get(PARAM_LOCATION_Y));

        System.out.println("locationX: " + locationX);
        System.out.println("locationY: " + locationY);

        Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));

        System.out.println("inplace: " + inplace);

        File tempDir = null;
        ContentWriter writer = null;
        KeyStore ks = null;
        NodeRef destinationNode = null;
        PdfStamper stamper = null;
        FileOutputStream fout = null;

        try {
            ContentReader keyReader = getReader(privateKey);
            ks = loadKeyStore(keyType, keyReader.getContentInputStream(), storePassword);
            System.out.println("Instancia de KeyStore obtenida");

            PrivateKey key = getPrivateKey(ks, alias, keyPassword);
            Certificate[] chain = getCertificateChain(ks, alias);
            System.out.println("Clave privada y cadena de certificados obtenidos");

            ContentReader pdfReader = getReader(targetNodeRef);
            PdfReader reader = new PdfReader(pdfReader.getContentInputStream());
            System.out.println("Lector de PDF inicializado");

            // If the page number is 0 because it couldn't be parsed or for
            // some other reason, set it to the first page, which is 1.
            // If the page number is negative, assume the intent is to "wrap".
            // For example, -1 would always be the last page.
            int numPages = reader.getNumberOfPages();
            if (pageNumber < 1 && pageNumber == 0) {
                pageNumber = 1; // use the first page
            } else {
                // page number is negative
                pageNumber = numPages + 1 + pageNumber;
                if (pageNumber <= 0) pageNumber = 1;
            }

            // if the page number specified is more than the num of pages,
            // use the last page
            if (pageNumber > numPages) {
                pageNumber = numPages;
            }
            System.out.println("Número de página calculado: " + pageNumber);

            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
            tempDir.mkdir();
            File file = new File(tempDir, ffs.getFileInfo(targetNodeRef).getName());
            System.out.println("Directorio y archivo temporales creados");

            fout = new FileOutputStream(file);

            if (appendToExisting) {
                stamper = PdfStamper.createSignature(reader, fout, '\0', tempDir, true);
            } else {
                stamper = PdfStamper.createSignature(reader, fout, '\0');
            }
            System.out.println("PdfStamper creado");

            PdfSignatureAppearance sap = stamper.getSignatureAppearance();
            sap.setReason(reason);
            sap.setLocation(location);

            if (visibility.equalsIgnoreCase(VISIBILITY_VISIBLE)) {
                if (position != null && !position.trim().equalsIgnoreCase("") && !position.trim().equalsIgnoreCase(POSITION_MANUAL)) {
                    Rectangle pageRect = reader.getPageSizeWithRotation(pageNumber);
                    sap.setVisibleSignature(positionSignature(position, pageRect, width, height), pageNumber, null);
                } else {
                    sap.setVisibleSignature(new Rectangle(locationX, locationY, locationX + width, locationY - height), pageNumber, null);
                }
            }
            System.out.println("Apariencia de firma configurada");

            ExternalDigest digest = new BouncyCastleDigest();
            ExternalSignature signature = new PrivateKeySignature(key, DigestAlgorithms.SHA256, "BC");

            MakeSignature.signDetached(sap, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
            System.out.println("Documento firmado");

            String fileName = getFilename(params, targetNodeRef);

            destinationNode = createDestinationNode(fileName, (NodeRef) params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
            writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
            System.out.println("Nodo de destino creado");

            writer.setEncoding(pdfReader.getEncoding());
            writer.setMimetype(FILE_MIMETYPE);
            writer.putContent(file);
            System.out.println("Contenido escrito en el nodo de destino");

            file.delete();
            System.out.println("Archivo temporal eliminado");

            if (useSignatureAspect) {
                ns.addAspect(destinationNode, PDFSignModel.ASPECT_SIGNED, new HashMap<QName, Serializable>());
                ns.setProperty(destinationNode, PDFSignModel.PROP_REASON, reason);
                ns.setProperty(destinationNode, PDFSignModel.PROP_LOCATION, location);
                ns.setProperty(destinationNode, PDFSignModel.PROP_SIGNATUREDATE, new java.util.Date());
                ns.setProperty(destinationNode, PDFSignModel.PROP_SIGNEDBY, AuthenticationUtil.getRunAsUser());
                System.out.println("Aspecto de firma añadido al nodo de destino");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    System.out.println("Excepción al cerrar FileOutputStream: " + e.getMessage());
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
                    System.out.println("Excepción al eliminar directorio temporal: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (Exception e) {
                    System.out.println("Excepción al cerrar PdfStamper: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return destinationNode;
    }

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

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        ns = serviceRegistry.getNodeService();
        cs = serviceRegistry.getContentService();
        ffs = serviceRegistry.getFileFolderService();
        ds = serviceRegistry.getDictionaryService();
        ps = serviceRegistry.getPersonService();
        as = serviceRegistry.getAuthenticationService();
    }

    public void setUseSignatureAspect(boolean useSignatureAspect) {
        this.useSignatureAspect = useSignatureAspect;
    }
}
