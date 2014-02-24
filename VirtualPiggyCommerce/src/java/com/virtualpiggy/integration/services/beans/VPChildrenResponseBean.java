package com.virtualpiggy.integration.services.beans;

public class VPChildrenResponseBean  implements java.io.Serializable {
  
	private static final long serialVersionUID = -647458444874062773L;

	private String mIdentifier;

    private String mName;

    private String mType;

    public VPChildrenResponseBean() {
    }

    public VPChildrenResponseBean(
           String pIdentifier,
           String pName,
           String pType) {
           this.mIdentifier = pIdentifier;
           this.mName = pName;
           this.mType = pType;
    }


    /**
     * Gets the identifier value for this VPChildrenResponseBean.
     * 
     * @return identifier
     */
    public String getIdentifier() {
        return mIdentifier;
    }


    /**
     * Sets the identifier value for this VPChildrenResponseBean.
     * 
     * @param identifier
     */
    public void setIdentifier(String identifier) {
        this.mIdentifier = identifier;
    }


    /**
     * Gets the name value for this VPChildrenResponseBean.
     * 
     * @return name
     */
    public String getName() {
        return mName;
    }


    /**
     * Sets the name value for this VPChildrenResponseBean.
     * 
     * @param name
     */
    public void setName(String name) {
        this.mName = name;
    }


    /**
     * Gets the type value for this VPChildrenResponseBean.
     * 
     * @return type
     */
    public String getType() {
        return mType;
    }


    /**
     * Sets the type value for this VPChildrenResponseBean.
     * 
     * @param type
     */
    public void setType(String type) {
        this.mType = type;
    }

}
