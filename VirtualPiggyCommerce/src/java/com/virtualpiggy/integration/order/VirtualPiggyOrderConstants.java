package com.virtualpiggy.integration.order;

import java.util.Locale;
import java.util.ResourceBundle;

import atg.core.i18n.LayeredResourceBundle;

public class VirtualPiggyOrderConstants {

	public static final String ORDER_STATE_PENDING_PARENT_APPROVAL = "PENDING_PARENT_APPROVAL";
	
    // Resource bundle name.
	public static final String VPIGGY_ORDER_RESOURCE_NAME = "com.virtualpiggy.integration.order.OrderResources";
	
	//Resource Bundle.
	private static java.util.ResourceBundle mResourceBundle = LayeredResourceBundle.getBundle(VPIGGY_ORDER_RESOURCE_NAME,
	      atg.service.dynamo.LangLicense.getLicensedDefault());

	public static String getMsgString(String pMsgKey){
		return mResourceBundle.getString(pMsgKey);
	}

	public static String getMsgString(String pMsgKey, Locale pLocale){
		ResourceBundle bundle = LayeredResourceBundle.getBundle(VPIGGY_ORDER_RESOURCE_NAME, pLocale);
		return bundle.getString(pMsgKey);
	}

}
