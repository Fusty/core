package com.dotcms.rest.api.v1.sites.rules;

import com.dotcms.TestBase;
import com.dotcms.repackage.javax.ws.rs.client.Client;
import com.dotcms.repackage.javax.ws.rs.client.Entity;
import com.dotcms.repackage.javax.ws.rs.client.WebTarget;
import com.dotcms.repackage.javax.ws.rs.core.MediaType;
import com.dotcms.repackage.javax.ws.rs.core.Response;
import com.dotcms.repackage.org.apache.commons.httpclient.HttpStatus;
import com.dotcms.repackage.org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import com.dotcms.repackage.org.junit.After;
import com.dotcms.repackage.org.junit.Test;
import com.dotcms.rest.RestClientBuilder;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.portlets.rules.actionlet.CountRequestsActionlet;
import com.dotmarketing.portlets.rules.conditionlet.MockTrueConditionlet;
import com.dotmarketing.portlets.rules.model.*;
import com.dotmarketing.portlets.rules.conditionlet.UsersCountryConditionlet;
import com.dotmarketing.portlets.rules.model.Condition;
import com.dotmarketing.portlets.rules.model.ConditionGroup;
import com.dotmarketing.portlets.rules.model.Rule;
import com.dotmarketing.portlets.rules.model.RuleAction;
import com.dotmarketing.servlets.test.ServletTestRunner;
import com.dotmarketing.util.json.JSONArray;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;
import com.liferay.portal.model.User;

import javax.servlet.http.HttpServletRequest;

import static com.dotcms.repackage.org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class ConditionGroupsRestAPITest extends TestBase {

    private HttpServletRequest request;
    private String serverName;
    private Integer serverPort;
    private User user;
    Host defaultHost;
    Client client;

    public ConditionGroupsRestAPITest() {
        request = ServletTestRunner.localRequest.get();
        serverName = request.getServerName();
        serverPort = request.getServerPort();
        HostAPI hostAPI = APILocator.getHostAPI();

        //Setting the test user
        try{
	        user = APILocator.getUserAPI().getSystemUser();
	        defaultHost = hostAPI.findDefaultHost(user, false);
        }catch(DotDataException dd){
        	dd.printStackTrace();
        }catch(DotSecurityException ds){
        	ds.printStackTrace();
        }
        
        client = RestClientBuilder.newClient();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("admin@dotcms.com", "admin");
        client.register(feature);

    }

    /**
     * Used to create as many rules as needed for testing, based on simple rule creation
     * @param ruleID
     * @return
     * @throws JSONException
     */
    private String createRule(String ruleID) throws JSONException{
    	//setup
    	JSONObject ruleJSON = new JSONObject();
        ruleJSON.put("name", ruleID);
        ruleJSON.put("enabled", "true");
        ruleJSON.put("fireOn", Rule.FireOn.EVERY_PAGE.toString());

        // client call
        WebTarget target = client.target("http://" + serverName + ":" + serverPort + "/api/v1");
        
        // create
        Response response = target.path("/sites/" + defaultHost.getIdentifier() + "/rules").request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(ruleJSON.toString()));
        
        // response
        String responseStr = response.readEntity(String.class);
    	JSONObject responseJSON = new JSONObject(responseStr);
    	String rule = (String) responseJSON.get("id");
    	
    	return rule;
    }
    
    /**
     * Used to delete as many rules as needed for testing
     * @param ruleID
     * @return
     * @throws JSONException
     */
    private void deleteRule(String ruleID){
        WebTarget target = client.target("http://" + serverName + ":" + serverPort + "/api/v1");
    	Response response = target.path("/sites/" + defaultHost.getIdentifier() + "/rules/" + ruleID).request(MediaType.APPLICATION_JSON_TYPE).delete();
    }
    
    @Test
    /**
     * Testing basic condition group creation... should succeed
     * @throws JSONException
     */
    public void testConditionGroup() throws JSONException{
    	Response response = null;
    	
    	// rules setup
    	String rule = createRule("testRuleConditionGroup");
    	
    	// condition testing
    	JSONObject groupJSON = new JSONObject();
    	groupJSON.put("operator", Condition.Operator.AND.name());

    	WebTarget target = client.target("http://" + serverName + ":" + serverPort + "/api/v1");
    	response = target.path("/sites/" + defaultHost.getIdentifier() + "/rules/" + rule + "/conditionGroups").request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(groupJSON.toString()));

    	assertTrue(response.getStatus() == HttpStatus.SC_OK);

    	String responseStr = response.readEntity(String.class);
    	JSONObject responseJSON = new JSONObject(responseStr);
    	String group = (String) responseJSON.get("id");

    	response = target.path("/sites/" + defaultHost.getIdentifier() + "/rules/" + rule + "/conditionGroups/" + group).request(MediaType.APPLICATION_JSON_TYPE).delete();
    	
        assertTrue(response.getStatus() == HttpStatus.SC_NO_CONTENT);
    	
        // rules clean up
    	deleteRule(rule);   	
    }
}
