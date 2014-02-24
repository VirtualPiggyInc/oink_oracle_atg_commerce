package com.virtualpiggy.integration.order.payment.processor;

import com.virtualpiggy.integration.order.payment.VirtualPiggyPayment;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentInfo;
import com.virtualpiggy.integration.purchase.VirtualPiggyPurchaseProcessHelper;

import atg.commerce.order.Order;
import atg.commerce.payment.PaymentManagerPipelineArgs;
import atg.nucleus.GenericService;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

public class ProcCreateVirtualPiggyPaymentInfo extends GenericService implements
		PipelineProcessor {

	private VirtualPiggyPurchaseProcessHelper mVpPurchaseProcessHelper;

	/** Return value for this processor. **/
	public static final int SUCCESS = 1;

	// property: VirtualPiggyPaymentInfoClass
	String mVirtualPiggyPaymentInfoClass = "com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentInfo";
	
	/**
	* Return the class to instantiate when creating a new VirtualPiggyPaymentInfo
	* object.
	**/
	public String getVirtualPiggyPaymentInfoClass() {
		return mVirtualPiggyPaymentInfoClass;
	}

	/**
	* Specify the class to instantiate when creating a new VirtualPiggyPaymentInfo
	* object. If the <code>VirtualPiggyPaymentInfo</code> class is extended to
	* include more information, this property can be changed to reflect the
	* new class.
	**/
	public void setVirtualPiggyPaymentInfoClass(
			String pVirtualPiggyPaymentInfoClass) {
		this.mVirtualPiggyPaymentInfoClass = pVirtualPiggyPaymentInfoClass;
	}
	
	/**
	* This method populates the <code>VirtualPiggyPaymentInfo</code> object with
	* data. 
	*
	* @param pOrder The Order object.
	* @param pPaymentGroup The virtual piggy payment group being processed.
	* @param pAmount The amount being authorized, debited, or credited
	* @param pParams The parameter dictionary passed to this pipeline processor
	* @param pVirtualPiggyPaymentInfo VirtualPiggyPaymentInfo object to hold request 
	* information for VirtualPiggyPaymentProcessor.
	**/
	protected void addDataToVirtualPiggyPaymentInfo(Order pOrder,
	VirtualPiggyPayment pPaymentGroup, double pAmount,
	PaymentManagerPipelineArgs pParams, VirtualPiggyPaymentInfo
	pVirtualPiggyPaymentInfo){
		pVirtualPiggyPaymentInfo.setAmount(pAmount);
		String escXml = getVpPurchaseProcessHelper().convertOrderToVirtualPiggyXML(pOrder);
		if(isLoggingDebug()){
			logDebug("Converted Order to Virtual Piggy XML : \\n" + escXml);
		}
		pVirtualPiggyPaymentInfo.setCheckoutData(escXml);
		pVirtualPiggyPaymentInfo.setVpChildId(pPaymentGroup.getVpChildId());
		pVirtualPiggyPaymentInfo.setVpPaymentAccId(pPaymentGroup.getVpPaymentAccId());
		pVirtualPiggyPaymentInfo.setVpToken(pPaymentGroup.getVpToken());
		pVirtualPiggyPaymentInfo.setVpTransDesc(pPaymentGroup.getVpTransDesc());
	}
	
	/** Factory method to create a new VirtualPiggyPaymentInfo object. The class
	* that is created is that specified by the <code>virtualPiggyPaymentInfoClass</code> 
	* property, and must be a subclass of <code>com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentInfo</code>
	*
	* @return An object of the class specified by <code>virtualPiggyPaymentInfoClass</code>
	* @throws Exception if any instantiation error occurs when creating the info object
	**/
	protected VirtualPiggyPaymentInfo getVirtualPiggyPaymentInfo() throws Exception {
		if (isLoggingDebug())
			logDebug("Making a new instance of type: " + getVirtualPiggyPaymentInfoClass());
		VirtualPiggyPaymentInfo vpi = (VirtualPiggyPaymentInfo)	Class.forName(getVirtualPiggyPaymentInfoClass()).newInstance();
		return vpi;
	}

	@Override
	public int[] getRetCodes() {
		int retCodes[] = {SUCCESS};
		return retCodes;
	}

	/**
	* Generate a VirtualPiggyPaymentInfo object of the class specified by <code>VirtualPiggyPaymentInfoClass</code>, 
	* populate it with data from a <code>VirtualPiggyPayment</code> payment group and add it to the pipeline argument
	* dictionary for downstream pipeline processors can access it.
	*
	* @param pParam Parameter dictionary of type PaymentManagerPipelineArgs.
	* @param pResult Pipeline result object, not used by this method.
	* @return An integer value used to determine which pipeline processor is called next.
	* @throws Exception If any error occurs creating or populating the VirtualPiggyPaymentInfo object.
	**/
	@Override
	public int runProcess(Object pParam, PipelineResult pResult) throws Exception {
		if (isLoggingDebug())
			logDebug("procCreateVirtualPiggyPaymentInfo pipeline processor invoked..." );
		
		PaymentManagerPipelineArgs params =	(PaymentManagerPipelineArgs)pParam;
		Order order = params.getOrder();
		VirtualPiggyPayment vpPayment = (VirtualPiggyPayment)params.getPaymentGroup();
		double amount = params.getAmount();
		
		// create and populate VirtualPiggyPaymentInfo info class
		VirtualPiggyPaymentInfo vpi = getVirtualPiggyPaymentInfo();
		addDataToVirtualPiggyPaymentInfo(order, vpPayment, amount, params, vpi);
		if (isLoggingDebug())
			logDebug("Putting VirtualPiggyPaymentInfo object into pipeline: " +	vpi.toString());
		
		params.setPaymentInfo(vpi);
		return SUCCESS;
		
	}

	public VirtualPiggyPurchaseProcessHelper getVpPurchaseProcessHelper() {
		return mVpPurchaseProcessHelper;
	}

	public void setVpPurchaseProcessHelper(
			VirtualPiggyPurchaseProcessHelper pVpPurchaseProcessHelper) {
		this.mVpPurchaseProcessHelper = pVpPurchaseProcessHelper;
	}

}
