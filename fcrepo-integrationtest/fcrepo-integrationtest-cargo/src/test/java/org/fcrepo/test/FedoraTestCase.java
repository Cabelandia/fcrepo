/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.xml.transform.TransformerFactory;

import net.sf.saxon.FeatureKeys;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import org.fcrepo.client.FedoraClient;
import org.fcrepo.common.FedoraTestConstants;
import org.fcrepo.server.config.ServerConfiguration;
import org.fcrepo.server.config.ServerConfigurationParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for Fedora Test Cases
 *
 * @author Edwin Shin
 */
public abstract class FedoraTestCase
        extends XMLTestCase
        implements FedoraTestConstants {
	
	private static Logger logger = LoggerFactory.getLogger(FedoraTestCase.class);
	
    public static String ssl = "http";

    public FedoraTestCase() {
        super();
        TransformerFactory factory = XMLUnit.getTransformerFactory();
        if (factory.getClass().getName()
                .equals("net.sf.saxon.TransformerFactoryImpl")) {
            factory.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
        }
    }

    public FedoraTestCase(String name) {
        super(name);
        TransformerFactory factory = XMLUnit.getTransformerFactory();
        if (factory.getClass().getName()
                .equals("net.sf.saxon.TransformerFactoryImpl")) {
            factory.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
        }
    }

    public static ServerConfiguration getServerConfiguration() {
        try {
            return new ServerConfigurationParser(new FileInputStream(FCFG))
                    .parse();
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    public static String getDemoBaseURL() {
        if (System.getProperty("fedora.baseURLDemo") != null) {
            return System.getProperty("fedora.baseURLDemo");
        } else {
            return getProtocol() + "://" + getHost() + ":" + getPort() + "/"
                    + getDemoAppServerContext();
        }
    }

    public static String getBaseURL() {
        if (System.getProperty("fedora.baseURL") != null) {
        		String baseURL = System.getProperty("fedora.baseURL");
        		//logger.debug("Discovered fedora.baseURL property: " + baseURL);
            return baseURL;
        } else {
        		String baseURL = getProtocol() + "://" + getHost() + ":" + getPort() + "/"
                        + getFedoraAppServerContext();
        		//logger.debug("Constructed baseURL: " + baseURL);
            return baseURL;
            
        }
    }

    public static String getHost() {
        return getServerConfiguration().getParameter("fedoraServerHost");
    }

    public static String getPort() {
        String port = null;
        if (getProtocol().equals("http")) {
            port =
                    getServerConfiguration().getParameter("fedoraServerPort");
        } else {
            port =
                    getServerConfiguration().getParameter("fedoraRedirectPort");
        }
        return port;
    }

    public static String getFedoraAppServerContext() {
        if (System.getProperty("fedoraAppServerContext") != null) {
            return System.getProperty("fedoraAppServerContext");
        }
        return getServerConfiguration().getParameter("fedoraAppServerContext");
    }

    public static String getDemoAppServerContext() {
        return getServerConfiguration()
                .getParameter("fedoraDemoAppServerContext") != null ? getServerConfiguration()
                .getParameter("fedoraDemoAppServerContext")
                : "fedora-demo";
    }

    // hack to dynamically set protocol based on settings in beSecurity
    // Settings for fedoraInternalCall-1 should have callSSL=true when server is secure
    public static String getProtocol() {
        BufferedReader br = null;
        try {
            br =
                    new BufferedReader(new InputStreamReader(new FileInputStream(BESECURITY)));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.indexOf("role=\"fedoraInternalCall-1\"") > 0
                        && line.indexOf("callSSL=\"true\"") > 0) {
                    ssl = "https";
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("fedora.home: " + FEDORA_HOME);
            fail("beSecurity file Not found: " + BESECURITY.getAbsolutePath());
        } finally {
            try {
                if (br != null) {
                    br.close();
                    br = null;
                }
            } catch (Exception e) {
                System.out.println("Unable to close BufferdReader");
            }
        }
        return ssl;
    }

    public static String getUsername() {
        return FEDORA_USERNAME;
    }

    public static String getPassword() {
        return FEDORA_PASSWORD;
    }

    public static FedoraClient getFedoraClient() throws Exception {
    	/*	logger.debug(
    				"Calling getFedoraClient(String baseURL, String username, String password) with baseURL: "
    + getBaseURL() + " username: " + getUsername() + " and password: " +getPassword());*/
        return getFedoraClient(getBaseURL(), getUsername(), getPassword());
    }

    public static FedoraClient getFedoraClient(String baseURL,
                                               String username,
                                               String password)
            throws Exception {
    		/*logger.debug("FedoraTestCase.getFedoraClient() called with baseURL: " +  baseURL);
    		logger.debug("FedoraTestCase.getFedoraClient() called with username: " + username);
    		logger.debug("FedoraTestCase.getFedoraClient() called with password: " + password);   
    		*/return new FedoraClient(baseURL, username, password);
    }
}
