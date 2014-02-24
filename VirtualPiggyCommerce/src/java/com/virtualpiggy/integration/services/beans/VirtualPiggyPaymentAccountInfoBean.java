/**
 * VirtualPiggyPaymentAccountInfoBean.java
 *
 */

package com.virtualpiggy.integration.services.beans;

public class VirtualPiggyPaymentAccountInfoBean  implements java.io.Serializable {
 
	private static final long serialVersionUID = -6502458234320260712L;

	private String identifier;

    private String name;

    private String type;

    private String url;

    public VirtualPiggyPaymentAccountInfoBean() {
    }

    public VirtualPiggyPaymentAccountInfoBean(
           String pIdentifier,
           String pName,
           String pType,
           String pUrl) {
           this.identifier = pIdentifier;
           this.name = pName;
           this.type = pType;
           this.url = pUrl;
    }


    /**
     * Gets the identifier value for this VirtualPiggyPaymentAccountInfoBean.
     * 
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }


    /**
     * Sets the identifier value for this VirtualPiggyPaymentAccountInfoBean.
     * 
     * @param pIdentifier
     */
    public void setIdentifier(String pIdentifier) {
        this.identifier = pIdentifier;
    }


    /**
     * Gets the name value for this VirtualPiggyPaymentAccountInfoBean.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the name value for this VirtualPiggyPaymentAccountInfoBean.
     * 
     * @param pName
     */
    public void setName(String pName) {
        this.name = pName;
    }


    /**
     * Gets the type value for this VirtualPiggyPaymentAccountInfoBean.
     * 
     * @return type
     */
    public String getType() {
        return type;
    }


    /**
     * Sets the type value for this VirtualPiggyPaymentAccountInfoBean.
     * 
     * @param pType
     */
    public void setType(String pType) {
        this.type = pType;
    }


    /**
     * Gets the url value for this VirtualPiggyPaymentAccountInfoBean.
     * 
     * @return url
     */
    public String getUrl() {
        return url;
    }


    /**
     * Sets the url value for this VirtualPiggyPaymentAccountInfoBean.
     * 
     * @param pUrl
     */
    public void setUrl(String pUrl) {
        this.url = pUrl;
    }

}
