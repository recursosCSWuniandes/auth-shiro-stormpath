package co.edu.uniandes.csw.auth.stormpath;

import java.util.Properties;

/**
 * Retrieves API Key Id and Secret from Environment Variables
 *
 * @author af.esguerra10
 */
public class ApiKeyProperties extends Properties {

    public ApiKeyProperties() {
        super.put("apiKey.id", System.getenv("STORMPATH_API_KEY_ID"));
        super.put("apiKey.secret", System.getenv("STORMPATH_API_KEY_SECRET"));
    }
}
