package com.virtualpiggy.integration.services.beans;

public class VPAddressResponseBean  extends VirtualPiggyBaseResponse implements java.io.Serializable {

	private static final long serialVersionUID = 6555718836337832638L;

    private String address;

    private String attentionOf;

    private String city;

    private String country;

    private String name;
    
    private String firstName;
    
    private String lastName;

    private String parentEmail;

    private String parentPhone;

    private String state;

    private String zip;

    public VPAddressResponseBean() {
    }

    public VPAddressResponseBean(
           String address,
           String attentionOf,
           String city,
           String country,
           String errorMessage,
           String name,
           String firstName,
           String lastName,
           String parentEmail,
           String parentPhone,
           String state,
           String status,
           String zip) {
           this.address = address;
           this.attentionOf = attentionOf;
           this.city = city;
           this.country = country;
           setErrorMessage(errorMessage);
           this.name = name;
           this.firstName= firstName;
           this.lastName = lastName;
           this.parentEmail = parentEmail;
           this.parentPhone = parentPhone;
           this.state = state;
           setStatus(status);
           this.zip = zip;
    }


    /**
     * Gets the address value for this VPAddressResponseBean.
     * 
     * @return address
     */
    public String getAddress() {
        return address;
    }


    /**
     * Sets the address value for this VPAddressResponseBean.
     * 
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }


    /**
     * Gets the attentionOf value for this VPAddressResponseBean.
     * 
     * @return attentionOf
     */
    public String getAttentionOf() {
        return attentionOf;
    }


    /**
     * Sets the attentionOf value for this VPAddressResponseBean.
     * 
     * @param attentionOf
     */
    public void setAttentionOf(String attentionOf) {
        this.attentionOf = attentionOf;
    }


    /**
     * Gets the city value for this VPAddressResponseBean.
     * 
     * @return city
     */
    public String getCity() {
        return city;
    }


    /**
     * Sets the city value for this VPAddressResponseBean.
     * 
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }


    /**
     * Gets the country value for this VPAddressResponseBean.
     * 
     * @return country
     */
    public String getCountry() {
        return country;
    }


    /**
     * Sets the country value for this VPAddressResponseBean.
     * 
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the name value for this VPAddressResponseBean.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the name value for this VPAddressResponseBean.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Gets the parentEmail value for this VPAddressResponseBean.
     * 
     * @return parentEmail
     */
    public String getParentEmail() {
        return parentEmail;
    }


    /**
     * Sets the parentEmail value for this VPAddressResponseBean.
     * 
     * @param parentEmail
     */
    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }


    /**
     * Gets the parentPhone value for this VPAddressResponseBean.
     * 
     * @return parentPhone
     */
    public String getParentPhone() {
        return parentPhone;
    }


    /**
     * Sets the parentPhone value for this VPAddressResponseBean.
     * 
     * @param parentPhone
     */
    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }


    /**
     * Gets the state value for this VPAddressResponseBean.
     * 
     * @return state
     */
    public String getState() {
        return state;
    }


    /**
     * Sets the state value for this VPAddressResponseBean.
     * 
     * @param state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the zip value for this VPAddressResponseBean.
     * 
     * @return zip
     */
    public String getZip() {
        return zip;
    }


    /**
     * Sets the zip value for this VPAddressResponseBean.
     * 
     * @param zip
     */
    public void setZip(String zip) {
        this.zip = zip;
    }
    
    public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
