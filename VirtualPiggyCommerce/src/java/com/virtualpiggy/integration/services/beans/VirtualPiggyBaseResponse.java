package com.virtualpiggy.integration.services.beans;

public class VirtualPiggyBaseResponse  implements java.io.Serializable {

	private static final long serialVersionUID = -4532022971071431732L;

    private String errorMessage;
    private String status;
    private boolean bStatus;
    
    public VirtualPiggyBaseResponse() {
    }

    public VirtualPiggyBaseResponse(
           String errorMessage,
           String status) {
           this.errorMessage = errorMessage;
           this.status = status;
    }

    /**
     * Gets the errorMessage value for this VirtualPiggyBaseResponse.
     * 
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }


    /**
     * Sets the errorMessage value for this VirtualPiggyBaseResponse.
     * 
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the status value for this VirtualPiggyBaseResponse.
     * 
     * @return status
     */
    public boolean getBooleanStatus() {
        return bStatus;
    }


    /**
     * Sets the status value for this VirtualPiggyBaseResponse.
     * 
     * @param status
     */
    public void setBooleanStatus(boolean status) {
        this.bStatus = status;
    }

    /**
     * Gets the status value for this VirtualPiggyBaseResponse.
     * 
     * @return status
     */
    public String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this VirtualPiggyBaseResponse.
     * 
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
