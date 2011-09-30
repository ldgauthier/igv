/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.gs;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.log4j.Logger;
import org.broad.igv.util.IGVHttpClientUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jrobinso
 * @date Jun 9, 2011
 * <p/>
 * <p/>
 * Prod servers
 * identityServerUrl=https://identity.genomespace.org/identityServer/basic
 * atmServer=https://atm.genomespace.org/atm
 * dmServer=https://dm.genomespace.org/datamanager
 * Test servers
 * identityServerUrl=https://identitytest.genomespace.org:8443/identityServer/basic
 * atmServer=https://atmtest.genomespace.org:8443/atm
 * dmServer=https://dmtest.genomespace.org:8444/datamanager
 * Dev servers
 * identityServerUrl=https://localhost:8443/identityServer/basic
 * atmServer=https://localhost:8443/atm
 * dmServer=https://dmtest.genomespace.org:8444/datamanager
 */
public class GSUtils {
    static final Logger log = Logger.getLogger(GSUtils.class);

    public static final String AUTH_TOKEN_COOKIE_NAME = "gs-token";
    public static final String AUTH_TOKEN_COOKIE_DEFAULT_PATH = "/";


    /*
    * Directory and filenames to save the token and username to facilitate SSO
    */
    private static String tokenSaveDir = ".gs";
    private static String tokenSaveFileName = ".gstoken";
    private static String usernameSaveFileName = ".gsusername";
    public static final String GENOME_SPACE_ID_SERVER = "identitytest.genomespace.org";


    public static final String identityServerUrl = "https://identitytest.genomespace.org:8443/identityServer/basic";
    public static String dmServer = "https://dmtest.genomespace.org:8444/datamanager/";
    public static final String atmServer = "https://atmtest.genomespace.org:8443/atm/";

    /*
    public static final String identityServerUrl = "https://identitytest.genomespace.org/identityServer/basic";
    public static String dmServer = "https://dmtest.genomespace.org/datamanager/";
    public static final String atmServer = "https://atmtest.genomespace.org/atm";
    */

    public static void checkForCookie(DefaultHttpClient httpClient, String host) {

        File file = getTokenFile();
        if (file.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String token = br.readLine();
                setGSAuthenticationToken(httpClient, host, token);
            } catch (IOException e) {
                log.error("Error reading GS cookie", e);
            } finally {
                if (br != null) try {
                    br.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public static void setGSAuthenticationToken(DefaultHttpClient httpClient, String host, String token) {
        BasicClientCookie cookie = new BasicClientCookie(AUTH_TOKEN_COOKIE_NAME, token);
        cookie.setDomain(getCookieDomainPattern(host));
        cookie.setPath(AUTH_TOKEN_COOKIE_DEFAULT_PATH);

        CookieStore cookieStore = new GSCookieStore();
        cookieStore.addCookie(cookie);
        httpClient.setCookieStore(cookieStore);
    }


    private static String getCookieDomainPattern(String serverName) {
        int firstDotIdx = serverName.indexOf(".");
        if (firstDotIdx > 0 && (firstDotIdx != serverName.lastIndexOf("."))) {
            return serverName.substring(serverName.indexOf("."));
        } else {
            return serverName;
        }
    }

    private static File getTokenSaveDir() {
        String userDir = System.getProperty("user.home");
        File gsDir = new File(userDir, tokenSaveDir);
        if (!gsDir.exists()) {
            gsDir.mkdir();
        }
        return gsDir;
    }

    public static File getTokenFile() {
        File gsDir = getTokenSaveDir();
        return (gsDir != null && gsDir.exists()) ? new File(gsDir, tokenSaveFileName) : null;
    }

    public static File getUsernameFile() {
        File gsDir = getTokenSaveDir();
        return (gsDir != null && gsDir.exists()) ? new File(gsDir, usernameSaveFileName) : null;
    }

    public static void saveGSLogin(String newToken, String username) {
        BufferedWriter bw = null;

        File gsDir = getTokenSaveDir();
        if (!gsDir.isDirectory()) {
            log.error("Could not store token for SSO.  File " + gsDir.getAbsolutePath() + "exists and is not a directory.");
            return; // someone made a file with this name...
        }
        File tokenFile = getTokenFile();
        if (tokenFile.exists()) tokenFile.delete();

        File userFile = getUsernameFile();
        if (userFile.exists()) userFile.delete();

        writeToFile(newToken, tokenFile);
        writeToFile(username, userFile);
    }

    public static void logout() {
        File userfile = getUsernameFile();
        if (userfile.exists()) {
            userfile.delete();
        }
        File tokenFile = getTokenFile();
        if (tokenFile.exists()) {
            tokenFile.delete();
        }

        IGVHttpClientUtils.getInstance().removeGSCookie();

    }


    private static void writeToFile(String line, File aFile) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(aFile));
            bw.write(line);

            bw.close();
        } catch (Exception e) {
            log.error("Failed to save the token for later Single Sign on", e);
        } finally {
            try {
                if (bw != null) bw.close();
            } catch (Exception e) {
            }
        }
    }


    public static String getCachedUsernameForSSO() throws IOException {
        String user = null;
        BufferedReader br = null;
        try {
            File tokenFile = getUsernameFile();
            if (tokenFile.exists()) {
                br = new BufferedReader(new FileReader(tokenFile));
                user = br.readLine().trim();
            }
        } finally {
            try {
                if (br != null) br.close();
            } catch (Exception e) {
            }
        }
        return user;
    }


    public static boolean isGenomeSpace(String path) {
        return path.contains("genomespace.org");
    }

    static class GSCookieStore implements CookieStore {

        List<Cookie> cookieList = new ArrayList();

        public void addCookie(Cookie cookie) {
            cookieList.add(cookie);
        }

        public List<Cookie> getCookies() {
            return cookieList;
        }

        public boolean clearExpired(Date date) {
            boolean removed = false;
            List<Cookie> newCookieList = new ArrayList(cookieList.size());
            for (Cookie cookie : cookieList) {
                if (cookie.getExpiryDate().after(date)) {
                    newCookieList.add(cookie);
                } else {
                    removed = true;
                }
            }
            cookieList = newCookieList;
            return removed;
        }

        public void clear() {
            cookieList.clear();
        }
    }
}