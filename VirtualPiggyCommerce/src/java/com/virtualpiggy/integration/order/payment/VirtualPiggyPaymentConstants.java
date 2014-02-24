package com.virtualpiggy.integration.order.payment;

import java.util.Locale;
import java.util.ResourceBundle;

import atg.core.i18n.LayeredResourceBundle;

public class VirtualPiggyPaymentConstants {
	public static final String PARAMETER_NAME_VP_TOKEN = "token";
	public static final String PARAMETER_NAME_VP_CHILD_ID = "childId";
	public static final String PARAMETER_NAME_TRANS_DESC = "transDesc";
	public static final String PARAMETER_NAME_PAYMENT_ACCOUNT_ID = "virtualPiggyPaymentSelect";

	public static final String ITEM_DESC_NAME_VIRTUAL_PIGGY_PAYMENT = "virtualPiggyPayment";
	
	public static final String PROPERTY_NAME_PAYMENT_TYPE = "type";
	
	public static final String PROPERTY_NAME_VP_TOKEN = "vpToken";
	public static final String PROPERTY_NAME_VP_CHILD_ID = "vpChildId";
	public static final String PROPERTY_NAME_TRANS_DESC = "vpTransDesc";
	public static final String PROPERTY_NAME_PAYMENT_ACCOUNT_ID = "vpPaymentAccId";
	
	public static final String PAYMENT_TRANSACTION_STATUS_PROCESSED = "Processed";
	public static final String PAYMENT_TRANSACTION_STATUS_APPROVAL_PENDING = "ApprovalPending";
	public static final String PAYMENT_TRANSACTION_STATUS_LIMITS_EXCEEDED = "LimitsExceeded";
	public static final String PAYMENT_TRANSACTION_STATUS_ERROR = "Error";
	
	//payment resource bundle name.
	public static final String PAYMENT_RESOURCE_BUNDLE_NAME = "com.virtualpiggy.integration.order.payment.PaymentResources";

	public static String getMsgString(String pMsgKey, Locale pLocale){
		ResourceBundle bundle = LayeredResourceBundle.getBundle(PAYMENT_RESOURCE_BUNDLE_NAME, pLocale);
		return bundle.getString(pMsgKey);
	}
}
