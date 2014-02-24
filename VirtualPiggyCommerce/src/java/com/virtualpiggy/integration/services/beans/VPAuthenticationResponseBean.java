package com.virtualpiggy.integration.services.beans;

public class VPAuthenticationResponseBean  extends VirtualPiggyAccountInfo implements java.io.Serializable {
	private static final long serialVersionUID = -1116280104167445304L;

	private String errorMessage;

    private Boolean status;

    public VPAuthenticationResponseBean() {
    }

    public VPAuthenticationResponseBean(
           String errorMessage,
           Boolean status,
           String token,
           String userType) {
           this.errorMessage = errorMessage;
           this.status = status;
           setVpToken(token);
           setUserType(userType);
    }


    /**
     * Gets the errorMessage value.
     * 
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }


    /**
     * Sets the errorMessage value.
     * 
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    /**
     * Gets the status value.
     * 
     * @return status
     */
    public Boolean getStatus() {
        return status;
    }


    /**
     * Sets the status value.
     * 
     * @param status
     */
    public void setStatus(Boolean status) {
        this.status = status;
    }

}
