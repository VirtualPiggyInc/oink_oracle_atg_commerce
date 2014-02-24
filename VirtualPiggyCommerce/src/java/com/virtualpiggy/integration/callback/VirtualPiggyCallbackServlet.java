package com.virtualpiggy.integration.callback;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import atg.nucleus.Nucleus;
import atg.nucleus.logging.ApplicationLogging;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.ServletUtil;

/**
 * This servlet class is the entry point for Virtual Piggy callback. It delegates the control to VirtualPiggyCallbackHandler to process the callback.
 * 
 * @author tarun
 *
 */
public class VirtualPiggyCallbackServlet extends HttpServlet {
	
	private static final long serialVersionUID = 2430677119538200218L;
	
	private static final String VIRTUAL_PIGGY_CALLBACK_HANDLER_COMPONENT_PATH = "/com/virtualpiggy/integration/callback/VirtualPiggyCallbackHandler";

	public VirtualPiggyCallbackServlet(){
	}
	
	/**
	 * Handle GET callback request. Delegates to doPost.
	 */
	@Override
	protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse)
			throws ServletException, IOException {
		doPost(pRequest, pResponse);
	}
	
	/**
	 * Handle POST callback request. Get Dynamo Request and Response and delegate control to callback handler.
	 * 
	 */
	@Override
	protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse)
			throws ServletException, IOException {
		
		VirtualPiggyCallbackHandler callbackHandler = (VirtualPiggyCallbackHandler)Nucleus.getGlobalNucleus().resolveName(VIRTUAL_PIGGY_CALLBACK_HANDLER_COMPONENT_PATH);
		DynamoHttpServletRequest request = ServletUtil.getDynamoRequest(getServletContext(), pRequest, pResponse);
		DynamoHttpServletResponse response = ServletUtil.getDynamoResponse(pRequest, pResponse);
		ApplicationLogging logger = request.getLog();
		try{
			callbackHandler.service(request, response);
		}catch(Exception ex){
			if(logger != null){
				logger.logError("VirtualPiggyCallbackServlet::::" + ex.getMessage());
			}
			throw new ServletException(ex);
		}
	}
}
