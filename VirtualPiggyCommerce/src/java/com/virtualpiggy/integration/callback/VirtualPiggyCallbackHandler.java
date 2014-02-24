package com.virtualpiggy.integration.callback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.transaction.TransactionManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderManager;
import atg.commerce.order.PaymentGroupImpl;
import atg.commerce.states.OrderStates;
import atg.commerce.states.PaymentGroupStates;
import atg.commerce.util.NoLockNameException;
import atg.commerce.util.TransactionLockFactory;
import atg.commerce.util.TransactionLockService;
import atg.core.util.StringUtils;
import atg.droplet.DropletException;
import atg.dtm.TransactionDemarcation;
import atg.dtm.TransactionDemarcationException;
import atg.nucleus.ServiceException;
import atg.service.lockmanager.ClientLockManager;
import atg.service.lockmanager.DeadlockException;
import atg.service.lockmanager.LockManagerException;
import atg.service.pipeline.PipelineManager;
import atg.service.pipeline.RunProcessException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.DynamoServlet;

import com.virtualpiggy.integration.order.VirtualPiggyOrderConstants;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPayment;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentConstants;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentStatus;
import com.virtualpiggy.integration.purchase.VirtualPiggyPurchaseProcessHelper;
import com.virtualpiggy.integration.services.VirtualPiggyServiceConfiguration;

/**
 * This class processes Virtual Piggy callback that either approves or rejects a child/parent's saved order using ATG OOTB order approval process.
 * 
 * @author tarun
 *
 */
public class VirtualPiggyCallbackHandler extends DynamoServlet {

	private ClientLockManager clientLockManager;
	private PipelineManager pipelineManager;
	private OrderManager orderManager;

	//List of Success status values.
	private List<String> mSuccessStatuses;
	//List of Failure status values.
	private List<String> mRejectStatuses;
	// ATG Order States
	private OrderStates orderStates;
	private PaymentGroupStates paymentGroupStates;

	private VirtualPiggyServiceConfiguration VPConfiguration;
	//Transaction lock factory used to acquire lock on profile to avoid multiple callbacks processing the same profile/order.
	private TransactionLockFactory mTransactionLockFactory;

	//http post param names.
	private static final String PARAM_NAME_MERCHANT_ID = "MerchantIdentifier";
	private static final String PARAM_NAME_STATUS = "Status";
	private static final String PARAM_NAME_REFERENCE_ID = "ReferenceId";
	private static final String PARAM_NAME_DATA = "Data";
	private static final String PARAM_INVALID_CALLBACK_STATUS = "invalidCallbackStatus";
	private static final String PARAM_PIPELINE_ERROR = "vpProcessOrderError";	
	private static final String VP_PROCESS_ORDER_CHAIN_NAME = "vpProcessOrder";

	private static final String XML_DATA_ELEMENT_NAME_WITH_ORDER_ID_INFO = "cart-shipment";
	private static final String XML_DATA_ORDER_ID_ATTRIBUTE_NAME = "shipment-reference";
	Map errorMap = new HashMap();

	/**
	 * This method processes the callback and based on the callback status, it will either continue or reject the fulfillment process. 
	 * If the order/payment is approved by Virtual Piggy, the order is marked as SUBMITTED and continues to Fulfillment Process. It also settles the payment for One Step Merchant. 
	 * If the order/payment is not approved by Virtual Piggy, the order is markes as REMOVED. 
	 * In case of any error, the order is marked as PENDING_MERCHANT_ACTION.
	 * 
	 */
	@Override
	public void service(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException, IOException {
		
		
		boolean continueFulfillment = false;
		String merchId = pRequest.getParameter(PARAM_NAME_MERCHANT_ID);
		vlogDebug("VirtualPiggyCallbackHandler->merchId=" + merchId);
		String transStatus = pRequest.getParameter(PARAM_NAME_STATUS);
		vlogDebug("VirtualPiggyCallbackHandler->status=" + transStatus);
		String orderId = getOrderIdFromRequest(pRequest);
		vlogDebug ("Order ID From Request : " +orderId);
		if(StringUtils.isEmpty(orderId)){
			//errorMap.put(PARAM_INVALID_ORDER_ID, "Order_Id could not be retrieved from call back response.");
			throw new ServletException("Order_Id could not be retrieved from call back response.");
		}	
		//check if the order is approved or rejected by Virtual Piggy.
		Boolean bStatus = isOrderTransactionApproved(transStatus);
		Order curOrder=null;
		vlogDebug("Transaction Status " + bStatus);
		if(bStatus != null ) {
			try {
				curOrder = getOrderManager().loadOrder(orderId);
			} catch (CommerceException e1) {
				if(isLoggingError())
					logError(e1);	

			}
			if (curOrder == null)
				throw new ServletException("error loading order with id=" + orderId);
			vlogDebug("Transaction Approved ??? " + bStatus.booleanValue());
			if(bStatus.booleanValue()) {				
				//Get Virtual Piggy Payment Group From Order
				VirtualPiggyPayment pg = getVPPaymentGroupFromOrder(curOrder);
				if(curOrder.getState() ==  getOrderStates().getStateValue(VirtualPiggyOrderConstants.ORDER_STATE_PENDING_PARENT_APPROVAL)) {
					vlogDebug("Order was in Parent Approval, So Submit the Order, Update Order, Continue Fulfillment");
					if(getVPConfiguration().isOneStepPaymentMerchant()) {	
						vlogDebug("One Step Merchant.. So Add Debit Status as well.. ");
						addDebitStatusToPaymentGroup(pg);
					}						
					setStateandUpdateOrder(curOrder,orderId,OrderStates.SUBMITTED);
					continueFulfillment = true;
				}
				else {
					vlogDebug("No Pending Approval.. Nothing to do..");
				}				
			}
			else {
				//Update Order status to "REMOVED"
				setStateandUpdateOrder(curOrder,orderId,OrderStates.REMOVED);
			}
			
		}
		else{
			//if the callback status could not be determined, process as error.
			errorMap.put(PARAM_INVALID_CALLBACK_STATUS, "Error Callback Status");
		}		
		vlogDebug("Continue Fulfillment" + continueFulfillment);
		if(continueFulfillment) {
			try {
				HashMap params = new HashMap();
				params.put("Order", curOrder);
				params.put("OrderManager", getOrderManager());
				getPipelineManager().runProcess(VP_PROCESS_ORDER_CHAIN_NAME, params);
			} catch (RunProcessException exc) {
				errorMap.put(PARAM_PIPELINE_ERROR, exc.getMessage());
			}
		}
		
		if (!errorMap.isEmpty()) {
			setStateandUpdateOrder(curOrder,orderId,OrderStates.PENDING_MERCHANT_ACTION);
		}
	}
	
	
	
	
	/**
	 * This method sets the order state to given state and updates the order.
	 * 
	 * @param pCurrentOrder - Order to be updated.
	 * @param pOrderId 
	 * @param pState - State to be set. 
	 * @throws ServletException
	 */
	private void setStateandUpdateOrder(Order pCurrentOrder, String pOrderId, String pState) throws ServletException {
		TransactionLockService tls = null;
			try {
				String profileId = getProfileId(pOrderId);
				tls = getTransactionLockFactory().getServiceInstance(profileId, this);
				tls.acquireTransactionLock();
			} catch (DeadlockException e) {
				logError(e.getMessage());
			} catch (NoLockNameException e) {
				logError(e.getMessage());
			}
			
			TransactionDemarcation td = new TransactionDemarcation();
			try {
				td.begin(getTransactionManager(), TransactionDemarcation.REQUIRED);			
				synchronized (pCurrentOrder) {	
					if(OrderStates.SUBMITTED.equalsIgnoreCase(pState)) {
						pCurrentOrder.setSubmittedTime(System.currentTimeMillis());
						pCurrentOrder.setState(getOrderStates().getStateValue(OrderStates.SUBMITTED));
					}
					else if (OrderStates.REMOVED.equalsIgnoreCase(pState)) {
						pCurrentOrder.setState(getOrderStates().getStateValue(OrderStates.REMOVED));
					}
					else if(OrderStates.PENDING_MERCHANT_ACTION.equalsIgnoreCase(pState)) {
						pCurrentOrder.setState(getOrderStates().getStateValue(OrderStates.PENDING_MERCHANT_ACTION));
						pCurrentOrder.setStateDetail(getErrorMessage(errorMap));
					}
					getOrderManager().updateOrder(pCurrentOrder);	
				}
			} catch (TransactionDemarcationException t) {
				if(isLoggingError())
					logError(t);
			} catch (RuntimeException r) {
				if(isLoggingError())
					logError(r);
			} catch (CommerceException cex) {
				if(isLoggingError())
					logError(cex);	
				try {
					getTransactionManager().getTransaction().setRollbackOnly();
				}
				catch (javax.transaction.SystemException se) {
					if(isLoggingError())
						logError(se);
				}
			}
			finally {
				try {
					td.end();
				}
				catch (TransactionDemarcationException tde) {
					if(isLoggingError())
						logError(tde);
				}
				if(tls != null){
					try {
						tls.releaseTransactionLock();
					} catch (LockManagerException e) {
						logError("VPCallbackHandler->" + e.getMessage());
					}
				}

			}
		
	}
	
	

	/**
	 * Returns the Profile Id from the given Order Id.  
	 * 
	 * @param pOrderId
	 * @return retrieved Profile Id. 
	 * @throws ServletException
	 */
	private String getProfileId(String pOrderId) throws ServletException{
		Order tmpOrder;
		String profileId = "";
		try {
			tmpOrder = getOrderManager().loadOrder(pOrderId);
		} catch (CommerceException ex) {
			logError(ex);
			throw new ServletException("error loading order with id=" + pOrderId);
		}
		if(tmpOrder == null){
			throw new ServletException("error loading order with id=" + pOrderId);
		}
		profileId = tmpOrder.getProfileId();
		tmpOrder = null;
		return profileId;
	}
	
	/**
	 * 
	 */

	/**
	 * This method adds the Debit Status to Payment Group associated with Order and mark the Payment Group as Settled. 
	 * 
	 * @param pPayGroup
	 */
	private void addDebitStatusToPaymentGroup(VirtualPiggyPayment pPayGroup){
		//add debit status and mark the payment group state as Settled.
		VirtualPiggyPaymentStatus vpDebitStatus = new VirtualPiggyPaymentStatus();
		List authStatuses = pPayGroup.getAuthorizationStatus();
		VirtualPiggyPaymentStatus vpAuthStatus = null;
		if(authStatuses != null && authStatuses.size() > 0){
			vpAuthStatus = (VirtualPiggyPaymentStatus)authStatuses.get(authStatuses.size() - 1);
			VirtualPiggyPurchaseProcessHelper.copyStatusValues(vpAuthStatus, vpDebitStatus, vpAuthStatus.getVpTransactionStatus());
		}
		pPayGroup.addDebitStatus(vpDebitStatus);
		pPayGroup.setAmountDebited(pPayGroup.getAmount());
		pPayGroup.setState(getPaymentGroupStates().getStateValue(PaymentGroupStates.SETTLED));
	}
	
	

	/**
	 *  Returns Virtual Piggy Payment Group from the Order. 
	 * 
	 * @param pOrder - Order to retrieve Payment Group
	 * @return - retrieved VirtualPiggy Payment Group
	 */
	public VirtualPiggyPayment getVPPaymentGroupFromOrder(Order pOrder) {
		if (pOrder == null)
			return null;
		List payGroups = pOrder.getPaymentGroups();
		if (payGroups == null || payGroups.isEmpty()) {
			return null;
		}
		for (int i = 0; i < payGroups.size(); i++) {
			PaymentGroupImpl payGrp = (PaymentGroupImpl) payGroups.get(i);
			String payType = (String) payGrp
					.getPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_PAYMENT_TYPE);
			if (VirtualPiggyPaymentConstants.ITEM_DESC_NAME_VIRTUAL_PIGGY_PAYMENT
					.equalsIgnoreCase(payType)) {
				return (VirtualPiggyPayment)payGrp;
			}
		}
		return null;	
	}
	
	/**
	 * Returns Error Message from the Error Map. 
	 * 
	 * @param pErrorMap
	 * @return String - Error Message
	 */

	public String getErrorMessage(Map pErrorMap) {
		String errorMessage = null;
		Iterator<String> iter = pErrorMap.values().iterator();
		while (iter.hasNext()) {
			errorMessage = (String)iter.next();
			break;
		}
		return errorMessage;
	}

	/**
	 * Checks if the status returned in the callback is mapping to a success status, failure status or unknown status.
	 * Success status means the order is approved. Failure status means that the order is rejected.
	 * 
	 * @param pTransStatus callback status.
	 * @return true, if order approved. false, if order is rejected. null, if unknown/error status.
	 */
	private Boolean isOrderTransactionApproved(String pTransStatus){
		if(getSuccessStatuses().contains(pTransStatus)){
			//processed
			return new Boolean(true);
		}else if (getRejectStatuses().contains(pTransStatus)){
			//rejected
			return new Boolean(false);
		}else{
			//error
			return null;
		}
	}

	/**
	 * Gets orderId from request parameters.
	 * 
	 * @param pRequest Dynamo HTTP Servlet request.
	 * @return retrieved orderId.
	 */
	private String getOrderIdFromRequest(DynamoHttpServletRequest pRequest){
		String orderId = null;
		String xmlData = pRequest.getParameter(PARAM_NAME_DATA);
		//if xml data is passed as parameter, try to get order-id from there.
		if(!StringUtils.isEmpty(xmlData)){
			orderId = getOrderIdFromXML(xmlData);
			if(isLoggingDebug()){
				logDebug("order id from data parameter: " + orderId);
			}
		}else{//if xml data is not present, check referenceId parameter.
			orderId = pRequest.getParameter(PARAM_NAME_REFERENCE_ID);
			if(isLoggingDebug()){
				logDebug("order id from reference id parameter: " + orderId);
			}
		}
		return orderId;
	}

	/**
	 * Parese order id from cart xml sent back in the callback parameter.
	 * 
	 * @param pXML cart xml.
	 * @return order-id.
	 */
	private String getOrderIdFromXML(String pXML){
		try{
			if(isLoggingDebug()){
				logDebug("Enter getOrderIdFromXML");
			}
			pXML = URLDecoder.decode(pXML, "UTF-8");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(pXML.getBytes()));
			NodeList nodeList = doc.getElementsByTagName(XML_DATA_ELEMENT_NAME_WITH_ORDER_ID_INFO);
			if(nodeList != null && nodeList.getLength()>0){
				if(isLoggingDebug()){
					logDebug("getOrderIdFromXML-->found xml element:" + XML_DATA_ELEMENT_NAME_WITH_ORDER_ID_INFO);
				}
				NamedNodeMap atts = nodeList.item(0).getAttributes();
				if(atts != null && atts.getLength() > 0){
					Node attShipRef = atts.getNamedItem(XML_DATA_ORDER_ID_ATTRIBUTE_NAME);
					if(attShipRef != null){
						if(isLoggingDebug()){
							logDebug("getOrderIdFromXML-->found order-id xml attribute :" + XML_DATA_ORDER_ID_ATTRIBUTE_NAME);
							logDebug("getOrderIdFromXML-->order-id xml attribute value:" + attShipRef.getNodeValue());
						}
						return attShipRef.getNodeValue();
					}
				}
			}
		}catch(Exception ex){
			if(isLoggingError()){
				logError("getOrderIdFromXML->>" + ex.getMessage());
			}
		}
		return null;
	}


	@Override
	public void doStartService() throws ServiceException {

		super.doStartService();
	}

	public List<String> getSuccessStatuses() {
		return mSuccessStatuses;
	}

	public void setSuccessStatuses(List<String> pSuccessStatuses) {
		this.mSuccessStatuses = pSuccessStatuses;
	}

	public List<String> getRejectStatuses() {
		return mRejectStatuses;
	}

	public void setRejectStatuses(List<String> pRejectStatuses) {
		this.mRejectStatuses = pRejectStatuses;
	}

	public ClientLockManager getClientLockManager() {
		return clientLockManager;
	}

	public void setClientLockManager(ClientLockManager clientLockManager) {
		this.clientLockManager = clientLockManager;
	}

	public PipelineManager getPipelineManager() {
		return pipelineManager;
	}

	public void setPipelineManager(PipelineManager pipelineManager) {
		this.pipelineManager = pipelineManager;
	}

	public OrderManager getOrderManager() {
		return orderManager;
	}

	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}

	public OrderStates getOrderStates() {
		return orderStates;
	}

	public void setOrderStates(OrderStates orderStates) {
		this.orderStates = orderStates;
	}

	public VirtualPiggyServiceConfiguration getVPConfiguration() {
		return VPConfiguration;
	}

	public void setVPConfiguration(VirtualPiggyServiceConfiguration VPConfiguration) {
		this.VPConfiguration = VPConfiguration;
	}

	public PaymentGroupStates getPaymentGroupStates() {
		return paymentGroupStates;
	}

	public void setPaymentGroupStates(PaymentGroupStates paymentGroupStates) {
		this.paymentGroupStates = paymentGroupStates;
	}

	public TransactionManager getTransactionManager() {
		return getPipelineManager().getTransactionManager();
	}

	public TransactionLockFactory getTransactionLockFactory() {
		return mTransactionLockFactory;
	}

	public void setTransactionLockFactory(
			TransactionLockFactory pTransactionLockFactory) {
		this.mTransactionLockFactory = pTransactionLockFactory;
	}

}
