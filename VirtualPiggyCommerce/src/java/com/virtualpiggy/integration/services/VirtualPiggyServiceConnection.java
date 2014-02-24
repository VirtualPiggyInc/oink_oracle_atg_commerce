package com.virtualpiggy.integration.services;

import java.net.URL;

import org.apache.axis.message.SOAPHeaderElement;

import com.virtualpiggy.stub.BasicHttpBinding_ITransactionServiceStub;
import com.virtualpiggy.stub.ITransactionService;
import com.virtualpiggy.stub.TransactionService;
import com.virtualpiggy.stub.TransactionServiceLocator;

import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;

/**
 * This class manages connection/communication with Virtual Piggy Web Services.
 * Client Code should use this class to get a connection to Virtual Piggy web services.
 * 
 * @author Tarun
 *
 */
public class VirtualPiggyServiceConnection extends GenericService {
	private transient ITransactionService trSvcStub = null;
	
	private static final String MSG_VP_SERVICE_STARTED = "Virtual Piggy Transaction Service Started.";
	private static final String ERR_MSG_VP_SERVICE_INIT_TR_SERVICE = "Error Initializing Virtual Piggy Transaction Service.";
	
	public ITransactionService getVPTransactionServiceClient(){
		return trSvcStub;
	}
	
	@Override
	public void doStartService() throws ServiceException {
		super.doStartService();
		
		trSvcStub = null;
		initializeVPTransactionServiceClient();
		if(isTrSvcInitialized()){
			if(isLoggingInfo()){
				logInfo(MSG_VP_SERVICE_STARTED);
			}
		}
		
	}
	
	public boolean isTrSvcInitialized(){
		return trSvcStub != null ? true : false;
	}
	
	private ITransactionService initializeVPTransactionServiceClient(){
		try{
			TransactionService service = new TransactionServiceLocator();
			URL endPoint = new URL(getConfiguration().getTrSrvcServerURL());
			if(isLoggingDebug()) logDebug("VP Transaction Service URL: " + getConfiguration().getTrSrvcServerURL());
			ITransactionService svcStub = service.getBasicHttpBinding_ITransactionService(endPoint);
			setHeaders(svcStub);
			trSvcStub = svcStub;
			return svcStub;
		}catch(Exception ex){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_INIT_TR_SERVICE + "\n" + ex.getMessage());
			}
		}
		return null;
	}
	
	private ITransactionService setHeaders(ITransactionService pSvcStub){
		SOAPHeaderElement merchID = new SOAPHeaderElement("vp", "MerchantIdentifier", getConfiguration().getMerchantId());
		SOAPHeaderElement apiKey = new SOAPHeaderElement("vp", "APIkey", getConfiguration().getAPIkey());
		((BasicHttpBinding_ITransactionServiceStub)pSvcStub).setHeader(merchID);
		((BasicHttpBinding_ITransactionServiceStub)pSvcStub).setHeader(apiKey);
		return pSvcStub;
	}

	private VirtualPiggyServiceConfiguration mVPSvcConfig;

	public VirtualPiggyServiceConfiguration getConfiguration() {
		return mVPSvcConfig;
	}

	public void setConfiguration(VirtualPiggyServiceConfiguration pVPSvcConfig) {
		this.mVPSvcConfig = pVPSvcConfig;
	}

}
