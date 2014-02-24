package com.virtualpiggy.integration.services.beans;

public class VirtualPiggyProcessTransactionResponse extends VirtualPiggyBaseResponse{

	private static final long serialVersionUID = -7777080637491641279L;

	private String dataXml;

    private String transactionIdentifier;

    private String transactionStatus;

    public VirtualPiggyProcessTransactionResponse() {
    	super();
    }

    /**
     * Gets the dataXml value for this VirtualPiggyProcessTransactionResponse.
     * 
     * @return dataXml
     */
    public String getDataXml() {
        return dataXml;
    }


    /**
     * Sets the dataXml value for this VirtualPiggyProcessTransactionResponse.
     * 
     * @param dataXml
     */
    public void setDataXml(String dataXml) {
        this.dataXml = dataXml;
    }

    /**
     * Gets the transactionIdentifier value for this VirtualPiggyProcessTransactionResponse.
     * 
     * @return transactionIdentifier
     */
    public String getTransactionIdentifier() {
        return transactionIdentifier;
    }


    /**
     * Sets the transactionIdentifier value for this VirtualPiggyProcessTransactionResponse.
     * 
     * @param transactionIdentifier
     */
    public void setTransactionIdentifier(String transactionIdentifier) {
        this.transactionIdentifier = transactionIdentifier;
    }


    /**
     * Gets the transactionStatus value for this VirtualPiggyProcessTransactionResponse.
     * 
     * @return transactionStatus
     */
    public String getTransactionStatus() {
        return transactionStatus;
    }


    /**
     * Sets the transactionStatus value for this VirtualPiggyProcessTransactionResponse.
     * 
     * @param transactionStatus
     */
    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

}
