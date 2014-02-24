<%-- 
  This page is an entry point into the shipping process. It determines, whether single or multiple shipping page should be displayed
  and renders it. This page also adds cart items stored in the UserItems component for the newly registered users.

  Required parameters:
    None.

  Optional parameters:
    None.
--%>

<dsp:page>
  <dsp:importbean bean="/atg/commerce/order/purchase/ShippingGroupFormHandler"/>
  <dsp:importbean bean="/atg/userprofiling/Profile"/>
  <dsp:importbean bean="/com/virtualpiggy/integration/purchase/VirtualPiggyShippingInfoDroplet"/>
  <dsp:importbean bean="/atg/commerce/util/MapToArrayDefaultFirst"/>
  <dsp:importbean bean="/atg/store/states/CheckoutProgressStates" var="checkoutProgressStates"/>
  <dsp:importbean bean="/atg/commerce/order/purchase/ShippingGroupContainerService"/>
  <dsp:importbean bean="/atg/store/order/purchase/CheckoutOptionSelections"/>

  <%-- 
    If the order has any hard goods shipping groups, figure out which 
    hard goods page to use.
  --%>
  <dsp:getvalueof var="anyHardgoodShippingGroups" vartype="java.lang.String"
                  bean="ShippingGroupFormHandler.anyHardgoodShippingGroups"/>

  <%-- 
    Will be true if the order has more than one hardgood shipping group
    with commerce item relationships.
   --%>                  
  <dsp:getvalueof var="isMultipleHardgoodShippingGroups"
                  bean="ShippingGroupFormHandler.multipleHardgoodShippingGroupsWithRelationships"/>                  

  <c:choose>
    <c:when test='${anyHardgoodShippingGroups}'>
      <%-- 
        If the order has more than one hard goods shipping group, go to the
        multi shipping group page. Otherwise, single shipping group page.
      --%>
	  <dsp:getvalueof var="isVPChkout" bean="Profile.isVirtualPiggyCheckout"/>
	  <dsp:getvalueof var="vpParentChild" bean="Profile.virtualPiggyUserType"/>
	  <c:choose>
        <c:when test='${isVPChkout}'>
		    <crs:pageContainer divId="atg_store_cart"
				 index="false" 
				 follow="false"
				 levelNeeded="SHIPPING"
				 redirectURL="../cart/cart.jsp" bodyClass="atg_store_pageShipping atg_store_checkout atg_store_rightCol">
			<fmt:message key="checkout_title.checkout" var="title"/>
			<crs:checkoutContainer currentStage="shipping"
						   title="${title}">
			<div class="atg_store_main">
			<%-- show form errors --%>
			<dsp:getvalueof var="formExceptions" vartype="java.lang.Object" bean="ShippingGroupFormHandler.formExceptions"/>
			<c:if test="${not empty formExceptions}">
				<c:forEach var="formException" items="${formExceptions}">
					<div style="text-align:center; font-weight:bold; font-size:11px;background-color:#ff9900;color:#ffffff;">
						<%--${formException.message}--%>
						<br>
						Internal Error associating Virtual Piggy Shipping Group with Order. 
					</div>
				</c:forEach>
			</c:if>	

			<div class="atg_store_generalMessage">
		    
		    <dsp:droplet name="VirtualPiggyShippingInfoDroplet">
			  <dsp:param name="clearShippingInfos" value="true"/>
			  <dsp:param name="clearShippingGroups" value="true"/>
			  <dsp:param name="shippingGroupTypes" value="hardgoodShippingGroup"/>
			  <dsp:param name="initShippingGroups" value="true"/>
			  <dsp:param name="initShippingInfos" value="true"/>
			  
		      <dsp:oparam name="output">
				<h3>
			     Your items will be shipped to address below:
				</h3>
				  <ul align="left" style="width:100%; margin-top:10px; padding-left:3%;">
				
			  <dsp:getvalueof var="defShippingGroupName" bean="ShippingGroupContainerService.defaultShippingGroupName"/>
				<dsp:droplet name="MapToArrayDefaultFirst">
				  <dsp:param name="map" param="shippingGroups"/>
				  <dsp:param name="defaultKey" value="${defShippingGroupName}"/>
				  <dsp:oparam name="output">
				    <dsp:getvalueof var="sortedArray" vartype="java.lang.Object" param="sortedArray"/>
					<c:forEach var="shippingAddressVar" items="${sortedArray}">
                      <dsp:getvalueof var="shippingGroup" vartype="java.lang.Object" value="${shippingAddressVar}"/>
                      <dsp:getvalueof var="shippingGroupValue" value="${shippingGroup.value.shippingAddress}"/>
					  <li>
					    <label> Name : <dsp:valueof value="${shippingGroupValue.firstName}"/> <dsp:valueof value="${shippingGroupValue.lastName}"/></label>
					  </li>
					  <li>&nbsp;</li>
					  <li>
					    <label> <dsp:valueof value="${shippingGroupValue.address1}"/> </label>
					  </li>
					  <li>
					    <label> <dsp:valueof value="${shippingGroupValue.address2}"/> </label>
					  </li>
					  <li>
					    <label> <dsp:valueof value="${shippingGroupValue.city}"/>, <label> <dsp:valueof value="${shippingGroupValue.state}"/> <dsp:valueof value="${shippingGroupValue.postalCode}"/></label>
					  </li>
					  <li>
						 <dsp:valueof value="${shippingGroupValue.country}"/>
					  </li>
					  <li>&nbsp;</li>
					   <li>
					    <label> Phone : <dsp:valueof value="${shippingGroupValue.phoneNumber}"/> </label>
					  </li>
					  <li>&nbsp;</li>
					  <li>
					    <label> Email : <dsp:valueof bean="Profile.email"/> </label>
					  </li>
					</c:forEach>
				  </dsp:oparam>
				  </dsp:droplet>
				  </ul>
				  <br>
				<dsp:form id="atg_store_checkoutShippingAddress" iclass="atg_store_checkoutOption" formid="atg_store_checkoutShippingAddress"
				   action="${pageContext.request.requestURI}" method="post">
				   <c:choose>
				     <c:when test='${vpParentChild == "Parent"}'>
					   <dsp:input bean="ShippingGroupFormHandler.applyShippingGroupsSuccessURL" type="hidden" value="shippingMethod.jsp"/>
					 </c:when>
					 <c:otherwise>
					   <dsp:input bean="ShippingGroupFormHandler.applyShippingGroupsSuccessURL" type="hidden" value="billing.jsp"/>
					 </c:otherwise>
				   </c:choose>
				  
				  <dsp:input bean="ShippingGroupFormHandler.applyShippingGroupsErrorURL" type="hidden" beanvalue="/OriginatingRequest.requestURI"/>
				  <div class="atg_store_formFooter">
					<div class="atg_store_formActions">
					  <span class="atg_store_basicButton">
			          <dsp:input bean="ShippingGroupFormHandler.applyShippingGroups" id="atg_store_shippingButton" type="submit" value="Continue"/>
					  </span>
					</div>
				  </div>
				</dsp:form>
				
				<dsp:setvalue bean="CheckoutProgressStates.currentLevel" value="BILLING" />
			  
			  </dsp:oparam>
			  <dsp:oparam name="error">
			    <h3>
			      Error Associating Shipping information for Virtual Piggy Profile. Please <a href="shipping.jsp">click here</a> to try again.
			    </h3>
			  </dsp:oparam>
		    </dsp:droplet>
		    </div>
			</div>
		    </crs:checkoutContainer>
			<%-- Order Summary --%>
			<dsp:include page="/checkout/gadgets/checkoutOrderSummary.jsp">
			  <dsp:param name="order" bean="/atg/commerce/ShoppingCart.current"/>
			  <dsp:param name="currentStage" value="shipping"/>
			</dsp:include>
			
		    </crs:pageContainer>
		  </c:when>
		<c:otherwise>
      
	      <c:choose>
        	<%-- 
          	show multi shipping groups page 
          	if we have more than one gift shipping group 
          	or at least one gift shipping group and one and more non-gift 
          	shipping groups
        	--%>
        	<c:when test='${isMultipleHardgoodShippingGroups}'>
          	<dsp:include page="shippingMultiple.jsp">
            	<dsp:param name="init" value="true"/>
          	</dsp:include>
        	</c:when>
        	<%-- Single shipping group? Then we'll display single shipping page. --%>
        	<c:otherwise>
          	<dsp:include page="shippingSingle.jsp">
            	<dsp:param name="init" value="true"/>
          	</dsp:include>
        	</c:otherwise>
    	  </c:choose>
   		</c:otherwise>
	  </c:choose>
      
    </c:when>
  </c:choose>

</dsp:page>
<%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/10.1.2/Storefront/j2ee/store.war/checkout/shipping.jsp#1 $$Change: 713790 $ --%>
